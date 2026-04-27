import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { useProjectStore, useAuthStore } from '../store';
import { commentAPI } from '../services/api';

function CommentThread({ comment, onReply, onResolve, onDelete, currentUserId }) {
  const [showReplies, setShowReplies] = useState(false);
  const [replyText, setReplyText] = useState('');
  const [showReplyInput, setShowReplyInput] = useState(false);

  const handleReply = async (e) => {
    e.preventDefault();
    if (!replyText.trim()) return;
    try {
      await onReply(comment.commentId, replyText);
      setReplyText('');
      setShowReplyInput(false);
    } catch (error) {
      toast.error('Failed to add reply');
    }
  };

  const isAuthor = comment.authorId === currentUserId;

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={`p-3 rounded-lg mb-2 ${
        comment.resolved 
          ? 'bg-green-500/10 border border-green-500/30' 
          : 'bg-surface-hover border border-transparent'
      }`}
    >
      <div className="flex items-start justify-between mb-2">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-white">User {comment.authorId}</span>
          <span className="text-xs text-zinc-500">Line {comment.lineNumber}</span>
        </div>
        <div className="flex items-center gap-1">
          {comment.resolved ? (
            <span className="text-xs text-green-400 px-2 py-1">✓ Resolved</span>
          ) : (
            <>
              <button
                onClick={() => setShowReplyInput(!showReplyInput)}
                className="text-xs text-zinc-500 hover:text-accent-cyan px-2 py-1"
              >
                Reply
              </button>
              {isAuthor && (
                <button
                  onClick={() => onResolve(comment.commentId)}
                  className="text-xs text-zinc-500 hover:text-green-400 px-2 py-1"
                >
                  ✓
                </button>
              )}
              <button
                onClick={() => onDelete(comment.commentId)}
                className="text-xs text-zinc-500 hover:text-red-400 px-2 py-1"
              >
                ✕
              </button>
            </>
          )}
        </div>
      </div>
      
      <p className="text-sm text-zinc-300 mb-2">{comment.content}</p>
      
      <div className="text-xs text-zinc-500 mb-2">
        {comment.createdAt ? new Date(comment.createdAt).toLocaleString() : 'Just now'}
        {comment.snapshotId && <span className="ml-2">📋 Snap: {comment.snapshotId}</span>}
      </div>

      {comment.replies && comment.replies.length > 0 && (
        <button
          onClick={() => setShowReplies(!showReplies)}
          className="text-xs text-accent-cyan hover:underline"
        >
          {showReplies ? '▼' : '▶'} {comment.replies.length} {comment.replies.length === 1 ? 'Reply' : 'Replies'}
        </button>
      )}

      <AnimatePresence>
        {showReplies && comment.replies && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="ml-4 mt-2 border-l border-zinc-700 pl-2"
          >
            {comment.replies.map((reply) => (
              <div key={reply.commentId} className="py-2">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xs font-medium text-zinc-400">User {reply.authorId}</span>
                  <span className="text-xs text-zinc-600">
                    {reply.createdAt ? new Date(reply.createdAt).toLocaleString() : ''}
                  </span>
                </div>
                <p className="text-sm text-zinc-400">{reply.content}</p>
              </div>
            ))}
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {showReplyInput && (
          <motion.form
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            onSubmit={handleReply}
            className="mt-2"
          >
            <input
              type="text"
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              placeholder="Write a reply..."
              className="w-full px-2 py-1 text-sm bg-surface-darker border border-surface-border rounded text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan"
            />
            <div className="flex gap-2 mt-1">
              <button
                type="submit"
                className="text-xs text-accent-cyan hover:underline"
              >
                Send
              </button>
              <button
                type="button"
                onClick={() => setShowReplyInput(false)}
                className="text-xs text-zinc-500 hover:text-white"
              >
                Cancel
              </button>
            </div>
          </motion.form>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

export default function CommentsPanel({ showInlineComment = false, onLineClick }) {
  const { currentFile, currentProject } = useProjectStore();
  const { user } = useAuthStore();
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [selectedLine, setSelectedLine] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (currentFile?.fileId) {
      loadComments();
    }
  }, [currentFile?.fileId, commentAPI]);

  const loadComments = async () => {
    if (!currentFile?.fileId) return;
    setIsLoading(true);
    try {
      const response = await commentAPI.getByFile(currentFile.fileId);
      console.log('Comments loaded:', response.data);
      setComments(response.data || []);
    } catch (error) {
      console.error('Failed to load comments:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    if (!currentProject?.projectId) {
      toast.error('No project selected');
      return;
    }
    if (!currentFile?.fileId) {
      toast.error('No file selected');
      return;
    }

    try {
      const response = await commentAPI.create({
        projectId: currentProject.projectId,
        fileId: currentFile.fileId,
        content: newComment,
        authorId: user?.id || user?.userId || 1,
        lineNumber: selectedLine || 1,
      });
      setNewComment('');
      setSelectedLine(null);
      loadComments();
      toast.success('Comment added');
    } catch (error) {
      console.error('Error adding comment:', error.response || error);
      toast.error(error.response?.data?.message || 'Failed to add comment');
    }
  };

  const handleReply = async (parentId, content) => {
    await commentAPI.create({
      projectId: currentProject.projectId,
      fileId: currentFile.fileId,
      content: content,
      authorId: user?.id || user?.userId || 1,
      lineNumber: 1,
      parentCommentId: parentId,
    });
    loadComments();
  };

  const handleResolve = async (id) => {
    try {
      await commentAPI.resolve(id);
      loadComments();
    } catch (error) {
      toast.error('Failed to resolve comment');
    }
  };

  const handleDelete = async (id) => {
    try {
      await commentAPI.delete(id);
      loadComments();
      toast.success('Comment deleted');
    } catch (error) {
      toast.error('Failed to delete comment');
    }
  };

  const getLinesWithComments = () => {
    const lines = new Set();
    comments.forEach(c => {
      if (c.lineNumber && !c.resolved) lines.add(c.lineNumber);
    });
    return lines;
  };

  const linesWithComments = getLinesWithComments();

  return (
    <div className="h-full flex flex-col">
      <div className="p-4 border-b border-surface-border">
        <h3 className="text-sm font-semibold text-white">Comments</h3>
        {currentFile && (
          <p className="text-xs text-zinc-500 mt-1">{comments.length} comments in this file</p>
        )}
      </div>
      
      <div className="flex-1 overflow-y-auto p-4 space-y-2">
        {!currentFile ? (
          <p className="text-zinc-500 text-sm text-center py-8">
            Select a file to view comments
          </p>
        ) : isLoading ? (
          <div className="space-y-3">
            {[1, 2].map((i) => (
              <div key={i} className="h-20 bg-surface-hover rounded-lg animate-pulse" />
            ))}
          </div>
        ) : comments.length === 0 ? (
          <p className="text-zinc-500 text-sm text-center py-8">
            No comments yet. Start a discussion!
          </p>
        ) : (
          comments.map((comment) => (
            <CommentThread
              key={comment.commentId}
              comment={comment}
              onReply={handleReply}
              onResolve={handleResolve}
              onDelete={handleDelete}
              currentUserId={user?.id}
            />
          ))
        )}
      </div>

      {currentFile && (
        <form onSubmit={handleAddComment} className="p-4 border-t border-surface-border">
          <div className="mb-2">
            <input
              type="number"
              value={selectedLine || ''}
              onChange={(e) => setSelectedLine(parseInt(e.target.value) || null)}
              placeholder="Line # (optional)"
              className="w-20 px-2 py-1 mr-2 bg-surface-darker border border-surface-border rounded text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan"
              min="1"
            />
            {selectedLine && (
              <span className="text-xs text-zinc-500">Commenting on line {selectedLine}</span>
            )}
          </div>
          <div className="flex gap-2">
            <input
              type="text"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add a comment... (use @username to mention)"
              className="flex-1 px-3 py-2 bg-surface-darker border border-surface-border rounded-lg text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-accent-cyan"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-accent-cyan text-white rounded-lg text-sm font-medium hover:bg-cyan-400 transition-colors"
            >
              Add
            </button>
          </div>
        </form>
      )}
    </div>
  );
}