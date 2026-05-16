# Frontend Testing Guide

This document explains how to use the testing infrastructure in this React application.

## Overview

The testing stack consists of:

| Tool | Purpose |
|------|---------|
| **Vitest** | Unit and integration testing framework |
| **React Testing Library** | Component testing with real DOM |
| **@testing-library/jest-dom** | Extended DOM matchers |
| **@testing-library/user-event** | User interaction simulation |
| **MSW (Mock Service Worker)** | API mocking at network level |
| **Playwright** | End-to-end browser testing |

## Running Tests

### Unit Tests

```bash
# Run all tests once
npm test

# Run tests in watch mode (recommended during development)
npm run test:watch

# Run tests with coverage report
npm run test:coverage

# Run tests with UI (Vitest UI)
npm run test:ui
```

### End-to-End Tests

```bash
# Run E2E tests
npm run test:e2e

# Run E2E tests with UI mode
npm run test:e2e:ui

# View test report
npm run test:e2e:report
```

## Test Structure

```
src/tests/
├── unit/              # Unit tests for components and utilities
│   ├── components/    # Component tests
│   ├── pages/         # Page tests
│   └── store/         # Zustand store tests
├── integration/       # Integration tests
│   └── services/      # API integration tests
├── e2e/              # End-to-end tests (Playwright)
├── mocks/            # MSW request handlers
│   ├── handlers.js   # API mock definitions
│   └── server.js     # MSW server setup
├── fixtures/         # Test data factories
└── utils/            # Test utilities and helpers
    └── test-utils.jsx
```

## Writing Tests

### Component Test Example

```jsx
import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithProviders } from '../utils/test-utils';
import MyComponent from '../../../components/MyComponent';

describe('MyComponent', () => {
  it('should render correctly', () => {
    renderWithProviders(<MyComponent />);
    
    expect(screen.getByRole('button')).toBeInTheDocument();
  });
});
```

### Testing with Store State

```jsx
// Render component as authenticated user
renderWithProviders(<Dashboard />, {
  initialRoute: '/dashboard',
  authenticated: true,
  user: { id: 1, name: 'Test User', email: 'test@example.com' }
});
```

### Testing User Interactions

```jsx
import userEvent from '@testing-library/user-event';

it('should handle form submission', async () => {
  const user = userEvent.setup();
  const mockSubmit = vi.fn();
  
  renderWithProviders(<Form onSubmit={mockSubmit} />);
  
  await user.type(screen.getByLabelText(/name/i), 'Test');
  await user.click(screen.getByRole('button', { name: /submit/i }));
  
  expect(mockSubmit).toHaveBeenCalledWith({ name: 'Test' });
});
```

## MSW Mocking

MSW intercepts network requests at the network level. Add new handlers in `src/tests/mocks/handlers.js`:

```javascript
rest.get('/api/endpoint', (req, res, ctx) => {
  return res(
    ctx.status(200),
    ctx.json({ data: 'mock response' })
  );
});

// Error responses
rest.get('/api/error', (req, res, ctx) => {
  return res(
    ctx.status(500),
    ctx.json({ message: 'Server Error' })
  );
});

// Network delay
rest.get('/api/slow', (req, res, ctx) => {
  return res(ctx.delay(3000), ctx.json({ data: 'delayed' }));
});
```

## Best Practices

### 1. Test User Behavior, Not Implementation

✅ **Good**
```javascript
// Test what user sees and does
expect(screen.getByRole('button', { name: /submit/i })).toBeDisabled();
```

❌ **Bad**
```javascript
// Tests implementation details
expect(wrapper.state('isLoading')).toBe(true);
```

### 2. Use Semantic Queries

Priority order:
1. `getByRole` - Most preferred (accessible)
2. `getByLabelText` - For form inputs
3. `getByPlaceholderText` - For inputs with placeholders
4. `getByTestId` - Last resort

### 3. Accessibility-First Testing

```javascript
it('should be accessible', () => {
  renderWithProviders(<Login />);
  
  // Check form structure
  const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
  expect(form).toBeInTheDocument();
  
  // Check labels
  expect(screen.getByLabelText(/email/i)).toHaveAttribute('required');
});
```

### 4. Async Testing

```javascript
it('should load data', async () => {
  renderWithProviders(<Dashboard />);
  
  // Wait for async content
  expect(await screen.findByText(/projects/i)).toBeInTheDocument();
});

// Or with waitFor
await waitFor(() => {
  expect(store.getState().data).toBeDefined();
});
```

## CI/CD Integration

Add this to your GitHub Actions workflow:

```yaml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - uses: actions/setup-node@v3
        with:
          node-version: 18
          
      - run: npm ci
      
      - run: npm run test:coverage
      
      - uses: codecov/codecov-action@v3
        with:
          file: ./coverage/lcov.info
          
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - uses: actions/setup-node@v3
        with:
          node-version: 18
          
      - run: npm ci
      
      - run: npx playwright install-deps
      
      - run: npm run test:e2e
```

## Debugging Tests

### Debug Unit Tests

1. **Use VS Code debugger**: Add breakpoint and run "Debug Vitest"
2. **Console.log**: Works in test files
3. **Vitest UI**: `npm run test:ui` for interactive debugging

### Debug E2E Tests

1. **Headed mode**: `PWDEBUG=1 npm run test:e2e`
2. **Inspector**: `npm run test:e2e:ui`
3. **Video/screenshots**: Check `playwright-report/` folder

### Common Issues

**Test fails with "element not found"**
- Use `screen.debug()` to see rendered HTML
- Check if component requires provider wrapper
- Use `findBy*` for async elements

**Flaky tests**
- Add proper `await` for async operations
- Use `waitFor` for assertions that need time
- Avoid test interdependencies

## Coverage Configuration

Coverage reports are generated in `./coverage/`. The configuration in `vitest.config.js`:

- **Excludes**: main.jsx, node_modules, test files, config files
- **Reporters**: text, json, html (open `coverage/index.html`)

Target coverage thresholds can be added:

```javascript
// vitest.config.js
coverage: {
  all: true,
  thresholds: {
    statements: 80,
    branches: 70,
    functions: 80,
    lines: 80,
  },
}
```