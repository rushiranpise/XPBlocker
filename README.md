<p align="center">
  <img src="images/ic_launcher.png" alt="XPBlocker Next" width="128" />
</p>

# XPBlocker Next

> **XPBlocker Next** is the successor of the classic **XPBlocker** Xposed module, modernized with
> a Material 3 UI and a working CI pipeline. It blocks ad hosts, activities, views, splash
> screens, services and receivers **without touching the system hosts file and without a VPN** —
> you can use any VPN you want while it blocks ads in the background.

- [Introduction](#introduction)
- [Features](#features)
- [Compatibility](#compatibility)
- [Installation](#installation)
- [Building](#building)
- [Distribution](#distribution)
- [Credits and License](#credits-and-license)

## Introduction

AdBlocker was a popular Xposed module created by [@aviraxp](https://github.com/aviraxp) and
abandoned since Nougat was coming. [@HardcodedCat](https://github.com/HardcodedCat) revived it as
**XPBlocker**, and [@AwaisKing](https://github.com/AwaisKing) kept it alive. **XPBlocker Next** is
an independent successor: a separate project with a new package name
(`com.aviraxp.xpblocker.next`), a modern **Material 3 UI**, updated blocklists and a working CI
pipeline — it can be installed side-by-side with the original XPBlocker.

XPBlocker Next is not VPN- or hosts-file-based: it hooks into the target apps themselves and blocks
the ad-related hosts, activities, views, splash screens, services and receivers **inside the app
process**. The blocklists ship inside the APK (including the
[badmojr/1Hosts](https://github.com/badmojr/1Hosts) list), so there is nothing to download at
runtime.

## Features

| Hook | What it blocks |
|---|---|
| **Isolated Hosts Blocking** | Ad URL addresses and domains, without touching the system hosts file |
| **WebView Block** | Ad page elements loaded inside WebViews |
| **Activity and View Blocking** | Ad activities and views according to a preloaded list |
| **Splash Screen Block** | Splash ads (optionally skipping straight to the real activity) |
| **Services Block** | Ad services started by apps |
| **Receivers Block** | Ad receivers |
| **Enforce Cancelable Dialog** | Force-cancel anti-ad dialogs |
| **Prevent Shortcut Auto Creation** | Stop apps from auto-creating shortcuts |
| **Bypass Xposed Block** | Prevent apps from disabling Xposed hooks themselves |
| **Aggressive Activity Block** | Also block activity/view names that only *contain* ad-related words (unstable) |
| **Aggressive URL Path Block** | Block URLs based on their paths |

Every hook can be toggled independently from the app's settings screen, and you can whitelist
specific activities, views and hosts. Debug mode logs every blocked item to the Xposed log.

## Compatibility

- **Android 8.0 – 15+ (API 26+)**, built against SDK 35.
- **Framework**: any Xposed-compatible runtime — **LSPosed** (recommended), legacy Xposed (API 82),
  EdXposed, etc.
- The module must be activated in your framework's manager and scoped to the apps you want to
  protect (most hooks apply to all apps once enabled).

## Installation

1. Download the latest APK from the [Releases](https://github.com/rushiranpise/XPBlocker/releases) page.
2. Install it as a normal app.
3. Enable **XPBlocker Next** in your Xposed framework's manager (LSPosed / Xposed Installer).
4. Reboot, then open the app and toggle the hooks you want.

You can also hide the launcher icon from the app's settings (access the module again through your
Xposed manager).

## Building

Requirements: JDK 17, Android SDK with platform **android-35**.

```bash
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (unsigned, sign with uber-apk-signer)
```

The debug and release APKs are built automatically by
[GitHub Actions](https://github.com/rushiranpise/XPBlocker/actions) on every push, and a signed
release is published when a `v*` tag is pushed.

## Distribution

Links to the original releases are gone, but you can always get fresh builds from:

- [GitHub Actions](https://github.com/rushiranpise/XPBlocker/actions) — every commit's debug APK
- [Releases](https://github.com/rushiranpise/XPBlocker/releases) — tagged releases with debug + release APKs

## Credits and License

- Successor of the original XPBlocker, maintained by [@rushiranpise](https://github.com/rushiranpise).
- Blocklist: [badmojr/1Hosts](https://github.com/badmojr/1Hosts).

This application is distributed under the **GPL-3.0 license** — see [LICENSE](LICENSE).
