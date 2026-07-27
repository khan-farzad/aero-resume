package com.aeroresume.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aeroresume.backend.model.Resume;
import com.aeroresume.backend.repository.ResumeRepository;

@Service
public class ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeService.class);

    private final String tempWorkspacePath;
    private final String permanentStoragePath;

    private ResumeRepository resumeRepository;

    public ResumeService(@Value("${resume.storage.temp-workspace}") String tempWorkspacePath, 
                        @Value("${resume.storage.permanentPath}") String permanentStoragePath,
                        ResumeRepository resumeRepository) {
        this.tempWorkspacePath = tempWorkspacePath;
        this.permanentStoragePath = permanentStoragePath;
        this.resumeRepository = resumeRepository;
    }

    public byte[] compileResume(String latexString, boolean isFinalExport, Long userId, Long resumeId) {

        String runId = UUID.randomUUID().toString();
        Path userWorkspace = Path.of(tempWorkspacePath, String.valueOf(userId));

        Path texFilePath = userWorkspace.resolve(runId + ".tex");
        Path pdfFilePath = userWorkspace.resolve(runId + ".pdf");

        // Other noisy files that might be generated during compilation. Trracking them for cleanup.
        Path logFilePath = userWorkspace.resolve(runId + ".log");
        Path auxFilePath = userWorkspace.resolve(runId + ".aux");
        Path outFilePath = userWorkspace.resolve(runId + ".out");

        try {
        
            // Create the user temp workspace directory if it doesn't exist
            Files.createDirectories(userWorkspace);

            //System.out.println("Compiling LaTeX for userId: " + userId + ", resumeId: " + resumeId + ", runId: " + runId);
            //System.out.println("texFilePath: " + texFilePath + ", latexString: " + latexString);
            //System.out.println("Absolute texFilePath: " + texFilePath.toAbsolutePath());

            // Write the LaTeX string to the .tex file
            Files.writeString(texFilePath, latexString);

            // Compile the LaTeX file to PDF using pdflatex
            ProcessBuilder processBuilder = new ProcessBuilder(
                "pdflatex",
                "-interaction=nonstopmode",
                "-halt-on-error",
                "-output-directory=" + userWorkspace.toAbsolutePath(),
                texFilePath.toAbsolutePath().toString()
            );

            processBuilder.redirectErrorStream(true); // Merge stdout and stderr i.e. Redirect error stream to standard output to avoid blocking
            Process process = processBuilder.start();

            boolean finalInTime = process.waitFor(10, TimeUnit.SECONDS); // Wait for the process to complete with a 10-second timeout

            if(!finalInTime) {
                process.destroyForcibly(); // Kill the process if it didn't finish in time
                log.error("LaTeX compilation timed out for userId: {} on Run: {}", userId, runId);
                throw new RuntimeException("LaTeX compilation timed out.");
            }

            if(process.exitValue() != 0) {
                String errorOutput = Files.readString(logFilePath);
                log.error("LaTeX compilation failed for userId: {} on Run: {}. Error: {}", userId, runId, errorOutput);
                throw new RuntimeException("LaTeX compilation failed. Check logs for details.");
            }

            byte[] pdfBytes = Files.readAllBytes(pdfFilePath);

            if(isFinalExport) {
                // Move the PDF to permanent storage
                persistPdf(pdfFilePath, userId, resumeId);
            }
            
            return pdfBytes;
        } catch (Exception e) {
            log.error("Error during LaTeX compilation for userId: {} on Run: {}. Error: {}", userId, runId, e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new RuntimeException("Error during LaTeX compilation: " + e.getMessage(), e);
        } finally {
            // Cleanup temporary files
            deleteIfExists(texFilePath);
            deleteIfExists(logFilePath);
            deleteIfExists(auxFilePath);
            deleteIfExists(outFilePath);
            deleteIfExists(pdfFilePath); // Only delete if not final export
            deleteIfExists(userWorkspace); // Attempt to delete the user workspace directory if it's empty
            
            log.debug("Temporary files cleaned up for userId: {} on Run: {}", userId, runId);
        }
    }

    private void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn("Failed to delete temp file: {}. This may cause a minor storage leak over time. Error: {}", path, e.getMessage(), e);
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
        resumeRepository.save(resume);
        log.info("Resume entity updated with final export URL for resumeId: {}", resumeId);

    }

}
