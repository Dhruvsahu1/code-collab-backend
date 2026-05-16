package com.codesync.execution.config;

import java.util.HashMap;
import java.util.Map;

public class LanguageConfig {
    
    public static final Map<String, LanguageDetails> LANGUAGES = new HashMap<>();
    
    static {
        LANGUAGES.put("java", new LanguageDetails(
            "openjdk:17",
            "javac Main.java",
            "java Main",
            "Main.java"
        ));
        
        LANGUAGES.put("python", new LanguageDetails(
            "python:3.10",
            null,
            "python3 main.py",
            "main.py"
        ));
        
        LANGUAGES.put("python3", new LanguageDetails(
            "python:3.10",
            null,
            "python3 main.py",
            "main.py"
        ));
        
        LANGUAGES.put("javascript", new LanguageDetails(
            "node:18",
            null,
            "node main.js",
            "main.js"
        ));
        
        LANGUAGES.put("js", new LanguageDetails(
            "node:18",
            null,
            "node main.js",
            "main.js"
        ));
        
        LANGUAGES.put("node", new LanguageDetails(
            "node:18",
            null,
            "node main.js",
            "main.js"
        ));
        
        LANGUAGES.put("typescript", new LanguageDetails(
            "node:18",
            "npx tsc main.ts --outDir . 2>&1 || true",
            "node main.js",
            "main.ts"
        ));
        
        LANGUAGES.put("ts", new LanguageDetails(
            "node:18",
            "npx tsc main.ts --outDir . 2>&1 || true",
            "node main.js",
            "main.ts"
        ));
        
        LANGUAGES.put("cpp", new LanguageDetails(
            "gcc:latest",
            "g++ main.cpp -o main",
            "./main",
            "main.cpp"
        ));
        
        LANGUAGES.put("c++", new LanguageDetails(
            "gcc:latest",
            "g++ main.cpp -o main",
            "./main",
            "main.cpp"
        ));
        
        LANGUAGES.put("c", new LanguageDetails(
            "gcc:latest",
            "gcc main.c -o main",
            "./main",
            "main.c"
        ));
    }
    
    public static class LanguageDetails {
        private final String dockerImage;
        private final String compileCommand;
        private final String runCommand;
        private final String fileName;
        
        public LanguageDetails(String dockerImage, String compileCommand, String runCommand, String fileName) {
            this.dockerImage = dockerImage;
            this.compileCommand = compileCommand;
            this.runCommand = runCommand;
            this.fileName = fileName;
        }
        
        public String getDockerImage() { return dockerImage; }
        public String getCompileCommand() { return compileCommand; }
        public String getRunCommand() { return runCommand; }
        public String getFileName() { return fileName; }
        public boolean requiresCompilation() { return compileCommand != null && !compileCommand.isEmpty(); }
    }
}