// our backend api
const API_BASE_URL = "http://localhost:8080/api";

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
            errorMessage = errorData.message || errorMessage;
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

// file upload - uses FormData instead of JSON
// used for chat image/file uploads
export const uploadFile = async (endpoint, file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        method: 'POST',
        credentials: 'include',
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
