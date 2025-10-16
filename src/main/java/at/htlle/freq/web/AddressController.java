// src/main/java/at/htlle/freq/web/AddressController.java
package at.htlle.freq.web;

import at.htlle.freq.application.AddressService;
import at.htlle.freq.domain.Address;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    // ---------- READ ----------

    @GetMapping
    public List<Address> list() {
        return service.getAllAddresses();
    }

    @GetMapping("/{id}")
    public Address byId(@PathVariable UUID id) {
        return service.getAddressById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    // ---------- WRITE ----------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Address create(@RequestBody Address payload) {
        try {
            return service.createAddress(payload);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Address update(@PathVariable UUID id, @RequestBody Address patch) {
        Optional<Address> updated = service.updateAddress(id, patch);
        return updated.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }
}
