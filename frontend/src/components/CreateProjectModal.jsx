import { useState, useEffect, useCallback, useRef } from 'react';
import { motion } from 'framer-motion';
import { useAuthStore } from '../store';
import { authAPI } from '../services/api';

const languages = [
  { value: 'javascript', label: 'JavaScript', icon: '🟨' },
  { value: 'typescript', label: 'TypeScript', icon: '🔷' },
  { value: 'python', label: 'Python', icon: '🐍' },
  { value: 'java', label: 'Java', icon: '☕' },
  { value: 'cpp', label: 'C++', icon: '⚙️' },
  { value: 'go', label: 'Go', icon: '🐹' },
  { value: 'rust', label: 'Rust', icon: '🦀' },
  { value: 'html', label: 'HTML', icon: '🌐' },
  { value: 'css', label: 'CSS', icon: '🎨' },
];

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.05 },
  },
};

const item = {
  hidden: { y: 20, opacity: 0 },
  show: { y: 0, opacity: 1 },
};

export default function CreateProjectModal({ onClose, onSubmit, projectData = null }) {
  const [name, setName] = useState(projectData?.name || '');
  const [description, setDescription] = useState(projectData?.description || '');
  const [language, setLanguage] = useState(projectData?.language || 'javascript');
  const [visibility, setVisibility] = useState(projectData?.visibility || 'private');
  const [templateId, setTemplateId] = useState(projectData?.templateId || null);
  
  // Collaborator state
  const [collaborators, setCollaborators] = useState([]); // { userId, username, email }
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  
  const user = useAuthStore((state) => state.user);
  const currentUserId = user?.id || user?.userId;
  
  // Debounced search ref
  const searchDebounceRef = useRef(null);
  
  // Real user search via auth API
  const handleSearch = useCallback(async (query) => {
    if (!query.trim() || query.trim().length < 2) {
      setSearchResults([]);
      return;
    }
    
    setSearchLoading(true);
    try {
      const response = await authAPI.searchUsers(query, 0, 10);
      const users = response.data?.content || response.data || [];
      
      // Filter out current user and already-added collaborators
      const addedIds = new Set(collaborators.map(c => c.userId));
      const filtered = users.filter(u => {
        const uid = u.userId || u.id;
        return uid !== currentUserId && !addedIds.has(uid);
      });
      
      setSearchResults(filtered);
    } catch (error) {
      console.error('User search error:', error);
      setSearchResults([]);
    } finally {
      setSearchLoading(false);
    }
  }, [collaborators, currentUserId]);
  
  const debouncedSearch = useCallback((query) => {
    if (searchDebounceRef.current) {
      clearTimeout(searchDebounceRef.current);
    }
    searchDebounceRef.current = setTimeout(() => {
      handleSearch(query);
    }, 300);
  }, [handleSearch]);
  
  const handleSearchChange = (e) => {
    const query = e.target.value;
    setSearchQuery(query);
    debouncedSearch(query);
  };
  
  const addCollaborator = (userObj) => {
    const uid = userObj.userId || userObj.id;
    if (!collaborators.find(c => c.userId === uid)) {
      setCollaborators([...collaborators, {
        userId: uid,
        username: userObj.username,
        email: userObj.email,
      }]);
    }
    setSearchQuery('');
    setSearchResults([]);
  };
  
  const removeCollaborator = (userId) => {
    setCollaborators(collaborators.filter(c => c.userId !== userId));
  };
  
  const handleSubmit = (e) => {
    e.preventDefault();
    const data = {
      name,
      description,
      language,
      visibility,
      templateId: templateId || undefined,
      // Pass collaborator IDs so Dashboard can add them after project creation
      collaboratorIds: collaborators.map(c => c.userId),
    };
    onSubmit(data);
  };
  
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="w-full max-w-xl glass-card rounded-2xl p-6 max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-2xl font-bold text-white mb-6">
          {!projectData ? 'Create New Project' : 'Edit Project'}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Project Name */}
          <motion.div variants={item}>
            <label className="block text-sm font-medium text-zinc-300 mb-2">
              Project Name
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors"
              placeholder="my-awesome-project"
              required
            />
          </motion.div>

          {/* Description */}
          <motion.div variants={item}>
            <label className="block text-sm font-medium text-zinc-300 mb-2">
              Description
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors resize-none h-24"
              placeholder="What does this project do?"
            />
          </motion.div>

          {/* Language */}
          <motion.div variants={item}>
            <label className="block text-sm font-medium text-zinc-300 mb-2">
              Language
            </label>
            <div className="grid grid-cols-3 gap-2">
              {languages.map((lang) => (
                <button
                  key={lang.value}
                  type="button"
                  onClick={() => setLanguage(lang.value)}
                  className={`px-3 py-2 rounded-xl text-sm transition-all ${
                    language === lang.value
                      ? 'bg-accent-cyan/20 border border-accent-cyan text-accent-cyan'
                      : 'bg-surface-darker border border-surface-border text-zinc-400 hover:border-zinc-500'
                  }`}
                >
                  {lang.icon} {lang.label}
                </button>
              ))}
            </div>
          </motion.div>

          {/* Visibility */}
          <motion.div variants={item}>
            <label className="block text-sm font-medium text-zinc-300 mb-2">
              Visibility
            </label>
            <div className="flex gap-3">
              {['public', 'private'].map((v) => (
                <button
                  key={v}
                  type="button"
                  onClick={() => setVisibility(v)}
                  className={`flex-1 px-4 py-3 rounded-xl text-sm capitalize transition-all ${
                    visibility === v
                      ? 'bg-accent-cyan/20 border border-accent-cyan text-accent-cyan'
                      : 'bg-surface-darker border border-surface-border text-zinc-400 hover:border-zinc-500'
                  }`}
                >
                  {v === 'public' ? '🌍 ' : '🔒 '}{v}
                </button>
              ))}
            </div>
          </motion.div>

          {/* Collaborators Section */}
          <motion.div variants={item}>
            <div className="space-y-3">
              <label className="block text-sm font-medium text-zinc-300">
                Add Collaborators
              </label>
              
              {/* Added Collaborators */}
              {collaborators.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {collaborators.map((collab) => (
                    <div
                      key={collab.userId}
                      className="flex items-center gap-2 bg-surface-hover rounded-lg px-3 py-1.5"
                    >
                      <div className="w-6 h-6 rounded-full bg-gradient-to-br from-accent-violet to-accent-rose flex items-center justify-center text-[10px] text-white font-bold">
                        {collab.username?.charAt(0).toUpperCase() || 'U'}
                      </div>
                      <span className="text-xs text-white">{collab.username}</span>
                      <button
                        type="button"
                        onClick={() => removeCollaborator(collab.userId)}
                        className="text-zinc-500 hover:text-red-400 text-sm leading-none"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              )}
              
              {/* Search Input */}
              <div className="relative">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={handleSearchChange}
                  placeholder="Search users by name or email..."
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors pl-10"
                />
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500 text-sm">👤</span>
                {searchLoading && (
                  <span className="absolute right-3 top-1/2 -translate-y-1/2">
                    <svg className="h-4 w-4 animate-spin text-accent-cyan" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"></path>
                    </svg>
                  </span>
                )}
              </div>
              
              {/* Search Results Dropdown */}
              {searchResults.length > 0 && (
                <div className="max-h-40 overflow-y-auto border border-surface-border rounded-xl bg-surface-card">
                  {searchResults.map((u) => {
                    const uid = u.userId || u.id;
                    return (
                      <div
                        key={uid}
                        className="flex items-center px-4 py-2.5 border-b border-surface-border/50 last:border-b-0 hover:bg-surface-hover cursor-pointer transition-colors"
                        onClick={() => addCollaborator(u)}
                      >
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent-violet to-accent-rose flex items-center justify-center text-xs text-white font-bold flex-shrink-0">
                          {u.username?.charAt(0).toUpperCase() || 'U'}
                        </div>
                        <div className="flex-1 ml-3 min-w-0">
                          <p className="text-sm text-white font-medium truncate">{u.username}</p>
                          <p className="text-xs text-zinc-500 truncate">{u.email}</p>
                        </div>
                        <span className="text-xs text-accent-cyan ml-2 flex-shrink-0">+ Add</span>
                      </div>
                    );
                  })}
                </div>
              )}
              
              {searchResults.length === 0 && searchQuery.trim().length >= 2 && !searchLoading && (
                <p className="text-xs text-zinc-500 text-center py-2">
                  No users found matching "{searchQuery}"
                </p>
              )}
            </div>
          </motion.div>

          {/* Form Actions */}
          <motion.div variants={item} className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-3 border border-surface-border text-zinc-400 rounded-xl hover:bg-surface-hover transition-colors"
            >
              Cancel
            </button>
            <motion.button
              type="submit"
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="flex-1 px-4 py-3 bg-gradient-to-r from-accent-cyan to-accent-magenta text-surface-dark font-semibold rounded-xl"
            >
              {!projectData ? 'Create Project' : 'Update Project'}
            </motion.button>
          </motion.div>
        </form>
      </motion.div>
    </motion.div>
  );
}