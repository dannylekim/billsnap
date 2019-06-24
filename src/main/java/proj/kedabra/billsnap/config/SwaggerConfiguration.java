package proj.kedabra.billsnap.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .produces(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .consumes(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .select().apis(RequestHandlerSelectors.basePackage("proj.kedabra.billsnap.presentation"))
                .paths(PathSelectors.any())
                .build().apiInfo(apiEndPointsInfo());
    }


    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Bill Snap Rest API")
                .description("Bill Management System and Splitting REST API Documentation")
                .version("1.0.0")
                .build();
    }
}
