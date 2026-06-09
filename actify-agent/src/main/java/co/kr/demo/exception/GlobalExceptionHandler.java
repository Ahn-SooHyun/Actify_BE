package co.kr.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import co.kr.demo.util.BaseResponse;
import org.springframework.http.ResponseEntity;

/** 예외를 일관된 HTTP 응답(RFC 7807 ProblemDetail)으로 변환한다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ProblemDetail handleNotFound(ScheduleNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ProblemDetail handleConflict(ScheduleConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** 조율 실패(가능한 슬롯 없음 등) → 422 Unprocessable Entity. */
    @ExceptionHandler(CoordinationFailedException.class)
    public ResponseEntity<BaseResponse<Void>> handleCoordinationFailed(
            CoordinationFailedException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(BaseResponse.fail(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("잘못된 요청입니다");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}