import { useState, useRef, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import CodeEditor from '@monaco-editor/react';
import toast from 'react-hot-toast';
import { useProjectStore, useEditorStore, useExecutionStore, useAuthStore, useVersionStore, useCollabStore } from '../store';
import { projectAPI, fileAPI, executionAPI, collabAPI, versionAPI } from '../services/api';
import FileExplorer from '../components/FileExplorer';
import OutputPanel from '../components/OutputPanel';
import CollaborationPanel from '../components/CollaborationPanel';
import CommentsPanel from '../components/CommentsPanel';
import ChatPanel from '../components/chat/ChatPanel';
import VersionHistory from '../components/VersionHistory';
import DiffViewer from '../components/DiffViewer';
import collabWebSocket from '../services/collabWebSocket';

function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

function ResizeHandle({ direction = 'horizontal', onResize }) {
  const startPos = useRef(0);
  const startSize = useRef(0);
  
  const handleMouseDown = (e) => {
    e.preventDefault();
    startPos.current = direction === 'horizontal' ? e.clientX : e.clientY;
    startSize.current = onResize?.() || 200;
    
    const handleMouseMove = (e) => {
      const currentPos = direction === 'horizontal' ? e.clientX : e.clientY;
      const delta = currentPos - startPos.current;
      const newSize = Math.max(150, Math.min(startSize.current + delta, 500));
      onResize?.(newSize);
    };
    
    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
    
    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  };
  
  return (
    <div
      className={`resize-handle ${direction === 'horizontal' ? 'w-1 cursor-col-resize hover:bg-accent-cyan/50' : 'h-1 cursor-row-resize'} bg-transparent hover:bg-accent-cyan/30 transition-colors`}
      onMouseDown={handleMouseDown}
    />
  );
}

function ResizablePanel({ children, defaultWidth = 250, minWidth = 150, maxWidth = 500, onResize }) {
  const [width, setWidth] = useState(defaultWidth);
  const handleResize = useCallback((newWidth) => {
    const clamped = Math.max(minWidth, Math.min(newWidth, maxWidth));
    setWidth(clamped);
    onResize?.(clamped);
  }, [minWidth, maxWidth, onResize]);
  
  return (
    <div className="flex" style={{ width }}>
      <div className="flex-1 overflow-hidden">
        {children}
      </div>
      <ResizeHandle onResize={handleResize} />
    </div>
  );
}

export default function EditorPage({ collabMode = false }) {
  const { projectId, sessionId } = useParams();
  const navigate = useNavigate();
  const collabSessionId = collabMode ? sessionId : null;
  const [showFiles, setShowFiles] = useState(!collabMode);
  const [showOutput, setShowOutput] = useState(!collabMode);
  const [showCollab, setShowCollab] = useState(collabMode || true);
  const [showComments, setShowComments] = useState(false);
  const [showChat, setShowChat] = useState(false);
  const [showVersionHistory, setShowVersionHistory] = useState(false);
  const [showDiffViewer, setShowDiffViewer] = useState(false);
  const [diffSnapshots, setDiffSnapshots] = useState([null, null]);
  const [monacoEditor, setMonacoEditor] = useState(null);
  
  // Panel widths for resizing
  const [filePanelWidth, setFilePanelWidth] = useState(220);
  const [outputPanelWidth, setOutputPanelWidth] = useState(280);
  const [chatPanelWidth, setChatPanelWidth] = useState(280);
  const [commentPanelWidth, setCommentPanelWidth] = useState(280);
  const [versionPanelWidth, setVersionPanelWidth] = useState(300);
  
  // CRITICAL FIX: Use ref instead of state for remote update flag to avoid race conditions
  const isRemoteUpdateRef = useRef(false);
  const codeChangeTimerRef = useRef(null);
  
  const user = useAuthStore((state) => state.user);
  const currentUserId = user?.id;

  const { currentProject, setCurrentProject, setCurrentFile } = useProjectStore();
  const { code, setCode, language, setLanguage, isDirty, markClean, setCursorPosition } = useEditorStore();
  const { setOutput, setRunning, setCurrentJob } = useExecutionStore();
  const { cursors, addParticipant, removeParticipant, updateCursor, setSessionId, setConnected, participants } = useCollabStore();

  const debouncedCode = useDebounce(code, 1500);
  const saveTimeoutRef = useRef(null);
  const cursorDecorationsRef = useRef([]);

  const saveContent = useCallback(async (contentToSave) => {
    const currentFile = useProjectStore.getState().currentFile;
    if (!currentFile || !isDirty) return;
    
    try {
      await fileAPI.updateContent(currentFile.fileId, { content: contentToSave });
      markClean();
    } catch (error) {
      console.error('Auto-save failed:', error);
    }
  }, [isDirty, markClean]);

  useEffect(() => {
    if (isDirty && debouncedCode !== code) {
      saveContent(debouncedCode);
    }
  }, [debouncedCode, code, isDirty, saveContent]);

  useEffect(() => {
    const loadProject = async () => {
      if (!projectId && collabSessionId) return;
      try {
        const response = await projectAPI.getById(projectId);
        setCurrentProject(response.data);
      } catch (error) {
        console.error('Failed to load project:', error);
        toast.error('Failed to load project: ' + (error.response?.data?.message || error.message));
      }
    };
    loadProject();
  }, [projectId, setCurrentProject]);

  const updateCursorDecorations = useCallback(() => {
    if (!monacoEditor || !window.monaco) return;

    const newDecorations = [];
    
    Object.entries(cursors).forEach(([userId, cursor]) => {
      if (String(userId) === String(currentUserId)) return;
      
      if (cursor && typeof cursor.line === 'number') {
        const lineNumber = cursor.line;
        const column = cursor.column || 1;
        const color = cursor.color || '#FF5733';
        
        newDecorations.push({
          range: new window.monaco.Range(lineNumber, column, lineNumber, column + 1),
          options: {
            className: `remote-cursor`,
            inlineClassName: `remote-cursor-inline`,
            afterContentClassName: `remote-cursor-widget`,
            stickiness: window.monaco.editor.TrackedRangeStickiness.AlwaysGrows,
            zIndex: 100
          }
        });
      }
    });

    cursorDecorationsRef.current = monacoEditor.deltaDecorations(
      cursorDecorationsRef.current,
      newDecorations
    );
  }, [monacoEditor, cursors, currentUserId]);

  useEffect(() => {
    updateCursorDecorations();
  }, [cursors, updateCursorDecorations]);

  const setupCursorTracking = useCallback((editor) => {
    let lastUpdate = 0;
    
    editor.onDidChangeCursorPosition((e) => {
      setCursorPosition({
        line: e.position.lineNumber,
        column: e.position.column,
      });

      // Only send cursor updates in collab mode, when connected, and not during remote updates
      if (collabSessionId && collabWebSocket.isConnected() && !isRemoteUpdateRef.current) {
        const now = Date.now();
        if (now - lastUpdate > 100) {
          lastUpdate = now;
          collabWebSocket.sendCursorUpdate(
            e.position.lineNumber,
            e.position.column,
            useCollabStore.getState().myColor || '#22d3ee',
            currentUserId
          );
        }
      }
    });
  }, [collabSessionId, currentUserId, setCursorPosition]);

  const handleEditorMount = useCallback((editor, monaco) => {
    setMonacoEditor(editor);
    setupCursorTracking(editor);
    
    // Add cursor styles
    const style = document.createElement('style');
    style.textContent = `
      .remote-cursor {
        border-left: 2px solid #FF5733;
        margin-left: -1px;
      }
      .remote-cursor-inline {
        background-color: rgba(255, 87, 51, 0.2);
      }
    `;
    document.head.appendChild(style);
  }, [setupCursorTracking]);

  // === SINGLE WebSocket connection point for collaboration ===
  // CollaborationPanel no longer manages its own connection
  useEffect(() => {
    if (!collabSessionId) return;

    setSessionId(collabSessionId);
    
    // Load initial session state via REST
    collabAPI.getSession(collabSessionId)
      .then(async response => {
        if (response.data?.code !== undefined) {
          isRemoteUpdateRef.current = true;
          setCode(response.data.code);
          if (response.data.language) {
            setLanguage(response.data.language);
          }
          // Use requestAnimationFrame to ensure the flag is cleared after React processes the state
          requestAnimationFrame(() => {
            isRemoteUpdateRef.current = false;
          });
          
          // Hydrate workspace context for guest users
          if (!projectId && response.data.projectId) {
            // Bypass projectAPI.getById because private projects reject non-collaborator guests.
            // The CollabSessionResponse already contains the essential project metadata!
            setCurrentProject({
              projectId: response.data.projectId,
              name: response.data.projectName || 'Collaboration Project',
              ownerId: response.data.ownerId
            });
          }
          
          if (!useProjectStore.getState().currentFile && response.data.fileId) {
            try {
              const fileRes = await fileAPI.getById(response.data.fileId);
              setCurrentFile({
                ...fileRes.data,
                content: response.data.code
              });
            } catch (err) {
              console.error('Failed to hydrate file:', err);
            }
          }
        }
      })
      .catch(err => console.error('Failed to load session:', err));

    // Connect WebSocket with all callbacks
    collabWebSocket.connect(collabSessionId, {
      userId: currentUserId,
      onConnected: () => {
        setConnected(true);
        toast.success('Connected to collaboration session!');
        collabWebSocket.joinSession(currentUserId, user?.name);
      },
      onDisconnected: () => {
        setConnected(false);
      },
      onCodeChange: (payload) => {
        // Own messages are already filtered in collabWebSocket.js
        isRemoteUpdateRef.current = true;
        setCode(payload.content || '');
        requestAnimationFrame(() => {
          isRemoteUpdateRef.current = false;
        });
      },
      onCursorUpdate: (payload) => {
        // Own messages are already filtered in collabWebSocket.js
        updateCursor(payload.userId, {
          line: payload.line,
          column: payload.column,
          color: payload.color
        });
      },
      onParticipantChange: (payload) => {
        if (payload.type === 'USER_JOINED') {
          addParticipant({
            userId: payload.userId,
            username: payload.username,
            color: payload.color,
            role: payload.role
          });
        } else if (payload.type === 'USER_LEFT' || payload.type === 'PARTICIPANT_LEFT') {
          removeParticipant(payload.userId);
        } else if (payload.type === 'SESSION_ENDED') {
          toast.error('Session has been ended by the host');
          navigate('/dashboard');
        }
      },
      onSessionState: (payload) => {
        // Full state sync when joining
        if (payload.code !== undefined) {
          isRemoteUpdateRef.current = true;
          setCode(payload.code || '');
          if (payload.language) {
            setLanguage(payload.language);
          }
          requestAnimationFrame(() => {
            isRemoteUpdateRef.current = false;
          });
        }
        
        // Sync active participants from server authoritative list
        if (payload.participants && Array.isArray(payload.participants)) {
          const store = useCollabStore.getState();
          payload.participants.forEach(p => {
            store.addParticipant({
              userId: p.userId,
              username: p.username,
              color: p.color,
              role: p.role
            });
          });
        }
      },
      onCommentUpdate: (payload) => {
        console.log('Comment update:', payload);
      }
    });

    // Add self as participant locally
    useCollabStore.getState().addParticipant({
      userId: currentUserId,
      username: user?.name,
      color: useCollabStore.getState().myColor || '#22d3ee'
    });
    
    return () => {
      if (collabSessionId) {
        collabWebSocket.leaveSession(currentUserId);
        collabWebSocket.disconnect();
        setConnected(false);
        setSessionId(null);
      }
    };
  }, [collabSessionId, currentUserId, user?.name]);

  const handleFileSelect = async (file) => {
    if (file.isFolder) return;
    
    try {
      const response = await fileAPI.getContent(file.fileId);
      setCode(response.data.content || '');
      setCurrentFile(file);
      const fileLanguage = getLanguageFromFileName(file.name);
      setLanguage(fileLanguage);
      markClean();
    } catch (error) {
      console.error('Failed to load file content:', error);
      toast.error('Failed to load file: ' + (error.response?.data?.error || error.message));
    }
  };

  const handleCodeChange = (value) => {
    // Don't process changes triggered by remote updates
    if (isRemoteUpdateRef.current) return;

    setCode(value || '');
    
    // Debounced WebSocket send (300ms) to avoid flooding
    if (collabSessionId && collabWebSocket.isConnected()) {
      if (codeChangeTimerRef.current) {
        clearTimeout(codeChangeTimerRef.current);
      }
      codeChangeTimerRef.current = setTimeout(() => {
        collabWebSocket.sendCodeChange(value || '', currentUserId);
        codeChangeTimerRef.current = null;
      }, 300);
    }
  };

  const handleRunCode = async () => {
    const file = useProjectStore.getState().currentFile;
    if (!file) {
      toast.error('Select a file first');
      return;
    }

    setRunning(true);
    setOutput('Running...\n');

    try {
      const response = await executionAPI.run({
        userId: useProjectStore.getState().currentProject?.ownerId,
        fileId: file.fileId,
        language: language,
        code: code,
      });
      
      const job = response.data;
      setCurrentJob(job);
      
      const pollForResult = async () => {
        let attempts = 0;
        const maxAttempts = 20;
        
        while (attempts < maxAttempts) {
          const statusResponse = await executionAPI.getJob(job.jobId);
          const updatedJob = statusResponse.data;
          
          if (updatedJob.status === 'COMPLETED' || updatedJob.status === 'FAILED') {
            const outputText = updatedJob.output || updatedJob.stdout || updatedJob.error || '(No output)';
            setOutput(`Job ID: ${updatedJob.jobId}\nStatus: ${updatedJob.status}\nLanguage: ${updatedJob.language}\n\nOutput:\n${outputText}`);
            setRunning(false);
            return;
          }
          
          setOutput(`Running...\nJob ID: ${updatedJob.jobId}\nStatus: ${updatedJob.status}`);
          await new Promise(resolve => setTimeout(resolve, 500));
          attempts++;
        }
        
        setOutput(`Job ID: ${job.jobId}\nStatus: TIMEOUT\n\nOutput:\n(Time limit exceeded)`);
        setRunning(false);
      };
      
      pollForResult();
    } catch (error) {
      setOutput(`Error: ${error.message}`);
      setRunning(false);
    }
  };

  const handleSaveSnapshot = async () => {
    const file = useProjectStore.getState().currentFile;
    const user = useAuthStore.getState().user;
    if (!file || !user) {
      toast.error('Please select a file and ensure you are logged in');
      return;
    }

    try {
      await versionAPI.createSnapshot({
        projectId: file.projectId,
        fileId: file.fileId,
        authorId: user.id,
        message: `Saved version from editor`,
        content: code,
        branch: 'main',
      });
      toast.success('Version saved successfully');
      if (showVersionHistory) {
        useVersionStore.getState().fetchFileHistory(file.fileId);
      }
    } catch (error) {
      toast.error('Failed to save version: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleShowDiff = (snapshot1Id, snapshot2Id) => {
    setDiffSnapshots([snapshot1Id, snapshot2Id]);
    setShowDiffViewer(true);
  };

  const handleCloseDiff = () => {
    setShowDiffViewer(false);
    setDiffSnapshots([null, null]);
  };

  const getLanguage = (lang) => {
    const map = {
      javascript: 'javascript',
      typescript: 'typescript',
      python: 'python',
      java: 'java',
      cpp: 'cpp',
      go: 'go',
      rust: 'rust',
      html: 'html',
      css: 'css',
    };
    return map[lang] || 'plaintext';
  };

  const getLanguageFromFileName = (fileName) => {
    if (!fileName) return 'javascript';
    const ext = fileName.split('.').pop()?.toLowerCase();
    const extMap = {
      js: 'javascript',
      jsx: 'javascript',
      ts: 'typescript',
      tsx: 'typescript',
      py: 'python',
      java: 'java',
      cpp: 'cpp',
      c: 'c',
      go: 'go',
      rs: 'rust',
      html: 'html',
      css: 'css',
      json: 'json',
      md: 'markdown',
      rb: 'ruby',
      php: 'php',
      cs: 'csharp',
      swift: 'swift',
      kt: 'kotlin',
    };
    return extMap[ext] || 'javascript';
  };

  return (
    <div className="h-screen bg-surface-dark flex flex-col">
      <motion.header
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="h-14 bg-surface-darker border-b border-surface-border flex items-center px-4 shrink-0"
      >
        <button
          onClick={() => navigate('/dashboard')}
          className="text-zinc-400 hover:text-white transition-colors mr-4"
        >
          ← Back
        </button>
        <div className="flex-1">
          <h1 className="text-lg font-semibold text-white">
            {currentProject?.name || 'Loading...'}
          </h1>
        </div>
        
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowFiles(!showFiles)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showFiles ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            📁 Files
          </button>
          <button
            onClick={() => setShowOutput(!showOutput)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showOutput ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            📺 Output
          </button>
          <button
            onClick={() => setShowComments(!showComments)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showComments ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            💬 Comments
          </button>
          <button
            onClick={() => setShowChat(!showChat)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showChat ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            💭 Chat
          </button>
          <button
            onClick={() => setShowCollab(!showCollab)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showCollab ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            👥 Collab
          </button>
          <button
            onClick={() => setShowVersionHistory(!showVersionHistory)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showVersionHistory ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            🕒 Versions
          </button>
          
          <motion.button
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={handleRunCode}
            className="ml-4 px-4 py-1.5 bg-accent-cyan text-surface-dark rounded-lg text-sm font-semibold"
          >
            ▶️ Run
          </motion.button>
        </div>
      </motion.header>

      <div className="flex-1 flex overflow-hidden">
        <AnimatePresence>
          {showFiles && (
            <ResizablePanel 
              defaultWidth={filePanelWidth} 
              minWidth={150} 
              maxWidth={400}
              onResize={setFilePanelWidth}
            >
              <FileExplorer onFileSelect={handleFileSelect} />
            </ResizablePanel>
          )}
        </AnimatePresence>

        <div className="flex-1 relative min-w-[300px]">
          <CodeEditor
            height="100%"
            language={getLanguage(language)}
            theme="vs-dark"
            value={code}
            onChange={handleCodeChange}
            onMount={handleEditorMount}
            options={{
              fontSize: 14,
              fontFamily: 'JetBrains Mono, monospace',
              minimap: { enabled: true },
              scrollBeyondLastLine: false,
              automaticLayout: true,
              tabSize: 2,
              wordWrap: 'on',
              lineNumbers: 'on',
              renderWhitespace: 'selection',
              cursorBlinking: 'smooth',
              cursorSmoothCaretAnimation: 'on',
              smoothScrolling: true,
            }}
          />
          
          <div className="absolute bottom-0 left-0 right-0 h-6 bg-surface-card border-t border-surface-border flex items-center px-4 text-xs text-zinc-500">
            <span>{language.toUpperCase()}</span>
            <span className="mx-4">Ln {useEditorStore.getState().cursorPosition.line}, Col {useEditorStore.getState().cursorPosition.column}</span>
            <span className="ml-auto">{isDirty ? '?' : ''} Saved</span>
            {collabSessionId && (
              <span className="ml-2 text-green-400">? Live</span>
            )}
          </div>
        </div>

        <AnimatePresence>
          {showOutput && (
            <ResizablePanel 
              defaultWidth={outputPanelWidth} 
              minWidth={180} 
              maxWidth={500}
              onResize={setOutputPanelWidth}
            >
              <OutputPanel />
            </ResizablePanel>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showComments && (
            <ResizablePanel 
              defaultWidth={commentPanelWidth} 
              minWidth={180} 
              maxWidth={450}
              onResize={setCommentPanelWidth}
            >
              <CommentsPanel />
            </ResizablePanel>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showChat && (
            <ResizablePanel 
              defaultWidth={chatPanelWidth} 
              minWidth={180} 
              maxWidth={450}
              onResize={setChatPanelWidth}
            >
              <ChatPanel projectId={projectId ? parseInt(projectId) : currentProject?.projectId} />
            </ResizablePanel>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showCollab && (
            <motion.div
              initial={{ width: 0, opacity: 0 }}
              animate={{ width: 280, opacity: 1 }}
              exit={{ width: 0, opacity: 0 }}
              className="bg-surface-darker border-l border-surface-border overflow-hidden"
            >
              <CollaborationPanel />
            </motion.div>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showVersionHistory && (
            <ResizablePanel 
              defaultWidth={versionPanelWidth} 
              minWidth={250} 
              maxWidth={450}
              onResize={setVersionPanelWidth}
            >
              <VersionHistory onShowDiff={handleShowDiff} />
            </ResizablePanel>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showDiffViewer && (
            <motion.div
              initial={{ width: 0, opacity: 0 }}
              animate={{ width: 600, opacity: 1 }}
              exit={{ width: 0, opacity: 0 }}
              className="bg-surface-darker border-l border-surface-border overflow-hidden"
            >
              <DiffViewer 
                snapshot1Id={diffSnapshots[0]} 
                snapshot2Id={diffSnapshots[1]} 
                onClose={handleCloseDiff}
              />
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
