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
  const { projects, fetchProjects, isLoading } = useProjectStore();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    fetchProjects();
  }, [fetchProjects]);

  const handleCreateProject = async (data) => {
    try {
      const response = await projectAPI.create(data);
      toast.success('Project created!');
      setShowCreateModal(false);
      navigate(`/editor/${response.data.id}`);
    } catch (error) {
      toast.error('Failed to create project');
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
                      key={project.id}
                      variants={item}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, scale: 0.95 }}
                      transition={{ delay: index * 0.05 }}
                    >
                      <Link
                        to={`/editor/${project.id}`}
                        className="block h-full glass-card rounded-xl p-6 hover:border-accent-cyan/30 transition-all duration-300 group"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-accent-cyan/20 to-accent-magenta/20 flex items-center justify-center text-2xl">
                            {project.language === 'javascript' ? '🟨' : 
                             project.language === 'python' ? '🐍' :
                             project.language === 'java' ? '☕' :
                             project.language === 'typescript' ? '🔷' : '📄'}
                          </div>
                          <span className="text-xs px-2 py-1 bg-surface-hover rounded-lg text-zinc-400">
                            {project.visibility || 'private'}
                          </span>
                        </div>
                        <h3 className="text-lg font-semibold text-white mb-2 group-hover:text-accent-cyan transition-colors">
                          {project.name}
                        </h3>
                        <p className="text-zinc-400 text-sm line-clamp-2">
                          {project.description || 'No description'}
                        </p>
                        <div className="flex items-center gap-4 mt-4 text-xs text-zinc-500">
                          <span>🕐 {new Date(project.createdAt).toLocaleDateString()}</span>
                          <span>⭐ {project.stars || 0}</span>
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

      <AnimatePresence>
        {showCreateModal && (
          <CreateProjectModal
            onClose={() => setShowCreateModal(false)}
            onSubmit={handleCreateProject}
          />
        )}
      </AnimatePresence>
    </div>
  );
}