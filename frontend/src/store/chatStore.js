import { create } from 'zustand';
import { chatAPI } from '../services/api';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;

export const useChatStore = create((set, get) => ({
  messages: [],
  currentProjectId: null,
  isConnected: false,
  usersTyping: [],

  connectWebSocket: (projectId) => {
    const token = sessionStorage.getItem('token');
    const user = JSON.parse(sessionStorage.getItem('auth-storage'))?.state?.user || {};

    if (!projectId) return;

    // Prevent duplicate connections
    if (stompClient && get().isConnected && get().currentProjectId === projectId) {
      console.log('[ChatWS] Already connected to project:', projectId);
      return;
    }

    // Disconnect existing first
    if (stompClient) {
      try { stompClient.deactivate(); } catch (e) { /* ignore */ }
      stompClient = null;
    }

    const wsUrl = import.meta.env.VITE_WS_URL || import.meta.env.VITE_API_URL || '';

    stompClient = new Client({
      webSocketFactory: () => new SockJS(`${wsUrl}/ws/chat`),
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : '',
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        console.log('[ChatWS] Connected to project:', projectId);
        set({ isConnected: true, currentProjectId: projectId });

        // Subscribe to project chat topic
        stompClient.subscribe(`/topic/chat/${projectId}`, (message) => {
          try {
            const parsedMessage = JSON.parse(message.body);
            const msgType = parsedMessage.type || parsedMessage.messageType;

            // Handle different message types
            switch (msgType) {
              case 'TYPING_START': {
                const senderId = parsedMessage.senderId;
                if (senderId !== user.id) {
                  set((state) => ({
                    usersTyping: state.usersTyping.includes(senderId)
                      ? state.usersTyping
                      : [...state.usersTyping, senderId]
                  }));
                }
                break;
              }
              case 'TYPING_STOP': {
                const senderId = parsedMessage.senderId;
                set((state) => ({
                  usersTyping: state.usersTyping.filter(id => id !== senderId)
                }));
                break;
              }
              case 'MESSAGE':
              case 'TEXT':
              case 'JOIN':
              case 'LEAVE': {
                set((state) => ({
                  messages: [...state.messages, parsedMessage],
                  // Remove from typing if they sent a message
                  usersTyping: state.usersTyping.filter(id => id !== parsedMessage.senderId)
                }));
                break;
              }
              default: {
                // Unknown type, add as message
                set((state) => ({
                  messages: [...state.messages, parsedMessage]
                }));
              }
            }
          } catch (e) {
            console.error('[ChatWS] Error parsing message:', e);
          }
        });

        // Send join message
        stompClient.publish({
          destination: '/app/chat.join',
          body: JSON.stringify({
            projectId,
            senderId: user.id,
            senderUsername: user.name,
            content: `${user.name} joined the project`,
            messageType: 'JOIN'
          })
        });

        // Load chat history
        chatAPI.getHistory(projectId)
          .then((response) => {
            set({ messages: response.data || [] });
          })
          .catch((err) => {
            console.warn('[ChatWS] Failed to load history:', err);
          });
      },
      onDisconnect: () => {
        console.log('[ChatWS] Disconnected');
        set({ isConnected: false });
      },
      onStompError: (frame) => {
        console.error('[ChatWS] STOMP error:', frame);
        set({ isConnected: false });
      },
    });

    stompClient.activate();
  },

  disconnectWebSocket: () => {
    const { currentProjectId } = get();
    const user = JSON.parse(sessionStorage.getItem('auth-storage'))?.state?.user || {};
    
    if (stompClient && currentProjectId) {
      try {
        stompClient.publish({
          destination: '/app/chat.leave',
          body: JSON.stringify({
            projectId: currentProjectId,
            senderId: user.id,
            senderUsername: user.name,
            content: `${user.name} left the project`,
            messageType: 'LEAVE'
          })
        });
      } catch (e) {
        console.warn('[ChatWS] Error sending leave:', e);
      }

      try {
        stompClient.deactivate();
      } catch (e) {
        console.warn('[ChatWS] Error deactivating:', e);
      }
    }

    stompClient = null;
    set({ 
      isConnected: false, 
      messages: [], 
      currentProjectId: null,
      usersTyping: []
    });
  },

  sendMessage: (content) => {
    const { currentProjectId } = get();
    const user = JSON.parse(sessionStorage.getItem('auth-storage'))?.state?.user || {};
    if (!stompClient || !currentProjectId) return;

    stompClient.publish({
      destination: '/app/chat.sendMessage',
      body: JSON.stringify({
        projectId: currentProjectId,
        senderId: user.id,
        senderUsername: user.name,
        content,
        messageType: 'TEXT'
      })
    });
  },

  sendTypingStart: () => {
    const { currentProjectId } = get();
    const user = JSON.parse(sessionStorage.getItem('auth-storage'))?.state?.user || {};
    if (!stompClient || !currentProjectId) return;

    stompClient.publish({
      destination: '/app/chat.typingStart',
      body: JSON.stringify({
        projectId: currentProjectId,
        senderId: user.id,
        senderUsername: user.name
      })
    });
  },

  sendTypingStop: () => {
    const { currentProjectId } = get();
    const user = JSON.parse(sessionStorage.getItem('auth-storage'))?.state?.user || {};
    if (!stompClient || !currentProjectId) return;

    stompClient.publish({
      destination: '/app/chat.typingStop',
      body: JSON.stringify({
        projectId: currentProjectId,
        senderId: user.id,
        senderUsername: user.name
      })
    });
  },

  setUsersTyping: (usersTyping) => set({ usersTyping }),
  clearMessages: () => set({ messages: [] }),
}));