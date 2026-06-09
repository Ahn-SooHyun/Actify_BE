package co.kr.demo.model.entity;

import co.kr.demo.model.vo.enums.ScheduleSource;
import co.kr.demo.model.vo.enums.ScheduleStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * 일정 엔티티 — Actify DB의 Schedules 테이블에 매핑한다.
 */
@Entity
@Table(name = "Schedules")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Schedules_IDX")
    private Long id;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Column(name = "Assistant_IDX")
    private Long assistantIdx;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Category", nullable = false, length = 50)
    private String category = "ETC";

    @Column(name = "Start_Time", nullable = false)
    private Instant startTime;

    @Column(name = "End_Time", nullable = false)
    private Instant endTime;

    @Column(name = "Timezone", nullable = false, length = 64)
    private String timezone = "Asia/Seoul";

    @Column(name = "All_Day", nullable = false)
    private boolean allDay = false;

    @Column(name = "Address", length = 512)
    private String address;

    @Column(name = "Address_Detail", length = 512)
    private String addressDetail;

    @Column(name = "Online_URL", length = 1024)
    private String onlineUrl;

    @Column(name = "Description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Source", nullable = false, length = 30)
    private ScheduleSource source = ScheduleSource.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 30)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @Column(name = "Review_Required", nullable = false)
    private boolean reviewRequired = true;

    @Column(name = "Priority", nullable = false)
    private int priority = 3;

    @Column(name = "Recurrence_Rule", length = 512)
    private String recurrenceRule;

    @Column(name = "Confirmed_at")
    private Instant confirmedAt;

    @Column(name = "Cancelled_at")
    private Instant cancelledAt;

    @CreationTimestamp
    @Column(name = "Created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "Updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "Del", nullable = false)
    private boolean del = false;

    protected ScheduleEntity() {
    }


    public ScheduleEntity(Long usersIdx, String title, Instant startTime, Instant endTime) {
        this.usersIdx = usersIdx;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // ---------- 도메인 동작 ----------

    /** 소프트 삭제 — 물리 삭제 대신 플래그만 변경한다. */
    public void softDelete() {
        this.del = true;
    }

    /** 일정 확정 — 상태와 확정 시각을 함께 변경한다. */
    public void confirm() {
        this.status = ScheduleStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    // ---------- getter / setter ----------

    public Long getId() {
        return id;
    }

    public Long getUsersIdx() {
        return usersIdx;
    }

    public void setUsersIdx(Long usersIdx) {
        this.usersIdx = usersIdx;
    }

    public Long getAssistantIdx() {
        return assistantIdx;
    }

    public void setAssistantIdx(Long assistantIdx) {
        this.assistantIdx = assistantIdx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public String getOnlineUrl() {
        return onlineUrl;
    }

    public void setOnlineUrl(String onlineUrl) {
        this.onlineUrl = onlineUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScheduleSource getSource() {
        return source;
    }

    public void setSource(ScheduleSource source) {
        this.source = source;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public boolean isReviewRequired() {
        return reviewRequired;
    }

    public void setReviewRequired(boolean reviewRequired) {
        this.reviewRequired = reviewRequired;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
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