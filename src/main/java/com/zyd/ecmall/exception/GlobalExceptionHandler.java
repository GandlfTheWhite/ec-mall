package com.zyd.ecmall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e) {
        List<Map<String, String>> errors =
                e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> Map.of(
                                "field", error.getField(),
                                "message", error.getDefaultMessage()
                        ))
                        .toList();
        Map<String, Object> response = Map.of(
                "status", 400,
                "message", "入力内容が正しくありません",
                "errors", errors
        );
        return ResponseEntity.badRequest().body(response);
    }
    // 検索会員不存在
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMemberNotFoundException(
            MemberNotFoundException e) {
        Map<String, Object> response = Map.of(
                "status", 404,
                "message", e.getMessage()
        );
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
    //メール重複
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmailException(
            DuplicateEmailException e) {
        Map<String, Object> response = Map.of(
                "status", 409,
                "message", e.getMessage()
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    //メールまたはパスワード違います
    @ExceptionHandler(LoginFailedException .class)
    public ResponseEntity<Map<String, Object>> handleLoginFailedException (
            LoginFailedException  e) {
        Map<String, Object> response = Map.of(
                "status", 401,
                "message", e.getMessage()
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


}
