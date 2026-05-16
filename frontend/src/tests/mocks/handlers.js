/**
 * MSW Request Handlers
 * 
 * MSW v2 uses `http` instead of `rest` for defining handlers.
 * Each handler simulates different API states:
 * - Success responses with realistic data
 * - Error responses (400, 401, 403, 404, 500)
 * - Loading states via delays
 * - Network timeouts
 */

import { http, HttpResponse } from 'msw';

// Mock user data factory
const createMockUser = (overrides = {}) => ({
  id: 1,
  name: 'Test User',
  username: 'testuser',
  email: 'test@example.com',
  ...overrides,
});

// Mock project data factory
const createMockProject = (overrides = {}) => ({
  projectId: 1,
  name: 'Test Project',
  description: 'A test project for code collaboration',
  language: 'javascript',
  visibility: 'PRIVATE',
  ownerId: 1,
  starCount: 0,
  forkCount: 0,
  createdAt: new Date().toISOString(),
  ...overrides,
});

// Mock token response
const createMockTokens = () => ({
  token: 'mock-jwt-token-' + Math.random().toString(36).substring(7),
  refreshToken: 'mock-refresh-token-' + Math.random().toString(36).substring(7),
  user: createMockUser(),
});

// Get the base URL from environment or use default
const BASE_URL = 'http://localhost';

export const handlers = [
  // Auth API Handlers
  http.post(`${BASE_URL}/api/auth/login`, async ({ request }) => {
    const body = await request.json();
    const { password } = body;
    
    // Simulate invalid credentials
    if (password === 'wrongpassword') {
      return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
    }
    
    // Simulate slow network
    if (body?.email === 'slow@example.com') {
      await new Promise(resolve => setTimeout(resolve, 1000));
      return HttpResponse.json(createMockTokens());
    }
    
    await new Promise(resolve => setTimeout(resolve, 100));
    return HttpResponse.json(createMockTokens());
  }),

  http.post(`${BASE_URL}/api/auth/register`, async () => {
    await new Promise(resolve => setTimeout(resolve, 100));
    return HttpResponse.json(createMockTokens());
  }),

  http.get(`${BASE_URL}/api/auth/profile`, () => {
    return HttpResponse.json(createMockUser());
  }),

  http.put(`${BASE_URL}/api/auth/profile`, async ({ request }) => {
    const body = await request.json();
    await new Promise(resolve => setTimeout(resolve, 100));
    return HttpResponse.json(createMockUser(body));
  }),

  // Project API Handlers
  http.get(`${BASE_URL}/api/projects/owner`, ({ request }) => {
    const url = new URL(request.url);
    const ownerId = url.searchParams.get('ownerId');
    
    // Simulate server error for specific owner
    if (ownerId === 'error') {
      return HttpResponse.json({ message: 'Server error' }, { status: 500 });
    }
    
    return HttpResponse.json({
      content: [
        createMockProject({ projectId: 1, name: 'Project Alpha' }),
        createMockProject({ projectId: 2, name: 'Project Beta' }),
      ],
      totalElements: 2,
      totalPages: 1,
    });
  }),

  http.get(`${BASE_URL}/api/projects/:projectId`, ({ params }) => {
    const { projectId } = params;
    
    if (projectId === '999') {
      return HttpResponse.json({ message: 'Project not found' }, { status: 404 });
    }
    
    return HttpResponse.json(createMockProject({ projectId: parseInt(projectId) }));
  }),

  http.get(`${BASE_URL}/api/projects/public`, () => {
    return HttpResponse.json({
      content: [
        createMockProject({ projectId: 10, name: 'Public Project 1', visibility: 'PUBLIC' }),
        createMockProject({ projectId: 11, name: 'Public Project 2', visibility: 'PUBLIC' }),
      ],
      totalElements: 2,
    });
  }),

  http.post(`${BASE_URL}/api/projects`, async ({ request }) => {
    const body = await request.json();
    await new Promise(resolve => setTimeout(resolve, 100));
    return HttpResponse.json(createMockProject({ ...body, projectId: Date.now() }));
  }),

  http.delete(`${BASE_URL}/api/projects/:projectId`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  // File API Handlers
  http.get(`${BASE_URL}/api/files/project/:projectId`, () => {
    return HttpResponse.json([
      { fileId: 1, name: 'index.js', path: '/index.js', isFolder: false },
      { fileId: 2, name: 'src', path: '/src', isFolder: true },
    ]);
  }),

  http.get(`${BASE_URL}/api/files/content/:fileId`, () => {
    return HttpResponse.json({ content: 'console.log("Hello, World!");' });
  }),

  http.put(`${BASE_URL}/api/files/content/:fileId`, () => {
    return new HttpResponse(null, { status: 204 });
  }),

  // Execution API Handlers
  http.post(`${BASE_URL}/api/executions/run`, () => {
    return HttpResponse.json({
      jobId: 'job-' + Date.now(),
      status: 'PENDING',
      language: 'javascript',
    });
  }),

  http.get(`${BASE_URL}/api/executions/jobs/:jobId`, ({ params }) => {
    const { jobId } = params;
    
    // Simulate different job states based on jobId
    if (jobId.includes('completed')) {
      return HttpResponse.json({ jobId, status: 'COMPLETED', output: 'Program completed successfully' });
    }
    
    if (jobId.includes('failed')) {
      return HttpResponse.json({ jobId, status: 'FAILED', error: 'Syntax error' });
    }
    
    return HttpResponse.json({ jobId, status: 'RUNNING' });
  }),

  // Error simulation handlers
  http.get(`${BASE_URL}/api/error/timeout`, () => {
    return new Promise(() => {}); // Never resolves, simulates timeout
  }),

  http.get(`${BASE_URL}/api/error/server-error`, () => {
    return HttpResponse.json({ message: 'Internal Server Error' }, { status: 500 });
  }),

  http.get(`${BASE_URL}/api/error/unauthorized`, () => {
    return HttpResponse.json({ message: 'Unauthorized' }, { status: 401 });
  }),
];