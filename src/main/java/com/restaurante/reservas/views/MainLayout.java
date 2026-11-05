package com.restaurante.reservas.views;

import com.restaurante.reservas.model.Reserva;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

import java.time.LocalDate;

/**
 * Layout compartido de DAIMOS.
 * - Menú lateral (verde manglar) con el logotipo completo y las 4 vistas.
 * - Barra superior con el botón del menú, la fecha de hoy y, en celular, el logotipo compacto.
 * En pantallas angostas el menú se oculta y se abre con el botón de tres rayas.
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        addClassName("daimos-app");
        setPrimarySection(Section.DRAWER);

        // Barra superior
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Abrir o cerrar el menú");

        Div marcaCompacta = ComponentesUI.marca(false);
        marcaCompacta.addClassName("marca-navbar");

        Span textoFecha = new Span("Hoy · " + ComponentesUI.fechaLarga(LocalDate.now()));
        textoFecha.addClassName("navbar-fecha-texto");
        Span fecha = new Span(VaadinIcon.CALENDAR.create(), textoFecha);
        fecha.addClassName("navbar-fecha");

        addToNavbar(toggle, marcaCompacta, fecha);

        // Menú lateral
        Div marca = ComponentesUI.marca(true);
        marca.addClassName("marca-daimos");

        Div franja = new Div();
        franja.addClassName("franja-pacifico");

        Span cabeceraMenu = new Span("Servicio");
        cabeceraMenu.addClassName("menu-cabecera");

        SideNav menu = new SideNav();
        menu.addClassName("menu-daimos");
        menu.addItem(new SideNavItem("Plano del salón", PlanoView.class, VaadinIcon.GRID_BIG.create()));
        menu.addItem(new SideNavItem("Reservas", ReservasView.class, VaadinIcon.CALENDAR_CLOCK.create()));
        menu.addItem(new SideNavItem("Ocupación del día", OcupacionView.class, VaadinIcon.CHART_GRID.create()));
        menu.addItem(new SideNavItem("Mesas", MesasView.class, VaadinIcon.CUTLERY.create()));

        Div pie = new Div();
        pie.addClassName("drawer-pie");
        Span tituloPie = new Span("Horario de reservas");
        tituloPie.addClassName("drawer-pie-titulo");
        Div textoPie = new Div(tituloPie, new Span(Reserva.HORA_APERTURA + " a " + Reserva.HORA_ULTIMA_RESERVA
                + ", cada 30 minutos"));
        pie.add(franja, textoPie);

        Div contenido = new Div(marca, cabeceraMenu, menu, pie);
        contenido.addClassName("drawer-contenido");
        addToDrawer(contenido);
    }
}
