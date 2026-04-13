package com.codesync.execution.worker;

import com.codesync.execution.entity.ExecutionJob;
import com.codesync.execution.entity.ExecutionJob.JobStatus;
import com.codesync.execution.repository.ExecutionRepository;
import com.codesync.execution.service.ExecutionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class ExecutionWorker {

    private static final Logger log = LoggerFactory.getLogger(ExecutionWorker.class);

    private final ExecutionRepository executionRepository;
    private final ExecutionServiceImpl executionService;
    private final SimpMessagingTemplate messagingTemplate;

    public ExecutionWorker(
            ExecutionRepository executionRepository,
            ExecutionServiceImpl executionService,
            SimpMessagingTemplate messagingTemplate) {
        this.executionRepository = executionRepository;
        this.executionService = executionService;
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "execution-queue")
    public void processJob(String jobId) {
        log.info("Processing job: {}", jobId);

        ExecutionJob job = executionRepository.findByJobId(jobId).orElse(null);
        if (job == null) {
            log.error("Job not found: {}", jobId);
            return;
        }

        try {
            job.setStatus(JobStatus.RUNNING);
            executionRepository.save(job);

            messagingTemplate.convertAndSend("/topic/execution/" + jobId, "Job started...");

            String dockerImage = executionService.getDockerImage(job.getLanguage())
                    .orElseThrow(() -> new RuntimeException("Unsupported language: " + job.getLanguage()));

            String result = executeInDocker(job, dockerImage);

            job.setStdout(result.get("stdout"));
            job.setStderr(result.get("stderr"));
            job.setExitCode(Integer.parseInt(result.get("exitCode")));
            job.setExecutionTimeMs(Long.parseLong(result.get("executionTimeMs")));
            job.setMemoryUsedKb(Long.parseLong(result.get("memoryUsedKb")));
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/execution/" + jobId, "Job completed");

        } catch (Exception e) {
            log.error("Execution failed for job: {}", jobId, e);
            job.setStatus(JobStatus.FAILED);
            job.setStderr(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());

            messagingTemplate.convertAndSend("/topic/execution/" + jobId, "Job failed: " + e.getMessage());
        }

        executionRepository.save(job);
    }

    private java.util.Map<String, String> executeInDocker(ExecutionJob job, String dockerImage) throws Exception {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        String language = job.getLanguage();

        String[] command;
        if ("java".equals(language)) {
            command = new String[]{"docker", "run", "--rm", "--network", "none",
                    "--memory", "256m", "--cpus", "1",
                    "-e", "JAVA_TOOL_OPTIONS=-Xmx256m",
                    dockerImage, "java", "Main.java"};
        } else if ("python".equals(language)) {
            command = new String[]{"docker", "run", "--rm", "--network", "none",
                    "--memory", "256m", "--cpus", "1",
                    dockerImage, "python", "script.py"};
        } else if ("javascript".equals(language) || "node".equals(language)) {
            command = new String[]{"docker", "run", "--rm", "--network", "none",
                    "--memory", "256m", "--cpus", "1",
                    dockerImage, "node", "script.js"};
        } else if ("cpp".equals(language) || "c".equals(language)) {
            command = new String[]{"docker", "run", "--rm", "--network", "none",
                    "--memory", "256m", "--cpus", "1",
                    dockerImage, "bash", "-c", "gcc main.c -o program && ./program"};
        } else {
            throw new RuntimeException("Unsupported language: " + language);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new java.io.File("/tmp"));

        java.io.File tempFile = new java.io.File("/tmp/script." + getExtension(language));
        if ("java".equals(language)) {
            tempFile = new java.io.File("/tmp/Main.java");
        }
        
        java.nio.file.Files.write(tempFile.toPath(), job.getSourceCode().getBytes());

        if ("stdin".equals(language) && job.getStdin() != null && !job.getStdin().isEmpty()) {
            java.io.File inputFile = new java.io.File("/tmp/input.txt");
            java.nio.file.Files.write(inputFile.toPath(), job.getStdin().getBytes());
        }

        long startTime = System.currentTimeMillis();

        Process process = processBuilder.start();

        boolean finished = process.waitFor(10, TimeUnit.SECONDS);

        long executionTime = System.currentTimeMillis() - startTime;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stdout.append(line).append("\n");
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stderr.append(line).append("\n");
            }
        }

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Execution timed out");
        }

        tempFile.delete();

        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("stdout", stdout.toString());
        result.put("stderr", stderr.toString());
        result.put("exitCode", String.valueOf(process.exitValue()));
        result.put("executionTimeMs", String.valueOf(executionTime));
        result.put("memoryUsedKb", "0");

        return result;
    }

    private String getExtension(String language) {
        return switch (language) {
            case "python" -> "py";
            case "javascript", "node" -> "js";
            case "cpp" -> "cpp";
            case "c" -> "c";
            default -> "txt";
        };
    }
}