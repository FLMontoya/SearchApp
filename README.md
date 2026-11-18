# SearchApp

App Android de búsqueda de música con pantalla de detalle, hecha con Jetpack Compose.

## API

[iTunes Search API](https://itunes.apple.com/search?term=jack&media=music): pública y sin API key.
Se consulta `GET /search?term=…&media=music&entity=song&limit=50`.

## Cómo ejecutar

1. Abrir el proyecto en Android Studio y ejecutar la configuración `app` en un emulador o dispositivo (minSdk 26), o bien:
   ```
   ./gradlew installDebug
   ```
2. Tests unitarios: `./gradlew :app:testDebugUnitTest`

## Arquitectura

Un solo módulo con tres capas separadas por paquetes, con dependencias en un único sentido
`presentation → domain ← data`:

- `domain`: `Track`, la interfaz `SearchRepository` y los casos de uso. Kotlin puro, sin Android ni Retrofit.
- `data`: Retrofit/OkHttp + kotlinx.serialization (`remote`), caché (`cache`) e implementación del repositorio.
- `presentation`: pantallas Compose, ViewModels con `StateFlow` y Navigation Compose.
- `di/AppContainer`: inyección manual (sin Hilt), creada una vez en `SearchApp`.

## Decisiones

**Caché.** `InMemoryTtlCache` guarda el resultado por query normalizada (`trim` + minúsculas) con TTL de 3 minutos.
Con una entrada válida no hay llamada de red; si expiró, se llama a la red y se reemplaza la entrada.
Los errores de red no se cachean. El reloj es una interfaz (`Clock`) inyectable y monotónico, por lo que la expiración se testea sin esperas.
El detalle resuelve la canción por id a partir de los resultados ya descargados (sin llamada extra), aunque la query ya haya expirado.

**Cancelación.** El `SearchViewModel` mantiene un único `Job` de búsqueda: cada cambio de texto lo cancela y lanza otro tras un debounce de 400 ms
(la acción *Buscar* del teclado lo salta). Una respuesta de una búsqueda cancelada no puede llegar a la UI.
Las `CancellationException` nunca se capturan sin relanzarlas.

**Accesibilidad.**
- `contentDescription` en la carátula del detalle y `null` en las imágenes e iconos decorativos.
- Áreas táctiles de al menos 48 dp, textos en `sp` y layouts sin alturas fijas para texto (diseñados para fuente al 200 %).
- Colores del tema Material 3 por defecto (contraste de texto ≥ 4,5:1) y el error se comunica con texto e icono, no solo con color.
- Títulos con `heading()`; cada fila de la lista y cada campo del detalle se agrupan como un único elemento.
- Los estados de carga, vacío y error se anuncian con `liveRegion`.
- Campo de búsqueda con `label` visible e `ImeAction.Search`; todos los textos en `strings.xml`.

## Tests

- Domain: caso de uso con un repositorio fake.
- Data: caché del repositorio (hit, miss, expiración y límite del TTL, errores no cacheados) y parseo de la respuesta con MockWebServer.
- ViewModel: debounce, estados, y cancelación de búsquedas anteriores.
