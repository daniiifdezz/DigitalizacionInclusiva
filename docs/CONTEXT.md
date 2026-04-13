# CONTEXTO DEL PROYECO - Cuaderno de Campo Digital

## Información General

- **Proyecto:** Digitalización Inclusiva - Cuaderno de Campo Agrícola
- **Tipo:** TFG (Trabajo de Fin de Grado)
- **Alumno:** David Fernández (dferna14)
- **Tecnología Backend:** Kotlin + Ktor
- **Base de datos:** PostgreSQL + Exposed ORM
- **Tecnología Frontend:** Kotlin Multiplatform (Android + Desktop)
- **UI:** Jetpack Compose

---

## Estructura del Proyecto

```
DigitalizacionInclusiva/
├── backend/                    # Servidor Ktor
│   └── src/main/kotlin/org/dferna14/project/backend/
│       ├── Application.kt      # Punto de entrada
│       ├── db/
│       │   ├── Tables.kt      # Definición de tablas Exposed
│       │   └── DatabaseFactory.kt
│       ├── model/
│       │   └── DTOs.kt        # Data Transfer Objects
│       ├── plugins/
│       │   ├── Routing.kt
│       │   ├── Serialization.kt
│       │   ├── Cors.kt
│       │   └── StatusPages.kt
│       └── routes/
│           ├── ActividadRoutes.kt
│           ├── ParcelaRoutes.kt1
│           ├── FertilizacionRoutes.kt
│           └── ProductoRoutes.kt
│
├── composeApp/                # App Kotlin Multiplatform
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/org/dferna14/project/
│       │   │   ├── App.kt                    # Navegación principal
│       │   │   ├── Platform.kt               # expect/actual
│       │   │   ├── di/AppModule.kt           # Koin DI
│       │   │   ├── domain/model/
│       │   │   │   └── Models.kt             # Modelo de dominio
│       │   │   ├── data/
│       │   │   │   ├── remote/
│       │   │   │   │   ├── ActividadApi.kt   # Cliente HTTP
│       │   │   │   │   └── ApiClient.kt      # Configuración Ktor
│       │   │   │   ├── repository/
│       │   │   │   │   └── ActividadRepository.kt
│       │   │   │   └── local/
│       │   │   │       └── DatabaseDriverFactory.kt (expect/actual)
│       │   │   └── ui/
│       │   │       ├── viewmodel/
│       │   │       │   └── ActividadViewModel.kt
│       │   │       ├── screens/
│       │   │       │   ├── ActividadListadoSc.kt
│       │   │       │   ├── ActividadDetalleSc.kt
│       │   │       │   ├── NuevaActividadSc.kt
│       │   │       │   └── EditarActividadSc.kt
│       │   │       └── theme/
│       │   │           ├── AppTheme.kt
│       │   │           └── Color.kt
│       │   └── sqldelight/
│       │       └── CuadernoCampo.sq          # Schema SQLDelight
│       ├── androidMain/
│       │   ├── MainActivity.kt
│       │   ├── MainApplication.kt             # Koin Android
│       │   └── AndroidManifest.xml
│       └── jvmMain/
│           └── main.kt                        # Entry point Desktop
│
├── gradle/
│   └── libs.versions.toml                      # Versiones centralizadas
│
└── .env                                       # Variables PostgreSQL
```

---

## Decisiones Técnicas Importantes

### 1. Fechas con java.time.LocalDate

**Problema original:** Conflicto entre `exposed-datetime` y `kotlinx-datetime` (versiones incompatibles).

**Solución aplicada:**
- Backend: Usa `org.jetbrains.exposed.sql.javatime.date` (JDK 11+)
- App móvil: Usa `kotlinx-datetime` para `Clock.System.todayIn()`
- Conversión: `LocalDate.parse()` en backend, `.toString()` al devolver JSON

```kotlin
// Tables.kt
val fechaInicio = date("fecha_inicio").nullable()

// Routes.kt - INSERT
val fechaLocal = java.time.LocalDate.parse(request.fechaInicio)
it[fechaInicio] = fechaLocal

// Routes.kt - SELECT
.toActividadResponse() {
    fechaInicio = this[Actividades.fechaInicio]?.toString() ?: ""
}
```

### 2. Navegación Manual (sin Navigation Compose)

**Problema:** `androidx.navigation.compose` versión `2.8.0-alpha10` tenía conflictos con `savedstate` causando crash en runtime.

**Solución:** Navegación con `sealed class Screen` y estado en `App.kt`.

```kotlin
sealed class Screen {
    object Listado : Screen()
    object NuevaActividad : Screen()
    data class Detalle(val actividadId: Int) : Screen()
    data class Editar(val actividadId: Int) : Screen()
}
```

