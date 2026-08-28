# HereToOsmAnd 🗺️ ➔ 🧭

**HereToOsmAnd** is a clean, lightweight Android helper application built with Kotlin and Material 3 (Material You). It seamlessly intercepts direction and map intents intended for HERE WeGo (`https://wego.here.com`, `here-location://`, `here-route://`, `geo:`) and automatically redirects them into **OsmAnd** for offline turn-by-turn navigation.

---

## 🌟 Key Features

- **Seamless Intent Redirection**: Intercepts HERE WeGo web links (`wego.here.com`), custom URIs (`here-route://`, `here-location://`), and generic `geo:` URIs.
- **OsmAnd Variant Auto-Detection**: Automatically detects installed OsmAnd editions (`net.osmand`, `net.osmand.plus`, `net.osmand.dev`) or allows forcing a preferred target app.
- **Smart URI Parser**: Extracts origin coordinates, destination coordinates, location names, search queries, and travel modes (`drive`, `walk`, `bicycle`, `public_transport`).
- **Turn-by-Turn Direct Launcher**: Constructs native `google.navigation:q=lat,lon&mode=d/w/b` intents to start navigation immediately in OsmAnd.
- **Interactive Sandbox & Test Suite**: Test HERE WeGo link parsing live inside the app before redirecting.
- **Activity History Log**: Logs recent redirected URIs with status, timestamp, and error diagnostics.
- **Modern Material 3 UI**: Clean, responsive layout adhering to high-contrast Material You design standards.

---

## 🚀 Supported Intent Formats

| Source Format | Example Incoming Link | Parsed Destination |
|---|---|---|
| **HERE WeGo Directions** | `https://wego.here.com/directions/drive/Berlin:52.5200,13.4050/Brandenburg:52.5163,13.3777` | `52.5163, 13.3777` |
| **HERE WeGo Location** | `https://wego.here.com/location/Brandenburg-Gate:52.5163,13.3777` | `52.5163, 13.3777` |
| **HERE Custom Route** | `here-route://52.5200,13.4050/52.5163,13.3777` | `52.5163, 13.3777` |
| **HERE Custom Location** | `here-location://52.5163,13.3777` | `52.5163, 13.3777` |
| **Standard Geo Intent** | `geo:52.5163,13.3777?q=52.5163,13.3777(Brandenburg+Gate)` | `52.5163, 13.3777` |

---

## 🛠️ How to Enable in Android Settings

1. Install **HereToOsmAnd**.
2. Open **Android Settings** ➔ **Apps** ➔ **HereToOsmAnd** ➔ **Open by default**.
3. Enable **Open supported links** and check `wego.here.com` and `*.here.com`.
4. When any third-party app opens HERE WeGo for directions, select **HereToOsmAnd** (or set to *Always*).
5. OsmAnd will launch automatically with turn-by-turn navigation ready!

---

## 📦 Building via GitHub Actions

This project uses cloud CI/CD pipeline offloading:

```bash
# Trigger build workflow on GitHub
gh workflow run build.yml

# Check workflow status
gh run list --workflow=build.yml

# Download compiled APK artifact to device
gh run download --name HereToOsmAnd-debug-apk

# Install on Android (Termux)
termux-open app-debug.apk
```

---

## 📄 License
MIT License.
