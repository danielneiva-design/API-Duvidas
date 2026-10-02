package br.com.senai.tagua;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class TratadorDeErros {

    // Quando o @Valid encontra uma regra quebrada, cai aqui
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validacao(MethodArgumentNotValidException erro) {
        List<String> erros = new ArrayList<>();

        // ✍️ VOCÊ: um for que percorre erro.getBindingResult().getFieldErrors()
        //          e, pra cada FieldError, adiciona o getDefaultMessage() na lista erros
        for (FieldError fieldError : erro.getBindingResult().getFieldErrors()) {
            erros.add(fieldError.getDefaultMessage());
        }

        return Map.of("status", 400, "erros", erros);
    }
}