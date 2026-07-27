package com.aeroresume.backend.dto;

public class CompileResumeRequest {
    
    private String latexString;
    private boolean isFinalExport;
    private Long userId;
    private Long resumeId;

    public CompileResumeRequest() {
    }

    public CompileResumeRequest(String latexString, boolean isFinalExport, Long userId, Long resumeId) {
        this.latexString = latexString;
        this.isFinalExport = isFinalExport;
        this.userId = userId;
        this.resumeId = resumeId;
    }

    public String getLatexString() {
        return latexString;
    }

    public void setLatexString(String latexString) {
        this.latexString = latexString;
    }

    public boolean isFinalExport() {
        return isFinalExport;
    }

    public void setFinalExport(boolean finalExport) {
        isFinalExport = finalExport;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }
}
