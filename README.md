# Connectify Flow 🌊

<p align="center">
  <img width="200px" height="200px" alt="Connectify Flow Logo" src="https://github.com/user-attachments/assets/69099494-967d-4718-a5cf-756120d7eb5f" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0-blue?style=for-the-badge&logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Android-Jetpack_Compose-green?style=for-the-badge&logo=android" alt="Android">
  <img src="https://img.shields.io/badge/Hilt-DI-orange?style=for-the-badge" alt="Hilt">
  <img src="https://img.shields.io/badge/Architecture-Clean_Modular-black?style=for-the-badge" alt="Architecture">
</p>

<p align="center">
  <strong>Connectify Flow</strong> is a high-performance Android application engineered for resilient real-time communication. It demonstrates the seamless integration of WebSockets, REST APIs, and reactive animations within a battery-aware and modular architecture.
</p>

<p align="center">
  <a href="#-about">About</a> •
  <a href="#-demo">Demo</a> •
  <a href="#-how-it-works">How it Works</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-tech-stack">Tech Stack</a> •
  <a href="#-quality--testing">Quality</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-download">Download</a>
</p>


---

## 📖 About

Connectify Flow was built to serve as a technical reference for implementing professional-grade real-time systems on Android. The project emphasizes **reactive state management**, **lifecycle-aware communication**, and a clean, testable codebase. It solves common mobile challenges such as maintaining stable persistent connections and providing meaningful UI feedback during network fluctuations.

---

## 🎥 Demo

### Video Demonstration
<p align="center">
  <a href="https://www.youtube.com/shorts/B1dWRi9kpqo">
    <img src="https://img.youtube.com/vi/B1dWRi9kpqo/0.jpg" width="600" alt="Watch the video"/>
  </a>
  <br>
  <em>▶️ Click to watch the full demonstration on YouTube</em>
</p>

---

## 🔄 How it Works

The core of the application is its resilient communication engine, designed to be both informative and resource-efficient.

### 1. Connection Lifecycle & States
The UI reacts instantly to four primary connection states:
- **Connected**: The bird animation (Rive) interacts with the button, signaling an active and healthy link.
- **Awaiting/Connecting**: The system negotiates the handshake or waits for a server "pong".
- **Disconnected/Error**: Interaction is disabled, and the UI provides a clear path for manual recovery.

### 2. Heartbeat Mechanism 💓
To prevent "ghost connections" (where the OS reports connectivity but the data tunnel is broken), we implement a 30-second heartbeat cycle:
- **Automatic Pulse**: The app sends a `"hello"` message every 30 seconds.
- **Round-trip Validation**: The connection is only considered healthy if a `"pong"` response is received.
- **Visual Feedback**: The countdown and bird interactions keep the user informed of the background health checks.

### 3. Resource & Battery Management 🔋
- **Retry Strategy**: The WebSocket performs a **limited number** of automatic reconnection attempts.
- **Back-off Logic**: If the network remains unstable, the app stops background attempts to prevent battery drain, delegating the next attempt to the user.

---

## 🏗️ Architecture

Connectify Flow follows a **feature-first modular architecture** combined with **Clean Architecture** principles.

### 📦 Modular Structure
<div align='left'>
  <img width="280" height="300" alt="Project Structure" src="https://github.com/user-attachments/assets/1fe437f0-c3c3-4375-ba2c-c024949eda40" />
</div>

- **`Presentation`**: Reactive UI using Jetpack Compose and ViewModels powered by `StateFlow`.
- **`Domain`**: Pure business logic, models, and repository interfaces (framework-independent).
- **`Infra` (Data)**: Repository implementations, OkHttp/Retrofit configurations, and WebSocket logic.
- **`Core`**: Shared abstractions, global utilities, and base infrastructure components.

---

## 🛠️ Tech Stack

- **Linguagem**: [Kotlin 2.0](https://kotlinlang.org/) (Coroutines + Flow)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Networking**: [OkHttp](https://square.github.io/okhttp/) (WebSockets) & [Retrofit](https://square.github.io/retrofit/)
- **Animations**: [Rive Android](https://rive.app/) (Interactive state-driven animations)
- **Memory Analysis**: [LeakCanary](https://square.github.io/leakcanary/) (Proactive leak detection)
- **Testing**: JUnit, Mockk, Turbine (Flow testing), and Truth (assertions)

---

## 🧪 Quality & Testing Strategy

To maintain a high-quality codebase, we use a multi-layered verification strategy:

### Static Analysis
| Tool | Scope | Objective |
| :--- | :--- | :--- |
| **Android Lint** | Android Platform | Detects performance issues and incorrect API usage. |
| **ktlint** | Style | Enforces official Kotlin coding conventions. |
| **detekt** | Complexity | Identifies code smells and high cyclomatic complexity. |

### Testing Focus
- **Unit Tests**: Coverage for ViewModels, Coordinators, and WebSocket abstractions.
- **Deterministic Testing**: Injection of `TestDispatcher` to ensure reliable Coroutine testing.
- **State Validation**: Using **Turbine** to verify `StateFlow` emissions during complex connection flows.

---

## 🧠 Design Decisions

- **Why Feature-First?** To improve scalability and allow independent development/testing of business domains.
- **Why Hilt?** For industry-standard DI that simplifies scope management (e.g., Singleton for WebSocket vs ViewModelScope).
- **Why Limited Retries?** To respect the user's device resources, avoiding infinite background loops that consume data and battery.
- **Why Rive?** To bridge the gap between technical connection states and delightful user experience through interactive animations.

---

## 🚀 Getting Started

### Requirements
- **Android Studio**: Panda (2025.3.3+)
- **JDK**: Version 17
- **Device**: Android SDK 24+

### Installation & Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/victorbezerra-dev/connectify-flow/
   cd connectify-flow
   ```
2. **Setup Git Hooks** (Recommended for contributors):
   ```bash
   ./scripts/setup-hooks.sh
   ```
3. **Build & Run**: Open in Android Studio and click the **Run ▶** button.

---

## 📦 Download

You can test the latest stable version by downloading the APK from our releases:

- [📦 Download Connectify Flow v1.0.0](https://github.com/victorbezerra-dev/connectify-flow/releases/tag/v1.0.0)

> [!NOTE]  
> Make sure to enable "Install from unknown sources" on your Android device settings.

---

## 🛣️ Roadmap
- [ ] Offline support & Data persistence
- [ ] Push notifications for real-time alerts
- [ ] Enhanced monitoring (Firebase Crashlytics & Analytics)
- [ ] Multi-module navigation refactoring

---

## 🤝 Contributing
1. Fork the project.
2. Create a feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

<p align="center">
  Developed with ❤️ by <a href="https://github.com/victorbezerra-dev">Victor Bezerra</a>
</p>
