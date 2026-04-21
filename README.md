# Connectify Flow

<p align="center">
  <img width="200px" height="200px" alt="WhatsApp Image 2026-04-19 at 02 22 22" src="https://github.com/user-attachments/assets/69099494-967d-4718-a5cf-756120d7eb5f" />
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-1.9+-blue.svg" alt="Kotlin"></a>
  <a href="https://developer.android.com/jetpack"><img src="https://img.shields.io/badge/Android-Jetpack-green.svg" alt="Android"></a>
  <a href="https://dagger.dev/hilt/"><img src="https://img.shields.io/badge/Hilt-Dependency%20Injection-orange.svg" alt="Hilt"></a>
</p>

Connectify Flow is a high-performance Android application engineered for real-time communication, combining WebSocket, HTTP APIs, and WebView integration into a cohesive and efficient user experience. The project emphasizes reactive state management, battery-aware lifecycle handling, and a clean, testable architecture.

---

## 🎥 Demo
<p align="center">
  <a href="https://www.youtube.com/shorts/B1dWRi9kpqo">
    <img src="https://img.youtube.com/vi/B1dWRi9kpqo/0.jpg" width="600" alt="Watch the video"/>
  </a>
</p>

<p align="center">
  ▶️ Click to watch the demo
</p>

---

## 🔄 Connection & Heartbeat Flow

<img width="1576" height="806" alt="image" src="https://github.com/user-attachments/assets/5cf869a7-8ff4-4aeb-b0cb-92f4a7727d91" />


1. **Connected State (User Interaction)**  
When the connection is active, the bird presses the button, indicating that communication with the server is available. A message is sent through the WebSocket, and the UI reflects a healthy and responsive connection.

2. **Awaiting and Receiving Response**  
After sending the message, the system waits for a server response. Once received, the UI updates to reflect successful communication, keeping the button active since the connection is still established.

3. **Disconnected State**  
When the connection is lost, the button becomes inactive, visually indicating that no interaction is possible. The system stops communication attempts and informs the user, allowing a manual retry.

---

### 🔄 Heartbeat Mechanism

While connected, a heartbeat cycle runs every 30 seconds:

- The system automatically sends a `"hello"` message through the WebSocket connection  
- It then **waits for the server response** to confirm the communication round-trip  
- This ensures the connection remains alive and responsive  
- It helps detect **silent connection drops** that would otherwise go unnoticed  
- The interaction is visually represented by the bird animation pressing the button  

### 🔋 Efficiency & Resource Management

To prevent unnecessary battery and network usage:

- The WebSocket performs a **limited number of automatic retries** when a disconnection is detected  
- After reaching the retry limit, automatic attempts stop  
- The user must manually trigger a new connection  

This approach ensures:

- No infinite retry loops  
- Reduced battery consumption  
- Controlled network usage  
- Better user experience  

---

## 📦 Download APK

You can download the latest published APK from the release below:

