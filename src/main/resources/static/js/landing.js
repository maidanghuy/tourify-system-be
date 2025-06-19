window.onload = function () {
    const token = getCookie("access_token");

    if (token) {
        // Lưu vào localStorage
        localStorage.setItem("accessToken", token);
        // console.log("Token saved to localStorage:", token);

        // Xóa cookie ngay sau khi lưu
        deleteCookie("access_token");
        // console.log("Cookie 'access_token' deleted");
    } else {
        // console.warn("Cookie 'access_token' not found");
    }
};

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

function deleteCookie(name) {
    document.cookie = `${name}=; Max-Age=0; path=/;`;
}

document.addEventListener("DOMContentLoaded", function () {
    const accessToken = localStorage.getItem("accessToken");
    const dropdownContainer = document.querySelector(".dropdown");
    const logoutButton = document.getElementById("logout-link");

    if (accessToken) {
        // User is logged in - show dropdown and logout button
        if (dropdownContainer) {
            dropdownContainer.style.display = "block";
        }
        if (logoutButton) {
            logoutButton.innerHTML = '<i class="fas fa-sign-out-alt me-1"></i>Logout';
            // Remove any existing click handlers to avoid conflicts
            logoutButton.replaceWith(logoutButton.cloneNode(true));
            const newLogoutButton = document.getElementById("logout-link");

            // Add the logout functionality from logout.js
            newLogoutButton.addEventListener("click", async function (event) {
                event.preventDefault();

                try {
                    const response = await fetch('http://localhost:8080/tourify/api/auth/logout', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${accessToken}`
                        }
                    });

                    if (response.ok) {
                        console.log('Logout successful on server.');
                    } else {
                        console.error('Logout failed on server:', response.status, response.statusText);
                    }
                } catch (error) {
                    console.error('Error during logout API call:', error);
                }

                // Always clear local storage and redirect
                localStorage.removeItem("username");
                localStorage.removeItem("accessToken");
                window.location.href = "./login";
            });
        }
    } else {
        // User is not logged in - hide dropdown and show login button
        if (dropdownContainer) {
            dropdownContainer.style.display = "none";
        }
        if (logoutButton) {
            logoutButton.innerHTML = '<i class="fas fa-sign-in-alt me-1"></i>Login';
            // Remove any existing click handlers
            logoutButton.replaceWith(logoutButton.cloneNode(true));
            const newLoginButton = document.getElementById("logout-link");

            newLoginButton.addEventListener("click", function () {
                window.location.href = "/tourify/login";
            });
        }
    }
});

const toggleBtn = document.getElementById("chatToggleBtn");
const chatWidget = document.getElementById("chatWidget");

// Pagination variables
let currentPage = 0;
const pageSize = 6;

toggleBtn.addEventListener("click", () => {
    chatWidget.style.display =
        chatWidget.style.display === "none" || chatWidget.style.display === ""
            ? "flex"
            : "none";
});

function sendMessage() {
    const userText = document.getElementById("userInput").value;
    if (!userText) return;
    // Replace with your actual API call
    fetch("https://api.chatbase.co/api/v1/chat", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer YOUR_CHATBASE_API_KEY",
        },
        body: JSON.stringify({
            messages: [{ role: "user", content: userText }],
            chatbotId: "YOUR_CHATBOT_ID",
        }),
    })
        .then((res) => res.json())
        .then((data) => {
            console.log("AI Response:", data);
            // You can update UI here with the response
        })
        .catch((err) => console.error("Error:", err));
}

// Function to load places with pagination
function loadPlaces(page = 0) {
    const container = document.getElementById("placeCardsContainer");
    const paginationContainer = document.getElementById("pagination");

    // Add loading class for smooth transition
    container.classList.add('loading');

    // Show loading state
    container.innerHTML = '<div class="col-12 text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';

    fetch(`http://localhost:8080/tourify/api/place/paged?page=${page}&size=${pageSize}`)
        .then((response) => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then((data) => {
            if (data.code === 1000 && data.result) {
                const pageData = data.result;
                currentPage = pageData.currentPage;

                // Clear existing cards
                container.innerHTML = "";

                // Render places
                if (pageData.content && pageData.content.length > 0) {
                    pageData.content.forEach((place) => {
                        const col = document.createElement("div");
                        const [lat, lng] = place.gpsCoordinates.split(",");
                        const imageUrl = `https://static-maps.yandex.ru/1.x/?ll=${lng.trim()},${lat.trim()}&size=400,400&z=13&l=map&pt=${lng.trim()},${lat.trim()},pm2rdm`;
                        col.className = "col-md-4";

                        col.innerHTML = `
                        <div class="d-flex align-items-center p-3 rounded shadow-sm tour-card-hover">
                            <img
                                src="${imageUrl}"
                                class="rounded me-3"
                                width="64"
                                height="64"
                                alt="${place.placeName}"
                            />
                            <div>
                                <h6 class="mb-1 fw-semibold">${place.placeName}</h6>
                                <small class="text-muted">Rating: ${place.rating}</small>
                            </div>
                        </div>
                    `;
                        container.appendChild(col);
                    });
                } else {
                    container.innerHTML = "<div class='col-12 text-center'><p class='text-muted'>No places found.</p></div>";
                }

                // Render pagination
                renderPagination(pageData);

                // Remove loading class after content is loaded
                setTimeout(() => {
                    container.classList.remove('loading');
                }, 100);
            } else {
                container.innerHTML = "<div class='col-12 text-center'><p class='text-danger'>No places found.</p></div>";
                container.classList.remove('loading');
            }
        })
        .catch((error) => {
            console.error("Fetch error:", error);
            container.innerHTML = "<div class='col-12 text-center'><p class='text-danger'>Failed to load places.</p></div>";
            container.classList.remove('loading');
        });
}

// Function to render pagination
function renderPagination(pageData) {
    const paginationContainer = document.getElementById("pagination");
    paginationContainer.innerHTML = "";

    // Previous button
    const prevLi = document.createElement("li");
    prevLi.className = `page-item ${pageData.hasPrevious ? '' : 'disabled'}`;
    prevLi.innerHTML = `
        <a class="page-link" href="#place" onclick="loadPlaces(${pageData.currentPage - 1})" ${pageData.hasPrevious ? '' : 'tabindex="-1" aria-disabled="true"'}>
            <i class="fas fa-chevron-left"></i>
        </a>
    `;
    paginationContainer.appendChild(prevLi);

    // Page numbers
    const startPage = Math.max(0, pageData.currentPage - 2);
    const endPage = Math.min(pageData.totalPages - 1, pageData.currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const pageLi = document.createElement("li");
        pageLi.className = `page-item ${i === pageData.currentPage ? 'active' : ''}`;
        pageLi.innerHTML = `
            <a class="page-link" href="#place" onclick="loadPlaces(${i})">${i + 1}</a>
        `;
        paginationContainer.appendChild(pageLi);
    }

    // Next button
    const nextLi = document.createElement("li");
    nextLi.className = `page-item ${pageData.hasNext ? '' : 'disabled'}`;
    nextLi.innerHTML = `
        <a class="page-link" href="#place" onclick="loadPlaces(${pageData.currentPage + 1})" ${pageData.hasNext ? '' : 'tabindex="-1" aria-disabled="true"'}>
            <i class="fas fa-chevron-right"></i>
        </a>
    `;
    paginationContainer.appendChild(nextLi);
}

document.addEventListener("DOMContentLoaded", function () {
    // Load initial page
    loadPlaces(0);
});
