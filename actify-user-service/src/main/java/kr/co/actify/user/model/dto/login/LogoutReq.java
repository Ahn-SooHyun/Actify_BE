package kr.co.actify.user.model.dto.login;

import lombok.Data;

@Data
public class LogoutReq {
    private String accessToken;
    private String refreshToken;
}