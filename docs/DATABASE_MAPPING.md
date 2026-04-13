# MAPEO BASE DE DATOS - PostgreSQL ↔ Exposed

> **Última actualización:** 10/04/2026
> **Importante:** Modelo RELACIONAL con tablas satélite.

---

## Arquitectura de la Base de Datos

```
parcela (tabla principal)
│
├─── referenciasigpac     (1:1 vía parcela_id)
│       └── Identificación legal SIGPAC
│
├─── datosagronomicos     (1:1 vía parcela_id)
│       └── Información del cultivo actual
│
├─── datosmedioambientales (1:1 vía parcela_id)
│       └── Zonas protegidas
│
├─── cultivo              (N:1 vía datosagronomicos.cultivo_id)
│       └── Catálogo de cultivos
│
├─── actividad            (1:N vía parcela_id)
│       └── Tratamientos fitosanitarios
│
├─── fertilizacion        (N:N vía fertilizacion_parcela)
│       └── Fertilizaciones
│
└─── semillatratada      (1:N vía parcela_id)
        └── Semillas tratadas
```

---

## Tabla: PARCELA

### PostgreSQL
```sql
CREATE TABLE parcela (
    id                    SERIAL PRIMARY KEY,
    explotacion_id        INTEGER REFERENCES explotacion(id),
    orden                 INTEGER,
    sistema_asesoramiento VARCHAR(50),
    zona_nitratos         BOOLEAN
);
```

### Exposed (Tables.kt) - ACTUAL
```kotlin
object Parcelas : IntIdTable("parcela") {
    val explotacionId        = integer("explotacion_id")
    val orden                = integer("orden").nullable()
    val sistemaAsesoramiento = varchar("sistema_asesoramiento", 50).nullable()
    val zonaNitratos         = bool("zona_nitratos").nullable()
}
```

### Exposed (Tables.kt) - REQUERIDO
```kotlin
object Parcelas : IntIdTable("parcela") {
    val explotacionId        = integer("explotacion_id")
    val orden                = integer("orden").nullable()
    val sistemaAsesoramiento = varchar("sistema_asesoramiento", 50).nullable()
    val zonaNitratos         = bool("zona_nitratos").nullable()
}
```

**Nota:** Los campos SIGPAC están en tabla SATÉLITE, NO en parcela.

---

## Tabla: REFERENCIASIGPAC (NUEVA - Tabla Satélite)

### PostgreSQL
```sql
CREATE TABLE referenciasigpac (
    id                  SERIAL PRIMARY KEY,
    parcela_id          INTEGER REFERENCES parcela(id),
    numero_poligono     INTEGER,
    numero_recinto      INTEGER,
    numero_parcela      INTEGER,
    provincia           VARCHAR(100),
    termino_municipal   VARCHAR(100),
    uso_sigpac         VARCHAR(20),
    superficie_ha       DECIMAL(10, 3)
);
```

### Exposed (Tables.kt) - REQUERIDO
```kotlin
object ReferenciaSigpac : IntIdTable("referenciasigpac") {
    val parcelaId         = integer("parcela_id").references(Parcelas.id)
    val numeroPoligono    = integer("numero_poligono")
    val numeroRecinto     = integer("numero_recinto")
    val numeroParcela     = integer("numero_parcela")
    val provincia         = varchar("provincia", 100).nullable()
    val terminoMunicipal  = varchar("termino_municipal", 100).nullable()
    val usoSigpac         = varchar("uso_sigpac", 20).nullable()
    val superficieHa      = decimal("superficie_ha", 10, 3).nullable()
}
```

---

## Tabla: DATOSAGRONOMICOS (NUEVA - Tabla Satélite)

### PostgreSQL
```sql
CREATE TABLE datosagronomicos (
    id                  SERIAL PRIMARY KEY,
    parcela_id          INTEGER REFERENCES parcela(id),
    especie_variedad    VARCHAR(200),
    cultivo_id          INTEGER REFERENCES cultivo(id),
    secano_regadio      VARCHAR(20),
    fecha_inicio        DATE,
    fecha_fin           DATE
);
```

### Exposed (Tables.kt) - REQUERIDO
```kotlin
object DatosAgronomicos : IntIdTable("datosagronomicos") {
    val parcelaId        = integer("parcela_id").references(Parcelas.id)
    val especieVariedad  = varchar("especie_variedad", 200).nullable()
    val cultivoId       = integer("cultivo_id").references(Cultivos.id).nullable()
    val secanoRegadio   = varchar("secano_regadio", 20).nullable()
    val fechaInicio      = date("fecha_inicio").nullable()
    val fechaFin        = date("fecha_fin").nullable()
}
```

---

## Tabla: DATOSMEDIOAMBIENTALES (NUEVA - Tabla Satélite)

### PostgreSQL
```sql
CREATE TABLE datosmedioambientales (
    id                  SERIAL PRIMARY KEY,
    parcela_id          INTEGER REFERENCES parcela(id)
    -- (verificar campos exactos en pgAdmin)
);
```

