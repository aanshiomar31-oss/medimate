package com.medimate.dto;

import javax.validation.constraints.NotBlank;

public class RecallRequest {

    @NotBlank(message = "Recall reason is required (e.g. Contamination, Defect, Regulatory Notice)")
    private String recallReason;

    @NotBlank(message = "Authorizing official name is required")
    private String authorizedBy;

    public RecallRequest() {
    }

    public RecallRequest(String recallReason, String authorizedBy) {
        this.recallReason = recallReason;
        this.authorizedBy = authorizedBy;
    }

    public String getRecallReason() {
        return recallReason;
    }

    public void setRecallReason(String recallReason) {
        this.recallReason = recallReason;
    }

    public String getAuthorizedBy() {
        return authorizedBy;
    }

    public void setAuthorizedBy(String authorizedBy) {
        this.authorizedBy = authorizedBy;
    }
}
