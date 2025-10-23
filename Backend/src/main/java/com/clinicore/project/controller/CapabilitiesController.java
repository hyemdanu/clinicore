//package com.clinicore.project.controller;
//
//import com.clinicore.project.entity.Capabilities;
//import com.clinicore.project.exception.CapabilitiesNotFoundException;
//import com.clinicore.project.repository.CapabilitiesRepository;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/capabilities")
//public class CapabilitiesController {
//
//    private final CapabilitiesRepository repo;
//
//    public CapabilitiesController(CapabilitiesRepository repo) {
//        this.repo = repo;
//    }
//
//    // GET all capabilities
//    @GetMapping
//    public List<Capabilities> getAll() {
//        return repo.findAll();
//    }
//
//    // GET by ID
//    @GetMapping("/{id}")
//    public Capabilities getOne(@PathVariable Long id) {
//        return repo.findById(id).orElseThrow(() -> new CapabilitiesNotFoundException(id));
//    }
//
//    // CREATE new record
//    @PostMapping
//    public Capabilities create(@RequestBody Capabilities capabilities) {
//        return repo.save(capabilities);
//    }
//
//    // UPDATE existing record
//    @PutMapping("/{id}")
//    public Capabilities update(@PathVariable Long id, @RequestBody Capabilities newCapabilities) {
//        return repo.findById(id).map(c -> {
//            c.setIsVerbal(newCapabilities.getIsVerbal());
//            c.setSelfMedicines(newCapabilities.getSelfMedicines());
//            c.setIncontinenceStatus(newCapabilities.getIncontinenceStatus());
//            c.setMobilityCapability(newCapabilities.getMobilityCapability());
//            return repo.save(c);
//        }).orElseThrow(() -> new CapabilitiesNotFoundException(id));
//    }
//
//    // DELETE record
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Long id) {
//        repo.deleteById(id);
//    }
//}
