import { useState, useEffect, useCallback } from 'react';
import { get } from '../../services/api';
import './css/messages.css';

// conversation list - left panel
// shows all conversations with preview and unread count
// has new chat button to start messaging someone
export default function ConversationList({
    conversations,
    selectedConversation,
    onSelectConversation,
    onStartNewChat,
    currentUserId,
    loading,
    error
}) {
    const [showNewChatModal, setShowNewChatModal] = useState(false);
    const [availableUsers, setAvailableUsers] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [loadingUsers, setLoadingUsers] = useState(false);

    const fetchAvailableUsers = useCallback(async () => {
        setLoadingUsers(true);
        try {
            const users = await get(`/messages/chat/available-users?currentUserId=${currentUserId}`);
            setAvailableUsers(users);
        } catch (err) {
            console.error('Error fetching users:', err);
        } finally {
            setLoadingUsers(false);
        }
    }, [currentUserId]);

    // fetch users when modal opens
    useEffect(() => {
        if (showNewChatModal) {
            fetchAvailableUsers();
        }
    }, [showNewChatModal, fetchAvailableUsers]);

    // filter users by search
    const filteredUsers = availableUsers.filter(user =>
        user.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // format time
    const formatTime = (dateString) => {
        if (!dateString) return '';

        const date = new Date(dateString);
        const now = new Date();
        const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));

        if (diffDays === 0) {
            // today
            return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        } else if (diffDays === 1) {
            return 'Yesterday';
        } else if (diffDays < 7) {
            // this week
            return date.toLocaleDateString([], { weekday: 'short' });
        } else {
            // older
            return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
        }
    };

    // get message preview text
    const getMessagePreview = (conversation) => {
        if (!conversation.lastMessage) return 'No messages yet';

        if (conversation.lastMessageType === 'IMAGE') {
            return '📷 Photo';
        } else if (conversation.lastMessageType === 'FILE') {
            return '📎 File';
        }

        // truncate if too long
        const maxLength = 30;
        if (conversation.lastMessage.length > maxLength) {
            return conversation.lastMessage.substring(0, maxLength) + '...';
        }
        return conversation.lastMessage;
    };

    // start new chat with user
    const handleUserClick = (user) => {
        onStartNewChat(user.id, user.name, user.role);
        setShowNewChatModal(false);
        setSearchQuery('');
    };

    return (
        <div className="conversation-list">
            <div className="conversation-list-header">
                <h3>Messages</h3>
                <button
                    className="new-chat-btn"
                    onClick={() => setShowNewChatModal(true)}
                    title="New conversation"
                >
                    <i className="pi pi-plus"></i>
                </button>
            </div>

            {/* search bar */}
            <div className="conversation-search">
                <i className="pi pi-search"></i>
                <input
                    type="text"
                    placeholder="Search conversations..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </div>

            {/* conversation list */}
            <div className="conversations">
                {loading ? (
                    <div className="conversations-loading">
                        <i className="pi pi-spin pi-spinner"></i>
                        <span>Loading...</span>
                    </div>
                ) : error ? (
                    <div className="conversations-error">{error}</div>
                ) : conversations.length === 0 ? (
                    <div className="conversations-empty">
                        <i className="pi pi-comments"></i>
                        <p>No conversations yet</p>
                        <button onClick={() => setShowNewChatModal(true)}>
                            Start a conversation
                        </button>
                    </div>
                ) : (
                    conversations
                        .filter(conv => conv.otherUserName.toLowerCase().includes(searchQuery.toLowerCase()))
                        .map(conversation => (
                            <div
                                key={conversation.conversationId}
                                className={`conversation-item ${selectedConversation?.conversationId === conversation.conversationId ? 'selected' : ''}`}
                                onClick={() => onSelectConversation(conversation)}
                            >
                                <div className="conversation-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <div className="conversation-info">
                                    <div className="conversation-header">
                                        <span className="conversation-name">{conversation.otherUserName}</span>
                                        <span className="conversation-time">{formatTime(conversation.lastMessageAt)}</span>
                                    </div>
                                    <div className="conversation-preview">
                                        <span className="preview-text">{getMessagePreview(conversation)}</span>
                                        {conversation.unreadCount > 0 && (
                                            <span className="unread-badge">{conversation.unreadCount}</span>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))
                )}
            </div>

            {/* new chat modal */}
            {showNewChatModal && (
                <div className="modal-overlay" onClick={() => setShowNewChatModal(false)}>
                    <div className="new-chat-modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>New Conversation</h3>
                            <button onClick={() => setShowNewChatModal(false)}>
                                <i className="pi pi-times"></i>
                            </button>
                        </div>

                        <div className="modal-search">
                            <i className="pi pi-search"></i>
                            <input
                                type="text"
                                placeholder="Search users..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                autoFocus
                            />
                        </div>

                        <div className="user-list">
                            {loadingUsers ? (
                                <div className="users-loading">Loading users...</div>
                            ) : filteredUsers.length === 0 ? (
                                <div className="users-empty">No users found</div>
                            ) : (
                                filteredUsers.map(user => (
                                    <div
                                        key={user.id}
                                        className="user-item"
                                        onClick={() => handleUserClick(user)}
                                    >
                                        <div className="user-avatar">
                                            <i className="pi pi-user"></i>
                                        </div>
                                        <div className="user-info">
                                            <span className="user-name">{user.name}</span>
                                            <span className="user-role">{user.role}</span>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
