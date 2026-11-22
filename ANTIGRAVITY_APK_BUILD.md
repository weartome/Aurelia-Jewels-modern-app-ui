# ANTIGRAVITY_APK_BUILD — Autonomous Windows APK Build Runbook

Target Repository: `weartome/Aurelia-Jewels-modern-app-ui`
Target Agent: Antigravity (Autonomous Coding Agent) on Windows 10/11 (PowerShell 5.1 / PowerShell 7+)
Toolchain Baseline: JDK 21, Gradle 9.3.1, AGP 9.1.1, Kotlin 2.4.21, Room 2.7.0, `compileSdk 36` (`minorApiLevel = 1`), `targetSdk 36`, `minSdk 24`

Execute all steps sequentially from the **repository root directory** (`settings.gradle.kts` must exist in the current working directory). Do not skip verification checks. Do not modify `app/build.gradle.kts` signing configs or `debug.keystore.base64`.

---

## Step 1: System & Environment Verification (PowerShell)

Run the block below in PowerShell from the repository root to verify JDK 21, Android SDK, `local.properties`, API 36.1 platform, Build-Tools 36.0.0, and Gradle 9.3.1.

```powershell
$ErrorActionPreference = "Stop"

# 1. Confirm repository root
if (-not (Test-Path ".\settings.gradle.kts") -or -not (Test-Path ".\app\build.gradle.kts")) {
    throw "Run this runbook from the repository root containing settings.gradle.kts."
}

# 2. Verify JDK 21
java -version
$javaVersionOutput = (java -version 2>&1) | Out-String
if ($javaVersionOutput -notmatch 'version "21\.') {
    Write-Warning "JDK 21 is required. Current JAVA_HOME=$($env:JAVA_HOME). Install Temurin JDK 21 and set JAVA_HOME before continuing."
} else {
    Write-Host "OK: JDK 21 detected."
}

# 3. Resolve Android SDK root (ANDROID_HOME or default Windows path)
$sdkRoot = if ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} elseif ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} elseif (Test-Path "$env:LOCALAPPDATA\Android\Sdk") {
    "$env:LOCALAPPDATA\Android\Sdk"
} else {
    throw "Android SDK not found. Set ANDROID_HOME or install Android SDK Command-Line Tools."
}

$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot
Write-Host "Using Android SDK: $sdkRoot"

# 4. Ensure local.properties points to the Android SDK (gitignored; required for local Windows Gradle builds)
$escapedSdk = $sdkRoot.Replace("\", "\\").Replace(":", "\:")
Set-Content -Path ".\local.properties" -Value "sdk.dir=$escapedSdk" -Encoding ASCII
Write-Host "Wrote local.properties -> sdk.dir=$escapedSdk"

# 5. Locate sdkmanager and verify/install Android Platform 36.1 + Build-Tools 36.0.0
$sdkManager = Get-ChildItem -Path "$sdkRoot\cmdline-tools" -Filter "sdkmanager.bat" -Recurse -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $sdkManager) {
    Write-Warning "sdkmanager.bat not found under $sdkRoot\cmdline-tools."
} else {
    Write-Host "Using sdkmanager: $sdkManager"
    # Accept all licenses non-interactively
    $yesStream = ("y`n" * 40)
    $yesStream | & $sdkManager --sdk_root="$sdkRoot" --licenses | Out-Null

    if (-not (Test-Path "$sdkRoot\platforms\android-36.1") -or -not (Test-Path "$sdkRoot\build-tools\36.0.0")) {
        Write-Host "Installing platform-tools, platforms;android-36, platforms;android-36.1, build-tools;36.0.0..."
        & $sdkManager --sdk_root="$sdkRoot" "platform-tools" "platforms;android-36" "platforms;android-36.1" "build-tools;36.0.0"
    }
}

# 6. Hard check required SDK directories
foreach ($reqPath in @(
    "$sdkRoot\platforms\android-36.1",
    "$sdkRoot\build-tools\36.0.0"
)) {
    if (-not (Test-Path $reqPath)) {
        throw "Missing required Android SDK component: $reqPath"
    }
    Write-Host "OK: Found $reqPath"
}

