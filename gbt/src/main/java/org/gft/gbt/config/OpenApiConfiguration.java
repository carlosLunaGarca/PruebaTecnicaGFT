package org.gft.gbt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info()
                .title("BTG - Gestión de Fondos")
                .version("v1")
                .description("API para suscripción, cancelación y consulta de transacciones de fondos.")
                .contact(new Contact().name("Soporte").email("soporte@example.com")));
    }
}
