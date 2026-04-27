import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store';
import Navbar from '../components/Navbar';

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

export default function Login() {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const login = useAuthStore((state) => state.login);
  const isLoading = useAuthStore((state) => state.isLoading);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await login(identifier, password);
    if (result.success) {
      toast.success('Welcome back!');
      navigate('/dashboard');
    } else {
      toast.error(result.error);
    }
  };

  return (
    <div className="min-h-screen bg-surface-dark grid-bg flex flex-col">
      <Navbar />
      
      <div className="flex-1 flex items-center justify-center px-4 py-20">
        <motion.div
          variants={container}
          initial="hidden"
          animate="show"
          className="w-full max-w-md"
        >
          <motion.div variants={item} className="glass-card rounded-2xl p-8">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-bold text-white mb-2">Welcome Back</h1>
              <p className="text-zinc-400">Sign in to continue coding</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">
                  Email or Username
                </label>
                <input
                  type="text"
                  value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors"
                  placeholder="you@example.com"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan transition-colors"
                  placeholder="••••••••"
                  required
                />
              </div>

              <motion.button
                type="submit"
                disabled={isLoading}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className="w-full py-3 bg-gradient-to-r from-accent-cyan to-accent-magenta text-surface-dark font-semibold rounded-xl disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? 'Signing in...' : 'Sign In'}
              </motion.button>
            </form>

            <div className="mt-6 text-center">
              <p className="text-zinc-400">
                Don't have an account?{' '}
                <Link to="/register" className="text-accent-cyan hover:underline">
                  Sign up
                </Link>
              </p>
            </div>
          </motion.div>

          <motion.div variants={item} className="mt-8 text-center">
            <Link to="/" className="text-zinc-500 hover:text-zinc-300 transition-colors">
              ← Back to home
            </Link>
          </motion.div>
        </motion.div>
      </div>
    </div>
  );
}
