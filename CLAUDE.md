# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Guidelines (IMPORTANT)

1. **Do NOT invent or hallucinate information** - Always verify facts using official documentation
2. **Use web search when needed** - Consult official Kotlin Multiplatform, Ktor, Supabase, and Compose documentation
3. **Ask questions if unclear** - If requirements are ambiguous, ask for clarification before proceeding
4. **Follow established patterns** - Use MVVM architecture and repository pattern
5. **Code comments must be in English** - User-facing UI strings can be in Spanish (localized via `composeResources/values` and `values-es`)
6. Code review responses must be in English (from `.github/copilot-instructions.md`)

## Project Overview

SpainDecides ("España Decide") is a **Kotlin Multiplatform (KMP)** citizen-participation app using **Compose Multiplatform** for shared UI across Android and iOS. Users create, vote on, and discuss proposals organized by category (economy, healthcare, etc.).

**Package namespace:** `com.apptolast.spaindecides`

**Backend stack (all accessed from `commonMain`):**
- **Supabase** — PostgreSQL (Postgrest), Auth (email/password + native Google login via ComposeAuth), Realtime (WebSocket postgres changes)
- **n8n webhook** (`https://n8n.apptolast.com/webhook/` + configurable path) — AI-powered duplicate detection when creating proposals (embeddings + vector search); falls back to direct Supabase insert if unavailable
- **Firebase Cloud Functions + FCM** (via KMPNotifier) — push notifications for new proposals
- **EmailJS** — content reporting

Dependency versions live in `gradle/libs.versions.toml` (version catalog) — check there rather than assuming. Currently: Kotlin 2.4.x, Compose Multiplatform 1.11.x (Material3 versioned independently), Ktor 3.5.x, Koin 4.2.x, Supabase SDK 3.6.x, minSdk 26 / targetSdk 36 / compileSdk 37. Compose dependencies use explicit catalog coordinates — the `compose.*` Gradle plugin aliases are deprecated and fail script compilation under Kotlin 2.4.

## Build and Run Commands

Android has two flavor dimensions crossed with build types: flavors `dev` (applicationId suffix `.dev`, app name "España Decide (DEV)") and `prod`. **The flavor selects the backend environment; the build type (debug/release) does not.**

