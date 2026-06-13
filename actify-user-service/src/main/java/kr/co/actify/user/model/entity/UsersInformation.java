package kr.co.actify.user.model.entity;

import jakarta.persistence.*;
import kr.co.actify.user.model.vo.PublicDel;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 사용자의 민감한 개인정보 및 자산(잔액) 정보를 관리하는 Entity 클래스입니다.
 * 별도의 테이블로 분리되어 보안 강화 및 암호화 처리가 집중되어 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Information")
public class UsersInformation {
    /** 사용자 고유 식별자 (Users 엔티티와 공유되는 PK 및 FK) */
    @Id
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /** AES-256-GCM으로 암호화된 사용자 이름 */
    @Column(name = "Name_Enc", nullable = false, columnDefinition = "TEXT")
    private String nameEnc;

    /** AES-256-GCM으로 암호화된 이메일 주소 */
    @Column(name = "Mail_Enc", nullable = false, columnDefinition = "TEXT")
    private String mailEnc;

    /** 이메일 중복 검사 및 조회용 HMAC-SHA256 hex 값 */
    @Column(name = "Mail_Hash", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String mailHash;

    /** AES-256-GCM으로 암호화된 휴대전화 번호 (DB에서 NULL 허용) */
    @Column(name = "Phone_Number_Enc", columnDefinition = "TEXT")
    private String phoneNumberEnc;

    /** 휴대전화 번호 조회 및 중복 검사 후보용 HMAC-SHA256 hex 값 (DB에서 NULL 허용) */
    @Column(name = "Phone_Number_Hash", length = 64, columnDefinition = "CHAR(64)")
    private String phoneNumberHash;

    /** AES-256-GCM으로 암호화된 생년월일 (DB에서 NULL 허용) */
    @Column(name = "Birth_Enc", columnDefinition = "TEXT")
    private String birthEnc;

    /** 프로필 이미지 URL (기존 Long 타입에서 DB 스펙인 VARCHAR(1024) String으로 변경) */
    @Column(name = "Profile_Image_URL", length = 1024)
    private String profileImageUrl;

    /** 마케팅 정보 수신 동의 일시 (중복 선언된 필드 제거 및 통일) */
    @Column(name = "Agree_Marketing_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime agreeMarketingAt;

    /** 정보 최초 등록 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 정보 최근 수정 일시 */
    @Column(name = "Updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** 삭제 여부 (0=정상, 1=삭제) */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private PublicDel del;

    @Builder
    public UsersInformation(Long usersIdx, String nameEnc, String mailEnc, String mailHash,
                            String phoneNumberEnc, String phoneNumberHash, String birthEnc,
                            String profileImageUrl, LocalDateTime agreeMarketingAt) {
        this.usersIdx = usersIdx;
        this.nameEnc = nameEnc;
        this.mailEnc = mailEnc;
        this.mailHash = mailHash;
        this.phoneNumberEnc = phoneNumberEnc;
        this.phoneNumberHash = phoneNumberHash;
        this.birthEnc = birthEnc;
        this.profileImageUrl = profileImageUrl;
        this.agreeMarketingAt = agreeMarketingAt;
        this.del = PublicDel.ACTIVE;
    }
}