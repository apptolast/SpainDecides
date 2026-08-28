# SpainDecides 🗳️

### Plataforma de Participación Ciudadana · Citizen Participation Platform

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4?style=flat)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-green.svg?style=flat)](https://www.android.com/)

---

## 🇪🇸 Español

### 📱 Descripción

**España Decide** es una aplicación móvil de participación ciudadana que ofrece a los ciudadanos la
posibilidad de para crear, votar y discutir propuestas sobre temas de interés público. Construida
con tecnología nativa multiplataforma, permite a los usuarios participar activamente en la
democracia desde sus dispositivos Android e iOS.

La aplicación facilita el debate democrático organizado por categorías como **Economía**, **Sanidad
**, **Educación**, **Medio Ambiente**, **Justicia** y más, permitiendo que las voces de los
ciudadanos sean escuchadas en tiempo real.

### ✨ Características Principales

- 🗳️ **Sistema de Votación**: Vota a favor o en contra de propuestas con un sistema intuitivo de
  upvote/downvote
- 📝 **Creación de Propuestas**: Cualquier usuario puede crear propuestas con título y descripción
  detallada
- 🏷️ **Categorías Temáticas**: Propuestas organizadas por áreas (economía, sanidad, educación, etc.)
- ⚡ **Actualizaciones en Tiempo Real**: Sincronización instantánea de votos y nuevas propuestas vía
  WebSockets
- 🔒 **Autenticación Segura**: Sistema de login con email/contraseña y Google OAuth
- 📊 **Ranking por Popularidad**: Las propuestas se ordenan por votos netos (upvotes - downvotes)
- 🎨 **Diseño Material 3**: Interfaz moderna con soporte para tema claro/oscuro
- 🌐 **Multiplataforma**: Misma experiencia en Android e iOS con código compartido

### 📸 Capturas de Pantalla

> 🚧 Próximamente: Capturas de las pantallas principales (Categorías, Lista de Propuestas, Detalle,
> Creación)

### 📥 Descarga

La aplicación estará disponible próximamente en:

- 📱 **Google Play Store**: _Enlace próximamente_
- 🍎 **Apple App Store**: _Enlace próximamente_

### 🛠️ Tecnología

SpainDecides está construido con tecnologías de vanguardia:

| Componente                 | Tecnología                              |
|----------------------------|-----------------------------------------|
| **Lenguaje**               | Kotlin 2.4.0                            |
| **Framework UI**           | Compose Multiplatform 1.11.1            |
| **Arquitectura**           | MVVM + Repository Pattern               |
| **Backend**                | Supabase (PostgreSQL + Auth + Realtime) |
| **Networking**             | Ktor Client 3.5.0                       |
| **Inyección Dependencias** | Koin 4.2.2                              |
| **Navegación**             | Navigation Compose 2.9.2 (multiplatform)|
| **Gestión Estado**         | Kotlin Coroutines + StateFlow           |
| **Serialización**          | Kotlinx Serialization                   |

### 🏗️ Arquitectura

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con arquitectura limpia en capas:

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                   │
│  ┌──────────────┐          ┌──────────────────────────┐ │
│  │  Compose UI  │ ◄──────► │      ViewModels          │ │
│  │  (Screens)   │          │  (StateFlow + Coroutines)│ │
│  └──────────────┘          └──────────┬───────────────┘ │
└───────────────────────────────────────┼─────────────────┘
                                        │
┌───────────────────────────────────────┼─────────────────┐
│                     DOMAIN LAYER      │                 │
│                  ┌────────────────────▼──────────────┐  │
│                  │   Repository Interfaces           │  │
│                  │   (Abstraction Layer)             │  │
│                  └─────────────────────┬─────────────┘  │
└────────────────────────────────────────┼────────────────┘
                                         │
┌────────────────────────────────────────┼─────────────────┐
│                      DATA LAYER        │                 │
│  ┌───────────────────────────────────┐ │                 │
│  │   Repository Implementations      │ │                 │
│  └────────┬──────────────────────────┘ │                 │
│           │                            │                 │
│  ┌────────▼─────────┐    ┌─────────────▼──────────────┐  │
│  │  Ktor Client     │    │  Supabase Client           │  │
│  │  (HTTP/REST)     │    │  (Auth + Realtime)         │  │
│  └──────────────────┘    └────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
                            │
                            ▼
              ┌──────────────────────────┐
              │   Supabase Backend       │
              │  - PostgreSQL Database   │
              │  - Authentication        │
              │  - Realtime (WebSockets) │
              │  - RLS Policies          │
              └──────────────────────────┘
```

**Flujo de Datos:**

1. **UI** → El usuario interactúa con Composables
2. **ViewModel** → Gestiona el estado y ejecuta lógica de negocio
3. **Repository** → Abstrae el origen de datos
4. **API/Database** → Ktor Client realiza peticiones HTTP a Supabase
5. **Realtime Updates** → WebSockets notifican cambios en tiempo real
6. **State Update** → StateFlow emite nuevos estados → UI se recompone

---

## 🇬🇧 English

### 📱 Description

**Spain Decides** is a citizen participation mobile application that empowers citizens to create,
vote, and discuss proposals on public interest topics. Built with native multiplatform technology,
it enables users to actively participate in democracy from their Android and iOS devices.

The app facilitates democratic debate organized by categories such as **Economy**, **Healthcare**, *
*Education**, **Environment**, **Justice**, and more, allowing citizens' voices to be heard in
real-time.

### ✨ Key Features

- 🗳️ **Voting System**: Vote for or against proposals with an intuitive upvote/downvote system
- 📝 **Proposal Creation**: Any user can create proposals with title and detailed description
- 🏷️ **Thematic Categories**: Proposals organized by areas (economy, healthcare, education, etc.)
- ⚡ **Real-time Updates**: Instant synchronization of votes and new proposals via WebSockets
- 🔒 **Secure Authentication**: Login system with email/password and Google OAuth
- 📊 **Popularity Ranking**: Proposals sorted by net votes (upvotes - downvotes)
- 🎨 **Material Design 3**: Modern interface with light/dark theme support
- 🌐 **Multiplatform**: Same experience on Android and iOS with shared codebase

### 📸 Screenshots

> 🚧 Coming soon: Screenshots of main screens (Categories, Proposal List, Detail, Creation)

### 📥 Download

The application will be available soon on:

- 📱 **Google Play Store**: _Link coming soon_
- 🍎 **Apple App Store**: _Link coming soon_

### 🛠️ Technology Stack

SpainDecides is built with cutting-edge technologies:

| Component                | Technology                              |
|--------------------------|-----------------------------------------|
| **Language**             | Kotlin 2.4.0                            |
| **UI Framework**         | Compose Multiplatform 1.11.1            |
| **Architecture**         | MVVM + Repository Pattern               |
| **Backend**              | Supabase (PostgreSQL + Auth + Realtime) |
| **Networking**           | Ktor Client 3.5.0                       |
| **Dependency Injection** | Koin 4.2.2                              |
| **Navigation**           | Navigation Compose 2.9.2 (multiplatform)|
| **State Management**     | Kotlin Coroutines + StateFlow           |
| **Serialization**        | Kotlinx Serialization                   |

### 🏗️ Architecture

The application follows the **MVVM (Model-View-ViewModel)** pattern with clean architecture layers:

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                   │
│  ┌──────────────┐          ┌──────────────────────────┐ │
│  │  Compose UI  │ ◄──────► │      ViewModels          │ │
│  │  (Screens)   │          │  (StateFlow + Coroutines)│ │
│  └──────────────┘          └──────────┬───────────────┘ │
└───────────────────────────────────────┼─────────────────┘
                                        │
┌───────────────────────────────────────┼──────────────────┐
│                     DOMAIN LAYER      │                  │
│                  ┌────────────────────▼────────────────┐ │
│                  │   Repository Interfaces             │ │
│                  │   (Abstraction Layer)               │ │
│                  └─────────────────────┬───────────────┘ │
└────────────────────────────────────────┼─────────────────┘
                                         │
┌────────────────────────────────────────┼─────────────────┐
│                      DATA LAYER        │                 │
│  ┌───────────────────────────────────┐ │                 │
│  │   Repository Implementations      │ │                 │
│  └────────┬──────────────────────────┘ │                 │
│           │                            │                 │
│  ┌────────▼─────────┐    ┌─────────────▼──────────────┐  │
│  │  Ktor Client     │    │  Supabase Client           │  │
│  │  (HTTP/REST)     │    │  (Auth + Realtime)         │  │
│  └──────────────────┘    └────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
                            │
                            ▼
              ┌──────────────────────────┐
              │   Supabase Backend       │
              │  - PostgreSQL Database   │
              │  - Authentication        │
              │  - Realtime (WebSockets) │
              │  - RLS Policies          │
              └──────────────────────────┘
```

**Data Flow:**

1. **UI** → User interacts with Composables
2. **ViewModel** → Manages state and executes business logic
3. **Repository** → Abstracts data source
4. **API/Database** → Ktor Client makes HTTP requests to Supabase
5. **Realtime Updates** → WebSockets notify changes in real-time
6. **State Update** → StateFlow emits new states → UI recomposes

---

## 🚀 Getting Started (Development)

### Prerequisites

- **Android Studio** Ladybug | 2024.2.1 or later
- **Xcode** 15.0+ (for iOS development on macOS)
- **JDK** 17 or later
- **Kotlin** 2.4.0 (included in project)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/apptolast/SpainDecides.git
   cd SpainDecides
   ```

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an Existing Project"
    - Navigate to the cloned `SpainDecides` folder

3. **Configure credentials**
    - Copy `local.properties.template` to `local.properties` and fill in the Supabase, Google OAuth
      and other credentials. See [SETUP.md](SETUP.md) for step-by-step instructions.
    - Add `composeApp/google-services.json` (Android) and `iosApp/iosApp/GoogleService-Info.plist`
      (iOS) from your Firebase project. See `docs/PUSH_NOTIFICATIONS_SETUP.md`.

4. **Sync Gradle**
    - Android Studio will automatically sync Gradle dependencies
    - Wait for the sync to complete

> **Note on build variants:** the Android app has two product flavors — `dev` (development backend,
> applicationId `com.apptolast.spaindecides.dev`) and `prod` (production backend). The flavor
> selects the backend environment; the build type (debug/release) does not.

### Build and Run

#### Android

**Option 1: Using Android Studio**

- Select the `composeApp` run configuration and the `devDebug` build variant
- Click the "Run" button (or press `Shift + F10`)

**Option 2: Using Terminal**

```bash
# Build debug APK (dev backend)
./gradlew :composeApp:assembleDevDebug

# Install on connected device/emulator
./gradlew :composeApp:installDevDebug

# Production build
./gradlew :composeApp:assembleProdRelease
```

**Run on emulator:**

```bash
# Start emulator (if not running)
emulator -avd Pixel_8_API_35

# Install and launch (note the .dev applicationId suffix of the dev flavor)
./gradlew :composeApp:installDevDebug
adb shell am start -n com.apptolast.spaindecides.dev/com.apptolast.spaindecides.MainActivity
```

#### iOS

**Option 1: Using Xcode**

1. Open the `/iosApp` directory in Xcode
2. Select your target device or simulator
3. Click the "Run" button (or press `Cmd + R`)

**Option 2: Build Kotlin Framework**

```bash
# For iOS Simulator (ARM64)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# For iOS Device (ARM64)
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

### Run Tests

```bash
# Run all common tests
./gradlew :composeApp:cleanTestDevDebugUnitTest :composeApp:testDevDebugUnitTest

# Run a single test class
./gradlew :composeApp:testDevDebugUnitTest --tests "com.apptolast.spaindecides.ComposeAppCommonTest"
```

---

## 📂 Project Structure

```
SpainDecides/
├── composeApp/                    # Multiplatform module
│   └── src/
│       ├── commonMain/            # Shared code (Android + iOS)
│       │   ├── kotlin/com/apptolast/spaindecides/
│       │   │   ├── presentation/  # UI Layer (MVVM)
│       │   │   │   ├── ui/
│       │   │   │   │   ├── screens/      # Composable screens
│       │   │   │   │   ├── components/   # Reusable UI components
│       │   │   │   │   └── theme/        # Material 3 theming
│       │   │   │   └── viewmodel/        # ViewModels
│       │   │   ├── domain/        # Domain Layer
│       │   │   │   └── repository/       # Repository interfaces
│       │   │   ├── data/          # Data Layer
│       │   │   │   ├── model/            # Data models
│       │   │   │   ├── remote/           # Ktor API services
│       │   │   │   └── repository/       # Repository implementations
│       │   │   └── di/            # Dependency Injection (Koin)
│       │   └── composeResources/  # Shared resources
│       ├── androidMain/           # Android-specific code
│       │   └── kotlin/
│       │       └── MainActivity.kt
│       ├── iosMain/               # iOS-specific code (Kotlin)
│       │   └── kotlin/
│       │       └── MainViewController.kt
│       └── commonTest/            # Shared tests
├── iosApp/                        # iOS application entry point
│   └── iosApp/
│       └── iOSApp.swift
├── gradle/                        # Gradle configuration
│   └── libs.versions.toml         # Version catalog
└── build.gradle.kts               # Root build script
```

---

## 🤝 Contributing

Contributions are welcome! If you'd like to contribute to SpainDecides:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please ensure your code follows the existing code style and includes appropriate tests.

---

## 👨‍💻 Author

**AppToLast**

- GitHub: [@apptolast](https://github.com/apptolast)
- Website: [apptolast.com](https://apptolast.com)

---

## 🙏 Acknowledgments

- Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- UI powered by [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- Backend by [Supabase](https://supabase.com)
- Icons from [Material Design Icons](https://fonts.google.com/icons)

---

## 📚 Additional Resources

- [CLAUDE.md](CLAUDE.md) - Comprehensive development guidelines and architecture documentation
- [Kotlin Multiplatform Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform Documentation](https://github.com/JetBrains/compose-multiplatform)
- [Ktor Client Documentation](https://ktor.io/docs/client-create-multiplatform-application.html)
- [Koin Documentation](https://insert-koin.io)

---

<div align="center">
  <p>Made with ❤️ for democratic participation</p>
  <p>⭐ If you find this project useful, please consider giving it a star!</p>
</div>
