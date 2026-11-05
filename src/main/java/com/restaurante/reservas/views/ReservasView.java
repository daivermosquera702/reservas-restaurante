package com.restaurante.reservas.views;

import com.restaurante.reservas.data.ArchivoDatos;
import com.restaurante.reservas.model.Mesa;
import com.restaurante.reservas.model.Reserva;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD de reservas: crear, ver, modificar, confirmar, cancelar y eliminar.
 * Tiene búsqueda en tiempo real y filtro por estado.
 * En pantallas anchas se muestra un Grid; en tablet y celular, tarjetas con las mismas acciones.
 */
@Route(value = "reservas", layout = MainLayout.class)
@PageTitle("Reservas · DAIMOS")
public class ReservasView extends Div {

    private static final String TODOS = "Todos los estados";

    private final Grid<Reserva> grid = new Grid<>();
    private final Div tarjetas = new Div();
    private final Div vacio = ComponentesUI.vacio("No hay reservas que coincidan con la búsqueda.");
    private final TextField busqueda = new TextField();
    private final Select<String> filtroEstado = new Select<>();
    private final Span contador = ComponentesUI.contador("");
    private Div tabla;

    /** Cantidad de reservas activas por teléfono, para marcar a los clientes frecuentes. */
    private Map<String, Integer> reservasPorTelefono = new HashMap<>();
    /** Mesas por número, para mostrar la zona de cada reserva. */
    private Map<Integer, Mesa> mesasPorNumero = new HashMap<>();

    public ReservasView() {
        addClassName("vista");

        Button nueva = ComponentesUI.botonPrimario("Nueva reserva", VaadinIcon.PLUS, e -> abrirFormulario(null));

        busqueda.setPlaceholder("Buscar por cliente o teléfono");
        busqueda.setAriaLabel("Buscar por cliente o teléfono");
        busqueda.setPrefixComponent(VaadinIcon.SEARCH.create());
        busqueda.setClearButtonVisible(true);
        busqueda.setValueChangeMode(ValueChangeMode.EAGER); // filtra mientras se escribe
        busqueda.addValueChangeListener(e -> actualizarGrid());
        busqueda.addClassName("campo-busqueda");

        filtroEstado.setAriaLabel("Filtrar por estado");
        filtroEstado.setItems(TODOS, Reserva.PENDIENTE, Reserva.CONFIRMADA, Reserva.CANCELADA);
        filtroEstado.setValue(TODOS);
        filtroEstado.addValueChangeListener(e -> actualizarGrid());

        Span info = new Span(contador);
        info.addClassName("toolbar-info");
        Div barra = ComponentesUI.panel("toolbar", busqueda, filtroEstado, info);

        configurarGrid();
        tabla = ComponentesUI.panel("tabla-panel solo-escritorio-amplio", grid);
        tarjetas.addClassNames("tarjeta-lista", "solo-movil-amplio");

        add(ComponentesUI.encabezado("Gestión de reservas", "Reservas",
                        "Registra las reservas que llegan por teléfono, confírmalas cuando el cliente lo pida "
                                + "y cancélalas para liberar la mesa.", nueva),
                barra, tabla, tarjetas, vacio);
        actualizarGrid();
    }

