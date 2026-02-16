import { useState, useEffect, useRef, useCallback } from 'react';
import { get, post, patch, uploadFile } from '../../services/api';
import './css/messages.css';

/*
 * Chat Window
 * displays message bubbles, handles sending messages and file uploads
 */
export default function ChatWindow({ conversation, currentUser, onMessageSent, onBack }) {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [initialLoading, setInitialLoading] = useState(false);
    const [sending, setSending] = useState(false);
    const [uploading, setUploading] = useState(false);

    // ref for auto-scrolling to bottom
    const messagesEndRef = useRef(null);
    const fileInputRef = useRef(null);
    const isFirstLoad = useRef(true);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const fetchMessages = useCallback(async () => {
        if (!conversation) return;

        // only show loading on first load of this conversation
        if (isFirstLoad.current) {
            setInitialLoading(true);
        }

        try {
            const data = await get(
                `/messages/chat/conversation/${conversation.conversationId}?userId=${currentUser.id}`
            );
            setMessages(data);
        } catch (err) {
            console.error('Error fetching messages:', err);
        } finally {
            setInitialLoading(false);
            isFirstLoad.current = false;
        }
    }, [conversation, currentUser.id]);

    // mark conversation as read when opened
    const markAsRead = useCallback(async () => {
        if (!conversation) return;

        try {
            await patch(
                `/messages/chat/conversation/${conversation.conversationId}/read?userId=${currentUser.id}`
            );
        } catch (err) {
            console.error('Error marking as read:', err);
        }
    }, [conversation, currentUser.id]);

    // fetch messages when conversation changes
    useEffect(() => {
        if (conversation) {
            isFirstLoad.current = true;
            fetchMessages();
            markAsRead();
        } else {
            setMessages([]);
            isFirstLoad.current = true;
        }
    }, [conversation, fetchMessages, markAsRead]);

    // auto-scroll to bottom when new messages arrive
    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    // send a text message
    const handleSendMessage = async (e) => {
        e.preventDefault();

        if (!newMessage.trim() || sending) return;

        setSending(true);
        try {
            const sentMessage = await post('/messages/chat/send', {
                senderId: currentUser.id,
                recipientId: conversation.otherUserId,
                message: newMessage.trim()
            });

            // add to messages list
            setMessages(prev => [...prev, sentMessage]);
            setNewMessage('');
            onMessageSent?.();
        } catch (err) {
            console.error('Error sending message:', err);
        } finally {
            setSending(false);
        }
    };

    // handle file selection
    const handleFileSelect = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setUploading(true);
        try {
            // upload file first
            const uploadResult = await uploadFile('/upload/chat', file);

            if (uploadResult.success) {
                // send message with attachment
                const sentMessage = await post('/messages/chat/send-with-attachment', {
                    senderId: currentUser.id,
                    recipientId: conversation.otherUserId,
                    message: '', // optional caption
                    messageType: uploadResult.type,
                    attachmentUrl: uploadResult.url,
                    attachmentName: uploadResult.originalName
                });

                setMessages(prev => [...prev, sentMessage]);
                onMessageSent?.();
            }
        } catch (err) {
            console.error('Error uploading file:', err);
        } finally {
            setUploading(false);
            // reset file input
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    // format message time
    const formatTime = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    // format date for date separators
    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        if (date.toDateString() === today.toDateString()) {
            return 'Today';
        } else if (date.toDateString() === yesterday.toDateString()) {
            return 'Yesterday';
        } else {
            return date.toLocaleDateString([], { month: 'long', day: 'numeric', year: 'numeric' });
        }
    };

    // check if we need a date separator between messages
    const needsDateSeparator = (currentMsg, prevMsg) => {
        if (!prevMsg) return true;

        const currentDate = new Date(currentMsg.sentAt).toDateString();
        const prevDate = new Date(prevMsg.sentAt).toDateString();

        return currentDate !== prevDate;
    };

    // render attachment based on type
    const renderAttachment = (message) => {
        if (message.messageType === 'IMAGE') {
            return (
                <div className="message-image">
                    <img
                        src={`http://localhost:8080${message.attachmentUrl}`}
                        alt={message.attachmentName || 'Image'}
                        onClick={() => window.open(`http://localhost:8080${message.attachmentUrl}`, '_blank')}
                    />
                </div>
            );
        } else if (message.messageType === 'FILE') {
            return (
                <a
                    href={`http://localhost:8080${message.attachmentUrl}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="message-file"
                >
                    <i className="pi pi-file"></i>
                    <span>{message.attachmentName || 'Download file'}</span>
                </a>
            );
        }
        return null;
    };

    // no conversation selected
    if (!conversation) {
        return (
            <div className="chat-window empty">
                <div className="empty-state">
                    <i className="pi pi-comments"></i>
                    <h3>Select a conversation</h3>
                    <p>Choose from your existing conversations or start a new one</p>
                </div>
            </div>
        );
    }

    return (
        <div className="chat-window">
            <div className="chat-header">
                <button className="chat-back-btn" onClick={onBack} title="Back">
                    <i className="pi pi-arrow-left"></i>
                </button>
                <div className="chat-header-avatar">
                    <i className="pi pi-user"></i>
                </div>
                <div className="chat-header-info">
                    <span className="chat-header-name">{conversation.otherUserName}</span>
                    <span className="chat-header-role">{conversation.otherUserRole}</span>
                </div>
            </div>

            <div className="chat-messages">
                {initialLoading ? (
                    <div className="messages-loading">
                        <i className="pi pi-spin pi-spinner"></i>
                        <span>Loading messages...</span>
                    </div>
                ) : messages.length === 0 ? (
                    <div className="messages-empty">
                        <p>No messages yet. Say hello!</p>
                    </div>
                ) : (
                    messages.map((message, index) => (
                        <div key={message.id}>
                            {needsDateSeparator(message, messages[index - 1]) && (
                                <div className="date-separator">
                                    <span>{formatDate(message.sentAt)}</span>
                                </div>
                            )}

                            <div
                                className={`message ${message.senderId === currentUser.id ? 'sent' : 'received'}`}
                            >
                                <div className="message-bubble">
                                    {renderAttachment(message)}

                                    {message.message && (
                                        <p className="message-text">{message.message}</p>
                                    )}

                                    <span className="message-time">
                                        {formatTime(message.sentAt)}
                                        {message.senderId === currentUser.id && (
                                            <i className={`pi ${message.isRead ? 'pi-check-circle' : 'pi-check'}`}></i>
                                        )}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))
                )}
                <div ref={messagesEndRef} />
            </div>

            <form className="chat-input" onSubmit={handleSendMessage}>
                <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handleFileSelect}
                    accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.txt"
                    style={{ display: 'none' }}
                />
                <button
                    type="button"
                    className="attach-btn"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploading}
                    title="Attach file"
                >
                    {uploading ? (
                        <i className="pi pi-spin pi-spinner"></i>
                    ) : (
                        <i className="pi pi-paperclip"></i>
                    )}
                </button>

                <input
                    type="text"
                    placeholder="Type a message..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    disabled={sending || uploading}
                />

                <button
                    type="submit"
                    className="send-btn"
                    disabled={!newMessage.trim() || sending || uploading}
                >
                    {sending ? (
                        <i className="pi pi-spin pi-spinner"></i>
                    ) : (
                        <i className="pi pi-send"></i>
                    )}
                </button>
            </form>
        </div>
    );
}
