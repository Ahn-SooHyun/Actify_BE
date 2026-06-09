package co.kr.demo.exception;

/** 비즈니스 규칙 위반(시간 충돌, 시작>=종료 등). 400/409로 변환된다. */
public class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}