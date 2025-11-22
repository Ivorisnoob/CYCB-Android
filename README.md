# CYCB Chat - Android Application

> **⚠️ EARLY DEVELOPMENT - BETA VERSION**  
> This app is currently in active development and may contain bugs, incomplete features, or breaking changes. Use at your own risk. Bug reports and feedback are welcome, but please be patient as features are still being implemented.

Modern Android chat application built with Jetpack Compose and Material 3 Expressive Design.

## Overview

CYCB Chat is a feature-rich real-time messaging application for Android that provides seamless communication through text, voice messages, images, GIFs, and voice calls. Built with modern Android development practices and Material Design 3.

## Tech Stack

### Core Technologies
- **Language**: Kotlin 2.1.20
- **UI Framework**: Jetpack Compose with Material 3 (1.5.0-alpha09)
- **Architecture**: MVVM with ViewModels and StateFlow
- **Minimum SDK**: 29 (Android 10)
- **Target SDK**: 35 (Android 15)
- **Build System**: Gradle 8.13.0 with Kotlin DSL

### Key Libraries

#### UI & Design
- Jetpack Compose BOM 2024.12.01
- Material 3 Expressive Components
- Material Icons Extended
- Coil 2.5.0 (Image loading with GIF support)

#### Networking & Real-time
- Retrofit 2.9.0 (REST API)
- OkHttp 4.12.0 (HTTP client with logging)
- Socket.IO Client 2.1.0 (Real-time messaging)
- Gson (JSON serialization)

#### Android Jetpack
- Navigation Compose 2.8.4
- Lifecycle & ViewModel 2.8.7
- DataStore Preferences 1.0.0
- Activity Compose 1.9.3

#### Voice & Media
- Agora RTC Voice SDK 4.6.0 (Voice calls)
- Android MediaRecorder (Voice messages)

#### Firebase
- Firebase BOM 32.7.0
- Firebase Cloud Messaging (Push notifications)
- Firebase Analytics

#### Other
- Kotlinx Coroutines 1.7.3
- Kotlinx Serialization 1.6.0

## Project Structure

```
app/src/main/java/com/cycb/chat/
├── data/
│   ├── api/              # API services and configuration
│   ├── local/            # Local data storage (DataStore)
│   ├── model/            # Data models
│   ├── preferences/      # User preferences
│   ├── socket/           # Socket.IO manager
│   └── storage/          # Token management
├── network/              # Network utilities (Tenor API)
├── receiver/             # Broadcast receivers
├── service/              # Background services (FCM, Call Overlay)
├── ui/
│   ├── auth/             # Login & Sign up screens
│   ├── components/       # Reusable UI components
│   ├── screens/          # App screens
│   └── theme/            # Material 3 theming
├── utils/                # Utility classes
├── viewmodel/            # ViewModels for screens
├── CYCBApplication.kt    # Application class
└── MainActivity.kt       # Main activity
```

## Features

### 💬 Messaging
- Real-time text messaging via Socket.IO
- Voice messages with recording and playback
- Image sharing with full-screen viewer and download
- GIF support via Tenor API integration
- Message reactions with emoji
- Message deletion
- Reply to messages
- Typing indicators with user avatars
- Read receipts
- System messages for group events

### 📞 Voice Calls
- One-on-one and group voice calls
- Powered by Agora RTC SDK
- Picture-in-Picture (PiP) mode support
- Floating overlay for calls (with SYSTEM_ALERT_WINDOW permission)
- Speaker/Listener mode switching
- Mute/Unmute functionality
- Call notifications with accept/reject actions
- In-call duration tracking
- Incoming call dialog with caller info

### 💬 Chat Management
- Direct messaging (1-on-1)
- Group chats with admin controls
- Public chat rooms discovery
- Custom chat backgrounds (per-chat)
- Pin important chats
- Unread message badges
- Search conversations
- Group member management (add/remove)
- Promote/demote group admins
- Leave group functionality
- Edit group name and description
- Group info screen with member list

### 👥 Social Features
- Friend system with friend requests
- User profiles with avatars and bio
- Online status indicators (real-time)
- User search functionality
- Notes/Status updates (60 character limit, 24h expiry)
- Dashboard with recent activity
- Online friends list
- Quick actions panel
- Follow/Unfollow users
- Profile picture upload

### 🎨 Customization & Themes
- **18 Pre-built Themes:**
  - Electric Sunset, Ocean Breeze, Forest Green
  - Royal Purple, Cherry Blossom, Midnight Blue
  - Sunset Orange, Mint Fresh, Rose Gold
  - Cyber Neon, Autumn Leaves, Arctic Ice
  - **Seasonal Themes:** Christmas Magic, Winter Wonderland, Valentine's Love, Halloween Spooky, Spring Bloom, Summer Vibes
- **Custom Theme Creator:**
  - Full color picker with hex input
  - Separate light/dark mode colors
  - Live preview with animated indicators
  - Quick presets (Vibrant, Nature, Ocean, Sunset)
  - Save unlimited custom themes
- Dark/Light mode toggle
- Material 3 Expressive design system
- Per-chat background customization
- Dynamic color support (Android 12+)
- Compact mode option

