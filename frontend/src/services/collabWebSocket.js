import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/**
 * Collaboration WebSocket Service.
 * 
 * Unified topic structure:
 *   /topic/session/{sessionId}/code         - code changes
 *   /topic/session/{sessionId}/cursors      - cursor positions
 *   /topic/session/{sessionId}/participants - join/leave events
 *   /topic/session/{sessionId}/state        - full state sync (on join)
 *   /topic/session/{sessionId}/comments     - comment updates
 */
class CollabWebSocketService {
  constructor() {
    this.client = null;
    this.sessionId = null;
    this.subscriptions = [];
    this.connected = false;
    this.callbacks = {
      onConnected: null,
      onDisconnected: null,
      onCodeChange: null,
      onCursorUpdate: null,
      onParticipantChange: null,
      onCommentUpdate: null,
      onSessionState: null,
      onError: null,
    };
    this.myUserId = null;
    this.throttleTimers = {};
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
  }

  connect(sessionId, options = {}) {
    // Prevent duplicate connections
    if (this.client && this.connected && this.sessionId === sessionId) {
      console.log('Already connected to session:', sessionId);
      return;
    }

    // Disconnect existing connection first
    if (this.client) {
      this.disconnect();
    }

    this.sessionId = sessionId;
    this.myUserId = options.userId || null;
    Object.assign(this.callbacks, options);

    const token = sessionStorage.getItem('token');
    const wsUrl = import.meta.env.VITE_WS_URL || import.meta.env.VITE_API_URL || '';

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${wsUrl}/ws/collab`),
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : '',
      },
      reconnectDelay: Math.min(5000 * Math.pow(1.5, this.reconnectAttempts), 30000), // Exponential backoff
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        this.connected = true;
        this.reconnectAttempts = 0;
        console.log('[CollabWS] Connected to session:', sessionId);
        this.subscribeToSession();
        if (this.callbacks.onConnected) {
          this.callbacks.onConnected();
        }
      },
      onDisconnect: () => {
        this.connected = false;
        this.reconnectAttempts++;
        console.log('[CollabWS] Disconnected. Attempt:', this.reconnectAttempts);
        if (this.callbacks.onDisconnected) {
          this.callbacks.onDisconnected();
        }
      },
      onStompError: (frame) => {
        console.error('[CollabWS] STOMP error:', frame);
        if (this.callbacks.onError) {
          this.callbacks.onError(frame);
        }
      },
    });

    this.client.activate();
  }

  subscribeToSession() {
    if (!this.client || !this.sessionId) return;

    // Unsubscribe existing before resubscribing (handles reconnect)
    this.unsubscribeAll();

    const sid = this.sessionId;

    // 1. Code changes — /topic/session/{id}/code
    this.subscriptions.push(
      this.client.subscribe(`/topic/session/${sid}/code`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          // Filter out own messages
          if (String(payload.userId) === String(this.myUserId)) return;
          if (payload.type === 'CODE_CHANGE' && this.callbacks.onCodeChange) {
            this.callbacks.onCodeChange(payload);
          }
        } catch (e) {
          console.error('[CollabWS] Error parsing code update:', e);
        }
      })
    );

    // 2. Cursor updates — /topic/session/{id}/cursors
    this.subscriptions.push(
      this.client.subscribe(`/topic/session/${sid}/cursors`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          if (String(payload.userId) === String(this.myUserId)) return;
          if (payload.type === 'CURSOR_UPDATE' && this.callbacks.onCursorUpdate) {
            this.callbacks.onCursorUpdate(payload);
          }
        } catch (e) {
          console.error('[CollabWS] Error parsing cursor update:', e);
        }
      })
    );

    // 3. Participant events — /topic/session/{id}/participants
    this.subscriptions.push(
      this.client.subscribe(`/topic/session/${sid}/participants`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          if (this.callbacks.onParticipantChange) {
            this.callbacks.onParticipantChange(payload);
          }
        } catch (e) {
          console.error('[CollabWS] Error parsing participant update:', e);
        }
      })
    );

    // 4. Session state (full sync on join) — /topic/session/{id}/state
    this.subscriptions.push(
      this.client.subscribe(`/topic/session/${sid}/state`, (message) => {
        try {
          const payload = JSON.parse(message.body);
          if (payload.type === 'SESSION_STATE' && this.callbacks.onSessionState) {
            this.callbacks.onSessionState(payload);
          }
        } catch (e) {
          console.error('[CollabWS] Error parsing session state:', e);
        }
      })
    );

    // 5. Comment updates — /topic/session/{id}/comments
    if (this.callbacks.onCommentUpdate) {
      this.subscriptions.push(
        this.client.subscribe(`/topic/session/${sid}/comments`, (message) => {
          try {
            const payload = JSON.parse(message.body);
            this.callbacks.onCommentUpdate(payload);
          } catch (e) {
            console.error('[CollabWS] Error parsing comment update:', e);
          }
        })
      );
    }
  }

  sendCodeChange(content, userId, fileId = null) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: '/app/session.change',
      body: JSON.stringify({
        sessionId: this.sessionId,
        userId: userId || this.myUserId,
        fileId,
        content,
        timestamp: Date.now(),
      }),
    });
  }

  sendCursorUpdate(line, column, color, userId = null) {
    if (!this.client || !this.connected) return;

    // Throttle to 100ms per user
    const userIdKey = userId || this.myUserId;
    if (this.throttleTimers[userIdKey]) return;

    this.throttleTimers[userIdKey] = setTimeout(() => {
      delete this.throttleTimers[userIdKey];
    }, 100);

    this.client.publish({
      destination: '/app/session.cursor',
      body: JSON.stringify({
        sessionId: this.sessionId,
        userId: userIdKey,
        line,
        column,
        color,
        timestamp: Date.now(),
      }),
    });
  }

  joinSession(userId, username = null, color = null) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: '/app/session.join',
      body: JSON.stringify({
        sessionId: this.sessionId,
        userId,
        username: username || `User ${userId}`,
        color,
      }),
    });
  }

  leaveSession(userId) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: '/app/session.leave',
      body: JSON.stringify({
        sessionId: this.sessionId,
        userId,
      }),
    });
  }

  unsubscribeAll() {
    this.subscriptions.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch (e) {
        // ignore
      }
    });
    this.subscriptions = [];
  }

  disconnect() {
    this.unsubscribeAll();

    // Clear throttle timers
    Object.values(this.throttleTimers).forEach(clearTimeout);
    this.throttleTimers = {};

    if (this.client) {
      try {
        this.client.deactivate();
      } catch (e) {
        console.warn('[CollabWS] Error deactivating:', e);
      }
      this.client = null;
    }

    this.connected = false;
    this.sessionId = null;
    this.reconnectAttempts = 0;
  }

  isConnected() {
    return this.connected;
  }
}

export const collabWebSocket = new CollabWebSocketService();
export default collabWebSocket;