package com.aiwork.helper.exception;

import com.aiwork.helper.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), sanitizeForLog(e.getMessage()));
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation exception: fieldErrorCount={}", e.getBindingResult().getFieldErrorCount());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail(errorMsg);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBindException(BindException e) {
        log.warn("Bind exception: fieldErrorCount={}", e.getFieldErrorCount());
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail(errorMsg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument exception: {}", sanitizeForLog(e.getMessage()));
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", sanitizeForLog(e.getMessage()));
        return Result.fail(403, "Access denied");
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Bad credentials");
        return Result.fail("Bad credentials");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Exception e) {
        log.error("Unexpected exception: type={}, message={}", e.getClass().getName(), sanitizeForLog(e.getMessage()));
        return Result.fail(500, "System error");
    }

    private String sanitizeForLog(String message) {
        if (message == null) {
            return "";
        }
        String sanitized = message
                .replaceAll("(?i)[a-z][a-z0-9+.-]*://[^\\s,;]+", "<url-redacted>")
                .replaceAll("(?i)(password|passwd|pwd)\\s*[=:]\\s*[^\\s,;]+", "$1=<redacted>")
                .replaceAll("(?i)(token|secret|api[-_]?key)\\s*[=:]\\s*[^\\s,;]+", "$1=<redacted>")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+", "Bearer <redacted>");
        if (sanitized.length() > 256) {
            return sanitized.substring(0, 256) + "...";
        }
        return sanitized;
    }
}
