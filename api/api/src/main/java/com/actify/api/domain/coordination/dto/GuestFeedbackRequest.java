package com.actify.api.domain.coordination.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class GuestFeedbackRequest {
    @Email private String guestEmail;
    @NotEmpty private List<String> selectedTimes;

    public GuestFeedbackRequest() {}

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public List<String> getSelectedTimes() { return selectedTimes; }
    public void setSelectedTimes(List<String> selectedTimes) { this.selectedTimes = selectedTimes; }
}
