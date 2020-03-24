package com;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankController {

    private static AtomicInteger counter = new AtomicInteger();

    @GetMapping("/bank")
    public Bank getBank(@RequestParam(value = "name", defaultValue = "N/A") String name) {
        return new Bank(name, counter.incrementAndGet());
    }
}
