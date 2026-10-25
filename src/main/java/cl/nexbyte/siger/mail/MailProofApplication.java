package cl.nexbyte.siger.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MailProofApplication {
    public static void main(String[] args) {
        SpringApplication.run(MailProofApplication.class, args);
    }
}
