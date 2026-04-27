import { useState, useEffect, useRef, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import FileNode from './FileNode';
import { fileService } from '../services/fileService';
import { useProjectStore } from '../store';

const FileExplorer = ({ onFileSelect }) => {
  const { currentProject, currentFile, setCurrentFile } = useProjectStore();
  const [fileTree, setFileTree] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [expandedFolders, setExpandedFolders] = useState(new Set());
  const [showNewItemModal, setShowNewItemModal] = useState(false);
  const [newItemType, setNewItemType] = useState('file');
  const [newItemName, setNewItemName] = useState('');
  const [parentNode, setParentNode] = useState(null);
  const [isRenaming, setIsRenaming] = useState(false);
  const [renamingNode, setRenamingNode] = useState(null);
  const [renameName, setRenameName] = useState('');
  const [childrenCache, setChildrenCache] = useState({});
  const modalInputRef = useRef(null);

  console.log('FileExplorer mounted, projectId:', currentProject?.projectId, 'currentProject:', currentProject);

  // Load file tree on project change
  useEffect(() => {
    console.log('useEffect triggered, currentProject:', currentProject);
    if (currentProject?.projectId) {
      console.log('Loading file tree for project:', currentProject.projectId);
      loadFileTree();
    } else {
      console.log('No projectId, skipping load. currentProject is:', currentProject);
    }
  }, [currentProject?.projectId, currentProject?.name]);

  // Focus input when modal opens
  useEffect(() => {
    if (showNewItemModal && modalInputRef.current) {
      setTimeout(() => modalInputRef.current?.focus(), 100);
    }
  }, [showNewItemModal]);

  const getParentPath = (path) => {
    if (!path || path === '/' || path === '') return '/';
    if (path.endsWith('/')) {
      path = path.slice(0, -1);
    }
    const lastSlash = path.lastIndexOf('/');
    if (lastSlash <= 0) return '/';
    return path.substring(0, lastSlash + 1);
  };

  // Build hierarchy from flat list
  const buildHierarchy = (items) => {
    const nodeMap = new Map();
    const rootNodes = [];
    
    // Create nodes
    items.forEach(item => {
      nodeMap.set(item.path, { ...item, children: [] });
    });
    
    // Build tree
    items.forEach(item => {
      const node = nodeMap.get(item.path);
      const parentPath = getParentPath(item.path);
      
      if (parentPath === '/' || parentPath === '' || !nodeMap.has(parentPath)) {
        rootNodes.push(node);
      } else {
        const parent = nodeMap.get(parentPath);
        if (parent && parent.isFolder) {
          parent.children.push(node);
        } else {
          rootNodes.push(node);
        }
      }
    });
    
    return rootNodes;
  };

  // Load file tree from backend
  const loadFileTree = async () => {
    if (!currentProject?.projectId) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      console.log('Fetching file tree from API...');
      const tree = await fileService.getFileTree(currentProject.projectId);
      console.log('File tree response:', tree);
      
      // Build hierarchy
      const builtTree = buildHierarchy(tree || []);
      setFileTree(builtTree);
      
      // Auto-expand first level folders
      if (tree && tree.length > 0) {
        const firstLevelFolders = tree
          .filter(node => node.isFolder)
          .map(node => node.path);
        setExpandedFolders(new Set(firstLevelFolders));
        
        // Pre-load children for expanded folders
        firstLevelFolders.forEach(async (path) => {
          try {
            const children = await fileService.getChildren(currentProject.projectId, path);
            setChildrenCache(prev => ({ ...prev, [path]: children }));
          } catch (e) {
            console.error('Failed to pre-load children for:', path, e);
          }
        });
      }
    } catch (err) {
      console.error('Failed to load file tree:', err);
      setError(err.message);
      toast.error('Failed to load files: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  // Toggle folder expansion - loads children from API
  const toggleFolder = useCallback(async (path) => {
    console.log('toggleFolder called with path:', path);
    
    setExpandedFolders(prev => {
      const next = new Set(prev);
      if (next.has(path)) {
        next.delete(path);
        console.log('Collapsing folder:', path);
      } else {
        next.add(path);
        console.log('Expanding folder:', path);
        
        // Load children if not cached
        if (!childrenCache[path]) {
          console.log('Loading children for:', path);
          fileService.getChildren(currentProject?.projectId, path)
            .then(children => {
              console.log('Got children for', path, ':', children);
              setChildrenCache(prev => ({ ...prev, [path]: children }));
            })
            .catch(err => console.error('Failed to load children:', err));
        }
      }
      return next;
    });
  }, [childrenCache, currentProject?.projectId]);

  // Update children in tree nodes
  const updateTreeWithChildren = useCallback((nodes, cache) => {
    return nodes.map(node => {
      if (node.isFolder && cache[node.path]) {
        return { 
          ...node, 
          children: updateTreeWithChildren(cache[node.path], cache) 
        };
      }
      if (node.children && node.children.length > 0) {
        return { ...node, children: updateTreeWithChildren(node.children, cache) };
      }
      return node;
    });
  }, []);

  // Get final tree with cached children
  const getFinalTree = useCallback(() => {
    return updateTreeWithChildren(fileTree, childrenCache);
  }, [fileTree, childrenCache, updateTreeWithChildren]);

  // Handle file/folder selection
  const handleSelect = useCallback(async (node) => {
    console.log('handleSelect called with:', node.name);
    
    if (node.isFolder) {
      toggleFolder(node.path);
    } else {
      // Fetch file content
      try {
        const contentData = await fileService.getFileContent(node.fileId);
        console.log('File content loaded:', contentData.content?.substring(0, 50));
        const fileWithContent = {
          ...node,
          content: contentData.content || ''
        };
        setCurrentFile(fileWithContent);
        onFileSelect?.(fileWithContent);
      } catch (err) {
        console.error('Failed to load file content:', err);
        setCurrentFile(node);
        onFileSelect?.(node);
      }
    }
  }, [toggleFolder, setCurrentFile, onFileSelect]);

  // Open modal to create new file/folder
  const handleCreateNew = (type, parent = null) => {
    setNewItemType(type);
    setNewItemName('');
    setParentNode(parent);
    setShowNewItemModal(true);
  };

  // Handle creating new item
  const handleCreateItem = async () => {
    if (!newItemName.trim()) {
      toast.error('Name is required');
      return;
    }

    const name = newItemName.trim();
    const projectId = currentProject?.projectId;
    
    if (!projectId) {
      toast.error('No project selected');
      return;
    }

    // Determine parent path
    let parentPath = '/';
    if (parentNode) {
      parentPath = parentNode.path;
      if (!parentPath.endsWith('/')) {
        parentPath = parentPath + '/';
      }
    }

    try {
      console.log('Creating item:', { name, projectId, parentPath, type: newItemType });
      
      if (newItemType === 'folder') {
        await fileService.createFolder({
          name: name,
          projectId: projectId,
          path: parentPath
        });
        toast.success('Folder created');
      } else {
        await fileService.createFile({
          name: name,
          projectId: projectId,
          path: parentPath,
          content: ''
        });
        toast.success('File created');
      }

      // Clear cache for parent and reload tree
      if (parentPath !== '/') {
        setChildrenCache(prev => {
          const next = { ...prev };
          delete next[parentPath];
          return next;
        });
      }

      await loadFileTree();

      // Auto-expand parent folder
      if (parentPath !== '/') {
        setExpandedFolders(prev => new Set([...prev, parentPath]));
      }

      setShowNewItemModal(false);
      setNewItemName('');
      setParentNode(null);
    } catch (err) {
      console.error('Failed to create:', err);
      toast.error(err.response?.data?.error || err.response?.data?.message || 'Failed to create');
    }
  };

  // Handle rename
  const handleRename = (node) => {
    setRenamingNode(node);
    setRenameName(node.name);
    setIsRenaming(true);
  };

  // Submit rename
  const handleRenameSubmit = async () => {
    if (!renamingNode || !renameName.trim()) return;

    const newName = renameName.trim();
    if (newName === renamingNode.name) {
      setIsRenaming(false);
      setRenamingNode(null);
      return;
    }

    try {
      await fileService.renameFile(renamingNode.fileId, newName);
      toast.success('Renamed successfully');
      await loadFileTree();
    } catch (err) {
      console.error('Failed to rename:', err);
      toast.error(err.response?.data?.error || 'Failed to rename');
    } finally {
      setIsRenaming(false);
      setRenamingNode(null);
    }
  };

  // Handle delete
  const handleDelete = async (node) => {
    const confirmMsg = node.isFolder 
      ? `Delete folder "${node.name}" and all its contents?`
      : `Delete file "${node.name}"?`;
    
    if (!confirm(confirmMsg)) return;

    try {
      await fileService.deleteFile(node.fileId);
      toast.success(`${node.isFolder ? 'Folder' : 'File'} deleted`);
      await loadFileTree();
    } catch (err) {
      console.error('Failed to delete:', err);
      toast.error(err.response?.data?.error || 'Failed to delete');
    }
  };

  const finalTree = getFinalTree();
  console.log('Rendering tree with', finalTree.length, 'root nodes');
  console.log('Expanded folders:', Array.from(expandedFolders));
  console.log('Children cache keys:', Object.keys(childrenCache));

  return (
    <div className="h-full flex flex-col bg-surface-darker text-zinc-300">
      {/* Header */}
      <div className="px-3 py-2 text-xs font-semibold uppercase tracking-wide text-zinc-400 flex items-center justify-between">
        <span>Explorer</span>
        <div className="flex gap-1">
          <button
            onClick={() => handleCreateNew('file', null)}
            className="p-1 hover:bg-surface-hover rounded"
            title="New File"
          >
            <span className="text-sm">📄</span>
          </button>
          <button
            onClick={() => handleCreateNew('folder', null)}
            className="p-1 hover:bg-surface-hover rounded"
            title="New Folder"
          >
            <span className="text-sm">📁</span>
          </button>
          <button
            onClick={loadFileTree}
            className="p-1 hover:bg-surface-hover rounded"
            title="Refresh"
          >
            <span className="text-sm">🔄</span>
          </button>
        </div>
      </div>

      {/* Debug indicator */}
      <div className="px-3 py-1 text-xs text-zinc-600 bg-black/20">
        Tree: {finalTree.length} items | Cache: {Object.keys(childrenCache).length} folders
      </div>

      {/* Project Name */}
      {currentProject ? (
        <div className="px-3 py-1.5 text-sm font-medium text-white flex items-center gap-2 hover:bg-surface-hover cursor-pointer">
          <span className={`text-[10px] transition-transform ${expandedFolders.size > 0 ? 'rotate-90' : ''}`}>▶</span>
          <span>{currentProject.name}</span>
        </div>
      ) : (
        <div className="px-3 py-1.5 text-sm text-red-400">
          ⚠️ No project loaded
        </div>
      )}

      {/* File Tree */}
      <div className="flex-1 overflow-y-auto py-1">
        {isLoading ? (
          <div className="px-3 py-2 text-sm text-zinc-500 flex items-center gap-2">
            <span className="animate-spin">⏳</span>
            Loading...
          </div>
        ) : error ? (
          <div className="px-3 py-2">
            <p className="text-sm text-red-400">Error: {error}</p>
            <button 
              onClick={loadFileTree}
              className="text-sm text-accent-cyan hover:underline mt-1"
            >
              Retry
            </button>
          </div>
        ) : finalTree.length === 0 ? (
          <div className="px-3 py-2 text-sm text-zinc-500">
            <p>No files yet</p>
            <button 
              onClick={() => handleCreateNew('file', null)}
              className="text-accent-cyan hover:underline mt-1 block"
            >
              + Create a file
            </button>
          </div>
        ) : (
          <div className="px-1">
            {finalTree.map((node) => (
              <FileNode
                key={node.fileId}
                node={node}
                depth={0}
                onSelect={handleSelect}
                onCreateFile={(parent) => handleCreateNew('file', parent)}
                onCreateFolder={(parent) => handleCreateNew('folder', parent)}
                onDelete={handleDelete}
                onRename={handleRename}
                selectedFileId={currentFile?.fileId}
                expandedFolders={expandedFolders}
                onToggleFolder={toggleFolder}
                projectId={currentProject?.projectId}
                onLoadChildren={(path, children) => {
                  setChildrenCache(prev => ({ ...prev, [path]: children }));
                }}
                treeData={finalTree}
              />
            ))}
          </div>
        )}
      </div>

      {/* New Item Modal */}
      <AnimatePresence>
        {showNewItemModal && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
            onClick={() => setShowNewItemModal(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-surface-card border border-surface-border rounded-lg p-4 w-80 shadow-xl"
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-sm font-semibold text-white mb-3">
                New {newItemType === 'folder' ? 'Folder' : 'File'}
              </h3>
              
              <input
                ref={modalInputRef}
                type="text"
                value={newItemName}
                onChange={(e) => setNewItemName(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleCreateItem();
                  if (e.key === 'Escape') setShowNewItemModal(false);
                }}
                placeholder={newItemType === 'folder' ? 'folder-name' : 'file-name.ext'}
                className="w-full px-3 py-2 bg-surface-darker border border-surface-border rounded text-zinc-300 text-sm focus:border-accent-cyan outline-none"
                autoFocus
              />

              {parentNode && (
                <p className="text-xs text-zinc-500 mt-2">
                  Location: {parentNode.path}
                </p>
              )}

              <div className="flex gap-2 mt-4">
                <button
                  onClick={() => setShowNewItemModal(false)}
                  className="flex-1 px-3 py-1.5 text-sm text-zinc-300 bg-surface-hover hover:bg-surface-border rounded"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateItem}
                  className="flex-1 px-3 py-1.5 text-sm text-surface-dark bg-accent-cyan hover:opacity-90 rounded font-medium"
                >
                  Create
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Rename Modal */}
      <AnimatePresence>
        {isRenaming && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
            onClick={() => setIsRenaming(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-surface-card border border-surface-border rounded-lg p-4 w-80 shadow-xl"
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-sm font-semibold text-white mb-3">
                Rename {renamingNode?.isFolder ? 'Folder' : 'File'}
              </h3>
              
              <input
                type="text"
                value={renameName}
                onChange={(e) => setRenameName(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') handleRenameSubmit();
                  if (e.key === 'Escape') setIsRenaming(false);
                }}
                className="w-full px-3 py-2 bg-surface-darker border border-surface-border rounded text-zinc-300 text-sm focus:border-accent-cyan outline-none"
                autoFocus
              />

              <div className="flex gap-2 mt-4">
                <button
                  onClick={() => setIsRenaming(false)}
                  className="flex-1 px-3 py-1.5 text-sm text-zinc-300 bg-surface-hover hover:bg-surface-border rounded"
                >
                  Cancel
                </button>
                <button
                  onClick={handleRenameSubmit}
                  className="flex-1 px-3 py-1.5 text-sm text-surface-dark bg-accent-cyan hover:opacity-90 rounded font-medium"
                >
                  Rename
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default FileExplorer;
