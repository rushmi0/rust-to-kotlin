# rustlin

A template for integrating Rust with Kotlin Multiplatform (KMP) using [UniFFI](https://mozilla.github.io/uniffi-rs/) and [gobley](https://gobley.dev/).

The template provides a complete build pipeline for exposing a Rust crate to Kotlin Multiplatform targets, including JVM, Android, and iOS. The entire workflow is managed through a single `justfile`.

## Supported Platforms

* JVM
* Android
* iOS
* Linux
* macOS
* Windows

## Prerequisites

Install the following tools before getting started.

### Rust

Rust is pinned to version `1.89.0` using `rust-toolchain.toml`.

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

Then install the required cross-compilation targets:

```bash
just setup
```

### just

```bash
# Fedora / RHEL
sudo dnf install just

# Debian / Ubuntu
sudo apt install just

# macOS
brew install just
```

### JDK 21

```bash
# Fedora / RHEL
sudo dnf install java-21-openjdk-devel

# Debian / Ubuntu
sudo apt install openjdk-21-jdk

# macOS
brew install openjdk@21
```

### Android SDK and NDK

Install the required Android SDK platform and NDK:

```bash
sdkmanager "platform-tools" \
           "platforms;android-36" \
           "ndk;30.0.14904198"
```

Set the Android SDK and NDK environment variables:

```bash
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export ANDROID_NDK_HOME="$ANDROID_SDK_ROOT/ndk/30.0.14904198"
```

Install `cargo-ndk`:

```bash
cargo install cargo-ndk
```

### Zig and cargo-zigbuild

These tools are required for Linux and macOS builds.

```bash
# Fedora / RHEL
sudo dnf install zig

# Debian / Ubuntu
snap install zig --classic

# macOS
brew install zig
```

Then install `cargo-zigbuild`:

```bash
cargo install cargo-zigbuild
```

### clang, lld, and cargo-xwin

These tools are required for Windows builds.

```bash
# Fedora / RHEL
sudo dnf install clang lld cmake
```

Install `cargo-xwin`:

```bash
cargo install cargo-xwin
```

### gobley-uniffi-bindgen

Install the Gobley UniFFI binding generator:

```bash
cargo install gobley-uniffi-bindgen
```

### Xcode

Xcode is required for iOS and macOS builds.

This step is only required on macOS:

```bash
xcode-select --install
```

## Getting Started

At minimum, you need:

* Rust
* `just`
* JDK 21
* `gobley-uniffi-bindgen`

Once the required tools are installed, run:

```bash
just setup
```

Then build the complete project:

```bash
just
```

## Common Commands

### Build Everything

Build all supported targets, generate Kotlin bindings, and assemble the KMP library.

```bash
just
```

### Build for the Host

Build the Rust library for the host Linux x86_64 target.

```bash
just build-host
```

### Generate Kotlin Bindings

Regenerate the Kotlin bindings based on the current Rust API.

```bash
just generate-bindings
```

### Build the KMP Library

Assemble the Kotlin Multiplatform library using Gradle.

```bash
just build-kmp
```

### Run KMP Tests

Run JVM and Android host tests.

```bash
just test-kmp
```

Run JVM tests only:

```bash
just test-kmp-jvm
```

### Publish to Local Maven Repository

Publish the KMP library to the local Maven repository.

```bash
just publish-local
```

The library will be available under:

```text
~/.m2
```

### Clean Build Outputs

Remove generated build artifacts and outputs.

```bash
just clean
```

## Available Recipes

To view all available `just` recipes:

```bash
just --list
```

The `justfile` is the central entry point for the build, binding generation, testing, packaging, and publishing workflow.
