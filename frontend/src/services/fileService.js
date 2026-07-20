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

export const fileService = {
  // Get file tree for a project
  getFileTree: async (projectId) => {
    const response = await api.get(`/files/tree/${projectId}`);
    return response.data;
  },

  // Get children of a folder
  getChildren: async (projectId, parentPath) => {
    const response = await api.get(`/files/children?projectId=${projectId}&parentPath=${encodeURIComponent(parentPath)}`);
    return response.data;
  },

  // Get all files in project (flat list)
  getFilesByProject: async (projectId) => {
    const response = await api.get(`/files/project/${projectId}`);
    return response.data;
  },

  // Get file content
  getFileContent: async (fileId) => {
    const response = await api.get(`/files/content/${fileId}`);
    return response.data;
  },

  // Get single file
  getFile: async (fileId) => {
    const response = await api.get(`/files/${fileId}`);
    return response.data;
  },

  // Create folder
  createFolder: async (data) => {
    const response = await api.post('/files/folder', data);
    return response.data;
  },

  // Create file
  createFile: async (data) => {
    const response = await api.post('/files', data);
    return response.data;
  },

  // Update file content
  updateFileContent: async (fileId, data) => {
    const response = await api.put(`/files/content/${fileId}`, data);
    return response.data;
  },

  // Rename file/folder
  renameFile: async (fileId, newName) => {
    const response = await api.put(`/files/rename/${fileId}`, { name: newName });
    return response.data;
  },

  // Move file/folder
  moveFile: async (fileId, newPath) => {
    const response = await api.put(`/files/move/${fileId}`, { path: newPath });
    return response.data;
  },

  // Delete file/folder (soft delete)
  deleteFile: async (fileId) => {
    const response = await api.delete(`/files/${fileId}`);
    return response.data;
  },

  // Restore file/folder
  restoreFile: async (fileId) => {
    const response = await api.post(`/files/restore/${fileId}`);
    return response.data;
  },

  // Search files
  searchFiles: async (projectId, keyword) => {
    const response = await api.get(`/files/search?projectId=${projectId}&keyword=${keyword}`);
    return response.data;
  },
};

export default fileService;