### 3. Flujo de Estados (Implementado 10/04/2026)

Sistema de workflow para actividades agrícolas.

**Estados:**
- `BORRADOR` - Creada en móvil, incompleta
- `PENDIENTE_VALIDAR` - Enviada, espera técnico
- `VALIDADA` - Cerrada por técnico desktop

**Transiciones:**
```
BORRADOR → [Enviar] → PENDIENTE_VALIDAR → [Validar] → VALIDADA
                    ↳ [Devolver] → BORRADOR
```

**Endpoints:**
```
GET  /api/actividades?estado=X
GET  /api/actividades/pendientes
POST /api/actividades/{id}/enviar
POST /api/actividades/{id}/validar
POST /api/actividades/{id}/devolver
```

### 4. Arquitectura por Capas

```
UI (Compose) → ViewModel → Repository → API → Backend → PostgreSQL
```

**No implementada aún:** Capa local con SQLDelight (offline-first).

---

## Modelo de Datos Actual

### Tablas Backend (Exposed)

```kotlin
// Parcelas - datos básicos
object Parcelas : IntIdTable("parcela") {
    val explotacionId        = integer("explotacion_id")
    val orden                = integer("orden").nullable()
    val sistemaAsesoramiento = varchar("sistema_asesoramiento", 50).nullable()
    val zonaNitratos         = bool("zona_nitratos").nullable()
}

// Productos - catálogo fitosanitario
object Productos : IntIdTable("producto") {
    val nombreComercial = varchar("nombre_comercial", 100).nullable()
    val materiaActiva   = varchar("materia_activa", 100).nullable()
    val numeroRegistro  = varchar("numero_registro", 50).nullable()
}

// Actividades - con estado
object Actividades : IntIdTable("actividad") {
    val parcelaId             = integer("parcela_id").references(Parcelas.id)
    val equipoId              = integer("equipo_id").nullable()
    val aplicadorId           = integer("aplicador_id").nullable()
    val fechaInicio           = date("fecha_inicio").nullable()
    val fechaFin              = date("fecha_fin").nullable()
    val superficieTratada     = double("superficie_tratada").nullable()
    val problemaFitosanitario = text("problema_fitosanitario").nullable()
    val eficacia              = varchar("eficacia", 50).nullable()
    val observaciones         = text("observaciones").nullable()
    val estado                = varchar("estado", 30).default("BORRADOR")
}

// ActividadProductos - relación N:M
object ActividadProductos : IntIdTable("actividad_producto") {
    val actividadId = integer("actividad_id").references(Actividades.id)
    val productoId  = integer("producto_id").references(Productos.id)
    val dosis       = double("dosis")
}

// SemillasTratadas
object SemillasTratadas : IntIdTable("semillatratada") {
    val actividadId       = integer("actividad_id").references(Actividades.id)
    val parcelaId         = integer("parcela_id").references(Parcelas.id)
    val aplica            = bool("aplica").default(false)
    val fechaSiembra      = date("fecha_siembra").nullable()
    val superficieHa      = double("superficie_ha").nullable()
    val cantidadSemillaKg = double("cantidad_semilla_kg").nullable()
    val productoId        = integer("producto_id").references(Productos.id).nullable()
}

// Cultivos
object Cultivos : IntIdTable("cultivo") {
    val especie  = varchar("especie", 100).nullable()
    val variedad = varchar("variedad", 100).nullable()
}

// Fertilizaciones
object Fertilizaciones : IntIdTable("fertilizacion") {
    val cultivoId         = integer("cultivo_id").references(Cultivos.id).nullable()
    val aplica            = bool("aplica").default(false)
    val fechaInicio       = date("fecha_inicio").nullable()
    val fechaFin          = date("fecha_fin").nullable()
    val tipoProducto      = varchar("tipo_producto", 10).nullable()
    val numeroAlbaran     = varchar("numero_albaran", 50).nullable()
    val riquezaNPK        = varchar("riqueza_npk", 50).nullable()
    val dosis             = double("dosis").nullable()
    val tipoFertilizacion = varchar("tipo_fertilizacion", 10).nullable()
    val observaciones     = text("observaciones").nullable()
}
```

---

## Funcionalidades Implementadas

### Backend ✅
- [x] CRUD Actividades completo
- [x] CRUD Fertilizaciones completo
- [x] GET Productos (solo lectura)
- [x] GET Parcelas (solo lectura)
- [x] Flujo de estados (enviar/validar/devolver)
- [x] Filtro por estado en listados
- [x] CORS configurado
- [x] Serialización JSON
- [x] Manejo de errores centralizado

