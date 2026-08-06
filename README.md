# NewsWire

A premium, addictive Sri Lankan news app for Android. Fast, full-screen story cards you swipe through like a feed — no lists, no clutter, just headlines that matter, from Sri Lankan and international sources covering Sri Lanka.

![Version](https://img.shields.io/badge/version-1.1.0-blue)
![Build](https://github.com/pasananishka-create/NewsWire/actions/workflows/build.yml/badge.svg)

## Features

- **TikTok-style vertical feed** — flick up for the next story, tap to read the full article
- **100% Sri Lanka focused** — live headlines via Google News Sri Lanka (`gl=LK`), refreshed on demand
- **6 categories** — All, Cricket, Politics, Business, Weather, Entertainment
- **Signature look** — each story gets a unique animated gradient canvas, bold typography, source badge and "time ago"
- **Addictive touches** — haptic tick on every card change, LIVE pulse, progress counter (`4 / 87`), swipe-up hint
- **Shimmer loading** deck instead of blank screens
- **In-app article reader** — WebView with live progress bar, share and open-in-browser
- **Smart caching** — each category fetched once per session; tap the refresh button for fresh stories

## Install (APK)

Download the latest signed APK from the **Releases** page on the right, open it on your phone, and allow "Install unknown apps".

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 (VerticalPager) |
| Networking | OkHttp + XmlPullParser (Google News RSS) |
| DI | Hilt |
| Navigation | Navigation Compose |
| Min / Target SDK | 26 / 36 |

## Getting Started

### 1. Configure `local.properties`

Create `local.properties` (already git-ignored) in the project root:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# For signing a release APK locally (optional):
KEYSTORE_PASSWORD=your-keystore-password
KEY_ALIAS=newswire
KEY_PASSWORD=your-key-password
```

No API key is needed — the feed comes from the free Google News RSS endpoints.

### 2. Build

Requires a JDK 17 (with `jlink`, e.g. Temurin) and the Android SDK.

```bash
./gradlew :app:assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions — automatic signed APK on every push

This repo ships a workflow (`.github/workflows/build.yml`) that builds a **signed** APK and attaches it to a GitHub Release automatically. Set these **repository secrets** (Settings → Secrets and variables → Actions):

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | base64 of `release-key.jks` |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | `newswire` |
| `KEY_PASSWORD` | key password |

Generate the base64 of the keystore (Windows PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\release-key.jks"))
```

Then push to `main` (or run the workflow manually via Actions → Build & Release APK → Run workflow). Each new `versionName` bump creates a `vX.Y.Z` release with the APK attached.

> Keep `release-key.jks` safe. It's git-ignored and only lives on your machine + as the `KEYSTORE_BASE64` secret. You need it to keep installing future updates over this APK.

## Project Structure

```
NewsWire/
├── .github/workflows/build.yml   CI → signed APK on Releases
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/newswire/
│       ├── data/                 models, RSS parser, repository
│       ├── di/                   Hilt module
│       └── ui/
│           ├── theme/            premium Material 3 themes
│           ├── components/       shimmer, time-ago helpers
│           ├── home/             vertical story deck + categories
│           └── reader/           in-app WebView reader
├── gradle/libs.versions.toml
└── release-key.jks               (git-ignored)
```

## License

MIT — see [LICENSE](LICENSE).
