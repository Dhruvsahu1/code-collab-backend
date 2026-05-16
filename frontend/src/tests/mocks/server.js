/**
 * MSW Server Configuration
 * 
 * Mock Service Worker (MSW) intercepts network requests at the network level,
 * allowing realistic API mocking without changing application code.
 * 
 * Features:
 * - Intercept REST and GraphQL requests
 * - Simulate network delays, errors, and edge cases
 * - Share mocks between unit and integration tests
 */

import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);