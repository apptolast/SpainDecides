# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Guidelines (IMPORTANT)

1. **Do NOT invent or hallucinate information** - Always verify facts using official documentation
2. **Use web search when needed** - Consult official Kotlin Multiplatform, Ktor, and Compose documentation for implementation details
3. **Ask questions if unclear** - If requirements are ambiguous or you're unsure about an approach, ask the user for clarification before proceeding
4. **Follow established patterns** - Use the MVVM architecture and repository pattern when implementing features
5. **Code comments must be in English** - All technical comments, documentation (KDoc), and code-level explanations must be written in English. User-facing strings in the UI (button labels, messages, etc.) can be in Spanish for the target audience

## Project Overview

SpainDecides is a **Kotlin Multiplatform (KMP) project** using **Compose Multiplatform** for shared UI across Android and iOS platforms. The project uses a single codebase to build native applications for both platforms.

**Package namespace:** `com.apptolast.spaindecides`

## Build and Run Commands

### Android

```bash
./gradlew :composeApp:assembleDebug    # Build debug APK
./gradlew :composeApp:assembleRelease  # Build release APK
./gradlew :composeApp:installDebug     # Run on connected device/emulator
```

### iOS

iOS builds must be done through Xcode:
1. Open the `/iosApp` directory in Xcode
2. Build and run from Xcode

The Kotlin framework can be compiled separately with:
```bash
./gradlew :composeApp:linkDebugFrameworkIosArm64
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Testing

Run common tests:
```bash
./gradlew :composeApp:cleanTestDebugUnitTest :composeApp:testDebugUnitTest
```

## Architecture

### MVVM Architecture Pattern

This project should follow **MVVM (Model-View-ViewModel)** architecture as recommended by Google for Kotlin Multiplatform:

```
composeApp/src/commonMain/kotlin/com/apptolast/spaindecides/
├── presentation/
│   ├── ui/              # Composable UI (View)
│   │   ├── App.kt       # Main app entry point
│   │   ├── screens/     # Individual screen composables
│   │   └── components/  # Reusable UI components
│   └── viewmodel/       # ViewModels
│       └── *ViewModel.kt
│
├── domain/
│   └── repository/      # Repository interfaces
│       └── *Repository.kt
│
├── data/
│   ├── model/           # Data models (DTOs)
│   │   └── *.kt
│   ├── remote/          # Network layer
│   │   ├── api/         # API service definitions
│   │   │   └── *ApiService.kt
│   │   └── KtorClient.kt  # Ktor HTTP client configuration
│   └── repository/      # Repository implementations
│       └── *RepositoryImpl.kt
│
├── di/                  # Dependency Injection modules
│   ├── KoinInitializer.kt
│   ├── DataModule.kt
│   ├── DomainModule.kt
│   └── PresentationModule.kt
│
└── util/                # Utilities and helpers
    └── *.kt
```

### Data Flow

1. **ViewModel initialization** → Calls repository methods
2. **Repository** → Calls API service or data source
3. **Ktor Client** → Makes HTTP requests
4. **StateFlow updates** → UI recomposes with new data
5. **User interaction** → Triggers ViewModel methods
6. **Success/Error** → Updates state and shows feedback

### Multiplatform Source Structure

```
composeApp/src/
├── commonMain/          # Shared code for all platforms
│   ├── kotlin/          # Common Kotlin code (MVVM architecture)
│   └── composeResources/ # Shared resources (images, strings, etc.)
├── androidMain/         # Android-specific code
│   └── kotlin/
│       └── MainActivity.kt
├── iosMain/             # iOS-specific code (Kotlin)
│   └── kotlin/
│       └── MainViewController.kt
└── commonTest/          # Shared test code
```

### Adding New Code

- **Models**: Add to `data/model/`
- **API Services**: Add to `data/remote/api/`
- **Repositories**: Interface in `domain/repository/`, implementation in `data/repository/`
- **ViewModels**: Add to `presentation/viewmodel/`
- **UI Screens**: Add to `presentation/ui/screens/`
- **Reusable Components**: Add to `presentation/ui/components/`
- **Platform-specific**: Add to respective platform source sets (androidMain, iosMain)

## Dependency Injection with Koin

This project uses **Koin** as the dependency injection framework for managing object creation and lifecycle across all platforms.

### Why Koin?

- **Multiplatform Support**: Official support for all KMP targets (Android, iOS, Desktop, Web)
- **Lightweight**: No code generation or reflection, just Kotlin DSL
- **Compose Integration**: First-class support for Compose Multiplatform with `koinViewModel()`
- **Easy Testing**: Simple to provide fake implementations for unit tests
- **Google Best Practices**: Follows MVVM architecture recommendations with constructor injection

### Koin Configuration

#### Version (gradle/libs.versions.toml)

```toml
[versions]
koin-bom = "4.1.1"

