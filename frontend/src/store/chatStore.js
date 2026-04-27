import { create } from 'zustand';
import { chatAPI } from '../services/api';

let stompClient = null;

export const useChatStore = create((set, get) => ({
  messages: [],
  currentProjectId: null,
  isConnected: false,
  usersTyping: [],
  socket: null,

  connectWebSocket: (projectId) => {
    const token = localStorage.getItem('token');
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    
    if (!token || !projectId) return;

    const socket = new WebSocket('ws://localhost:8080/ws/chat');
    
    socket.onopen = () => {
      set({ isConnected: true, socket, currentProjectId: projectId });
      
      // Subscribe to project chat
      socket.send(JSON.stringify({
        command: 'subscribe',
        destination: `/topic/chat/${projectId}`
      }));
      
      // Send join message
      socket.send(JSON.stringify({
        command: 'send',
        destination: '/app/chat.join',
        body: JSON.stringify({
          projectId,
          senderId: user.id,
          senderUsername: user.name,
          content: `${user.name} joined`,
          messageType: 'JOIN'
        })
      }));
    };

    socket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (message) {
        set((state) => ({
          messages: [...state.messages, message]
        }));
      }
    };

    socket.onclose = () => {
      set({ isConnected: false, socket: null });
    };

    // Load chat history
    chatAPI.getHistory(projectId)
      .then((response) => {
        set({ messages: response.data || [] });
      })
      .catch(console.error);
  },

  disconnectWebSocket: () => {
    const { socket, currentProjectId } = get();
    if (socket && currentProjectId) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      socket.send(JSON.stringify({
        command: 'send',
        destination: '/app/chat.join',
        body: JSON.stringify({
          projectId: currentProjectId,
          senderId: user.id,
          senderUsername: user.name,
          content: `${user.name} left`,
          messageType: 'LEAVE'
        })
      }));
      socket.close();
    }
    set({ socket: null, isConnected: false, messages: [], currentProjectId: null });
  },

  sendMessage: (content) => {
    const { socket, currentProjectId } = get();
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    
    if (!socket || !currentProjectId) return;

    const message = {
      projectId: currentProjectId,
      senderId: user.id,
      senderUsername: user.name,
      content,
      messageType: 'TEXT'
    };

    socket.send(JSON.stringify({
      command: 'send',
      destination: '/app/chat.sendMessage',
      body: JSON.stringify(message)
    }));
  },

  clearMessages: () => set({ messages: [] }),
}));