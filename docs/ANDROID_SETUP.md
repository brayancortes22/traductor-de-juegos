# 📱 Guía de Instalación y Uso — Celular y Tablet Android

Esta guía te explica paso a paso cómo abrir el proyecto, compilar el APK e instalarlo en tu celular o tablet.

---

## 🛠️ Requisitos Previos
1. **Android Studio** (Recomendado: versión Iguana, Jellyfish o superior).
2. Celular o tablet con **Android 8.0 (Oreo)** en adelante (probado y compatible hasta Android 14+).
3. Cable USB para conectar el dispositivo a tu PC (o depuración inalámbrica por Wi-Fi).

---

## 🚀 Paso 1: Abrir el Proyecto en Android Studio
1. Abre **Android Studio**.
2. En la pantalla de bienvenida, haz clic en **Open** (o ve a `File` -> `Open`).
3. Selecciona la carpeta del proyecto:
   `c:\Users\NITRO ACER\Desktop\proyectos con ia\traductor de juegos`
4. Android Studio sincronizará automáticamente los archivos Gradle y descargará las dependencias necesarias de Google ML Kit.

---

## 📲 Paso 2: Activar Modo Desarrollador en tu Celular o Tablet
1. En tu celular o tablet, ve a **Ajustes** -> **Acerca del teléfono / Acerca de la tablet**.
2. Busca la opción **Número de compilación** (en Xiaomi es *Versión de MIUI / HyperOS*).
3. Toca esa opción **7 veces seguidas** hasta que aparezca el mensaje: *"¡Ya eres desarrollador!"*.
4. Vuelve a **Ajustes** -> **Opciones de desarrollador** (o *Ajustes adicionales* -> *Opciones de desarrollador*).
5. Activa:
   - ✅ **Depuración por USB** (USB Debugging).
   - ✅ (Si usas Xiaomi/Redmi) **Instalar vía USB**.

---

## ⚡ Paso 3: Compilar e Instalar la Aplicación
1. Conecta tu celular o tablet a la PC con el cable USB.
2. Acepta el mensaje que saldrá en la pantalla del celular: *"¿Permitir depuración por USB?"* (marca *Permitir siempre*).
3. En la barra superior de Android Studio, verás el nombre de tu dispositivo en la lista de dispositivos seleccionados.
4. Presiona el botón verde de **Play** (o pulsa `Shift + F10`).
5. Android Studio compilará el proyecto e instalará la aplicación directamente en tu dispositivo en pocos segundos.

---

## 🎮 Paso 4: Configuración Inicial en el Dispositivo
1. Abre la app **Traductor de Juegos** en tu celular/tablet.
2. **Permiso de Superposición:** Toca el botón *"Conceder Permiso de Superposición"* y activa el interruptor *"Mostrar sobre otras aplicaciones"*.
3. **Descarga del Modelo Offline:** Toca *"Descargar Modelo Offline"*. Solo toma unos 15 segundos (~30 MB). Una vez descargado, **no requerirás internet nunca más para traducir**.
4. **Iniciar Servicio:** Toca *"Iniciar Servicio Flotante"*. El sistema te pedirá confirmación para la captura de pantalla; presiona *"Iniciar ahora"*.
5. Verás aparecer una **burbuja flotante** en el borde de tu pantalla.

---

## 🎯 Paso 5: Cómo Usarlo en LifeAfter
1. Abre tu juego **LifeAfter**.
2. La burbuja permanecerá visible y accesible en todo momento:
   - **Para traducir rápidamente lo que ves:** Toca la burbuja una sola vez. En menos de un segundo saldrá una tarjeta translúcida con la traducción al español.
   - **Para traducir un cuadro específico (un arma, una carta, una misión):** Mantén presionada la burbuja y arrastra con el dedo sobre el área que quieres leer.
   - **Para mover la burbuja:** Arrástrala a cualquier borde cómodo donde no tape tus botones de disparo o movimiento.