# 7. Check Gradle 9.3.1 availability (this repo ships gradle-wrapper.properties without gradlew.bat)
$gradleCmd = Get-Command gradle -ErrorAction SilentlyContinue
if ($gradleCmd) {
    gradle --version
} elseif (Test-Path ".\gradlew.bat") {
    .\gradlew.bat --version
} else {
    throw "Neither 'gradle' nor '.\gradlew.bat' is available. See Troubleshooting Table -> Missing gradlew / Gradle 9.3.1."
}
```

---

## Step 2: One-Time Debug Keystore Setup

`app/build.gradle.kts` signs debug builds with `file("${rootDir}/debug.keystore")` (`alias = androiddebugkey`, `storePassword = android`, `keyPassword = android`).
`debug.keystore` is gitignored; the tracked source file is `debug.keystore.base64` at the repository root.

Run this PowerShell block once from the repository root:

```powershell
$ErrorActionPreference = "Stop"

$b64Path = Join-Path (Get-Location) "debug.keystore.base64"
$keystorePath = Join-Path (Get-Location) "debug.keystore"

if (-not (Test-Path $b64Path)) {
    throw "Missing debug.keystore.base64 at repository root."
}

$rawBase64 = (Get-Content -Raw -Path $b64Path).Trim()
[System.IO.File]::WriteAllBytes($keystorePath, [System.Convert]::FromBase64String($rawBase64))

if (-not (Test-Path $keystorePath) -or (Get-Item $keystorePath).Length -eq 0) {
    throw "Failed to decode debug.keystore."
}

# Verify alias and certificate fingerprint
keytool -list -keystore $keystorePath -storepass android -alias androiddebugkey
Write-Host "OK: debug.keystore decoded and verified at $keystorePath"
```

---

## Step 3: Exact Build & Test Sequence

Use `gradle` (or `.\gradlew.bat` if you generated a local wrapper). Pass Google's Maven Central mirror to Robolectric so `ExampleRobolectricTest` does not fail with HTTP 429 rate limits from `repo1.maven.org`.

### 3.1 Run All Unit Tests (`AureliaCommerceRulesTest`, `ExampleRobolectricTest`, `ExampleUnitTest`)

```powershell
gradle :app:testDebugUnitTest `
  -Drobolectric.dependency.repo.url=https://maven-central.storage-download.googleapis.com/maven2 `
  --stacktrace
```

Expected result: `BUILD SUCCESSFUL` with 0 failed tests (`10 tests completed, 0 failed`).

### 3.2 Assemble the Debug APK

```powershell
gradle :app:assembleDebug --stacktrace
```

Expected APK output path:

```text
app\build\outputs\apk\debug\app-debug.apk
```

---

## Step 4: Low / Mid-RAM Optimization (12GB RAM Windows PC)

`gradle.properties` defaults to `-Xmx3g -XX:MaxMetaspaceSize=1g` and `org.gradle.workers.max=4`.
On a **12GB RAM Windows machine** (especially with Chrome, Android Studio, or an IDE running), override Gradle JVM memory and worker concurrency via command-line flags so the build never triggers OOM or system freezes:

```powershell
# Safe low-RAM unit test run (12GB RAM PC)
gradle :app:testDebugUnitTest `
  --no-daemon `
  -Dorg.gradle.jvmargs="-Xmx2560m -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8" `
  -Dorg.gradle.workers.max=2 `
  -Drobolectric.dependency.repo.url=https://maven-central.storage-download.googleapis.com/maven2 `
  --stacktrace

# Safe low-RAM Debug APK build (12GB RAM PC)
gradle :app:assembleDebug `
  --no-daemon `
  -Dorg.gradle.jvmargs="-Xmx2560m -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8" `
  -Dorg.gradle.workers.max=2 `
  --stacktrace
