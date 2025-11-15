# LinkedIn Post — Spain Decides Launch

## Post body (copiar y pegar en LinkedIn)

I spent the last few months building a mobile app from scratch — from the first line of code to the Google Play Store.
🚀

The idea: a platform where citizens can create and vote on policy proposals, one by one, building a democratic political
program together. No party affiliations, no ideology filters. Just people voting on what matters to them.

The real challenge was the tech behind it.

I built it as a solo developer using Kotlin Multiplatform, sharing a single codebase for Android and iOS. The backend
runs on Supabase (PostgreSQL + real-time WebSockets + auth). And the part I'm most proud of: an AI pipeline orchestrated
through n8n that detects duplicate proposals using vector embeddings before they're created.

The full journey:

🏗️ Concept definition and UX design
📐 MVVM architecture with Compose Multiplatform
🗄️ Supabase integration: database, auth (email + Google OAuth), and real-time sync
🤖 AI workflow: Google Vertex AI generates embeddings, Supabase pgvector searches for semantic duplicates, and OpenAI
generates summaries
🔔 Push notifications via Firebase Cloud Functions triggered by database webhooks
🔀 Environment isolation (dev/prod) across every service
📦 Production deployment on Google Play Store

Every phase came with its own set of problems. Debugging silent failures in n8n nodes that lacked proper error handling.
Fixing WebSocket channel conflicts in Supabase Realtime. Preventing dev notifications from reaching production users.
Each one forced me to understand the system deeper than I planned to.

Shoutout to @Pablo Hurtado Gonzalo for his help throughout the process — having someone to bounce ideas off and test
with made a real difference.

📝 If you want to see the technical deep-dive, I wrote a detailed Medium post covering the architecture, the n8n
workflow, and the lessons learned (link in the first comment).

The app is called Spain Decides and it's live on Google Play. I'd love for you to try it.

What's been the hardest integration challenge in your projects? 👇

#KotlinMultiplatform #Supabase #n8n #MobileDevelopment #AppLaunch

---

## First comment (publicar inmediatamente despues del post)

📝 Technical deep-dive on Medium (architecture, n8n workflow, lessons learned): [LINK AL POST DE MEDIUM]

📱 The app on Google Play: [LINK A GOOGLE PLAY]

💻 Source code on GitHub: [LINK A GITHUB]
