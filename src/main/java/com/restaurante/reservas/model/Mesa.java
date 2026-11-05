package com.restaurante.reservas.model;

/**
 * Una mesa del restaurante.
 * Se guarda en datos/mesas.txt con el formato: numero|capacidad|zona|fila|columna
 */
public class Mesa {

    private int numero;
    private int capacidad;
    private String zona;   // "Salón" o "Terraza"
    private int fila;      // posición en el plano (empieza en 0)
    private int columna;   // posición en el plano (empieza en 0)

    public Mesa() {
    }

    public Mesa(int numero, int capacidad, String zona, int fila, int columna) {
        this.numero = numero;
        this.capacidad = capacidad;
        this.zona = zona;
        this.fila = fila;
        this.columna = columna;
    }

    /** Convierte la mesa en una línea de texto para el archivo. */
    public String aLinea() {
        return numero + "|" + capacidad + "|" + zona + "|" + fila + "|" + columna;
    }

    /** Crea una mesa a partir de una línea del archivo. Lanza excepción si la línea está mal escrita. */
    public static Mesa desdeLinea(String linea) {
        String[] partes = linea.split("\\|");
        return new Mesa(
                Integer.parseInt(partes[0].trim()),
                Integer.parseInt(partes[1].trim()),
                partes[2].trim(),
                Integer.parseInt(partes[3].trim()),
                Integer.parseInt(partes[4].trim()));
    }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public int getFila() { return fila; }
    public void setFila(int fila) { this.fila = fila; }

    public int getColumna() { return columna; }
    public void setColumna(int columna) { this.columna = columna; }

    @Override
    public String toString() {
        return "Mesa " + numero + " (" + capacidad + " pers., " + zona + ")";
    }
}
