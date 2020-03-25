package com.university.bank;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
class CustomerController {

    private final CustomerRepository repository;

    CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }
    

    @GetMapping("/customers")
    List<Customer> all() {
        return repository.findAll();
    }

    @PostMapping(path = "/customers")
    Customer newCostumer(@RequestBody Customer nCustomer) {
        return repository.save(nCustomer); 
    }

    @GetMapping("/customers/{id}")
    Customer one(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @PutMapping("/customers/{id}")
    Customer repCostumer(@RequestBody Customer nCustomer, @PathVariable Long id) {
        return repository.findById(id)
            .map(costumer -> {
                costumer.setName(nCustomer.getName());
                costumer.setValue(nCustomer.getValue());
                return repository.save(costumer);
            })
            .orElseGet(() -> {
                nCustomer.setId(id);
                return repository.save(nCustomer);
            });
    }

    @DeleteMapping("/customers/{id}")
    void deleteCostumer(@PathVariable Long id) {
        repository.deleteById(id);
    }
}