import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '../../services/api';
import ConversationList from './ConversationList';
import ChatWindow from './ChatWindow';
import './css/messages.css';

export default function MessagesTab() {
    const navigate = useNavigate();

    const [currentUser, setCurrentUser] = useState(null);
    const [conversations, setConversations] = useState([]);
    const [selectedConversation, setSelectedConversation] = useState(null);
    const [initialLoading, setInitialLoading] = useState(true);
    const [error, setError] = useState(null);

    const isFirstLoad = useRef(true);
    const isFetching = useRef(false);

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
        if (isFetching.current) return;
        if (isFirstLoad.current) {
            setInitialLoading(true);
        }
        setError(null);

        isFetching.current = true;
        try {
            const data = await get(`/messages/chat/conversations?userId=${currentUser.id}`);
            setConversations(data);
        } catch (err) {
            console.error('Error fetching conversations:', err);
            if (isFirstLoad.current) {
                setError('Failed to load conversations');
            }
        } finally {
            isFetching.current = false;
            setInitialLoading(false);
            isFirstLoad.current = false;
        }
    }, [currentUser]);

    useEffect(() => {
        if (currentUser) {
            fetchConversations();

            const interval = setInterval(fetchConversations, 5000);
            return () => clearInterval(interval);
        }
    }, [currentUser, fetchConversations]);

    const handleSelectConversation = (conversation) => {
        setSelectedConversation(conversation);
    };

    const handleBackToList = () => {
        setSelectedConversation(null);
    };

    const handleConversationUpdated = () => {
        fetchConversations();
    };

    const handleStartNewChat = async (otherUserId, otherUserName, otherUserRole) => {
        try {
            const data = await get(`/messages/chat/start/${otherUserId}?currentUserId=${currentUser.id}`);

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
            fetchConversations();
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
                <ConversationList
                    conversations={conversations}
                    selectedConversation={selectedConversation}
                    onSelectConversation={handleSelectConversation}
                    onStartNewChat={handleStartNewChat}
                    currentUserId={currentUser.id}
                    loading={initialLoading}
                    error={error}
                />

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
