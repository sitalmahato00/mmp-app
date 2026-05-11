# MMP App

Android application built with Kotlin, Jetpack Compose, Hilt, Room, Retrofit, and Firebase Messaging.

## 1) Prerequisites

Install these on the new device before cloning:

- Git
- Android Studio (latest stable)
- Android SDK Platform 36
- Android SDK Build-Tools (latest available in Android Studio)
- JDK 17 (Android Studio can use its bundled JDK)

Project toolchain used in this repo:

- Android Gradle Plugin: `8.9.1`
- Kotlin: `2.1.0`
- Gradle Wrapper: `9.4.1`
- `compileSdk` / `targetSdk`: `36`
- `minSdk`: `24`

## 2) Clone the Project

Use PowerShell on Windows:

```powershell
git clone https://github.com/sitalmahato00/mmp-app.git
Set-Location mmp-app
```

## 3) Open in Android Studio

1. Open Android Studio.
2. Click **Open** and select the cloned `mmp-app` folder.
3. Let Gradle sync complete.

If prompted:

- Use the Gradle Wrapper from the project.
- Use JDK 17.

## 4) SDK and `local.properties`

`local.properties` is machine-specific and should point to your Android SDK path.

Android Studio usually creates/updates this automatically. If needed, create or edit `local.properties` in project root:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
```

## 5) Build the App

From project root in PowerShell:

```powershell
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

To run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## 6) Run on Emulator or Physical Device

### Emulator

1. Open **Device Manager** in Android Studio.
2. Create/start an emulator (API 24+).
3. Select the emulator and click **Run**.

### Physical Android Device

1. Enable **Developer options** and **USB debugging** on the phone.
2. Connect by USB.
3. Accept the RSA debug prompt on the device.
4. Select device in Android Studio and click **Run**.

## 7) Common Issues and Fixes

### Gradle sync fails with JDK error

- Set Gradle JDK to 17:
  - **File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**

### SDK not found

- Confirm `local.properties` has a valid `sdk.dir`.
- Confirm SDK Platform 36 is installed in SDK Manager.

### Device not visible

- Reconnect USB cable.
- Re-enable USB debugging.
- Install OEM USB driver (Windows) if needed.
- Verify device is detected:

```powershell
adb devices
```

### Slow/failed dependency download

- Retry sync on stable internet.
- Optionally clear Gradle caches only if needed:

```powershell
Remove-Item -Recurse -Force "$env:USERPROFILE\.gradle\caches" -ErrorAction SilentlyContinue
```

## 8) Useful Commands

```powershell
# Build debug APK
.\gradlew.bat assembleDebug

# Install debug APK to connected device
.\gradlew.bat installDebug

# Run lint
.\gradlew.bat lintDebug

# Run instrumentation tests (requires emulator/device)
.\gradlew.bat connectedDebugAndroidTest
```

## 9) Git Workflow (Quick)

```powershell
git checkout -b feature/my-change
git add .
git commit -m "Describe your change"
git push -u origin feature/my-change
```

## 10) Notes

- Build outputs are ignored by Git (`/build`, `app/build`).
- Keep `local.properties` local to each machine.
- If you add Firebase config later, include `google-services.json` setup in this README.
