package com.restaurante.reservas.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Una reserva de una mesa.
 * Se guarda en datos/reservas.txt con el formato:
 * id|cliente|telefono|fecha|hora|personas|numeroMesa|estado
 */
public class Reserva {

    public static final String PENDIENTE = "Pendiente";
    public static final String CONFIRMADA = "Confirmada";
    public static final String CANCELADA = "Cancelada";

    /** Horario del restaurante: las reservas se toman cada 30 minutos entre estas horas. */
    public static final LocalTime HORA_APERTURA = LocalTime.of(12, 0);
    public static final LocalTime HORA_ULTIMA_RESERVA = LocalTime.of(21, 0);

    private int id;
    private String cliente;
    private String telefono;
    private LocalDate fecha;
    private LocalTime hora;
    private int personas;
    private int numeroMesa;
    private String estado = PENDIENTE;

    public Reserva() {
    }

    public Reserva(int id, String cliente, String telefono, LocalDate fecha, LocalTime hora,
                   int personas, int numeroMesa, String estado) {
        this.id = id;
        this.cliente = cliente;
        this.telefono = telefono;
        this.fecha = fecha;
        this.hora = hora;
        this.personas = personas;
        this.numeroMesa = numeroMesa;
        this.estado = estado;
    }

    /** Una reserva cancelada ya no ocupa la mesa. */
    public boolean estaActiva() {
        return !CANCELADA.equals(estado);
    }

    public String aLinea() {
        return id + "|" + cliente + "|" + telefono + "|" + fecha + "|" + hora + "|"
                + personas + "|" + numeroMesa + "|" + estado;
    }

    public static Reserva desdeLinea(String linea) {
        String[] p = linea.split("\\|");
        return new Reserva(
                Integer.parseInt(p[0].trim()),
                p[1].trim(),
                p[2].trim(),
                LocalDate.parse(p[3].trim()),
                LocalTime.parse(p[4].trim()),
                Integer.parseInt(p[5].trim()),
                Integer.parseInt(p[6].trim()),
                p[7].trim());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public int getPersonas() { return personas; }
    public void setPersonas(int personas) { this.personas = personas; }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
