import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { fileAPI } from '../services/api';
import { useProjectStore } from '../store';

export default function FileExplorer({ onFileSelect }) {
  const { currentProject, currentFile, setCurrentFile } = useProjectStore();
  const [files, setFiles] = useState([]);
  const [expandedFolders, setExpandedFolders] = useState(new Set());
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (currentProject?.id) {
      loadFiles();
    }
  }, [currentProject?.id]);

  const loadFiles = async () => {
    setIsLoading(true);
    try {
      const response = await fileAPI.getTree(currentProject.id);
      setFiles(response.data || []);
    } catch (error) {
      console.error('Failed to load files');
    } finally {
      setIsLoading(false);
    }
  };

  const toggleFolder = (id) => {
    setExpandedFolders((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleFileClick = (file) => {
    if (file.isFolder) {
      toggleFolder(file.id);
    } else {
      setCurrentFile(file);
      onFileSelect(file);
    }
  };

  const getFileIcon = (file) => {
    if (file.isFolder) {
      return expandedFolders.has(file.id) ? '📂' : '📁';
    }
    const ext = file.name.split('.').pop();
    const icons = {
      js: '🟨',
      ts: '🔷',
      jsx: '⚛️',
      tsx: '⚛️',
      py: '🐍',
      java: '☕',
      go: '🐹',
      rs: '🦀',
      html: '🌐',
      css: '🎨',
      json: '📋',
      md: '📝',
    };
    return icons[ext] || '📄';
  };

  const renderFileTree = (items, depth = 0) => {
    return items.map((item) => (
      <motion.div
        key={item.id}
        initial={{ opacity: 0, x: -10 }}
        animate={{ opacity: 1, x: 0 }}
      >
        <button
          onClick={() => handleFileClick(item)}
          className={`w-full flex items-center gap-2 px-3 py-2 text-left hover:bg-surface-hover transition-colors ${
            currentFile?.id === item.id ? 'bg-surface-hover border-l-2 border-accent-cyan' : ''
          }`}
          style={{ paddingLeft: `${depth * 12 + 12}px` }}
        >
          <span className="text-base">{getFileIcon(item)}</span>
          <span className="text-sm text-zinc-300 truncate">{item.name}</span>
        </button>
        {item.isFolder && expandedFolders.has(item.id) && item.children && (
          <div>{renderFileTree(item.children, depth + 1)}</div>
        )}
      </motion.div>
    ));
  };

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 border-b border-surface-border">
        <h3 className="text-sm font-semibold text-white">Files</h3>
      </div>
      
      <div className="flex-1 overflow-y-auto py-2">
        {isLoading ? (
          <div className="p-4 space-y-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-8 bg-surface-hover rounded animate-pulse" />
            ))}
          </div>
        ) : files.length === 0 ? (
          <div className="p-4 text-center text-zinc-500 text-sm">
            <p>No files yet</p>
            <button
              onClick={loadFiles}
              className="mt-2 text-accent-cyan hover:underline"
            >
              Refresh
            </button>
          </div>
        ) : (
          renderFileTree(files)
        )}
      </div>

      <div className="p-3 border-t border-surface-border">
        <button
          onClick={async () => {
            const name = prompt('File name:');
            if (!name) return;
            try {
              await fileAPI.create({
                name,
                projectId: currentProject.id,
                content: '',
              });
              loadFiles();
              toast.success('File created');
            } catch (error) {
              toast.error('Failed to create file');
            }
          }}
          className="w-full px-3 py-2 text-sm text-zinc-400 hover:text-white hover:bg-surface-hover rounded-lg transition-colors"
        >
          + New File
        </button>
      </div>
    </div>
  );
}