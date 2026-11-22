# AgroMatch

AgroMatch es una aplicación Android para conectar agricultores que necesitan servicios agrícolas con pilotos de drones. Permite publicar solicitudes de terreno, encontrar pilotos compatibles, gestionar solicitudes y valorar los servicios.

La aplicación está desarrollada con Kotlin y Jetpack Compose. La mayoría de sus flujos y datos se gestionan localmente con Room; además, hay una integración parcial con una API de Xano para autenticación y algunas operaciones.

## Cómo funciona la aplicación

### Inicio de sesión y perfiles

- Al iniciar, la aplicación inicializa la base local, crea datos de demostración si faltan y restaura la sesión guardada en `SharedPreferences`.
- El registro y el inicio de sesión envían las credenciales a Xano. Si la respuesta es correcta, la aplicación guarda una copia local del usuario en Room y mantiene localmente el identificador de la sesión.
- Hay perfiles de agricultor y piloto. El perfil del piloto incluye zonas de cobertura, cultivos o especialidades, capacidad de trabajo y disponibilidad.

### Flujo del agricultor

1. Crea una solicitud con tipo de servicio, cultivo, zona, superficie y fechas. Las entradas se validan antes de guardarse; también se pueden guardar coordenadas de ubicación.
2. Puede adjuntar hasta **5 fotos** desde la galería o la cámara. La aplicación copia las imágenes al almacenamiento privado del dispositivo y guarda sus rutas en Room.
3. Consulta la solicitud, sus fotos y su estado.
4. Busca pilotos compatibles. El cálculo local considera especialidad, zona, disponibilidad y capacidad; ordena los resultados por compatibilidad, valoración media, coincidencia de zona, productividad (ha/h) y alias.
5. Envía una solicitud a un piloto y, cuando el servicio está terminado, puede marcarlo como completado y dejar una valoración.

### Flujo del piloto y estados

El piloto consulta las solicitudes dirigidas a él, revisa sus datos y fotos, y puede aceptar una solicitud. Los estados de una solicitud son:

- `ACTIVE`: solicitud pendiente.
- `ACCEPTED`: aceptada por el piloto; **esto no significa que el servicio haya terminado**.
- `COMPLETED`: servicio finalizado por el agricultor.
- `REPLACED`: solicitud activa sustituida al elegir otro piloto.

La selección de otro piloto reemplaza una solicitud pendiente. No se permite reemplazarla si ya fue aceptada. Las valoraciones se guardan localmente, se admiten una vez por servicio completado y usan una puntuación de 1 a 5.

## Arquitectura y almacenamiento

El flujo principal sigue esta estructura:

```text
Pantallas Compose → ViewModels → Repositories → Room / Retrofit
```

- **Compose** muestra las pantallas; **Navigation Compose** organiza las rutas.
- Los **ViewModels** mantienen el estado de presentación y coordinan las acciones de la interfaz.
- Los **repositorios** aplican validaciones y reglas de negocio, y deciden cuándo acceder a Room o llamar a la API.
- **Room/SQLite** almacena usuarios, perfiles, cobertura y especialidades de pilotos, solicitudes, intereses, valoraciones y referencias a fotos. La base actual es la versión 6; las migraciones y esquemas exportados están en `app/src/main/java/cl/duoc/agromatch/database/` y `app/schemas/`.
- Los archivos de fotos se guardan bajo el almacenamiento privado de la aplicación (`files/need_photos/`). No se suben a Xano.
- La sesión local guarda el identificador del usuario activo en `SharedPreferences`.

## Conexión con Xano

La app usa Retrofit y Gson para comunicarse por HTTP con Xano:

1. `RetrofitInstance.kt` crea el cliente Retrofit y define en código la URL base de la API.
2. `ApiService.kt` declara las rutas y métodos HTTP; los modelos de petición y respuesta están en `remote/`.
3. Los repositorios llaman a ese cliente desde los flujos de autenticación, creación y aceptación de solicitudes. Android declara el permiso `INTERNET` en `AndroidManifest.xml`.

