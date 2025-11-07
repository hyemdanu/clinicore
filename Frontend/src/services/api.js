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
        throw new Error(`API Error: ${response.status}`);
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
export const del = (endpoint) => apiFetch(endpoint, 'DELETE');
