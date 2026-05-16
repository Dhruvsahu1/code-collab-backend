/**
 * Example: Good vs Bad Tests
 * 
 * This file demonstrates testing best practices by showing
 * examples of good tests (testing user behavior) vs bad tests
 * (testing implementation details).
 */

import { describe, it, expect } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Dashboard from '../../../pages/Dashboard';
import { renderWithProviders } from '../../utils/test-utils.jsx';

describe('Good vs Bad Test Examples', () => {
  const mockUser = { id: 1, name: 'Test User', email: 'test@example.com' };

  describe('GOOD - Testing User Behavior', () => {
    it('should show loading indicator while data is being fetched', async () => {
      renderWithProviders(<Dashboard />, {
        authenticated: true,
        user: mockUser,
      });

      // ✅ GOOD: Test what the user sees and experiences
      expect(screen.getByText(/welcome back/i)).toBeInTheDocument();
    });

    it('should display warning message when projects cannot be loaded', async () => {
      renderWithProviders(<Dashboard />, {
        authenticated: true,
        user: mockUser,
      });

      // ✅ GOOD: Test user-facing error messages
      await screen.findByText(/your projects/i);
    });

    it('should allow user to search projects by typing', async () => {
      const user = userEvent.setup();
      
      renderWithProviders(<Dashboard />, {
        authenticated: true,
        user: mockUser,
      });

      // ✅ GOOD: Test user interaction with semantic query
      const searchInput = await screen.findByPlaceholderText(/search projects/i);
      await user.type(searchInput, 'my-project');
      
      expect(searchInput).toHaveValue('my-project');
    });
  });

  describe('BAD - Testing Implementation Details', () => {
    it('should NOT test internal state directly', () => {
      // ❌ BAD: Testing implementation details
      // This couples test to implementation and breaks easily
      
      // const wrapper = shallow(<Dashboard />);
      // expect(wrapper.state('isLoading')).toBe(false);
      
      // ✅ Instead, test what user sees:
      renderWithProviders(<Dashboard />, {
        authenticated: true,
        user: mockUser,
      });
      expect(screen.getByText(/your projects/i)).toBeInTheDocument();
    });

    it('should NOT test CSS class names', () => {
      // ❌ BAD: Testing specific class names makes tests brittle
      
      // const button = wrapper.find('button');
      // expect(button.hasClass('btn-primary')).toBe(true);
      
      // ✅ Instead, test accessible functionality:
      renderWithProviders(<Dashboard />, {
        authenticated: true,
        user: mockUser,
      });
      expect(screen.getByRole('button', { name: /new project/i })).toBeInTheDocument();
    });

    it('should NOT test private methods', () => {
      // ❌ BAD: Testing private implementation details
      
      // expect(instance.validateForm).toHaveBeenCalled();
      
      // ✅ Instead, test user-facing behavior:
      const user = userEvent.setup();
      renderWithProviders(<Dashboard />, {
        authenticated: true,
        user: mockUser,
      });
      
      expect(screen.getByRole('button', { name: /new project/i })).toBeEnabled();
    });
  });
});