### App Móvil ✅
- [x] Listado de actividades con estado (chip de color)
- [x] Pantalla nueva actividad
- [x] Pantalla detalle actividad
- [x] Pantalla editar actividad
- [x] Selector de parcela
- [x] Navegación entre pantallas
- [x] Estados de carga/error
- [x] Diálogo confirmación eliminar
- [x] Botones enviar a validar / validar (según estado)

---

## Pendiente de Implementar

# PLANIFICACIÓN PRÓXIMA SESIÓN: Extensión SIGPAC
### Objetivo
Mapear los campos SIGPAC que YA EXISTEN en PostgreSQL pero NO están mapeados en Exposed.
### Estado actual
PostgreSQL (tabla: parcela)         Exposed (Parcelas.kt)
────────────────────────────────    ────────────────────────────────
✅ id                               ✅ id
✅ explotacion_id                   ✅ explotacionId
✅ orden                            ✅ orden
❌ poligono                         ❌ NO MAPEADO
❌ recinto                          ❌ NO MAPEADO
❌ superficie_ha                    ❌ NO MAPEADO
❌ sistema_explotacion              ❌ NO MAPEADO
❌ uso_sigpac                       ❌ NO MAPEADO
❌ especie                          ❌ NO MAPEADO
❌ variedad                         ❌ NO MAPEADO
✅ zona_nitratos                   ✅ zonaNitratos
✅ sistema_asesoramiento            ✅ sistemaAsesoramiento
### IMPORTANTE: Verificación de datos
**Los datos en PostgreSQL fueron extraídos del CUADERNO DE CAMPO OFICIAL real.**
**Pendiente verificar:**
- ¿Los nombres de columna en PostgreSQL coinciden con el documento original?
- ¿Hay campos adicionales en PostgreSQL que no estén en el cuaderno?
- ¿Hay campos en el cuaderno que falten en PostgreSQL?
  **Acción requerida antes de implementar:**
