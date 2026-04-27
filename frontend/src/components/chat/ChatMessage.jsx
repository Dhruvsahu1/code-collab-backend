import { motion } from 'framer-motion';

export default function ChatMessage({ message, isOwn }) {
  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  if (message.messageType === 'JOIN' || message.messageType === 'LEAVE') {
    return (
      <motion.div
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-center py-2"
      >
        <span className="text-xs text-zinc-500 bg-surface-hover px-3 py-1 rounded-full">
          {message.senderUsername} {message.messageType === 'JOIN' ? 'joined' : 'left'} the chat
        </span>
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={`flex flex-col mb-3 ${isOwn ? 'items-end' : 'items-start'}`}
    >
      <div className={`flex items-end gap-2 ${isOwn ? 'flex-row-reverse' : ''}`}>
        {!isOwn && (
          <div className="w-7 h-7 rounded-full bg-gradient-to-br from-accent-violet to-accent-rose flex items-center justify-center text-xs text-white font-semibold flex-shrink-0">
            {message.senderUsername?.charAt(0).toUpperCase()}
          </div>
        )}
        <div>
          {!isOwn && (
            <span className="text-xs text-zinc-400 mb-1 block">{message.senderUsername}</span>
          )}
          <div
            className={`max-w-[75%] px-3 py-2 rounded-xl text-sm ${
              isOwn
                ? 'bg-gradient-to-r from-accent-cyan to-accent-magenta text-surface-dark rounded-br-sm'
                : 'bg-surface-hover text-zinc-200 rounded-bl-sm'
            }`}
          >
            {message.content}
          </div>
          <span className={`text-xs text-zinc-500 mt-1 block ${isOwn ? 'text-right' : ''}`}>
            {formatTime(message.timestamp)}
          </span>
        </div>
      </div>
    </motion.div>
  );
}