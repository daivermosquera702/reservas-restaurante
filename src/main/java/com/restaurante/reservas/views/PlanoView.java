package com.restaurante.reservas.views;

import com.restaurante.reservas.data.ArchivoDatos;
import com.restaurante.reservas.model.Mesa;
import com.restaurante.reservas.model.Reserva;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plano del restaurante: una cuadrícula de botones (como el Buscaminas), separada por zonas.
 * Cada mesa aparece en su fila y columna. Si está libre se ve en verde agua con un chulo;
 * si está reservada para la fecha y hora elegidas, en terracota rayada con un candado.
 * Al tocar una mesa, un Popover muestra sus reservas del día.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Plano · DAIMOS")
public class PlanoView extends Div {

    private final DatePicker fecha = new DatePicker("Fecha");
    private final TimePicker hora = new TimePicker("Hora");
    private final Div resumen = new Div();
    private final Div zonas = new Div();

    public PlanoView() {
        addClassName("vista");

        fecha.setValue(LocalDate.now());
        configurarHora(hora);
        hora.setValue(LocalTime.of(13, 0));
        fecha.addValueChangeListener(e -> dibujarPlano());
        hora.addValueChangeListener(e -> dibujarPlano());

        resumen.addClassName("plano-resumen");
        Div filtros = ComponentesUI.panel("plano-filtros", fecha, hora, resumen);

        zonas.addClassName("plano-zonas");

        add(ComponentesUI.encabezado("DAIMOS · Sabores del Pacífico", "Plano del restaurante",
                        "Elige la fecha y la hora que pide el cliente para ver qué mesas están libres. "
                                + "Toca una mesa para ver sus reservas del día."),
                filtros, crearLeyenda(), zonas);
        dibujarPlano();
    }

    /** TimePicker limitado al horario del restaurante, con saltos de 30 minutos. */
    static void configurarHora(TimePicker campo) {
        campo.setStep(Duration.ofMinutes(30));
        campo.setMin(Reserva.HORA_APERTURA);
        campo.setMax(Reserva.HORA_ULTIMA_RESERVA);
        campo.setHelperText("Entre " + Reserva.HORA_APERTURA + " y " + Reserva.HORA_ULTIMA_RESERVA);
    }

    private Div crearLeyenda() {
        Div leyenda = new Div(
                itemLeyenda("mesa-libre", VaadinIcon.CHECK, "Libre a la hora elegida"),
                itemLeyenda("mesa-reservada", VaadinIcon.LOCK, "Reservada a la hora elegida"),
                ComponentesUI.conIcono(VaadinIcon.USERS, "Capacidad de la mesa"));
        leyenda.addClassName("leyenda");
        leyenda.getElement().setAttribute("aria-label", "Leyenda del plano");
        return leyenda;
    }

    private Span itemLeyenda(String claseMesa, VaadinIcon icono, String texto) {
        Span muestra = new Span(icono.create());
        muestra.addClassNames("leyenda-muestra", claseMesa);
        Span item = new Span(muestra, new Span(texto));
        item.addClassName("leyenda-item");
        return item;
    }

    /** Vuelve a construir el plano según la fecha y hora elegidas. */
    private void dibujarPlano() {
        zonas.removeAll();
        resumen.removeAll();
        if (fecha.getValue() == null || hora.getValue() == null) {
            zonas.add(ComponentesUI.vacio("Elige una fecha y una hora para ver el plano."));
            return;
        }

        List<Mesa> mesas = ArchivoDatos.leerMesas();
        List<Reserva> reservasDelDia = ArchivoDatos.reservasDelDia(fecha.getValue());

        // Todas las zonas usan el mismo número de columnas para que el plano quede alineado
        int columnas = 1;
        for (Mesa m : mesas) {
            columnas = Math.max(columnas, m.getColumna() + 1);
        }

        // Agrupar las mesas por zona, primero Salón y luego Terraza
        Map<String, List<Mesa>> porZona = new LinkedHashMap<>();
        porZona.put("Salón", new ArrayList<>());
        porZona.put("Terraza", new ArrayList<>());
        for (Mesa m : mesas) {
            porZona.computeIfAbsent(m.getZona(), z -> new ArrayList<>()).add(m);
        }

        int libres = 0;
        int reservadas = 0;
        for (Map.Entry<String, List<Mesa>> entrada : porZona.entrySet()) {
            if (entrada.getValue().isEmpty()) {
                continue;
            }
            int[] conteo = new int[2];
            zonas.add(crearZona(entrada.getKey(), entrada.getValue(), columnas, reservasDelDia, conteo));
            libres += conteo[0];
            reservadas += conteo[1];
        }

        if (mesas.isEmpty()) {
            zonas.add(ComponentesUI.vacio("Todavía no hay mesas. Créalas en la vista Mesas."));
        }

        resumen.add(etiquetaResumen("resumen-libres", VaadinIcon.CHECK, libres + " libres"),
                etiquetaResumen("resumen-reservadas", VaadinIcon.LOCK, reservadas + " reservadas"));
    }

    private Span etiquetaResumen(String clase, VaadinIcon icono, String texto) {
        Span s = new Span(icono.create(), new Span(texto));
        s.addClassNames("estado", clase);
        return s;
    }

