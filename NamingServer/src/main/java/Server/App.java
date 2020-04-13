package Server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class App
{
    public static void main(String[] args) {
        // Component scan and enable configuration
        SpringApplication.run(App.class, args);
    }
}