[libraries]
koin-bom = { module = "io.insert-koin:koin-bom", version.ref = "koin-bom" }
koin-core = { module = "io.insert-koin:koin-core" }
koin-compose = { module = "io.insert-koin:koin-compose" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel" }
koin-compose-viewmodel-navigation = { module = "io.insert-koin:koin-compose-viewmodel-navigation" }
koin-android = { module = "io.insert-koin:koin-android" }
koin-test = { module = "io.insert-koin:koin-test" }
```

#### Build Configuration (composeApp/build.gradle.kts)

```kotlin
commonMain.dependencies {
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.viewmodel.navigation)
}

androidMain.dependencies {
    implementation(libs.koin.android)
}

commonTest.dependencies {
    implementation(libs.koin.test)
}
```

### Defining Koin Modules

#### Data Module (di/DataModule.kt)

Provides network and repository dependencies:

```kotlin
val dataModule = module {
    // HttpClient singleton
    single { createHttpClient() }

    // API Service with constructor injection
    singleOf(::ApiService)

    // Repository Implementation bound to interface
    singleOf(::RepositoryImpl) bind Repository::class
}
```

**Key Points:**
- `single` creates a singleton (one instance for app lifetime)
- `singleOf(::ClassName)` is concise syntax for constructor injection
- `bind` allows injecting by interface type
- Koin automatically resolves constructor dependencies with `get()`

#### Presentation Module (di/PresentationModule.kt)

Provides ViewModels with lifecycle management:

```kotlin
val presentationModule = module {
    // ViewModel with lifecycle-aware scope
    viewModelOf(::MainViewModel)
}
```

**Key Points:**
- `viewModelOf` creates a ViewModel-scoped instance
- Automatically handles lifecycle and configuration changes
- Repository is auto-injected via constructor

### Using Koin for Injection

#### Injecting ViewModel in Composables

Starting with **Koin 4.1+**, use `koinViewModel()` for **all scenarios**:

```kotlin
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val viewModel: MainViewModel = koinViewModel()
    MainScreen(viewModel = viewModel)
}
```

**For Navigation Compose**:

```kotlin
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") {
            // koinViewModel() automatically handles Navigation integration
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(viewModel = viewModel)
        }

        composable("detail/{itemId}") {
            val viewModel: DetailViewModel = koinViewModel()
            DetailScreen(viewModel)
        }
    }
}
```

**Important Note**: `koinNavViewModel()` is **DEPRECATED** in Koin 4.1+. Always use `koinViewModel()`.

#### Constructor Injection in Classes

All dependencies use constructor injection (no field injection):

```kotlin
// ViewModel receives Repository
class MainViewModel(
    private val repository: Repository  // Koin injects
) : ViewModel()

// Repository receives API service
class RepositoryImpl(
    private val apiService: ApiService  // Koin injects
) : Repository

// API Service receives HttpClient
class ApiService(
    private val httpClient: HttpClient  // Koin injects
)
```

### Platform-Specific Initialization

#### Android

Create an `Application` class to initialize Koin:

```kotlin
class SpainDecidesApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()  // Enable Android logging
            androidContext(this@SpainDecidesApplication)  // Provide context
        }
    }
}
```

Register in `AndroidManifest.xml`:

```xml
<application
    android:name=".SpainDecidesApplication"
    ...>
```

#### iOS

Initialize in `iOSApp.swift`:

```swift
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### Koin Scoping Strategies

| Scope | Usage | Lifecycle |
|-------|-------|-----------|
| `single` | Singletons (HttpClient, Repositories, API services) | App lifetime |
| `factory` | Short-lived objects (use cases) | Created on each injection |
| `viewModelOf` | ViewModels | Survives configuration changes |

### Best Practices

1. **Constructor Injection Only**: Never use field injection
2. **Interface-Based Design**: Depend on abstractions (Repository interface, not RepositoryImpl)
3. **Single Responsibility**: Each class should depend only on what it needs
4. **Module Organization**: Separate by architectural layers (data, domain, presentation)

