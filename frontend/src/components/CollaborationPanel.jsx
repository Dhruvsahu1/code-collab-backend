import { useState, useEffect, useCallback } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useAuthStore, useProjectStore } from '../store';
import { collabAPI } from '../services/api';
import collabWebSocket from '../services/collabWebSocket';

const cursorColors = ['#22d3ee', '#e879f9', '#fbbf24', '#fb7185', '#a78bfa'];

export default function CollaborationPanel() {
  const user = useAuthStore((state) => state.user);
  const { currentFile } = useProjectStore();
  const [session, setSession] = useState(null);
  const [collaborators, setCollaborators] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isConnected, setIsConnected] = useState(false);

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
      setSession(response.data);
      toast.success('Collaboration session started!');
    } catch (error) {
      console.error('Failed to start session:', error);
      toast.error('Failed to start session: ' + (error.response?.data?.message || error.message));
    } finally {
      setIsLoading(false);
    }
  };

  const connectToSession = useCallback((sessionId) => {
    collabWebSocket.connect(
      sessionId,
      () => {
        setIsConnected(true);
        toast.success('Connected to collaboration session!');
      },
      (code, userId) => {
        console.log('Code update received:', code);
      },
      (cursorData) => {
        setCollaborators((prev) => {
          const existing = prev.find((c) => c.id === cursorData.userId);
          if (existing) {
            return prev.map((c) => (c.id === cursorData.userId ? { ...c, ...cursorData } : c));
          }
          return [...prev, { id: cursorData.userId, name: cursorData.userName, color: cursorData.color }];
        });
      },
      (message) => {
        console.log('Participant update:', message);
        if (message.type === 'SESSION_ENDED') {
          toast.error('Session has ended');
          collabWebSocket.disconnect();
          setSession(null);
          setCollaborators([]);
          setIsConnected(false);
        } else if (message.type === 'PARTICIPANT_JOINED') {
          setCollaborators(prev => [...prev, { id: message.userId, name: `User ${message.userId}`, color: message.color }]);
        } else if (message.type === 'PARTICIPANT_LEFT') {
          setCollaborators(prev => prev.filter(c => c.id !== message.userId));
        }
      }
    );
  }, []);

  const copySessionLink = () => {
    const link = `${window.location.origin}/collab/${session.sessionId}`;
    navigator.clipboard.writeText(link);
    toast.success('Link copied to clipboard!');
  };

  const endCollaboration = async () => {
    try {
      // Use WebSocket to end session and notify all participants
      collabWebSocket.endSession();
      
      // Also call REST API to close the session on backend
      await collabAPI.closeSession(session.sessionId);
      
      collabWebSocket.disconnect();
      setSession(null);
      setCollaborators([]);
      setIsConnected(false);
      toast.success('Session closed');
    } catch (error) {
      console.error('Failed to close session:', error);
    }
  };

  useEffect(() => {
    if (session && user) {
      connectToSession(session.sessionId);
    }

    return () => {
      if (isConnected) {
        collabWebSocket.disconnect();
      }
    };
  }, [session?.sessionId, user?.id]);

  useEffect(() => {
    if (session) {
      setCollaborators([{ id: user.id, name: user.name, color: cursorColors[0] }]);
    }
  }, [session, user]);

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 border-b border-surface-border">
        <h3 className="text-sm font-semibold text-white">Collaboration</h3>
      </div>
      
      <div className="flex-1 p-4 overflow-y-auto">
        {!session ? (
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
                  value={`codesync.app/collab/${session.sessionId}`}
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
              <label className="text-xs text-zinc-500">Active Collaborators</label>
              <div className="mt-2 space-y-2">
                {collaborators.map((collab, index) => (
                  <motion.div
                    key={collab.id}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="flex items-center gap-2"
                  >
                    <div
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: collab.color || cursorColors[index % cursorColors.length] }}
                    />
                    <span className="text-sm text-white">{collab.name}</span>
                    {collab.id === user.id && <span className="text-xs text-green-400">● you</span>}
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
