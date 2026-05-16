/**
 * Test Fixtures
 * 
 * Factory functions for creating consistent mock data across tests.
 * Each factory creates realistic test data with sensible defaults.
 */

// User fixture
export const createUserFixture = (overrides = {}) => ({
  id: 1,
  name: 'John Doe',
  username: 'johndoe',
  email: 'john@example.com',
  avatar: null,
  ...overrides,
});

// Project fixture
export const createProjectFixture = (overrides = {}) => ({
  projectId: 1,
  name: 'My Project',
  description: 'A collaborative coding project',
  language: 'javascript',
  visibility: 'PRIVATE',
  ownerId: 1,
  starCount: 5,
  forkCount: 2,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
  ...overrides,
});

// File fixture
export const createFileFixture = (overrides = {}) => ({
  fileId: 1,
  name: 'index.js',
  path: '/index.js',
  content: 'console.log("Hello World");',
  isFolder: false,
  projectId: 1,
  ...overrides,
});

// Folder fixture
export const createFolderFixture = (overrides = {}) => ({
  fileId: 2,
  name: 'src',
  path: '/src',
  isFolder: true,
  projectId: 1,
  children: [],
  ...overrides,
});

// Execution job fixture
export const createJobFixture = (overrides = {}) => ({
  jobId: 'job-123',
  status: 'PENDING',
  language: 'javascript',
  output: '',
  error: null,
  createdAt: new Date().toISOString(),
  ...overrides,
});

// Snapshot fixture
export const createSnapshotFixture = (overrides = {}) => ({
  snapshotId: 1,
  message: 'Initial commit',
  authorId: 1,
  authorName: 'John Doe',
  content: 'const x = 1;',
  createdAt: new Date().toISOString(),
  ...overrides,
});

// Comment fixture
export const createCommentFixture = (overrides = {}) => ({
  commentId: 1,
  content: 'This is a comment',
  authorId: 1,
  authorName: 'John Doe',
  fileId: 1,
  line: 10,
  resolved: false,
  createdAt: new Date().toISOString(),
  ...overrides,
});

// Collaborator fixture
export const createCollaboratorFixture = (overrides = {}) => ({
  userId: 1,
  username: 'collaborator',
  color: '#22d3ee',
  role: 'EDITOR',
  ...overrides,
});