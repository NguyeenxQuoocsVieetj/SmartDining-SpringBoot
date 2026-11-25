package org.springboot.smartdining.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Đổi mọi lỗi thành ProblemDetail có thêm trường "code" ổn định.
 * Không trả SQL, tên ràng buộc hay stack trace cho client.
 * Các lỗi chuẩn của Spring MVC (404, 405, 415...) do lớp cha xử lý.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /** MariaDB ER_CHECKREAD: "Record has changed since last read". */
    private static final int MARIADB_RECORD_CHANGED = 1020;

    private static final String CODE = "code";

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(BadRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(TableSessionInvalidException.class)
    public ResponseEntity<ProblemDetail> handleTableSession(TableSessionInvalidException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "TABLE_SESSION_INVALID", ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ProblemDetail> handleTooManyRequests(TooManyRequestsException ex) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "Dữ liệu bị trùng, đang được nơi khác dùng hoặc vi phạm ràng buộc");
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ProblemDetail> handleConcurrency(ConcurrencyFailureException ex) {
        return concurrentModification();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        if (isRecordChanged(ex)) {
            return concurrentModification();
        }
        logger.error("Lỗi chưa được xử lý", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Lỗi máy chủ");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Không đưa giá trị bị từ chối vào phản hồi
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", String.valueOf(error.getDefaultMessage())))
                .toList();
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, "Dữ liệu gửi lên không hợp lệ");
        body.setProperty(CODE, "VALIDATION_FAILED");
        body.setProperty("errors", errors);
        return createResponseEntity(body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status,
                "Không đọc được nội dung: JSON sai, sai kiểu dữ liệu hoặc giá trị enum không tồn tại");
        body.setProperty(CODE, "MALFORMED_REQUEST");
        return createResponseEntity(body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, "Tham số trên đường dẫn sai kiểu");
        body.setProperty(CODE, "TYPE_MISMATCH");
        return createResponseEntity(body, headers, status, request);
    }

    /** Lỗi chuẩn chưa có "code" thì lấy tên mã HTTP, ví dụ METHOD_NOT_ALLOWED. */
    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problem
                && (problem.getProperties() == null || !problem.getProperties().containsKey(CODE))) {
            HttpStatus status = HttpStatus.resolve(statusCode.value());
            problem.setProperty(CODE, status != null ? status.name() : "HTTP_" + statusCode.value());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private static ResponseEntity<ProblemDetail> concurrentModification() {
        return problem(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "Dữ liệu vừa được người khác thay đổi, hãy tải lại rồi thử lại");
    }

    private static boolean isRecordChanged(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getErrorCode() == MARIADB_RECORD_CHANGED) {
                return true;
            }
        }
        return false;
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setProperty(CODE, code);
        return ResponseEntity.status(status).body(body);
    }
}
