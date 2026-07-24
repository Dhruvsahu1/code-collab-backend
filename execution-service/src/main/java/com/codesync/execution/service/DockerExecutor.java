package com.codesync.execution.service;

import com.codesync.execution.config.LanguageConfig;
import com.codesync.execution.config.LanguageConfig.LanguageDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;

@Service
public class DockerExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DockerExecutor.class);
    private static final long TIMEOUT_SECONDS = 10;

    public static class DockerResult {
        private final String stdout;
        private final String stderr;
        private final int exitCode;
        private final long executionTimeMs;
        private final boolean timedOut;
        private final String error;

        public DockerResult(String stdout, String stderr, int exitCode, long executionTimeMs, boolean timedOut, String error) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
            this.executionTimeMs = executionTimeMs;
            this.timedOut = timedOut;
            this.error = error;
        }

        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public int getExitCode() { return exitCode; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public boolean isTimedOut() { return timedOut; }
        public String getError() { return error; }
    }

    public interface OutputCallback {
        void onOutput(String line, boolean isError);
    }

    public DockerResult execute(String language, String code, String stdin, OutputCallback callback) {
        LanguageDetails config = LanguageConfig.LANGUAGES.get(language.toLowerCase());
        
        if (config == null) {
            return new DockerResult("", "Language not supported: " + language, -1, 0, false, "Language not supported");
        }

        return executeLocally(config, code, stdin, callback);
    }

    private DockerResult executeLocally(LanguageDetails config, String code, String stdin, OutputCallback callback) {
        Path tempDir = null;
        
        try {
            tempDir = Files.createTempDirectory("exec-");
            String fileName = config.getFileName();
            
            if ("Main.java".equals(fileName)) {
                String className = extractJavaClassName(code);
                fileName = className + ".java";
            }
            
            Path codeFile = tempDir.resolve(fileName);
            Files.writeString(codeFile, code);
            
            logger.info("Created temp file: {} with content: {}", codeFile, code);

            StringBuilder stdoutCapture = new StringBuilder();
            StringBuilder stderrCapture = new StringBuilder();

            OutputCallback captureCallback = (line, isError) -> {
                if (isError) {
                    stderrCapture.append(line).append("\n");
                } else {
                    stdoutCapture.append(line).append("\n");
                }
                if (callback != null) {
                    callback.onOutput(line, isError);
                }
            };

            ProcessBuilder pb = buildLocalProcess(fileName, tempDir);
            if (pb == null) {
                return new DockerResult("", "Local execution not available for this language: " + fileName, -1, 0, false, null);
            }
            
            pb.directory(tempDir.toFile());
            pb.redirectErrorStream(false);
            
            logger.info("Executing: command={}, dir={}", pb.command(), pb.directory());
            
            long startTime = System.currentTimeMillis();
            Process p = pb.start();
            
            ExecutorService exec = Executors.newFixedThreadPool(2);
            Future<?> outFut = exec.submit(() -> readStream(p.getInputStream(), false, captureCallback));
            Future<?> errFut = exec.submit(() -> readStream(p.getErrorStream(), true, captureCallback));
            
            boolean finished = p.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long execTime = System.currentTimeMillis() - startTime;
            
            logger.info("Process finished: {}, exitCode: {}, time: {}ms", finished, p.exitValue(), execTime);
            
            if (!finished) {
                p.destroyForcibly();
                exec.shutdownNow();
                return new DockerResult(stdoutCapture.toString(), stderrCapture.toString(), -1, execTime, true, "Execution timed out");
            }
            
            try { outFut.get(2, TimeUnit.SECONDS); } catch (Exception ignored) {}
            try { errFut.get(2, TimeUnit.SECONDS); } catch (Exception ignored) {}
            
            int exitCode = p.exitValue();
            exec.shutdown();
            
            logger.info("Execution complete. stdout: '{}', stderr: '{}', exitCode: {}", 
                stdoutCapture, stderrCapture, exitCode);
            
            return new DockerResult(stdoutCapture.toString(), stderrCapture.toString(), exitCode, execTime, false, null);
            
        } catch (Exception e) {
            logger.error("Local execution failed", e);
            return new DockerResult("", "Execution failed: " + e.getMessage(), -1, 0, false, e.getMessage());
        } finally {
            if (tempDir != null) {
                try { deleteDirectory(tempDir); } catch (Exception ignored) {}
            }
        }
    }

    private ProcessBuilder buildLocalProcess(String fileName, Path tempDir) {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("windows");
        
        logger.info("Building process for: {}, Windows: {}, dir: {}", fileName, isWindows, tempDir);
        
        if (fileName.endsWith(".js")) {
            if (isWindows) {
                return new ProcessBuilder("cmd", "/c", "cd " + tempDir.toString().replace("\\", "/") + " && node " + fileName);
            }
            return new ProcessBuilder("sh", "-c", "cd " + tempDir.toString() + " && node " + fileName);
        } else if (fileName.endsWith(".py")) {
            if (isWindows) {
                return new ProcessBuilder("cmd", "/c", "cd " + tempDir.toString().replace("\\", "/") + " && py " + fileName);
            }
            return new ProcessBuilder("sh", "-c", "cd " + tempDir.toString() + " && python3 " + fileName);
        } else if (fileName.endsWith(".java")) {
            String className = fileName.substring(0, fileName.length() - 5);
            if (isWindows) {
                ProcessBuilder compilePb = new ProcessBuilder("cmd", "/c", 
                    "cd " + tempDir.toString().replace("\\", "/") + " && javac " + fileName);
                compilePb.redirectErrorStream(true);
                try {
                    Process compileProcess = compilePb.start();
                    compileProcess.waitFor();
                } catch (Exception e) {
                    logger.error("Compilation failed", e);
                }
                return new ProcessBuilder("cmd", "/c", "cd " + tempDir.toString().replace("\\", "/") + " && java -cp . " + className);
            }
            return new ProcessBuilder("sh", "-c", "cd " + tempDir.toString() + " && javac " + fileName + " && java " + className);
        } else if (fileName.endsWith(".cpp")) {
            if (isWindows) {
                ProcessBuilder compileCpp = new ProcessBuilder("cmd", "/c", 
                    "cd " + tempDir.toString().replace("\\", "/") + " && g++ " + fileName + " -o main.exe");
                compileCpp.redirectErrorStream(true);
                try {
                    Process cppProcess = compileCpp.start();
                    cppProcess.waitFor();
                } catch (Exception e) {
                    logger.error("C++ compilation failed", e);
                }
                return new ProcessBuilder("cmd", "/c", "cd " + tempDir.toString().replace("\\", "/") + " && main.exe");
            }
            return new ProcessBuilder("sh", "-c", "cd " + tempDir.toString() + " && g++ " + fileName + " -o main && ./main");
        } else if (fileName.endsWith(".c")) {
            if (isWindows) {
                ProcessBuilder compileC = new ProcessBuilder("cmd", "/c", 
                    "cd " + tempDir.toString().replace("\\", "/") + " && gcc " + fileName + " -o main.exe");
                compileC.redirectErrorStream(true);
                try {
                    Process cProcess = compileC.start();
                    cProcess.waitFor();
                } catch (Exception e) {
                    logger.error("C compilation failed", e);
                }
                return new ProcessBuilder("cmd", "/c", "cd " + tempDir.toString().replace("\\", "/") + " && main.exe");
            }
            return new ProcessBuilder("sh", "-c", "cd " + tempDir.toString() + " && gcc " + fileName + " -o main && ./main");
        } else if (fileName.endsWith(".ts")) {
            if (isWindows) {
                ProcessBuilder compileTs = new ProcessBuilder("cmd", "/c", 
                    "cd " + tempDir.toString().replace("\\", "/") + " && npx tsc " + fileName + " --outDir .");
                compileTs.redirectErrorStream(true);
                try {
                    Process tsProcess = compileTs.start();
                    tsProcess.waitFor();
                } catch (Exception e) {
                    logger.error("TS compilation failed", e);
                }
                String jsFileName = fileName.substring(0, fileName.length() - 3) + ".js";
                return new ProcessBuilder("cmd", "/c", "cd " + tempDir.toString().replace("\\", "/") + " && node " + jsFileName);
            }
            String jsFileName = fileName.substring(0, fileName.length() - 3) + ".js";
            return new ProcessBuilder("sh", "-c", "cd " + tempDir.toString() + " && npx tsc " + fileName + " --outDir . && node " + jsFileName);
        }
        
        return null;
    }

    private String extractJavaClassName(String code) {
        if (code == null) return "Main";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("public\\s+class\\s+([A-Za-z0-9_]+)").matcher(code);
        if (m.find()) {
            return m.group(1);
        }
        m = java.util.regex.Pattern.compile("class\\s+([A-Za-z0-9_]+)").matcher(code);
        if (m.find()) {
            return m.group(1);
        }
        return "Main";
    }

    private void readStream(InputStream inputStream, boolean isError, OutputCallback callback) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("Stream {}: {}", isError ? "error" : "output", line);
                if (callback != null) {
                    callback.onOutput(line, isError);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading stream", e);
        }
    }

    private void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try { Files.delete(path); } catch (IOException ignored) {}
                });
        } catch (IOException ignored) {}
    }
}