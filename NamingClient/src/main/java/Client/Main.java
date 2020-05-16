package Client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.UnknownHostException;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        // Start REST application
        SpringApplication.run(Main.class, args);

        System.out.println("Gewone main runned");
    }
}
