import { useState } from 'react';
import { motion } from 'framer-motion';

export default function ChatInput({ onSend }) {
  const [message, setMessage] = useState('');

  const handleSend = () => {
    if (!message.trim()) return;
    onSend(message.trim());
    setMessage('');
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="p-3 border-t border-surface-border">
      <div className="flex items-center gap-2">
        <textarea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Type a message..."
          className="flex-1 bg-surface-darker border border-surface-border rounded-xl px-3 py-2 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan resize-none h-10 max-h-24"
          rows={1}
        />
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={handleSend}
          disabled={!message.trim()}
          className="p-2 bg-accent-cyan text-surface-dark rounded-xl disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </motion.button>
      </div>
      <p className="text-xs text-zinc-500 mt-2">Press Enter to send, Shift+Enter for new line</p>
    </div>
  );
}