Las llamadas actuales se comportan así:

| Operación | Comportamiento |
| --- | --- |
| Registro e inicio de sesión | Se envían a `auth/signup` y `auth/login`. Si Xano no responde correctamente, el flujo de autenticación falla; no hay inicio de sesión offline. |
| Crear una solicitud | Se intenta enviar a `create-need`. Después se guarda la solicitud y sus fotos localmente. Si falla la llamada remota, la solicitud todavía se guarda en Room. |
| Aceptar una solicitud | Primero se cambia el estado local en Room y luego se intenta notificar a Xano mediante `accept-need`. Si la llamada remota falla, la aceptación local se conserva. |
| Consultar y probar endpoints | La pantalla **Laboratorio de APIs (Xano)** permite probar `get-needs`, `status` y operaciones CRUD de solicitudes. Es una herramienta de prueba, no el flujo normal de sincronización. |

**Importante:** la integración no es una sincronización completa entre Room y Xano. Los datos locales y remotos pueden quedar distintos si una llamada falla; las fotos y los perfiles de piloto se gestionan localmente. Aunque Xano devuelve un token al iniciar sesión, la app actualmente no lo guarda ni lo adjunta a las peticiones siguientes; tampoco conserva el ID remoto del usuario, por lo que las operaciones usan IDs locales y no hay reconciliación entre ambos sistemas. El método `syncNeedsFromXano()` existe, pero actualmente no se invoca. Tampoco hay código de backend de Xano incluido en este repositorio: aquí está el cliente Android.

## Tecnologías y configuración

- Kotlin 2.2.10, Jetpack Compose y Material 3
- Navigation Compose
- Room 2.8.5
- Retrofit 2.9.0 y Gson
- Android Gradle Plugin 9.3.0
- Gradle Wrapper 9.6.0
- `minSdk`: 24 · `compileSdk` / `targetSdk`: 37
- Destino de bytecode de Kotlin y Java: JVM 11

### Requisitos

- Android Studio compatible con Android Gradle Plugin 9.3.0.
- Android SDK Platform 37 y las herramientas de compilación correspondientes.
- JDK 17 como mínimo para Android Gradle Plugin 9.3.0 ([compatibilidad](https://developer.android.com/build/releases/agp-9-3-0-release-notes#compatibility)). **Gradle 9.6.0 admite JDK 25** ([compatibilidad de Java](https://docs.gradle.org/current/userguide/compatibility.html#java_runtime)), pero los archivos del proyecto no fijan esa versión; selecciona el JDK en la configuración de Gradle de Android Studio o en `JAVA_HOME`.
- Emulador Android o dispositivo físico para ejecutar la app. Las pruebas instrumentadas requieren que esté disponible mediante ADB.

El destino JVM 11 indica la compatibilidad del bytecode de la aplicación; no determina qué versión de JDK ejecuta Gradle.

## Abrir, compilar y ejecutar

1. Abre la carpeta `AgroMatch` desde Android Studio.
2. Espera a que termine la sincronización de Gradle e instala los componentes del SDK que solicite.
3. Selecciona un emulador o conecta un dispositivo Android.
4. Ejecuta la configuración `app`.

En Windows también puedes compilar desde la carpeta del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Pruebas

Pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Compilar las pruebas instrumentadas:

```powershell
.\gradlew.bat :app:compileDebugAndroidTestKotlin
```

Ejecutarlas en un emulador o dispositivo conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

## Estructura principal

```text
app/src/main/java/cl/duoc/agromatch/
├── database/      # Base Room, DAO, datos de demostración y migraciones
├── model/         # Entidades, catálogos, validaciones y reglas de matching
├── remote/        # Cliente Retrofit, endpoints y modelos para Xano
├── repository/    # Acceso a datos y coordinación de persistencia/API
├── ui/            # Navegación y pantallas Compose
├── utils/         # Utilidades, incluida la ubicación
└── viewmodel/     # Estado y acciones de presentación
```

Los esquemas exportados de Room se encuentran en `app/schemas/`.
