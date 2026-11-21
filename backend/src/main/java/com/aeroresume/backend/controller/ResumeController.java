package com.aeroresume.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aeroresume.backend.dto.CompileResumeRequest;
import com.aeroresume.backend.dto.ResumeResponse;
import com.aeroresume.backend.security.CustomUserDetails;
import com.aeroresume.backend.service.ResumeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/")
    public ResponseEntity<List<ResumeResponse>> getResumes(@AuthenticationPrincipal CustomUserDetails userDetail) {
        return ResponseEntity.ok().body(resumeService.getResumesForUser(userDetail.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(@AuthenticationPrincipal CustomUserDetails userDetail, @PathVariable Long id) {
        ResumeResponse resume = resumeService.getResumeForUser(userDetail.getId(), id);
        return ResponseEntity.ok().body(resume);
    }

    @PostMapping("/")
    public ResponseEntity<ResumeResponse> createResume(@RequestBody Map<String, Object> request, @AuthenticationPrincipal CustomUserDetails userDetail) {
        Long userId = userDetail.getId();
        Long templateId = Long.valueOf((Integer)request.get("templateId"));
        String resumeName = (String) request.get("resumeName");
        ResumeResponse resume = resumeService.createResume(userId, templateId, resumeName);

        return ResponseEntity.status(HttpStatus.CREATED).body(resume);
    }

    @PostMapping("/compile")
    public ResponseEntity<byte[]> compileResume(@RequestBody CompileResumeRequest request, @AuthenticationPrincipal CustomUserDetails userDetail) {
        Long userId = userDetail.getId();

        byte[] pdfBytes = resumeService.compileResume(
            request.getLatexString(),
            request.isFinalExport(),
            userId,
            request.getResumeId(),
            request.getResumeName()
        );
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename="+request.getResumeName().trim()+".pdf")
                .body(pdfBytes);

    }

    // @PostMapping("/latex")
    // public String getLatexString(@AuthenticationPrincipal CustomUserDetails userDetail, @RequestParam Long templateId) {
    //     Long userId = userDetail.getId();
    //     return resumeService.getLatex(userId, templateId);
    // }
}