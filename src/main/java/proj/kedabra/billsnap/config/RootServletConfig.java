package proj.kedabra.billsnap.config;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration allows us to redirect the root directly to our context path by creating a second servlet solely
 * dedicated to redirecting. While normally this would not be recommended, for our use case of using 1 servlet,
 * this will be fine and will simplify our workflow process
 */
@Configuration
public class RootServletConfig {

    @Bean
    public TomcatServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory() {

            @Override
            protected void prepareContext(final Host host, final ServletContextInitializer[] initializers) {
                super.prepareContext(host, initializers);
                StandardContext child = new StandardContext();
                child.addLifecycleListener(new Tomcat.FixContextListener());
                child.setPath("");
                ServletContainerInitializer initializer = getServletContextInitializer(getContextPath());
                child.addServletContainerInitializer(initializer, Collections.emptySet());
                child.setCrossContext(true);
                host.addChild(child);
            }
        };
    }

    private ServletContainerInitializer getServletContextInitializer(final String contextPath) {
        return (c, context) -> {
            final Servlet servlet = new HttpServlet() {

                private static final long serialVersionUID = -2165415721331976965L;

                @Override
                protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                    resp.sendRedirect(contextPath);
                }
            };
            context.addServlet("root", servlet).addMapping("/*");
        };
    }
}