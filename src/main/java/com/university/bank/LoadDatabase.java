package com.university.bank;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
@Slf4j
class LoadDatabase {
    
    @Bean
    CommandLineRunner initDatabase(final CustomerRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Customer("Cedric Bammens", 500)));
        };
    }
}