# rustlin

A template for exposing a Rust crate to Kotlin Multiplatform (JVM, Android, iOS) via [UniFFI](https://mozilla.github.io/uniffi-rs/) and [gobley](https://gobley.dev/), driven by a single `justfile`.

## Prerequisites

| Tool | Used for | Verified version |
|---|---|---|
| [Rust](https://rustup.rs/) (via `rustup`) | building the crate; pinned by `rust-toolchain.toml` | 1.89.0 |
| [`just`](https://github.com/casey/just) | running the build recipes in `justfile` | 1.55.1 |
| JDK | running Gradle / compiling Kotlin | 21 |
| Android SDK + NDK | building the Android `.so`s (`cargo ndk`) | NDK 30.0.14904198 |
| [`cargo-ndk`](https://github.com/bbqsrc/cargo-ndk) | cross-compiling for Android | 4.1.2 |
| [Zig](https://ziglang.org/) + [`cargo-zigbuild`](https://github.com/rust-cross/cargo-zigbuild) | cross-compiling for Linux (glibc/musl), FreeBSD, macOS | zig 0.16.0, cargo-zigbuild 0.23.0 |
| `clang` + `lld` | providing `clang-cl`/`lld-link`, used by `cargo-xwin` | clang 21.1.8 |
| [`cargo-xwin`](https://github.com/rust-cross/cargo-xwin) | cross-compiling for Windows (MSVC) | 0.23.0 |
| [`gobley-uniffi-bindgen`](https://gobley.dev/) | generating the Kotlin Multiplatform bindings from the built library | 0.3.4 |
| Xcode (macOS only) | building/testing the iOS and macOS targets | — |

You do **not** need to install every tool above unless you intend to build every target. At minimum you need Rust, `just`, a JDK, and `gobley-uniffi-bindgen` to build for the host + run the KMP/JVM path.

## Install

Commands below assume Fedora (`dnf`); substitute your distro's package manager where noted. All commands are safe to re-run.

### 1. Rust

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

Once installed, just run any `cargo`/`rustup` command inside this repo — `rust-toolchain.toml` pins the exact channel (`1.89.0`) and declares every cross-compilation target rustup needs to fetch (Android, Linux gnu/musl, FreeBSD, Apple, Windows, wasm32). `just setup` (or any `build-*` recipe, which depends on it) triggers this automatically:

```bash
just setup   # runs `rustup show`, which installs anything missing
```

### 2. `just`

```bash
sudo dnf install just
# Debian/Ubuntu: sudo apt install just
# macOS:         brew install just
# or, any platform:
cargo install just
```

### 3. JDK

Any JDK 17+ works (CI uses 17 for tests, 21 for publishing). This machine uses a JetBrains Runtime 21, pinned explicitly in `libkmp/gradle.properties` via `org.gradle.java.home`:

```bash
sudo dnf install java-21-openjdk-devel
# Debian/Ubuntu: sudo apt install openjdk-21-jdk
# macOS:         brew install openjdk@21
```

If `libkmp/gradle.properties`' `org.gradle.java.home` points at a JDK that doesn't exist on your machine, either remove that line or point it at your own JDK install.

### 4. Android SDK + NDK (for `build-android` / `copy-android`)

Install [Android Studio](https://developer.android.com/studio) or just the command-line tools, then:

```bash
sdkmanager "platform-tools" "platforms;android-36" "ndk;30.0.14904198"
```

`cargo-ndk` picks up the NDK from `ANDROID_NDK_HOME` (or `ANDROID_NDK_ROOT` / `ANDROID_HOME/ndk/<version>`). Export it in your shell profile:

```bash
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export ANDROID_NDK_HOME="$ANDROID_SDK_ROOT/ndk/30.0.14904198"
```

`libkmp/gradle/libs.versions.toml` pins `android-compileSdk = 36` / `android-minSdk = 24` — match the platform you install to that.

### 5. `cargo-ndk`

```bash
cargo install cargo-ndk
```

### 6. Zig + `cargo-zigbuild` (for `build-linux` / `build-macos`)

```bash
sudo dnf install zig
# Debian/Ubuntu: snap install zig --classic  (or download from ziglang.org)
# macOS:         brew install zig
cargo install cargo-zigbuild
```

### 7. `clang`/`lld` + `cargo-xwin` (for `build-windows`)

```bash
sudo dnf install clang lld cmake
# Debian/Ubuntu: sudo apt install clang lld cmake
cargo install cargo-xwin
```

`cargo-xwin` downloads the MSVC CRT and Windows SDK headers itself on first use (needs network access and acceptance of Microsoft's license). On Fedora, the system `clang` package already provides a working `clang-cl`/`lld-link`, so no extra shim should be necessary — the `justfile`'s `build-windows` recipe currently also sets `LUNA_REAL_CLANG`/`CLANG_CL` and prepends a local `scripts/` directory to `PATH`; that directory doesn't exist yet in this repo. If `build-windows` fails looking for a `clang-cl` wrapper script, either remove those three lines from the recipe or add your own wrapper under `scripts/`.

### 8. `gobley-uniffi-bindgen`

```bash
cargo install gobley-uniffi-bindgen
```

If a released version isn't compatible with your `uniffi` version pin (see `Cargo.toml`), install a specific tag from source instead:

```bash
cargo install --git https://github.com/gobley/gobley --tag v0.3.4 gobley-uniffi-bindgen
```

### 9. Xcode (macOS only)

Required for `build-ios`, `build-macos`, `copy-ios`, `copy-macos`, and `test-kmp-ios`. Install from the App Store, then:

```bash
xcode-select --install
```

## Verify your setup

```bash
rustc --version && cargo --version
just --version
java -version
cargo ndk --version
cargo-zigbuild --version   # `cargo zigbuild --version` does not work; call the binary directly
cargo xwin --version
gobley-uniffi-bindgen --version
zig version
```

## Usage

```bash
just                 # full pipeline: build every platform, generate bindings, assemble the KMP library
just build-host      # just the host Linux x86_64 build
just generate-bindings  # (re)generate the Kotlin bindings from the current Rust API
just build-kmp        # ./gradlew :library:assemble
just test-kmp          # run JVM + Android host tests
just test-kmp-jvm       # just the JVM test
just publish-local      # publish the KMP library to ~/.m2 (local Maven)
just clean               # remove build output
```

Run `just --list` for the full recipe list. macOS-only recipes (`build-ios`, `build-macos`, `copy-ios`, `copy-macos`, `test-kmp-ios`) are opt-in and not part of the default pipeline — add them yourself when building on macOS (see the comment on the `default` recipe in `justfile`).