package br.com.senai.tagua;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class DuvidaController {

    private final DuvidaRepository repository;

    public DuvidaController(DuvidaRepository repository) {
        this.repository = repository;
    }

    // Alô Mundo
    @GetMapping("/alo")
    public String alo() {
        return "Alô Mundo, Spring Boot!";
    }

    @GetMapping("/saude")
    public Map<String, Object> saude() {
        return Map.of("status", "ok", "duvidas", repository.count());
    }

    // CREATE
    @PostMapping("/duvidas")
    public Duvida criar(@Valid @RequestBody Duvida duvida) {
        duvida.setId(null); // garante que será um INSERT
        return repository.save(duvida);
    }

    // READ (todas)
    @GetMapping("/duvidas")
    public List<Duvida> listar() {
        return repository.findAll();
    }

    // READ (uma)
    @GetMapping("/duvidas/{id}")
    public ResponseEntity<Duvida> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/duvidas/{id}")
    public ResponseEntity<Duvida> atualizar(@PathVariable Long id, @Valid @RequestBody Duvida dados) {
        return repository.findById(id)
                .map(duvida -> {
                    duvida.setMensagem(dados.getMensagem());
                    return ResponseEntity.ok(repository.save(duvida));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/duvidas/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
