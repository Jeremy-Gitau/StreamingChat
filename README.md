# StreamingChat

A Compose Multiplatform chat app (Android, iOS, Desktop) that streams AI replies token by token from a local [Ollama](https://ollama.com) server, including the model's live "thinking" output.

- **Two streaming engines**, switchable at runtime in settings: a hand-built SSE pipeline on Ktor, or [JetBrains Koog](https://github.com/JetBrains/koog).
- **Ollama** powers the replies. The app uses `nemotron-3-super:cloud` by default; pull and run it with `ollama run nemotron-3-super:cloud`, or use any model you like.
  - Change the model in `shared/src/commonMain/.../domain/model/AiModel.kt` (`ModelCatalog`).
  - Change the server URL in the platform `HttpClientFactory.*.kt` files (`defaultOllamaBaseUrl`). It defaults to `localhost:11434`, or `10.0.2.2:11434` on the Android emulator.
- Uses Koin for DI, retries failed streams, and lets you set a system prompt.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- Desktop tests: `./gradlew :shared:jvmTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…