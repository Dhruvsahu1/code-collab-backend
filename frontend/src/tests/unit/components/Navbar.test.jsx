/**
 * Navbar Component Tests
 * 
 * Tests verify:
 * - Navigation links render correctly
 * - Accessibility attributes are present
 * - Interactive behavior works as expected
 * 
 * Best practices followed:
 * - Using semantic queries (getByRole, getByLabelText)
 * - Testing user behavior, not implementation details
 * - Accessibility-first assertions
 */

import { describe, it, expect, vi } from 'vitest';
import { screen } from '@testing-library/react';
import Navbar from '../../../components/Navbar';
import { renderWithProviders } from '../../utils/test-utils.jsx';

describe('Navbar Component', () => {
  it('should render the logo with correct link', () => {
    renderWithProviders(<Navbar />);
    
    const logoLink = screen.getByRole('link', { name: /codesync/i });
    expect(logoLink).toBeInTheDocument();
    expect(logoLink).toHaveAttribute('href', '/dashboard');
  });

  it('should render sign in link pointing to login page', () => {
    renderWithProviders(<Navbar />);
    
    const signInLink = screen.getByRole('link', { name: /sign in/i });
    expect(signInLink).toBeInTheDocument();
    expect(signInLink).toHaveAttribute('href', '/login');
  });

  it('should render get started button pointing to register page', () => {
    renderWithProviders(<Navbar />);
    
    const getStartedButton = screen.getByRole('link', { name: /get started/i });
    expect(getStartedButton).toBeInTheDocument();
    expect(getStartedButton).toHaveAttribute('href', '/register');
  });

  it('should have accessible navigation structure', () => {
    renderWithProviders(<Navbar />);
    
    // Check that nav element exists
    const nav = screen.getByRole('navigation');
    expect(nav).toBeInTheDocument();
  });

  it('should apply correct styling classes for hover states', () => {
    renderWithProviders(<Navbar />);
    
    const signInLink = screen.getByRole('link', { name: /sign in/i });
    expect(signInLink).toHaveClass('transition-colors');
  });
});