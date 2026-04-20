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
│           ├── ParcelaRoutes.kt
│           ├── FertilizacionRoutes.kt
│           └── ProductoRoutes.kt
│
├── composeApp/                # App Kotlin Multiplatform
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/org/dferna14/project/
│       │   │   ├── App.kt                    # Navegación principal
│       │   │   ├── Platform.kt           # expect/actual
│       │   │   ├── di/AppModule.kt       # Koin DI
│       │   │   ├── domain/model/
│       │   │   │   └── Models.kt        # Modelo de dominio
│       │   │   ├── data/
│       │   │   │   ├── remote/
│       │   │   │   │   ├── ActividadApi.kt  # Cliente HTTP
│       │   │   │   │   └── ApiClient.kt     # Configuración Ktor
│       │   │   │   ├── repository/
│       │   │   │   │   └── ActividadRepository.kt
│       │   │   │   └── local/
│       │   │   │       └── DatabaseDriverFactory.kt
│       │   │   └── ui/
│       │   │       ├── viewmodel/
│       │   │       │   └── ActividadViewModel.kt
│       │   │       ├── screens/
│       │   │       │   ├── ActividadListadoSc.kt
│       │   │       │   ├── ActividadDetalleSc.kt
│       │   │       │   ├── NuevaActividadSc.kt
│       │   │       │   ├── EditarActividadSc.kt
│       │   │       │   ├── ValidarActividadSc.kt
│       │   │       │   ├── DesktopSc.kt        # NUEVO
│       │   │       │   └── PendientesSc.kt     # NUEVO
│       │   │       └── theme/
│       │   │           ├── AppTheme.kt
│       │   │           └── Color.kt
│       │   └── sqldelight/
│       │       └── CuadernoCampo.sq      # Schema SQLDelight
│       ├── androidMain/
│       │   ├── MainActivity.kt
│       │   ├── MainApplication.kt
│       │   └── AndroidManifest.xml
│       └── jvmMain/
│           └── main.kt                  # Entry point Desktop
│
├── gradle/
│   └── libs.versions.toml                # Versiones centralizadas
│
└── .env                                 # Variables PostgreSQL
```

---

## Esquema Real PostgreSQL (20/04/2026)

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
| cosecha | - | ❌ FALTA |

---

## Decisiones Técnicas Importantes

### 1. Fechas con java.time.LocalDate

- Backend: Usa `org.jetbrains.exposed.sql.javatime.date`
- App: Usa `kotlinx-datetime` para `Clock.System.todayIn()`

### 2. Navegación Diferenciada por Plataforma

**Mobile:** Secuencial simple
```
Listado → Nueva → Detalle → Editar → Listado
```

**Desktop:** Con menú lateral + pantallas separadas
```
DesktopHome → Menu lateral
           → Todas las actividades (con botón volver)
           → Pendientes de validar
           → Parcelas
           → Productos
```

### 3. Flujo de Estados (Implementado)

**Estados:**
- `BORRADOR` - Creada en móvil, incompleta
- `PENDIENTE_VALIDAR` - Enviada, espera técnico
- `VALIDADA` - Cerrada por técnico

**Transiciones:**
```
BORRADOR → [Enviar] → PENDIENTE_VALIDAR → [Validar] → VALIDADA
                                ↳ [Devolver] → BORRADOR
