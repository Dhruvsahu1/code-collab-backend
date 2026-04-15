import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
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
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });
          const { token, refreshToken: newRefreshToken } = response.data;
          localStorage.setItem('token', token);
          localStorage.setItem('refreshToken', newRefreshToken);
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        } catch (refreshError) {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
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
  getByOwner: (ownerId, page = 0, size = 20) =>
    api.get(`/projects/owner/${ownerId}?page=${page}&size=${size}`),
  getPublic: (page = 0, size = 20) =>
    api.get(`/projects/public?page=${page}&size=${size}`),
  search: (keyword, page = 0, size = 20) =>
    api.get(`/projects/search?keyword=${keyword}&page=${page}&size=${size}`),
  getByMember: (userId) => api.get(`/projects/member/${userId}`),
  update: (id, data) => api.put(`/projects/${id}`, data),
  archive: (id) => api.put(`/projects/archive/${id}`),
  star: (id) => api.put(`/projects/star/${id}`),
  delete: (id) => api.delete(`/projects/${id}`),
};

export const fileAPI = {
  create: (data) => api.post('/files', data),
  createFolder: (data) => api.post('/files/folder', data),
  getById: (fileId) => api.get(`/files/${fileId}`),
  getByProject: (projectId) => api.get(`files/project/${projectId}`),
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
  createSession: (data) => api.post('/collab/sessions', data),
  getSession: (sessionId) => api.get(`/collab/sessions/${sessionId}`),
  getActiveSession: (fileId) => api.get(`/collab/sessions/file/${fileId}`),
  updateCode: (sessionId, code) => api.put(`/collab/sessions/${sessionId}`, { code }),
  closeSession: (sessionId) => api.delete(`/collab/sessions/${sessionId}`),
};

export const executionAPI = {
  run: (data) => api.post('/execution/run', data),
  getJob: (jobId) => api.get(`/execution/jobs/${jobId}`),
  completeJob: (jobId, output, error) =>
    api.post(`/execution/jobs/${jobId}/complete`, { output, error }),
  cancelJob: (jobId) => api.post(`/execution/jobs/${jobId}/cancel`),
};

export const commentAPI = {
  create: (data) => api.post('/comments', data),
  getByFile: (fileId) => api.get(`/comments/file/${fileId}`),
  update: (id, content) => api.put(`/comments/${id}`, { content }),
  delete: (id) => api.delete(`/comments/${id}`),
  resolve: (id) => api.post(`/comments/${id}/resolve`),
  reply: (id, data) => api.post(`/comments/${id}/replies`, data),
};

export default api;