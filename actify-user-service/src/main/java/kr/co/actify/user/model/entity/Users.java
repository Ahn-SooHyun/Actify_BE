package kr.co.actify.user.model.entity;

import jakarta.persistence.*;
import kr.co.actify.user.model.vo.PublicDel;
import kr.co.actify.user.model.vo.UsersRole;
import kr.co.actify.user.model.vo.UsersStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 서비스의 핵심 사용자 계정 정보를 관리하는 Entity 클래스입니다.
 * 인증(로그인), 권한 관리, 계정 상태(잠금, 삭제) 등의 기능을 담당합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert // insert 시 null인 필드 제외 (DB 기본값 활용)
@Table(name = "Users")
public class Users {
    /** 사용자 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /** 로그인 ID (결정적 암호화 적용으로 DB 내 검색 가능) */
    @Column(name = "ID", nullable = false)
    private String id;

    /** BCrypt로 암호화된 비밀번호 */
    @Column(name = "PW", nullable = false)
    private String pw;

    /** 로그인 실패 횟수 (계정 잠금 처리용) */
    @Column(name = "Failed_Login_Attempts", nullable = false, columnDefinition = "SMALLINT UNSIGNED")
    private Integer failedLoginAttempts;

    /** 계정 잠금 해제 일시 */
    @Column(name = "Locked_Until")
    private LocalDateTime lockedUntil;

    /** 계정 잠금 발생 사유 */
    @Column(name = "Locked_Reason")
    private String lockedReason;

    /** 사용자 역할 권한 (USERS, ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false, length = 20)
    private UsersRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 30)
    private UsersStatus status;

    /** 이용약관 동의 일시 */
    @Column(name = "Agree_Terms_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime agreeTermsAt;

    /** 개인정보 처리방침 동의 일시 */
    @Column(name = "Agree_Privacy_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime agreePrivacyAt;

    /** 계정 생성 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 계정 정보 수정 일시 */
    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** 삭제/활성 상태 (0: ACTIVE, 1: DELETED) */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private PublicDel del;

    @Builder
    public Users(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.failedLoginAttempts = 0;
        this.role = UsersRole.USERS;
        this.status = UsersStatus.PENDING_EMAIL; // 최초 생성 시 이메일 인증 대기 상태
        this.agreeTermsAt = LocalDateTime.now(); // 최초 생성 시 이용약관 동의 일시
        this.agreePrivacyAt = LocalDateTime.now();
        this.del = PublicDel.ACTIVE;
    }

    public void activateUsers() {
        this.status = UsersStatus.ACTIVE;
    }

}