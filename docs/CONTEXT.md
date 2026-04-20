# CONTEXTO DEL PROYECTO - Cuaderno de Campo Digital

## Información General

- **Proyecto:** Digitalización Inclusiva - Cuaderno de Campo Agrícola
- **Tipo:** TFG (Trabajo de Fin de Grado)
- **Alumno:** David Fernández (dferna14)
- **Tecnología Backend:** Kotlin + Ktor
- **Base de datos:** PostgreSQL + Exposed ORM
- **Tecnología Frontend:** Kotlin Multiplatform (Android + Desktop)
- **UI:** Jetpack Compose
- **Dispositivo objetivo:** Samsung Tablet
- **Entrega:** Julio 2026

---

## Estructura del Proyecto

```
DigitalizacionInclusiva/
├── backend/                    # Servidor Ktor
│   └── src/main/kotlin/org/dferna14/project/backend/
│       ├── Application.kt      # Punto de entrada
│       ├─��� db/
│       │   ├── Tables.kt     # Definición de tablas Exposed
│       │   └── DatabaseFactory.kt
│       ├── model/
│       │   └── DTOs.kt       # Data Transfer Objects
│       ├── plugins/
│       │   ├── Routing.kt
│       │   ├── Serialization.kt
│       │   ├── Cors.kt
│       │   └── StatusPages.kt
│       └── routes/
│           ├── ActividadRoutes.kt
│           ├── ParcelaRoutes.kt
│           ├── FertilizacionRoutes.kt
│           └── ProductoRoutes.kt
│
├── composeApp/                # App Kotlin Multiplatform
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/org/dferna14/project/
│       │   │   ├── App.kt                    # Navegación principal
│       │   │   ├── Platform.kt               # expect/actual
│       │   │   ├── di/AppModule.kt          # Koin DI
│       │   │   ├── domain/model/
│       │   │   │   └── Models.kt           # Modelo de dominio
│       │   │   ├── data/
│       │   │   │   ├── remote/
│       │   │   │   │   ├── ActividadApi.kt   # Cliente HTTP
│       │   │   │   │   └── ApiClient.kt    # Configuración Ktor
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
│       │   │       │   ├── EditarActividadSc.kt
│       │   │       │   └── ValidarActividadSc.kt
│       │   │       └── theme/
│       │   │           ├── AppTheme.kt
│       │   │           └── Color.kt
│       │   └── sqldelight/
│       │       └── CuadernoCampo.sq          # Schema SQLDelight
│       ├── androidMain/
│       │   ├── MainActivity.kt
│       │   ├── MainApplication.kt
│       │   └── AndroidManifest.xml
│       └── jvmMain/
│           └── main.kt                        # Entry point Desktop
│
├── gradle/
│   └── libs.versions.toml                    # Versiones centralizadas
│
└── .env                                   # Variables PostgreSQL
```

---

## Esquema Real PostgreSQL (13/04/2026)

### Todas las tablas en la base de datos

```
empresaservicio_asesor
parcela
referenciasigpac
datosagronomicos
datosmedioambientales
puntocaptacion
zonaespecifica
actividad
usuario
actividad_producto
producto
semillatratada
postcosecha
almacenamiento
transporte
cultivo
analisisproducto
cliente
cosechacomercializada
cosecha_parcela
fertilizacion
fertilizacion_parcela
explotacion
titular
empresaservicio
equipoaplicacion
explotacion_asesor
asesor
```

### Mapeo: PostgreSQL → Exposed (Tables.kt)

| Tabla PostgreSQL | En Exposed | Estado |
|-----------------|------------|---------|
| parcela | Parcelas | ✅ |
| referenciasigpac | ReferenciaSigpac | ✅ |
| datosagronomicos | DatosAgronomicos | ✅ |
| datosmedioambientales | DatosMedioambientales | ✅ |
| actividad | Actividades | ✅ |
| producto | Productos | ✅ |
| actividad_producto | ActividadProductos | ✅ |
| semillatratada | SemillasTratadas | ✅ |
| cultivo | Cultivos | ✅ |
| fertilizacion | Fertilizaciones | ✅ |
| **explotacion** | - | ❌ FALTA |
| **titular** | - | ❌ FALTA |
| **asesor** | - | ❌ FALTA |
| **equipoaplicacion** | - | ❌ FALTA |
| **usuario** | - | ❌ FALTA |
| almacenamiento | - | ❌ FALTA |
| transporte | - | ❌ FALTA |
| cosecha / cosecha_parcela | - | ❌ FALTA |
| analisisproducto | - | ❌ FALTA |
| puntocaptacion | - | ❌ FALTA |
| zonaespecifica | - | ❌ FALTA |
| postcosecha | - | ❌ FALTA |
| cliente | - | ❌ FALTA |
| empresaservicio | - | ❌ FALTA |
| empresaservicio_asesor | - | ❌ FALTA |
| explotacion_asesor | - | ❌ FALTA |