### 🔔 Notifications & Settings
- Firebase Cloud Messaging integration
- Push notifications for messages
- Friend request notifications
- Chat invite notifications
- Call notifications with actions
- Granular notification controls:
  - Toggle messages, friend requests, chat invites separately
  - Sound on/off
  - Vibration on/off
- Background notification support

### 🔒 Privacy & Security
- JWT authentication
- Read receipts toggle
- Typing indicator toggle
- Last seen visibility control
- Profile photo visibility control
- Password change functionality

### 📱 User Experience
- **Auto-update system:**
  - GitHub releases integration
  - In-app update checker
  - Download progress tracking
  - One-tap install
  - Release notes display
  - File size and version info
- **Media Handling:**
  - Auto-download media toggle
  - Auto-play GIFs toggle
  - Image caching with Coil
  - GIF support (ImageDecoder/GifDecoder)
  - Memory and disk cache optimization
- **UI/UX:**
  - Edge-to-edge display
  - Smooth animations and transitions
  - Material 3 Expressive components
  - Loading indicators (Wavy, Contained, Standard)
  - Pull-to-refresh
  - Shimmer loading effects
  - Swipeable tabs (Dashboard/Chats/Friends)
  - Bottom navigation with icons
  - Floating action buttons
  - Snackbar notifications

### 🛠️ Developer Features
- Comprehensive logging system
- Global exception handler
- API config with auto-detection (local/production)
- DataStore for preferences
- Offline-first architecture
- Coroutines for async operations
- StateFlow for reactive state
- ViewModel architecture
- Navigation Compose
- Dependency injection ready

## Permissions

The app requires the following permissions:

- `INTERNET` - Network communication
- `ACCESS_NETWORK_STATE` - Check connectivity
- `POST_NOTIFICATIONS` - Push notifications (Android 13+)
- `RECORD_AUDIO` - Voice messages and calls
- `MODIFY_AUDIO_SETTINGS` - Audio routing for calls
- `BLUETOOTH` & `BLUETOOTH_CONNECT` - Bluetooth audio devices
- `SYSTEM_ALERT_WINDOW` - Floating call overlay
- `REQUEST_INSTALL_PACKAGES` - App updates
- `WRITE_EXTERNAL_STORAGE` - File downloads (Android 9 and below)

## Building the Project

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35
- Gradle 8.13+

### Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard (currently disabled)

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on device
./gradlew installDebug
```

## Configuration

### API Configuration

The app uses dynamic API configuration in `ApiConfig.kt`:
- Automatically detects local development server
- Falls back to production server
- Configurable base URL and Socket.IO endpoint

### Firebase Setup

1. Create a Firebase project
2. Add Android app with package name `com.cycb.chat`
3. Download `google-services.json`
4. Enable Firebase Cloud Messaging
5. Configure notification channels

### Agora Voice SDK

The app uses Agora RTC SDK for voice calls. Ensure your backend provides valid Agora tokens for authentication.

## Version Information

- **Version Code**: 8
- **Version Name**: 0.6_BETA
- **Package Name**: com.cycb.chat

## Architecture

### MVVM Pattern
- **Model**: Data classes and repository pattern
- **View**: Composable functions
- **ViewModel**: Business logic and state management

### State Management
- Kotlin StateFlow for reactive state
- Compose State for UI state
- DataStore for persistent preferences

### Navigation
- Jetpack Navigation Compose
- Type-safe navigation arguments
- Deep linking support

### Dependency Injection
- Manual DI with singleton pattern
- ViewModels created with factory pattern

## Key Components

### SocketManager
Singleton class managing WebSocket connections:
- Real-time message delivery
- Typing indicators
- Call signaling
- Presence updates

### VoiceCallViewModel
Manages voice call state:
- Agora RTC engine integration
- Call lifecycle management
- Audio routing
- Participant tracking

### ChatsViewModel
Handles chat list:
- Chat synchronization
- Unread count management
- Real-time updates
- Local caching

### ChatRoomViewModel
Manages individual chat rooms:
- Message history
- Send/receive messages
- Media uploads
- Reactions and deletions

## Testing

The project currently focuses on manual testing. Future additions may include:
- Unit tests with JUnit
- UI tests with Compose Testing
- Integration tests

## Known Issues & Limitations

- ProGuard/R8 optimization disabled in release builds
- Minimum SDK 29 (no support for Android 9 and below)
- Voice calls require stable internet connection
- Some Material 3 Expressive components are in alpha

## Contributing

When contributing to this project:
1. Follow Kotlin coding conventions
2. Use Jetpack Compose best practices
3. Maintain MVVM architecture
4. Add appropriate logging
5. Test on multiple Android versions

## License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0).

**What this means:**
- ✅ Free for personal, educational, and open-source use
- ✅ You can modify and distribute the code
- ❌ Commercial use requires the entire application (including modifications) to be open-sourced under AGPL-3.0
- ❌ Running as a paid service requires releasing all source code to users

For commercial licensing inquiries, please contact the project maintainer.

See the [LICENSE](../LICENSE) file for full details.

## Support

For issues and questions:
- Check the backend API documentation
- Review Socket.IO event specifications
- Consult Material 3 design guidelines

---


---

**Made by Ivorisnoob**
