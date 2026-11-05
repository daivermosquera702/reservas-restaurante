package com.restaurante.reservas.views;

import com.restaurante.reservas.data.ArchivoDatos;
import com.restaurante.reservas.model.Mesa;
import com.restaurante.reservas.model.Reserva;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ocupación del día: indicadores calculados con las reservas activas de una fecha
 * y, debajo, todas esas reservas ordenadas por hora.
 */
@Route(value = "ocupacion", layout = MainLayout.class)
@PageTitle("Ocupación · DAIMOS")
public class OcupacionView extends Div {

    private final DatePicker fecha = new DatePicker("Fecha");
    private final Grid<Reserva> grid = new Grid<>();
    private final Div tarjetas = new Div();
    private final Div kpis = new Div();
    private final Div vacio = ComponentesUI.vacio("No hay reservas activas para esta fecha.");
    private final Span fechaTexto = ComponentesUI.contador("");
    private final Div tabla;
    private Map<Integer, Mesa> mesasPorNumero = new HashMap<>();

    public OcupacionView() {
        addClassName("vista");

        fecha.setValue(LocalDate.now());
        fecha.addValueChangeListener(e -> actualizar());
        Button hoy = new Button("Hoy", e -> fecha.setValue(LocalDate.now()));
        Button manana = new Button("Mañana", e -> fecha.setValue(LocalDate.now().plusDays(1)));
        Span info = new Span(fechaTexto);
        info.addClassName("toolbar-info");
        Div barra = ComponentesUI.panel("toolbar", fecha, hoy, manana, info);

        kpis.addClassName("kpis");

        grid.addClassName("tabla-daimos");
        grid.setAllRowsVisible(true);
        grid.addColumn(new ComponentRenderer<>(r -> {
            Span h = new Span(r.getHora().toString());
            h.addClassNames("celda-fecha", "celda-hora");
            return h;
        })).setHeader("Hora").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(this::celdaMesa)).setHeader("Mesa").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(r -> {
            Span nombre = new Span(r.getCliente());
            nombre.addClassName("celda-titulo");
            return nombre;
        })).setHeader("Cliente").setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(r -> ComponentesUI.conIcono(VaadinIcon.USERS, String.valueOf(r.getPersonas()))))
                .setHeader("Personas").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(r -> ComponentesUI.estado(r.getEstado())))
                .setHeader("Estado").setAutoWidth(true).setFlexGrow(0);

        tabla = ComponentesUI.panel("tabla-panel solo-escritorio", grid);
        tarjetas.addClassNames("tarjeta-lista", "solo-movil");

        add(ComponentesUI.encabezado("Servicio del día", "Ocupación del día",
                        "Resumen de las reservas activas de una fecha. Las canceladas no se cuentan."),
                barra, kpis, tabla, tarjetas, vacio);
        actualizar();
    }

    private Div celdaMesa(Reserva r) {
        Span numero = new Span(String.valueOf(r.getNumeroMesa()));
        numero.addClassName("numero-mesa");
        Div celda = new Div(numero);
        celda.addClassName("posicion");
        Mesa mesa = mesasPorNumero.get(r.getNumeroMesa());
        if (mesa != null) {
            celda.add(ComponentesUI.zona(mesa.getZona()));
        }
        return celda;
    }

    private Div tarjeta(Reserva r) {
        Span hora = new Span(r.getHora().toString());
        hora.addClassNames("celda-fecha", "celda-hora");
        Span nombre = new Span(r.getCliente());
        nombre.addClassName("celda-titulo");
        Div textos = new Div(hora, nombre);
        textos.addClassName("celda-principal");
        Div cabecera = new Div(textos, ComponentesUI.estado(r.getEstado()));
        cabecera.addClassName("tarjeta-cabecera");

        Mesa mesa = mesasPorNumero.get(r.getNumeroMesa());
        Div meta = new Div(
                ComponentesUI.conIcono(VaadinIcon.CUTLERY, "Mesa " + r.getNumeroMesa()
                        + (mesa != null ? " · " + mesa.getZona() : "")),
                ComponentesUI.conIcono(VaadinIcon.USERS, r.getPersonas() + " personas"));
        meta.addClassName("tarjeta-meta");
        return ComponentesUI.panel("tarjeta-registro", cabecera, meta);
    }

    private Div kpi(String clase, VaadinIcon icono, String etiqueta, String valor, String detalle) {
        Span textoEtiqueta = new Span(etiqueta);
        textoEtiqueta.addClassName("kpi-etiqueta");
        Span iconoKpi = new Span(icono.create());
        iconoKpi.addClassName("kpi-icono");
        Div cabecera = new Div(textoEtiqueta, iconoKpi);
        cabecera.addClassName("kpi-cabecera");
        Span textoValor = new Span(valor);
        textoValor.addClassName("kpi-valor");
        Span textoDetalle = new Span(detalle);
        textoDetalle.addClassName("kpi-detalle");
        Div tarjeta = ComponentesUI.panel("dashboard-card" + (clase == null ? "" : " " + clase),
                cabecera, textoValor, textoDetalle);
        tarjeta.getElement().setAttribute("role", "group");
        tarjeta.getElement().setAttribute("aria-label", etiqueta + ": " + valor + ". " + detalle);
        return tarjeta;
    }

    private void actualizar() {
        kpis.removeAll();
        tarjetas.removeAll();
        if (fecha.getValue() == null) {
            grid.setItems(List.of());
            fechaTexto.setText("Elige una fecha");
            tabla.setVisible(false);
            vacio.setVisible(true);
            return;
        }

        List<Mesa> mesas = ArchivoDatos.leerMesas();
        mesasPorNumero = new HashMap<>();
        for (Mesa m : mesas) {
            mesasPorNumero.put(m.getNumero(), m);
        }

        List<Reserva> delDia = ArchivoDatos.reservasDelDia(fecha.getValue());
        delDia.sort(Comparator.comparing(Reserva::getHora).thenComparing(Reserva::getNumeroMesa));
        grid.setItems(delDia);
        for (Reserva r : delDia) {
            tarjetas.add(tarjeta(r));
        }

        int personas = 0;
        int confirmadas = 0;
        int pendientes = 0;
        Set<Integer> mesasUsadas = new HashSet<>();
        for (Reserva r : delDia) {
            personas += r.getPersonas();
            mesasUsadas.add(r.getNumeroMesa());
            if (Reserva.CONFIRMADA.equals(r.getEstado())) {
                confirmadas++;
            } else if (Reserva.PENDIENTE.equals(r.getEstado())) {
                pendientes++;
            }
        }
        int totalMesas = mesas.size();
        int mesasLibres = Math.max(0, totalMesas - mesasUsadas.size());

        kpis.add(
                kpi(null, VaadinIcon.CALENDAR_CLOCK, "Reservas", String.valueOf(delDia.size()),
                        confirmadas + " confirmadas · " + pendientes + " pendientes"),
                kpi("kpi-oro", VaadinIcon.USERS, "Personas", String.valueOf(personas),
                        "Comensales esperados en el día"),
                kpi("kpi-terracota", VaadinIcon.LOCK, "Mesas ocupadas", String.valueOf(mesasUsadas.size()),
                        "Con al menos una reserva, de " + totalMesas),
                kpi("kpi-agua", VaadinIcon.CHECK, "Mesas disponibles", String.valueOf(mesasLibres),
                        "Sin ninguna reserva en el día"));

        fechaTexto.setText(ComponentesUI.fechaCompleta(fecha.getValue()));
        tabla.setVisible(!delDia.isEmpty());
        vacio.setVisible(delDia.isEmpty());
    }
}