### Tabla ACTIVIDAD - Campos actuales (verificado 13/04/2026)

Los campos de **validación** fueron añadidos hoy:
- `fecha_fin` ✅ (añadido hoy)
- `eficacia` ✅ (añadido hoy)

---

## Decisiones Técnicas Importantes

### 1. Fechas con java.time.LocalDate

- Backend: Usa `org.jetbrains.exposed.sql.javatime.date`
- App móvil: Usa `kotlinx-datetime` para `Clock.System.todayIn()`

### 2. Navegación Manual (sin Navigation Compose)

Navegación con `sealed class Screen` y estado en `App.kt`.

```kotlin
sealed class Screen {
    object Listado : Screen()
    object NuevaActividad : Screen()
    data class Detalle(val actividadId: Int) : Screen()
    data class Editar(val actividadId: Int) : Screen()
    data class Validar(val actividadId: Int) : Screen()
}
```

### 3. Flujo de Estados

**Estados:**
- `BORRADOR` - Creada en móvil, incompleta
- `PENDIENTE_VALIDAR` - Enviada, espera técnico
- `VALIDADA` - Cerrada por técnico desktop (fechaFin, eficacia)

**Transiciones:**
```
BORRADOR → [Enviar] → PENDIENTE_VALIDAR → [Validar] → VALIDADA
                                ↳ [Devolver] → BORRADOR
```

### 4. Arquitectura por Capas

```
UI (Compose) → ViewModel → Repository → API → Backend → PostgreSQL
```

**No implementada aún:** Capa local con SQLDelight (offline-first).

---

## Estado del Proyecto

### ✅ COMPLETADO

#### Backend
| Feature | Estado |
|---------|--------|
| CRUD Actividades | ✅ |
| Flujo de estados (enviar/validar/devolver) | ✅ |
| Filtro por estado | ✅ |
| GET /api/actividades/pendientes | ✅ |
| CRUD Fertilizaciones | ✅ |
| GET Productos (solo lectura) | ✅ |
| GET Parcelas (solo lectura) | ✅ |
| GET Parcelas/{id}/completa | ✅ |
| Tablas satélite (ReferenciaSigpac, DatosAgronomicos, DatosMedioambientales) | ✅ |
| CORS, Serialización JSON, Manejo errores | ✅ |

#### App Móvil/Android
| Feature | Estado |
|---------|--------|
| Listado actividades con chips de estado | ✅ |
| NuevaActividadSc | ✅ |
| ActividadDetalleSc | ✅ |
| EditarActividadSc | ✅ |
| ValidarActividadSc | ✅ |
| Selector de parcela | ✅ |
| Navegación manual | ✅ |
| Estados carga/error | ✅ |
| Diálogo confirmar eliminar | ✅ |
| Botones dinámicos según estado | ✅ |

#### App Desktop
| Feature | Estado |
|---------|--------|
| Usa código compartido (commonMain) | ✅ |
| Acceso a pantalla Validar | ✅ |
| Compila y ejecuta | ✅ |

---

### ❌ PENDIENTE

#### Backend - Nuevas tablas a mapear
| Tabla | Prioridad |
|-------|-----------|
| Explotacion | ALTA |
| Asesor | ALTA |
| EquipoAplicacion | MEDIA |
| Usuario | MEDIA |
| Titular | MEDIA |
| Almacenamiento | BAJA |
| Transporte | BAJA |
| Cosecha | BAJA |

