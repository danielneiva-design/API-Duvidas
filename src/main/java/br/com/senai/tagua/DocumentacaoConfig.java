package br.com.senai.tagua;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class DocumentacaoConfig {

    @Bean
    public OpenAPI documentacao() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Dúvidas")
                        .description("API para gerenciar dúvidas dos alunos")
                        .version("1.0"))
                // Como a API é protegida: uma chave no header X-API-Key
                .components(new Components()
                        .addSecuritySchemes("chave", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave ALUNO (ver e criar) ou PROFESSOR (tudo). Peça ao professor.")))
                // Todos os endpoints usam essa chave
                .addSecurityItem(new SecurityRequirement().addList("chave"));
    }
}