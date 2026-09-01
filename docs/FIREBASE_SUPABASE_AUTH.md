# Firebase Auth + Supabase (Third-Party Auth)

Authentication moved from **Supabase Auth** to **Firebase Auth**, provided by the
[BaseLogin](https://github.com/apptolast/BaseLogin) library. Supabase keeps its role as the database
(Postgrest + Realtime), but it no longer issues the tokens.

This document covers the work that is **not** in this repository: the Firebase console, the Supabase
project, the n8n webhook and the Xcode project. Until those are done the app compiles but cannot
read or write any data.

---

## What changed in the client

| | Before | After |
|---|---|---|
| Sign-in / registration UI | `presentation/ui/screens/auth/LoginScreen.kt`, `RegisterScreen.kt` | BaseLogin's `authRoutesFlow`, branded through `SpainDecidesAuthSlots.kt` |
| Auth backend | Supabase Auth (`auth-kt`) | Firebase Auth via `dev.gitlive:firebase-auth`, wrapped by BaseLogin |
| Auth repository | `domain/repository/AuthRepository.kt` (own) | `com.apptolast.baselogin.domain.AuthRepository` |
| Token sent to Postgrest | Supabase session JWT (`Auth` plugin) | Firebase ID token (`accessToken` lambda in `SupabaseClient.kt`) |
| Token sent to n8n | Supabase session JWT | Firebase ID token |
| User id written to rows | Supabase `auth.users` UUID | Firebase UID (28-character string, **not** a UUID) |

`supabase-kt` forbids installing the `Auth` plugin together with a custom `accessToken`, so
`auth-kt` and `compose-auth` were dropped from the dependencies.

---

## 1. Firebase console

1. Open the Firebase project already used for push notifications.
2. **Authentication → Sign-in method**: enable **Email/Password** and **Google**.
3. **Project settings → Your apps → Android**: add the SHA-1 **and** SHA-256 fingerprints of the
   debug and release keystores. Google sign-in through Credential Manager fails without them.
4. Download the updated `google-services.json` into `composeApp/` and
   `GoogleService-Info.plist` into `iosApp/iosApp/`. Both are gitignored; CI injects the Android one
   from the `FIREBASE_JSON` secret, so update that secret too.
5. Copy the **Web client ID** (Authentication → Sign-in method → Google → Web SDK configuration)
   and, for iOS, the **iOS client ID**.

### local.properties

```properties
GOOGLE_WEB_CLIENT_ID_DEBUG=<web client id>.apps.googleusercontent.com
GOOGLE_WEB_CLIENT_ID_RELEASE=<web client id>.apps.googleusercontent.com
# Only needed to build iOS with Google sign-in
GOOGLE_IOS_CLIENT_ID_DEBUG=<ios client id>.apps.googleusercontent.com
GOOGLE_IOS_CLIENT_ID_RELEASE=<ios client id>.apps.googleusercontent.com
```

`GOOGLE_WEB_CLIENT_ID` now points at the Firebase OAuth client, not the one that used to be
configured in Supabase. They may or may not be the same client depending on how the Google Cloud
project was set up — check before reusing the old value.

For Xcode, also set the matching URL scheme in `iosApp/Configuration/Config.xcconfig`:

```xcconfig
GOOGLE_IOS_CLIENT_ID=<ios client id>.apps.googleusercontent.com
GOOGLE_IOS_REVERSED_CLIENT_ID=com.googleusercontent.apps.<ios client id prefix>
GOOGLE_WEB_CLIENT_ID=<web client id>.apps.googleusercontent.com
```

---

## 2. Supabase: enable Third-Party Auth for Firebase

Dashboard → **Authentication → Third Party Auth → Add integration → Firebase**, with the Firebase
project id. Self-hosted equivalent in `supabase/config.toml`:

```toml
[auth.third_party.firebase]
enabled = true
project_id = "<firebase-project-id>"
```

From then on Supabase validates Firebase ID tokens, and inside a policy the token is readable via
`auth.jwt()`. Its `sub` claim is the Firebase UID.

---

## 3. Database migration (required)

This is the part that breaks silently if skipped: every write keeps succeeding locally through the
optimistic UI and then fails against the database.

### 3.1 Column types

The app writes the signed-in user id into:

- `public.proposals.user_id`
- `public.proposal_votes.user_id`

Both were UUIDs referencing `auth.users`. A Firebase UID is **not** a UUID, so the columns have to
become `text` and the foreign key to `auth.users` has to go:

```sql
alter table public.proposals   drop constraint if exists proposals_user_id_fkey;
alter table public.proposal_votes drop constraint if exists proposal_votes_user_id_fkey;

alter table public.proposals      alter column user_id type text using user_id::text;
alter table public.proposal_votes alter column user_id type text using user_id::text;
```

Check the actual constraint names first — the ones above are Postgres' default naming and may
differ.

### 3.2 RLS policies

Every policy comparing `auth.uid()` to a `user_id` must switch to the JWT subject, and should also
pin the issuer so tokens from an unrelated Firebase project are rejected:

```sql
-- Example for proposal_votes; repeat for every policy that used auth.uid()
create policy "Users manage their own votes"
on public.proposal_votes
for all
to authenticated
using (
  auth.jwt() ->> 'sub' = user_id
  and auth.jwt() ->> 'iss' = 'https://securetoken.google.com/<firebase-project-id>'
)
with check (
  auth.jwt() ->> 'sub' = user_id
  and auth.jwt() ->> 'iss' = 'https://securetoken.google.com/<firebase-project-id>'
);
```

Run `select * from pg_policies where schemaname = 'public';` to find every policy that still
mentions `auth.uid()`.

### 3.3 The profiles table

`public.profiles` was populated by a trigger on `auth.users`. Nothing inserts into `auth.users` any
more, so that trigger will never fire again. The app does not read `profiles` today
(`data/model/Profile.kt` is an unused DTO), so the options are to drop the table or to populate it
from the client after sign-up. Decide before something starts depending on it again.

### 3.4 Existing users and their data

Accounts that exist in Supabase Auth do **not** exist in Firebase, and their UUIDs do not match any
Firebase UID. Existing proposals and votes therefore end up orphaned: still visible, but no longer
owned by anyone who can sign in.

Migrating them means importing the users into Firebase (`firebase auth:import`, which preserves
emails but not Supabase's bcrypt hashes unless they are exported in a supported format) and then
rewriting `user_id` on every row through a UUID → Firebase UID mapping table. If the user base is
small, asking people to register again and accepting the orphaned rows is the cheaper path — but it
is a product decision, not a technical one.

---

## 4. n8n webhook

The webhook validated Supabase's JWT with the Supabase JWT secret. It now receives a Firebase ID
token and must validate it against Google:

- **JWKS**: `https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com`
- **Issuer**: `https://securetoken.google.com/<firebase-project-id>`
- **Audience**: `<firebase-project-id>`
- **Algorithm**: RS256

The workflow also inserts proposals with the `user_id` it reads from the token subject, so the
column type change in §3.1 applies there too.

---

## 5. iOS (Xcode)

The Kotlin and Swift sides are done; the Xcode project still needs two Swift Package products added
to the `iosApp` target:

- **FirebaseAuth** (from the firebase-ios-sdk package already used for FirebaseCore/Messaging)
- **GoogleSignIn** (`https://github.com/google/GoogleSignIn-iOS`)

`iosApp/iosApp/iOSApp.swift` already imports both and installs
`GoogleSignInProviderIOS.shared.signInHandler` / `signOutHandler`; **until the packages are added
the iOS target will not compile.**

Also add the reversed iOS client ID as a URL scheme in the target's Info settings, otherwise the
Google flow cannot return to the app. `Info.plist` already reads it from
`GOOGLE_IOS_REVERSED_CLIENT_ID`.

---

## 6. What was intentionally left disabled

`di/LoginConfig.kt` turns off Phone OTP and Magic Link. Both are supported by BaseLogin but need
extra Firebase setup (SMS quota, email link templates) and, on iOS, additional Swift handlers this
app does not install. Enabling them later is a change to `spainDecidesLoginConfig()` plus the
platform setup described in BaseLogin's README.
