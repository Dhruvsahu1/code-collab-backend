import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { useExecutionStore } from '../store';

export default function OutputPanel() {
  const { output, isRunning, history } = useExecutionStore();

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 border-b border-surface-border flex items-center justify-between">
        <h3 className="text-sm font-semibold text-white">Output</h3>
        {isRunning && (
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ repeat: Infinity, duration: 1, ease: 'linear' }}
            className="w-4 h-4 border-2 border-accent-cyan border-t-transparent rounded-full"
          />
        )}
      </div>
      
      <div className="flex-1 overflow-y-auto p-4 font-mono text-sm">
        {output ? (
          <pre className="text-zinc-300 whitespace-pre-wrap">{output}</pre>
        ) : (
          <p className="text-zinc-500">Click "Run" to execute your code</p>
        )}
      </div>

      {/* Execution History */}
      {history.length > 0 && (
        <div className="border-t border-surface-border">
          <div className="p-3">
            <h4 className="text-xs font-semibold text-zinc-500 mb-2">History</h4>
            <div className="space-y-1">
              {history.slice(0, 5).map((job, index) => (
                <div
                  key={index}
                  className="flex items-center justify-between text-xs text-zinc-400 px-2 py-1 rounded hover:bg-surface-hover"
                >
                  <span>{job.language}</span>
                  <span className={job.status === 'completed' ? 'text-green-400' : 'text-yellow-400'}>
                    {job.status}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}