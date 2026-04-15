import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import Editor from '@monaco-editor/react';
import toast from 'react-hot-toast';
import { useProjectStore, useEditorStore, useExecutionStore } from '../store';
import { projectAPI, fileAPI, executionAPI } from '../services/api';
import FileExplorer from '../components/FileExplorer';
import OutputPanel from '../components/OutputPanel';
import CollaborationPanel from '../components/CollaborationPanel';
import CommentsPanel from '../components/CommentsPanel';

export default function EditorPage() {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [showFiles, setShowFiles] = useState(true);
  const [showOutput, setShowOutput] = useState(true);
  const [showCollab, setShowCollab] = useState(false);
  const [showComments, setShowComments] = useState(false);
  
  const { currentProject, setCurrentProject } = useProjectStore();
  const { code, setCode, language, setLanguage, isDirty, markClean, setCursorPosition } = useEditorStore();
  const { setOutput, setRunning, setCurrentJob } = useExecutionStore();

  useEffect(() => {
    const loadProject = async () => {
      try {
        const response = await projectAPI.getById(projectId);
        setCurrentProject(response.data);
      } catch (error) {
        toast.error('Failed to load project');
        navigate('/dashboard');
      }
    };
    loadProject();
  }, [projectId, setCurrentProject, navigate]);

  const handleFileSelect = async (file) => {
    if (isDirty) {
      // Auto-save could be implemented here
    }
    try {
      const response = await fileAPI.getContent(file.id);
      setCode(response.data.content || '');
      markClean();
    } catch (error) {
      console.error('Failed to load file content');
    }
  };

  const handleCodeChange = (value) => {
    setCode(value || '');
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
        fileId: file.id,
        language: language,
        code: code,
      });
      
      const job = response.data;
      setCurrentJob(job);
      
      // Simulate execution result
      setTimeout(() => {
        setOutput(`Job ${job.jobId} started...\n\n(Execution sandbox coming soon)`);
        setRunning(false);
      }, 1000);
    } catch (error) {
      setOutput(`Error: ${error.message}`);
      setRunning(false);
    }
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

  return (
    <div className="h-screen bg-surface-dark flex flex-col">
      {/* Header */}
      <motion.header
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="h-14 bg-surface-darker border-b border-surface-border flex items-center px-4"
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
            onClick={() => setShowCollab(!showCollab)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showCollab ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            👥 Collab
          </button>
          <button
            onClick={() => setShowComments(!showComments)}
            className={`px-3 py-1.5 rounded-lg text-sm transition-colors ${
              showComments ? 'bg-accent-cyan/20 text-accent-cyan' : 'text-zinc-400 hover:text-white'
            }`}
          >
            💬 Comments
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

      {/* Main Content */}
      <div className="flex-1 flex overflow-hidden">
        {/* File Explorer */}
        <AnimatePresence>
          {showFiles && (
            <motion.div
              initial={{ width: 0, opacity: 0 }}
              animate={{ width: 260, opacity: 1 }}
              exit={{ width: 0, opacity: 0 }}
              className="bg-surface-darker border-r border-surface-border overflow-hidden"
            >
              <FileExplorer onFileSelect={handleFileSelect} />
            </motion.div>
          )}
        </AnimatePresence>

        {/* Editor */}
        <div className="flex-1 relative">
          <Editor
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
            <motion.div
              initial={{ width: 0, opacity: 0 }}
              animate={{ width: 320, opacity: 1 }}
              exit={{ width: 0, opacity: 0 }}
              className="bg-surface-darker border-l border-surface-border overflow-hidden"
            >
              <OutputPanel />
            </motion.div>
          )}
        </AnimatePresence>

        {/* Collaboration Panel */}
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

        {/* Comments Panel */}
        <AnimatePresence>
          {showComments && (
            <motion.div
              initial={{ width: 0, opacity: 0 }}
              animate={{ width: 300, opacity: 1 }}
              exit={{ width: 0, opacity: 0 }}
              className="bg-surface-darker border-l border-surface-border overflow-hidden"
            >
              <CommentsPanel />
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}