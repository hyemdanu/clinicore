import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/capabilities")
public class CapabilitiesController {

    private final CapabilitiesRepository repo;

    public CapabilitiesController(CapabilitiesRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Capabilities> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Capabilities getOne(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Capabilities create(@RequestBody Capabilities capabilities) {
        return repo.save(capabilities);
    }

    @PutMapping("/{id}")
    public Capabilities update(@PathVariable Long id, @RequestBody Capabilities newCapabilities) {
        return repo.findById(id).map(c -> {
            c.setIsVerbal(newCapabilities.getIsVerbal());
            c.setSelfMedicines(newCapabilities.getSelfMedicines());
            c.setIncontinenceStatus(newCapabilities.getIncontinenceStatus());
            c.setMobilityCapability(newCapabilities.getMobilityCapability());
            return repo.save(c);
        }).orElseThrow();
    }
}
