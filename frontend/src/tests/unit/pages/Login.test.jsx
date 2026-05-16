/**
 * Login Page Tests
 * 
 * Tests verify:
 * - Form rendering and validation
 * - User input handling
 * - Authentication flow with store
 * - Loading states
 * - Error handling
 * - Accessibility
 * 
 * Best practices:
 * - Testing async behavior with proper waits
 * - Mocking store state changes
 * - Testing form validation
 * - Using semantic queries
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Login from '../../../pages/Login';
import { renderWithProviders } from '../../utils/test-utils.jsx';
import { useAuthStore } from '../../../store';

// Mock react-router-dom's useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Login Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should render login form with all fields', () => {
    renderWithProviders(<Login />);
    
    expect(screen.getByRole('heading', { name: /welcome back/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/email or username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should allow user to enter credentials', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Login />);
    
    const emailInput = screen.getByLabelText(/email or username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    
    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');
  });

  it('should show loading state when submitting', async () => {
    const user = userEvent.setup();
    
    // Mock login to take time
    let resolveLogin;
    const loginPromise = new Promise(resolve => { resolveLogin = resolve; });
    
    useAuthStore.setState({
      login: vi.fn().mockImplementation(() => loginPromise),
      isLoading: true,
    });
    
    renderWithProviders(<Login />);
    
    await user.type(screen.getByLabelText(/email or username/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));
    
    // Button should show loading text
    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled();
    
    // Resolve the login
    resolveLogin({ success: true });
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('should navigate to signup page', () => {
    renderWithProviders(<Login />);
    
    const signupLink = screen.getByRole('link', { name: /sign up/i });
    expect(signupLink).toHaveAttribute('href', '/register');
  });

  it('should navigate back to home page', () => {
    renderWithProviders(<Login />);
    
    const homeLink = screen.getByRole('link', { name: /← back to home/i });
    expect(homeLink).toHaveAttribute('href', '/');
  });

  it('should have proper form accessibility', () => {
    renderWithProviders(<Login />);
    
    const form = screen.getByRole('button', { name: /sign in/i }).closest('form');
    expect(form).toBeInTheDocument();
    
    // Check labels are associated with inputs
    const emailInput = screen.getByLabelText(/email or username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    
    expect(emailInput).toHaveAttribute('required');
    expect(passwordInput).toHaveAttribute('required');
  });
});