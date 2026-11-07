#!/usr/bin/env bash

# Resolve JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/opt/homebrew/Cellar/openjdk@17" ]; then
        LATEST_JDK=$(ls -d /opt/homebrew/Cellar/openjdk@17/* | tail -n 1)
        export JAVA_HOME="$LATEST_JDK/libexec/openjdk.jdk/Contents/Home"
    elif [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

JAR_PATH="target/CollegeCertGen-Pro.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "⚡ Building application JAR..."
    if [ -x "$HOME/.antigravity-tools/apache-maven-3.9.9/bin/mvn" ]; then
        "$HOME/.antigravity-tools/apache-maven-3.9.9/bin/mvn" clean package -DskipTests
    elif command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    fi
fi

echo "🚀 Starting College Certificate Generation Suite..."
java -jar "$JAR_PATH"

