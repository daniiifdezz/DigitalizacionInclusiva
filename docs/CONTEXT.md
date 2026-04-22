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
│       │   │       │   ├── DesktopSc.kt
│       │   │       │   └── PendientesSc.kt
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

## Esquema Real PostgreSQL

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

**Mobile:** Bottom Navigation Bar (4 tabs)
```
├── Tab 1: Actividades (listado + nueva + detalle + editar)
├── Tab 2: Parcelas (listado + crear)
├── Tab 3: Productos (listado + crear)
└── Tab 4: Ajustes
```

**Desktop:** Menú lateral
```
DesktopHome → Menu lateral
           → Todas las actividades
           → Pendientes de validar
           → Parcelas
           → Productos
```

### 3. Flujo de Estados

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
|------------|--------|---------|
| Navegación | Bottom Bar 4 tabs | Menú lateral |
| Crear parcela | ✅ Solo nombre | ✅ Completo |
| Crear producto | ✅ Campos mínimos | ✅ Completo |
| Validar | ❌ NO accesible | ✅ Con pestañas |
| Datos SIGPAC | ❌ NO | ✅ Sí |

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
| Tablas satélite | ✅ |
| CORS, Serialización JSON | ✅ |

#### App Móvil
| Feature | Estado |
|---------|--------|
| Listado actividades | ✅ |
| NuevaActividadSc | ✅ |
| Selector productos + dosis | ✅ |
| ActividadDetalleSc | ✅ |
| EditarActividadSc | ✅ |
| Selector de parcela | ✅ |
| Estados carga/error | ✅ |

#### App Desktop
| Feature | Estado |
|---------|--------|
| DesktopMainSc (menú lateral) | ✅ |
| Stats | ✅ |
| PendientesSc (filtros) | ✅ |
| ValidarActividadSc (4 pestañas) | ✅ |
| ParcelasSc (solo lectura) | ✅ |

---

## Sprint Planning

### SPRINT 1 — Funcionalidad básica demostrable

**Objetivo:** App móvil con 4 tabs funcional + backend para crear parcelas y productos

| Tarea | Descripción | Ficheros | Estimación | Prioridad |
|-------|-------------|----------|------------|-----------|
| 1.1 | Backend - POST crear parcelas | ParcelaRoutes.kt, DTOs.kt | 30 min | CRÍTICO |
| 1.2 | Backend - POST crear productos | ProductoRoutes.kt, DTOs.kt | 30 min | CRÍTICO |
| 1.3 | App - MisParcelasSc (móvil) | MisParcelasSc.kt (NUEVO) + modificar App.kt, ViewModel | 1 hora | CRÍTICO |
| 1.4 | App - ProductosSc (móvil) | ProductosSc.kt (NUEVO) + modificar App.kt, ViewModel | 1 hora | CRÍTICO |
| 1.5 | App - AjustesSc (móvil) | AjustesSc.kt (NUEVO) | 30 min | IMPORTANTE |
| 1.6 | App - Bottom Navigation | App.kt (navigation) | 1 hora | CRÍTICO |
| 1.7 | App - Repository/ViewModel actualizados | Repository, Api, ViewModel | 1 hora | CRÍTICO |

**Total Sprint 1:** ~6 horas

### SPRINT 2 — Funcionalidad completa móvil

| Tarea | Descripción | Estimación | Prioridad |
|-------|-------------|------------|-----------|
| 2.1 | Registrar semillas tratadas (móvil) | 1.5 horas | IMPORTANTE |
| 2.2 | Registrar fertilización básica (móvil) | 1 hora | IMPORTANTE |
| 2.3 | Backend - Rutas semillas (GET/POST) | 30 min | IMPORTANTE |
| 2.4 | Desktop - GestorParcelasSc completo | 2 horas | IMPORTANTE |
| 2.5 | Desktop - GestorProductosSc completo | 1.5 horas | IMPORTANTE |

**Total Sprint 2:** ~6.5 horas

### BACKLOG — Mejoras no urgentes

| Tarea | Prioridad |
|-------|-----------|
| SQLDelight offline | DESEABLE |
| OCR | DESEABLE |
| Voz | DESEABLE |
| API Clima | DESEABLE |
| Tablas Exposed faltantes | DESEABLE |
| Informes PDF | DESEABLE |
| Revisión DATABASE_MAPPING | DESEABLE |

---

## Cómo Ejecutar

### 1. Arrancar Backend (NECESARIO)
```powershell
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
```

### 4. Compilar todo
```powershell
.\gradlew.bat build
```

---

## Dependencias Clave

| Dependencia | Versión |
|-------------|---------|
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

---

## Próximo Día

1. **Sprint 1.1:** Backend - POST crear parcelas
2. **Sprint 1.2:** Backend - POST crear productos
3. Continuar con Sprint 1

---

*Última actualización: 22/04/2026*