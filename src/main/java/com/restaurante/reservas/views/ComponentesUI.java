package com.restaurante.reservas.views;

import com.restaurante.reservas.model.Reserva;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Piezas visuales que se repiten en todas las vistas (encabezados, paneles,
 * etiquetas de estado, avisos). Así cada vista no repite el mismo código
 * y la apariencia queda definida por las clases de styles.css.
 */
public final class ComponentesUI {

    private static final Locale ES = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ES);
    private static final DateTimeFormatter FECHA_COMPLETA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES);

    private ComponentesUI() {
    }

    // ------------------------------------------------------------------
    // Marca
    // ------------------------------------------------------------------

    /** Logotipo: isotipo (marimba de chonta y río) + nombre. El lema es opcional. */
    public static Div marca(boolean conLema) {
        Image isotipo = new Image("images/daimos-isotipo.svg", "");
        isotipo.addClassName("marca-isotipo");

        Span nombre = new Span("DAIMOS");
        nombre.addClassName("marca-nombre");
        Div texto = new Div(nombre);
        texto.addClassName("marca-texto");
        if (conLema) {
            Span lema = new Span("Sabores del Pacífico");
            lema.addClassName("marca-lema");
            texto.add(lema);
        }

        Div marca = new Div(isotipo, texto);
        marca.addClassName("marca");
        marca.getElement().setAttribute("role", "img");
        marca.getElement().setAttribute("aria-label", "DAIMOS, Sabores del Pacífico");
        return marca;
    }

    // ------------------------------------------------------------------
    // Estructura de las vistas
    // ------------------------------------------------------------------

    /** Encabezado de sección: antetítulo, título, descripción y botones a la derecha. */
    public static Div encabezado(String antetitulo, String titulo, String descripcion, Component... acciones) {
        Span kicker = new Span(antetitulo);
        kicker.addClassName("section-kicker");
        H1 h1 = new H1(titulo);
        h1.addClassName("section-titulo");
        Paragraph sub = new Paragraph(descripcion);
        sub.addClassName("section-sub");

        Div textos = new Div(kicker, h1, sub);
        textos.addClassName("section-textos");

        Div encabezado = new Div(textos);
        encabezado.addClassName("section-header");
        if (acciones.length > 0) {
            Div zonaAcciones = new Div(acciones);
            zonaAcciones.addClassName("section-acciones");
            encabezado.add(zonaAcciones);
        }
        return encabezado;
    }

    /** Contenedor blanco con borde y sombra suave. */
    public static Div panel(String claseExtra, Component... hijos) {
        Div panel = new Div(hijos);
        panel.addClassName("panel");
        if (claseExtra != null) {
            panel.addClassNames(claseExtra.split(" "));
        }
        return panel;
    }

    /** Mensaje para cuando una lista no tiene resultados. */
    public static Div vacio(String mensaje) {
        Icon icono = VaadinIcon.CUTLERY.create();
        Div vacio = new Div(icono, new Span(mensaje));
        vacio.addClassName("vacio");
        return vacio;
    }

    public static Icon icono(VaadinIcon icono) {
        Icon i = icono.create();
        i.addClassName("icono-pequeno");
        return i;
    }

    /** Texto con un ícono delante (por ejemplo, el número de personas). */
    public static Span conIcono(VaadinIcon icono, String texto) {
        Span s = new Span(icono(icono), new Span(texto));
        s.addClassName("celda-icono");
        return s;
    }

    // ------------------------------------------------------------------
    // Botones
    // ------------------------------------------------------------------

    public static Button botonPrimario(String texto, VaadinIcon icono, ComponentEventListener<ClickEvent<Button>> accion) {
        Button b = new Button(texto, icono.create(), accion);
        b.getElement().getThemeList().add("primary");
        return b;
    }

    /** Botón solo con ícono: lleva tooltip y aria-label para que se entienda qué hace. */
    public static Button botonIcono(VaadinIcon icono, String descripcion, ComponentEventListener<ClickEvent<Button>> accion) {
        Button b = new Button(icono.create(), accion);
        b.getElement().getThemeList().add("tertiary");
        b.getElement().getThemeList().add("icon");
        b.setTooltipText(descripcion);
        b.setAriaLabel(descripcion);
        return b;
    }

    // ------------------------------------------------------------------
    // Etiquetas
    // ------------------------------------------------------------------

    /** Etiqueta del estado de una reserva, con ícono para no depender solo del color. */
    public static Span estado(String estado) {
        VaadinIcon icono;
        String clase;
        switch (estado) {
            case Reserva.CONFIRMADA -> { icono = VaadinIcon.CHECK_CIRCLE; clase = "estado-confirmada"; }
            case Reserva.CANCELADA -> { icono = VaadinIcon.CLOSE_CIRCLE; clase = "estado-cancelada"; }
            case Reserva.PENDIENTE -> { icono = VaadinIcon.CLOCK; clase = "estado-pendiente"; }
            default -> { icono = VaadinIcon.CIRCLE_THIN; clase = "estado-neutral"; }
        }
        Span etiqueta = new Span(icono.create(), new Span(estado));
        etiqueta.addClassNames("estado", clase);
        return etiqueta;
    }

    public static Span frecuente() {
        Span s = new Span(VaadinIcon.STAR.create(), new Span("Frecuente"));
        s.addClassName("distintivo-frecuente");
        s.getElement().setAttribute("title", "Cliente frecuente: 3 o más reservas activas");
        return s;
    }

    /** Etiqueta de zona: salón con ícono de casa, terraza con ícono de sol. */
    public static Span zona(String zona) {
        boolean terraza = "Terraza".equalsIgnoreCase(zona);
        Span s = new Span((terraza ? VaadinIcon.SUN_O : VaadinIcon.HOME).create(), new Span(zona));
        s.addClassNames("zona-badge", terraza ? "zona-terraza-badge" : "zona-salon");
        return s;
    }

    public static Span contador(String texto) {
        Span s = new Span(texto);
        s.addClassName("contador");
        return s;
    }

    // ------------------------------------------------------------------
    // Fechas
    // ------------------------------------------------------------------

    /** "jue. 5 nov." */
    public static String fechaCorta(LocalDate fecha) {
        return FECHA_CORTA.format(fecha);
    }

    /** "jueves, 5 de noviembre" */
    public static String fechaLarga(LocalDate fecha) {
        return FECHA_LARGA.format(fecha);
    }

    /** "jueves 5 de noviembre de 2026" */
    public static String fechaCompleta(LocalDate fecha) {
        return FECHA_COMPLETA.format(fecha);
    }

    // ------------------------------------------------------------------
    // Avisos
    // ------------------------------------------------------------------

    public static void exito(String mensaje) {
        Notification n = Notification.show(mensaje, 3000, Notification.Position.BOTTOM_CENTER);
        n.getElement().getThemeList().add("success");
    }

    public static void error(String mensaje) {
        Notification n = Notification.show(mensaje, 4500, Notification.Position.MIDDLE);
        n.getElement().getThemeList().add("error");
    }
}