```bash
# Android - development (dev backend)
./gradlew :composeApp:assembleDevDebug
./gradlew :composeApp:installDevDebug

# Android - production backend
./gradlew :composeApp:assembleProdRelease

# Tests (unit tests live in commonTest)
./gradlew :composeApp:cleanTestDevDebugUnitTest :composeApp:testDevDebugUnitTest

# Run a single test class
./gradlew :composeApp:testDevDebugUnitTest --tests "com.apptolast.spaindecides.ComposeAppCommonTest"

# iOS - open /iosApp in Xcode and build from there, or build the framework:
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## Configuration & Secrets (BuildKonfig)

There is no Android `BuildConfig` (disabled). All secrets come from `local.properties` and are injected into `commonMain` via the **BuildKonfig** plugin as `com.apptolast.spaindecides.BuildKonfig`, exposed through the `expect object Environment` in `data/remote/Environment.kt` (actuals in `androidMain`/`iosMain` read BuildKonfig).

- Environment-dependent keys use a `_DEBUG` / `_RELEASE` suffix with fallback to the unsuffixed name: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `GOOGLE_WEB_CLIENT_ID`, `N8N_WEBHOOK_PATH`, `FIREBASE_FUNCTION_URL`, `FIREBASE_FUNCTION_API_KEY`. The `_RELEASE` variant is chosen when the flavor is production.
- Production detection (`composeApp/build.gradle.kts`): Android → any Gradle task name containing "prod"; iOS → `XCODE_CONFIGURATION=Release` property sent by Xcode. **Because flavor detection is task-name based, BuildKonfig values are baked at configuration time — don't mix dev and prod tasks in one Gradle invocation.**
- Flavor-independent keys: `EMAILJS_SERVICE_ID`, `EMAILJS_TEMPLATE_ID`, `EMAILJS_PUBLIC_KEY`.
- FCM topic is derived, not configured: `new_proposals_prod` vs `new_proposals_dev`.
- Release signing also reads from `local.properties` (`signing.storeFile`, `signing.storePassword`, `signing.keyAlias`, `signing.keyPassword`).
- Firebase config files: `composeApp/google-services.json` (Android), `iosApp/iosApp/GoogleService-Info.plist` (iOS).

See `SETUP.md` (Spanish) for obtaining Supabase/Google OAuth credentials and `docs/PUSH_NOTIFICATIONS_SETUP.md` for notifications.

## CI/CD (GitHub Actions)

- `build.yml` — runs on PRs/pushes to `develop`: builds debug, assembles tests, runs lint, submits dependency graph. Secrets are written into `local.properties` in the workflow.
- `deploy.yml` — runs on push to `main`: builds signed `prodRelease` AAB and uploads to Google Play (internal track, draft). Release notes in `distribution/whatsnew`.
- Branch model: work targets `develop`; merging to `main` deploys.

## Architecture

### MVVM + Repository Pattern

```
composeApp/src/commonMain/kotlin/com/apptolast/spaindecides/
├── App.kt                   # Root composable: NavHost, deep link handling
├── navigation/
│   ├── Routes.kt            # @Serializable sealed interface Route + all routes
│   └── DeepLinkManager.kt   # Global StateFlow bridge from push notifications to navigation
├── notification/            # NotificationInitializer (FCM topic subscription)
├── presentation/
│   ├── ui/screens/          # auth/, home/, proposals/, settings/
│   ├── ui/components/       # Reusable composables (ProposalCard, ReportDialog, ...)
│   ├── ui/theme/            # Material 3 theming (Color.kt, Type.kt, Theme.kt)
│   └── viewmodel/           # AuthViewModel, CategoryViewModel, ProposalViewModel, ReportViewModel
├── domain/
│   ├── model/               # Domain models (AuthUser)
│   └── repository/          # Repository interfaces
├── data/
│   ├── model/               # DTOs with @Serializable (Proposal, ProposalVote, ...)
│   ├── remote/              # SupabaseClient.kt, N8nWebhookClient.kt, ReportApiService.kt, Environment.kt
│   └── repository/          # Repository implementations
├── di/                      # Koin modules (DataModule, AuthModule, PresentationModule, KoinInitializer)
└── util/                    # AuthErrorMapper, DateTimeUtils, Platform
```

### Key Data Flows

**Realtime proposals:** `ProposalRepositoryImpl` exposes `Flow`s built with `callbackFlow` + Supabase `postgresChangeFlow` on the `proposals` and `proposal_votes` tables. Each subscription creates a **unique channel name** (prevents channel-reuse conflicts) and re-fetches the full list on any change. Voting is optimistic: UI state updates immediately, then syncs via realtime.

**Proposal creation:** `CreateProposalScreen` → ViewModel → `ProposalRepositoryImpl` → `N8nWebhookClient.processProposal()` (authenticated with the user's Supabase JWT as Bearer token). n8n returns either "created" or a list of similar proposals → `DuplicateProposalsScreen` for user review. HTTP timeout is 90s because the n8n workflow runs embeddings + vector search + AI model.

**Auth:** `SupabaseClientConfig` (singleton `object`) installs Auth (deep link scheme `com.apptolast.spaindecides://auth-callback` for OAuth), ComposeAuth (Google native login), Postgrest, and Realtime. Supabase persists sessions itself — there is no custom token storage. `AuthRepositoryImpl` takes no constructor dependencies.

**Push notification → navigation:** Platform code (MainActivity / iOS) parses the notification payload and calls `DeepLinkManager.setDeepLink()`; `App.kt` observes `pendingDeepLink`, looks up the `categoryKey` from `CategoryRepository`, navigates, then calls `consumeDeepLink()`.

