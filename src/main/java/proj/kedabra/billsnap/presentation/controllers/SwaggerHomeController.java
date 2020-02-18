package proj.kedabra.billsnap.presentation.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller reroutes all root requests from the context path directly to our swagger UI API documentation page
 * so we could access directly from {domain}:{port}/{contextPath}
 */
@RestController
public class SwaggerHomeController {

    private final String contextPath;

    public SwaggerHomeController(final Environment env) {
        contextPath = env.getProperty("server.servlet.context-path", "");
    }

    @GetMapping("/")
    public void redirectToSwagger(final HttpServletResponse response) throws IOException {
        response.sendRedirect(contextPath + "/swagger-ui.html");
    }
}
