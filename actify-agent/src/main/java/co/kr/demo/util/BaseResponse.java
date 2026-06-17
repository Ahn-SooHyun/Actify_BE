package co.kr.demo.util;

/**
 * 모든 API 응답의 공통 포맷.
 *
 * 컨벤션에 따라 응답은 항상 이 구조로 감싼다.
 * resultCode: "ok" 또는 "fail"
 * message:    사용자에게 보여줄 간단한 설명
 * data:       실제 응답 데이터 (실패 시 null)
 *
 * 정적 팩토리 메서드 ok()와 fail()로 편하게 만들 수 있다.
 */
public record BaseResponse<T>(
        String resultCode,
        String message,
        T data
) {
    /** 성공 응답 (메시지 + 데이터). */
    public static <T> BaseResponse<T> ok(String message, T data) {
        return new BaseResponse<>("ok", message, data);
    }

    /** 성공 응답 (데이터만, 메시지는 기본값). */
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>("ok", "성공", data);
    }

    /** 실패 응답 (메시지만, 데이터는 null). */
    public static <T> BaseResponse<T> fail(String message) {
        return new BaseResponse<>("fail", message, null);
    }
}