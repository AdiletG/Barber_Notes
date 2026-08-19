package kg.barbernotes.barbernotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BarberNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarberNotesApplication.class, args);
    }

}
