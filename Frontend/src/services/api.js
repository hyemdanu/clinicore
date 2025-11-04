const API_BASE_URL = "http://localhost:8080/api";

export const apiCall = async (endpoint, options = {}) => {
    const defaultOptions = {
        headers: { "Content-Type": "application/json" },
        credentials: 'include',  // Important for CORS with authentication
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...defaultOptions,
        ...options,
    });

    if (!response.ok) {
        throw new Error(`API Error: ${response.statusText}`);
    }

    return response.json();
};

export const post = async (endpoint, data) => {
       const response = await fetch(`${API_BASE_URL}${endpoint}`, {
           method: 'POST',
           headers: {
               'Content-Type': 'application/json',
           },
           credentials: 'include', // Important for CORS with cookies
           body: JSON.stringify(data)
       });
       
       if (!response.ok) {
           throw new Error(`API Error: ${response.status}`);
       }
       
       return response.json();
   };
