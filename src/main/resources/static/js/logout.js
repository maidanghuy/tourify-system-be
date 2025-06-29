document.addEventListener("DOMContentLoaded", function () {
    const username = localStorage.getItem("username");
    const greetingEl = document.getElementById("greeting");
    if (username && greetingEl) {
        greetingEl.textContent = `Hello, ${username}`;
    }

    const logoutLink = document.getElementById("logout-link");
    if (logoutLink) {
        logoutLink.addEventListener("click", async function (event) { // Added 'async' keyword here
            event.preventDefault(); // Prevent default link behavior

            const accessToken = localStorage.getItem('accessToken'); // Get the access token from localStorage

            if (accessToken) {
                try {
                    const response = await fetch('/tourify/api/auth/logout', {
                        method: 'POST', // Typically logout is a POST request
                        headers: {
                            'Content-Type': 'application/json', // Assuming your API expects JSON, adjust if needed
                            'Authorization': `Bearer ${accessToken}` // Add the Authorization header
                        },
                        // You might not need a body for logout, but if your API expects one, add it here:
                        // body: JSON.stringify({})
                    });

                    if (response.ok) {
                        // Successfully logged out on the server
                        console.log('Logout successful on server.');
                    } else {
                        // Handle server-side errors or invalid tokens
                        console.error('Logout failed on server:', response.status, response.statusText);
                        // Depending on your API, you might still want to clear local storage even if server logout fails
                        // e.g., if the token is already expired on the server, but still present locally.
                    }
                } catch (error) {
                    console.error('Error during logout API call:', error);
                    // Handle network errors
                }
            } else {
                console.warn('No access token found in localStorage. Proceeding with local logout.');
            }

            // Always clear local storage and redirect after attempting API call
            localStorage.removeItem("username"); // Clear username
            localStorage.removeItem("role");
            localStorage.removeItem("accessToken"); // Clear the accessToken


              window.location.href = "/tourify/login";
             // Redirect to login page
        });
    }
});