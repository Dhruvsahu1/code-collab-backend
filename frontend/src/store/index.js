import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authAPI, projectAPI } from '../services/api';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,

      login: async (email, password) => {
        set({ isLoading: true });
        try {
          const response = await authAPI.login({ email, password });
          const { token, refreshToken, user } = response.data;
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', refreshToken);
          set({ user, token, refreshToken, isAuthenticated: true, isLoading: false });
          return { success: true };
        } catch (error) {
          set({ isLoading: false });
          return { success: false, error: error.response?.data?.message || 'Login failed' };
        }
      },

      register: async (data) => {
        set({ isLoading: true });
        try {
          const response = await authAPI.register(data);
          const { token, refreshToken, user } = response.data;
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', refreshToken);
          set({ user, token, refreshToken, isAuthenticated: true, isLoading: false });
          return { success: true };
        } catch (error) {
          set({ isLoading: false });
          return { success: false, error: error.response?.data?.message || 'Registration failed' };
        }
      },

      logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        set({ user: null, token: null, refreshToken: null, isAuthenticated: false });
      },

      fetchProfile: async () => {
        try {
          const response = await authAPI.getProfile();
          set({ user: response.data });
        } catch (error) {
          console.error('Failed to fetch profile:', error);
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

export const useProjectStore = create((set, get) => ({
  projects: [],
  currentProject: null,
  files: [],
  currentFile: null,
  isLoading: false,

  fetchProjects: async () => {
    set({ isLoading: true });
    try {
      const user = useAuthStore.getState().user;
      if (user) {
        const response = await projectAPI.getByOwner(user.id);
        set({ projects: response.data.content || [], isLoading: false });
      }
    } catch (error) {
      set({ isLoading: false });
    }
  },

  fetchPublicProjects: async (page = 0) => {
    set({ isLoading: true });
    try {
      const response = await projectAPI.getPublic(page);
      set({ projects: response.data.content || [], isLoading: false });
    } catch (error) {
      set({ isLoading: false });
    }
  },

  setCurrentProject: (project) => set({ currentProject: project }),
  setCurrentFile: (file) => set({ currentFile: file }),
  clearFiles: () => set({ files: [], currentFile: null }),
}));

export const useEditorStore = create((set) => ({
  code: '',
  language: 'javascript',
  theme: 'vs-dark',
  isDirty: false,
  collaborators: [],
  cursorPosition: { line: 1, column: 1 },

  setCode: (code) => set({ code, isDirty: true }),
  setLanguage: (language) => set({ language }),
  setTheme: (theme) => set({ theme }),
  markClean: () => set({ isDirty: false }),
  setCursorPosition: (position) => set({ cursorPosition: position }),
  setCollaborators: (collaborators) => set({ collaborators }),
  addCollaborator: (user) => set((state) => ({ 
    collaborators: [...state.collaborators, user] 
  })),
  removeCollaborator: (userId) => set((state) => ({ 
    collaborators: state.collaborators.filter(c => c.id !== userId) 
  })),
}));

export const useExecutionStore = create((set) => ({
  output: '',
  isRunning: false,
  currentJob: null,
  history: [],

  setOutput: (output) => set({ output }),
  setRunning: (isRunning) => set({ isRunning }),
  setCurrentJob: (job) => set({ currentJob: job }),
  addToHistory: (job) => set((state) => ({ 
    history: [job, ...state.history].slice(0, 50) 
  })),
  clearOutput: () => set({ output: '' }),
}));

import { versionAPI } from '../services/api';

export const useVersionStore = create((set) => ({
  snapshots: [],
  currentDiff: null,
  isLoading: false,
  error: null,
  
  fetchFileHistory: async (fileId) => {
    set({ isLoading: true, error: null });
    try {
      const response = await versionAPI.getFileHistory(fileId);
      set({ snapshots: response.data, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error.message });
    }
  },
  
  fetchProjectHistory: async (projectId) => {
    set({ isLoading: true, error: null });
    try {
      const response = await versionAPI.getProjectHistory(projectId);
      set({ snapshots: response.data, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error.message });
    }
  },
  
  setSnapshots: (snapshots) => set({ snapshots }),
  setCurrentDiff: (diff) => set({ currentDiff: diff }),
  
  createSnapshot: async (data) => {
    try {
      const response = await versionAPI.createSnapshot(data);
      return response.data;
    } catch (error) {
      console.error('Error creating snapshot:', error);
      throw error;
    }
  },
  
  restoreSnapshot: async (snapshotId, authorId, message) => {
    try {
      const response = await versionAPI.restoreSnapshot({ snapshotId, authorId, message });
      return response.data;
    } catch (error) {
      console.error('Error restoring snapshot:', error);
      throw error;
    }
  },
  
  tagSnapshot: async (snapshotId, tag) => {
    try {
      const response = await versionAPI.tagSnapshot(snapshotId, tag);
      return response.data;
    } catch (error) {
      console.error('Error tagging snapshot:', error);
      throw error;
    }
  },

  clearSnapshots: () => set({ snapshots: [], currentDiff: null }),
}));