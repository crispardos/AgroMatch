# AgroMatch

Aplicación Android para conectar agricultores que necesitan servicios agrícolas con pilotos de drones. El proyecto está desarrollado con Kotlin y Jetpack Compose, y almacena sus datos localmente.

## Funcionalidades

- Registro e inicio de sesión con perfiles de agricultor y piloto.
- Creación de solicitudes de terreno con cultivo, zona, superficie y fechas.
- Adjuntar hasta **5 fotos por terreno**, combinando imágenes de la galería y capturas de cámara.
- Consultar los datos y las fotos guardadas en el detalle del terreno.
- Buscar pilotos compatibles y enviarles una solicitud.
- El piloto puede abrir la solicitud, revisar sus datos y fotos y aceptarla.
- Los estados de solicitud son distintos: pendiente (`ACTIVE`), aceptada (`ACCEPTED`) y completada (`COMPLETED`). Aceptar no completa el servicio.
- Persistencia local mediante Room/SQLite; las fotos se copian al almacenamiento privado de la aplicación.

## Tecnologías

- Kotlin 2.2.10
- Jetpack Compose y Material 3
- Navigation Compose
- Room 2.8.5
- Gradle 9.6.0
- Android Gradle Plugin 9.3.0
- `minSdk`: 24 · `compileSdk` / `targetSdk`: 37

## Requisitos

- Android Studio compatible con Android Gradle Plugin 9.3.0.
- JDK 25 (el proyecto configura esta versión para Gradle).
- Android SDK Platform 37 y las herramientas de compilación correspondientes.
- Emulador Android o dispositivo físico para ejecutar la aplicación.

## Abrir y ejecutar

1. Abre la carpeta `AgroMatch` desde Android Studio.
2. Espera a que termine la sincronización de Gradle y acepta la instalación de los componentes del SDK que solicite.
3. Selecciona un emulador o conecta un dispositivo Android.
4. Ejecuta la configuración `app` desde Android Studio.

También puedes compilar desde la terminal, en la carpeta del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Pruebas

En Windows, ejecuta:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
```

Para ejecutar las pruebas instrumentadas en un emulador o dispositivo conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

La prueba instrumentada requiere un dispositivo o emulador disponible para ADB.

## Estructura principal

```text
app/src/main/java/cl/duoc/agromatch/
├── database/      # Base de datos Room, DAO y migraciones
├── model/         # Entidades y reglas de dominio
├── repository/    # Acceso a datos y lógica de persistencia
├── ui/            # Navegación y pantallas Compose
└── viewmodel/     # Estado y acciones de presentación
```

Los esquemas exportados de Room se encuentran en `app/schemas/`.
