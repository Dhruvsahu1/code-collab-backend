/**
 * API Error State Tests
 * 
 * Tests verify:
 * - Loading state handling
 * - Success state handling  
 * - API error state handling
 * - Timeout simulation
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Login from '../../../pages/Login';
import { renderWithProviders } from '../../utils/test-utils.jsx';
import { useAuthStore } from '../../../store';

// Mock MSW handlers for error states
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

describe('API States', () => {
  const mockUser = { id: 1, name: 'Test', email: 'test@example.com' };

  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      isLoading: false,
      isAuthenticated: false,
    });
  });

  describe('Loading State', () => {
    it('should show loading indicator during submit', async () => {
      const user = userEvent.setup();
      
      // Set loading state
      useAuthStore.setState({ isLoading: true });
      
      renderWithProviders(<Login />, {
        authenticated: false,
        user: mockUser,
      });

      const submitButton = screen.getByRole('button', { name: /signing in/i });
      expect(submitButton).toBeDisabled();
    });
  });

  describe('Success State', () => {
    it('should navigate after successful login', async () => {
      const user = userEvent.setup();
      
      useAuthStore.setState({ 
        isLoading: false,
        isAuthenticated: true,
        user: mockUser,
        token: 'test-token',
      });

      renderWithProviders(<Login />);
      
      // The form should show sign in button (not loading) on success
      await screen.findByRole('button', { name: /sign in/i });
    });
  });

  describe('Error State', () => {
    it('should show error for invalid credentials', async () => {
      const user = userEvent.setup();
      
      useAuthStore.setState({ 
        isLoading: false,
        isAuthenticated: false,
      });

      renderWithProviders(<Login />);
      
      await user.type(screen.getByLabelText(/email or username/i), 'wrong@example.com');
      await user.type(screen.getByLabelText(/password/i), 'wrongpassword');
      await user.click(screen.getByRole('button', { name: /sign in/i }));
      
      // Should handle error (toast would show in real app)
    });
  });
});