### Common Issues and Solutions

#### Issue: `KoinAppAlreadyStartedException`

**Cause:** Starting Koin multiple times

**Solution:** Only call `initKoin()` once at Application/App entry point, never in Activities or Composables

#### Issue: Missing dependency injection

**Cause:** Class not defined in any module

**Solution:** Add the class to the appropriate module (DataModule, DomainModule, or PresentationModule)

### Koin Resources

- **Official Documentation**: https://insert-koin.io
- **KMP Guide**: https://insert-koin.io/docs/reference/koin-mp/kmp/
- **Compose Integration**: https://insert-koin.io/docs/reference/koin-compose/compose/

## Navigation with Compose

### Type-Safe Navigation

Use **Jetpack Navigation Compose** with Kotlin Serialization for type-safe navigation:

#### Version Configuration (gradle/libs.versions.toml)

```toml
[versions]
navigation-compose = "2.8.0-alpha10"  # Or latest version

[libraries]
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
```

#### Build Configuration (composeApp/build.gradle.kts)

```kotlin
commonMain.dependencies {
    implementation(libs.navigation.compose)
}
```

#### Define Routes with Serialization

```kotlin
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DetailRoute(val itemId: String)

@Serializable
data class ProfileRoute(val userId: String)
```

#### Navigation Setup

```kotlin
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            val viewModel: HomeViewModel = koinViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { itemId ->
                    navController.navigate(DetailRoute(itemId))
                }
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val route: DetailRoute = backStackEntry.toRoute()
            val viewModel: DetailViewModel = koinViewModel()
            DetailScreen(
                itemId = route.itemId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

### Shared ViewModels Across Destinations

```kotlin
NavHost(
    navController = navController,
    startDestination = "screenA",
    route = "parentRoute"  // Important: Define parent route
) {
    composable("screenA") { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("parentRoute")
        }
        val sharedViewModel: SharedViewModel = koinViewModel(
            viewModelStoreOwner = parentEntry  // Scope to parent
        )
        ScreenA(sharedViewModel)
    }

    composable("screenB") { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("parentRoute")
        }
        val sharedViewModel: SharedViewModel = koinViewModel(
            viewModelStoreOwner = parentEntry  // Same instance
        )
        ScreenB(sharedViewModel)
    }
}
```

### Navigation Best Practices

1. **Pass data through routes**: Use serializable data classes for type-safety
2. **Keep navigation logic in UI layer**: Don't navigate from ViewModels
3. **Use callbacks**: Pass navigation callbacks as lambda parameters to screens
4. **Avoid deep navigation stacks**: Provide clear back navigation

## Network Layer with Ktor Client

This project uses **Ktor Client** for HTTP communication instead of Retrofit, as it provides full multiplatform support.

### Why Ktor Over Retrofit?

- **Multiplatform Support**: Works on Android, iOS, Desktop, Web
- **Coroutines-First**: Built from the ground up for Kotlin coroutines
- **Lightweight**: Smaller footprint than Retrofit
- **Official JetBrains Library**: Maintained by the Kotlin team

### Ktor Configuration

#### Version (gradle/libs.versions.toml)

```toml
[versions]
ktor = "3.0.3"
kotlinx-serialization = "1.8.0"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

[plugins]
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

#### Build Configuration (composeApp/build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)  // OkHttp engine for Android
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)  // Darwin engine for iOS
        }
    }
}
```

### HttpClient Setup

Create a factory function in `data/remote/KtorClient.kt`:

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }

        defaultRequest {
            url("https://api.example.com/")  // Base URL
        }
    }
}
```

### API Service Example

