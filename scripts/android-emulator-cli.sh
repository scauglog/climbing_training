#!/usr/bin/env bash
set -euo pipefail

# CLI bootstrap for Android emulator usage without Android Studio.
# Supports Apple Silicon and Intel Macs.

ACTION="${1:-help}"

ANDROID_API_LEVEL="${ANDROID_API_LEVEL:-35}"
BUILD_TOOLS_VERSION="${BUILD_TOOLS_VERSION:-34.0.0}"
DEVICE_NAME="${DEVICE_NAME:-pixel_7}"
AVD_NAME="${AVD_NAME:-Pixel_Android15}"
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"

if [[ "$(uname -m)" == "arm64" ]]; then
  SYSTEM_IMAGE_ABI="${SYSTEM_IMAGE_ABI:-arm64-v8a}"
else
  SYSTEM_IMAGE_ABI="${SYSTEM_IMAGE_ABI:-x86_64}"
fi

SYSTEM_IMAGE="system-images;android-${ANDROID_API_LEVEL};google_apis;${SYSTEM_IMAGE_ABI}"
PLATFORM="platforms;android-34"
CMDLINE_TOOLS_URL="${CMDLINE_TOOLS_URL:-https://dl.google.com/android/repository/commandlinetools-mac-13114758_latest.zip}"

CMDLINE_TOOLS_BIN="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin"
SDKMANAGER="$CMDLINE_TOOLS_BIN/sdkmanager"
AVDMANAGER="$CMDLINE_TOOLS_BIN/avdmanager"
EMULATOR_BIN="$ANDROID_SDK_ROOT/emulator/emulator"
ADB_BIN="$ANDROID_SDK_ROOT/platform-tools/adb"

ensure_cmdline_tools() {
  mkdir -p "$ANDROID_SDK_ROOT"

  if [[ -x "$SDKMANAGER" ]]; then
    return
  fi

  echo "Android command-line tools not found. Downloading into $ANDROID_SDK_ROOT ..."
  local tmp_dir
  tmp_dir="$(mktemp -d)"
  local zip_path="$tmp_dir/cmdline-tools.zip"

  curl -fL "$CMDLINE_TOOLS_URL" -o "$zip_path"
  mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
  unzip -q "$zip_path" -d "$ANDROID_SDK_ROOT/cmdline-tools"

  # Normalize to expected path: cmdline-tools/latest
  if [[ -d "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" ]]; then
    rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
  fi

  rm -rf "$tmp_dir"

  if [[ ! -x "$SDKMANAGER" ]]; then
    echo "Failed to install Android command-line tools."
    exit 1
  fi
}

ensure_sdk_packages() {
  ensure_cmdline_tools

  export ANDROID_SDK_ROOT
  export PATH="$CMDLINE_TOOLS_BIN:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"

  # Under pipefail, `yes` can exit with SIGPIPE after sdkmanager finishes reading.
  # Ignore that specific pipeline exit behavior so setup can continue.
  yes | "$SDKMANAGER" --licenses >/dev/null || true

  "$SDKMANAGER" \
    "platform-tools" \
    "emulator" \
    "$PLATFORM" \
    "build-tools;${BUILD_TOOLS_VERSION}" \
    "$SYSTEM_IMAGE"
}

ensure_avd() {
  ensure_sdk_packages

  if "$EMULATOR_BIN" -list-avds | grep -qx "$AVD_NAME"; then
    echo "AVD '$AVD_NAME' already exists."
    return
  fi

  echo "Creating AVD '$AVD_NAME' ..."
  echo "no" | "$AVDMANAGER" create avd -n "$AVD_NAME" -k "$SYSTEM_IMAGE" -d "$DEVICE_NAME"
}

start_emulator() {
  ensure_avd

  if pgrep -f "emulator.*-avd[[:space:]]+$AVD_NAME" >/dev/null 2>&1; then
    echo "Emulator '$AVD_NAME' is already running."
    return
  fi

  echo "Starting emulator '$AVD_NAME' ..."
  nohup "$EMULATOR_BIN" -avd "$AVD_NAME" -no-snapshot-load -no-boot-anim >/tmp/${AVD_NAME}.log 2>&1 &
  echo "Waiting for device boot ..."
  "$ADB_BIN" wait-for-device

  until [[ "$("$ADB_BIN" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
    sleep 2
  done

  echo "Emulator is ready."
}

install_debug_apk() {
  if [[ ! -x "./gradlew" ]]; then
    echo "Run this command from the repository root (where ./gradlew exists)."
    exit 1
  fi

  ./gradlew installDebug
}

run_connected_tests() {
  if [[ ! -x "./gradlew" ]]; then
    echo "Run this command from the repository root (where ./gradlew exists)."
    exit 1
  fi

  ./gradlew connectedDebugAndroidTest
}

print_env_hint() {
  cat <<EOF
Use these environment variables in your shell profile (e.g. ~/.zshrc):

export ANDROID_SDK_ROOT="$ANDROID_SDK_ROOT"
export PATH="\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools:\$ANDROID_SDK_ROOT/emulator:\$PATH"
EOF
}

usage() {
  cat <<EOF
Usage: scripts/android-emulator-cli.sh <action>

Actions:
  setup              Install command-line tools, SDK packages, and create the AVD
  start              Start the emulator and wait until boot completes
  install-app        Build and install debug APK on connected device/emulator
  connected-tests    Run connected Android tests on emulator/device
  env                Print shell environment exports
  help               Show this help

Configurable env vars:
  ANDROID_SDK_ROOT      (default: $ANDROID_SDK_ROOT)
  ANDROID_API_LEVEL     (default: $ANDROID_API_LEVEL)
  BUILD_TOOLS_VERSION   (default: $BUILD_TOOLS_VERSION)
  SYSTEM_IMAGE_ABI      (default: auto-detected: $SYSTEM_IMAGE_ABI)
  DEVICE_NAME           (default: $DEVICE_NAME)
  AVD_NAME              (default: $AVD_NAME)
  CMDLINE_TOOLS_URL     (default: Google mac command-line tools URL)
EOF
}

case "$ACTION" in
  setup)
    ensure_avd
    ;;
  start)
    start_emulator
    ;;
  install-app)
    install_debug_apk
    ;;
  connected-tests)
    run_connected_tests
    ;;
  env)
    print_env_hint
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    echo "Unknown action: $ACTION"
    usage
    exit 1
    ;;
esac
