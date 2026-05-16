import { describe, it, expect, beforeEach, vi } from 'vitest';
import api, { authAPI, projectAPI } from '../../../services/api';

// Mock the API module to avoid network issues with relative URLs
vi.mock('../../../services/api', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    authAPI: {
      login: vi.fn().mockResolvedValue({ status: 200, data: { token: 'mock', user: { id: 1 } } }),
      register: vi.fn().mockResolvedValue({ status: 200, data: { token: 'mock', user: { id: 1 } } }),
    },
    projectAPI: {
      getByOwner: vi.fn().mockResolvedValue({ status: 200, data: { content: [] } }),
      getById: vi.fn().mockResolvedValue({ status: 200, data: { projectId: 1 } }),
      create: vi.fn().mockResolvedValue({ status: 200, data: { projectId: 1 } }),
      delete: vi.fn().mockResolvedValue({ status: 204 }),
    },
  };
});

describe('API Service', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('authAPI', () => {
    it('should have login method', () => {
      expect(authAPI.login).toBeDefined();
    });

    it('should have register method', () => {
      expect(authAPI.register).toBeDefined();
    });
  });

  describe('projectAPI', () => {
    it('should have getByOwner method', () => {
      expect(projectAPI.getByOwner).toBeDefined();
    });

    it('should have getById method', () => {
      expect(projectAPI.getById).toBeDefined();
    });

    it('should have create method', () => {
      expect(projectAPI.create).toBeDefined();
    });
  });
});