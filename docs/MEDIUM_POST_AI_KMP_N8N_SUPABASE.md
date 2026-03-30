# How I Built an AI-Powered Mobile App with KMP, n8n, and Supabase

*Vector embeddings, webhook orchestration, and real-time sync — lessons from shipping a cross-platform app with AI at
its core*

---

When I started building a mobile app that needed AI-powered duplicate detection, I assumed the hard part would be the
machine learning. I was wrong. The real challenge was orchestrating five different services — a KMP mobile app,
Supabase, n8n, Google Vertex AI, and Firebase — and making them work together without the user noticing the complexity
behind a simple "Submit" button.

The app lets citizens build their own political program democratically — proposal by proposal, beyond party lines.
Instead of choosing a single party's full agenda, users create and vote on individual policy proposals across categories
like economy, healthcare, education, or justice. The result is a crowdsourced electoral program that reflects what
people actually want, not what any single party offers. The technical integration behind this deceptively simple concept
turned out to be a rich learning experience.

Here's what I learned building it.

## The Stack

| Layer            | Technology                                   | Role                                       |
|------------------|----------------------------------------------|--------------------------------------------|
| Mobile           | Kotlin Multiplatform + Compose Multiplatform | Shared UI for Android & iOS                |
| Backend          | Supabase (PostgreSQL + Auth + Realtime)      | Database, authentication, WebSocket sync   |
| Vector Search    | Supabase pgvector extension                  | Semantic similarity matching               |
| AI Orchestration | n8n (self-hosted)                            | Workflow automation, webhook processing    |
| Embeddings       | Google Vertex AI                             | Text-to-vector conversion (768 dimensions) |
| Text Generation  | OpenAI GPT (via n8n)                         | Auto-generated proposal summaries          |
| Notifications    | Firebase Cloud Functions + FCM               | Push notifications across platforms        |
| Development      | Claude Code                                  | AI-assisted pair programming               |

The idea behind this combination was to avoid building a custom backend. Supabase handles the database and auth, n8n
handles the AI logic, and Firebase handles notifications. The mobile app just talks to these services directly. No
Express server, no Docker containers to maintain, no deployment pipelines for a backend I didn't need.

## The Architecture

When a user submits a proposal, here's what actually happens:

```
User taps "Submit" in KMP app
  → Ktor HTTP POST to n8n webhook (with Supabase JWT token)
  → n8n validates JWT
  → n8n calls Google Vertex AI to generate a 768-dimensional embedding
  → n8n calls Supabase RPC to find similar proposals (vector search)
  → If similarity > 0.85: return duplicates to the app
  → If unique: GPT generates a short summary, proposal is inserted into DB
  → Supabase INSERT trigger fires a webhook to Firebase Cloud Function
  → Firebase sends push notification to all subscribed devices
```

The heart of the vector search is a PostgreSQL function using the `pgvector` extension. The cosine distance operator
`<=>` does the heavy lifting:

```sql
CREATE FUNCTION match_documents(
  query_embedding vector,
  match_count integer DEFAULT 5
)
RETURNS TABLE(id uuid, content text, metadata jsonb, similarity double precision)
AS $$
  SELECT pe.id, pe.content, pe.metadata,
         1 - (pe.embedding <=> query_embedding) AS similarity
  FROM proposal_embeddings pe
  WHERE pe.embedding IS NOT NULL
  ORDER BY pe.embedding <=> query_embedding
  LIMIT match_count;
$$;
```

This runs entirely inside PostgreSQL. No external vector database needed. Supabase gives you `pgvector` out of the box,
and for a dataset of a few thousand proposals, the performance is more than sufficient.

## n8n as the AI Orchestrator

I chose n8n over building the AI pipeline directly in the app (or in a custom API) for one reason: iteration speed. When
you're experimenting with embedding models, similarity thresholds, and prompt engineering, being able to drag nodes
around in a visual editor and test them individually is invaluable.

The production workflow (`EspañaDecide-PRO`) has 17 nodes. The core path looks like this:

**Webhook** → **JWT Validator** → **Combine title + description** → **Generate Embedding (HTTP Request)** → **Vector
Search (HTTP Request)** → **Evaluate Duplicates (JavaScript)** → **If should create?** → **GPT: generate summary** → *
*Insert to Supabase** → **Store embedding** → **Respond to webhook**

One thing worth mentioning: I originally used n8n's built-in Supabase Vector Store node (from the LangChain integration)
for the vector search step. It worked fine in testing, but in production it started returning empty results
intermittently. The problem was the LangChain node wrapper — it doesn't support "Always Output Data", "Retry On Fail",
or "On Error" options, so when it silently failed, the entire workflow stopped with no recovery path. I would have loved
for these nodes to be production-ready, but after struggling to get logs and debug the issue, I replaced them with plain
HTTP Request nodes calling the Google Vertex AI and Supabase RPC APIs directly. The intermittent failures disappeared
immediately. Sometimes the less magical option is the right one.

The JavaScript node that evaluates duplicates is where the business logic lives:

```javascript
const items = $input.all();
const forceCreation = $('Combine text').first().json
  .originalRequest.forceCreation;

const duplicates = items.filter(item => item.json.similarity > 0.85);
const shouldCreate = forceCreation || duplicates.length === 0;

const formattedDuplicates = duplicates.map(d => ({
  id: d.json.metadata.proposal_id,
  title: d.json.content.split(' - ')[0],
  similarity: Math.round(d.json.similarity * 100) / 100,
  votesCount: d.json.metadata.votes_count || 0
}));

return [{ json: { shouldCreate, duplicates: formattedDuplicates } }];
```

