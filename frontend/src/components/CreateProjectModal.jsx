import { useState } from 'react';
import { motion } from 'framer-motion';

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

export default function CreateProjectModal({ onClose, onSubmit }) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [language, setLanguage] = useState('javascript');
  const [visibility, setVisibility] = useState('private');

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit({ name, description, language, visibility });
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
        className="w-full max-w-lg glass-card rounded-2xl p-6"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-2xl font-bold text-white mb-6">Create New Project</h2>

        <form onSubmit={handleSubmit} className="space-y-5">
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
                  {v}
                </button>
              ))}
            </div>
          </motion.div>

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
              Create Project
            </motion.button>
          </motion.div>
        </form>
      </motion.div>
    </motion.div>
  );
}