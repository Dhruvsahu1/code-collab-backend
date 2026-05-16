import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { useAuthStore, useProjectStore } from '../store';
import { projectAPI } from '../services/api';
import Sidebar from '../components/Sidebar';
import CreateProjectModal from '../components/CreateProjectModal';

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

export default function Dashboard() {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const { projects, fetchProjects, deleteProject, isLoading } = useProjectStore();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [deleteConfirm, setDeleteConfirm] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    console.log('User:', user);
    console.log('Projects:', projects);
    fetchProjects();
  }, [fetchProjects]);

  const handleCreateProject = async (data) => {
    try {
      const { collaboratorIds, ...projectFields } = data;
      const projectData = {
        ...projectFields,
        ownerId: user?.id || user?.userId
      };
      const response = await projectAPI.create(projectData);
      const newProjectId = response.data.projectId;

      // Add collaborators if any were selected
      if (collaboratorIds && collaboratorIds.length > 0) {
        const addPromises = collaboratorIds.map(userId =>
          projectAPI.addCollaborator(newProjectId, userId).catch(err => {
            console.warn(`Failed to add collaborator ${userId}:`, err.message);
          })
        );
        await Promise.all(addPromises);
      }

      toast.success('Project created!');
      setShowCreateModal(false);
      fetchProjects(); // Refresh the project list
      navigate(`/editor/${newProjectId}`);
    } catch (error) {
      console.error('Project creation error:', error.response?.data);
      toast.error('Failed to create project: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDeleteProject = async (projectId) => {
    try {
      await deleteProject(projectId);
      toast.success('Project deleted!');
      setDeleteConfirm(null);
    } catch (error) {
      console.error('Delete error:', error.response?.data);
      toast.error('Failed to delete project: ' + (error.response?.data?.message || error.message));
    }
  };

  const filteredProjects = projects.filter(p => 
    p.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-surface-dark flex">
      <Sidebar />
      
      <main className="flex-1 ml-64 p-8">
        <motion.div
          initial="hidden"
          animate="show"
          variants={container}
          className="max-w-6xl mx-auto"
        >
          <motion.div variants={item} className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-bold text-white">
                Welcome back, {user?.name?.split(' ')[0]} 👋
              </h1>
              <p className="text-zinc-400 mt-1">Ready to create something amazing?</p>
            </div>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => setShowCreateModal(true)}
              className="px-6 py-3 bg-gradient-to-r from-accent-cyan to-accent-magenta text-surface-dark font-semibold rounded-xl"
            >
              New Project
            </motion.button>
          </motion.div>

          <motion.div variants={item} className="mb-6">
            <div className="relative">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search projects..."
                className="w-full px-6 py-4 bg-surface-card border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors pl-12"
              />
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500">🔍</span>
            </div>
          </motion.div>

          <motion.div variants={item}>
            <h2 className="text-xl font-semibold text-white mb-4">Your Projects</h2>
            
            {isLoading ? (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="h-48 bg-surface-card rounded-xl animate-pulse" />
                ))}
              </div>
            ) : filteredProjects.length === 0 ? (
              <div className="text-center py-20">
                <div className="text-6xl mb-4">📁</div>
                <h3 className="text-xl font-semibold text-white mb-2">No projects yet</h3>
                <p className="text-zinc-400 mb-6">Create your first project to get started</p>
                <motion.button
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  onClick={() => setShowCreateModal(true)}
                  className="px-6 py-3 bg-surface-hover border border-surface-border text-white rounded-xl"
                >
                  Create Project
                </motion.button>
              </div>
            ) : (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                <AnimatePresence>
                  {filteredProjects.map((project, index) => (
                    <motion.div
                      key={project.projectId}
                      variants={item}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, scale: 0.95 }}
                      transition={{ delay: index * 0.05 }}
                    >
                      <Link
                        to={`/editor/${project.projectId}`}
                        className="block h-full glass-card rounded-xl p-6 hover:border-accent-cyan/30 transition-all duration-300 relative"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-accent-cyan/20 to-accent-magenta/20 flex items-center justify-center text-2xl">
                            {project.language === 'javascript' ? '🟨' : 
                             project.language === 'python' ? '🐍' :
                             project.language === 'java' ? '☕' :
                             project.language === 'typescript' ? '🔷' : '📄'}
                          </div>
                          {/* Public/Private badge + Delete button in same row */}
                          <div className="flex items-center gap-2">
                            <span className={`text-xs px-2 py-1 rounded-lg ${
                              project.visibility === 'PUBLIC' 
                                ? 'bg-green-500/20 text-green-400' 
                                : 'bg-surface-hover text-zinc-400'
                            }`}>
                              {project.visibility || 'PRIVATE'}
                            </span>
                            {String(project.ownerId) === String(user?.id) && (
                              <button
                                onClick={(e) => {
                                  e.preventDefault();
                                  e.stopPropagation();
                                  setDeleteConfirm(project.projectId);
                                }}
                                className="p-1 rounded-md bg-surface-hover/40 text-zinc-500 hover:bg-red-500/80 hover:text-white hover:shadow-[0_0_12px_rgba(239,68,68,0.6)] transition-all duration-200"
                                title="Delete project"
                              >
                                <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor">
                                  <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                                </svg>
                              </button>
                            )}
                          </div>
                        </div>
                        <h3 className="text-lg font-semibold text-white mb-2 group-hover:text-accent-cyan transition-colors">
                          {project.name}
                        </h3>
                        <p className="text-zinc-400 text-sm line-clamp-2">
                          {project.description || 'No description'}
                        </p>
                        <div className="flex items-center gap-4 mt-4 text-xs text-zinc-500">
                          <span>🕐 {new Date(project.createdAt).toLocaleDateString()}</span>
                          <span>⭐ {project.starCount || 0}</span>
                          <span>🍴 {project.forkCount || 0}</span>
                        </div>
                      </Link>
                    </motion.div>
                  ))}
                </AnimatePresence>
              </div>
            )}
          </motion.div>
        </motion.div>
      </main>

      {/* Create Project Modal */}
      <AnimatePresence>
        {showCreateModal && (
          <CreateProjectModal
            onClose={() => setShowCreateModal(false)}
            onSubmit={handleCreateProject}
          />
        )}
      </AnimatePresence>

      {/* Delete Confirmation Modal */}
      <AnimatePresence>
        {deleteConfirm && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
            onClick={() => setDeleteConfirm(null)}
          >
            <motion.div
              initial={{ scale: 0.9 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.9 }}
              className="bg-surface-card border border-surface-border rounded-2xl p-6 max-w-md w-full mx-4"
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-xl font-bold text-white mb-2">Delete Project?</h3>
              <p className="text-zinc-400 mb-6">
                This action cannot be undone. All files and data associated with this project will be permanently deleted.
              </p>
              <div className="flex gap-4">
                <button
                  onClick={() => setDeleteConfirm(null)}
                  className="flex-1 px-4 py-2 bg-surface-hover border border-surface-border text-white rounded-xl hover:bg-surface-border transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={() => handleDeleteProject(deleteConfirm)}
                  className="flex-1 px-4 py-2 bg-red-600 text-white rounded-xl hover:bg-red-700 transition-colors"
                >
                  Delete
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}