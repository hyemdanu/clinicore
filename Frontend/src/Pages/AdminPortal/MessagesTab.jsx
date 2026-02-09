import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '../../services/api';
import ConversationList from './ConversationList';
import ChatWindow from './ChatWindow';
import './css/messages.css';

// messages tab
// left side: list of conversations
// right side: active chat window
export default function MessagesTab() {
    const navigate = useNavigate();

    const [currentUser, setCurrentUser] = useState(null);
    const [conversations, setConversations] = useState([]);
    const [selectedConversation, setSelectedConversation] = useState(null);
    const [initialLoading, setInitialLoading] = useState(true);
    const [error, setError] = useState(null);

    // track if this is first load or a refresh
    const isFirstLoad = useRef(true);

    // get current user from localStorage
    useEffect(() => {
        const userStr = localStorage.getItem('currentUser');
        if (!userStr) {
            navigate('/');
            return;
        }
        const user = JSON.parse(userStr);
        setCurrentUser(user);
    }, [navigate]);

    const fetchConversations = useCallback(async () => {
        if (!currentUser) return;

        // show loading only on first load
        if (isFirstLoad.current) {
            setInitialLoading(true);
        }
        setError(null);

        try {
            const data = await get(`/messages/chat/conversations?userId=${currentUser.id}`);
            setConversations(data);
        } catch (err) {
            console.error('Error fetching conversations:', err);
            if (isFirstLoad.current) {
                setError('Failed to load conversations');
            }
        } finally {
            setInitialLoading(false);
            isFirstLoad.current = false;
        }
    }, [currentUser]);

    // fetch conversations when user is loaded
    useEffect(() => {
        if (currentUser) {
            fetchConversations();
        }
    }, [currentUser, fetchConversations]);

    // select a conversation
    const handleSelectConversation = (conversation) => {
        setSelectedConversation(conversation);
    };

    // go back to conversation list for smaller screen like mobile
    const handleBackToList = () => {
        setSelectedConversation(null);
    };

    // refresh conversations
    const handleConversationUpdated = () => {
        fetchConversations();
    };

    // start a new chat with someone
    const handleStartNewChat = async (otherUserId, otherUserName, otherUserRole) => {
        try {
            const data = await get(`/messages/chat/start/${otherUserId}?currentUserId=${currentUser.id}`);

            // create a temporary conversation object for the new chat
            const newConversation = {
                conversationId: data.conversationId,
                otherUserId: otherUserId,
                otherUserName: otherUserName,
                otherUserRole: otherUserRole,
                lastMessage: null,
                lastMessageAt: null,
                unreadCount: 0
            };

            setSelectedConversation(newConversation);
            fetchConversations(); // refresh without loading
        } catch (err) {
            console.error('Error starting conversation:', err);
        }
    };

    if (!currentUser) {
        return <div className="messages-loading">Loading...</div>;
    }

    return (
        <div className="messages-tab">
            <div className={`messages-container ${selectedConversation ? 'chat-active' : ''}`}>
                {/* conversation list */}
                <ConversationList
                    conversations={conversations}
                    selectedConversation={selectedConversation}
                    onSelectConversation={handleSelectConversation}
                    onStartNewChat={handleStartNewChat}
                    currentUserId={currentUser.id}
                    loading={initialLoading}
                    error={error}
                />

                {/* chat window */}
                <ChatWindow
                    conversation={selectedConversation}
                    currentUser={currentUser}
                    onMessageSent={handleConversationUpdated}
                    onBack={handleBackToList}
                />
            </div>
        </div>
    );
}
