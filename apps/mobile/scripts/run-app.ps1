<#
.SYNOPSIS
    Build the Enso Legacy debug APK, boot the emulator if needed, then install + launch.

.DESCRIPTION
    A convenience wrapper around gradlew + adb + the emulator so you can test the app
    with a single command. Derives the Android SDK path from local.properties and uses
    JAVA_HOME (falling back to the Microsoft OpenJDK 17 install).

.PARAMETER Avd
    Name of the AVD to boot. Default: enso_api35.

.PARAMETER SkipBuild
    Skip the Gradle build and just (re)install the existing APK.

.PARAMETER NoEmulator
    Don't start an emulator — assume a device/emulator is already connected.

.EXAMPLE
    .\scripts\run-app.ps1
    .\scripts\run-app.ps1 -SkipBuild
    powershell -ExecutionPolicy Bypass -File .\scripts\run-app.ps1
#>
param(
    [string]$Avd = 'enso_api35',
    [switch]$SkipBuild,
    [switch]$NoEmulator
)

$ErrorActionPreference = 'Stop'

# --- Paths -------------------------------------------------------------------
# Project root = parent of this script's folder (apps/mobile).
$ProjRoot = Split-Path -Parent $PSScriptRoot
$AppId = 'com.ensolegacy.mobile'
$Apk = Join-Path $ProjRoot 'app\build\outputs\apk\debug\app-debug.apk'

# --- JDK 17 ------------------------------------------------------------------
$Jdk = $env:JAVA_HOME
if (-not ($Jdk -and (Test-Path (Join-Path $Jdk 'bin\java.exe')))) {
    $Jdk = 'C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot'
}
if (-not (Test-Path (Join-Path $Jdk 'bin\java.exe'))) {
    throw "JDK 17 not found. Set JAVA_HOME or install Microsoft.OpenJDK.17."
}
$env:JAVA_HOME = $Jdk
$env:PATH = "$Jdk\bin;$env:PATH"

# --- Android SDK (read sdk.dir from local.properties) ------------------------
$LocalProps = Join-Path $ProjRoot 'local.properties'
if (-not (Test-Path $LocalProps)) { throw "Missing $LocalProps (sdk.dir=...)" }
$sdkLine = Select-String -Path $LocalProps -Pattern '^\s*sdk\.dir\s*=\s*(.+)$' | Select-Object -First 1
if (-not $sdkLine) { throw "sdk.dir not found in local.properties" }
# Properties files escape backslashes and often use forward slashes - normalize.
$Sdk = $sdkLine.Matches[0].Groups[1].Value.Trim() -replace '\\\\', '\' -replace '/', '\'
if (-not (Test-Path $Sdk)) { throw "SDK path from local.properties does not exist: $Sdk" }
$env:ANDROID_SDK_ROOT = $Sdk
$env:ANDROID_HOME = $Sdk

$Adb = Join-Path $Sdk 'platform-tools\adb.exe'
$Emulator = Join-Path $Sdk 'emulator\emulator.exe'

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }

# --- Emulator ----------------------------------------------------------------
& $Adb start-server | Out-Null
$connected = (& $Adb devices) -match '\bdevice$'

if (-not $connected -and -not $NoEmulator) {
    Write-Step "Booting emulator '$Avd' (a window will open)..."
    # Detached so the emulator outlives this script.
    Start-Process -FilePath $Emulator -ArgumentList @('-avd', $Avd, '-no-snapshot', '-no-boot-anim')
    & $Adb wait-for-device
    Write-Host "Waiting for Android to finish booting..." -NoNewline
    for ($i = 0; $i -lt 90; $i++) {
        $b = (& $Adb shell getprop sys.boot_completed 2>$null | Out-String).Trim()
        if ($b -eq '1') { break }
        Start-Sleep -Seconds 3
        Write-Host '.' -NoNewline
    }
    Write-Host " ready."
} elseif ($connected) {
    Write-Step "Using already-connected device/emulator."
} else {
    throw "No device connected and -NoEmulator was set."
}

# --- Build -------------------------------------------------------------------
if (-not $SkipBuild) {
    Write-Step "Building debug APK (gradlew clean assembleDebug)..."
    Push-Location $ProjRoot
    try { & (Join-Path $ProjRoot 'gradlew.bat') clean assembleDebug --console=plain }
    finally { Pop-Location }
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit $LASTEXITCODE)." }
}
if (-not (Test-Path $Apk)) { throw "APK not found at $Apk - run without -SkipBuild first." }

# --- Install + launch --------------------------------------------------------
Write-Step "Installing APK..."
& $Adb install -r "$Apk"

Write-Step "Launching $AppId..."
& $Adb shell monkey -p $AppId -c android.intent.category.LAUNCHER 1 | Out-Null

Write-Host "`nDone. App launched on the emulator." -ForegroundColor Green
