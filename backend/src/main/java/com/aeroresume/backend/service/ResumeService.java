package com.aeroresume.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aeroresume.backend.constants.ResumeStatus;
import com.aeroresume.backend.dto.ResumeResponse;
import com.aeroresume.backend.mapper.ResumeMapper;
import com.aeroresume.backend.model.Resume;
import com.aeroresume.backend.model.Template;
import com.aeroresume.backend.model.User;
import com.aeroresume.backend.repository.ResumeRepository;
import com.aeroresume.backend.repository.TemplateRepository;
import com.aeroresume.backend.repository.UserRepository;
import com.aeroresume.backend.templating.ResumeDataPreparer;
import com.aeroresume.backend.templating.ResumeTemplateRenderer;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private final String tempWorkspacePath;
    private final String permanentStoragePath;

    private ResumeRepository resumeRepository;
    private TemplateRepository templateRepository;
    private UserRepository userRepository;
    private ResumeMapper resumeMapper;
    private ResumeDataPreparer resumeDataPreparer;
    private ResumeTemplateRenderer resumeTemplateRenderer;

    public ResumeService(@Value("${resume.storage.temp-workspace}") String tempWorkspacePath, 
                        @Value("${resume.storage.permanentPath}") String permanentStoragePath,
                        ResumeRepository resumeRepository,
                        TemplateRepository templateRepository,
                        UserRepository userRepository,
                        ResumeMapper resumeMapper,
                        ResumeDataPreparer resumeDataPreparer,
                        ResumeTemplateRenderer resumeTemplateRenderer) {
        this.tempWorkspacePath = tempWorkspacePath;
        this.permanentStoragePath = permanentStoragePath;
        this.resumeRepository = resumeRepository;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.resumeMapper = resumeMapper;
        this.resumeDataPreparer = resumeDataPreparer;
        this.resumeTemplateRenderer = resumeTemplateRenderer;
    }

    /**
     * Compiles LaTeX code into a PDF. 
     * * @param latexCode     The raw LaTeX string from the frontend
     * @param userId        The ID of the user compiling the resume (for folder organization)
     * @param isFinalExport True if the user clicked "Download", False if they clicked "Compile"
     * @param resumeId      The ID of the resume (used for naming the final export file)
     * @param resumeName    The name of the resume (used for updating the resume entity)
     * @return              The binary bytes of the generated PDF
     */
    public byte[] compileResume(String latexString, boolean isFinalExport, Long userId, Long resumeId, String resumeName) {

        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId).orElseThrow(() -> new RuntimeException("Resume not found."));
        if(resume  == null) {
            throw new RuntimeException("Resume not found for id: " + resumeId + " and user id: " + userId);
        }

        UUID runId = UUID.randomUUID();
        resume.setRunId(runId);
        resumeRepository.saveAndFlush(resume);

        Path userWorkspace = Path.of(tempWorkspacePath, String.valueOf(userId));

        Path texFilePath = userWorkspace.resolve(runId + ".tex");
        Path pdfFilePath = userWorkspace.resolve(runId + ".pdf");

        // Other noisy files that might be generated during compilation. Tracking them for cleanup.
        Path logFilePath = userWorkspace.resolve(runId + ".log");
        Path auxFilePath = userWorkspace.resolve(runId + ".aux");
        Path outFilePath = userWorkspace.resolve(runId + ".out");

        Boolean compilationSuccess = false; // Track if compilation was successful for cleanup purposes

        Process process = null; // Initialize the process variable outside the try block for proper cleanup in case of exceptions

        try {
        
            // Create the user temp workspace directory if it doesn't exist
            Files.createDirectories(userWorkspace);

            // Write the LaTeX string to the .tex file
            Files.writeString(texFilePath, latexString);

            log.info("Starting LaTeX compilation for userId: {} on Run: {}", userId, runId);

            // Compile the LaTeX file to PDF using pdflatex
            ProcessBuilder processBuilder = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-halt-on-error",
                "-output-directory=" + userWorkspace.toAbsolutePath(),
                texFilePath.toAbsolutePath().toString()
            );

            processBuilder.redirectErrorStream(true); // Merge stdout and stderr i.e. Redirect error stream to standard output to avoid blocking
            
            try{
                process = processBuilder.start();
            }
            catch(Exception e){
                log.error("Failed to start pdflatex process for userId: {} on Run: {}. Error: {}", userId, runId, e.getMessage(), e);
                throw new RuntimeException("Failed to start LaTeX compilation process: " + e.getMessage(), e);
            }

            boolean finalInTime = process.waitFor(10, TimeUnit.SECONDS); // Wait for the process to complete with a 10-second timeout

            if(!finalInTime) {
                process.destroyForcibly(); // Kill the process if it didn't finish in time
                log.error("LaTeX compilation timed out for userId: {} on Run: {}", userId, runId);
                throw new RuntimeException("LaTeX compilation timed out.");
            }

            compilationSuccess = process.exitValue() == 0;
            if(!compilationSuccess) {
                log.error("LaTeX compilation failed for userId: {} on Run: {}.", userId, runId);
                throw new RuntimeException("LaTeX compilation failed. Check log file for details.");
            }

            log.info("LaTex compilation succcessful for userId: {} on Run: {} for ResumeID: {}", userId, runId, resumeId);

            byte[] pdfBytes = Files.readAllBytes(pdfFilePath);

            
            resume.setName(resumeName);
            resume.setLatexContent(latexString);
            resume.setStatus(ResumeStatus.COMPILED);
            resumeRepository.save(resume);
            log.info("Resume entity updated with LaTeX content and template for resumeId: {}", resumeId);
            
            if(isFinalExport) {
                // Move the PDF to permanent storage
                persistPdf(pdfFilePath, userId, resumeId);
                log.info("Final PDF persisted succesfully for userId: {} on Run: {} for ResumeID: {}", userId, runId, resumeId);
            }

            return pdfBytes;

        } catch(InterruptedException e) {
            if(process != null) {
                process.destroyForcibly(); // Ensure the process is killed if interrupted
            }

            markFailed(resume, runId, e);
            log.error("LaTeX compilation interrupted for userId: {} on Run: {}.", userId, runId);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("LaTeX compilation was interrupted: " + e.getMessage(), e);
        }
        catch (Exception e) {
            markFailed(resume, runId, e);
            log.error("Error during LaTeX compilation for userId: {} on Run: {}.", userId, runId);
            throw new RuntimeException("Error during LaTeX compilation: " + e.getMessage(), e);
        } finally {
            // Cleanup temporary files
            try {
                Files.deleteIfExists(texFilePath);
                Files.deleteIfExists(auxFilePath);
                Files.deleteIfExists(outFilePath);
                Files.deleteIfExists(pdfFilePath);
                if(compilationSuccess) {
                    Files.deleteIfExists(logFilePath);
                }
                if(Files.exists(userWorkspace) && Files.list(userWorkspace).findAny().isEmpty()) {
                    Files.delete(userWorkspace);
                }
            } catch (IOException e) {
                log.warn("Failed to delete empty user workspace for userId: {} on Run: {}. Error: {}", userId, runId, e.getMessage(), e);
            }
            
            log.info("Temporary files cleaned up for userId: {} on Run: {}", userId, runId);
        }
    }

    private void markFailed(Resume resume, UUID runId, Exception failure) {
        resume.setStatus(ResumeStatus.FAILED);
        try {
            resumeRepository.save(resume);
            log.info("Resume entity marked as FAILED for resumeId: {} on Run: {} due to exception: {}", resume.getId(), runId);
        } catch (RuntimeException saveError) {
            failure.addSuppressed(saveError);
            log.error("Failed to mark Resume entity as FAILED for resumeId: {} on Run: {}. Error: {}", resume.getId(), runId, saveError.getMessage(), saveError);
        }
    }

    private void persistPdf(Path pdfFilePath, Long userId, Long resumeId) {
        
        Path userPermanentStorage = Path.of(permanentStoragePath, String.valueOf(userId));

        if(!Files.exists(userPermanentStorage)) {
            try {
                Files.createDirectories(userPermanentStorage);
                log.info("Created permanent storage directory for userId: {}", userId);
            } catch (Exception e) {
                log.error("Failed to create permanent storage directory for userId: {}. Error: {}", userId, e.getMessage(), e);
                throw new RuntimeException("Failed to create permanent storage directory: " + e.getMessage(), e);
            }
        }

        Path finalPdfFile = userPermanentStorage.resolve(resumeId.toString() + ".pdf");

        try {
            Files.move(pdfFilePath, finalPdfFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("PDF successfully moved to permanent storage for userId: {}, resumeId: {}", userId, resumeId);
        }
        catch(Exception e) {
            log.error("Failed to move PDF to permanent storage for userId: {}, resumeId: {}. Error: {}", userId, resumeId, e.getMessage(), e);
            throw new RuntimeException("Failed to move PDF to permanent storage: " + e.getMessage(), e);
        }

        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new RuntimeException("Resume not found for id: " + resumeId));
        resume.setFinalExportUrl(finalPdfFile.toAbsolutePath().toString());
        resume.setStatus(ResumeStatus.COMPLETED);
        Resume completedResume = resumeRepository.save(resume);
        log.info("Resume entity updated with final export URL for resumeId: {} and status: {}", resumeId, completedResume.getStatus());

    }

    public String getLatex(Long userId, Long templateId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found for id: " + templateId));
        
        String latexString = template.getLatexBoilerplate();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));

        Map<String, Object> escapedJsonData = resumeDataPreparer.prepare(user.getMasterJsonData());
        escapedJsonData.put("name", user.getFirstName() + " " + user.getLastName());
        escapedJsonData.put("email", user.getEmail());
        
        String renderedLatex = resumeTemplateRenderer.render(latexString, escapedJsonData);

        return renderedLatex;
    }

    public ResumeResponse createResume(Long userId, Long templateId, String resumeName) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found for id: " + templateId));

        Resume resume = new Resume();
        resume.setName(resumeName);
        resume.setUser(user);
        resume.setTemplate(template);
        resume.setRawJsonData(user.getMasterJsonData());
        resume.setLatexContent(getLatex(userId, templateId));

        Resume savedResume = resumeRepository.save(resume);
        log.info("New Resume created with ID: {} for userId: {} using templateId: {}", savedResume.getId(), userId, templateId);

        return resumeMapper.resumeToResumeResponse(savedResume);
    }

    public List<ResumeResponse> getResumesForUser(Long userId) {
        List<Resume> resumes = resumeRepository.findByUserId(userId);
        return resumes.stream()
                .map(resumeMapper::resumeToResumeResponse)
                .collect(Collectors.toList());
    }

    public ResumeResponse getResumeForUser(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new RuntimeException("Resume not found for id: " + resumeId + " and user id: " + userId));
        return resumeMapper.resumeToResumeResponse(resume);
    }
}
