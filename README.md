# Codeforces Android App

A premium, native Android client for Codeforces built with Kotlin and Jetpack Compose. This app provides a seamless and modern experience for browsing competitive programming problems, viewing contests, and checking user profiles directly from your Android device.

## Features

- **Native Problem Rendering**: Eliminates the reliance on WebViews by parsing problem statements natively.
- **Clean Math Rendering**: Uses regex-based LaTeX-to-Unicode conversion and MathJax for beautiful inline and block mathematical formulas.
- **Interactive Sample I/O**: Easily view and copy sample inputs and outputs for problems.
- **Bypass Scraping Restrictions**: Features a robust network layer designed to smoothly handle Codeforces' 403 Forbidden responses with proper session management and browser headers.
- **Modern Jetpack Compose UI**: A fully native, high-performance, and dark-themed UI that is responsive and pleasing to use.
- **Offline Capabilities**: Caches problems and data locally (using Room database) for quick access.

## Tech Stack

- **Kotlin**: 100% Kotlin codebase.
- **Jetpack Compose**: For the entire UI layer.
- **Coroutines & Flow**: For asynchronous programming and reactive data streams.
- **Retrofit & OkHttp**: For networking and API requests.
- **Room**: For local database persistence.
- **Jsoup**: For robust HTML parsing and scraping of Codeforces problem statements.
- **Hilt / Dagger**: Dependency injection (if applicable based on architecture).

## Architecture

The application follows the **MVVM (Model-View-ViewModel)** architectural pattern and adheres to clean architecture principles:
- **UI Layer**: Jetpack Compose screens and ViewModels.
- **Domain/Data Layer**: Repositories abstracting the data sources.
- **Network Layer**: Codeforces API integration and HTML scraping via Jsoup.
- **Local Storage**: Room database for offline support.

## Getting Started

### Prerequisites
- Android Studio (Latest stable version recommended)
- JDK 17 or higher
- Android SDK API 34+

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/scodez3-14/CF-Android.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the app on an emulator or physical device:
   ```bash
   ./gradlew installDebug
   ```

## Rendering Details

The application takes a unique approach to rendering Codeforces problems:
1. **Scraping**: `Jsoup` is used to extract the abstract content of problem statements from the Codeforces website.
2. **Parsing**: HTML nodes are traversed to map paragraphs, lists, and formatting tags to Compose `AnnotatedString` objects.
3. **Math**: MathJax equations (`$$...$$` and `\(...\)`) are handled carefully to ensure they integrate cleanly into the paragraph flow rather than breaking the layout.

## License

This project is intended for educational purposes and personal use. All competitive programming content and problems belong to [Codeforces](https://codeforces.com).