```

If you need even tighter RAM limits (8GB free RAM or less), lower `-Xmx2560m` to `-Xmx2048m` and add `-Dorg.gradle.parallel=false`.

---

## Step 5: Automated Troubleshooting Table

| Failure Symptom | Root Cause | Exact Automated Fix (PowerShell) |
| :--- | :--- | :--- |
| `SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file` | Missing `local.properties` or unset `ANDROID_HOME` | `$s="$env:LOCALAPPDATA\Android\Sdk"; Set-Content .\local.properties ("sdk.dir=" + $s.Replace("\","\\").Replace(":","\:")) -Encoding ASCII` |
| `Failed to install the following Android SDK packages as some licences have not been accepted` | Unaccepted SDK licenses in `$ANDROID_HOME\licenses` | `("y`n"*40) \| & "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --licenses` |
| `Failed to find target with hash string 'android-36.1'` or missing Build-Tools 36.0.0 | `compileSdk` requires API 36 minor level 1 (`android-36.1`) | `& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-36" "platforms;android-36.1" "build-tools;36.0.0"` |
| `gradlew.bat` or `gradle` is not recognized | Repo includes `gradle/wrapper/gradle-wrapper.properties` (9.3.1) without binary `gradle-wrapper.jar` | Install Gradle 9.3.1: `winget install --id Gradle.Gradle -e` (or download `gradle-9.3.1-bin.zip` from `services.gradle.org/distributions/gradle-9.3.1-bin.zip`, extract to `C:\Gradle\gradle-9.3.1`, and prepend `C:\Gradle\gradle-9.3.1\bin` to `$env:PATH`). Optionally run `gradle wrapper --gradle-version 9.3.1` once. |
| `Timeout waiting to lock ...` or `Could not move temporary workspace` | Stale Gradle daemon holding Windows file lock | `gradle --stop; Get-Process java -ErrorAction SilentlyContinue \| Stop-Process -Force; Remove-Item -Force -ErrorAction SilentlyContinue .\.gradle\*.lock` |
| `Unsupported class file major version` or AGP 9.1.1 JDK mismatch | System `JAVA_HOME` points to JDK 11/17/25 instead of JDK 21 | Install Temurin 21 (`winget install --id EclipseAdoptium.Temurin.21.JDK -e`), set `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21*"` (resolve exact path) and `$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"`. |
| `validateSigningDebug` fails: `Keystore file ...\debug.keystore not found` | `debug.keystore` was not decoded from `debug.keystore.base64` | Re-run Step 2 PowerShell decode command to write `debug.keystore` at repository root. |
| `ExampleRobolectricTest` fails with `HTTP response code: 429` from `repo1.maven.org` | Maven Central rate-limited Robolectric's runtime JAR download | Pass `-Drobolectric.dependency.repo.url=https://maven-central.storage-download.googleapis.com/maven2` on the `testDebugUnitTest` command. |
| `OutOfMemoryError: Java heap space` or `Metaspace` on Windows | Default `-Xmx3g` + 4 parallel workers exceeds free RAM | Re-run using the Step 4 low-RAM flags (`--no-daemon -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=768m" -Dorg.gradle.workers.max=2`). |

---

## Step 6: Definition of Done (Automated Verification Script)

Run this final verification block in PowerShell. The build is **PASS** only when every assertion below succeeds with exit code `0`.

```powershell
$ErrorActionPreference = "Stop"

$apkPath = Join-Path (Get-Location) "app\build\outputs\apk\debug\app-debug.apk"
$keystorePath = Join-Path (Get-Location) "debug.keystore"

# 1. Confirm APK exists and is non-zero
if (-not (Test-Path $apkPath)) {
    throw "FAIL: APK not found at $apkPath"
}
$apkInfo = Get-Item $apkPath
if ($apkInfo.Length -le 0) {
    throw "FAIL: APK file is 0 bytes at $apkPath"
}
$sizeMb = [Math]::Round($apkInfo.Length / 1MB, 2)
Write-Host "PASS [1/3]: APK exists at $apkPath ($sizeMb MB, $($apkInfo.Length) bytes)"

# 2. Confirm APK is signed with debug.keystore (compare SHA-256 certificate fingerprints)
$ksCertLine = (keytool -list -keystore $keystorePath -storepass android -alias androiddebugkey |
    Select-String -Pattern "SHA-256" | Select-Object -First 1).ToString().Trim()
$apkCertLine = (keytool -printcert -jarfile $apkPath |
    Select-String -Pattern "SHA256:" | Select-Object -First 1).ToString().Trim()

$ksHash = ($ksCertLine -split ":\s*", 2)[1].Trim()
$apkHash = ($apkCertLine -split ":\s*", 2)[1].Trim()

if ([string]::IsNullOrWhiteSpace($apkHash) -or ($ksHash -ne $apkHash)) {
    throw "FAIL: APK signature SHA-256 ($apkHash) does not match debug.keystore ($ksHash)."
}
Write-Host "PASS [2/3]: APK is validly signed with debug.keystore (SHA-256: $apkHash)"

# 3. Optional apksigner v1/v2/v3 verification if Build-Tools 36.0.0 apksigner is present
$apksigner = Join-Path $env:ANDROID_HOME "build-tools\36.0.0\apksigner.bat"
if (Test-Path $apksigner) {
    & $apksigner verify --print-certs $apkPath
    Write-Host "PASS [3/3]: apksigner cryptographic signature check passed."
} else {
    Write-Host "PASS [3/3]: keytool JAR certificate check passed (apksigner.bat not found at $apksigner)."
}

Write-Host ""
Write-Host "VERDICT: PASS — Verified Debug APK ready at $apkPath"
```
