package kr.co.actify.user.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "actify.mail")
public class MailProperties {

    @NotBlank(message = "메일 발신자 주소는 필수입니다.")
    private String fromAddress;

    @NotBlank(message = "메일 발신자 이름은 필수입니다.")
    private String fromName = "Actify";
}