The `forceCreation` flag is important. When the app detects duplicates, it shows them to the user with their current
vote counts. The user can then choose to vote on an existing proposal or create theirs anyway. This keeps the user in
control while still preventing accidental duplicates.

On the KMP side, the webhook client authenticates with the user's Supabase JWT token:

```kotlin
class N8nWebhookClient(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository
) {
    suspend fun processProposal(
        request: ProposalProcessingRequest
    ): Result<ProposalProcessingResponse> {
        val accessToken = authRepository.getAccessToken()
            ?: return Result.failure(
                N8nWebhookException.Unauthorized("No active session")
            )

        val response = httpClient.post(WEBHOOK_URL) {
            contentType(ContentType.Application.Json)
            bearerAuth(accessToken)
            setBody(request)
        }
        // ...
    }
}
```

This means n8n never needs its own authentication system. It just validates the Supabase JWT (configured with ES256 and
Supabase's public key) and extracts the user ID from the token claims.

## Real-Time Sync with Supabase Realtime

Vote counts in the app update live across all connected clients via WebSocket. When someone upvotes a proposal on their
phone, every other user seeing that proposal gets the updated count instantly.

The implementation uses Supabase Realtime with Kotlin's `callbackFlow`:

```kotlin
val channelId = "proposals_${Random.nextLong()}"
val channel = supabase.channel(channelId)

val proposalChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "proposals"
}
val voteChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
    table = "proposal_votes"
}

channel.subscribe()
// Emit initial data, then re-fetch on every change
merge(proposalChanges, voteChanges).collect {
    send(fetchProposalsWithVotes())
}
awaitClose { launch { channel.unsubscribe() } }
```

Notice the `Random.nextLong()` in the channel ID. That's there because of a bug I hit: Supabase throws "cannot call
postgresChangeFlow after joining" if you reuse a channel ID. This happens when the user navigates away from a screen and
comes back — the old channel is closed but the ID is "taken." Randomizing the ID sidesteps this entirely.

## Push Notifications: The Webhook Chain

The notification pipeline chains three services together:

1. A Supabase **database trigger** fires on every INSERT to the proposals table
2. It sends an HTTP webhook to a **Firebase Cloud Function**
3. The Cloud Function sends a push notification via **FCM** to all subscribed devices

One problem I hit late in development: dev notifications were appearing on production devices. Both environments
subscribed to the same `new_proposals` FCM topic.

The fix was environment-specific topics. The app subscribes to `new_proposals_dev` or `new_proposals_prod` based on
build flavor, and the Firebase function routes to the correct topic via a query parameter (`?env=prod`). The Supabase
trigger passes this parameter in its webhook URL configuration.

The notification body uses the `short_description` field — a one-sentence summary generated by GPT during the n8n
workflow. If that field is empty (edge case), it falls back to the first sentence of the full description.

## AI-Assisted Development with Claude Code

There's a meta aspect to this project: I used AI to build an app that uses AI. Claude Code was my pair programming
partner throughout the development, and the key enabler was **MCP (Model Context Protocol) integration**.

By connecting Claude Code to Supabase, n8n, GitHub, and Jira via MCP servers, the AI had full visibility into every
layer of the project — not just the code in front of it. It could query production tables, inspect n8n workflow nodes,
read Jira tickets for context on what needed to be built, and manage the GitHub repository. This wasn't just
autocomplete; it was a collaborator that understood the database schema, the workflow architecture, and the project
management context simultaneously.

Concrete examples: writing the idempotent Supabase migration SQL with triggers and RLS policies, debugging the n8n
LangChain node issue by cross-referencing workflow execution data with database state, cleaning up test proposals
directly in the production database through MCP tools, and generating documentation for Jira tickets based on what was
actually implemented in the code.

Where human judgment was still essential: architectural decisions about how services should communicate, choosing
similarity thresholds, and understanding the UX implications of showing duplicate proposals to users. The AI is
excellent at implementation once you know *what* to build, but the *what* still requires understanding the domain.

## What I'd Do Differently

**Use HTTP Request nodes in n8n from the start.** I wish the Supabase and LangChain nodes had been production-ready, but
after the debugging headaches and the lack of proper error handling options, plain HTTP requests turned out to be more
transparent, configurable, and reliable.

**Set up environment isolation earlier.** I added dev/prod separation as an afterthought, and retrofitting FCM topics,
separate Supabase projects, and different n8n webhook paths was more work than it would have been upfront.

**Add end-to-end tests for the webhook chain.** The pipeline from mobile app → n8n → Supabase → Firebase has a lot of
surface area for subtle failures. I tested each piece individually, but never the full chain in an automated way.

## Wrapping Up

What I value most about this project isn't any single integration — it's having taken an idea through every phase of the
software lifecycle: from initial concept and definition, through architecture design, implementation of a cross-platform
app with AI capabilities, integration of five different services, environment management with dev/prod isolation, all
the way to deployment on the Google Play Store with real users. Each phase came with its own set of problems that
required different skills — from writing PostgreSQL functions to debugging silent n8n node failures to configuring
Firebase push notifications for both Android and iOS.

The takeaway is that the hard part of building modern apps is rarely any individual technology. It's the orchestration —
making all the pieces talk to each other reliably, handling the edge cases between services, and keeping the user
experience seamless despite the complexity underneath.

The full source code is available on [GitHub](https://github.com/apptolast/SpainDecides). The app is live on
the [Google Play Store](https://play.google.com/store/apps/details?id=com.apptolast.spaindecides).

If you're building something similar — or found a better way to handle any of these integrations — I'd genuinely like to
hear about it.
