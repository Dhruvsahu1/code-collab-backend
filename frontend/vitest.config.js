import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

/**
 * Vitest Configuration
 * 
 * Vitest is a Vite-native test runner that provides:
 * - Fast execution with native ES modules
 * - Jest-compatible APIs
 * - Built-in coverage via @vitest/coverage-v8
 * - Watch mode optimization
 */
export default defineConfig({
  plugins: [react()],
  test: {
    // Use jsdom environment for DOM testing
    environment: 'jsdom',
    
    // Setup files run before each test file
    setupFiles: ['./src/tests/setupTests.js'],
    
    // Global test configuration
    globals: true,
    css: true,
    
    // File patterns for tests
    include: [
      'src/**/*.{test,spec}.{js,jsx,ts,tsx}',
      'src/tests/**/*.{test,spec}.{js,jsx,ts,tsx}',
    ],
    
    // Exclude patterns - exclude e2e tests from vitest
    exclude: [
      'node_modules/',
      'dist/',
      'e2e/',
      'src/tests/e2e/**',
    ],
    
    // Coverage configuration
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      reportsDirectory: './coverage',
      exclude: [
        'node_modules/',
        'src/main.jsx',
        'src/tests/',
        '**/*.d.ts',
        '**/*.config.{js,ts}',
        '**/index.css',
        'src/vite-env.d.ts',
      ],
      include: ['src/**/*'],
      all: true,
    },
    
    // Mock configuration
    mockReset: true,
    restoreMocks: true,
    
    // Retry configuration for flaky test protection
    retry: 1,
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
});