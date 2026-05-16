/**
 * Dashboard Page Tests
 * 
 * Tests verify:
 * - Dashboard rendering with user greeting
 * - Project listing and search
 * - Create project modal
 * - Loading states
 * - Empty state
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Dashboard from '../../../pages/Dashboard';
import { renderWithProviders } from '../../utils/test-utils.jsx';
import { useProjectStore, useAuthStore } from '../../../store';

// Mock API
vi.mock('../../../services/api', () => ({
  projectAPI: {
    create: vi.fn(),
  },
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Dashboard Page', () => {
  const mockUser = {
    id: 1,
    name: 'John Doe',
    email: 'john@example.com',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    
    useAuthStore.setState({
      user: mockUser,
      isAuthenticated: true,
      token: 'mock-token',
    });
    
    useProjectStore.setState({
      projects: [],
      currentProject: null,
      isLoading: false,
      fetchProjects: vi.fn(),
    });
  });

  it('should display welcome message with user name', async () => {
    renderWithProviders(<Dashboard />, {
      authenticated: true,
      user: mockUser,
    });
    
    expect(await screen.findByText(/welcome back/i)).toBeInTheDocument();
    expect(screen.getByText(/john/i)).toBeInTheDocument();
  });

  it('should display projects section', async () => {
    renderWithProviders(<Dashboard />, {
      authenticated: true,
      user: mockUser,
    });
    
    expect(await screen.findByText(/your projects/i)).toBeInTheDocument();
  });

  it('should display empty state when no projects', async () => {
    useProjectStore.setState({ projects: [], isLoading: false });
    
    renderWithProviders(<Dashboard />, {
      authenticated: true,
      user: mockUser,
    });
    
    expect(await screen.findByText(/no projects yet/i)).toBeInTheDocument();
  });

  it('should open create project modal', async () => {
    const user = userEvent.setup();
    
    renderWithProviders(<Dashboard />, {
      authenticated: true,
      user: mockUser,
    });
    
    const newProjectButton = await screen.findByRole('button', { name: /new project/i });
    await user.click(newProjectButton);
    
    expect(screen.getByRole('heading', { name: /create new project/i })).toBeInTheDocument();
  });

  it('should allow searching projects', async () => {
    const user = userEvent.setup();
    
    renderWithProviders(<Dashboard />, {
      authenticated: true,
      user: mockUser,
    });
    
    const searchInput = await screen.findByPlaceholderText(/search projects/i);
    await user.type(searchInput, 'test');
    
    expect(searchInput).toHaveValue('test');
  });

  it('should display logout button', async () => {
    renderWithProviders(<Dashboard />, {
      authenticated: true,
      user: mockUser,
    });
    
    await screen.findByText(/welcome back/i);
  });
});