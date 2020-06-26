package proj.kedabra.billsnap.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI billSnapOpenApi(final BuildProperties buildProperties) {
        final var securitySchemeName = "Bearer Authentication";
        return new OpenAPI()
                .info(new Info().title("Bill Snap Rest API")
                        .description("Bill Management System and Splitting REST API Documentation")
                        .version(buildProperties.getVersion()))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .description("This application uses a Bearer Token to authenticate the roles and access that a user may have. Please input the token.")
                                .scheme("bearer")));
    }

}
