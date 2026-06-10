package com.actify.api.domain.coordination.dto;

import java.util.List;

public class GuestProposalResponse {
    private Long coordinationId;
    private String hostName;
    private List<String> timeSlots;

    public GuestProposalResponse() {}
    public GuestProposalResponse(Long coordinationId, String hostName, List<String> timeSlots) {
        this.coordinationId = coordinationId;
        this.hostName = hostName;
        this.timeSlots = timeSlots;
    }

    public Long getCoordinationId() { return coordinationId; }
    public void setCoordinationId(Long coordinationId) { this.coordinationId = coordinationId; }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public List<String> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<String> timeSlots) { this.timeSlots = timeSlots; }
}
