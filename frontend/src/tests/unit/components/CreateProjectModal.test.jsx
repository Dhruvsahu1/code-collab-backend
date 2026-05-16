/**
 * Create Project Modal Tests
 * 
 * Tests verify:
 * - Modal rendering and visibility
 * - Form inputs and validation
 * - Language selection
 * - Visibility toggle
 * - Form submission
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CreateProjectModal from '../../../components/CreateProjectModal';
import { renderWithProviders } from '../../utils/test-utils.jsx';

describe('CreateProjectModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSubmit = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render modal with form elements', () => {
    renderWithProviders(
      <CreateProjectModal onClose={mockOnClose} onSubmit={mockOnSubmit} />
    );

    expect(screen.getByRole('heading', { name: /create new project/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/project name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/language/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/visibility/i)).toBeInTheDocument();
  });

  it('should allow entering project details', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CreateProjectModal onClose={mockOnClose} onSubmit={mockOnSubmit} />
    );

    await user.type(screen.getByLabelText(/project name/i), 'My Project');
    await user.type(screen.getByLabelText(/description/i), 'A test project');

    expect(screen.getByLabelText(/project name/i)).toHaveValue('My Project');
    expect(screen.getByLabelText(/description/i)).toHaveValue('A test project');
  });

  it('should allow selecting a language', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CreateProjectModal onClose={mockOnClose} onSubmit={mockOnSubmit} />
    );

    // Default is JavaScript
    const jsButton = screen.getByRole('button', { name: /🟨 javascript/i });
    expect(jsButton).toHaveClass('bg-accent-cyan/20');

    // Select Python
    await user.click(screen.getByRole('button', { name: /🐍 python/i }));
    expect(screen.getByRole('button', { name: /🐍 python/i })).toHaveClass('bg-accent-cyan/20');
  });

  it('should allow toggling visibility', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CreateProjectModal onClose={mockOnClose} onSubmit={mockOnSubmit} />
    );

    const publicButton = screen.getByRole('button', { name: /public/i });
    const privateButton = screen.getByRole('button', { name: /private/i });

    // Default is private
    expect(privateButton).toHaveClass('bg-accent-cyan/20');

    // Switch to public
    await user.click(publicButton);
    expect(publicButton).toHaveClass('bg-accent-cyan/20');
  });

  it('should submit form with project data', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CreateProjectModal onClose={mockOnClose} onSubmit={mockOnSubmit} />
    );

    await user.type(screen.getByLabelText(/project name/i), 'Test Project');
    await user.type(screen.getByLabelText(/description/i), 'Test description');

    await user.click(screen.getByRole('button', { name: /create project/i }));

    expect(mockOnSubmit).toHaveBeenCalledWith({
      name: 'Test Project',
      description: 'Test description',
      language: 'javascript',
      visibility: 'private',
    });
  });

  it('should close modal when cancel is clicked', async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <CreateProjectModal onClose={mockOnClose} onSubmit={mockOnSubmit} />
    );

    const cancelButton = screen.getByRole('button', { name: /cancel/i });
    await user.click(cancelButton);

    expect(mockOnClose).toHaveBeenCalled();
  });
});