    private void configurarGrid() {
        grid.addClassName("tabla-daimos");
        grid.setAllRowsVisible(true);
        grid.addColumn(new ComponentRenderer<>(this::celdaFecha)).setHeader("Fecha")
                .setComparator(r -> r.getFecha().atTime(r.getHora())).setSortable(true)
                .setWidth("116px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(this::celdaMesa)).setHeader("Mesa")
                .setComparator(Reserva::getNumeroMesa).setSortable(true).setWidth("92px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(this::celdaCliente)).setHeader("Cliente")
                .setComparator(Reserva::getCliente).setSortable(true).setWidth("210px").setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(r -> ComponentesUI.conIcono(VaadinIcon.USERS, String.valueOf(r.getPersonas()))))
                .setHeader("Personas").setWidth("118px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(r -> ComponentesUI.estado(r.getEstado()))).setHeader("Estado")
                .setComparator(Reserva::getEstado).setSortable(true).setWidth("156px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(this::celdaAcciones)).setHeader("Acciones")
                .setAutoWidth(true).setFlexGrow(0);
    }

    // ------------------------------------------------------------------
    // Celdas del Grid
    // ------------------------------------------------------------------

    private Div celdaFecha(Reserva r) {
        Span fecha = new Span(ComponentesUI.fechaCorta(r.getFecha()));
        fecha.addClassName("celda-fecha");
        Span hora = new Span(r.getHora().toString());
        hora.addClassNames("celda-hora", "texto-secundario");
        Div celda = new Div(fecha, hora);
        celda.addClassName("celda-principal");
        return celda;
    }

    private Div celdaMesa(Reserva r) {
        Span numero = new Span(String.valueOf(r.getNumeroMesa()));
        numero.addClassName("numero-mesa");
        Mesa mesa = mesasPorNumero.get(r.getNumeroMesa());
        Div celda = new Div(numero);
        celda.addClassName("posicion");
        if (mesa != null) {
            // La zona va como texto de ayuda; en las tarjetas se muestra completa
            numero.getElement().setAttribute("title", "Mesa " + mesa.getNumero() + " · " + mesa.getZona());
        }
        return celda;
    }

    private boolean esFrecuente(Reserva r) {
        return reservasPorTelefono.getOrDefault(r.getTelefono(), 0) >= ArchivoDatos.RESERVAS_CLIENTE_FRECUENTE;
    }

    private Div celdaCliente(Reserva r) {
        Span nombre = new Span(r.getCliente());
        Span titulo = new Span(nombre);
        titulo.addClassName("celda-titulo");
        if (esFrecuente(r)) {
            titulo.add(ComponentesUI.frecuente());
        }
        Span telefono = new Span(r.getTelefono());
        telefono.addClassName("texto-secundario");
        Div celda = new Div(titulo, telefono);
        celda.addClassName("celda-principal");
        return celda;
    }

    /**
     * Acciones con jerarquía: Confirmar es la acción principal (solo si está pendiente);
     * editar y cancelar son secundarias; eliminar va en rojo.
     */
    private Div celdaAcciones(Reserva r) {
        Component confirmar;
        if (Reserva.PENDIENTE.equals(r.getEstado())) {
            Button b = new Button("Confirmar", VaadinIcon.CHECK.create(), e -> cambiarEstado(r, Reserva.CONFIRMADA));
            b.getElement().getThemeList().add("primary");
            b.getElement().getThemeList().add("success");
            b.getElement().getThemeList().add("small");
            confirmar = b;
        } else {
            Span hueco = new Span();
            hueco.addClassName("hueco-accion");
            confirmar = hueco;
        }

        Button editar = ComponentesUI.botonIcono(VaadinIcon.EDIT, "Editar reserva", e -> abrirFormulario(r));
        Button cancelar = ComponentesUI.botonIcono(VaadinIcon.BAN, "Cancelar reserva", e -> confirmarCancelacion(r));
        Button eliminar = ComponentesUI.botonIcono(VaadinIcon.TRASH, "Eliminar reserva", e -> confirmarEliminacion(r));
        eliminar.getElement().getThemeList().add("error");

        editar.setEnabled(r.estaActiva());
        cancelar.setEnabled(r.estaActiva());

        Div acciones = new Div(confirmar, editar, cancelar, eliminar);
        acciones.addClassName("acciones-fila");
        return acciones;
    }

    // ------------------------------------------------------------------
    // Tarjeta para celular
    // ------------------------------------------------------------------

    private Div tarjeta(Reserva r) {
        Div cliente = celdaCliente(r);
        Div cabecera = new Div(cliente, ComponentesUI.estado(r.getEstado()));
        cabecera.addClassName("tarjeta-cabecera");

        Mesa mesa = mesasPorNumero.get(r.getNumeroMesa());
        Div meta = new Div(
                ComponentesUI.conIcono(VaadinIcon.CALENDAR, ComponentesUI.fechaCorta(r.getFecha())),
                ComponentesUI.conIcono(VaadinIcon.CLOCK, r.getHora().toString()),
                ComponentesUI.conIcono(VaadinIcon.CUTLERY, "Mesa " + r.getNumeroMesa()
                        + (mesa != null ? " · " + mesa.getZona() : "")),
                ComponentesUI.conIcono(VaadinIcon.USERS, r.getPersonas() + " personas"));
        meta.addClassName("tarjeta-meta");

        Div acciones = new Div();
        acciones.addClassName("mobile-actions");
        if (Reserva.PENDIENTE.equals(r.getEstado())) {
            Button confirmar = new Button("Confirmar", VaadinIcon.CHECK.create(), e -> cambiarEstado(r, Reserva.CONFIRMADA));
            confirmar.getElement().getThemeList().add("primary");
            confirmar.getElement().getThemeList().add("success");
            confirmar.addClassName("accion-crece");
            acciones.add(confirmar);
        }
        Button editar = new Button("Editar", VaadinIcon.EDIT.create(), e -> abrirFormulario(r));
        editar.addClassName("accion-crece");
        editar.setEnabled(r.estaActiva());
        acciones.add(editar);

        // Las acciones menos frecuentes van en un menú para que no se salgan de la pantalla
        MenuBar mas = new MenuBar();
        mas.getElement().getThemeList().add("tertiary");
        mas.getElement().getThemeList().add("icon");
        MenuItem item = mas.addItem(VaadinIcon.ELLIPSIS_DOTS_V.create());
        item.setAriaLabel("Más acciones");
        item.getElement().setAttribute("aria-label", "Más acciones");
        mas.setTooltipText(item, "Más acciones");
        SubMenu sub = item.getSubMenu();
        MenuItem cancelar = sub.addItem(ComponentesUI.conIcono(VaadinIcon.BAN, "Cancelar reserva"), e -> confirmarCancelacion(r));
        cancelar.setEnabled(r.estaActiva());
        sub.addItem(ComponentesUI.conIcono(VaadinIcon.TRASH, "Eliminar reserva"), e -> confirmarEliminacion(r));
        acciones.add(mas);

        return ComponentesUI.panel("tarjeta-registro", cabecera, meta, acciones);
    }

    /** Lee el archivo, aplica búsqueda y filtro, y recarga el Grid y las tarjetas. */
    private void actualizarGrid() {
        List<Reserva> todas = ArchivoDatos.leerReservas();

        reservasPorTelefono = new HashMap<>();
        for (Reserva r : todas) {
            if (r.estaActiva()) {
                reservasPorTelefono.merge(r.getTelefono(), 1, Integer::sum);
            }
        }
        mesasPorNumero = new HashMap<>();
        for (Mesa m : ArchivoDatos.leerMesas()) {
            mesasPorNumero.put(m.getNumero(), m);
        }

        String texto = busqueda.getValue() == null ? "" : busqueda.getValue().trim().toLowerCase();
        String estado = filtroEstado.getValue();

        List<Reserva> filtradas = new ArrayList<>();
        for (Reserva r : todas) {
            boolean coincideTexto = texto.isEmpty()
                    || r.getCliente().toLowerCase().contains(texto)
                    || r.getTelefono().contains(texto);
            boolean coincideEstado = estado == null || TODOS.equals(estado) || estado.equals(r.getEstado());
            if (coincideTexto && coincideEstado) {
                filtradas.add(r);
            }
        }
        filtradas.sort(Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHora));
        grid.setItems(filtradas);

        tarjetas.removeAll();
        for (Reserva r : filtradas) {
            tarjetas.add(tarjeta(r));
        }

        contador.setText(filtradas.size() + (filtradas.size() == 1 ? " reserva" : " reservas"));
        vacio.setVisible(filtradas.isEmpty());
        tabla.setVisible(!filtradas.isEmpty());
    }

    // ------------------------------------------------------------------
    // Formulario (crear y editar)
    // ------------------------------------------------------------------

    /** Abre el formulario. Si reserva es null, se crea una nueva. */
    private void abrirFormulario(Reserva reserva) {
        boolean esNueva = reserva == null;
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esNueva ? "Nueva reserva" : "Editar reserva");

        TextField cliente = new TextField("Cliente");
        TextField telefono = new TextField("Teléfono");
        DatePicker fecha = new DatePicker("Fecha");
        TimePicker hora = new TimePicker("Hora");
        IntegerField personas = new IntegerField("Personas");
        ComboBox<Mesa> mesa = new ComboBox<>("Mesa");

        cliente.setPrefixComponent(VaadinIcon.USER.create());
        telefono.setPrefixComponent(VaadinIcon.PHONE.create());
        telefono.setHelperText("Entre 7 y 10 dígitos");
        fecha.setMin(LocalDate.now());
        PlanoView.configurarHora(hora);
        personas.setMin(1);
        personas.setStepButtonsVisible(true);
        List<Mesa> mesas = ArchivoDatos.leerMesas();
        mesa.setItems(mesas);
        mesa.setItemLabelGenerator(Mesa::toString);
        mesa.setHelperText("La capacidad debe alcanzar para el grupo");

        if (!esNueva) {
            cliente.setValue(reserva.getCliente());
            telefono.setValue(reserva.getTelefono());
            fecha.setValue(reserva.getFecha());
            hora.setValue(reserva.getHora());
            personas.setValue(reserva.getPersonas());
            mesa.setValue(ArchivoDatos.buscarMesa(reserva.getNumeroMesa()));
        } else {
            fecha.setValue(LocalDate.now());
        }

        FormLayout formulario = new FormLayout(cliente, telefono, fecha, hora, personas, mesa);
        formulario.addClassName("formulario-daimos");
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));
        Paragraph intro = new Paragraph(esNueva
                ? "La reserva queda pendiente hasta que la confirmes."
                : "Los cambios se validan igual que una reserva nueva.");
        intro.addClassName("dialogo-intro");
        dialogo.add(intro, formulario);

