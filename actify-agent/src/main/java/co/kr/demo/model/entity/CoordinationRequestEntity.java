package co.kr.demo.model.entity;

import co.kr.demo.model.vo.enums.CoordinationMode;
import co.kr.demo.model.vo.enums.CoordinationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 조율 요청 마스터 — Coordination_Requests 테이블에 매핑.
 *
 * 회원 간 조율 한 건을 표현하는 최상위 엔티티.
 * 참여자(Recipients)와 1:N 관계지만, 조회 효율을 위해 JPA 연관관계는
 * 매핑하지 않고 외래키 ID(Coordination_IDX)만 다룬다.
 *
 * 회원 간 조율에서는:
 *   - coordinationMode = MULTI_AGENT (회원 간 조율)
 *   - status = NEGOTIATING로 시작, 검수 결과에 따라 CONFIRMED 또는 CANCELLED
 *   - finalStartTime, finalEndTime은 조율 결과 선택된 슬롯 시각
 */
@Entity
@Table(name = "Coordination_Requests")
public class CoordinationRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Coordination_IDX")
    private Long id;

    @Column(name = "Requester_Users_IDX", nullable = false)
    private Long requesterUsersIdx;

    @Column(name = "Assistant_IDX")
    private Long assistantIdx;

    /** 최종 확정 시 연결될 일정. 우리 흐름에선 사용 안 함(짝 매칭은 동적). */
    @Column(name = "Schedules_IDX")
    private Long schedulesIdx;

    /** UUID 등 외부 식별 코드. 우리는 UUID로 생성한다. */
    @Column(name = "Request_Code", nullable = false, length = 64)
    private String requestCode;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "Category", nullable = false, length = 50)
    private String category = "MEETING";

    @Enumerated(EnumType.STRING)
    @Column(name = "Coordination_Mode", nullable = false, length = 30)
    private CoordinationMode coordinationMode = CoordinationMode.MULTI_AGENT;

    @Column(name = "Channel", nullable = false, length = 20)
    private String channel = "INTERNAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 30)
    private CoordinationStatus status = CoordinationStatus.DRAFT;

    @Column(name = "Final_Start_Time")
    private Instant finalStartTime;

    @Column(name = "Final_End_Time")
    private Instant finalEndTime;

    @Column(name = "Final_Candidate_IDX")
    private Long finalCandidateIdx;

    @Column(name = "Expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "Created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "Updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "Del", nullable = false)
    private boolean del = false;

    protected CoordinationRequestEntity() {
    }

    public CoordinationRequestEntity(
            Long requesterUsersIdx,
            String requestCode,
            String title,
            Instant finalStartTime,
            Instant finalEndTime
    ) {
        this.requesterUsersIdx = requesterUsersIdx;
        this.requestCode = requestCode;
        this.title = title;
        this.finalStartTime = finalStartTime;
        this.finalEndTime = finalEndTime;
        // 회원 간 조율 기본값
        this.coordinationMode = CoordinationMode.MULTI_AGENT;
        this.status = CoordinationStatus.NEGOTIATING;
    }

    // ---------- 도메인 동작 ----------

    /** 양측 모두 ACCEPT 시 호출 — 상태를 확정으로 전환. */
    public void confirm() {
        this.status = CoordinationStatus.CONFIRMED;
    }

    /** 한쪽이라도 DECLINE 시 호출 — 상태를 취소로 전환. */
    public void cancel() {
        this.status = CoordinationStatus.CANCELLED;
    }

    public void softDelete() {
        this.del = true;
    }

    // ---------- getter / setter ----------

    public Long getId() {
        return id;
    }

    public Long getRequesterUsersIdx() {
        return requesterUsersIdx;
    }

    public void setRequesterUsersIdx(Long requesterUsersIdx) {
        this.requesterUsersIdx = requesterUsersIdx;
    }

    public Long getAssistantIdx() {
        return assistantIdx;
    }

    public void setAssistantIdx(Long assistantIdx) {
        this.assistantIdx = assistantIdx;
    }

    public Long getSchedulesIdx() {
        return schedulesIdx;
    }

    public void setSchedulesIdx(Long schedulesIdx) {
        this.schedulesIdx = schedulesIdx;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public CoordinationMode getCoordinationMode() {
        return coordinationMode;
    }

    public void setCoordinationMode(CoordinationMode coordinationMode) {
        this.coordinationMode = coordinationMode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public CoordinationStatus getStatus() {
        return status;
    }

    public void setStatus(CoordinationStatus status) {
        this.status = status;
    }

    public Instant getFinalStartTime() {
        return finalStartTime;
    }

    public void setFinalStartTime(Instant finalStartTime) {
        this.finalStartTime = finalStartTime;
    }

    public Instant getFinalEndTime() {
        return finalEndTime;
    }

    public void setFinalEndTime(Instant finalEndTime) {
        this.finalEndTime = finalEndTime;
    }

    public Long getFinalCandidateIdx() {
        return finalCandidateIdx;
    }

    public void setFinalCandidateIdx(Long finalCandidateIdx) {
        this.finalCandidateIdx = finalCandidateIdx;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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