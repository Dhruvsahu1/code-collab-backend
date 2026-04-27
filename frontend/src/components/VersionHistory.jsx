import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useProjectStore, useVersionStore, useAuthStore, useEditorStore } from '../store';
import toast from 'react-hot-toast';
import { versionAPI } from '../services/api';

export default function VersionHistory({ onShowDiff }) {
  const { currentFile, currentProject } = useProjectStore();
  const { snapshots, isLoading, fetchFileHistory, restoreSnapshot, createSnapshot } = useVersionStore();
  const { user } = useAuthStore();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [commitMessage, setCommitMessage] = useState('');
  const [selectedSnapshots, setSelectedSnapshots] = useState([]);
  const maxSelections = 2;

  useEffect(() => {
    if (currentFile?.fileId) {
      fetchFileHistory(currentFile.fileId);
    }
  }, [currentFile?.fileId, fetchFileHistory]);

  const handleCreateSnapshot = async () => {
    if (!currentFile || !user) return;

    try {
      const code = useEditorStore.getState().code;
      const authorId = user.id || user.userId || user.sub;
      if (!authorId) {
        toast.error('User ID not found. Please log in again.');
        return;
      }
      
      const projectId = currentProject?.projectId || currentFile?.projectId || currentFile?.project?.id;
      if (!projectId) {
        toast.error('No project selected. Please open a file from a project.');
        return;
      }
      
      await createSnapshot({
        projectId: projectId,
        fileId: currentFile.fileId,
        authorId: authorId,
        message: commitMessage || 'Auto-saved version',
        content: code,
        branch: 'main',
      });
      setCommitMessage('');
      setShowCreateModal(false);
      toast.success('Snapshot created successfully');
    } catch (error) {
      toast.error('Failed to create snapshot: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleRestore = async (snapshotId) => {
    if (!user) {
      toast.error('You must be logged in to restore');
      return;
    }

    const confirmed = window.confirm('Are you sure you want to restore this version? This will create a new snapshot.');
    if (!confirmed) return;

    try {
      const authorId = user.id || user.userId || user.sub;
      await restoreSnapshot(snapshotId, authorId, `Restored to snapshot ${snapshotId}`);
      toast.success('Snapshot restored successfully');
    } catch (error) {
      toast.error('Failed to restore snapshot: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleTag = async (snapshotId, tag) => {
    try {
      await versionAPI.tagSnapshot(snapshotId, tag);
      fetchFileHistory(currentFile.fileId);
      toast.success(`Tagged as ${tag}`);
    } catch (error) {
      toast.error('Failed to tag: ' + (error.response?.data?.message || error.message));
    }
  };

  const toggleSelection = (snapshotId) => {
    setSelectedSnapshots(prev => {
      if (prev.includes(snapshotId)) {
        return prev.filter(id => id !== snapshotId);
      } else if (prev.length < maxSelections) {
        return [...prev, snapshotId];
      }
      return prev;
    });
  };

  const handleCompare = () => {
    if (selectedSnapshots.length === maxSelections) {
      onShowDiff(selectedSnapshots[0], selectedSnapshots[1]);
      setSelectedSnapshots([]);
    }
  };

  const formatDate = (dateString) => {
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diffMs = now - date;
      const diffSecs = Math.floor(diffMs / 1000);
      const diffMins = Math.floor(diffSecs / 60);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffSecs < 60) return 'just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;
      return date.toLocaleDateString();
    } catch {
      return 'Unknown';
    }
  };

  if (!currentFile) {
    return (
      <div className="p-4 text-zinc-400 text-center">
        Select a file to view version history
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col bg-surface-dark">
      {/* Header */}
      <div className="p-4 border-b border-surface-border flex items-center justify-between shrink-0">
        <div>
          <h2 className="text-lg font-semibold text-white">Version History</h2>
          <p className="text-sm text-zinc-400">{currentFile.name}</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-3 py-1.5 bg-accent-cyan text-surface-dark rounded-lg text-sm font-semibold hover:bg-accent-cyan/90 transition-colors"
        >
          + Save Version
        </button>
      </div>

      {/* Branch selector (placeholder for future enhancement) */}
      <div className="px-4 py-2 border-b border-surface-border">
        <span className="text-xs text-zinc-500">Branch: main</span>
      </div>

      {/* Snapshot list */}
      <div className="flex-1 overflow-y-auto p-2 space-y-2">
        {isLoading ? (
          <div className="text-center text-zinc-400 py-8">Loading history...</div>
        ) : snapshots.length === 0 ? (
          <div className="text-center text-zinc-400 py-8">No snapshots yet</div>
        ) : (
          snapshots.map((snapshot, index) => (
            <motion.div
              key={snapshot.snapshotId}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
              className={`p-3 rounded-lg border transition-all cursor-pointer ${
                selectedSnapshots.includes(snapshot.snapshotId)
                  ? 'border-accent-cyan bg-accent-cyan/10'
                  : 'border-surface-border bg-surface-card hover:border-accent-cyan/50'
              }`}
              onClick={() => toggleSelection(snapshot.snapshotId)}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-sm font-medium text-white truncate">
                      {snapshot.message || 'Untitled snapshot'}
                    </span>
                    {snapshot.tag && (
                      <span className="px-2 py-0.5 bg-accent-green/20 text-accent-green text-xs rounded-full">
                        {snapshot.tag}
                      </span>
                    )}
                  </div>
                  <div className="flex items-center gap-3 text-xs text-zinc-400">
                    <span>By User {snapshot.authorId}</span>
                    <span>{formatDate(snapshot.createdAt)}</span>
                    <span className="px-1.5 py-0.5 bg-surface-dark rounded text-zinc-300">
                      {snapshot.branch || 'main'}
                    </span>
                  </div>
                </div>
              </div>
              <div className="flex items-center gap-1 shrink-0">
                {selectedSnapshots.includes(snapshot.snapshotId) ? (
                  <div className="w-5 h-5 rounded-full border-2 border-accent-cyan bg-accent-cyan flex items-center justify-center">
                    <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                ) : (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRestore(snapshot.snapshotId);
                    }}
                    className="px-2 py-1 text-xs bg-surface-darker text-zinc-300 hover:text-white hover:bg-surface-border rounded transition-colors"
                    title="Restore this version"
                  >
                    Restore
                  </button>
                )}
              </div>
            </motion.div>
          ))
        )}
      </div>

      {/* Compare button */}
      {selectedSnapshots.length > 0 && (
        <div className="p-3 border-t border-surface-border bg-surface-darker">
          <button
            onClick={handleCompare}
            disabled={selectedSnapshots.length !== maxSelections}
            className={`w-full py-2 rounded-lg text-sm font-medium transition-colors ${
              selectedSnapshots.length === maxSelections
                ? 'bg-accent-purple text-white hover:bg-accent-purple/90'
                : 'bg-surface-border text-zinc-400 cursor-not-allowed'
            }`}
          >
            Compare {selectedSnapshots.length}/{maxSelections}
          </button>
        </div>
      )}

      {/* Create Snapshot Modal */}
      <AnimatePresence>
        {showCreateModal && (
          <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-surface-card border border-surface-border rounded-xl p-6 w-full max-w-md"
            >
              <h3 className="text-xl font-bold text-white mb-4">Save New Version</h3>
              <textarea
                value={commitMessage}
                onChange={(e) => setCommitMessage(e.target.value)}
                placeholder="Enter a message for this version (optional)"
                className="w-full p-3 bg-surface-dark border border-surface-border rounded-lg text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan resize-none h-32 mb-4"
                autoFocus
              />
              <div className="flex gap-3 justify-end">
                <button
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-zinc-300 hover:text-white transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateSnapshot}
                  className="px-4 py-2 bg-accent-cyan text-surface-dark rounded-lg font-semibold hover:bg-accent-cyan/90 transition-colors"
                >
                  Save Version
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}