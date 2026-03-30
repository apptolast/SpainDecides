# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Guidelines (IMPORTANT)

1. **Do NOT invent or hallucinate information** - Always verify facts using official documentation
2. **Use web search when needed** - Consult official Kotlin Multiplatform, Ktor, and Compose documentation
3. **Ask questions if unclear** - If requirements are ambiguous, ask for clarification before proceeding
4. **Follow established patterns** - Use MVVM architecture and repository pattern
5. **Code comments must be in English** - User-facing UI strings can be in Spanish

## Project Overview

SpainDecides is a **Kotlin Multiplatform (KMP) project** using **Compose Multiplatform** for shared UI across Android
and iOS.

**Package namespace:** `com.apptolast.spaindecides`

## Build and Run Commands

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# iOS - Open /iosApp in Xcode and build from there

# Tests
./gradlew :composeApp:cleanTestDebugUnitTest :composeApp:testDebugUnitTest
```

## Architecture

### MVVM Architecture Pattern

```
composeApp/src/commonMain/kotlin/com/apptolast/spaindecides/
├── presentation/
│   ├── ui/              # Composable UI (View)
│   │   ├── App.kt       # Main app entry point
│   │   ├── screens/     # Individual screen composables
│   │   └── components/  # Reusable UI components
│   └── viewmodel/       # ViewModels
├── domain/
│   └── repository/      # Repository interfaces
├── data/
│   ├── model/           # Data models (DTOs)
│   ├── remote/          # Network layer (Ktor)
│   │   ├── api/         # API service definitions
│   │   └── KtorClient.kt
│   └── repository/      # Repository implementations
├── di/                  # Koin DI modules
└── util/                # Utilities and helpers
```

### Data Flow

1. ViewModel → Repository → API Service → Ktor Client
2. StateFlow updates → UI recomposes
3. User interaction → ViewModel methods → State updates

### Adding New Code

- **Models**: `data/model/`
- **API Services**: `data/remote/api/`
- **Repositories**: Interface in `domain/repository/`, impl in `data/repository/`
- **ViewModels**: `presentation/viewmodel/`
- **UI Screens**: `presentation/ui/screens/`
- **Components**: `presentation/ui/components/`

## Dependency Injection with Koin

### Defining Modules

```kotlin
// DataModule.kt
val dataModule = module {
    single { createHttpClient() }
    singleOf(::ApiService)
    singleOf(::RepositoryImpl) bind Repository::class
}

// PresentationModule.kt
val presentationModule = module {
    viewModelOf(::MainViewModel)
}
```

### Using Koin

```kotlin
// In Composables - use koinViewModel() for all scenarios (Koin 4.1+)
@Composable
fun App() {
    val viewModel: MainViewModel = koinViewModel()
    MainScreen(viewModel = viewModel)
}

// Constructor injection in classes
class MainViewModel(private val repository: Repository) : ViewModel()
class RepositoryImpl(private val apiService: ApiService) : Repository
```

### Platform Initialization

**Android** - In Application class:
```kotlin
class SpainDecidesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@SpainDecidesApplication)
        }
    }
}
```

**iOS** - In iOSApp.swift:
```swift
init() {
    KoinInitializerKt.doInitKoin()
}
```

### Scoping: `single` (singletons), `factory` (new instance), `viewModelOf` (lifecycle-aware)

## Navigation with Compose

Use type-safe navigation with Kotlin Serialization:

```kotlin
@Serializable
object HomeRoute
@Serializable
data class DetailRoute(val itemId: String)

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(viewModel = viewModel, onNavigateToDetail = { navController.navigate(DetailRoute(it)) })
        }
        composable<DetailRoute> { backStackEntry ->
            val route: DetailRoute = backStackEntry.toRoute()
            DetailScreen(itemId = route.itemId, onBack = { navController.popBackStack() })
        }
    }
}
```

## Network Layer with Ktor Client

```kotlin
// KtorClient.kt
fun createHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) { level = LogLevel.INFO }
        install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        defaultRequest { url("https://api.example.com/") }
    }
}

// ApiService.kt
class ApiService(private val httpClient: HttpClient) {
    suspend fun getUsers(): Result<List<User>> = runCatching {
        httpClient.get("users").body()
    }
}
```

## Expect/Actual Pattern

Use only when no multiplatform library exists. Prefer interfaces for testability.

```kotlin
// commonMain
expect fun getCurrentTimestamp(): String

// androidMain
actual fun getCurrentTimestamp(): String = Clock.System.now().toString()

// iosMain
actual fun getCurrentTimestamp(): String {
    val formatter = NSISO8601DateFormatter()
    return formatter.stringFromDate(NSDate())
}
```

## UI Design & Theming

Use **Material Design 3**. Theme files in `presentation/ui/theme/`:

- `Color.kt` - Color schemes
- `Font.kt` - Custom fonts
- `Type.kt` - Typography
- `Theme.kt` - AppTheme composable

**Always use theme colors and typography:**
```kotlin
Text(
    text = "Title",
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.onSurface
)
```

### Screen/Content Pattern

Separate stateful Screen (with ViewModel) from stateless Content (pure UI):

```kotlin
// Screen - stateful
@Composable
fun ProposalListScreen(
    viewModel: ProposalViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    ProposalListContent(state = state, onBack = onBack, onVote = viewModel::vote)
}

// Content - stateless, easy to preview
@Composable
fun ProposalListContent(
    state: ProposalState,
    onBack: () -> Unit,
    onVote: (String, Int) -> Unit
) { /* UI implementation */
}
```

### Spacing: Use multiples of 4dp (4, 8, 16, 24, 32)

### Corners: Small 8-12dp, Medium 12dp, Large 16-24dp

## Key Configuration

- Kotlin: 2.2.20
- Compose Multiplatform: 1.9.1
- Android minSdk: 24, targetSdk: 36
- Dependencies managed via `gradle/libs.versions.toml`

## Push Notifications

Uses **Firebase Cloud Messaging** via **KMPNotifier** library.

### How It Works

1. App subscribes to `new_proposals` FCM topic on startup
2. Proposal creation triggers Firebase Cloud Function
3. Cloud Function sends notification to all subscribers

### Key Components

- `NotificationInitializer` - Topic subscription and listeners
- `NotificationService` - Sends via Firebase Cloud Function
- `firebase/functions/src/index.ts` - Cloud Function

### Configuration

1. Add `google-services.json` to `composeApp/` (Android)
2. Add `GoogleService-Info.plist` to `iosApp/iosApp/` (iOS)
3. Add `FIREBASE_FUNCTION_URL` and `FIREBASE_FUNCTION_API_KEY` to `local.properties`

See `docs/PUSH_NOTIFICATIONS_SETUP.md` for details.

## Resources

- **KMP**: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- **Compose Multiplatform**: https://www.jetbrains.com/compose-multiplatform/
- **Ktor Client**: https://ktor.io/docs/client-create-multiplatform-application.html
- **Koin**: https://insert-koin.io
- **Material Design 3**: https://m3.material.io
- **KMPNotifier**: https://github.com/mirzemehdi/KMPNotifier