```kotlin
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class User(val id: String, val name: String)

class ApiService(private val httpClient: HttpClient) {
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = httpClient.get("users")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(id: String): Result<User> {
        return try {
            val response = httpClient.get("users/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(user: User): Result<User> {
        return try {
            val response = httpClient.post("users") {
                setBody(user)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Ktor Resources

- **Official Documentation**: https://ktor.io/docs/client-create-multiplatform-application.html
- **Content Negotiation**: https://ktor.io/docs/serialization-client.html
- **Ktor Client Setup**: https://ktor.io/docs/client-create-new-application.html

## Expect/Actual Pattern - Platform-Specific Code

### When to Use Expect/Actual

The expect/actual mechanism enables accessing platform-specific APIs when:

1. **No multiplatform library exists** - The functionality is not available through official KMP libraries
2. **Factory functions** - Need to return platform-specific implementations
3. **Inheriting platform classes** - Must extend existing platform-specific base classes
4. **Direct native API access** - Require direct access to platform APIs for performance or features

### When NOT to Use (IMPORTANT)

**Official Recommendation**: Prefer interfaces over expect/actual in most cases.

❌ **DO NOT use expect/actual if:**
- A multiplatform library already exists (e.g., kotlinx-datetime, kotlinx-coroutines)
- An interface would be sufficient
- You can use dependency injection with interfaces
- Standard Kotlin constructs solve the problem

✅ **Interfaces are better because:**
- Allow multiple implementations per platform
- Make testing easier with fake/mock implementations
- More flexible and standard Kotlin approach
- Avoid Beta feature limitations

### Example: Platform-Specific Date/Time

```kotlin
// commonMain/util/DateTimeProvider.kt
expect fun getCurrentTimestamp(): String

// androidMain/util/DateTimeProvider.android.kt
actual fun getCurrentTimestamp(): String {
    return kotlin.time.Clock.System.now().toString()
}

// iosMain/util/DateTimeProvider.ios.kt
actual fun getCurrentTimestamp(): String {
    // iOS uses Foundation NSDate directly for better platform integration
    val formatter = NSISO8601DateFormatter()
    return formatter.stringFromDate(NSDate())
}
```

### Process for Handling Missing Multiplatform Libraries

When encountering functionality without multiplatform support:

1. **Search for Official KMP Libraries**
   - Check JetBrains kotlinx.* libraries first
   - Search Maven Central for "kmp-*" or "kmm-*" prefixed libraries
   - Verify library supports all your target platforms

2. **Verify Library Documentation**
   - Read official documentation to confirm multiplatform support
   - Check GitHub releases for latest stable versions
   - Review platform compatibility matrix

3. **Test Library Integration**
   - Add dependency to `commonMain`
   - Sync Gradle and verify no errors
   - Test compilation for each platform target

4. **Implement Expect/Actual as Last Resort**
   - Only when no suitable multiplatform library exists
   - Document the decision and alternatives evaluated
   - Create expect declaration in `commonMain`
   - Provide actual implementations for each platform
   - Use platform-native APIs (e.g., NSDate for iOS, java.time for JVM)

5. **Document the Implementation**
   - Add comments explaining why expect/actual was necessary
   - Reference any GitHub issues or documentation consulted
   - Note future migration path if library becomes available

### Rules for Expect/Actual Declarations

1. **Declaration Location**: `expect` in `commonMain`, `actual` in each platform source set
2. **Same Package**: Both must be in the identical package
3. **Matching Signatures**: Names, parameters, and return types must match exactly
4. **No Implementation in Expect**: Expected declarations cannot contain implementation code
5. **All Platforms**: Every platform must provide an `actual` implementation

### Best Practices

- **Verify First**: Always search for existing multiplatform solutions before implementing expect/actual
- **Use Web Search**: When unsure, search official Kotlin and library documentation
- **Ask Questions**: If requirements are unclear, ask for clarification rather than guessing
- **Document Decisions**: Explain why expect/actual was chosen over alternatives
- **Keep It Simple**: Minimize the surface area of platform-specific code
- **Test All Platforms**: Verify implementation works on every target platform

### Beta Feature Warning

Expected/actual classes are in **Beta status** - migration steps may be required in future Kotlin versions. Suppress warnings if needed:

```kotlin
freeCompilerArgs.add("-Xexpect-actual-classes")
```

### Resources for Expect/Actual

- **Official Documentation**: https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-expect-actual.html
- **Kotlin Language Docs**: https://kotlinlang.org/docs/multiplatform-expect-actual.html
- **Connect to Platform APIs**: https://kotlinlang.org/docs/multiplatform-connect-to-apis.html

## UI Design & Theming System

This project uses **Material Design 3** with custom theming for consistent UI across all platforms.

### Theme Architecture

The theming system should be located in `presentation/ui/theme/`:

```
presentation/ui/theme/
├── Color.kt       # Color palette definitions (light & dark schemes)
├── Font.kt        # Custom font family definitions
├── Type.kt        # Typography scale (Material 3)
└── Theme.kt       # Main AppTheme composable
```

### Color System

#### Material 3 Color Roles

Always use `MaterialTheme.colorScheme` - never hardcode colors:

```kotlin
// ✅ CORRECT - Uses theme colors
Text(
    text = "Title",
    color = MaterialTheme.colorScheme.onSurface
)

Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    Text("Action")
}

