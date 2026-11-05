package com.restaurante.reservas.data;

import com.restaurante.reservas.model.Mesa;
import com.restaurante.reservas.model.Reserva;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee y escribe los archivos de texto de la aplicación (carpeta "datos").
 * Cada tipo de dato tiene su propio .txt con los campos separados por |.
 * La primera vez que corre la aplicación se crean datos de ejemplo.
 */
public class ArchivoDatos {

    private static final Path CARPETA = Paths.get("datos");
    private static final Path ARCHIVO_MESAS = CARPETA.resolve("mesas.txt");
    private static final Path ARCHIVO_RESERVAS = CARPETA.resolve("reservas.txt");

    /** Número de reservas activas a partir del cual un cliente es frecuente. */
    public static final int RESERVAS_CLIENTE_FRECUENTE = 3;

    private ArchivoDatos() {
    }

    // ------------------------------------------------------------------
    // Lectura y escritura genérica de líneas
    // ------------------------------------------------------------------

    /** Lee todas las líneas no vacías de un archivo. Si no existe, devuelve una lista vacía. */
    private static List<String> leerLineas(Path archivo) {
        try {
            if (!Files.exists(archivo)) {
                return new ArrayList<>();
            }
            List<String> lineas = new ArrayList<>();
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (!linea.isBlank()) {
                    lineas.add(linea);
                }
            }
            return lineas;
        } catch (IOException e) {
            System.err.println("No se pudo leer " + archivo + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void escribirLineas(Path archivo, List<String> lineas) {
        try {
            Files.createDirectories(CARPETA);
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("No se pudo escribir " + archivo + ": " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Mesas
    // ------------------------------------------------------------------

    public static List<Mesa> leerMesas() {
        crearDatosDeEjemploSiFaltan();
        List<Mesa> mesas = new ArrayList<>();
        for (String linea : leerLineas(ARCHIVO_MESAS)) {
            try {
                mesas.add(Mesa.desdeLinea(linea));
            } catch (Exception e) {
                // Una línea mal escrita no tumba la aplicación: se ignora y se avisa en consola
                System.err.println("Línea de mesa inválida, se ignora: " + linea);
            }
        }
        return mesas;
    }

    public static void guardarMesas(List<Mesa> mesas) {
        List<String> lineas = new ArrayList<>();
        for (Mesa m : mesas) {
            lineas.add(m.aLinea());
        }
        escribirLineas(ARCHIVO_MESAS, lineas);
    }

    public static Mesa buscarMesa(int numero) {
        for (Mesa m : leerMesas()) {
            if (m.getNumero() == numero) {
                return m;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Reservas
    // ------------------------------------------------------------------

    public static List<Reserva> leerReservas() {
        crearDatosDeEjemploSiFaltan();
        List<Reserva> reservas = new ArrayList<>();
        for (String linea : leerLineas(ARCHIVO_RESERVAS)) {
            try {
                reservas.add(Reserva.desdeLinea(linea));
            } catch (Exception e) {
                System.err.println("Línea de reserva inválida, se ignora: " + linea);
            }
        }
        return reservas;
    }

    public static void guardarReservas(List<Reserva> reservas) {
        List<String> lineas = new ArrayList<>();
        for (Reserva r : reservas) {
            lineas.add(r.aLinea());
        }
        escribirLineas(ARCHIVO_RESERVAS, lineas);
    }

    /** Siguiente id disponible para una reserva nueva. */
    public static int siguienteIdReserva() {
        int mayor = 0;
        for (Reserva r : leerReservas()) {
            mayor = Math.max(mayor, r.getId());
        }
        return mayor + 1;
    }

    /** Reservas activas (no canceladas) de una fecha. */
    public static List<Reserva> reservasDelDia(LocalDate fecha) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : leerReservas()) {
            if (r.estaActiva() && r.getFecha().equals(fecha)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    /**
     * Indica si una mesa ya tiene una reserva activa a esa fecha y hora.
     * idIgnorar sirve para no chocar con la misma reserva cuando se está editando (usar 0 si es nueva).
     */
    public static boolean mesaOcupada(int numeroMesa, LocalDate fecha, LocalTime hora, int idIgnorar) {
        for (Reserva r : leerReservas()) {
            if (r.getId() != idIgnorar && r.estaActiva()
                    && r.getNumeroMesa() == numeroMesa
                    && r.getFecha().equals(fecha)
                    && r.getHora().equals(hora)) {
                return true;
            }
        }
        return false;
    }

    /** Un cliente (identificado por su teléfono) es frecuente si tiene 3 o más reservas no canceladas. */
    public static boolean esClienteFrecuente(String telefono) {
        int cantidad = 0;
        for (Reserva r : leerReservas()) {
            if (r.estaActiva() && r.getTelefono().equals(telefono)) {
                cantidad++;
            }
        }
        return cantidad >= RESERVAS_CLIENTE_FRECUENTE;
    }

    // ------------------------------------------------------------------
    // Datos de ejemplo
    // ------------------------------------------------------------------

    private static void crearDatosDeEjemploSiFaltan() {
        if (!Files.exists(ARCHIVO_MESAS)) {
            List<Mesa> mesas = new ArrayList<>();
            // Salón: filas 0 a 2
            mesas.add(new Mesa(1, 2, "Salón", 0, 0));
            mesas.add(new Mesa(2, 2, "Salón", 0, 2));
            mesas.add(new Mesa(3, 4, "Salón", 0, 4));
            mesas.add(new Mesa(4, 4, "Salón", 1, 1));
            mesas.add(new Mesa(5, 6, "Salón", 1, 3));
            mesas.add(new Mesa(6, 4, "Salón", 2, 0));
            mesas.add(new Mesa(7, 8, "Salón", 2, 4));
            // Terraza: filas 4 y 5
            mesas.add(new Mesa(8, 2, "Terraza", 4, 0));
            mesas.add(new Mesa(9, 4, "Terraza", 4, 2));
            mesas.add(new Mesa(10, 4, "Terraza", 4, 4));
            mesas.add(new Mesa(11, 6, "Terraza", 5, 1));
            mesas.add(new Mesa(12, 2, "Terraza", 5, 3));
            guardarMesas(mesas);
        }
        if (!Files.exists(ARCHIVO_RESERVAS)) {
            LocalDate hoy = LocalDate.now();
            List<Reserva> reservas = new ArrayList<>();
            reservas.add(new Reserva(1, "Laura Gómez", "3001234567", hoy, LocalTime.of(13, 0), 2, 1, Reserva.CONFIRMADA));
            reservas.add(new Reserva(2, "Carlos Ruiz", "3109876543", hoy, LocalTime.of(13, 0), 4, 4, Reserva.PENDIENTE));
            reservas.add(new Reserva(3, "Laura Gómez", "3001234567", hoy.plusDays(1), LocalTime.of(19, 0), 2, 8, Reserva.PENDIENTE));
            reservas.add(new Reserva(4, "Laura Gómez", "3001234567", hoy.plusDays(7), LocalTime.of(20, 0), 3, 9, Reserva.PENDIENTE));
            reservas.add(new Reserva(5, "Andrés Pérez", "3155551234", hoy, LocalTime.of(19, 30), 6, 5, Reserva.CONFIRMADA));
            reservas.add(new Reserva(6, "María Torres", "3204445566", hoy, LocalTime.of(20, 0), 7, 7, Reserva.PENDIENTE));
            reservas.add(new Reserva(7, "Julián Díaz", "3017778899", hoy.plusDays(2), LocalTime.of(12, 30), 2, 2, Reserva.CANCELADA));
            guardarReservas(reservas);
        }
    }
}
