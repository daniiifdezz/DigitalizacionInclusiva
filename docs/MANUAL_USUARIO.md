# Manual de Usuario - Digitalización Inclusiva

Este manual describe los flujos de uso principales de la aplicación **Digitalización Inclusiva**, organizados por el rol de usuario. El sistema consta de una aplicación móvil (Android/Tablet) para el trabajo en campo y una aplicación de escritorio (Windows/JVM) para la gestión técnica.

---

## B.1. Manual del Agricultor (Aplicación Móvil / Tablet)

El agricultor utiliza la aplicación móvil para registrar de forma ágil y accesible las actividades realizadas en el campo.

### Paso 1 - Inicio de Sesión
Al abrir la aplicación se solicitan las credenciales de acceso.
* *Nota:* Si es la primera vez, el técnico de la cooperativa habrá creado previamente la cuenta del agricultor desde la aplicación de escritorio.

### Paso 2 - Pantalla de Inicio
Tras iniciar sesión, el agricultor accede a una pantalla principal diseñada con **alta visibilidad y accesos rápidos** a las funcionalidades habituales:
* Registrar una nueva actividad.
* Consultar parcelas.
* Revisar el historial de actividades.

### Paso 3 - Registrar una Nueva Actividad
Al pulsar el botón **"Nueva actividad"** se inicia un formulario asistido con los siguientes pasos:

1. **Seleccionar la Parcela:** Elegir la parcela tratada desde la lista asociada a su explotación.
2. **Superficie:** Indicar las hectáreas (`ha`) afectadas.
3. **Fecha:** Confirmar la fecha (el sistema propone la actual por defecto, pero es editable).
4. **Problema Fitosanitario:** Escribir el problema detectado.
    * 🎙️ *Función de Accesibilidad:* Admite **dictado por voz** pulsando el icono del micrófono. La app reconoce el castellano y rellena el campo de texto de forma automática.
5. **Productos Utilizados:** Añadir uno o varios productos fitosanitarios indicando su dosis.
    * *Función Avanzada:* Dispone de un **reconocedor óptico (OCR)** que permite fotografiar la etiqueta del producto para extraer automáticamente su número de registro y de albarán.
6. **Guardar:** Pulsar "Guardar actividad" (este botón solo se activará al completar todos los campos obligatorios).

>  **Estado Borrador:** La actividad queda registrada inicialmente en estado **"Borrador"** y se sincroniza automáticamente con el servidor para que el técnico pueda validarla.

### 🔍 Paso 4 - Consultar y Editar Actividades Pendientes
El agricultor puede revisar su historial y editar aquellas actividades que sigan en estado de *Borrador*. Una vez que el técnico la valida, la actividad pasa a estar **bloqueada** para garantizar la integridad de los datos.

### Paso 5 - Cerrar Sesión
Desde la pantalla de ajustes se puede cerrar la sesión. La aplicación **elimina los datos de la sesión activa** de forma segura, garantizando la privacidad si la tablet es compartida entre varios agricultores.

---

## B.2. Manual del Técnico (Aplicación de Escritorio)

El técnico utiliza la aplicación de escritorio en Windows para la gestión administrativa de la explotación y la validación legal de los datos.

### Paso 1 - Inicio de Sesión y Configuración Inicial
En el primer acceso, el técnico debe rellenar los datos legales obligatorios de la explotación: *Titular, dirección, número de registro nacional, equipos de aplicación disponibles y aplicadores habilitados con sus carnets ROPO*. Esta información se introduce una sola vez.

### Paso 2 - Gestión de Agricultores
Desde la pantalla de **Ajustes**, el técnico puede dar de alta a nuevos agricultores introduciendo su nombre, correo electrónico y una contraseña provisional que luego le facilitará al trabajador para su app móvil.

### Paso 3 - Gestión de Aplicadores
Desde **Configuración → Aplicadores**, se registra a las personas físicas habilitadas para los tratamientos fitosanitarios, introduciendo: *Nombre, apellidos, NIF, número de inscripción ROPO y el tipo de carnet* desde un menú desplegable.

### Paso 4 - Validación de Actividades
La pantalla **"Actividades"** muestra los registros agrupados por estado. Para validar las que están *"Pendientes de validar"*, el técnico sigue estos pasos:
1. Pulsa sobre la actividad para abrir su desglose.
2. **Pestaña "Datos":** Revisa y corrige fallos del agricultor (ej. fechas incorrectas) y completa datos obligatorios.
3. **Pestaña "Productos":** Verifica los productos aplicados y sus dosis correctas.
4. **Pestaña "Parcela":** Confirma los datos catastrales **SIGPAC**.
5. **Validar:** Al pulsar "Validar actividad", el registro pasa a estado **"Validada"** y queda blindado para el cuaderno oficial.

### Paso 5 - Generar el Cuaderno de Campo (Legal)
Desde el menú principal se accede a **"Cuaderno PDF"**:
1. Se selecciona el periodo de tiempo deseado.
2. Al pulsar **"Generar"**, la app exporta un documento **PDF oficial** que reproduce con total fidelidad la estructura exigida por el **Real Decreto 1311/2012**.
3. El documento queda listo para imprimirse, guardarse o presentarse digitalmente ante el organismo regulador.

---

## B.3. Notas de Accesibilidad y UX

Para todas las interfaces (tanto móviles como de escritorio) se ha priorizado el **diseño accesible**:
* **Botones grandes** y zonas de pulsación optimizadas.
* **Alto contraste** cromático para lecturas en exteriores (campo).
* **Flujos lineales** sin menús anidados complicados.

*El objetivo clave es minimizar por completo la fricción y la fatiga tecnológica para usuarios con baja alfabetización digital o dificultades motoras/visuales.*