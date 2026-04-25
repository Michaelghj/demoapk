#!/bin/sh

#
# Gradle wrapper script - downloads Gradle if not present and runs it
#

set -e

# Set up Android SDK if not already configured
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    for sdk_path in "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" "/usr/local/lib/android/sdk" "/opt/android-sdk" "$HOME/.android/sdk"; do
        if [ -d "$sdk_path" ]; then
            export ANDROID_HOME="$sdk_path"
            break
        fi
    done
    if [ -z "$ANDROID_HOME" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
        mkdir -p "$ANDROID_HOME"
        echo "Installing Android SDK command-line tools..."
        curl -sL "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" -o /tmp/cmdline-tools.zip
        mkdir -p "$ANDROID_HOME/cmdline-tools"
        unzip -q /tmp/cmdline-tools.zip -d "$ANDROID_HOME/cmdline-tools/tmp"
        mv "$ANDROID_HOME/cmdline-tools/tmp/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
        rm /tmp/cmdline-tools.zip
        yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1
        "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "platforms;android-34" "build-tools;34.0.0" > /dev/null 2>&1
        echo "Android SDK installed."
    fi
fi

GRADLE_VERSION="8.4"
GRADLE_HOME_DIR="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin"

# If Gradle not installed, download it
if [ ! -d "$GRADLE_HOME_DIR" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$HOME/.gradle/wrapper/dists"
    curl -sL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d "$HOME/.gradle/wrapper/dists/"
    rm /tmp/gradle.zip
fi

GRADLE_BIN="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION/gradle-$GRADLE_VERSION/bin/gradle"

exec "$GRADLE_BIN" "$@"