- [Download APK - v1.0.0](https://github.com/victorbezerra-dev/connectify-flow/releases/tag/v1.0.0)

> ⚠️ Make sure to download the APK asset attached to the GitHub release page.

### 📱 Installation Instructions

1. Download the APK file to your Android device  
2. Enable **"Install from unknown sources"** (if not already enabled)  
3. Open the APK file  
4. Tap **Install** and wait for the process to finish  

## 🧠 Memory Leak Detection
During development, **LeakCanary** was integrated to proactively detect memory leaks and ensure proper lifecycle management.

<div align="center">
  <img width="180" height="300" alt="image" src="https://github.com/user-attachments/assets/435c178a-00e0-45e4-908c-6d6901a5f213" />
</div>


- Automatically monitors object retention and reports potential leaks  
- Helped validate that screens, ViewModels, and WebSocket-related components are correctly released  
- Ensured that no unnecessary references were kept after navigation or lifecycle changes  
- Contributed to a more stable and memory-efficient application  

> 🛠️ LeakCanary was used only in debug builds and is not included in the production APK.


# 🧪 Code Quality & Static Analysis Strategy

To ensure high code quality, consistency, and long-term maintainability, this project integrates a robust suite of static analysis tools into both the local development workflow and the CI pipeline.

---

## 🛠️ Tooling Overview

| Tool | Scope | Primary Objective |
| :--- | :--- | :--- |
| **Android Lint** | Android Platform | Detects performance issues, incorrect API usage, and lifecycle-related bugs. |
| **ktlint** | Style & Formatting | Enforces official Kotlin coding conventions and maintains a unified style. |
| **detekt** | Complexity & Design | Identifies code smells, high cyclomatic complexity, and potential logic errors. |

---



## 🔁 Local Workflow (Pre-commit)

We maintain a fast feedback loop during development. Before each commit, the following checks are executed to ensure the codebase remains clean without hindering productivity.

## ⚙️ Setup (First Time Only)

After cloning the repository, you need to install the Git hooks:

```bash
./scripts/setup-hooks.sh
````

This script configures the pre-commit hooks that automatically trigger the quality checks before each commit.

## 🧪 Automated Checks
### Execution Commands:
```bash
./gradlew ktlintFormat  # Automatically fixes formatting issues
./gradlew ktlintCheck   # Validates that all code adheres to style guides
./gradlew detekt        # Analyzes code quality and architectural complexity
````

> [!IMPORTANT]  
> **Why skip Android Lint locally?**  
> Android Lint is a resource-intensive tool that performs deep analysis of resources and binaries.  
> To keep the developer workflow fluid and ensure near-instant commits, full Lint scans are delegated to the CI environment.


## 🚀 Continuous Integration (CI) - GitHub Actions

The CI pipeline, powered by GitHub Actions, acts as the **final quality gate**.  
Every Pull Request triggers a workflow to ensure that no code is merged without passing the full suite of automated inspections.

---

### ✅ Automated Checks

- **ktlintCheck**  
  Guarantees 100% style consistency across the codebase.

- **detekt**  
  Blocks code that exceeds complexity thresholds or contains discouraged patterns.

- **lint**  
  Runs a comprehensive Android platform analysis (**Full Lint**) to prevent production-level regressions.

---

### ⚙️ Workflow Trigger

The pipeline is configured to run on:

- Every push to the `main` branch  
- All `pull_request` events  

**File:** `.github/workflows/ci.yml`

```yaml
jobs:
  static-analysis:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'zulu'

      - name: Run Quality Checks
        run: ./gradlew ktlintCheck detekt lint
````

## 🏗️ Architecture Overview

This project follows a **feature-first modular architecture**, where each business domain is isolated into its own module.  
Instead of organizing the codebase by technical layers globally (e.g., activities, view models, repositories), we group everything by **feature**, improving scalability and maintainability.

---

### 📦 Project Structure
<div align='left'>
  <img width="280" height="300" alt="image" src="https://github.com/user-attachments/assets/1fe437f0-c3c3-4375-ba2c-c024949eda40" />
</div>


## 🚀 Running the Project

Follow the steps below to set up and run the project locally.

---

### 🧰 Requirements

Make sure you have the following installed:

- **Android Studio**: Android Studio Panda 3 | 2025.3.3 Patch 1
- **JDK**: Version 17 (recommended)
- **Android SDK**: Installed via Android Studio
- **Git**: For cloning the repository

---

### 📦 Clone the Repository

```bash
git https://github.com/victorbezerra-dev/connectify-flow/
cd connectify-flow
````


1.  **Open Android Studio**
2.  Click on **"Open"**
3.  Select the project folder

Android Studio will automatically:
* Sync Gradle
* Download dependencies
* Configure the project

---

### 🔌 Setup Git Hooks (Recommended)

To ensure code quality checks run before every commit, run the following command in your terminal:

```bash
./scripts/setup-hooks.sh
````

### ▶️ Run the App

Click on **Run ▶** or press `Shift + F10`.

---

### 🛠️ Build Variants

You can run different build variants (e.g., `debug` / `release`):

1. Go to the **Build Variants** panel.
2. Select the desired variant (usually `debug`).

## ⚠️ Troubleshooting

### ❌ Gradle sync failed?
Try:
- `File > Sync Project with Gradle Files`

---

### 🛠️ Build issues?
Try:
- `Build > Clean Project`
- Then `Build > Rebuild Project`

---

### 📱 Device not detected?
Make sure:
- USB Debugging is enabled on your device
- Proper device drivers are installed (for Windows)

## 🧰 Tech Stack

- Kotlin
- Jetpack Compose / XML
- Coroutines + Flow
- Hilt (DI)
- WebSocket (OkHttp)
- WebView
- LeakCanary
- JUnit + Truth + Mockk + Turbine
- Detekt + Ktlint
- GitHub Actions (CI)


## 🧪 Testing Strategy

The project follows a layered testing approach:

- **Unit Tests**
  - ViewModels
  - Coordinators (connection & heartbeat logic)
  - WebSocket abstraction

- **Tools**
  - JUnit
  - Mockk
  - Turbine (Flow testing)
  - Truth (assertions)

- **Focus**
  - Deterministic coroutine testing using TestDispatcher
  - State-driven validation (StateFlow)
  - Happy path + failure scenarios

## 💉 Dependency Injection

This project uses **Hilt** to manage dependencies and improve testability.

- Interfaces are injected instead of implementations (e.g., `WebSocketClient`)
- Coroutine dispatchers are injected for better test control
- Coordinators and ViewModels are fully decoupled from concrete implementations

## 🧠 Design Decisions

### Why WebSocket + Heartbeat?
To ensure real-time communication and detect silent disconnections.

### Why stop retries?
To prevent battery drain and infinite retry loops.

### Why feature-first architecture?
To improve scalability and reduce coupling between domains.

### Why inject CoroutineDispatcher?
To make coroutine-based logic fully testable and deterministic.

## 🔋 Performance Considerations

- WebSocket lifecycle tied to UI visibility
- Heartbeat only active when needed
- Retry strategy with limits
- WebView optimized for reduced resource usage

## 🛣️ Roadmap

- [ ] Offline support
- [ ] Push notifications integration
- [ ] Metrics & monitoring (Crashlytics / Logs)

## 🤝 Contributing

Contributions are welcome!

1. Fork the project
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request
