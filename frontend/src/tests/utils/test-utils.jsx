/**
 * Test Utilities
 * 
 * Custom render function and utilities for testing React components.
 * Provides consistent setup across all tests with providers and routing.
 */

import React from 'react';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { useAuthStore, useProjectStore, useEditorStore } from '../../store';

/**
 * Custom render function that wraps components with necessary providers.
 * 
 * Usage:
 * - renderWithProviders(<Component />)
 * - renderWithProviders(<Component />, { initialRoute: '/dashboard' })
 * - renderWithProviders(<Component />, { authenticated: true })
 * 
 * @param {React.ReactElement} ui - The component to render
 * @param {Object} options - Configuration options
 * @param {string} options.initialRoute - Initial route for router
 * @param {boolean} options.authenticated - Whether to mock authenticated state
 * @param {Object} options.user - Mock user data (used with authenticated)
 * @param {Object} options.initialStores - Initial store state overrides
 */
export function renderWithProviders(
  ui,
  {
    initialRoute = '/',
    authenticated = false,
    user = null,
    initialStores = {},
    ...renderOptions
  } = {}
) {
  // Set up store state before rendering
  if (authenticated) {
    useAuthStore.setState({
      isAuthenticated: true,
      user: user || {
        id: 1,
        name: 'Test User',
        email: 'test@example.com',
        username: 'testuser',
      },
      token: 'mock-token',
      ...initialStores,
    });
  } else {
    useAuthStore.setState({
      isAuthenticated: false,
      user: null,
      token: null,
      ...initialStores,
    });
  }

  // Create wrapper with router
  function Wrapper({ children }) {
    return (
      <MemoryRouter initialEntries={[initialRoute]}>
        {children}
      </MemoryRouter>
    );
  }

  return {
    ...render(ui, { wrapper: Wrapper, ...renderOptions }),
    // Re-export everything for convenience
    rerender: (ui, options) => render(ui, { wrapper: Wrapper, ...options }),
  };
}

// Re-export utilities from testing library
export * from '@testing-library/react';

/**
 * Wait for element to be removed from the DOM
 * @param {Document} container - DOM container
 * @param {string} text - Text content to wait for removal
 */
export async function waitForElementToBeRemoved(container, text) {
  const { waitFor } = await import('@testing-library/react');
  await waitFor(() => {
    expect(container.querySelector(`text="${text}"`)).toBeNull();
  });
}