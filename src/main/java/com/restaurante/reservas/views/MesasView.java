package com.restaurante.reservas.views;

import com.restaurante.reservas.data.ArchivoDatos;
import com.restaurante.reservas.model.Mesa;
import com.restaurante.reservas.model.Reserva;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** CRUD de mesas: número, capacidad, zona y posición en el plano. */
@Route(value = "mesas", layout = MainLayout.class)
@PageTitle("Mesas · DAIMOS")
public class MesasView extends Div {

    private static final String TODAS = "Todas las zonas";
    private static final int MAX_FILAS = 10;
    private static final int MAX_COLUMNAS = 8;

    private final Grid<Mesa> grid = new Grid<>();
    private final Div tarjetas = new Div();
    private final Div vacio = ComponentesUI.vacio("No hay mesas que coincidan con la búsqueda.");
    private final TextField busqueda = new TextField();
    private final Select<String> filtroZona = new Select<>();
    private final Span contador = ComponentesUI.contador("");
    private final Div tabla;

    /** Datos para dibujar el mini plano de cada fila. */
    private int filasPlano = 1;
    private int columnasPlano = 1;
    private Set<String> posicionesOcupadas = new HashSet<>();

    public MesasView() {
        addClassName("vista");

        busqueda.setPlaceholder("Buscar por número");
        busqueda.setAriaLabel("Buscar mesa por número");
        busqueda.setPrefixComponent(VaadinIcon.SEARCH.create());
        busqueda.setClearButtonVisible(true);
        busqueda.setValueChangeMode(ValueChangeMode.EAGER);
        busqueda.addValueChangeListener(e -> actualizarGrid());
        busqueda.addClassName("campo-busqueda");

        filtroZona.setAriaLabel("Filtrar por zona");
        filtroZona.setItems(TODAS, "Salón", "Terraza");
        filtroZona.setValue(TODAS);
        filtroZona.addValueChangeListener(e -> actualizarGrid());

        Button nueva = ComponentesUI.botonPrimario("Nueva mesa", VaadinIcon.PLUS, e -> abrirFormulario(null));

        Span info = new Span(contador);
        info.addClassName("toolbar-info");
        Div barra = ComponentesUI.panel("toolbar", busqueda, filtroZona, info);

        grid.addClassName("tabla-daimos");
        grid.setAllRowsVisible(true);
        grid.addColumn(new ComponentRenderer<>(m -> {
            Span numero = new Span(String.valueOf(m.getNumero()));
            numero.addClassName("numero-mesa");
            return numero;
        })).setHeader("Mesa").setComparator(Mesa::getNumero).setSortable(true).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(m -> ComponentesUI.conIcono(VaadinIcon.USERS, m.getCapacidad() + " personas")))
                .setHeader("Capacidad").setComparator(Mesa::getCapacidad).setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(m -> ComponentesUI.zona(m.getZona())))
                .setHeader("Zona").setComparator(Mesa::getZona).setSortable(true).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::celdaPosicion)).setHeader("Posición en el plano").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(m -> {
            Button editar = ComponentesUI.botonIcono(VaadinIcon.EDIT, "Editar mesa " + m.getNumero(), e -> abrirFormulario(m));
            Button eliminar = ComponentesUI.botonIcono(VaadinIcon.TRASH, "Eliminar mesa " + m.getNumero(), e -> intentarEliminar(m));
            eliminar.getElement().getThemeList().add("error");
            Div acciones = new Div(editar, eliminar);
            acciones.addClassName("acciones-fila");
            return acciones;
        })).setHeader("Acciones").setAutoWidth(true).setFlexGrow(0);

        tabla = ComponentesUI.panel("tabla-panel solo-escritorio", grid);
        tarjetas.addClassNames("tarjeta-lista", "solo-movil");

        add(ComponentesUI.encabezado("Configuración del local", "Mesas",
                        "Define las mesas del salón y la terraza, su capacidad y el lugar que ocupan en el plano.", nueva),
                barra, tabla, tarjetas, vacio);
        actualizarGrid();
    }

    /** Mini plano con la celda de la mesa resaltada, más el texto de fila y columna. */
    private Div celdaPosicion(Mesa m) {
        Div mini = new Div();
        mini.addClassName("mini-plano");
        // Dato del plano: número de columnas de la cuadrícula
        mini.getStyle().set("--columnas", String.valueOf(columnasPlano));
        mini.getElement().setAttribute("aria-hidden", "true");
        for (int f = 0; f < filasPlano; f++) {
            for (int c = 0; c < columnasPlano; c++) {
                Span celda = new Span();
                celda.addClassName("mini-celda");
                if (f == m.getFila() && c == m.getColumna()) {
                    celda.addClassName("actual");
                } else if (posicionesOcupadas.contains(f + "," + c)) {
                    celda.addClassName("ocupada");
                }
                mini.add(celda);
            }
        }
        Span texto = new Span("Fila " + m.getFila() + " · Col. " + m.getColumna());
        texto.addClassName("texto-secundario");
        Div celda = new Div(mini, texto);
        celda.addClassName("posicion");
        return celda;
    }

    private Div tarjeta(Mesa m) {
        Span numero = new Span(String.valueOf(m.getNumero()));
        numero.addClassName("numero-mesa");
        Span titulo = new Span("Mesa " + m.getNumero());
        titulo.addClassName("celda-titulo");
        Div textos = new Div(titulo, ComponentesUI.conIcono(VaadinIcon.USERS, m.getCapacidad() + " personas"));
        textos.addClassName("celda-principal");
        Div izquierda = new Div(numero, textos);
        izquierda.addClassName("posicion");
        Div cabecera = new Div(izquierda, ComponentesUI.zona(m.getZona()));
        cabecera.addClassName("tarjeta-cabecera");

        Div posicion = celdaPosicion(m);

        Button editar = new Button("Editar", VaadinIcon.EDIT.create(), e -> abrirFormulario(m));
        editar.addClassName("accion-crece");
        Button eliminar = new Button("Eliminar", VaadinIcon.TRASH.create(), e -> intentarEliminar(m));
        eliminar.getElement().getThemeList().add("tertiary");
        eliminar.getElement().getThemeList().add("error");
        Div acciones = new Div(editar, eliminar);
        acciones.addClassName("mobile-actions");

        return ComponentesUI.panel("tarjeta-registro", cabecera, posicion, acciones);
    }

    private void actualizarGrid() {
        List<Mesa> todas = ArchivoDatos.leerMesas();

        filasPlano = 1;
        columnasPlano = 1;
        posicionesOcupadas = new HashSet<>();
        for (Mesa m : todas) {
            filasPlano = Math.max(filasPlano, m.getFila() + 1);
            columnasPlano = Math.max(columnasPlano, m.getColumna() + 1);
            posicionesOcupadas.add(m.getFila() + "," + m.getColumna());
        }

        String texto = busqueda.getValue() == null ? "" : busqueda.getValue().trim();
        String zona = filtroZona.getValue();
        List<Mesa> filtradas = new ArrayList<>();
        for (Mesa m : todas) {
            boolean coincideTexto = texto.isEmpty() || String.valueOf(m.getNumero()).contains(texto);
            boolean coincideZona = zona == null || TODAS.equals(zona) || zona.equals(m.getZona());
            if (coincideTexto && coincideZona) {
                filtradas.add(m);
            }
        }
        filtradas.sort(Comparator.comparing(Mesa::getNumero));
        grid.setItems(filtradas);

        tarjetas.removeAll();
        for (Mesa m : filtradas) {
            tarjetas.add(tarjeta(m));
        }
        int puestos = filtradas.stream().mapToInt(Mesa::getCapacidad).sum();
        contador.setText(filtradas.size() + (filtradas.size() == 1 ? " mesa · " : " mesas · ") + puestos + " puestos");
        tabla.setVisible(!filtradas.isEmpty());
        vacio.setVisible(filtradas.isEmpty());
    }

    private void abrirFormulario(Mesa mesa) {
        boolean esNueva = mesa == null;
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esNueva ? "Nueva mesa" : "Editar mesa " + mesa.getNumero());

        IntegerField numero = new IntegerField("Número");
        IntegerField capacidad = new IntegerField("Capacidad");
        Select<String> zona = new Select<>();
        zona.setLabel("Zona");
        zona.setItems("Salón", "Terraza");
        IntegerField fila = new IntegerField("Fila en el plano");
        IntegerField columna = new IntegerField("Columna en el plano");

        capacidad.setMin(1);
        capacidad.setMax(12);
        capacidad.setStepButtonsVisible(true);
        capacidad.setHelperText("De 1 a 12 personas");
        fila.setMin(0);
        fila.setMax(MAX_FILAS - 1);
        columna.setMin(0);
        columna.setMax(MAX_COLUMNAS - 1);
        fila.setHelperText("De 0 a " + (MAX_FILAS - 1));
        columna.setHelperText("De 0 a " + (MAX_COLUMNAS - 1));

        if (!esNueva) {
            numero.setValue(mesa.getNumero());
            numero.setReadOnly(true); // el número identifica a la mesa en las reservas
            capacidad.setValue(mesa.getCapacidad());
            zona.setValue(mesa.getZona());
            fila.setValue(mesa.getFila());
            columna.setValue(mesa.getColumna());
        }

        FormLayout formulario = new FormLayout(numero, capacidad, zona, fila, columna);
        formulario.addClassName("formulario-daimos");
        formulario.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2));
        Paragraph intro = new Paragraph("La fila y la columna indican dónde aparece la mesa en el plano.");
        intro.addClassName("dialogo-intro");
        dialogo.add(intro, formulario);

        Button guardar = ComponentesUI.botonPrimario("Guardar mesa", VaadinIcon.CHECK, e -> {
            String error = validar(esNueva, numero.getValue(), capacidad.getValue(), zona.getValue(),
                    fila.getValue(), columna.getValue());
            if (error != null) {
                ComponentesUI.error(error);
                return;
            }
            List<Mesa> mesas = ArchivoDatos.leerMesas();
            if (esNueva) {
                mesas.add(new Mesa(numero.getValue(), capacidad.getValue(), zona.getValue(),
                        fila.getValue(), columna.getValue()));
            } else {
                for (Mesa m : mesas) {
                    if (m.getNumero() == mesa.getNumero()) {
                        m.setCapacidad(capacidad.getValue());
                        m.setZona(zona.getValue());
                        m.setFila(fila.getValue());
                        m.setColumna(columna.getValue());
                    }
                }
            }
            ArchivoDatos.guardarMesas(mesas);
            dialogo.close();
            ComponentesUI.exito(esNueva ? "Mesa creada" : "Mesa actualizada");
            actualizarGrid();
        });

        Button cerrar = new Button("Cerrar", e -> dialogo.close());
        cerrar.getElement().getThemeList().add("tertiary");
        dialogo.getFooter().add(cerrar, guardar);
        dialogo.open();
    }

    private String validar(boolean esNueva, Integer numero, Integer capacidad, String zona,
                           Integer fila, Integer columna) {
        if (numero == null || numero < 1) {
            return "El número de la mesa es obligatorio y debe ser mayor que 0.";
        }
        if (capacidad == null || capacidad < 1 || capacidad > 12) {
            return "La capacidad debe estar entre 1 y 12 personas.";
        }
        if (zona == null) {
            return "Elige la zona.";
        }
        if (fila == null || fila < 0 || fila >= MAX_FILAS || columna == null || columna < 0 || columna >= MAX_COLUMNAS) {
            return "La posición debe estar dentro del plano (fila 0-" + (MAX_FILAS - 1)
                    + ", columna 0-" + (MAX_COLUMNAS - 1) + ").";
        }
        for (Mesa m : ArchivoDatos.leerMesas()) {
            if (esNueva && m.getNumero() == numero) {
                return "Ya existe una mesa con el número " + numero + ".";
            }
            if (m.getNumero() != numero && m.getFila() == fila && m.getColumna() == columna) {
                return "Esa posición ya la ocupa la mesa " + m.getNumero() + ".";
            }
        }
        return null;
    }

    /** No se borra una mesa que tiene reservas activas de hoy en adelante; si no las tiene, se pide confirmación. */
    private void intentarEliminar(Mesa mesa) {
        for (Reserva r : ArchivoDatos.leerReservas()) {
            if (r.estaActiva() && r.getNumeroMesa() == mesa.getNumero()
                    && !r.getFecha().isBefore(LocalDate.now())) {
                ComponentesUI.error("La mesa " + mesa.getNumero()
                        + " tiene reservas pendientes; cancélalas antes de eliminarla.");
                return;
            }
        }
        ConfirmDialog dialogo = new ConfirmDialog();
        dialogo.setHeader("¿Eliminar la mesa " + mesa.getNumero() + "?");
        dialogo.setText("Se quitará del plano. Esta acción no se puede deshacer.");
        dialogo.setCancelable(true);
        dialogo.setCancelText("Volver");
        dialogo.setConfirmText("Eliminar");
        dialogo.setConfirmButtonTheme("error primary");
        dialogo.addConfirmListener(ev -> {
            List<Mesa> mesas = ArchivoDatos.leerMesas();
            mesas.removeIf(m -> m.getNumero() == mesa.getNumero());
            ArchivoDatos.guardarMesas(mesas);
            ComponentesUI.exito("Mesa eliminada");
            actualizarGrid();
        });
        dialogo.open();
    }
}
