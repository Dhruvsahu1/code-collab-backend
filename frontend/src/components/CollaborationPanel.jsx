import { useState, useCallback } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useAuthStore, useProjectStore, useCollabStore } from '../store';
import { collabAPI } from '../services/api';

const cursorColors = ['#22d3ee', '#e879f9', '#fbbf24', '#fb7185', '#a78bfa', '#FF5733', '#33A1FF', '#33FF57'];

/**
 * CollaborationPanel — display-only panel.
 * WebSocket connection is managed by Editor.jsx to prevent double-connections.
 * This panel only handles: create session, copy link, display participants, end session.
 */
export default function CollaborationPanel() {
  const user = useAuthStore((state) => state.user);
  const { currentFile } = useProjectStore();
  const { sessionId, participants, isConnected, myColor, setSessionId, clearCollab, setMyColor } = useCollabStore();
  const [isLoading, setIsLoading] = useState(false);

  const startCollaboration = async () => {
    setIsLoading(true);
    try {
      const projectStore = useProjectStore.getState();
      const projectId = projectStore.currentProject?.projectId || 1;
      const fileId = currentFile?.id || 1;
      const response = await collabAPI.createSession({
        projectId,
        fileId,
        ownerId: user.id,
      });
      const newSessionId = response.data.sessionId;
      setSessionId(newSessionId);
      setMyColor(cursorColors[0]);
      toast.success('Collaboration session started! Share the link to invite others.');
      
      // Navigate to the collab URL so the Editor picks up the sessionId
      window.location.href = `/collab/${newSessionId}`;
    } catch (error) {
      console.error('Failed to start session:', error);
      toast.error('Failed to start session: ' + (error.response?.data?.message || error.message));
    } finally {
      setIsLoading(false);
    }
  };

  const copySessionLink = () => {
    const link = `${window.location.origin}/collab/${sessionId}`;
    navigator.clipboard.writeText(link);
    toast.success('Link copied to clipboard!');
  };

  const endCollaboration = async () => {
    try {
      await collabAPI.closeSession(sessionId);
      clearCollab();
      toast.success('Session closed');
      window.location.href = '/dashboard';
    } catch (error) {
      console.error('Failed to close session:', error);
    }
  };

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 border-b border-surface-border">
        <h3 className="text-sm font-semibold text-white">Collaboration</h3>
      </div>
      
      <div className="flex-1 p-4 overflow-y-auto">
        {!sessionId ? (
          <div className="text-center py-8">
            <p className="text-zinc-400 text-sm mb-6">
              Start a collaboration session to code together in real-time
            </p>
            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={startCollaboration}
              disabled={isLoading}
              className="w-full px-4 py-2 bg-accent-cyan text-surface-dark font-semibold rounded-lg disabled:opacity-50"
            >
              {isLoading ? 'Starting...' : 'Start Session'}
            </motion.button>
          </div>
        ) : (
          <>
            <div className="mb-4">
              <label className="text-xs text-zinc-500">Session Link</label>
              <div className="flex items-center gap-2 mt-1">
                <input
                  type="text"
                  readOnly
                  value={`${window.location.origin}/collab/${sessionId}`}
                  className="flex-1 px-3 py-2 bg-surface-darker border border-surface-border rounded-lg text-xs text-zinc-300"
                />
                <button
                  onClick={copySessionLink}
                  className="px-3 py-2 bg-surface-hover rounded-lg text-zinc-300 hover:text-white"
                >
                  📋
                </button>
              </div>
            </div>

            <div className="mb-4">
              <label className="text-xs text-zinc-500">Connection Status</label>
              <div className="mt-1 flex items-center gap-2">
                <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-400' : 'bg-yellow-400'}`} />
                <span className="text-xs text-zinc-400">{isConnected ? 'Connected' : 'Connecting...'}</span>
              </div>
            </div>

            <div className="mb-4">
              <label className="text-xs text-zinc-500">Active Collaborators ({participants.length})</label>
              <div className="mt-2 space-y-2">
                {participants.map((collab, index) => (
                  <motion.div
                    key={collab.userId}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="flex items-center gap-2"
                  >
                    <div
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: collab.color || cursorColors[index % cursorColors.length] }}
                    />
                    <span className="text-sm text-white">{collab.username || `User ${collab.userId}`}</span>
                    {collab.userId === user?.id && <span className="text-xs text-green-400">● you</span>}
                  </motion.div>
                ))}
              </div>
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={endCollaboration}
              className="w-full px-4 py-2 bg-red-600 text-white font-semibold rounded-lg"
            >
              End Session
            </motion.button>
          </>
        )}
      </div>
    </div>
  );
}