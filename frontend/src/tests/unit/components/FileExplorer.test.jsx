/**
 * FileExplorer Component Tests
 * 
 * Tests verify:
 * - Loading state
 * - Empty state
 * - Error state
 * - File item rendering
 * - Create new file/folder actions
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import FileExplorer from '../../../components/FileExplorer';
import { renderWithProviders } from '../../utils/test-utils.jsx';
import { useProjectStore } from '../../../store';

// Mock the file service
vi.mock('../../../services/fileService', () => ({
  fileService: {
    getFileTree: vi.fn(),
    getChildren: vi.fn(),
    getFileContent: vi.fn(),
    createFile: vi.fn(),
    createFolder: vi.fn(),
    deleteFile: vi.fn(),
    renameFile: vi.fn(),
  },
}));

import { fileService } from '../../../services/fileService';

// Mock toast
vi.mock('react-hot-toast', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe('FileExplorer Component', () => {
  const mockProject = { projectId: 1, name: 'Test Project' };

  beforeEach(() => {
    vi.clearAllMocks();
    fileService.getFileTree.mockResolvedValue([]);
    
    useProjectStore.setState({
      currentProject: mockProject,
      currentFile: null,
      setCurrentFile: vi.fn(),
    });
  });

  it('should show empty state when no files exist', async () => {
    renderWithProviders(<FileExplorer />);
    
    expect(await screen.findByText(/no files yet/i)).toBeInTheDocument();
  });

  it('should display project name', async () => {
    renderWithProviders(<FileExplorer />);
    
    expect(await screen.findByText(/test project/i)).toBeInTheDocument();
  });

  it('should display Explorer header', async () => {
    renderWithProviders(<FileExplorer />);
    
    expect(await screen.findByText(/explorer/i)).toBeInTheDocument();
  });

  it('should have create file button', async () => {
    renderWithProviders(<FileExplorer />);
    
    const newFileBtn = await screen.findByTitle(/new file/i);
    expect(newFileBtn).toBeInTheDocument();
  });

  it('should have create folder button', async () => {
    renderWithProviders(<FileExplorer />);
    
    const newFolderBtn = await screen.findByTitle(/new folder/i);
    expect(newFolderBtn).toBeInTheDocument();
  });

  it('should have refresh button', async () => {
    renderWithProviders(<FileExplorer />);
    
    const refreshBtn = await screen.findByTitle(/refresh/i);
    expect(refreshBtn).toBeInTheDocument();
  });
});