    /** Un panel por zona con su propia cuadrícula. conteo[0] = libres, conteo[1] = reservadas. */
    private Div crearZona(String nombreZona, List<Mesa> mesasZona, int columnas,
                          List<Reserva> reservasDelDia, int[] conteo) {
        boolean terraza = "Terraza".equalsIgnoreCase(nombreZona);

        int filaMinima = Integer.MAX_VALUE;
        int capacidadTotal = 0;
        for (Mesa m : mesasZona) {
            filaMinima = Math.min(filaMinima, m.getFila());
            capacidadTotal += m.getCapacidad();
        }

        Div cuadricula = new Div();
        cuadricula.addClassName("plano-cuadricula");
        // Dato del plano (no es un estilo): cuántas columnas tiene la cuadrícula
        cuadricula.getStyle().set("--columnas", String.valueOf(columnas));

        for (Mesa mesa : mesasZona) {
            boolean reservada = ArchivoDatos.mesaOcupada(mesa.getNumero(), fecha.getValue(), hora.getValue(), 0);
            if (reservada) {
                conteo[1]++;
            } else {
                conteo[0]++;
            }
            cuadricula.add(botonMesa(mesa, reservada, mesa.getFila() - filaMinima + 1, reservasDelDia));
        }

        Span icono = new Span((terraza ? VaadinIcon.SUN_O : VaadinIcon.HOME).create());
        icono.addClassName("zona-icono");
        H2 titulo = new H2(nombreZona);
        titulo.addClassName("zona-nombre");
        Span detalle = new Span(mesasZona.size() + " mesas · " + capacidadTotal + " puestos");
        detalle.addClassName("zona-detalle");
        Div textos = new Div(titulo, detalle);

        Div cabecera = new Div(icono, textos);
        cabecera.addClassName("zona-cabecera");

        Div scroll = new Div(cuadricula);
        scroll.addClassName("plano-cuadricula-scroll");

        Div zona = ComponentesUI.panel("zona" + (terraza ? " zona-terraza" : ""), cabecera, scroll);
        zona.getElement().setAttribute("aria-label", "Zona " + nombreZona);
        return zona;
    }

    private Button botonMesa(Mesa mesa, boolean reservada, int filaEnZona, List<Reserva> reservasDelDia) {
        Span numero = new Span(String.valueOf(mesa.getNumero()));
        numero.addClassName("mesa-numero");

        Span capacidad = new Span(VaadinIcon.USER.create(), new Span(String.valueOf(mesa.getCapacidad())));
        capacidad.addClassName("mesa-capacidad");

        Span textoEstado = new Span(reservada ? "Reservada" : "Libre");
        textoEstado.addClassName("mesa-estado-texto");
        Icon iconoEstado = (reservada ? VaadinIcon.LOCK : VaadinIcon.CHECK).create();
        Span estado = new Span(iconoEstado, textoEstado);
        estado.addClassName("mesa-estado");

        Div contenido = new Div(numero, capacidad, estado);
        contenido.addClassName("mesa-contenido");

        Button boton = new Button();
        boton.getElement().appendChild(contenido.getElement());
        boton.addClassNames("mesa", reservada ? "mesa-reservada" : "mesa-libre");
        // Posición de la mesa dentro de la cuadrícula (datos del plano, no estilos)
        boton.getStyle().set("--fila", String.valueOf(filaEnZona));
        boton.getStyle().set("--col", String.valueOf(mesa.getColumna() + 1));
        boton.setAriaLabel("Mesa " + mesa.getNumero() + ", " + mesa.getCapacidad() + " personas, "
                + mesa.getZona() + ", " + (reservada ? "reservada" : "libre") + " a las " + hora.getValue()
                + ". Ver reservas del día");

        // Popover con las reservas del día de esta mesa (se abre al tocar el botón)
        Popover popover = new Popover();
        popover.setTarget(boton);
        popover.add(contenidoPopover(mesa, reservasDelDia));
        zonas.add(popover);

        return boton;
    }

    private Div contenidoPopover(Mesa mesa, List<Reserva> reservasDelDia) {
        Span numero = new Span(String.valueOf(mesa.getNumero()));
        numero.addClassName("numero-mesa");
        Span titulo = new Span("Mesa " + mesa.getNumero());
        titulo.addClassName("popover-titulo");
        Div datos = new Div(titulo, new Div(ComponentesUI.zona(mesa.getZona()),
                ComponentesUI.conIcono(VaadinIcon.USERS, mesa.getCapacidad() + " personas")));
        datos.addClassName("celda-principal");
        Div cabecera = new Div(numero, datos);
        cabecera.addClassName("popover-cabecera");

        Div lista = new Div();
        lista.addClassName("popover-lista");
        Span fechaTexto = new Span("Reservas del " + ComponentesUI.fechaLarga(fecha.getValue()));
        fechaTexto.addClassName("texto-secundario");
        lista.add(fechaTexto);

        List<Reserva> deEstaMesa = reservasDelDia.stream()
                .filter(r -> r.getNumeroMesa() == mesa.getNumero())
                .sorted(Comparator.comparing(Reserva::getHora))
                .toList();

        if (deEstaMesa.isEmpty()) {
            lista.add(new Span("Sin reservas este día."));
        } else {
            for (Reserva r : deEstaMesa) {
                Span horaReserva = new Span(r.getHora().toString());
                horaReserva.addClassName("popover-hora");
                Span cliente = new Span(r.getCliente());
                cliente.addClassName("popover-cliente");
                Div detalle = new Div(ComponentesUI.conIcono(VaadinIcon.USERS, r.getPersonas() + " pers."),
                        ComponentesUI.estado(r.getEstado()));
                detalle.addClassName("popover-detalle");
                Div item = new Div(horaReserva, cliente, detalle);
                item.addClassName("popover-item");
                if (r.getHora().equals(hora.getValue())) {
                    item.addClassName("es-ahora");
                }
                lista.add(item);
            }
        }

        Div contenido = new Div(cabecera, lista);
        contenido.addClassName("popover-mesa");
        return contenido;
    }
}
