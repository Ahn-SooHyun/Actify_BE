package kr.co.actify.user.model.entity;

import jakarta.persistence.*;
import kr.co.actify.user.model.vo.PublicDel;
import kr.co.actify.user.model.vo.UsersVerificationsPurPose;
import kr.co.actify.user.model.vo.UsersVerificationsStatus;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 이메일 또는 휴대폰 인증 등의 본인 확인 절차 정보를 관리하는 Entity 클래스입니다.
 * 발급된 코드의 유효 기간, 인증 목적, 현재 상태 등을 추적합니다.
 */
@Entity
@Getter
// @Setter // (💡Tip: Entity 클래스 레벨의 @Setter는 의도치 않은 데이터 변경을 유발할 수 있어 실무에서는 지양합니다. 필요시 필드 레벨에만 적용하세요!)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Verifications")
public class UsersVerifications {

    /** 인증 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Verification_IDX")
    private Long verificationIdx;

    /** 인증을 요청한 사용자의 식별자 (FK) */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /** 인증 목적 (SIGNUP, RESET_PW, DELETE_ACCOUNT 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Purpose", nullable = false, length = 30)
    private UsersVerificationsPurPose purpose;

    /** * 발급된 보안 인증 코드 또는 토큰 해시
     * 🚨 DDL은 VARBINARY(64)를 요구하므로 String과 매핑 시 주의가 필요합니다.
     */
    @Column(name = "Code", nullable = false, length = 64, columnDefinition = "VARBINARY(64)")
    private String code;

    /** 인증 요청 생성 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    /** 인증 코드 만료 예정 일시 */
    @Column(name = "Expires_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime expiresAt;

    /** 실제 인증이 완료된 일시 */
    @Column(name = "Verified_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime verifiedAt;

    /** 인증 현재 상태 (PENDING, VERIFIED, EXPIRED 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private UsersVerificationsStatus status;

    /** 해당 인증 데이터의 삭제/유효 여부 */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT(1)")
    private PublicDel del;

    @Builder
    public UsersVerifications(Long usersIdx, UsersVerificationsPurPose purpose, String code, LocalDateTime expiresAt, UsersVerificationsStatus status) {
        this.usersIdx = usersIdx;
        this.purpose = purpose;
        this.code = code;
        this.expiresAt = expiresAt;
        this.status = status != null ? status : UsersVerificationsStatus.PENDING;
        this.del = PublicDel.ACTIVE;
    }

    public void setExpiresAt() {
        this.status = UsersVerificationsStatus.EXPIRED;
    }

    public void setFailed() {
        this.status = UsersVerificationsStatus.FAILED;
    }

    /** 사용자가 입력한 코드가 확인되어 인증을 완료 처리합니다. */
    public void confirmVerification() {
        this.status = UsersVerificationsStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }
}