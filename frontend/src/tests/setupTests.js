/**
 * Test Setup File
 * 
 * This file runs before each test and configures:
 * - jest-dom matchers for extended DOM assertions
 * - MSW (Mock Service Worker) for API mocking
 * - Global fetch mock for whatwg-fetch
 * - Custom matchers and utilities
 * - Framer-motion mock for animation tests
 */

import '@testing-library/jest-dom';
import React from 'react';
import { beforeAll, afterEach, afterAll, vi } from 'vitest';
import { server } from './mocks/server';

// Polyfill for fetch - required for whatwg-fetch
import 'whatwg-fetch';

// Create a simple object store for localStorage mock
const store = {};

const localStorageMock = {
  getItem: vi.fn((key) => store[key] || null),
  setItem: vi.fn((key, value) => {
    store[key] = value;
  }),
  removeItem: vi.fn((key) => {
    delete store[key];
  }),
  clear: vi.fn(() => {
    Object.keys(store).forEach(key => delete store[key]);
  }),
};

global.localStorage = localStorageMock;

// Mock window.location
delete window.location;
window.location = { href: '' };

// Mock matchMedia for responsive components
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// Mock IntersectionObserver for components that use it
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock framer-motion to avoid animation issues in tests
vi.mock('framer-motion', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    motion: {
      div: ({ children, ...props }) => React.createElement('div', props, children),
      aside: ({ children, ...props }) => React.createElement('aside', props, children),
      nav: ({ children, ...props }) => React.createElement('nav', props, children),
      button: ({ children, ...props }) => React.createElement('button', props, children),
      span: ({ children, ...props }) => React.createElement('span', props, children),
      h1: ({ children, ...props }) => React.createElement('h1', props, children),
      h2: ({ children, ...props }) => React.createElement('h2', props, children),
      h3: ({ children, ...props }) => React.createElement('h3', props, children),
      p: ({ children, ...props }) => React.createElement('p', props, children),
    },
    AnimatePresence: ({ children }) => children,
  };
});

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    loading: vi.fn(),
  },
  Toaster: () => null,
  default: {
    toast: {
      success: vi.fn(),
      error: vi.fn(),
      loading: vi.fn(),
    },
    Toaster: () => null,
  },
}));

// MSW Server Setup
beforeAll(() => server.listen({ onUnhandledRequest: 'warn' }));
afterEach(() => {
  server.resetHandlers();
  Object.keys(store).forEach(key => delete store[key]);
});
afterAll(() => server.close());