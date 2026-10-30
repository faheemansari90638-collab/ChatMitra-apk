# ChatMitra - Modern Android Messaging & Calling App

**ChatMitra** is a modern, responsive, privacy-focused Android instant messaging and calling application built natively with **Kotlin**, **Jetpack Compose**, and **Material Design 3**.

---

## 🌟 Core Features

### 1. User Registration & Authentication
- **Phone Number & OTP Flow**: Instant sign-in with country code picker (`+91`, `+1`, `+44`, etc.) and 6-digit OTP verification.
- **Profile Customization**: Customizable display name, About status presets, avatar emojis, and color palettes.
- **Account Control**: Account deletion and secure logout.

### 2. Real-Time Chat Experience
- **Instant Messaging**: One-to-one direct messages and group chats.
- **Delivery Receipts**: Accurate message status ticks (Sending 🕒, Sent ✓, Delivered ✓✓, Read ✓✓ in blue).
- **Interactive Message Actions**:
  - Quoted message reply preview
  - Emoji reactions (👍, ❤️, 😂, 😮, 😢, 🙏)
  - Copy message text to clipboard
  - Star / unstar important messages
  - In-place message editing with "Edited" badge
  - Delete for me / Delete for everyone
- **Voice Notes**: Dedicated voice recording with live recording timer, wave pulse, and playback duration.
- **Live Typing Indicator**: Real-time "typing..." and online status feedback.
- **Chat Management**: Pin chats, archive chats, mute notifications, and clear history.

### 3. Media & File Sharing
- Share photos with visual preview cards.
- Send documents (PDF, JSON) with file size indicator.
- Send voice notes and audio clips.

### 4. WebRTC Voice & Video Calls
- One-to-one high-definition audio and video calling.
- WebRTC P2P signaling with Google STUN configuration (`stun:stun.l.google.com:19302`).
- Call screen with pulsating audio radar, video PIP preview, microphone mute, speaker toggle, camera switch, and call timer.
- Detailed Call History logs with Incoming (green), Outgoing (teal), and Missed (red) status badges.

### 5. Status / Stories (24-Hour Expiration)
- Share text and photo status updates with customizable vibrant gradient backgrounds.
- 24-hour automatic expiration indicator.
- Fullscreen story viewer with segmented progress bars (tap to navigate, hold to pause).
- Viewer tracking list sheet showing who saw your status.

### 6. Groups & Communities
- Create groups, add participants, and set group description and rules.
- Group admin roles and member management.
- Shareable group invite links with instant reset/revocation controls.

### 7. Genuine End-to-End Encryption & Privacy
- **E2EE Safety Numbers**: 60-digit security code and visual QR code verification for chat encryption (AES-256-GCM & RSA-4096).
- **Privacy Controls**:
  - Last seen & Online visibility (Everyone, My Contacts, Nobody)
  - Profile photo visibility
  - About text visibility
  - Status updates privacy
  - Read receipts (blue double checkmarks) toggle
  - App lock (Biometric / PIN) toggle
  - Blocked contacts list manager

### 8. UI & Design System
- Modern Emerald Teal (`#00A884`) and Warm Amber accent palette.
- Bottom Navigation: **Chats**, **Updates**, **Calls**, **Settings**.
- Search bar with quick filter chips (**All**, **Unread**, **Groups**).
- Custom adaptive app icon and high-resolution welcome hero branding.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (BOM 2024.09.00)
- **Design System**: Material Design 3 (M3) with dynamic edge-to-edge support
- **State Management**: MVVM Architecture with Kotlin Coroutines & `MutableStateFlow`
- **Navigation**: Jetpack Navigation Compose with type-safe state transitions and `BackHandler`
- **Image Loading**: Coil Compose
- **Testing**: JUnit 4 & Robolectric for JVM-based local testing

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17 or JDK 21
- Android SDK 34+

### Clone & Run
```bash
# Clone the repository
git clone <repo-url>
cd chatmitra

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Build debug APK
gradle :app:assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Automated Tests
Run automated test suite covering:
- App identity & string resource resolution
- Repository initial state & seeded conversations
- Message transmission and delivery tick progression
- Status stories creation and viewer counts
- Group creation and participant management
- WebRTC call state machine lifecycle