### Adding New Code

- **Models**: `data/model/` (DTO) or `domain/model/`
- **Remote services**: `data/remote/`
- **Repositories**: Interface in `domain/repository/`, impl in `data/repository/`, register in `di/DataModule.kt`
- **ViewModels**: `presentation/viewmodel/`, register in `di/PresentationModule.kt` with `viewModelOf(...)`
- **Screens**: `presentation/ui/screens/<feature>/`, add route to `navigation/Routes.kt` and NavHost in `App.kt`

## Dependency Injection with Koin

```kotlin
// DataModule.kt style
val dataModule = module {
    single { HttpClient { /* ContentNegotiation + HttpTimeout */ } }
    singleOf(::N8nWebhookClient)
    singleOf(::ProposalRepositoryImpl) bind ProposalRepository::class
}

// PresentationModule.kt
val presentationModule = module {
    viewModelOf(::ProposalViewModel)
}

// In Composables — use koinViewModel() (Koin 4.x)
val viewModel: ProposalViewModel = koinViewModel()
```

`AuthModule` uses `expect fun createAuthModule()` for platform-specific additions on top of `authModuleCommon()`.

**Platform init:** Android calls `initKoin { androidLogger(); androidContext(...) }` in the Application class; iOS calls `KoinInitializerKt.doInitKoin()` in `iOSApp.swift`.

## Navigation

Type-safe Navigation Compose with Kotlin Serialization. All routes are `@Serializable` and implement `sealed interface Route` (see `navigation/Routes.kt`). Route args carry `categoryId` (DB UUID) plus `categoryKey` (i18n key like `"economy"`) so screens can resolve localized category names without a lookup.

```kotlin
composable<ProposalListRoute> { backStackEntry ->
    val route: ProposalListRoute = backStackEntry.toRoute()
    ...
}
```

## Expect/Actual Pattern

Used for `Environment` (BuildKonfig access), `Platform`, and `createAuthModule()`. `-Xexpect-actual-classes` is enabled. Prefer interfaces over expect/actual when a multiplatform library exists.

## UI Design & Theming

Use **Material Design 3** with theme files in `presentation/ui/theme/`. Always use theme colors and typography (`MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`) — never hardcoded values.

### Screen/Content Pattern

Separate stateful Screen (collects ViewModel state) from stateless Content (pure UI, previewable with `presentation/ui/preview/SampleData.kt`):

```kotlin
@Composable
fun ProposalListScreen(viewModel: ProposalViewModel = koinViewModel(), onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    ProposalListContent(state = state, onBack = onBack, onVote = viewModel::vote)
}

@Composable
fun ProposalListContent(state: ProposalState, onBack: () -> Unit, onVote: (String, Int) -> Unit) { /* UI */ }
```

- Spacing: multiples of 4dp (4, 8, 16, 24, 32)
- Corners: small 8-12dp, medium 12dp, large 16-24dp

## Push Notifications

**FCM via KMPNotifier.** App subscribes to the environment-specific topic (`FCM_TOPIC_NEW_PROPOSALS` from BuildKonfig) on startup via `NotificationInitializer`. Creating a proposal triggers the Firebase Cloud Function in `firebase/functions/src/index.ts`, which notifies all subscribers. Notification taps flow through `DeepLinkManager` (see Data Flows above). Setup details: `docs/PUSH_NOTIFICATIONS_SETUP.md`.

## Resources

- **KMP**: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- **Compose Multiplatform**: https://www.jetbrains.com/compose-multiplatform/
- **Supabase Kotlin**: https://supabase.com/docs/reference/kotlin/introduction
- **Ktor Client**: https://ktor.io/docs/client-create-multiplatform-application.html
- **Koin**: https://insert-koin.io
- **Material Design 3**: https://m3.material.io
- **KMPNotifier**: https://github.com/mirzemehdi/KMPNotifier
