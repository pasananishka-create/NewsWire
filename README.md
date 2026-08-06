# NewsWire

A premium, high-end Android news reader. Trusted headlines from 150,000+ sources (BBC, Reuters, AP, The Guardian and more) via the free [NewsAPI.org](https://newsapi.org) service — delivered with a polished Material 3 experience.

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Build](https://github.com/pasananishka-create/NewsWire/actions/workflows/build.yml/badge.svg)

## Features

- **6 curated categories** — Top, Tech, Business, Entertainment, Health, Science
- **Premium design** — Midnight dark theme (plus crisp light), custom typography, gradient surfaces
- **Smooth animations** — animated category pills, staggered card entrances, pulsing LIVE indicator, press-scale feedback, pull-to-refresh
- **Shimmer loading** skeletons instead of blank screens
- **In-app article reader** — WebView with live progress bar, fade-in, share and open-in-browser
- **Source identity** — each outlet gets its own brand color + badge
- **Smart caching** — each category fetched once per session; pull-to-refresh re-fetches
- **API-key safe** — key is injected at build time, never hardcoded in source

## Install (APK)

Download the latest signed APK from the **Releases** page on the right, open it on your phone, and allow "Install unknown apps".

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Networking | Retrofit + OkHttp + kotlinx.serialization |
| Images | Coil |
| DI | Hilt |
| Navigation | Navigation Compose |
| Min / Target SDK | 26 / 36 |

## Getting Started

### 1. Get a free News API key

Sign up at [newsapi.org](https://newsapi.org) → Dashboard → generate an API key.
The free plan allows 100 requests/day — more than enough for personal use.

### 2. Configure `local.properties`

Create `local.properties` (already git-ignored) in the project root:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

NEWSAPI_KEY=your-key-here

# For signing a release APK locally (optional):
KEYSTORE_PASSWORD=your-keystore-password
KEY_ALIAS=newswire
KEY_PASSWORD=your-key-password
```

### 3. Build

Requires a JDK 17 (with `jlink`, e.g. Temurin) and the Android SDK.

```bash
./gradlew :app:assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

## GitHub Actions — automatic signed APK on every push

This repo ships a workflow (`.github/workflows/build.yml`) that builds a **signed** APK and attaches it to a GitHub Release automatically. Set these **repository secrets** (Settings → Secrets and variables → Actions):

| Secret | Value |
|--------|-------|
| `NEWSAPI_KEY` | your free NewsAPI.org key |
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
│       ├── data/                 models, Retrofit service, repository
│       ├── di/                   Hilt module
│       └── ui/
│           ├── theme/            premium Material 3 themes
│           ├── components/       article cards, shimmer, badges
│           ├── home/             category tabs + headline feed
│           └── reader/           in-app WebView reader
├── gradle/libs.versions.toml
└── release-key.jks               (git-ignored)
```

## License

MIT — see [LICENSE](LICENSE).
