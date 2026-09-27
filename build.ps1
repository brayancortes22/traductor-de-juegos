# Script de automatización para Traductor de Juegos (Android)
# Autor: Brayan Stid Cortés Lombana (bscl)

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "  🎮 Traductor de Juegos — Build & Deploy" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Localizar ADB (en PATH o en platform-tools local)
$adb = "adb"
$adbFound = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbFound) {
    $fallbackAdb = "C:\Users\NITRO ACER\Desktop\proyectos con ia\app de netflix modo tv\platform-tools\adb.exe"
    if (Test-Path $fallbackAdb) {
        $adb = $fallbackAdb
        Write-Host "[OK] ADB localizado en: $adb" -ForegroundColor Green
    }
}

# 2. Verificar dispositivos Android conectados
Write-Host "`n[1/3] Verificando dispositivos Android conectados..." -ForegroundColor Yellow
$devices = & $adb devices -l
$devices | Out-String | Write-Host -ForegroundColor White

$connectedDevice = $devices | Where-Object { $_ -match "device\b" -and $_ -notmatch "List of" }

if ($connectedDevice) {
    Write-Host ">>> Dispositivo detectado exitosamente!" -ForegroundColor Green
} else {
    Write-Host ">>> [AVISO IMPORTANTE PARA SAMSUNG GALAXY TAB]:" -ForegroundColor Red
    Write-Host "    Si la depuracion USB dice 'Bloqueado por Bloqueador automatico':" -ForegroundColor Yellow
    Write-Host "    1. En tu tablet ve a: Ajustes -> Seguridad y privacidad." -ForegroundColor White
    Write-Host "    2. Toca en: Bloqueador automatico (Auto Blocker)." -ForegroundColor White
    Write-Host "    3. Desactivalo (ponlo en Desactivado)." -ForegroundColor White
    Write-Host "    4. Vuelve a Opciones de desarrollador y activa Depuracion por USB." -ForegroundColor White
    Write-Host "    5. Acepta el mensaje en la pantalla de la tablet: 'Permitir siempre'." -ForegroundColor White
}

# 3. Compilación y APK
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
Write-Host "`n[2/3] Verificando estado del APK..." -ForegroundColor Yellow
if (Test-Path $apkPath) {
    Write-Host "APK listo en: $apkPath" -ForegroundColor Green
    if ($connectedDevice) {
        Write-Host "`n[3/3] Instalando en tu tablet..." -ForegroundColor Yellow
        & $adb install -r $apkPath
        Write-Host ">>> ¡Instalacion completada! Abre Traductor de Juegos en tu tablet." -ForegroundColor Green
    }
} else {
    Write-Host "Para compilar el APK:" -ForegroundColor Yellow
    Write-Host "1. Abre la carpeta del proyecto en Android Studio." -ForegroundColor White
    Write-Host "2. Presiona Shift + F10 o ve a Build -> Build APK(s)." -ForegroundColor White
}
