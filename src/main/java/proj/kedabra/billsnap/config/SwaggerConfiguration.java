package proj.kedabra.billsnap.config;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.models.auth.In;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.paths.DefaultPathProvider;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {

    public static final String API_KEY = "Bearer Token";

    private final String contextPath;

    @Autowired
    public SwaggerConfiguration(@Value("${server.servlet.context-path}") final String contextPath) {
        this.contextPath = contextPath;
    }

    @Bean
    public Docket api() {
        DefaultPathProvider pathProvider = new DefaultPathProvider() {
            @Override
            public String getOperationPath(String operationPath) {
                if (operationPath.startsWith(contextPath)) {
                    operationPath = operationPath.substring(contextPath.length());
                }
                return Paths.removeAdjacentForwardSlashes(UriComponentsBuilder.newInstance().replacePath(operationPath)
                        .build().toString());
            }
        };

        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(List.of(apiKey()))
                .securityContexts(List.of(securityContext()))
                .useDefaultResponseMessages(false)
                .produces(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .consumes(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .select().apis(RequestHandlerSelectors.basePackage("proj.kedabra.billsnap.presentation"))
                .paths(PathSelectors.any())
                .build().apiInfo(apiEndPointsInfo())
                .pathProvider(pathProvider);
    }


    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Bill Snap Rest API")
                .description("Bill Management System and Splitting REST API Documentation")
                .version("1.0.0")
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey(API_KEY, HttpHeaders.AUTHORIZATION, In.HEADER.name());
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(List.of(securityReference()))
                .build();
    }

    private SecurityReference securityReference() {
        return SecurityReference.builder()
                .reference("JWT")
                .scopes(new AuthorizationScope[0])
                .build();
    }
}
