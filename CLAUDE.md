# Prueba técnica Android
App Android de búsqueda + detalle. API: iTunes Search API
(https://itunes.apple.com/search?term=...&media=music), sin API key.

## Stack
- Kotlin, Jetpack Compose (todas las pantallas), Navigation Compose.
- Coroutines + StateFlow. Retrofit + OkHttp + kotlinx.serialization (o Moshi).
- Sin Hilt: inyección manual simple, para mantenerlo pequeño.
- Pantallas con alta accesibilidad (TalkBack, tamaño de fuente, contraste y navegación por teclado):
    - Texto accesible en iconos e imágenes con significado (`contentDescription`), `null` en los decorativos.
    - Áreas táctiles de al menos 48dp x 48dp.
    - Contraste de texto mínimo 4.5:1 y sin transmitir información solo con color.
    - Textos en `sp` y layouts que no se rompen con fuente al 200%.
    - Títulos con `heading()` y cada fila de la lista agrupada como un único elemento (`mergeDescendants`).
    - Estados de carga, vacío y error anunciados a TalkBack (`liveRegion`).
    - TextField con `label` visible y `ImeAction.Search`.
    - Strings en `strings.xml`.

## Requisitos
- Pantalla de búsqueda (TextField + LazyColumn) y pantalla de detalle (mín. 3 campos).
- Una búsqueda nueva cancela la anterior; nunca se muestran resultados antiguos.
- Capas Data / Domain / Presentation separadas (paquetes o módulos Gradle).
- Caché en memoria por query con TTL de 3 minutos. Con caché válida no hay llamada de red; si expira, red y se actualiza la caché.
- Reloj inyectable en la caché para poder testear la expiración.
- Tests: Domain con repositorio fake/mock; Data (hit, miss y expiración de caché); ViewModel (opcional).
- README breve: API elegida, cómo ejecutar, decisiones (caché, cancelación, arquitectura, accesibilidad).

## Criterios
- Código claro y simple, sin features extra.
- Nunca capturar CancellationException sin relanzarla.
- Revisar la accesibilidad con la checklist del Stack antes de dar la tarea por terminada.