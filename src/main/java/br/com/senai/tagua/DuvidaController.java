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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Dúvidas", description = "Registrar, consultar, editar e apagar as dúvidas da turma")
@RestController
public class DuvidaController {

    private final DuvidaRepository repository;

    public DuvidaController(DuvidaRepository repository) {
        this.repository = repository;
    }

    @Operation(
            summary = "Diz se a API está ligada",
            description = "Não precisa de chave. Responde sem consultar o banco: "
                    + "para conferir também o banco, use GET /saude.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A API está ligada.")
    })
    @GetMapping("/alo")
    public String alo() {
        return "Alô Mundo, Spring Boot!";
    }

    @Operation(
            summary = "Diz se a API e o banco estão funcionando",
            description = "Não precisa de chave. Consulta o banco e devolve quantas dúvidas existem. "
                    + "Se o banco estiver fora do ar, a resposta é um erro. "
                    + "É o endereço usado pelo despertador e pelo health check do Render.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A API e o banco estão funcionando."),
            @ApiResponse(responseCode = "500", description = "A API está ligada, mas não conseguiu falar com o banco.")
    })
    @GetMapping("/saude")
    public Map<String, Object> saude() {
        return Map.of("status", "ok", "duvidas", repository.count());
    }

    @Operation(
            summary = "Registra uma dúvida nova",
            description = "Chave ALUNO ou PROFESSOR. A mensagem é obrigatória (até 2000 caracteres). "
                    + "A datahora é opcional: se não vier, a API usa o horário de Brasília do momento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registrada. A resposta traz a dúvida com id e datahora."),
            @ApiResponse(responseCode = "400", description = "Mensagem vazia ou com mais de 2000 caracteres. "
                    + "A lista \"erros\" da resposta explica o motivo."),
            @ApiResponse(responseCode = "401", description = "Faltou a chave, ou ela está errada.")
    })
    @PostMapping("/duvidas")
    public Duvida criar(@Valid @RequestBody Duvida duvida) {
        duvida.setId(null); // garante que será um INSERT
        return repository.save(duvida);
    }

    @Operation(
            summary = "Lista todas as dúvidas",
            description = "Chave ALUNO ou PROFESSOR. Devolve uma lista (array) com todas as dúvidas registradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A lista de dúvidas (pode vir vazia: [])."),
            @ApiResponse(responseCode = "401", description = "Faltou a chave, ou ela está errada.")
    })
    @GetMapping("/duvidas")
    public List<Duvida> listar() {
        return repository.findAll();
    }

    @Operation(
            summary = "Busca uma dúvida pelo id",
            description = "Chave ALUNO ou PROFESSOR. Se não existir dúvida com esse id, responde 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A dúvida encontrada."),
            @ApiResponse(responseCode = "401", description = "Faltou a chave, ou ela está errada."),
            @ApiResponse(responseCode = "404", description = "Não existe dúvida com esse id.")
    })
    @GetMapping("/duvidas/{id}")
    public ResponseEntity<Duvida> buscar(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Edita a mensagem de uma dúvida",
            description = "Só a chave PROFESSOR (a ALUNO recebe 403). Muda apenas a mensagem: "
                    + "o id e a datahora continuam os mesmos. Se o id não existir, responde 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Editada. A resposta traz a dúvida atualizada."),
            @ApiResponse(responseCode = "400", description = "Mensagem vazia ou com mais de 2000 caracteres. "
                    + "A lista \"erros\" da resposta explica o motivo."),
            @ApiResponse(responseCode = "401", description = "Faltou a chave, ou ela está errada."),
            @ApiResponse(responseCode = "403", description = "A chave ALUNO não pode editar."),
            @ApiResponse(responseCode = "404", description = "Não existe dúvida com esse id.")
    })
    @PutMapping("/duvidas/{id}")
    public ResponseEntity<Duvida> atualizar(@PathVariable Long id, @Valid @RequestBody Duvida dados) {
        return repository.findById(id)
                .map(duvida -> {
                    duvida.setMensagem(dados.getMensagem());
                    return ResponseEntity.ok(repository.save(duvida));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Apaga uma dúvida",
            description = "Só a chave PROFESSOR (a ALUNO recebe 403). Quando dá certo, responde 204, sem corpo. "
                    + "Se o id não existir, responde 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Apagada. A resposta vem sem corpo."),
            @ApiResponse(responseCode = "401", description = "Faltou a chave, ou ela está errada."),
            @ApiResponse(responseCode = "403", description = "A chave ALUNO não pode apagar."),
            @ApiResponse(responseCode = "404", description = "Não existe dúvida com esse id.")
    })
    @DeleteMapping("/duvidas/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
