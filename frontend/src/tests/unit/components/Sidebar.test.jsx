/**
 * Sidebar Component Tests
 * 
 * Tests verify:
 * - Navigation links render correctly
 * - User info displays properly
 * - Logout functionality works
 * - Accessibility attributes
 * 
 * Best practices followed:
 * - Using semantic queries (getByRole, getByLabelText)
 * - Testing user interactions with userEvent
 * - Accessibility-first assertions
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Sidebar from '../../../components/Sidebar';
import { renderWithProviders } from '../../utils/test-utils.jsx';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Sidebar Component', () => {
  const mockUser = {
    id: 1,
    name: 'John Doe',
    email: 'john@example.com',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render navigation links', () => {
    renderWithProviders(<Sidebar />, {
      authenticated: true,
      user: mockUser,
    });

    expect(screen.getByRole('link', { name: /codesync/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /dashboard/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /explore/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /profile/i })).toBeInTheDocument();
  });

  it('should display user initials in avatar', () => {
    renderWithProviders(<Sidebar />, {
      authenticated: true,
      user: mockUser,
    });

    expect(screen.getByText('J')).toBeInTheDocument();
  });

  it('should display user name and email', () => {
    renderWithProviders(<Sidebar />, {
      authenticated: true,
      user: mockUser,
    });

    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
  });

  it('should call logout and navigate to login on sign out click', async () => {
    const user = userEvent.setup();
    
    renderWithProviders(<Sidebar />, {
      authenticated: true,
      user: mockUser,
    });

    const logoutButton = screen.getByRole('button', { name: /sign out/i });
    await user.click(logoutButton);

    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('should have accessible navigation structure', () => {
    renderWithProviders(<Sidebar />, {
      authenticated: true,
      user: mockUser,
    });

    const nav = screen.getByRole('navigation');
    expect(nav).toBeInTheDocument();
  });

  it('should have correct navigation link hrefs', () => {
    renderWithProviders(<Sidebar />, {
      authenticated: true,
      user: mockUser,
    });

    expect(screen.getByRole('link', { name: /dashboard/i })).toHaveAttribute('href', '/dashboard');
    expect(screen.getByRole('link', { name: /explore/i })).toHaveAttribute('href', '/explore');
    expect(screen.getByRole('link', { name: /profile/i })).toHaveAttribute('href', '/profile');
  });
});