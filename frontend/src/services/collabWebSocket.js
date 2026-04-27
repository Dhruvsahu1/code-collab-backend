import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class CollabWebSocketService {
  constructor() {
    this.client = null;
    this.sessionId = null;
    this.subscriptions = [];
    this.connected = false;
    this.connectCallback = null;
    this.codeUpdateCallback = null;
    this.cursorUpdateCallback = null;
    this.participantCallback = null;
    this.commentCallback = null;
  }

  connect(sessionId, onConnected, onCodeUpdate, onCursorUpdate, onParticipantChange, onCommentUpdate) {
    this.sessionId = sessionId;
    this.connectCallback = onConnected;
    this.codeUpdateCallback = onCodeUpdate;
    this.cursorUpdateCallback = onCursorUpdate;
    this.participantCallback = onParticipantChange;
    this.commentCallback = onCommentUpdate;

    const wsUrl = 'http://localhost:8084';
    
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${wsUrl}/ws-collab`),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connected = true;
        console.log('Connected to collaboration session');
        this.subscribeToSession();
        if (this.connectCallback) {
          this.connectCallback();
        }
      },
      onDisconnect: () => {
        this.connected = false;
        console.log('Disconnected from collaboration session');
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });

    this.client.activate();
  }

  subscribeToSession() {
    if (!this.client || !this.sessionId) return;

    // Main session topic for code changes and session events
    const mainSubscription = this.client.subscribe(
      `/topic/session/${this.sessionId}`,
      (message) => {
        try {
          const payload = JSON.parse(message.body);
          console.log('Main topic message:', payload);
          if (payload.type === 'CODE_CHANGE') {
            if (this.codeUpdateCallback) {
              this.codeUpdateCallback(payload.code, payload.userId);
            }
          } else if (payload.type === 'SESSION_ENDED') {
            if (this.participantCallback) {
              this.participantCallback(payload);
            }
          }
        } catch (e) {
          console.error('Error parsing main topic message:', e);
        }
      }
    );
    this.subscriptions.push(mainSubscription);

    // Code changes topic
    const codeSubscription = this.client.subscribe(
      `/topic/session/${this.sessionId}/changes`,
      (message) => {
        try {
          const payload = JSON.parse(message.body);
          console.log('Code change:', payload);
          if (payload.type === 'CODE_UPDATE' && this.codeUpdateCallback) {
            this.codeUpdateCallback(payload.code, payload.userId);
          }
        } catch (e) {
          console.error('Error parsing code update:', e);
        }
      }
    );
    this.subscriptions.push(codeSubscription);

    // Cursor updates topic
    const cursorSubscription = this.client.subscribe(
      `/topic/session/${this.sessionId}/cursor`,
      (message) => {
        try {
          const payload = JSON.parse(message.body);
          console.log('Cursor update:', payload);
          if (payload.type === 'CURSOR_UPDATE' && this.cursorUpdateCallback) {
            this.cursorUpdateCallback(payload);
          }
        } catch (e) {
          console.error('Error parsing cursor update:', e);
        }
      }
    );
    this.subscriptions.push(cursorSubscription);

    // Participants topic
    const participantSubscription = this.client.subscribe(
      `/topic/session/${this.sessionId}/participants`,
      (message) => {
        try {
          const payload = JSON.parse(message.body);
          console.log('Participant update:', payload);
          if (this.participantCallback) {
            this.participantCallback(payload);
          }
        } catch (e) {
          console.error('Error parsing participant update:', e);
        }
      }
    );
    this.subscriptions.push(participantSubscription);

    // Comments topic for real-time comment updates
    if (this.commentCallback) {
      const commentSubscription = this.client.subscribe(
        `/topic/session/${this.sessionId}/comments`,
        (message) => {
          try {
            const payload = JSON.parse(message.body);
            console.log('Comment update:', payload);
            this.commentCallback(payload);
          } catch (e) {
            console.error('Error parsing comment update:', e);
          }
        }
      );
      this.subscriptions.push(commentSubscription);
    }
  }

  sendCodeUpdate(code, userId) {
    if (!this.client || !this.connected) {
      console.warn('Cannot send code update: WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: `/app/code.change`,
      body: JSON.stringify({
        sessionId: this.sessionId,
        code,
        userId,
        timestamp: Date.now(),
      }),
    });
  }

  sendCodeChange(code, userId) {
    if (!this.client || !this.connected) {
      console.warn('Cannot send code change: WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: '/app/code.change',
      body: JSON.stringify({
        sessionId: this.sessionId,
        code,
        userId,
      }),
    });
  }

  endSession() {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: '/app/session.end',
      body: JSON.stringify({
        sessionId: this.sessionId,
      }),
    });
  }

  sendCursorUpdate(userId, cursorLine, cursorCol, color) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: `/app/session/${this.sessionId}/cursor`,
      body: JSON.stringify({
        type: 'CURSOR_UPDATE',
        userId,
        cursorLine,
        cursorCol,
        color,
        timestamp: Date.now(),
      }),
    });
  }

  joinSession(userId, role = 'EDITOR', sessionPassword = null) {
    if (!this.client || !this.connected) return;

    const payload = {
      type: 'JOIN_SESSION',
      userId,
      role,
    };
    
    if (sessionPassword) {
      payload.sessionPassword = sessionPassword;
    }

    this.client.publish({
      destination: `/app/session/${this.sessionId}/join`,
      body: JSON.stringify(payload),
    });
  }

  leaveSession(userId) {
    if (!this.client || !this.connected) return;

    this.client.publish({
      destination: `/app/session/${this.sessionId}/leave`,
      body: JSON.stringify({
        type: 'LEAVE_SESSION',
        userId,
      }),
    });
  }

  disconnect() {
    this.subscriptions.forEach((sub) => {
      try {
        sub.unsubscribe();
      } catch (e) {
        console.warn('Error unsubscribing:', e);
      }
    });
    this.subscriptions = [];
    
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    
    this.connected = false;
    this.sessionId = null;
  }

  isConnected() {
    return this.connected;
  }
}

export const collabWebSocket = new CollabWebSocketService();
export default collabWebSocket;