```

### 4. Arquitectura Mobile vs Desktop

| Componente | Mobile | Desktop |
|------------|--------|----------|
| Listado | Simple (sin volver) | Con menú lateral + botón volver |
| Nueva | 5 campos básicos | - |
| Detalle | Datos mínimos | - |
| Validar | ❌ NO accesible | ✅ Pestañas |
| Menú | ❌ NO tiene | ✅ Menu lateral |

---

## Estado del Proyecto (20/04/2026)

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
| Tablas satélite | ✅ |
| CORS, Serialización JSON | ✅ |

#### App Móvil
| Feature | Estado |
|---------|--------|
| Listado actividades | ✅ |
| NuevaActividadSc (5 campos) | ✅ |
| + Selector productos + dosis | ✅ (20/04/2026) |
| ActividadDetalleSc | ✅ |
| - Sin Validar | ✅ (20/04/2026) |
| - Sin fechaFin/eficacia | ✅ (20/04/2026) |
| EditarActividadSc | ✅ |
| Selector de parcela | ✅ |
| Estados carga/error | ✅ |

#### App Desktop (NUEVO 20/04/2026)
| Feature | Estado |
|---------|--------|
| DesktopMainSc (menú lateral) | ✅ |
| Stats: Pendientes/Validadas/Total | ✅ |
| Botones acción rápida | ✅ |
| PendientesSc (con filtros) | ✅ |
| Botón "< Menu" para volver | ✅ |
| Listing con botón volver al menú | ✅ |
| Navegación diferenciada | ✅ |

---

### ⏳ PENDIENTE

#### Backend
| Tabla | Prioridad |
|-------|----------|
| Explotacion | MEDIA |
| Asesor | MEDIA |
| EquipoAplicacion | MEDIA |

#### App Móvil
| Feature | Prioridad |
|---------|----------|
| SQLDelight (offline) | ALTA |
| OCR | ALTA |
| Voz | ALTA |
| API clima | ALTA |

#### App Desktop
| Feature | Prioridad |
|---------|----------|
| ValidarActividadSc con pestañas | ALTA |
| Pantalla Parcelas | MEDIA |
| Pantalla Productos | MEDIA |

---

## Diferencias App Móvil vs Desktop

| Aspecto | Móvil (Android) | Desktop |
|--------|----------------|---------|
| Uso principal | Campo - registro rápido | Oficina - validación |
| Navegación | Pantallas secuenciales | Menú lateral |
| Detalle | Sin fechaFin/eficacia | ✅ Ver todos |
| Validar | ❌ NO accesible | ✅ Con pestañas |
| Productos | Selector simple | Catálogo completo |
| Volver | Botón simple | Botón "< Menu" |
| Stats | ❌ NO | ✅ sí |

### Flujo Mobile (Campo)
```
NuevaActividadSc:
  - Parcela (dropdown)
  - Fecha (auto)
  - Superficie tratada
  - Problema fitosanitario
  - Producto + dosis (NUEVO)
  - Observaciones
→ Guardar → BORRADOR

ActividadDetalleSc:
  - Ver datos (sin fechaFin/eficacia)
  - Editar (solo BORRADOR)
  - Enviar → PENDIENTE_VALIDAR
  - Eliminar
```

### Flujo Desktop (Oficina)
```
DesktopHome:
  - Stats (Pendientes, Validadas, Total)
  - Botones: Ver pendientes, Informes
  - Menu lateral

Menu → Todas las actividades:
  - Listado completo
  - Botón "< Menu" para volver

Menu → Pendientes de validar:
  - Filtros (estado, fecha)
  - Lista actividades
  - Botón "< Menu"

Menu → Validar:
  - Pestañas: Datos, Productos, Parcela, Validar
  - Campos técnicos (fecha fin, eficacia, equipo, aplicador)
```

---

## Planificación (Abril - Julio 2026)

**Tiempo disponible:** ~8 horas/semana

```
Semana 1-2 (Abr):  Desktop básico + navegación
Semana 3 (28-4 May):  SQLDelight offline
Semana 4 (May):     OCR
Semana 5 (May):     Voz + clima
Semana 6 (May):     Catálogos
Semana 7-8 (Jun):  Testing + memoria
```

---

## Cómo Ejecutar

### 1. Arrancar Backend (NECESARIO)
```powershell
# El backend debe estar corriendo para que la app funcione
.\gradlew.bat :backend:run
# Servidor en http://localhost:8080
```

### 2. Ejecutar App Desktop
```powershell
.\gradlew.bat :composeApp:run
```

### 3. Ejecutar App Android
```powershell
.\gradlew.bat :composeApp:installDebug
# O desde Android Studio con emulador
```

### 4. Compilar todo
```powershell
.\gradlew.bat build
```

---

## Dependencias Clave

| Dependencia | Versión |
|------------|---------|
| kotlin | 2.3.0 |
| ktor | 3.1.3 |
| exposed | 0.61.0 |
| compose | 1.10.0 |
| material3 | 1.10.0-alpha05 |
| sqldelight | 2.1.0 |
| koin | 4.1.0 |

---

## Problemas Conocidos

1. **Icons:** Sin iconos por conflicto con material-icons-extended
2. **Navigation Compose:** Eliminado, navegación manual
3. **menuAnchor():** Deprecated warning
4. **Icons Default:** No disponibles

---

## Próximo Día

1. **Pestañas en ValidarActividadSc** (Datos/Productos/Parcela/Validar)
2. **Pantalla Parcelas Desktop** (ver SIGPAC completo)
3. **Seguir con SQLDelight offline**

---

*Última actualización: 20/04/2026*