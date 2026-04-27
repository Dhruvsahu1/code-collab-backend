import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useVersionStore } from '../store';

export default function DiffViewer({ snapshot1Id, snapshot2Id, onClose }) {
  const { currentDiff, compareSnapshots } = useVersionStore();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDiff = async () => {
      try {
        setLoading(true);
        await compareSnapshots(snapshot1Id, snapshot2Id);
      } catch (error) {
        toast.error('Failed to load diff: ' + (error.response?.data?.message || error.message));
      } finally {
        setLoading(false);
      }
    };
    loadDiff();
  }, [snapshot1Id, snapshot2Id]);

  const getLineClass = (type) => {
    switch (type) {
      case 'addition':
        return 'bg-accent-green/20 border-l-2 border-accent-green';
      case 'deletion':
        return 'bg-accent-red/20 border-l-2 border-accent-red';
      default:
        return 'border-l-2 border-transparent';
    }
  };

  const getLinePrefix = (type) => {
    switch (type) {
      case 'addition':
        return '+';
      case 'deletion':
        return '-';
      default:
        return ' ';
    }
  };

  if (loading) {
    return (
      <div className="h-full flex items-center justify-center text-zinc-400">
        Loading diff...
      </div>
    );
  }

  if (!currentDiff || currentDiff.lines.length === 0) {
    return (
      <div className="h-full flex flex-col">
        <div className="p-4 border-b border-surface-border flex items-center justify-between">
          <h2 className="text-lg font-semibold text-white">Diff Viewer</h2>
          <button
            onClick={onClose}
            className="p-1 text-zinc-400 hover:text-white transition-colors"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div className="flex-1 flex items-center justify-center text-zinc-400">
          No differences found
        </div>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col bg-surface-dark">
      {/* Header */}
      <div className="p-4 border-b border-surface-border flex items-center justify-between shrink-0">
        <div>
          <h2 className="text-lg font-semibold text-white">Diff Viewer</h2>
          <div className="flex items-center gap-3 text-xs text-zinc-400 mt-1">
            <span>
              {currentDiff.additions} additions
            </span>
            <span>
              {currentDiff.deletions} deletions
            </span>
          </div>
        </div>
        <button
          onClick={onClose}
          className="p-2 text-zinc-400 hover:text-white hover:bg-surface-card rounded-lg transition-colors"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      {/* Stats bar */}
      <div className="px-4 py-2 bg-surface-darker border-b border-surface-border flex gap-4 text-xs">
        <div className="flex items-center gap-2">
          <span className="text-zinc-500">Snapshot A:</span>
          <span className="text-zinc-300">
            #{currentDiff.snapshot1Id}
          </span>
          <span className="text-zinc-500">({currentDiff.snapshot1Hash?.substring(0, 8)})</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-zinc-500">Snapshot B:</span>
          <span className="text-zinc-300">
            #{currentDiff.snapshot2Id}
          </span>
          <span className="text-zinc-500">({currentDiff.snapshot2Hash?.substring(0, 8)})</span>
        </div>
      </div>

      {/* Diff lines */}
      <div className="flex-1 overflow-y-auto p-4 font-mono text-sm">
        <div className="space-y-0.5">
          {currentDiff.lines.map((line, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.002 }}
              className={`flex ${getLineClass(line.type)} px-2 py-0.5 rounded`}
            >
              <span className="w-10 text-right pr-3 text-zinc-500 select-none">
                {line.lineNumber}
              </span>
              <span
                className={`flex-1 ${
                  line.type === 'addition'
                    ? 'text-accent-green'
                    : line.type === 'deletion'
                    ? 'text-accent-red'
                    : 'text-zinc-300'
                }`}
              >
                <span className="mr-2 select-none opacity-50">
                  {getLinePrefix(line.type)}
                </span>
                {line.content || ' '}
              </span>
            </motion.div>
          ))}
        </div>
      </div>
    </div>
  );
}