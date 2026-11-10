#!/bin/bash
# 不依赖 Gradle 打「PXT鸿蒙信号」只读小包：aapt2 link → javac → d8 → zipalign → apksigner（debug keystore）
set -euo pipefail
cd "$(dirname "$0")"
SDK="$HOME/Library/Android/sdk"; BT="$SDK/build-tools/36.1.0-rc1"; JAR="$SDK/platforms/android-35/android.jar"
rm -rf build && mkdir -p build/classes
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o build/base.apk
javac -source 8 -target 8 -bootclasspath "$JAR" -Xlint:-options -d build/classes $(find src -name '*.java')
"$BT/d8" --min-api 24 --lib "$JAR" --output build $(find build/classes -name '*.class')
(cd build && cp base.apk unsigned.apk && zip -q -j unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
"$BT/apksigner" sign --ks "$HOME/.android/debug.keystore" --ks-pass pass:android --key-pass pass:android \
  --out build/pxt-harmonysignal.apk build/aligned.apk
"$BT/apksigner" verify build/pxt-harmonysignal.apk && ls -la build/pxt-harmonysignal.apk
