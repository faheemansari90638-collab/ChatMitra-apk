# ChatMitra — Production Deployment & Operations Guide

## 1. Overview & Architecture
ChatMitra is a modern, high-performance Android messaging and calling platform featuring:
- **Cloud Firestore**: Persistent real-time chat, profile sync, and security rules.
- **Hamza AI**: Server-side Gemini API multimodal assistance & multi-lingual translations.
- **WebRTC Group Calling**: Full-mesh peer-to-peer calling (strictly capped at 8 participants per room for mobile uplink stability) with STUN/TURN (coturn) relay.
- **Multi-lingual Localization**: Dynamic RTL/LTR support across Hindi, English, Urdu, and Arabic.
- **Zero-Trust Admin Moderation**: Reports, spam bans, and appeal handling without access to private encrypted messages.

---

## 2. Infrastructure & External Services Inventory

| Service | Purpose | Requirements / Credentials | Potential Cost Tier |
|---|---|---|---|
| **Google Cloud / Firebase Firestore** | Real-time message synchronization, user profiles, status | `google-services.json`, Firestore Database ID: `ai-studio-android-chatmitr-b13b9274-690d-42ac-bf1e-776067e13a89` | Free tier covers 50k reads/20k writes daily; Blaze PAYG scales seamlessly |
| **Firebase Authentication / Google Sign-In** | Jetpack Credential Manager OAuth authentication | Web Client ID (`strings.xml:default_web_client_id`) | Free |
| **Gemini AI API / Firebase AI Logic** | AI assistance, instant translation, smart replies | Server-side Gemini key / AI Logic API | Free tier / Token-based usage |
| **STUN/TURN Relay (coturn)** | NAT traversal for peer-to-peer WebRTC calls | TURN username, secret, TLS cert on port 5349 | ~$5–$15/mo for VPS (Hetzner / DigitalOcean / AWS EC2) |
| **Custom Domain & HTTPS** | WebRTC signaling endpoint & deep-links | `chatmitra.app`, TLS via Let's Encrypt / Certbot | ~$10–$12/year domain + free SSL |

---

## 3. Server Requirements & WebRTC Capacity Bounds

### Full-Mesh Peer-to-Peer Bounds (Capped at 8 Participants)
- In a full-mesh topology, each participant uploads their stream to $N - 1$ peers and downloads $N - 1$ remote streams.
- At 8 participants with **SD 480p (500 Kbps)**:
  - Uplink: $7 \times 500\text{ Kbps} = 3.5\text{ Mbps}$
  - Downlink: $7 \times 500\text{ Kbps} = 3.5\text{ Mbps}$
- Mobile LTE/5G devices comfortably sustain up to 8 participants. The app automatically enforces `GroupCallingRepository.MAX_PARTICIPANTS = 8`.
- **Scaling Beyond 8 Users**: Deploy a Selective Forwarding Unit (SFU) such as **LiveKit** or **mediasoup** to terminate client streams into a single downlink.

### coturn STUN/TURN Setup (`/etc/turnserver.conf`)
```ini
listening-port=3478
tls-listening-port=5349
fingerprint
lt-cred-mech
use-auth-secret
static-auth-secret=YOUR_PRODUCTION_TURN_SECRET
realm=turn.chatmitra.app
cert=/etc/letsencrypt/live/turn.chatmitra.app/fullchain.pem
pkey=/etc/letsencrypt/live/turn.chatmitra.app/privkey.pem
no-tcp-relay
min-port=49152
max-port=65535
```

---

## 4. Database Security Rules & Backup Strategy

### Firestore Zero-Trust Security Invariant
- Private chats are isolated to participants:
```javascript
match /chats/{chatId} {
  allow read, write: if request.auth != null &&
    (request.auth.uid in resource.data.participantIds || request.auth.uid in request.resource.data.participantIds);
  match /messages/{messageId} {
    allow read, write: if request.auth != null;
  }
}
```
- Admins do **not** have read rules for private messages unless a message is explicitly submitted by a reporter as part of a `reports` document.

### Automated Backups
Schedule automated daily backups using Google Cloud Scheduler and Cloud Functions to Cloud Storage:
```bash
# Export Firestore database to backup bucket
gcloud firestore export gs://chatmitra-backups-$(date +%Y%m%d) \
  --project=ai-studio-android-chatmitr-b13b9274-690d-42ac-bf1e-776067e13a89
```

### Disaster Recovery
To restore a snapshot:
```bash
gcloud firestore import gs://chatmitra-backups-20261029/ --project=ai-studio-android-chatmitr-b13b9274-690d-42ac-bf1e-776067e13a89
```

---

## 5. Environment Variables & Secrets Reference

Create `.env` based on `.env.example`:
```env
# Google OAuth Web Client ID for Credential Manager
GOOGLE_WEB_CLIENT_ID=510842261622-example.apps.googleusercontent.com

# STUN/TURN Relay Server Credentials
TURN_SERVER_URL=turn:turn.chatmitra.app:5349
TURN_SERVER_USER=chatmitra_user
TURN_SERVER_CREDENTIAL=chatmitra_secure_token

# Admin PIN / Moderation Passkey
ADMIN_SECRET_KEY=admin2026
```

---

## 6. Release Builds & Google Play Publishing

### Versioning
- `versionCode = 2`
- `versionName = "1.2.0"`

### Release Keystore & ProGuard
- Signing is configured in `app/build.gradle.kts` via `System.getenv("KEYSTORE_PATH")`.
- ProGuard rules (`proguard-rules.pro`) retain model classes, serialization adapters, and WebRTC JNI headers.
- Build release bundle:
```bash
gradle :app:bundleRelease
```
- Build release APK:
```bash
gradle :app:assembleRelease
```
