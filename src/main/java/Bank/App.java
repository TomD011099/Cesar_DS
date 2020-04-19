package Bank;
import Bank.Controllers.BankRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.Collections;

@SpringBootApplication
public class App
{
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(App.class);
        // specify a specifik port to use
        app.setDefaultProperties(Collections.singletonMap("server.port", "8083"));
        app.run(args);
        //SpringApplication.run(App.class, args);
    }
}