#### App Móvil/Android
| Feature | Prioridad |
|---------|-----------|
| **SQLDelight (offline cache)** | **ALTA** |
| Pantalla parcelas | MEDIA |
| Pantalla productos (catálogo) | Media |
| **OCR (CameraX + ML Kit)** | **ALTA** |
| **Dictado por voz** | **ALTA** |
| **API clima (OpenMeteo)** | **ALTA** |

#### App Desktop Específico
| Feature | Prioridad |
|---------|-----------|
| **Menú de navegación** | **ALTA** |
| **Listado pendientes con filtros** | **ALTA** |
| **Pantalla validación avanzada** | **ALTA** |

---

## Diferencias App Móvil vs Desktop

| Aspecto | Móvil (Android/Tablet) | Desktop |
|--------|------------------------|---------|
| **Uso principal** | Campo - registro rápido | Oficina - validación técnico |
| **Navegación** | Pantallas simples secuenciales | Menú completo (Drawer) |
| **Listado actividades** | Todas | Filtrable por estado/fecha/parcela |
| **Validar** | Accesible | Pantalla principal |
| **Datos mostrados** | Mínimos | Completos + campos SIGPAC |
| **OCR** | ✅ Cámara | ❌ No tiene sentido |
| **Voz** | ✅ Dictado en campo | ❌ Teclado |
| **Offline** | ✅ Obligatorio | ❌ Siempre conectado |
| **Clima** | ✅ Visible | ❌ No necesario |

---

## Planificación (Abril - Julio 2026)

**Tiempo disponible:** ~8 horas/semana

```
Semana 1 (14-20 Abr):  SQLDelight offline + sincronización local
Semana 2 (21-27 Abr):  Desktop menú + filtros + validar avanzada
Semana 3 (28-4 May):   OCR (CameraX + ML Kit) - tablets Samsung
Semana 4 (5-11 May):   Voz (Speech-to-text) + API clima OpenMeteo
Semana 5 (12-18 May):  Catálogos (Parcelas/Productos/Fertilizaciones)
Semana 6 (19-25 May):  Backend: Explotacion, Asesor, EquipoAplicacion
Semana 7-8 (Jun):      Testing + memoria TFG
Semana 9 (Jun-Jul):    Ajustes finales + presentación
```

---

## Semana 1: SQLDelight Offline

### Objetivos
1. Configurar schema SQLDelight
2. Implementar queries locales
3. Sincronización: guardar local → enviar cuando haya conexión

### Archivos a modificar
- `composeApp/src/commonMain/sqldelight/CuadernoCampo.sq`
- `composeApp/src/commonMain/kotlin/.../data/local/`
- `composeApp/src/commonMain/kotlin/.../data/repository/ActividadRepository.kt`

### Strategy: Offline-First
```
1. App guarda en SQLite local (SQLDelight)
2. Cuando hay conexión → sincroniza con backend
3. Estados: LOCAL → SINCRONIZANDO → SINCRONIZADO
```

---

## Dependencias Clave

| Dependencia | Versión | Uso |
|-------------|---------|-----|
| kotlin | 2.3.0 | Lenguaje |
| ktor | 3.1.3 | Server + Client HTTP |
| exposed | 0.61.0 | ORM PostgreSQL |
| exposed-java-time | 0.51.1 | Tipos fecha en Exposed |
| kotlinx-datetime | 0.7.1 | Fechas en Compose |
| sqldelight | 2.1.0 | SQLite local |
| koin | 4.1.0 | Inyección de dependencias |
| compose | 1.10.0 | UI (multiplatform) |
| material3 | 1.10.0-alpha05 | Diseño Material 3 |
| postgresql | 42.7.7 | Driver JDBC |
| camerax | 1.3.0 | Cámara OCR |
| mlkit-text-recognition | 16.0.0 | OCR |

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
```

### Ejecutar App Desktop
```bash
./gradlew :composeApp:run
```

### Compilar Todo
```bash
./gradlew build
```

### Variables de Entorno (.env)
```
DB_HOST=localhost
DB_PORT=5435
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=1234
```

---

## Problemas Conocidos

1. **Icons de Material3:** Sin iconos por conflicto. Usar botones de texto.
2. **Navigation Compose:** Eliminado por conflictos. Navegación manual.
3. **menuAnchor() deprecated:** Warning. Usar sobrecarga.
4. **expect/actual classes beta:** Warning Kotlin 2.3.

---

*Última actualización: 13/04/2026*