// ❌ WRONG - Hardcoded color
Text(
    text = "Title",
    color = Color(0xFFFFFFFF)  // Don't do this!
)
```

| Role | Usage | Example Components |
|------|-------|-------------------|
| `primary` | Main brand color, primary actions | FABs, prominent buttons |
| `primaryContainer` | Tinted backgrounds | Cards with emphasis |
| `secondary` | Less prominent actions | Secondary buttons |
| `tertiary` | Contrasting accents | Special highlights |
| `surface` | Backgrounds for components | Cards, dialogs |
| `surfaceVariant` | Alternative surfaces | Input fields |
| `outline` | Borders and dividers | TextField borders |
| `error` | Error states and warnings | Error messages |

### Typography System

#### Material 3 Type Scale

| Category | Sizes | Usage | Font Weight |
|----------|-------|-------|-------------|
| **Display** | Large (57sp), Medium (45sp), Small (36sp) | Large, expressive text | Bold/Normal |
| **Headline** | Large (32sp), Medium (28sp), Small (24sp) | Page titles | SemiBold/Medium |
| **Title** | Large (22sp), Medium (16sp), Small (14sp) | Section titles | SemiBold/Medium |
| **Body** | Large (16sp), Medium (14sp), Small (12sp) | Main content | Normal |
| **Label** | Large (14sp), Medium (12sp), Small (11sp) | Buttons, labels | Medium |

#### Using Typography in UI

**Always use MaterialTheme.typography**:

```kotlin
// Page title
Text(
    text = "Dashboard",
    style = MaterialTheme.typography.headlineMedium,
    color = MaterialTheme.colorScheme.onSurface
)

// Section heading
Text(
    text = "Recent Items",
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onSurface
)

