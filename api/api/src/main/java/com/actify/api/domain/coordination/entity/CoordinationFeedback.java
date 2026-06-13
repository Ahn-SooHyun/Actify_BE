package com.actify.api.domain.coordination.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "coordination_feedbacks")
public class CoordinationFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long coordinationId;
    private String guestEmail;
    private String selectedTimeSlot;

    protected CoordinationFeedback() {}
    public CoordinationFeedback(Long id, Long coordinationId, String guestEmail, String selectedTimeSlot) {
        this.id = id;
        this.coordinationId = coordinationId;
        this.guestEmail = guestEmail;
        this.selectedTimeSlot = selectedTimeSlot;
    }

    public Long getId() { return id; }
    public Long getCoordinationId() { return coordinationId; }
    public String getGuestEmail() { return guestEmail; }
    public String getSelectedTimeSlot() { return selectedTimeSlot; }
}
