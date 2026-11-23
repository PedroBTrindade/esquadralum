package com.controle.insumos.exception;

import com.esquadralum.insumos.dto.Dtos.ErroResponse;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratadorDeErros {

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String msg) {
        return ResponseEntity.status(status).body(new ErroResponse(msg));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(NaoEncontradoException ex) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regra(RegraNegocioException ex) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, "Dados inválidos. " + msg);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> conflito(OptimisticLockingFailureException ex) {
        return erro(HttpStatus.CONFLICT,
                "Esta embalagem foi alterada por outra pessoa agora há pouco. Recarregue e tente novamente.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return erro(HttpStatus.CONFLICT, "A operação viola uma regra de integridade dos dados (ex.: código duplicado).");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido (verifique formatos de data e números).");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametro(MethodArgumentTypeMismatchException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName());
    }
}
