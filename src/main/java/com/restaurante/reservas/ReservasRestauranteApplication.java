package com.restaurante.reservas;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal. Carga el tema Lumo y, encima, la identidad visual de DAIMOS
 * (src/main/resources/META-INF/resources/styles.css).
 */
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
public class ReservasRestauranteApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(ReservasRestauranteApplication.class, args);
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "images/daimos-isotipo.svg", "64x64");
        settings.addMetaTag("theme-color", "#17483E");
    }
}
