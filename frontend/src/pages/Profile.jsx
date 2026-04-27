import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useAuthStore, useProjectStore } from '../store';
import { authAPI } from '../services/api';
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

export default function Profile() {
  const { user, fetchProfile } = useAuthStore();
  const { projects, fetchProjects } = useProjectStore();
  const navigate = useNavigate();
  
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchProfile();
    fetchProjects();
  }, [fetchProfile, fetchProjects]);

  useEffect(() => {
    if (user) {
      setName(user.name || '');
      setEmail(user.email || '');
    }
  }, [user]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await authAPI.updateProfile({ fullName: name });
      toast.success('Profile updated!');
      setIsEditing(false);
      fetchProfile();
    } catch (error) {
      toast.error('Failed to update profile');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    
    setIsLoading(true);
    try {
      await authAPI.changePassword({ currentPassword, newPassword });
      toast.success('Password changed!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error) {
      toast.error('Failed to change password');
    } finally {
      setIsLoading(false);
    }
  };

  const stats = {
    projects: projects.length,
    stars: projects.reduce((sum, p) => sum + (p.stars || 0), 0),
  };

  return (
    <div className="min-h-screen bg-surface-dark flex">
      <Sidebar />
      
      <main className="flex-1 ml-64 p-8">
        <motion.div
          initial="hidden"
          animate="show"
          variants={container}
          className="max-w-4xl mx-auto"
        >
          <motion.div variants={item} className="mb-8">
            <h1 className="text-3xl font-bold text-white mb-2">Profile</h1>
            <p className="text-zinc-400">Manage your account settings</p>
          </motion.div>

          {/* Stats */}
          <motion.div variants={item} className="grid md:grid-cols-3 gap-4 mb-8">
            <div className="glass-card rounded-xl p-6 text-center">
              <div className="text-3xl font-bold gradient-text">{stats.projects}</div>
              <div className="text-zinc-400">Projects</div>
            </div>
            <div className="glass-card rounded-xl p-6 text-center">
              <div className="text-3xl font-bold gradient-text">{stats.stars}</div>
              <div className="text-zinc-400">Total Stars</div>
            </div>
            <div className="glass-card rounded-xl p-6 text-center">
              <div className="text-3xl font-bold gradient-text">
                {user?.createdAt ? new Date(user.createdAt).getFullYear() : '-'}
              </div>
              <div className="text-zinc-400">Member Since</div>
            </div>
          </motion.div>

          {/* Profile Info */}
          <motion.div variants={item} className="glass-card rounded-xl p-6 mb-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-white">Account Info</h2>
              <button
                onClick={() => setIsEditing(!isEditing)}
                className="text-accent-cyan hover:underline"
              >
                {isEditing ? 'Cancel' : 'Edit'}
              </button>
            </div>

            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  disabled={!isEditing}
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white disabled:opacity-50 disabled:cursor-not-allowed"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">Email</label>
                <input
                  type="email"
                  value={email}
                  disabled
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-zinc-500 disabled:cursor-not-allowed"
                />
              </div>
              {isEditing && (
                <motion.button
                  type="submit"
                  disabled={isLoading}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className="w-full py-3 bg-accent-cyan text-surface-dark font-semibold rounded-xl disabled:opacity-50"
                >
                  {isLoading ? 'Saving...' : 'Save Changes'}
                </motion.button>
              )}
            </form>
          </motion.div>

          {/* Change Password */}
          <motion.div variants={item} className="glass-card rounded-xl p-6">
            <h2 className="text-xl font-semibold text-white mb-6">Change Password</h2>
            
            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">
                  Current Password
                </label>
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">
                  New Password
                </label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-300 mb-2">
                  Confirm New Password
                </label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-4 py-3 bg-surface-darker border border-surface-border rounded-xl text-white"
                />
              </div>
              <motion.button
                type="submit"
                disabled={isLoading || !currentPassword || !newPassword}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className="w-full py-3 bg-accent-cyan text-surface-dark font-semibold rounded-xl disabled:opacity-50"
              >
                {isLoading ? 'Changing...' : 'Change Password'}
              </motion.button>
            </form>
          </motion.div>

          {/* Danger Zone */}
          <motion.div variants={item} className="glass-card rounded-xl p-6 mt-6 border border-red-500/30">
            <h2 className="text-xl font-semibold text-red-400 mb-4">Danger Zone</h2>
            <p className="text-zinc-400 text-sm mb-4">
              Once you delete your account, there is no going back. Please be certain.
            </p>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="px-6 py-2 bg-red-500 text-white font-semibold rounded-xl"
            >
              Delete Account
            </motion.button>
          </motion.div>
        </motion.div>
      </main>
    </div>
  );
}
