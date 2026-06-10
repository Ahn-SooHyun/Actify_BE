package com.actify.api.domain.coordination.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coordination_sessions")
public class CoordinationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hostName;
    private String secureToken;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coordination_candidate_slots", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "candidate_time")
    private List<String> candidateTimeSlots = new ArrayList<>();

    private String finalFixedSchedule = "미확정";

    protected CoordinationSession() {}
    public CoordinationSession(Long id, String hostName, String secureToken, List<String> candidateTimeSlots, String finalFixedSchedule) {
        this.id = id;
        this.hostName = hostName;
        this.secureToken = secureToken;
        this.candidateTimeSlots = candidateTimeSlots;
        this.finalFixedSchedule = finalFixedSchedule;
    }

    public Long getId() { return id; }
    public String getHostName() { return hostName; }
    public String getSecureToken() { return secureToken; }
    public List<String> getCandidateTimeSlots() { return candidateTimeSlots; }
    public String getFinalFixedSchedule() { return finalFixedSchedule; }
}
