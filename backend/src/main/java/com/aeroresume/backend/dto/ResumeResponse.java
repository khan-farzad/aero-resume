package com.aeroresume.backend.dto;

public class ResumeResponse {
    private Long id;
    private String name;
    private String latexContent;
    private String pdfFilePath;

    public ResumeResponse() {}

    public ResumeResponse(Long id, String name, String latexContent, String pdfFilePath) {
        this.id = id;
        this.name = name;
        this.latexContent = latexContent;
        this.pdfFilePath = pdfFilePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatexContent() {
        return latexContent;
    }

    public void setLatexContent(String latexContent) {
        this.latexContent = latexContent;
    }

    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }
}
