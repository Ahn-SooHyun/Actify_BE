package kr.co.actify.user.global.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 애플리케이션의 헬스 체크(상태 점검)를 담당하는 컨트롤러입니다.
 * 외부에서 서비스가 정상적으로 동작 중인지 확인할 때 사용됩니다.
 */
@RestController // @Controller와 @ResponseBody가 결합된 어노테이션으로, 응답 결과를 HTTP 응답 본문(Body)에 직접 작성하는 REST API용 컨트롤러임을 나타냅니다.
public class HealthCheckController {

    /**
     * 서비스 헬스 체크 API
     * HTTP GET 요청을 통해 "/health" 경로로 접근 시 서비스의 정상 가동 여부를 확인합니다.
     *
     * @return 서비스 동작 상태를 나타내는 문자열 ("actify-user-service is running")
     */
    @GetMapping("/health") // HTTP GET 메서드의 "/health" URL 요청을 이 메서드에 매핑합니다.
    public String health() {
        return "actify-user-service is running";
    }
}