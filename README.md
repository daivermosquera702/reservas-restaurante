# DAIMOS · Sabores del Pacífico — Reservas de restaurante (Proyecto 6)

Aplicación web en **Vaadin 25** para DAIMOS, un restaurante inspirado en la gastronomía y la cultura del Pacífico colombiano (Chocó). Antes, el restaurante tomaba las reservas por teléfono y las anotaba en un plano de papel. La aplicación permite ver de un vistazo qué mesas están libres para una fecha y una hora.

- **Autor:** Daiver Mosquera_
- **Curso:** Lógica de Programación 12-2 — 2026-2
- **Fecha:** 11 de noviembre de 2026

## Tecnologías

- Java 21
- Spring Boot 4.1 (proyecto creado con Spring Initializr)
- Vaadin 25.3 con el tema Lumo, solo con componentes gratuitos
- Archivos de texto (`.txt`) con los campos separados por `|`, sin base de datos
- Fuentes Inter y Playfair Display servidas desde el propio proyecto (licencia SIL OFL)

## Cómo ejecutarlo

1. Abre la carpeta del proyecto en VS Code.
2. Ejecuta la clase `ReservasRestauranteApplication`, o corre en la terminal:
   ```bash
   ./mvnw spring-boot:run
   ```
   (en Windows: `mvnw.cmd spring-boot:run`).
3. Abre http://localhost:8080 en el navegador.

La primera vez que corre, la aplicación crea la carpeta `datos/` con mesas y reservas de ejemplo.

## Estructura

```
src/main/java/com/restaurante/reservas/
├── ReservasRestauranteApplication.java   Clase principal: carga Lumo + styles.css y el favicon
├── model/
│   ├── Mesa.java                         número, capacidad, zona, fila, columna
│   └── Reserva.java                      cliente, teléfono, fecha, hora, personas, mesa, estado
├── data/
│   └── ArchivoDatos.java                 lee y escribe los .txt, y crea los datos de ejemplo
└── views/
    ├── MainLayout.java                   AppLayout: menú lateral con la marca y barra superior
    ├── ComponentesUI.java                piezas visuales repetidas (encabezados, etiquetas, avisos)
    ├── PlanoView.java                    plano por zonas con Popover y TimePicker   (ruta "")
    ├── ReservasView.java                 CRUD de reservas: tabla y tarjetas          (ruta "reservas")
    ├── OcupacionView.java                indicadores y reservas de una fecha         (ruta "ocupacion")
    └── MesasView.java                    CRUD de mesas con mini plano                (ruta "mesas")

src/main/resources/META-INF/resources/
├── styles.css                            toda la identidad visual (colores, tipografía, responsive)
├── fonts/                                Inter y Playfair Display (.woff2) + licencias OFL
└── images/daimos-isotipo.svg             isotipo: teclas de marimba de chonta y el río
```

### Formato de los archivos

| Archivo | Formato de cada línea |
|---|---|
| `datos/mesas.txt` | `numero\|capacidad\|zona\|fila\|columna` |
| `datos/reservas.txt` | `id\|cliente\|telefono\|fecha\|hora\|personas\|numeroMesa\|estado` |

Si falta un archivo, la aplicación lo vuelve a crear. Si una línea está mal escrita, se ignora (con `try/catch`) y la aplicación sigue funcionando.

## Identidad visual

| Elemento | Decisión |
|---|---|
| Marca | **DAIMOS · Sabores del Pacífico**. El isotipo en SVG representa las teclas de una marimba de chonta sobre su travesaño, con una onda de río debajo. |
| Color principal | Verde manglar `#17483E`: reemplaza el azul de Lumo en botones, menú y enlaces. |
| Acento | Oro chonta `#D49A43`: se usa con moderación (menú activo, franja decorativa, distintivo de cliente frecuente) y nunca con texto blanco encima. |
| Fondo | Arena `#F4EBDD`, con paneles blancos. |
| Texto | Ébano `#26332E`; el texto secundario es `#56635E`. |
| Disponibilidad (plano) | Libre: verde agua `#287C66` con fondo claro y un chulo. Reservada: terracota `#B84C3B` rayada, con un candado. |
| Estados de reserva | Etiquetas con fondo suave e ícono: Pendiente (reloj), Confirmada (chulo) y Cancelada (X, texto tachado). |
| Tipografía | Playfair Display para títulos y números destacados; Inter para textos, formularios y tablas. |

El plano y las reservas no se confunden: el plano usa fichas llenas (rayadas cuando están reservadas) y las reservas usan etiquetas en forma de pastilla, cada una con su propio ícono. Todos los pares de texto y fondo cumplen el contraste WCAG AA (4.5:1 o más).

### Responsive

- **Escritorio:** menú lateral fijo, plano con las zonas Salón y Terraza lado a lado, y tablas completas.
- **Tablet y portátiles pequeños:** el menú se abre con el botón de tres rayas. Las reservas se muestran en tarjetas de dos columnas por debajo de 1366 px.
- **Celular (320 a 430 px):** formularios en una sola columna y tarjetas en lugar de tablas. Las acciones poco frecuentes van en un menú "⋮", los botones miden al menos 44 px y no hay desplazamiento horizontal. Las mesas del plano se reducen hasta 46 px y, si no caben, solo se desplaza el plano.

## Manual de uso

> _Agrega aquí las capturas de pantalla de cada vista._

1. **Plano del salón:** elige la fecha y la hora. Cada mesa muestra su número, su capacidad y su estado: libre (verde agua, chulo) o reservada (terracota rayada, candado). Al tocar una mesa se abre un Popover con sus reservas de ese día, y la de la hora elegida aparece resaltada.
2. **Reservas:** busca por cliente o teléfono y filtra por estado. Con **Nueva reserva** abres el formulario. Desde cada reserva puedes:
   - **Confirmar**, solo si está pendiente.
   - **Editar**.
   - **Cancelar**, que pide confirmación.
   - **Eliminar**, que pide confirmación.

   Los clientes con 3 o más reservas activas llevan el distintivo **Frecuente**.
3. **Ocupación del día:** muestra 4 indicadores y debajo todas las reservas activas de la fecha:
   - reservas, con cuántas están confirmadas y cuántas pendientes;
   - personas;
   - mesas ocupadas;
   - mesas sin ninguna reserva.
4. **Mesas:** crea, edita y elimina mesas, y define su posición en el plano. Un mini plano muestra dónde queda cada una. No deja borrar una mesa que tiene reservas pendientes.

### Validaciones

- Cliente obligatorio y teléfono de 7 a 10 dígitos.
- No se aceptan fechas pasadas ni horas de hoy que ya pasaron.
- La hora debe estar entre 12:00 y 21:00, en saltos de 30 minutos.
- El número de personas no puede superar la capacidad de la mesa.
- No se puede reservar la misma mesa dos veces a la misma fecha y hora.

## Componentes investigados

| Componente | Uso en el proyecto |
|---|---|
| **TimePicker** | Hora de la reserva cada 30 min, limitada al horario del restaurante (`setStep`, `setMin`, `setMax`). |
| **Popover** | Al tocar una mesa del plano, muestra sus reservas del día (`setTarget`). |
| **ConfirmDialog** | Pide confirmación antes de cancelar o eliminar una reserva y antes de eliminar una mesa. |

## Bonificación

_No implementada todavía._ (Reservas de dos horas con detección de solapes, sugerencia de la mesa libre más pequeña y del horario libre más cercano.)
