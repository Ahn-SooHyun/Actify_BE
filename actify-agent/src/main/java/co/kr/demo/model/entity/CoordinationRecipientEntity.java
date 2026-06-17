package co.kr.demo.model.entity;

import co.kr.demo.model.vo.enums.RecipientInviteStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 조율 참여자 — Coordination_Recipients 테이블에 매핑.
 *
 * 한 조율 요청(Coordination_IDX)에 참여하는 사용자 한 명을 표현.
 * 회원 간 조율에서는 A와 B 두 행이 같은 Coordination_IDX 아래에 생성된다.
 *
 * 회원 간 조율 시:
 *   - recipientType = "APP_MEMBER"
 *   - participationMode = "APP_RESPONSE" (앱 안에서 직접 응답)
 *   - inviteStatus = PENDING으로 시작, ACCEPTED 또는 DECLINED로 전환
 *   - recipientUsersIdx = 참여 사용자의 ID
 */
@Entity
@Table(name = "Coordination_Recipients")
public class CoordinationRecipientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Recipient_IDX")
    private Long id;

    @Column(name = "Coordination_IDX", nullable = false)
    private Long coordinationIdx;

    @Column(name = "Contact_IDX")
    private Long contactIdx;

    /** 회원 간 조율에선 필수, 비회원 이메일 조율에선 NULL. */
    @Column(name = "Recipient_Users_IDX")
    private Long recipientUsersIdx;

    /** APP_MEMBER 또는 EMAIL_EXTERNAL. 회원 간 조율은 APP_MEMBER. */
    @Column(name = "Recipient_Type", nullable = false, length = 20)
    private String recipientType = "APP_MEMBER";

    /** AGENT_NEGOTIATION / APP_RESPONSE / EMAIL_LINK / MANUAL */
    @Column(name = "Participation_Mode", nullable = false, length = 30)
    private String participationMode = "APP_RESPONSE";

    @Column(name = "Recipient_Name", nullable = false, length = 255)
    private String recipientName;

    @Column(name = "Recipient_Email", length = 512)
    private String recipientEmail;

    @Column(name = "Invite_Token", length = 255)
    private String inviteToken;

    @Column(name = "Token_Expires_at")
    private Instant tokenExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "Invite_Status", nullable = false, length = 30)
    private RecipientInviteStatus inviteStatus = RecipientInviteStatus.PENDING;

    @Column(name = "Email_Sent_at")
    private Instant emailSentAt;

    @Column(name = "Last_Opened_at")
    private Instant lastOpenedAt;

    @Column(name = "Responded_at")
    private Instant respondedAt;

    @CreationTimestamp
    @Column(name = "Created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "Updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "Del", nullable = false)
    private boolean del = false;

    protected CoordinationRecipientEntity() {
    }

    /**
     * 회원 간 조율용 참여자 생성자.
     * 비회원 조율은 별도 생성자로 만들거나 setter로 처리.
     */
    public CoordinationRecipientEntity(
            Long coordinationIdx,
            Long recipientUsersIdx,
            String recipientName
    ) {
        this.coordinationIdx = coordinationIdx;
        this.recipientUsersIdx = recipientUsersIdx;
        this.recipientName = recipientName;
        // 회원 간 조율 기본값
        this.recipientType = "APP_MEMBER";
        this.participationMode = "APP_RESPONSE";
        this.inviteStatus = RecipientInviteStatus.PENDING;
    }

    // ---------- 도메인 동작 ----------

    /** 참여자가 수락. 상태와 응답 시각을 함께 변경. */
    public void accept() {
        this.inviteStatus = RecipientInviteStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    /** 참여자가 거절. 상태와 응답 시각을 함께 변경. */
    public void decline() {
        this.inviteStatus = RecipientInviteStatus.DECLINED;
        this.respondedAt = Instant.now();
    }

    public void softDelete() {
        this.del = true;
    }

    // ---------- getter / setter ----------

    public Long getId() {
        return id;
    }

    public Long getCoordinationIdx() {
        return coordinationIdx;
    }

    public void setCoordinationIdx(Long coordinationIdx) {
        this.coordinationIdx = coordinationIdx;
    }

    public Long getContactIdx() {
        return contactIdx;
    }

    public void setContactIdx(Long contactIdx) {
        this.contactIdx = contactIdx;
    }

    public Long getRecipientUsersIdx() {
        return recipientUsersIdx;
    }

    public void setRecipientUsersIdx(Long recipientUsersIdx) {
        this.recipientUsersIdx = recipientUsersIdx;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public String getParticipationMode() {
        return participationMode;
    }

    public void setParticipationMode(String participationMode) {
        this.participationMode = participationMode;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public RecipientInviteStatus getInviteStatus() {
        return inviteStatus;
    }

    public void setInviteStatus(RecipientInviteStatus inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    public Instant getEmailSentAt() {
        return emailSentAt;
    }

    public void setEmailSentAt(Instant emailSentAt) {
        this.emailSentAt = emailSentAt;
    }

    public Instant getLastOpenedAt() {
        return lastOpenedAt;
    }

    public void setLastOpenedAt(Instant lastOpenedAt) {
        this.lastOpenedAt = lastOpenedAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDel() {
        return del;
    }
}