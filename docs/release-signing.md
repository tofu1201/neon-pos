# Release Build & Signing Deployment

## 1. Prepare signing assets

1. Generate or obtain a release keystore (`.jks`).
2. Copy [keystore.properties.example](../keystore.properties.example) to `keystore.properties`.
3. Fill in `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`.
4. Keep both the `.jks` file and `keystore.properties` outside version control.

## 2. Build a release APK

From the project root:

```bash
./gradlew clean assembleRelease
```

The signed APK will be in:

`app/build/outputs/apk/release/`

## 3. Build a release AAB

If you want Play Console or enterprise distribution:

```bash
./gradlew clean bundleRelease
```

The signed bundle will be in:

`app/build/outputs/bundle/release/`

## 4. Verify signing

Use `apksigner` or Android Studio's APK Analyzer to confirm the APK is signed with the expected certificate.

## 5. Deploy to the handheld terminal

### Option A: USB / adb sideload

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

If the app is already installed, the `-r` flag upgrades it in place.

### Option B: MDM / enterprise app store

1. Upload the signed APK or AAB to your device management platform.
2. Assign the app to the target terminal group.
3. Push the update and confirm the device reports the new version code.

### Option C: Manual file install

1. Copy the signed APK to the device.
2. Open it with the system installer.
3. Allow unknown sources if required by the device policy.

## 6. Recommended release checklist

- Confirm the printer service package `com.incar.printerservice` is installed on the terminal.
- Test NFC reader mode with a real member card.
- Test receipt printing, missing paper, reconnect, and offline checkout.
- Verify the release certificate fingerprint is recorded for future updates.
- Increase `versionCode` for every production release.

## 7. Operational notes

- Keep `keystore.properties` and the `.jks` file in a secure location.
- Back up the keystore in at least two safe places.
- Do not reuse the debug signing key for production.
