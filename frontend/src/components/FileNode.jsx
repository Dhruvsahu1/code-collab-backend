import { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { fileService } from '../services/fileService';
import toast from 'react-hot-toast';

const FileNode = ({ 
  node, 
  depth = 0, 
  onSelect, 
  onCreateFile, 
  onCreateFolder, 
  onDelete, 
  onRename,
  selectedFileId,
  expandedFolders,
  onToggleFolder,
  projectId,
  onLoadChildren,
  treeData 
}) => {
  const [showContextMenu, setShowContextMenu] = useState(false);
  const [contextMenuPos, setContextMenuPos] = useState({ x: 0, y: 0 });
  const [isLoadingChildren, setIsLoadingChildren] = useState(false);
  const contextMenuRef = useRef(null);

  const isFolder = node.isFolder;
  const isExpanded = expandedFolders.has(node.path);
  const isSelected = selectedFileId === node.fileId;

  console.log('FileNode rendering:', node.name, 'isFolder:', isFolder, 'isExpanded:', isExpanded, 'path:', node.path);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (contextMenuRef.current && !contextMenuRef.current.contains(e.target)) {
        setShowContextMenu(false);
      }
    };
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  const handleClick = async (e) => {
    e.stopPropagation();
    console.log('handleClick called:', node.name, 'isFolder:', isFolder);

    if (isFolder) {
      console.log('Calling onToggleFolder with:', node.path);
      onToggleFolder(node.path);
    } else {
      console.log('Calling onSelect with:', node.name);
      onSelect(node);
    }
  };

  const handleContextMenu = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setContextMenuPos({ x: e.clientX, y: e.clientY });
    setShowContextMenu(true);
  };

  const closeContextMenu = () => {
    setShowContextMenu(false);
  };

  const getFileIcon = () => {
    if (isFolder) {
      return isExpanded ? '📂' : '📁';
    }
    const ext = node.name?.split('.').pop();
    const icons = {
      js: '🟨', jsx: '⚛️',
      ts: '🔷', tsx: '⚛️',
      py: '🐍',
      java: '☕',
      go: '🐹',
      rs: '🦀',
      html: '🌐',
      css: '🎨',
      json: '📋',
      md: '📝',
      txt: '📄',
    };
    return icons[ext] || '📄';
  };

  const handleCreateFile = (e) => {
    e.stopPropagation();
    closeContextMenu();
    onCreateFile(node);
  };

  const handleCreateFolder = (e) => {
    e.stopPropagation();
    closeContextMenu();
    onCreateFolder(node);
  };

  const handleRename = (e) => {
    e.stopPropagation();
    closeContextMenu();
    onRename(node);
  };

  const handleDelete = (e) => {
    e.stopPropagation();
    closeContextMenu();
    onDelete(node);
  };

  const handleOpenFile = (e) => {
    e.stopPropagation();
    closeContextMenu();
    if (!isFolder) {
      onSelect(node);
    }
  };

  return (
    <div className="select-none">
      <motion.div
        initial={{ opacity: 0, x: -10 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.15 }}
      >
        <div
          onClick={handleClick}
          onContextMenu={handleContextMenu}
          className={`
            flex items-center gap-1.5 px-2 py-1.5 cursor-pointer rounded-md
            transition-all duration-150
            ${isSelected 
              ? 'bg-surface-hover border-l-2 border-accent-cyan' 
              : 'hover:bg-surface-hover'
            }
          `}
          style={{ paddingLeft: `${depth * 12 + 8}px` }}
        >
          {/* Expand/Collapse Arrow */}
          {isFolder && (
            <span className={`text-[10px] text-zinc-500 transition-transform ${isExpanded ? 'rotate-90' : ''}`}>
              ▶
            </span>
          )}
          {!isFolder && <span className="w-[10px]" />}

          {/* Icon */}
          <span className="text-sm">{getFileIcon()}</span>

          {/* Name */}
          <span className="text-sm text-zinc-300 truncate flex-1">
            {node.name}
          </span>
        </div>

        {/* Children - Direct rendering from node.children */}
        {isFolder && isExpanded && node.children && node.children.length > 0 && (
          <AnimatePresence>
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              transition={{ duration: 0.15 }}
            >
              {node.children.map((child) => (
                <FileNode
                  key={child.fileId}
                  node={child}
                  depth={depth + 1}
                  onSelect={onSelect}
                  onCreateFile={onCreateFile}
                  onCreateFolder={onCreateFolder}
                  onDelete={onDelete}
                  onRename={onRename}
                  selectedFileId={selectedFileId}
                  expandedFolders={expandedFolders}
                  onToggleFolder={onToggleFolder}
                  projectId={projectId}
                  onLoadChildren={onLoadChildren}
                  treeData={treeData}
                />
              ))}
            </motion.div>
          </AnimatePresence>
        )}
      </motion.div>

      {/* Context Menu */}
      {showContextMenu && (
        <>
          <div 
            className="fixed inset-0 z-40" 
            onClick={closeContextMenu}
          />
          <div
            ref={contextMenuRef}
            className="fixed z-50 bg-surface-card border border-surface-border rounded-md shadow-lg py-1 min-w-[160px] overflow-hidden"
            style={{ 
              left: Math.min(contextMenuPos.x, window.innerWidth - 180), 
              top: Math.min(contextMenuPos.y, window.innerHeight - 200) 
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {isFolder && (
              <>
                <button
                  onClick={handleCreateFile}
                  className="w-full px-3 py-1.5 text-left text-sm text-zinc-300 hover:bg-surface-hover flex items-center gap-2"
                >
                  <span>📄</span> New File
                </button>
                <button
                  onClick={handleCreateFolder}
                  className="w-full px-3 py-1.5 text-left text-sm text-zinc-300 hover:bg-surface-hover flex items-center gap-2"
                >
                  <span>📁</span> New Folder
                </button>
                <div className="h-[1px] bg-surface-border my-1" />
              </>
            )}
            <button
              onClick={handleOpenFile}
              className="w-full px-3 py-1.5 text-left text-sm text-zinc-300 hover:bg-surface-hover flex items-center gap-2"
            >
              <span>📝</span> {isFolder ? 'Open' : 'Open File'}
            </button>
            <button
              onClick={handleRename}
              className="w-full px-3 py-1.5 text-left text-sm text-zinc-300 hover:bg-surface-hover flex items-center gap-2"
            >
              <span>✏️</span> Rename
            </button>
            <div className="h-[1px] bg-surface-border my-1" />
            <button
              onClick={handleDelete}
              className="w-full px-3 py-1.5 text-left text-sm text-red-400 hover:bg-surface-hover flex items-center gap-2"
            >
              <span>🗑️</span> Delete
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default FileNode;
