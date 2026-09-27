# Script de automatización para Traductor de Juegos (Android)
# Autor: Brayan Stid Cortés Lombana (bscl)

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  🎮 Traductor de Juegos — Build & Deploy" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Verificar si hay dispositivos conectados por ADB
Write-Host "`n[1/3] Verificando dispositivos Android conectados por ADB..." -ForegroundColor Yellow
$adbCmd = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCmd) {
    & adb devices
} else {
    Write-Host "ADB no encontrado en PATH. Puedes compilar directamente abriendo el proyecto en Android Studio." -ForegroundColor Gray
}

# 2. Instrucciones para compilar en Android Studio
Write-Host "`n[2/3] Para compilar y generar el APK:" -ForegroundColor Yellow
Write-Host "  1. Abre Android Studio." -ForegroundColor White
Write-Host "  2. Selecciona 'Open' y elige esta carpeta:" -ForegroundColor White
Write-Host "     $(Get-Location)" -ForegroundColor Green
Write-Host "  3. Presiona 'Run' (Shift + F10) con tu celular o tablet conectado por USB o Wi-Fi." -ForegroundColor White
Write-Host "  4. O ve al menu 'Build' -> 'Build Bundle(s) / APK(s)' -> 'Build APK(s)'." -ForegroundColor White

# 3. Comprobar existencia del APK compilado
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
Write-Host "`n[3/3] Verificando APK generado..." -ForegroundColor Yellow
if (Test-Path $apkPath) {
    Write-Host "APK encontrado en: $apkPath" -ForegroundColor Green
    if ($adbCmd) {
        $installChoice = Read-Host "¿Deseas instalarlo ahora en tu dispositivo conectado? (s/n)"
        if ($installChoice -eq "s") {
            & adb install -r $apkPath
            Write-Host "Instalación completada. Abre la app en tu celular/tablet." -ForegroundColor Green
        }
    }
} else {
    Write-Host "El APK aún no ha sido generado. Compílalo en Android Studio o con './gradlew assembleDebug'." -ForegroundColor Gray
}
