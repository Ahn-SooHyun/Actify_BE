package co.kr.demo.exception;

/**
 * 조율 실패 시 발생.
 *
 * 예: 두 사용자의 일정이 너무 겹쳐서 검색 기간 안에 양쪽 모두
 *     비어있는 슬롯을 찾을 수 없는 경우.
 *
 * 핸들러가 422(Unprocessable Entity)로 변환한다.
 */
public class CoordinationFailedException extends RuntimeException {
    public CoordinationFailedException(String message) {
        super(message);
    }
}