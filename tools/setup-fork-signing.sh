#!/usr/bin/env bash
# One-time setup: creates the fork's release keystore in ~/.private_keys and uploads it
# to the GitHub repo secrets, so CI can publish signed APKs that the in-app updater accepts.
# Keep the .jks: losing it means every TV must uninstall the app to take the next update.
set -euo pipefail
D="$HOME/.private_keys"
JKS="$D/streamvault-release.jks"
PROPS="$D/streamvault-keystore.properties"
mkdir -p "$D"

if [ ! -f "$JKS" ]; then
  PW=$(openssl rand -hex 20)
  keytool -genkeypair -keystore "$JKS" -storetype JKS -alias streamvault -keyalg RSA \
    -keysize 4096 -validity 36500 -storepass "$PW" -keypass "$PW" \
    -dname "CN=Francesco Vagliante, O=Tild"
  printf 'storeFile=keystore/release.jks\nstorePassword=%s\nkeyAlias=streamvault\nkeyPassword=%s\n' \
    "$PW" "$PW" > "$PROPS"
  chmod 600 "$JKS" "$PROPS"
fi

PW=$(sed -n 's/^storePassword=//p' "$PROPS")
REPO=francescovaglia/StreamVault-IPTV
base64 -i "$JKS" | gh secret set -R "$REPO" RELEASE_KEYSTORE_BASE64
printf '%s' "$PW" | gh secret set -R "$REPO" RELEASE_STORE_PASSWORD
printf '%s' "$PW" | gh secret set -R "$REPO" RELEASE_KEY_PASSWORD
printf '%s' streamvault | gh secret set -R "$REPO" RELEASE_KEY_ALIAS
gh secret list -R "$REPO"
