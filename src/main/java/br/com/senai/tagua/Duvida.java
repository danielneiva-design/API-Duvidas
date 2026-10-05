package br.com.senai.tagua;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Uma dúvida registrada pela turma.")
@Entity
public class Duvida {

    @Schema(description = "Número de protocolo, criado pela API. É único, mas não sequencial.",
            example = "30001",
            accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "O texto da dúvida.",
            example = "Qual a diferença entre 401 e 403?")
    @NotBlank(message = "A mensagem da dúvida é obrigatória.")
    @Size(max = 2000, message = "A mensagem pode ter no máximo 2000 caracteres.")
    @Column(length = 2000, nullable = false)
    private String mensagem;

    @Schema(description = "Quando a dúvida foi feita, no horário de Brasília (sem fuso horário). "
            + "Opcional ao criar: se não vier, a API usa o momento atual. Não muda ao editar.",
            example = "2026-10-02T09:30:00")
    private LocalDateTime datahora;

    @PrePersist
    private void definirDataHora() {
        if (this.datahora == null) {
            this.datahora = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDatahora() {
        return datahora;
    }

    public void setDatahora(LocalDateTime datahora) {
        this.datahora = datahora;
    }
}