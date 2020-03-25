package com.server.nameserver;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
class NodeController {

    private final NodeRepository repository;

    NodeController(NodeRepository repository) {
        this.repository = repository;
    }
    

    @GetMapping("/nodes")
    List<Node> all() {
        return repository.findAll();
    }

    @PostMapping(path = "/nodes")
    Node newCostumer(@RequestBody Node nCustomer) {
        return repository.save(nCustomer); 
    }

    @GetMapping("/nodes/{id}")
    Node one(@PathVariable Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new NodeNotFoundException(id));
    }

    @PutMapping("/nodes/{id}")
    Node repCostumer(@RequestBody Node nCustomer, @PathVariable Long id) {
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

    @DeleteMapping("/nodes/{id}")
    void deleteCostumer(@PathVariable Long id) {
        repository.deleteById(id);
    }
}