```sql
-- Ejecutar en PostgreSQL para ver estructura real:
\d parcela
-- Ver todas las columnas y tipos:
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'parcela';
---
Paso 1: Consultar estructura PostgreSQL (10 min)
\d parcela
-- Anotar TODOS los nombres de columna exactos
-- Ver datos de ejemplo:
SELECT * FROM parcela LIMIT 3;
Paso 2: Mapear en Exposed (Tables.kt)
// Añadir a object Parcelas:
val poligono = integer("poligono").default(0)
val recinto = integer("recinto").default(0)
val superficieHa = decimal("superficie_ha", 10, 3).nullable()
val sistemaExplotacion = varchar("sistema_explotacion", 20).nullable()
val usoSigpac = varchar("uso_sigpac", 10).nullable()
val especie = varchar("especie", 100).nullable()
val variedad = varchar("variedad", 100).nullable()
Paso 3: Actualizar DTOs (DTOs.kt)
@Serializable
data class ParcelaResponse(
    val id                  : Int,
    val explotacionId       : Int,
    val orden               : Int?     = null,
    val poligono            : Int,                    // NUEVO
    val recinto             : Int,                    // NUEVO
    val superficieHa        : Double? = null,       // NUEVO
    val sistemaExplotacion   : String? = null,       // NUEVO
    val usoSigpac           : String? = null,         // NUEVO
    val especie              : String? = null,        // NUEVO
    val variedad             : String? = null,        // NUEVO
    val sistemaAsesoramiento : String?  = null,
    val zonaNitratos        : Boolean? = null
)
Paso 4: Actualizar ParcelaRoutes.kt
// En toParcelaResponse():
ParcelaResponse(
    id                   = it[Parcelas.id].value,
    explotacionId        = it[Parcelas.explotacionId],
    orden                = it[Parcelas.orden],
    poligono             = it[Parcelas.poligono],
    recinto              = it[Parcelas.recinto],
    superficieHa         = it[Parcelas.superficieHa]?.toDouble(),
    sistemaExplotacion   = it[Parcelas.sistemaExplotacion],
    usoSigpac           = it[Parcelas.usoSigpac],
    especie              = it[Parcelas.especie],
    variedad             = it[Parcelas.variedad],
    sistemaAsesoramiento = it[Parcelas.sistemaAsesoramiento],
    zonaNitratos         = it[Parcelas.zonaNitratos]
)
Paso 5: Modelo de dominio (Models.kt)
data class Parcela(
    val id                   : Int,
    val explotacionId        : Int,
    val orden                : Int?     = null,
    val poligono             : Int,                    // NUEVO
    val recinto              : Int,                    // NUEVO
    val superficieHa          : Double? = null,         // NUEVO
    val sistemaExplotacion    : String? = null,         // NUEVO
    val usoSigpac            : String? = null,          // NUEVO
    val especie               : String? = null,          // NUEVO
    val variedad              : String? = null,          // NUEVO
    val sistemaAsesoramiento  : String?  = null,
    val zonaNitratos         : Boolean? = null
)
Paso 6: DTOs cliente (ActividadApi.kt)
@Serializable
data class ParcelaDto(
    val id                   : Int,
    val explotacionId        : Int,
    val orden                : Int?     = null,
    val poligono             : Int,
    val recinto              : Int,
    val superficieHa          : Double? = null,
    val sistemaExplotacion    : String? = null,
    val usoSigpac           : String? = null,
    val especie               : String? = null,
    val variedad              : String? = null,
    val sistemaAsesoramiento  : String? = null,
    val zonaNitratos         : Boolean? = null
)
Paso 7: Repository
// En ActividadRepository.kt, getParcelas():
val parcelas = api.getParcelas().map { dto ->
    Parcela(
        id                   = dto.id,
        explotacionId        = dto.explotacionId,
        orden                = dto.orden,
        poligono             = dto.poligono,
        recinto              = dto.recinto,
        superficieHa         = dto.superficieHa,
        sistemaExplotacion    = dto.sistemaExplotacion,
        usoSigpac           = dto.usoSigpac,
        especie               = dto.especie,
        variedad              = dto.variedad,
        sistemaAsesoramiento = dto.sistemaAsesoramiento,
        zonaNitratos         = dto.zonaNitratos
    )
}
NO TOCAR en App Móvil
- El agricultor NO necesita ver estos campos en el móvil
- El selector de parcela sigue funcionando igual (muestra "Parcela ${orden}" o "Parcela ${poligono}-${recinto}")
SÍ TOCAR en App Desktop
- Nueva pantalla "Detalle Parcela" para técnicos
- Mostrar todos los campos SIGPAC
- Posibilidad de editar (si aplica)
---
Tiempo estimado: 30-45 minutos

### Backend
- [ ] Campos SIGPAC en Parcela (poligono, recinto, especie, variedad)
- [ ] CRUD Equipos
- [ ] CRUD Aplicadores
- [ ] CRUD Explotaciones/Titulares
- [ ] Tabla Cosecha
- [ ] Tabla Almacén
- [ ] Tabla Análisis/Laboratorio
- [ ] Tabla Transporte
- [ ] Validación de datos en endpoints
- [ ] Paginación en listados

### App Móvil
- [ ] Pantalla de parcelas
- [ ] Pantalla de productos (catálogo)
- [ ] OCR con CameraX + ML Kit
- [ ] Dictado por voz
- [ ] API de clima (OpenMeteo)
- [ ] Integración SQLDelight (offline)
- [ ] Pantalla Desktop de validación completa
- [ ] Navegación Desktop (menú)

---

## Problemas Conocidos

1. **Icons de Material3:** No se usan iconos (Icons.Default) por problemas con la dependencia `material-icons-extended`. Solución temporal: botones de texto.

2. **Navigation Compose:** Eliminado por conflictos de versión. Navegación manual implementada.

3. **menuAnchor() deprecated:** Warning en tiempo de compilación. Usar sobrecarga con `ExposedDropdownMenuAnchorType`.

4. **expect/actual classes beta:** Warning de Kotlin 2.3 sobre clases expect/actual. Añadir flag `-Xexpect-actual-classes` si es necesario.

---

## Dependencias Clave

| Dependencia | Versión | Uso |
|-------------|---------|-----|
| kotlin | 2.3.0 | Lenguaje |
| ktor | 3.1.3 | Server + Client HTTP |
| exposed | 0.61.0 | ORM PostgreSQL |
| exposed-java-time | 0.51.1 | Tipos fecha en Exposed |
| kotlinx-datetime | 0.7.1 | Fechas en Compose |
| sqldelight | 2.1.0 | SQLite local (schema listo) |
| koin | 4.1.0 | Inyección de dependencias |
| compose | 1.10.0 | UI (multiplatform) |
| material3 | 1.10.0-alpha05 | Diseño Material 3 |
| postgresql | 42.7.7 | Driver JDBC |

---

## Notas de Desarrollo

### Ejecutar Backend
```bash
./gradlew :backend:run
# Servidor en http://localhost:8080
```

### Ejecutar App Android
```bash
./gradlew :composeApp:installDebug
# O desde Android Studio
```

### Ejecutar App Desktop
```bash
./gradlew :composeApp:run
```

### Variables de Entorno (.env)
```
DB_HOST=localhost
DB_PORT=5435
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=1234
```

### Compilar Todo
```bash
./gradlew build
```

---

## Próximos Pasos (según planificación)

1. **Extensión Parcela** - Añadir campos SIGPAC
2. **Pantalla Desktop** - Listado con filtros + validación
3. **SQLDelight** - Integrar cache local

---

*Última actualización: 10/04/2026*
