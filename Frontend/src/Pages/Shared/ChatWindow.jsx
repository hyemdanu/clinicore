import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { get, post, patch, API_BASE_URL } from '../../services/api';
import './css/messages.css';

export default function ChatWindow({ conversation, currentUser, onMessageSent, onBack }) {
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [sending, setSending] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [loadingChat, setLoadingChat] = useState(false);

    const messagesEndRef = useRef(null);
    const fileInputRef = useRef(null);
    const isFirstLoad = useRef(true);
    const lastSentAt = useRef(0);
    const prevMessageCount = useRef(0);
    const activeConversationId = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const fetchMessages = useCallback(async () => {
        if (!conversation) return;

        if (!isFirstLoad.current && Date.now() - lastSentAt.current < 5000) return;

        const fetchingConversationId = conversation.conversationId;
        if (isFirstLoad.current) setLoadingChat(true);
        try {
            const data = await get(
                `/messages/chat/conversation/${fetchingConversationId}?userId=${currentUser.id}`
            );
            if (activeConversationId.current !== fetchingConversationId) return;
            setMessages(prev => (isFirstLoad.current || data.length >= prev.length) ? data : prev);
        } catch (err) {
            console.error('Error fetching messages:', err);
        } finally {
            isFirstLoad.current = false;
            setLoadingChat(false);
        }
    }, [conversation, currentUser.id]);

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

    useEffect(() => {
        if (conversation) {
            activeConversationId.current = conversation.conversationId;
            isFirstLoad.current = true;
            prevMessageCount.current = 0;
            setMessages([]);
            fetchMessages();
            markAsRead();

            const interval = setInterval(fetchMessages, 3000);
            return () => clearInterval(interval);
        } else {
            setMessages([]);
            isFirstLoad.current = true;
        }
    }, [conversation, fetchMessages, markAsRead]);

    useEffect(() => {
        if (messages.length > prevMessageCount.current) {
            scrollToBottom();
        }
        prevMessageCount.current = messages.length;
    }, [messages]);

    const handleSendMessage = async (e) => {
        e.preventDefault();

        if (!newMessage.trim() || sending) return;

        setSending(true);
        lastSentAt.current = Date.now();
        try {
            const sentMessage = await post('/messages/chat/send', {
                senderId: currentUser.id,
                recipientId: conversation.otherUserId,
                message: newMessage.trim()
            });

            setMessages(prev => [...prev, sentMessage]);
            setNewMessage('');
            lastSentAt.current = Date.now();
            onMessageSent?.();
        } catch (err) {
            console.error('Error sending message:', err);
        } finally {
            setSending(false);
        }
    };

    const handleFileSelect = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setUploading(true);
        lastSentAt.current = Date.now();
        try {
            const formData = new FormData();
            formData.append('senderId', currentUser.id);
            formData.append('recipientId', conversation.otherUserId);
            formData.append('message', '');
            formData.append('file', file);

            const response = await fetch(`${API_BASE_URL}/messages/chat/send-with-attachment`, {
                method: 'POST',
                headers: { Authorization: `Bearer ${currentUser.token}` },
                body: formData,
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                console.error('Error uploading file:', errorData.error || `Upload failed (${response.status})`);
                return;
            }

            const sentMessage = await response.json();
            setMessages(prev => [...prev, sentMessage]);
            lastSentAt.current = Date.now();
            onMessageSent?.();
        } catch (err) {
            console.error('Error uploading file:', err);
        } finally {
            setUploading(false);
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    const formatTime = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

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

    const needsDateSeparator = (currentMsg, prevMsg) => {
        if (!prevMsg) return true;

        const currentDate = new Date(currentMsg.sentAt).toDateString();
        const prevDate = new Date(prevMsg.sentAt).toDateString();

        return currentDate !== prevDate;
    };

    // pre-compute formatted dates/times so they don't recalculate on every render
    const processedMessages = useMemo(() =>
        messages.map((msg, index) => ({
            ...msg,
            formattedTime: formatTime(msg.sentAt),
            formattedDate: formatDate(msg.sentAt),
            showDateSeparator: needsDateSeparator(msg, messages[index - 1])
        })),
    [messages]);

    const downloadAttachment = async (message) => {
        if (message.messageType !== 'IMAGE' && message.messageType !== 'FILE') return;
        try {
            const response = await fetch(`${API_BASE_URL}/messages/chat/attachment/${message.id}`, {
                headers: { Authorization: `Bearer ${currentUser.token}` },
            });
            if (!response.ok) {
                console.error('Error downloading attachment:', `fetch failed (${response.status})`);
                return;
            }

            const blob = await response.blob();
            const objectUrl = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = objectUrl;
            a.download = message.attachmentName || 'attachment';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(objectUrl);
        } catch (err) {
            console.error('Error downloading attachment:', err);
        }
    };

    const renderAttachment = (message) => {
        if (message.messageType !== 'IMAGE' && message.messageType !== 'FILE') return null;
        const iconClass = message.messageType === 'IMAGE' ? 'pi pi-image' : 'pi pi-file';
        return (
            <button
                type="button"
                className="message-file"
                onClick={() => downloadAttachment(message)}
            >
                <i className={iconClass}></i>
                <span>{message.attachmentName || 'Download attachment'}</span>
            </button>
        );
    };

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
                {loadingChat && (
                    <div className="chat-loading">
                        <i className="pi pi-spin pi-spinner"></i>
                    </div>
                )}
                {!loadingChat && processedMessages.length === 0 ? null : (
                    processedMessages.map((message) => (
                        <div key={message.id}>
                            {message.showDateSeparator && (
                                <div className="date-separator">
                                    <span>{message.formattedDate}</span>
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
                                        {message.formattedTime}
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
                    className="visually-hidden-input"
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
                    aria-label="Send message"
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
