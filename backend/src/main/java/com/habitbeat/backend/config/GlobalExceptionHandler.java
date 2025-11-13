package com.habitbeat.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	private Map<String, Object> buildBody(HttpStatus status, String message, HttpServletRequest request) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		if (request != null) {
			body.put("path", request.getRequestURI());
		}
		return body;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = "Invalid path or query parameter: '" + ex.getName() + "' must be of type " + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected type");
		return ResponseEntity.status(status).body(buildBody(status, message, request));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = "Malformed JSON request body";
		return ResponseEntity.status(status).body(buildBody(status, message, request));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.orElse("Validation failed");
		return ResponseEntity.status(status).body(buildBody(status, message, request));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = ex.getConstraintViolations().stream()
				.findFirst()
				.map(v -> v.getPropertyPath() + ": " + v.getMessage())
				.orElse("Constraint violation");
		return ResponseEntity.status(status).body(buildBody(status, message, request));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(buildBody(status, ex.getMessage(), request));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
		HttpStatus status;
		String message = ex.getMessage();
		
		// Map auth-specific errors to appropriate HTTP status codes
		if (message != null) {
			if (message.contains("Invalid credentials") || 
				message.contains("Invalid refresh token") ||
				message.contains("Invalid session") ||
				message.contains("No refresh token found")) {
				status = HttpStatus.UNAUTHORIZED;
			} else if (message.contains("Email not verified")) {
				status = HttpStatus.FORBIDDEN;
				message = "Please verify your email before logging in";
			} else if (message.contains("Invalid or expired token")) {
				status = HttpStatus.BAD_REQUEST;
				message = "Token is invalid or has expired";
			} else if (message.contains("User not found")) {
				status = HttpStatus.NOT_FOUND;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				message = "An unexpected error occurred";
			}
		} else {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			message = "An unexpected error occurred";
		}
		
		return ResponseEntity.status(status).body(buildBody(status, message, request));
	}
}
