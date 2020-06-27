package proj.kedabra.billsnap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class BillsnapApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BillsnapApplication.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();

        String url = environment.getProperty("server.address", "localhost") + ":" +
                environment.getProperty("server.port", "8080") +
                environment.getProperty("server.servlet.context-path", "");

        log.info("Server located at: http://" + url);
    }

}