        Button guardar = ComponentesUI.botonPrimario("Guardar reserva", VaadinIcon.CHECK, e -> {
            int idActual = esNueva ? 0 : reserva.getId();
            String error = validar(cliente.getValue(), telefono.getValue(), fecha.getValue(),
                    hora.getValue(), personas.getValue(), mesa.getValue(), idActual);
            if (error != null) {
                ComponentesUI.error(error);
                return;
            }
            guardarReserva(reserva, cliente.getValue().trim(), telefono.getValue().trim(),
                    fecha.getValue(), hora.getValue(), personas.getValue(), mesa.getValue().getNumero());
            dialogo.close();
            ComponentesUI.exito(esNueva ? "Reserva creada" : "Reserva actualizada");
            actualizarGrid();
        });

        Button cerrar = new Button("Cerrar", e -> dialogo.close());
        cerrar.getElement().getThemeList().add("tertiary");
        dialogo.getFooter().add(cerrar, guardar);
        dialogo.open();
    }

    /** Devuelve el mensaje de error, o null si todo está bien. */
    private String validar(String cliente, String telefono, LocalDate fecha, LocalTime hora,
                           Integer personas, Mesa mesa, int idActual) {
        if (cliente == null || cliente.isBlank()) {
            return "El nombre del cliente es obligatorio.";
        }
        if (telefono == null || !telefono.trim().matches("\\d{7,10}")) {
            return "El teléfono debe tener entre 7 y 10 dígitos.";
        }
        if (fecha == null || hora == null) {
            return "La fecha y la hora son obligatorias.";
        }
        if (fecha.isBefore(LocalDate.now())) {
            return "No se puede reservar en una fecha pasada.";
        }
        if (fecha.equals(LocalDate.now()) && hora.isBefore(LocalTime.now())) {
            return "Esa hora de hoy ya pasó.";
        }
        if (hora.isBefore(Reserva.HORA_APERTURA) || hora.isAfter(Reserva.HORA_ULTIMA_RESERVA)) {
            return "La hora debe estar entre " + Reserva.HORA_APERTURA + " y " + Reserva.HORA_ULTIMA_RESERVA + ".";
        }
        if (personas == null || personas < 1) {
            return "El número de personas debe ser al menos 1.";
        }
        if (mesa == null) {
            return "Elige una mesa.";
        }
        if (personas > mesa.getCapacidad()) {
            return "La mesa " + mesa.getNumero() + " solo tiene capacidad para " + mesa.getCapacidad() + " personas.";
        }
        if (ArchivoDatos.mesaOcupada(mesa.getNumero(), fecha, hora, idActual)) {
            return "La mesa " + mesa.getNumero() + " ya está reservada el " + fecha + " a las " + hora + ".";
        }
        return null;
    }

    private void guardarReserva(Reserva existente, String cliente, String telefono, LocalDate fecha,
                                LocalTime hora, int personas, int numeroMesa) {
        List<Reserva> reservas = ArchivoDatos.leerReservas();
        if (existente == null) {
            reservas.add(new Reserva(ArchivoDatos.siguienteIdReserva(), cliente, telefono, fecha, hora,
                    personas, numeroMesa, Reserva.PENDIENTE));
        } else {
            for (Reserva r : reservas) {
                if (r.getId() == existente.getId()) {
                    r.setCliente(cliente);
                    r.setTelefono(telefono);
                    r.setFecha(fecha);
                    r.setHora(hora);
                    r.setPersonas(personas);
                    r.setNumeroMesa(numeroMesa);
                }
            }
        }
        ArchivoDatos.guardarReservas(reservas);
    }

    // ------------------------------------------------------------------
    // Confirmar, cancelar y eliminar
    // ------------------------------------------------------------------

    private void cambiarEstado(Reserva reserva, String nuevoEstado) {
        List<Reserva> reservas = ArchivoDatos.leerReservas();
        for (Reserva r : reservas) {
            if (r.getId() == reserva.getId()) {
                r.setEstado(nuevoEstado);
            }
        }
        ArchivoDatos.guardarReservas(reservas);
        ComponentesUI.exito("Reserva " + nuevoEstado.toLowerCase());
        actualizarGrid();
    }

    private void confirmarCancelacion(Reserva r) {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("¿Cancelar la reserva?");
        dialogo.setText("Reserva de " + r.getCliente() + " el " + ComponentesUI.fechaCorta(r.getFecha())
                + " a las " + r.getHora() + " en la mesa " + r.getNumeroMesa() + ". La mesa quedará libre.");
        dialogo.setCancelable(true);
        dialogo.setCancelText("Volver");
        dialogo.setConfirmText("Sí, cancelar");
        dialogo.setConfirmButtonTheme("error primary");
        dialogo.addConfirmListener(e -> cambiarEstado(r, Reserva.CANCELADA));
        dialogo.open();
    }

    private void confirmarEliminacion(Reserva r) {
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("¿Eliminar la reserva?");
        dialogo.setText("Se borrará para siempre la reserva de " + r.getCliente() + " del "
                + ComponentesUI.fechaCorta(r.getFecha()) + ".");
        dialogo.setCancelable(true);
        dialogo.setCancelText("Volver");
        dialogo.setConfirmText("Eliminar");
        dialogo.setConfirmButtonTheme("error primary");
        dialogo.addConfirmListener(e -> {
            List<Reserva> reservas = ArchivoDatos.leerReservas();
            reservas.removeIf(x -> x.getId() == r.getId());
            ArchivoDatos.guardarReservas(reservas);
            ComponentesUI.exito("Reserva eliminada");
            actualizarGrid();
        });
        dialogo.open();
    }
}
