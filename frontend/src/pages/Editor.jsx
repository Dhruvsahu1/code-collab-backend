import { useState, useRef, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import CodeEditor from '@monaco-editor/react';
import toast from 'react-hot-toast';
import { useProjectStore, useEditorStore, useExecutionStore, useAuthStore, useVersionStore } from '../store';
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
  
  // Panel widths for resizing
  const [filePanelWidth, setFilePanelWidth] = useState(220);
  const [outputPanelWidth, setOutputPanelWidth] = useState(280);
  const [chatPanelWidth, setChatPanelWidth] = useState(280);
  const [commentPanelWidth, setCommentPanelWidth] = useState(280);
  const [versionPanelWidth, setVersionPanelWidth] = useState(300);
  
  // CRITICAL: Prevent infinite loops - track if update is from remote
  const [isRemoteUpdate, setIsRemoteUpdate] = useState(false);
  const currentUserId = useAuthStore((state) => state.user)?.id;
  
  const { currentProject, setCurrentProject, setCurrentFile } = useProjectStore();
  const { code, setCode, language, setLanguage, isDirty, markClean, setCursorPosition } = useEditorStore();
  const { setOutput, setRunning, setCurrentJob } = useExecutionStore();

  const debouncedCode = useDebounce(code, 1500);
  const saveTimeoutRef = useRef(null);

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
      console.log('Loading project from URL:', projectId);
      if (!projectId && collabSessionId) return;
      try {
        const response = await projectAPI.getById(projectId);
        console.log('Project API response:', response.data);
        setCurrentProject(response.data);
        console.log('Set currentProject to:', response.data);
      } catch (error) {
        console.error('Failed to load project:', error);
        toast.error('Failed to load project: ' + (error.response?.data?.message || error.message));
      }
    };
    loadProject();
  }, [projectId, setCurrentProject]);

  // Auto-connect to collaboration session
  useEffect(() => {
    if (collabSessionId) {
      console.log('Auto-connecting to collaboration session:', collabSessionId);
      
      // Fetch existing session code first
      collabAPI.getSession(collabSessionId)
        .then(response => {
          if (response.data?.code) {
            console.log('Loading existing session code');
            setCode(response.data.code);
          }
        })
        .catch(err => console.error('Failed to load session:', err));

      collabWebSocket.connect(
        collabSessionId,
        () => {
          toast.success('Connected to collaboration session!');
        },
        (newCode, userId) => {
          console.log('Code update received:', newCode, 'from user:', userId);
          // CRITICAL: Skip if this is our own update (prevent infinite loop)
          if (userId && currentUserId && String(userId) === String(currentUserId)) {
            console.log('Skipping own update');
            return;
          }
          // Mark as remote update to prevent sending back
          setIsRemoteUpdate(true);
          setCode(newCode || '');
          // Reset after a short delay
          setTimeout(() => setIsRemoteUpdate(false), 100);
        },
        (cursorData) => {
          console.log('Cursor update:', cursorData);
        },
        (message) => {
          console.log('Participant update:', message);
          if (message.type === 'SESSION_ENDED') {
            toast.error('Session has ended');
            collabWebSocket.disconnect();
            navigate('/dashboard');
          }
        }
      );
    }
    return () => {
      if (collabSessionId) {
        collabWebSocket.disconnect();
      }
    };
  }, [collabSessionId]);

  const handleFileSelect = async (file) => {
    console.log('Editor handleFileSelect called with:', file);
    if (file.isFolder) return;
    
    const currentFile = useProjectStore.getState().currentFile;
    if (currentFile && isDirty) {
      try {
        await fileAPI.updateContent(currentFile.fileId, { content: code });
      } catch (error) {
        console.error('Failed to save file:', error);
      }
    }
    
    try {
      const response = await fileAPI.getContent(file.fileId);
      console.log('Got content:', response.data.content?.substring(0, 50));
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
    setCode(value || '');
    // Send code changes to collaboration session ONLY if local change (not from WebSocket)
    if (collabSessionId && collabWebSocket.isConnected() && !isRemoteUpdate) {
      const user = useAuthStore.getState().user;
      collabWebSocket.sendCodeChange(value || '', user?.id);
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
      // Refresh version history if open
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
      {/* Header */}
      <motion.header
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="h-14 bg-surface-darker border-b border-surface-border flex items-center px-4 shrink-0"
      >
        <button
          onClick={() => navigate('/dashboard')}
          className="text-zinc-400 hover:text-white transition-colors mr-4"
        >
          ←
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
             ▶️ Output
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
             📜 Versions
           </button>
           
           <motion.button
             whileHover={{ scale: 1.05 }}
             whileTap={{ scale: 0.95 }}
             onClick={handleRunCode}
             className="ml-4 px-4 py-1.5 bg-accent-cyan text-surface-dark rounded-lg text-sm font-semibold"
           >
             ▶ Run
           </motion.button>
         </div>
      </motion.header>

      {/* Main Content - Resizable Panels */}
      <div className="flex-1 flex overflow-hidden">
        {/* File Explorer */}
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

        {/* Editor - Takes remaining space */}
        <div className="flex-1 relative min-w-[300px]">
          <CodeEditor
            height="100%"
            language={getLanguage(language)}
            theme="vs-dark"
            value={code}
            onChange={handleCodeChange}
            onMount={(editor) => {
              editor.onDidChangeCursorPosition((e) => {
                setCursorPosition({
                  line: e.position.lineNumber,
                  column: e.position.column,
                });
              });
            }}
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
          
          {/* Status Bar */}
          <div className="absolute bottom-0 left-0 right-0 h-6 bg-surface-card border-t border-surface-border flex items-center px-4 text-xs text-zinc-500">
            <span>{language.toUpperCase()}</span>
            <span className="mx-4">Ln {useEditorStore.getState().cursorPosition.line}, Col {useEditorStore.getState().cursorPosition.column}</span>
            <span className="ml-auto">{isDirty ? '●' : ''} Saved</span>
          </div>
        </div>

        {/* Output Panel */}
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

        {/* Comments Panel */}
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

        {/* Chat Panel */}
        <AnimatePresence>
          {showChat && (
            <ResizablePanel 
              defaultWidth={chatPanelWidth} 
              minWidth={180} 
              maxWidth={450}
              onResize={setChatPanelWidth}
            >
              <ChatPanel projectId={parseInt(projectId)} />
            </ResizablePanel>
          )}
        </AnimatePresence>

         {/* Collaboration Panel - Fixed width */}
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

         {/* Version History Panel - Resizable */}
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

         {/* Diff Viewer Panel - Fixed width or modal? */}
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