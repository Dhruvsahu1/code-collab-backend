/**
 * Store Tests (Zustand)
 * 
 * Tests verify:
 * - Store state initialization
 * - State mutations (login, logout, actions)
 * - Persistence behavior
 * - Async actions work correctly
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuthStore, useProjectStore, useEditorStore } from '../../../store';

// Mock API
vi.mock('../../../services/api', () => ({
  authAPI: {
    login: vi.fn(),
    register: vi.fn(),
    getProfile: vi.fn(),
  },
  projectAPI: {
    getByOwner: vi.fn(),
  },
}));

import { authAPI, projectAPI } from '../../../services/api';

describe('Auth Store', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset store state
    useAuthStore.setState({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
    });
    localStorage.clear();
  });

  it('should initialize with default values', () => {
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().token).toBeNull();
  });

  it('should login successfully', async () => {
    const mockResponse = {
      data: {
        token: 'mock-token',
        refreshToken: 'mock-refresh',
        user: { id: 1, name: 'Test User', email: 'test@example.com' },
      },
    };

    authAPI.login.mockResolvedValue(mockResponse);

    const result = await useAuthStore.getState().login('test@example.com', 'password');

    expect(result.success).toBe(true);
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().user).toEqual(mockResponse.data.user);
  });

  it('should handle login failure', async () => {
    const error = new Error('Invalid credentials');
    error.response = { data: { message: 'Invalid credentials' } };
    authAPI.login.mockRejectedValue(error);

    const result = await useAuthStore.getState().login('wrong@example.com', 'wrong');

    expect(result.success).toBe(false);
    expect(result.error).toBe('Invalid credentials');
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });

  it('should logout and clear state', () => {
    // Set authenticated state first
    useAuthStore.setState({
      user: { id: 1, name: 'Test' },
      token: 'token',
      refreshToken: 'refresh',
      isAuthenticated: true,
    });

    useAuthStore.getState().logout();

    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().user).toBeNull();
    expect(useAuthStore.getState().token).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });
});

describe('Project Store', () => {
  beforeEach(() => {
    useProjectStore.setState({
      projects: [],
      currentProject: null,
      files: [],
      currentFile: null,
      isLoading: false,
    });
  });

  it('should initialize with default values', () => {
    expect(useProjectStore.getState().projects).toEqual([]);
    expect(useProjectStore.getState().currentProject).toBeNull();
    expect(useProjectStore.getState().isLoading).toBe(false);
  });

  it('should set current project', () => {
    const project = { projectId: 1, name: 'Test Project' };
    
    useProjectStore.getState().setCurrentProject(project);
    
    expect(useProjectStore.getState().currentProject).toEqual(project);
  });

  it('should set current file', () => {
    const file = { fileId: 1, name: 'test.js' };
    
    useProjectStore.getState().setCurrentFile(file);
    
    expect(useProjectStore.getState().currentFile).toEqual(file);
  });

  it('should clear files', () => {
    useProjectStore.setState({ files: [{ fileId: 1 }], currentFile: { fileId: 1 } });
    
    useProjectStore.getState().clearFiles();
    
    expect(useProjectStore.getState().files).toEqual([]);
    expect(useProjectStore.getState().currentFile).toBeNull();
  });
});

describe('Editor Store', () => {
  beforeEach(() => {
    useEditorStore.setState({
      code: '',
      language: 'javascript',
      theme: 'vs-dark',
      isDirty: false,
      collaborators: [],
      cursorPosition: { line: 1, column: 1 },
    });
  });

  it('should update code and mark as dirty', () => {
    useEditorStore.getState().setCode('const x = 1;');
    
    expect(useEditorStore.getState().code).toBe('const x = 1;');
    expect(useEditorStore.getState().isDirty).toBe(true);
  });

  it('should change language', () => {
    useEditorStore.getState().setLanguage('python');
    
    expect(useEditorStore.getState().language).toBe('python');
  });

  it('should mark clean after saving', () => {
    useEditorStore.setState({ isDirty: true });
    
    useEditorStore.getState().markClean();
    
    expect(useEditorStore.getState().isDirty).toBe(false);
  });

  it('should update cursor position', () => {
    useEditorStore.getState().setCursorPosition({ line: 10, column: 5 });
    
    expect(useEditorStore.getState().cursorPosition).toEqual({ line: 10, column: 5 });
  });
});