# 🎮 Traductor de Juegos — Android (Celular y Tablet)

Traductor flotante en tiempo real y on-demand para videojuegos móviles en Android (especialmente probado para *LifeAfter* y juegos MMORPG/Survival con textos en inglés o idiomas asiáticos).

---

## ✨ Características Principales
- 🫧 **Burbuja Flotante Flotante (Overlay):** Siempre accesible sobre cualquier juego sin interrumpir la partida.
- ⚡ **100% On-Device & Offline:** Utiliza **Google ML Kit** (Text Recognition & Translate). Cero consumo de datos móviles tras la primera descarga.
- 🛡️ **Antiban Garantizado:** Funciona a través de la API oficial de captura de pantalla (`MediaProjection`). No toca la memoria RAM ni inyecta código en *LifeAfter*.
- ✂️ **Modo Francotirador (Sniper / Crop):** Arrastra el dedo para recortar y traducir solo el diálogo, carta o menú deseado.
- 💾 **Caché Inteligente en Memoria:** Frases y palabras repetitivas se traducen al instante en 0 ms.
- 🎨 **Diseño Moderno & Glassmorphic:** Tarjeta translúcida con soporte para copiar al portapapeles y ajuste dinámico de opacidad.

---

## 🛠️ Pila Tecnológica
- **Lenguaje:** Kotlin 1.9
- **Plataforma:** Android Nativo (SDK 26 a 34+)
- **OCR:** `com.google.mlkit:text-recognition`
- **Traducción:** `com.google.mlkit:translate`
- **Servicio:** Foreground Service con `mediaProjection`
- **Ventanas:** Android `WindowManager` con `TYPE_APPLICATION_OVERLAY`
- **Arquitectura:** Modular, SRP, Clean Architecture (< 150-200 líneas por archivo)

---

## 🚀 Inicio Rápido
Consulta la [Guía de Instalación y Uso](docs/ANDROID_SETUP.md) para abrir el proyecto en Android Studio y compilarlo en tu celular o tablet.

O ejecuta el script de automatización en PowerShell:
```powershell
.\build.ps1
```
