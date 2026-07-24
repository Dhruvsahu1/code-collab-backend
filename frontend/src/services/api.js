import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = sessionStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });
          const token = response.data?.token ?? response.data?.accessToken;
          const newRefreshToken = response.data?.refreshToken;
          sessionStorage.setItem('token', token);
          sessionStorage.setItem('refreshToken', newRefreshToken);
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        } catch (refreshError) {
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  getProfile: () => api.get('/auth/profile'),
  updateProfile: (data) => api.put('/auth/profile', data),
  changePassword: (data) => api.put('/auth/password', data),
  searchUsers: (query, page = 0, size = 20) =>
    api.get(`/auth/search?query=${query}&page=${page}&size=${size}`),
};

export const projectAPI = {
  create: (data) => api.post('/projects', data),
  getById: (id) => api.get(`/projects/${id}`),
  getDashboard: () => api.get('/projects/dashboard'),
  getByOwner: (ownerId, page = 0, size = 20) =>
    api.get(`/projects/owner?ownerId=${ownerId}&page=${page}&size=${size}`),
  getPublic: (page = 0, size = 20) =>
    api.get(`/projects/public?page=${page}&size=${size}`),
  search: (keyword, page = 0, size = 20) =>
    api.get(`/projects/search?keyword=${keyword}&page=${page}&size=${size}`),
  getByMember: () => api.get('/projects/member'),
  getByLanguage: (lang, page = 0, size = 20) =>
    api.get(`/projects/language?lang=${lang}&page=${page}&size=${size}`),
  update: (id, data) => api.put(`/projects/${id}`, data),
  archive: (id) => api.put(`/projects/${id}/archive`),
  toggleStar: (id) => api.put(`/projects/${id}/star`),
  isStarred: (id) => api.get(`/projects/${id}/star`),
  fork: (id) => api.post(`/projects/fork/${id}`),
  delete: (id) => api.delete(`/projects/${id}`),
  // Collaborator endpoints
  addCollaborator: (projectId, userId) =>
    api.post(`/projects/${projectId}/collaborators`, { userId }),
  removeCollaborator: (projectId, userId) =>
    api.delete(`/projects/${projectId}/collaborators/${userId}`),
  getCollaborators: (projectId) =>
    api.get(`/projects/${projectId}/collaborators`),
};

export const fileAPI = {
  create: (data) => api.post('/files', data),
  createFolder: (data) => api.post('/files/folder', data),
  getById: (fileId) => api.get(`/files/${fileId}`),
  getByProject: (projectId) => api.get(`/files/project/${projectId}`),
  getContent: (fileId) => api.get(`/files/content/${fileId}`),
  getTree: (projectId) => api.get(`/files/tree/${projectId}`),
  search: (projectId, keyword) => api.get(`/files/search?projectId=${projectId}&keyword=${keyword}`),
  updateContent: (fileId, data) => api.put(`/files/content/${fileId}`, data),
  rename: (fileId, name) => api.put(`/files/rename/${fileId}`, { name }),
  move: (fileId, path) => api.put(`/files/move/${fileId}`, { path }),
  delete: (fileId) => api.delete(`/files/${fileId}`),
  restore: (fileId) => api.post(`/files/restore/${fileId}`),
};

export const collabAPI = {
  createSession: (data) => api.post('/sessions', data),
  getSession: (sessionId) => api.get(`/sessions/${sessionId}`),
  getActiveSession: (fileId) => api.get(`/sessions/file/${fileId}`),
  updateCode: (sessionId, code) => api.put(`/sessions/${sessionId}`, { code }),
  closeSession: (sessionId, requesterId) => api.delete(`/sessions/${sessionId}?requesterId=${requesterId}`),
};

export const executionAPI = {
  run: (data) => api.post('/executions/run', data),
  getJob: (jobId) => api.get(`/executions/jobs/${jobId}`),
  getResult: (jobId) => api.get(`/executions/result/${jobId}`),
  completeJob: (jobId, output, error) =>
    api.post(`/executions/jobs/${jobId}/complete`, { output, error }),
  cancelJob: (jobId) => api.post(`/executions/jobs/${jobId}/cancel`),
};

export const commentAPI = {
  create: (data) => api.post('/comments', data),
  getByFile: (fileId) => api.get(`/comments/file/${fileId}`),
  getByProject: (projectId) => api.get(`/comments/project/${projectId}`),
  getById: (id) => api.get(`/comments/${id}`),
  getReplies: (parentId) => api.get(`/comments/replies/${parentId}`),
  update: (id, content, authorId) => api.put(`/comments/${id}`, { content, authorId }),
  delete: (id) => api.delete(`/comments/${id}`),
  resolve: (id) => api.put(`/comments/resolve/${id}`),
  unresolve: (id) => api.put(`/comments/unresolve/${id}`),
  getByLine: (fileId, line) => api.get(`/comments/line`, { params: { fileId, line } }),
  getCount: (fileId) => api.get(`/comments/count/${fileId}`),
  getUnresolved: (fileId) => api.get(`/comments/unresolved/${fileId}`),
};

export const chatAPI = {
  getHistory: (projectId) => api.get(`/chats/${projectId}`),
  getRecentMessages: (projectId, size = 50) =>
    api.get(`/chats/${projectId}/recent?size=${size}`),
  getOlderMessages: (projectId, timestamp, size = 20) =>
    api.get(`/chats/${projectId}/older?timestamp=${timestamp}&size=${size}`),
  getMessagesPaginated: (projectId, page = 0, size = 20) =>
    api.get(`/chats/${projectId}/messages?page=${page}&size=${size}`),
  getCollaboratorCount: (projectId) =>
    api.get(`/chats/${projectId}/collaborator-count`),
  getOnlineCount: (projectId) =>
    api.get(`/chats/${projectId}/online-count`),
};

export const versionAPI = {
  createSnapshot: (data) => api.post('/versions', data),
  getById: (id) => api.get(`/versions/${id}`),
  getByFile: (fileId) => api.get(`/versions/file/${fileId}`),
  getByProject: (projectId) => api.get(`/versions/project/${projectId}`),
  getFileHistory: (fileId) => api.get(`/versions/file/${fileId}/history`),
  getProjectHistory: (projectId) => api.get(`/versions/project/${projectId}/history`),
  getBranchHistory: (projectId, branch) => 
    api.get(`/versions/project/${projectId}/branch/${branch}/history`),
  getLatestSnapshot: (fileId, branch) => 
    api.get(`/versions/file/${fileId}/latest`, { params: { branch } }),
  restoreSnapshot: (data) => api.post('/versions/restore', data),
  diffSnapshots: (fileId, snapshot1Id, snapshot2Id) =>
    api.get(`/versions/diff/file/${fileId}`, {
      params: { snapshot1Id, snapshot2Id }
    }),
  diffBetweenSnapshots: (snapshot1Id, snapshot2Id) =>
    api.get(`/versions/diff/snapshots/${snapshot1Id}/${snapshot2Id}`),
  getBranches: (projectId) => api.get(`/versions/branches/project/${projectId}`),
  createBranch: (data) => api.post('/versions/branch', data),
  branchExists: (projectId, branch) =>
    api.get(`/versions/branch/exists`, { params: { projectId, branch } }),
  tagSnapshot: (snapshotId, tag) => api.post('/versions/tag', { snapshotId, tag }),
  getSnapshotByTag: (projectId, fileId, tag) =>
    api.get(`/versions/tag/project/${projectId}/file/${fileId}`, { params: { tag } }),
};

export default api;