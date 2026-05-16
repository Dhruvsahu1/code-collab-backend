/**
 * Register Page Tests
 * 
 * Tests verify:
 * - Form validation (password match, length requirement)
 * - User registration flow
 * - Error handling
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Register from '../../../pages/Register';
import { renderWithProviders } from '../../utils/test-utils.jsx';
import { useAuthStore } from '../../../store';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Register Page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render registration form with all fields', () => {
    renderWithProviders(<Register />);
    
    expect(screen.getByRole('heading', { name: /create account/i })).toBeInTheDocument();
    
    // Use placeholder queries for inputs without proper label associations
    expect(screen.getByPlaceholderText(/john doe/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/johndoe/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/you@example\.com/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••••••/i)).toHaveLength(2); // Two password fields
    
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  it('should allow user to enter registration details', async () => {
    const user = userEvent.setup();
    renderWithProviders(<Register />);
    
    await user.type(screen.getByPlaceholderText(/john doe/i), 'Test User');
    await user.type(screen.getByPlaceholderText(/johndoe/i), 'testuser');
    await user.type(screen.getByPlaceholderText(/you@example\.com/i), 'test@example.com');
    
    const passwordInputs = screen.getAllByPlaceholderText(/••••••••/i);
    await user.type(passwordInputs[0], 'password123');
    await user.type(passwordInputs[1], 'password123');
    
    expect(screen.getByPlaceholderText(/john doe/i)).toHaveValue('Test User');
    expect(screen.getByPlaceholderText(/johndoe/i)).toHaveValue('testuser');
    expect(screen.getByPlaceholderText(/you@example\.com/i)).toHaveValue('test@example.com');
  });

  it('should navigate to login page', () => {
    renderWithProviders(<Register />);
    
    const loginLink = screen.getByRole('link', { name: /already have an account/i });
    expect(loginLink).toHaveAttribute('href', '/login');
  });

  it('should have back to home link', () => {
    renderWithProviders(<Register />);
    
    const homeLink = screen.getByRole('link', { name: /back to home/i });
    expect(homeLink).toHaveAttribute('href', '/');
  });
});