package Server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        // Start running REST server
        SpringApplication.run(App.class, args);
    }
}