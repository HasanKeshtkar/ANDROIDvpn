# VPN Manager for Android (Multi-Tunnel Router)

A high-performance Android VPN client with a cyber-minimalist terminal UI, inspired by the Linux `vpn` TUI. It runs **WireGuard**, **V2Ray/Xray**, **OpenVPN**, and **Direct** connections simultaneously inside a single user-space Android `VpnService` with per-app and per-domain routing rules.

---

## 📸 Architecture & Design

In standard Android, only **one** `VpnService` can be active at any time. This application solves that limitation by running a unified multi-protocol engine in user-space (`libbox` / `sing-box` core) within the Android VPN service:

```
[ Android Apps / Network Traffic ]
               │
               ▼
   [ Android VpnService (tun0) ]
               │
  ┌────────────┴────────────┐
  │  SingBox / libbox Core  │
  │  - DNS Sniffing         │
  │  - Per-App UID Match    │
  │  - Routing Engine       │
  └────────────┬────────────┘
               │
   ┌───────────┼───────────┬───────────┬───────────┐
   ▼           ▼           ▼           ▼           ▼
[ WireGuard ] [ V2Ray ] [ OpenVPN ] [ Direct ]  [ Block ]
 (Go Userspace)(VLESS/VMess)(Endpoint) (Protect) (Blackhole)
```

### UI Comparison to Linux
- **Header:** Live upload/download bandwidth (`↓ 102 B/s  ↑ 102 B/s`) and connection status dot (`◆ vpn`).
- **TUNNELS Section:** Color-coded status dots (`● vray`, `● openvpn`, `● wireguard`) with real-time ping latency.
- **ROUTES Section:** Interactive routing table with app / site filters and custom tunnel assignment (`→ ● wireguard`).
- **RECENT Section:** Real-time log of recent routed IP destinations and domain names.
- **Commands Bar:** Quick actions (`↵ connect/disconnect`, `+ add route`, `p ping all`, `d default fallback`).

---

## 📁 Project Structure

```
ANDROIDvpn/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/vpnmanager/android/
│   │   │   ├── VpnApplication.kt          # App lifecycle & notification channel
│   │   │   ├── MainActivity.kt            # Entry Activity & VPN permission handler
│   │   │   ├── data/
│   │   │   │   ├── model/                 # Data models (Tunnel, RouteRule, ConnectionLog)
│   │   │   │   ├── generator/             # SingBoxConfigGenerator (JSON config builder)
│   │   │   │   └── repository/            # VpnRepository (State persistence & Live stats)
│   │   │   ├── service/
│   │   │   │   ├── AppVpnService.kt       # Android VpnService & TUN manager
│   │   │   │   └── VpnTileService.kt      # Android Quick Settings tile
│   │   │   └── ui/
│   │   │       ├── theme/                 # Dark terminal color palette & monospace type
│   │   │       ├── components/            # HeaderStats, TunnelItem, RouteRow, RecentLogView
│   │   │       └── screens/               # DashboardScreen
│   │   └── res/                           # Android drawables, colors, strings
│   ├── build.gradle.kts
│   └── libs/                              # Place libbox.aar here
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

---

## 🚀 How to Run & Build

### 1. Open in Android Studio
1. Open Android Studio.
2. Choose **Open an Existing Project** and select `/home/hassan/Work/ANDROIDvpn`.
3. Let Gradle sync dependencies.
4. Run on your Android device (Android 8.0 / API 26 or newer).

### 2. Build via Command Line
```bash
./gradlew assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.
