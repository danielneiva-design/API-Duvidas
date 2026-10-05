package br.com.senai.tagua;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class PorteiroFilter extends OncePerRequestFilter {

    private final String chaveAluno;
    private final String chaveProfessor;

    public PorteiroFilter(@Value("${app.chave.aluno}") String chaveAluno,
            @Value("${app.chave.professor}") String chaveProfessor) {
        this.chaveAluno = chaveAluno;
        this.chaveProfessor = chaveProfessor;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse resposta, FilterChain corrente)
            throws ServletException, IOException {

        String metodo = pedido.getMethod();

         // Liberados sem chave:
        // - OPTIONS: a "pergunta prévia" do CORS
        // - GET /alo e GET /saude: usados pra ver se a API e o banco estão vivos
        String endereco = pedido.getRequestURI();
        boolean livre = metodo.equals("GET") && (endereco.equals("/alo") || endereco.equals("/saude"));

        if (metodo.equals("OPTIONS") || livre) {
            corrente.doFilter(pedido, resposta);
            return;
        }

        String chave = pedido.getHeader("X-API-Key");
        boolean ehProfessor = mesmaChave(chave, chaveProfessor);
        boolean ehAluno = mesmaChave(chave, chaveAluno);

        // ✍️ VOCÊ — Regra 1: se NÃO é professor E NÃO é aluno → responde 401 e para
        // aqui

        if (!ehProfessor && !ehAluno) {
            resposta.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ✍️ VOCÊ — Regra 2: se é aluno E o método é PUT ou DELETE → responde 403 e
        // para aqui

        if (ehAluno && (metodo.equals("PUT") || metodo.equals("DELETE"))) {
            resposta.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Passou pelas regras: deixa o pedido seguir até o DuvidaController
        corrente.doFilter(pedido, resposta);
    }

    // Compara as chaves de um jeito seguro (explicação abaixo)
    private boolean mesmaChave(String recebida, String correta) {
        if (recebida == null) {
            return false;
        }
        return MessageDigest.isEqual(
                recebida.getBytes(StandardCharsets.UTF_8),
                correta.getBytes(StandardCharsets.UTF_8));
    }
}