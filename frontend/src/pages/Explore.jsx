import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { useProjectStore, useAuthStore } from '../store';
import { projectAPI } from '../services/api';
import Sidebar from '../components/Sidebar';

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

const languages = ['all', 'javascript', 'typescript', 'python', 'java', 'go', 'rust', 'cpp', 'c'];

export default function Explore() {
  const user = useAuthStore((state) => state.user);
  const { 
    projects, 
    fetchPublicProjects, 
    searchProjects, 
    fetchProjectsByLanguage,
    toggleStar,
    forkProject,
    isLoading 
  } = useProjectStore();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('all');
  const [forkingProject, setForkingProject] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    console.log('Explore - User:', user);
    console.log('Explore - Projects:', projects);
    fetchPublicProjects();
  }, [fetchPublicProjects]);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      await searchProjects(searchQuery);
    } else {
      fetchPublicProjects();
    }
  };

  const handleLanguageFilter = async (lang) => {
    setSelectedLanguage(lang);
    if (lang === 'all') {
      fetchPublicProjects();
    } else {
      await fetchProjectsByLanguage(lang);
    }
  };

  const handleStar = async (projectId, e) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      await toggleStar(projectId);
    } catch (error) {
      toast.error('Failed to star project');
    }
  };

  const handleFork = async (projectId, e) => {
    e.preventDefault();
    e.stopPropagation();
    setForkingProject(projectId);
    try {
      const forked = await forkProject(projectId);
      toast.success('Project forked successfully!');
      navigate(`/editor/${forked.projectId}`);
    } catch (error) {
      toast.error('Failed to fork project: ' + (error.response?.data?.message || error.message));
    } finally {
      setForkingProject(null);
    }
  };

  const filteredProjects = projects.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.description?.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesLang = selectedLanguage === 'all' || p.language === selectedLanguage;
    return matchesSearch && matchesLang;
  });

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
          <motion.div variants={item} className="mb-8">
            <h1 className="text-3xl font-bold text-white mb-2">Explore</h1>
            <p className="text-zinc-400">Discover public projects from the community</p>
          </motion.div>

          <motion.div variants={item} className="flex flex-col md:flex-row gap-4 mb-8">
            <form onSubmit={handleSearch} className="flex-1 relative">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search projects..."
                className="w-full px-6 py-4 bg-surface-card border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors pl-12"
              />
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500">🔍</span>
            </form>
            
            <div className="flex gap-2 flex-wrap">
              {languages.map((lang) => (
                <button
                  key={lang}
                  onClick={() => handleLanguageFilter(lang)}
                  className={`px-4 py-2 rounded-lg text-sm capitalize transition-colors ${
                    selectedLanguage === lang
                      ? 'bg-accent-cyan/20 text-accent-cyan'
                      : 'bg-surface-card text-zinc-400 hover:text-white'
                  }`}
                >
                  {lang}
                </button>
              ))}
            </div>
          </motion.div>

          <motion.div variants={item}>
            {isLoading ? (
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
                {[1, 2, 3, 4, 5, 6].map((i) => (
                  <div key={i} className="h-64 bg-surface-card rounded-xl animate-pulse" />
                ))}
              </div>
            ) : filteredProjects.length === 0 ? (
              <div className="text-center py-20">
                <div className="text-6xl mb-4">🔍</div>
                <h3 className="text-xl font-semibold text-white mb-2">No projects found</h3>
                <p className="text-zinc-400">Try adjusting your search or filters</p>
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
                      transition={{ delay: index * 0.05 }}
                    >
                      <Link
                        to={`/editor/${project.projectId}`}
                        className="block h-full glass-card rounded-xl p-6 hover:border-accent-cyan/30 transition-all duration-300 relative"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-accent-violet/20 to-accent-rose/20 flex items-center justify-center text-2xl">
                            {project.language === 'javascript' ? '🟨' : 
                             project.language === 'python' ? '🐍' :
                             project.language === 'java' ? '☕' :
                             project.language === 'typescript' ? '🔷' :
                             project.language === 'go' ? '🐹' :
                             project.language === 'rust' ? '🦀' : '📄'}
                          </div>
                          {/* Language badge + Star and Fork buttons in same row */}
                          <div className="flex items-center gap-2">
                            <span className="text-xs px-2 py-1 bg-green-500/20 text-green-400 rounded-lg">
                              {project.language || 'text'}
                            </span>
                            {user && (
                              <div className="flex items-center gap-1">
                                <button
                                  onClick={(e) => handleStar(project.projectId, e)}
                                  className="p-1 rounded-md bg-surface-hover/40 text-zinc-500 hover:bg-yellow-500/80 hover:text-white hover:shadow-[0_0_12px_rgba(234,179,8,0.6)] transition-all duration-200"
                                  title="Star project"
                                >
                                  <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor">
                                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                                  </svg>
                                </button>
                                <button
                                  onClick={(e) => handleFork(project.projectId, e)}
                                  disabled={forkingProject === project.projectId}
                                  className="p-1 rounded-md bg-surface-hover/40 text-zinc-500 hover:bg-green-500/80 hover:text-white hover:shadow-[0_0_12px_rgba(34,197,94,0.6)] transition-all duration-200 disabled:opacity-30 disabled:hover:bg-surface-hover/40 disabled:hover:text-zinc-500 disabled:hover:shadow-none"
                                  title="Fork project"
                                >
                                  {forkingProject === project.projectId ? (
                                    <svg className="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
                                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                  ) : (
                                    <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor">
                                      <path fillRule="evenodd" d="M7.707 3.293a1 1 0 010 1.414L5.414 7H11a7 7 0 017 7v2a1 1 0 11-2 0v-2a5 5 0 00-5-5H5.414l2.293 2.293a1 1 0 11-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                                    </svg>
                                  )}
                                </button>
                              </div>
                            )}
                          </div>
                        </div>
                        <h3 className="text-lg font-semibold text-white mb-2 group-hover:text-accent-cyan transition-colors">
                          {project.name}
                        </h3>
                        <p className="text-zinc-400 text-sm line-clamp-2 mb-4">
                          {project.description || 'No description'}
                        </p>
                        <div className="flex items-center gap-4 text-xs text-zinc-500">
                          <span>👤 ID: {project.ownerId}</span>
                          <span>⭐ {project.starCount || 0}</span>
                          <span>🍴 {project.forkCount || 0}</span>
                          <span>📅 {new Date(project.createdAt).toLocaleDateString()}</span>
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
    </div>
  );
}