// Body text
Text(
    text = "Description text",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

// Button text
Button(onClick = { }) {
    Text(
        text = "Save",
        style = MaterialTheme.typography.labelLarge
    )
}
```

### Custom Fonts

#### Adding Custom Fonts (Step-by-Step)

##### Step 1: Download Font Files

1. Visit **Google Fonts**: https://fonts.google.com
2. **Recommended fonts**:
   - **Inter**: Modern, screen-optimized (https://fonts.google.com/specimen/Inter)
   - **Roboto**: Material Design standard (https://fonts.google.com/specimen/Roboto)
3. Download at least **4 weights**: Regular (400), Medium (500), SemiBold (600), Bold (700)

##### Step 2: Create Font Directory

```bash
mkdir -p composeApp/src/commonMain/composeResources/font
```

##### Step 3: Place Font Files

Copy the `.ttf` files with **lowercase, underscore-separated names**:

```
composeApp/src/commonMain/composeResources/font/
├── inter_regular.ttf
├── inter_medium.ttf
├── inter_semibold.ttf
└── inter_bold.ttf
```

##### Step 4: Build Project

```bash
./gradlew build
```

This generates resource accessors in `spaindecides.composeapp.generated.resources.Res.font.*`

##### Step 5: Create Font.kt

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.inter_regular
import spaindecides.composeapp.generated.resources.inter_medium
import spaindecides.composeapp.generated.resources.inter_semibold
import spaindecides.composeapp.generated.resources.inter_bold

@Composable
fun appFontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_bold, FontWeight.Bold)
    )
}
```

##### Step 6: Use in Typography

```kotlin
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
fun appTypography(): Typography {
    val fontFamily = appFontFamily()
    return Typography(
        // Use fontFamily in all text styles
        displayLarge = TextStyle(fontFamily = fontFamily),
        headlineMedium = TextStyle(fontFamily = fontFamily),
        bodyMedium = TextStyle(fontFamily = fontFamily),
        // ... all other styles
    )
}
```

#### Important: Font() is Composable

In Compose Multiplatform, **Font() is a @Composable function**:

- FontFamily must be created inside `@Composable` functions
- Typography must be created inside `@Composable` functions
- Cannot define fonts as top-level `val` properties

**This is why** `appFontFamily()` and `appTypography()` are functions, not values.

### Creating New Screens

#### 1. Follow Material 3 Component Patterns

```kotlin
@Composable
fun MyNewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Page title
        Text(
            text = "Screen Title",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Content card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            // Card content
        }

        // Primary action button
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Action")
        }
    }
}
```

#### 2. Consistent Spacing

Use **multiples of 4dp** for spacing:

```kotlin
val spacing = object {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

Column(
    modifier = Modifier.padding(spacing.medium),
    verticalArrangement = Arrangement.spacedBy(spacing.small)
) {
    // Content with consistent spacing
}
```

#### 3. Rounded Corners

- **Small components**: `8.dp` or `12.dp`
- **Medium components**: `12.dp`
- **Large components**: `16.dp` or `24.dp`

```kotlin
Card(shape = RoundedCornerShape(16.dp)) { /* ... */ }
Button(shape = RoundedCornerShape(12.dp)) { /* ... */ }
```

### Status Bar & System UI Management

#### Android Edge-to-Edge Design

Configure status bar in `MainActivity.kt`:

```kotlin
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Configure edge-to-edge with auto-adjusting status bar icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

Configure icon colors in theme using expect/actual:

```kotlin
// commonMain/presentation/ui/theme/Theme.kt
@Composable
expect fun ConfigureSystemUI(darkTheme: Boolean)

// androidMain/presentation/ui/theme/Theme.android.kt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun ConfigureSystemUI(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // isAppearanceLightStatusBars = true → dark icons (for light backgrounds)
            // isAppearanceLightStatusBars = false → light icons (for dark backgrounds)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
```

Call from your theme composable:

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ConfigureSystemUI(darkTheme)

    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = appTypography(),
        content = content
    )
}
```

### UI Design Resources

- **Material Design 3**: https://m3.material.io
- **Compose Material 3**: https://developer.android.com/develop/ui/compose/designsystems/material3
- **Google Fonts**: https://fonts.google.com
- **Edge-to-Edge Design**: https://developer.android.com/develop/ui/compose/system/system-bars

## Key Configuration

### Version Catalog (`gradle/libs.versions.toml`)

Current versions:
- Kotlin: 2.2.20
- Compose Multiplatform: 1.9.1
- Android minSdk: 24, targetSdk: 36
- AndroidX Lifecycle: 2.9.5

Dependencies are managed through Gradle version catalogs for centralized version control.

### Build Configuration (`composeApp/build.gradle.kts`)

- Defines all platform targets (Android, iOS)
- Configures sourceSets and dependencies
- Android namespace: `com.apptolast.spaindecides`

### Gradle Properties (`gradle.properties`)

- JVM max memory: Configure if needed for large projects
- Configuration cache: Can be enabled for faster builds

## Development Notes

- All code comments and technical documentation must be in English
- User-facing UI strings can be in Spanish for the target audience
- Project uses Material Design 3 for consistent UI
- Compose Multiplatform enables write-once UI code across all platforms
- Network calls should use Ktor Client (not Retrofit)
- All API communication should go through the Repository pattern
- ViewModels use Kotlin Coroutines and StateFlow for reactive state management
- The main UI entry point is `App.kt` in `commonMain`
- Resources (images, strings, etc.) use Compose Multiplatform resources system: `Res.drawable.*`, `Res.string.*`

## Useful Resources

When implementing new features or troubleshooting, consult these official resources:

### Kotlin Multiplatform
- **Official Guide**: https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
- **Compose Multiplatform**: https://www.jetbrains.com/compose-multiplatform/
- **KMP Architecture**: https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html

### Ktor Client
- **Official Documentation**: https://ktor.io/docs/client-create-multiplatform-application.html
- **Content Negotiation**: https://ktor.io/docs/serialization-client.html

### Compose & Android
- **Compose Documentation**: https://developer.android.com/jetpack/compose
- **ViewModel Guide**: https://developer.android.com/topic/libraries/architecture/viewmodel
- **StateFlow**: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
- **Navigation Compose**: https://developer.android.com/jetpack/compose/navigation

### Dependency Injection
- **Koin Documentation**: https://insert-koin.io
- **Koin KMP Guide**: https://insert-koin.io/docs/reference/koin-mp/kmp/
- **Koin Compose**: https://insert-koin.io/docs/reference/koin-compose/compose/
