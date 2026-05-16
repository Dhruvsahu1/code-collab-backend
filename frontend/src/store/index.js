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
          const response = await authAPI.login({ usernameOrEmail: email, password });
          const data = response.data;
          // Auth service returns accessToken (not token) and user.userId (not user.id)
          const token = data.token || data.accessToken;
          const refreshToken = data.refreshToken;
          const rawUser = data.user;
          // Normalize user object: ensure both 'id' and 'userId' exist
          const user = rawUser ? {
            ...rawUser,
            id: rawUser.id || rawUser.userId,
            userId: rawUser.userId || rawUser.id,
            name: rawUser.name || rawUser.fullName || rawUser.username,
          } : null;
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
          const respData = response.data;
          const token = respData.token || respData.accessToken;
          const refreshToken = respData.refreshToken;
          const rawUser = respData.user;
          const user = rawUser ? {
            ...rawUser,
            id: rawUser.id || rawUser.userId,
            userId: rawUser.userId || rawUser.id,
            name: rawUser.name || rawUser.fullName || rawUser.username,
          } : null;
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
          const rawUser = response.data;
          const user = rawUser ? {
            ...rawUser,
            id: rawUser.id || rawUser.userId,
            userId: rawUser.userId || rawUser.id,
            name: rawUser.name || rawUser.fullName || rawUser.username,
          } : null;
          set({ user });
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

  // Fetch ALL projects accessible to the user (owned + collaborated)
  fetchProjects: async () => {
    set({ isLoading: true });
    try {
      const user = useAuthStore.getState().user;
      if (!user) {
        set({ projects: [], isLoading: false });
        return;
      }
      const userId = user.id || user.userId;
      
      // Fetch owned projects
      let ownedProjects = [];
      try {
        const ownedRes = await projectAPI.getByOwner(userId);
        ownedProjects = ownedRes.data?.content || ownedRes.data || [];
      } catch (e) {
        console.error('Failed to fetch owned projects:', e);
      }

      // Fetch collaborated projects (where user is a member)
      let memberProjects = [];
      try {
        const memberRes = await projectAPI.getByMember();
        memberProjects = memberRes.data || [];
      } catch (e) {
        console.error('Failed to fetch member projects:', e);
      }

      // Merge and deduplicate by projectId
      const allProjects = [...ownedProjects];
      const ownedIds = new Set(ownedProjects.map(p => p.projectId));
      for (const p of memberProjects) {
        if (!ownedIds.has(p.projectId)) {
          allProjects.push(p);
        }
      }

      set({ projects: allProjects, isLoading: false });
    } catch (error) {
      console.error('fetchProjects error:', error);
      set({ isLoading: false });
    }
  },

  fetchPublicProjects: async (page = 0) => {
    set({ isLoading: true });
    try {
      const response = await projectAPI.getPublic(page);
      set({ projects: response.data?.content || response.data || [], isLoading: false });
    } catch (error) {
      console.error('fetchPublicProjects error:', error);
      set({ isLoading: false });
    }
  },

  searchProjects: async (keyword, page = 0) => {
    set({ isLoading: true });
    try {
      const response = await projectAPI.search(keyword, page);
      set({ projects: response.data?.content || response.data || [], isLoading: false });
    } catch (error) {
      console.error('searchProjects error:', error);
      set({ isLoading: false });
    }
  },

  fetchProjectsByLanguage: async (lang, page = 0) => {
    set({ isLoading: true });
    try {
      const response = await projectAPI.getByLanguage(lang, page);
      set({ projects: response.data?.content || response.data || [], isLoading: false });
    } catch (error) {
      console.error('fetchProjectsByLanguage error:', error);
      set({ isLoading: false });
    }
  },

  toggleStar: async (projectId) => {
    try {
      const response = await projectAPI.toggleStar(projectId);
      const isStarred = response.data?.starred;
      // Update the project's star count in local state
      set((state) => ({
        projects: state.projects.map(p =>
          p.projectId === projectId
            ? { ...p, starCount: isStarred ? (p.starCount || 0) + 1 : Math.max(0, (p.starCount || 0) - 1) }
            : p
        ),
      }));
      return isStarred;
    } catch (error) {
      console.error('toggleStar error:', error);
      throw error;
    }
  },

  forkProject: async (projectId) => {
    try {
      const response = await projectAPI.fork(projectId);
      return response.data;
    } catch (error) {
      console.error('forkProject error:', error);
      throw error;
    }
  },

  deleteProject: async (projectId) => {
    try {
      await projectAPI.delete(projectId);
      // Remove from local state
      set((state) => ({
        projects: state.projects.filter(p => p.projectId !== projectId),
      }));
    } catch (error) {
      console.error('deleteProject error:', error);
      throw error;
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

export const useCollabStore = create((set, get) => ({
  sessionId: null,
  participants: [],
  cursors: {},
  isConnected: false,
  myColor: null,

  setSessionId: (sessionId) => set({ sessionId }),
  setConnected: (connected) => set({ isConnected: connected }),
  setMyColor: (color) => set({ myColor: color }),

  addParticipant: (participant) => set((state) => ({
    participants: [...state.participants.filter(p => p.userId !== participant.userId), participant]
  })),

  removeParticipant: (userId) => set((state) => ({
    participants: state.participants.filter(p => p.userId !== userId),
    cursors: Object.fromEntries(Object.entries(state.cursors).filter(([key]) => key !== String(userId)))
  })),

  updateCursor: (userId, cursor) => set((state) => ({
    cursors: {
      ...state.cursors,
      [userId]: cursor
    }
  })),

  clearCollab: () => set({
    sessionId: null,
    participants: [],
    cursors: {},
    isConnected: false,
    myColor: null
  }),
}));

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