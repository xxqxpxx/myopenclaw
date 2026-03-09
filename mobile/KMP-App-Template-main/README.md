# Trading App - Kotlin Multiplatform

A clean Kotlin Multiplatform (KMP) trading application for Android and iOS.

## Project Structure

```
TradingApp/
├── composeApp/              # Main application module
│   ├── src/
│   │   ├── commonMain/     # Shared Compose UI code
│   │   ├── androidMain/    # Android-specific code
│   │   └── iosMain/        # iOS-specific code
│   └── build.gradle.kts
├── iosApp/                  # iOS wrapper application
│   └── iosApp/
│       ├── iOSApp.swift
│       └── ContentView.swift
├── gradle/
│   └── libs.versions.toml  # Centralized dependency management
├── build.gradle.kts         # Root build configuration
└── settings.gradle.kts      # Project settings
```

## Tech Stack

- **Kotlin**: 2.2.10
- **Compose Multiplatform**: 1.8.2
- **Ktor**: 3.1.3 (Networking)
- **Koin**: 4.1.0 (Dependency Injection)
- **Navigation Compose**: 2.9.0-beta03
- **Coil**: 3.2.0 (Image loading)
- **Kotlinx DateTime**: 0.6.1

## Requirements

### Android
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 26+ (minSdk)
- Target SDK 36

### iOS
- Xcode 15.0+
- macOS 13.0+
- CocoaPods (optional, for native dependencies)

## Getting Started

### 1. Clone and Setup

```bash
git clone <your-repo>
cd TradingApp
```

### 2. Run on Android

```bash
./gradlew :composeApp:assembleDebug
# or open in Android Studio and run
```

### 3. Run on iOS

```bash
# Open iOS project in Xcode
open iosApp/iosApp.xcodeproj

# Or use command line
cd iosApp
xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug
```

## Project Configuration

### Package Name
- **Android**: `com.tradingapp`
- **iOS**: `com.tradingapp`

### Version
- Version Code: 1
- Version Name: 1.0

## Key Features to Implement

This is a clean skeleton. Implement your trading app features:

1. **Authentication**
   - User login/signup
   - Session management

2. **Trading Features**
   - Market data visualization
   - Order placement
   - Portfolio tracking
   - Real-time price updates

3. **Navigation**
   - Bottom navigation
   - Screen navigation with Jetpack Navigation Compose

4. **Networking**
   - API integration with Ktor
   - WebSocket for real-time data

5. **State Management**
   - ViewModels with Koin DI
   - State flows for reactive UI

## Architecture

This app follows **MVVM (Model-View-ViewModel)** pattern:

- **Model**: Data classes, repositories (to be implemented)
- **View**: Composable UI functions in `commonMain`
- **ViewModel**: Business logic and state management

### Recommended Structure

```
composeApp/src/commonMain/kotlin/com/tradingapp/
├── data/
│   ├── model/          # Data models
│   ├── repository/     # Data repositories
│   └── remote/         # API services
├── ui/
│   ├── screens/        # Screen composables
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   └── theme/          # App theme
└── viewmodel/          # ViewModels
```

## Development Tips

1. **Platform-Specific Code**: Use `expect/actual` declarations for platform-specific implementations
2. **Resources**: Add resources in `composeApp/src/commonMain/composeResources/`
3. **Dependencies**: Update `gradle/libs.versions.toml` for version management
4. **Testing**: Add tests in `commonTest`, `androidTest`, and `iosTest` source sets

## Build & Deploy

### Android
```bash
./gradlew :composeApp:assembleRelease
```

### iOS
Build through Xcode for App Store deployment

## License

See LICENSE file for details.

## Next Steps

1. Set up your trading API integration
2. Implement authentication flow
3. Create market data screens
4. Add real-time updates with WebSocket
5. Implement order placement functionality
6. Add portfolio tracking
7. Set up proper error handling
8. Add unit and UI tests

Happy coding! 🚀
