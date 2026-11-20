# Esquema Xano de AgroMatch

Las ocho tablas se crearon en el workspace compartido `171003` de Xano. Los archivos locales incluyen los GUID asignados por Xano para vincular cada definición con su tabla remota. Antes de nuevas subidas, inspeccionar las tablas y los grupos de API del destino y revisar la vista previa. Cualquier Push to Draft o Publish de endpoints requiere autorización explícita.

## Tablas

| Archivo | Datos y restricciones |
| --- | --- |
| [`users.xs`](table/users.xs) | Tabla de autenticación, alias único normalizado, contraseña nativa marcada como sensible y roles `farmer` y `pilot`. |
| [`pilot_profiles.xs`](table/pilot_profiles.xs) | Perfil por usuario. Capacidad y fechas admiten valores nulos para que signup pueda crear un perfil base. |
| [`pilot_coverages.xs`](table/pilot_coverages.xs) | Una fila por perfil y zona. |
| [`pilot_specialties.xs`](table/pilot_specialties.xs) | Una fila por perfil y especialidad. |
| [`needs.xs`](table/needs.xs) | Terreno de un agricultor con tipo de servicio, cultivo, zona, superficie y fechas en días desde el inicio de la época Unix. |
| [`need_photos.xs`](table/need_photos.xs) | Hasta cinco cupos numerados por terreno. La columna `file` guarda metadatos y la referencia del archivo en Xano Storage. |
| [`interests.xs`](table/interests.xs) | Historial por terreno y piloto, fechas de transición y clave única para la solicitud vigente. |
| [`ratings.xs`](table/ratings.xs) | Una valoración por interés, con puntaje de 1 a 5 y origen `farmer_to_pilot`. |

En coberturas y especialidades, `pilotId` referencia `pilot_profiles.id`. En solicitudes, `pilotId` referencia `users.id`, que es el identificador del piloto autenticado. Los endpoints traducirán entre ambos a través de `pilot_profiles.userId`. El perfil base queda fuera de la búsqueda de pilotos mientras le falten los datos necesarios para calcular compatibilidad.

El alias se guarda en minúsculas y sin espacios al inicio o al final. El login deberá usar la misma normalización y validar el alias con autenticación nativa. Las respuestas de autenticación deberán enumerar los campos públicos que devuelven y ocultar credenciales en el historial de la API. Room queda como respaldo para las filas locales existentes.

## Concurrencia y almacenamiento

**Solicitudes.** `interests.activeNeedId` debe ser igual a `needId` en `ACTIVE` y `ACCEPTED`, y nulo en `REPLACED` y `COMPLETED`. El índice único impide que dos filas reclamen la misma clave. La regla que vincula la clave con el estado debe aplicarse en cada escritura, y el workspace de desarrollo debe confirmar que varias filas históricas pueden compartir el valor nulo.

Aceptar, reemplazar y completar requieren una escritura condicional que compruebe en la base el ID, la clave vigente y el estado previo. Para aceptar, el predicado incluye el ID del piloto autenticado. Para reemplazar o completar, el backend comprueba previamente la propiedad del terreno y sus cambios de estado permitidos. En un reemplazo, solo si la actualización del interés anterior afectó exactamente una fila se inserta el nuevo interés en la misma transacción. Un fallo de base de datos al insertar debe revertir también la actualización anterior.

Xano documenta consultas preparadas mediante `db.direct_query` y operaciones agrupadas mediante `db.transaction`. Antes de implementar los endpoints, comprobar en el workspace que una actualización condicional devuelve las filas afectadas y que comparte la transacción con la inserción siguiente. Un error de lógica por sí solo no demuestra que Xano revierte la transacción. Si el entorno no proporciona esa garantía, detener la implementación de las transiciones. [Operaciones de base de datos](https://docs.xano.com/xanoscript/function-reference/database-operations), [Transacciones en Xano](https://docs.xano.com/the-function-stack/functions/database-requests/database-transaction)

**Fotos.** El endpoint debe insertar primero una reserva con `file = null`, el ID del terreno y un `sortOrder` elegido en el servidor entre 1 y 5. El índice de `(needId, sortOrder)` arbitra intentos concurrentes por el mismo número. Ante un conflicto de unicidad, probar otro número dentro del rango. Si todos están ocupados, rechazar la carga. Las lecturas solo incluyen filas con archivo registrado. `reservedAt` sirve para identificar reservas que deban conciliarse tras una interrupción.

Si falla la carga o el registro, liberar la reserva y, si ya se guardó el archivo, eliminar el huérfano con `storage.delete_file`. La siguiente fase debe validar la limpieza y los reintentos, incluido un corte entre el guardado en Storage y el registro del archivo. El acceso a imágenes debe comprobarse en el backend antes de firmar un enlace. Xano documenta el almacenamiento privado para el plan Pro. Si el workspace de desarrollo no lo admite, la protección de las fotos queda pendiente. [Funciones de Storage](https://docs.xano.com/xanoscript/function-reference/file-storage), [Almacenamiento privado](https://docs.xano.com/file-storage/private-file-storage)

**Valoraciones.** El endpoint debe verificar propiedad y estado `COMPLETED` e insertar la valoración. La unicidad de `interestId` será la defensa contra dos altas en paralelo. La aplicación deberá tratar el rechazo por índice único como un conflicto de negocio.

## Verificación en desarrollo

La validación local con Xano Developer MCP comprueba la sintaxis de los ocho archivos. Antes de activar los endpoints, inspeccionar los índices y referencias en Xano, comprobar que no haya CRUD alternativo que eluda las reglas y ejecutar estas pruebas con cuentas de desarrollo en un entorno aislado.

| Prueba | Resultado esperado |
| --- | --- |
| Enviar dos altas de interés en paralelo para el mismo terreno. | Solo una solicitud vigente. |
| Aceptar y reemplazar el mismo interés en paralelo. | Solo una transición gana. Si gana el reemplazo, quedan una fila histórica y una nueva vigente. |
| Forzar un error en la inserción del reemplazo, después de actualizar el interés anterior. | El interés anterior conserva `ACTIVE` y su clave vigente. |
| Intentar aceptar como otro piloto, aceptar una solicitud reemplazada o completar una solicitud pendiente. | Operación rechazada y estado intacto. |
| Lanzar seis cargas en paralelo para el mismo terreno. Repetir forzando un error de carga y otro de registro. | Como máximo cinco cupos. Los fallos liberan el cupo y eliminan el archivo huérfano cuando corresponda. |
| Valorar antes de completar y enviar dos valoraciones simultáneas después de completar. | La primera operación se rechaza. De las dos posteriores, solo se guarda una. |

Para la prueba en paralelo, enviar las peticiones simultáneamente desde terminales independientes o un cliente HTTP, registrar las respuestas y verificar las filas resultantes en Xano. Un rechazo por límite de peticiones no comprueba la regla de concurrencia.