### Exposed (Tables.kt) - REQUERIDO
```kotlin
object DatosMedioambientales : IntIdTable("datosmedioambientales") {
    val parcelaId = integer("parcela_id").references(Parcelas.id)
    // Añadir más campos según verificación
}
```

---

## Tabla: CULTIVO (Ya existe en código)

### PostgreSQL
```sql
CREATE TABLE cultivo (
    id                  SERIAL PRIMARY KEY,
    especie             VARCHAR(100),
    variedad            VARCHAR(100)
);
```

### Exposed (Tables.kt) - ACTUAL ✅
```kotlin
object Cultivos : IntIdTable("cultivo") {
    val especie  = varchar("especie", 100).nullable()
    val variedad = varchar("variedad", 100).nullable()
}
```

---

## Cómo Consultar Parcela Completa

### Query SQL para obtener todos los datos de una parcela
```sql
SELECT 
    p.id,
    p.orden,
    p.zona_nitratos,
    -- SIGPAC
    rs.numero_poligono,
    rs.numero_recinto,
    rs.uso_sigpac,
    rs.superficie_ha,
    -- Agronómicos
    da.especie_variedad,
    da.secano_regadio,
    -- Cultivo
    c.especie,
    c.variedad
FROM parcela p
LEFT JOIN referenciasigpac rs ON rs.parcela_id = p.id
LEFT JOIN datosagronomicos da ON da.parcela_id = p.id
LEFT JOIN cultivo c ON c.id = da.cultivo_id
WHERE p.id = ?;
```

### En Exposed (Routes)
```kotlin
fun getParcelaCompleta(id: Int): ParcelaResponse {
    return transaction {
        val parcela = Parcelas.selectAll()
            .where { Parcelas.id eq id }
            .singleOrNull()
            ?: return@transaction null

        // LEFT JOIN con tablas satélite
        val refSigpac = ReferenciaSigpac.selectAll()
            .where { ReferenciaSigpac.parcelaId eq id }
            .singleOrNull()

        val datosAgro = DatosAgronomicos.selectAll()
            .where { DatosAgronomicos.parcelaId eq id }
            .singleOrNull()

        // Construir respuesta combinada
        ParcelaCompletaResponse(
            id = parcela[Parcelas.id].value,
            orden = parcela[Parcelas.orden],
            // SIGPAC
            poligono = refSigpac?.get(ReferenciaSigpac.numeroPoligono),
            recinto = refSigpac?.get(ReferenciaSigpac.numeroRecinto),
            usoSigpac = refSigpac?.get(ReferenciaSigpac.usoSigpac),
            superficieHa = refSigpac?.get(ReferenciaSigpac.superficieHa)?.toDouble(),
            // Agronómicos
            especieVariedad = datosAgro?.get(DatosAgronomicos.especieVariedad),
            secanoRegadio = datosAgro?.get(DatosAgronomicos.secanoRegadio),
            zonaNitratos = parcela[Parcelas.zonaNitratos]
        )
    }
}
```

---

## DTO Propuesto: ParcelaCompletaResponse

```kotlin
@Serializable
data class ParcelaCompletaResponse(
    // Datos de parcela
    val id: Int,
    val orden: Int?,
    val zonaNitratos: Boolean?,
    val sistemaAsesoramiento: String?,

    // SIGPAC
    val poligono: Int?,
    val recinto: Int?,
    val numeroParcela: Int?,
    val provincia: String?,
    val terminoMunicipal: String?,
    val usoSigpac: String?,
    val superficieHa: Double?,

    // Agronómicos
    val especieVariedad: String?,
    val secanoRegadio: String?,

    // Cultivo
    val especie: String?,
    val variedad: String?
)
```

---

## Estrategia de Implementación

### Paso 1: Crear tablas satélite en Exposed
- [ ] `ReferenciaSigpac`
- [ ] `DatosAgronomicos`
- [ ] `DatosMedioambientales`

### Paso 2: Añadir a Tables.kt
- [ ] Mapear todas las columnas

### Paso 3: Crear endpoint combinado
- [ ] `GET /api/parcelas/{id}` con LEFT JOIN

### Paso 4: Modelo de dominio
- [ ] `ParcelaCompleta` en Models.kt

### Paso 5: Frontend
- [ ] DTO con campos SIGPAC
- [ ] Pantalla Desktop para ver/editar

---

## Importante para App Móvil

**NO necesita conocer las tablas satélite.**

El móvil solo recibe:
```kotlin
// Lo que ve el agricultor
ParcelaSimple(
    id = 1,
    nombre = "Parcela 12-45"  // mostrar orden o poligono-recinto
)
```

**Desktop recibe:**
```kotlin
// Lo que ve el técnico
ParcelaCompleta(
    // ... todos los campos
)
```

---

## Pendiente Verificar

- [ ] ¿Existen más tablas satélite conectadas a parcela?
- [ ] ¿Cuáles son los campos exactos de `datosmedioambientales`?
- [ ] ¿Hay `cultivo_id` en `datosagronomicos` o es solo `especie_variedad`?
- [ ] ¿La tabla `cosecha` está vinculada a parcela directamente o via actividad?
