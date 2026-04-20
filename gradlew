#!/bin/sh

#
# Gradle wrapper script - downloads Gradle if not present and runs it
#

set -e

GRADLE_VERSION="8.2"
GRADLE_HOME_DIR="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION"

# If Gradle not installed, download it
if [ ! -d "$GRADLE_HOME_DIR" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    mkdir -p "$HOME/.gradle/wrapper/dists"
    curl -sL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o /tmp/gradle.zip
    unzip -q /tmp/gradle.zip -d "$HOME/.gradle/wrapper/dists/"
    rm /tmp/gradle.zip
fi

GRADLE_BIN="$HOME/.gradle/wrapper/dists/gradle-$GRADLE_VERSION/bin/gradle"

exec "$GRADLE_BIN" "$@"
