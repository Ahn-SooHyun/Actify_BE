package co.kr.demo.exception;

/** 일정을 찾을 수 없을 때. 404로 변환된다. */
public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(Long id) {
        super("일정을 찾을 수 없습니다: id=" + id);
    }
}