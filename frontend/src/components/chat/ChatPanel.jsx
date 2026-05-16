import { useEffect, useRef, useState } from 'react';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import { useChatStore } from '../../store/chatStore';
import { useAuthStore } from '../../store';

export default function ChatPanel({ projectId }) {
  const messagesEndRef = useRef(null);
  const user = useAuthStore((state) => state.user);
  const { 
    messages, 
    connectWebSocket, 
    disconnectWebSocket, 
    sendMessage,
    isConnected,
    usersTyping
  } = useChatStore();
  const [typingTimeout, setTypingTimeout] = useState(null);

  useEffect(() => {
    if (projectId) {
      connectWebSocket(projectId);
    }
    return () => {
      disconnectWebSocket();
    };
  }, [projectId, connectWebSocket, disconnectWebSocket]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Handle typing indicators
  const handleTyping = () => {
    // Clear existing timeout
    if (typingTimeout) {
      clearTimeout(typingTimeout);
    }
    
    // Send typing start
    const { sendTypingStart } = useChatStore.getState();
    sendTypingStart();
    
    // Set timeout to send typing stop after 1 second of inactivity
    const newTimeout = setTimeout(() => {
      const { sendTypingStop } = useChatStore.getState();
      sendTypingStop();
    }, 1000);
    
    setTypingTimeout(newTimeout);
  };

  // Clean up timeout on unmount
  useEffect(() => {
    return () => {
      if (typingTimeout) {
        clearTimeout(typingTimeout);
      }
    };
  }, [typingTimeout]);

  return (
    <div className="h-full flex flex-col bg-surface-darker">
      <div className="p-4 border-b border-surface-border flex items-center justify-between">
        <h3 className="text-sm font-semibold text-white">Chat</h3>
        <div className="flex items-center gap-2">
          <div className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-400' : 'bg-red-400'}`} />
          <span className="text-xs text-zinc-500">{isConnected ? 'Connected' : 'Disconnected'}</span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-4">
        {messages.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-zinc-500 text-sm">No messages yet</p>
            <p className="text-zinc-600 text-xs mt-1">Start the conversation!</p>
          </div>
        ) : (
          <>
            {messages.map((msg, index) => (
              <ChatMessage
                key={index}
                message={msg}
                isOwn={msg.senderId === user?.id}
              />
            ))}
            {/* Typing indicator */}
            {usersTyping.length > 0 && (
              <div className="flex items-center gap-2 mt-2 text-zinc-400">
                <div className="w-2 h-2 rounded-full bg-zinc-400 animate-pulse" />
                <span>
                  {usersTyping.map((userId, idx) => {
                    // In a real app, we would get the username from userId
                    // For now, we'll just show "Someone" or the userId
                    return idx === 0 ? `${userId === user?.id ? 'You' : 'Someone'}` : '';
                  }).join(', ')}
                  {usersTyping.length > 1 && ` and ${usersTyping.length - 1} others`} 
                  {usersTyping.length === 1 ? ' is typing...' : ' are typing...'}
                </span>
              </div>
            )}
          </>
        )}
        <div ref={messagesEndRef} />
      </div>

      <ChatInput 
        onSend={sendMessage} 
        onTyping={handleTyping} 
      />
    </div>
  );
}