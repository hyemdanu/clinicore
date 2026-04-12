// reads from .env.development file
// fallback to localhost for dev if .env.development is missing
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// base options for all requests
const defaultOptions = {
    headers: { "Content-Type": "application/json" },
    credentials: 'include',
};

// generic api fetch function (get by default)
const apiFetch = async (endpoint, method = 'GET', data = null) => {
    const config = {
        ...defaultOptions,
        method,
    };

    // attach JWT token if user is logged in
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (user?.token) {
        config.headers = { ...config.headers, Authorization: `Bearer ${user.token}` };
    }

    // add parsed body if data is provided
    if (data) {
        config.body = JSON.stringify(data);
    }

    // make the request using fetch(api endpoint, our configs)
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    if (!response.ok) {
        let errorMessage = `API Error: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorData.error || errorMessage;
        } catch {
            // If response is not JSON, use status text
            errorMessage = `${errorMessage} - ${response.statusText}`;
        }
        throw new Error(errorMessage);
    }

    // return the response as json
    return response.json();
};

// individual methods for convenience
// use these instead of apiFetch directly
// we can also add more methods here if needed...
export const get = (endpoint) => apiFetch(endpoint, 'GET');
export const post = (endpoint, data) => apiFetch(endpoint, 'POST', data);
export const put = (endpoint, data) => apiFetch(endpoint, 'PUT', data);
export const patch = (endpoint, data) => apiFetch(endpoint, 'PATCH', data);
export const del = (endpoint) => apiFetch(endpoint, 'DELETE');

// document upload - builds FormData with title + file, attaches JWT
export const uploadDocument = async (residentId, title, file) => {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    const formData = new FormData();
    formData.append('currentUserId', user.id);
    formData.append('residentId', residentId);
    formData.append('title', title);
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}/documents/upload`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${user.token}` },
        body: formData,
    });

    if (!response.ok) {
        let errorMessage = `Upload Error: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
        } catch {
            errorMessage = `${errorMessage} - ${response.statusText}`;
        }
        throw new Error(errorMessage);
    }

    return response.json();
};

// document delete - admin only
export const deleteDocument = async (documentId) => {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    const response = await fetch(`${API_BASE_URL}/documents/${documentId}?userId=${user.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${user.token}` },
    });

    if (!response.ok) {
        let errorMessage = `Delete Error: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
        } catch {
            errorMessage = `${errorMessage} - ${response.statusText}`;
        }
        throw new Error(errorMessage);
    }

    return response.json();
};

// file upload - uses FormData instead of JSON
// used for chat image/file uploads
export const uploadFile = async (endpoint, file) => {
    const formData = new FormData();
    formData.append('file', file);

    const headers = {};
    const user = JSON.parse(localStorage.getItem('currentUser'));
    if (user?.token) {
        headers['Authorization'] = `Bearer ${user.token}`;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        credentials: 'include',
        headers,
        body: formData,
        // don't set Content-Type header - browser will set it with boundary
    });

    if (!response.ok) {
        let errorMessage = `Upload Error: ${response.status}`;
        try {
            const errorData = await response.json();
            errorMessage = errorData.error || errorMessage;
        } catch {
            errorMessage = `${errorMessage} - ${response.statusText}`;
        }
        throw new Error(errorMessage);
    }

    return response.json();
};
