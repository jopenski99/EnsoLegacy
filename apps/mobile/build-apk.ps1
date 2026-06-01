# Builds the Ensō Legacy Android app into an installable APK.
#
# Usage (from anywhere, in PowerShell):
#   .\build-apk.ps1             # build the debug APK, print where it landed
#   .\build-apk.ps1 -Install    # also install it onto a connected phone/emulator (needs USB debugging)
#
# The debug APK is fine for testing on your own phone — just enable
# "Install unknown apps" for your file manager, copy the .apk over, and tap it.
# (A release APK would need a signing key; not needed for personal testing.)

param([switch]$Install)

$ErrorActionPreference = 'Stop'

# --- Toolchain paths (match this machine's setup) ---------------------------
# If JAVA_HOME is already set to a valid JDK 17, we keep it; otherwise default.
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $env:JAVA_HOME = 'C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot'
}
$env:ANDROID_SDK_ROOT = 'Z:\Android\Sdk'
$env:ANDROID_HOME      = 'Z:\Android\Sdk'
$env:PATH              = "$env:JAVA_HOME\bin;$env:PATH"

$here = $PSScriptRoot

Write-Host "Building debug APK..." -ForegroundColor Cyan
& "$here\gradlew.bat" -p $here assembleDebug --console=plain
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit code $LASTEXITCODE)" }

$apk = Join-Path $here 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apk)) { throw "Build reported success but APK not found at:`n  $apk" }

$sizeMb = [math]::Round((Get-Item $apk).Length / 1MB, 1)
Write-Host ""
Write-Host "APK ready ($sizeMb MB):" -ForegroundColor Green
Write-Host "  $apk" -ForegroundColor Green

if ($Install) {
    $adb = 'Z:\Android\Sdk\platform-tools\adb.exe'
    Write-Host ""
    Write-Host "Installing onto connected device..." -ForegroundColor Cyan
    & $adb install -r $apk
    if ($LASTEXITCODE -ne 0) { throw "adb install failed (is a device connected with USB debugging on?)" }
    Write-Host "Installed." -ForegroundColor Green
}
