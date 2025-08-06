
window.onload = function () {
    const token = getCookie("access_token");
    if (token) {
        // Lưu vào localStorage rồi xóa cookie
        localStorage.setItem("accessToken", token);
        deleteCookie("access_token");
    }
};

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

function deleteCookie(name) {
    document.cookie = `${name}=; Max-Age=0; path=/;`;
}

document.addEventListener("DOMContentLoaded", function () {
    // Xử lý hiển thị login/logout dropdown
    const accessToken = localStorage.getItem("accessToken");
    const dropdownContainer = document.querySelector(".dropdown");
    const logoutButton = document.getElementById("logout-link");

    if (accessToken) {
        dropdownContainer && (dropdownContainer.style.display = "block");
        if (logoutButton) {
            logoutButton.innerHTML = '<i class="fas fa-sign-out-alt me-1"></i>Logout';
            logoutButton.replaceWith(logoutButton.cloneNode(true));
            const btn = document.getElementById("logout-link");
            btn.addEventListener("click", async (e) => {
                e.preventDefault();
                try {
                    await fetch("/tourify/api/auth/logout", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": `Bearer ${accessToken}`
                        }
                    });
                } catch (err) {
                    console.error("Logout API error:", err);
                }
                localStorage.removeItem("accessToken");
                window.location.href = "./login";
            });
        }
    } else {
        dropdownContainer && (dropdownContainer.style.display = "none");
        if (logoutButton) {
            logoutButton.innerHTML = '<i class="fas fa-sign-in-alt me-1"></i>Login';
            logoutButton.replaceWith(logoutButton.cloneNode(true));
            document.getElementById("logout-link")
                .addEventListener("click", () => window.location.href = "/tourify/login");
        }
    }
});


// Pagination state
let currentPage = 0;
const pageSize = 6;

// Load places with clickable cards that auto-fill & submit search
function loadPlaces(page = 0) {
    const container = document.getElementById("placeCardsContainer");
    container.classList.add("loading");
    container.innerHTML = `
        <div class="col-12 text-center">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>`;

    fetch(`/tourify/api/place/paged?page=${page}&size=${pageSize}`)
        .then(res => {
            if (!res.ok) throw new Error("Network response was not ok");
            return res.json();
        })
        .then(data => {
            const pageData = data.code === 1000 && data.result;
            currentPage = pageData.currentPage;
            container.innerHTML = "";

            if (pageData.content && pageData.content.length > 0) {
                pageData.content.forEach(place => {
                    const col = document.createElement("div");
                    const [lat, lng] = place.gpsCoordinates.split(",");
                    const imageUrl = `https://static-maps.yandex.ru/1.x/?ll=${lng.trim()},${lat.trim()}&size=400,400&z=13&l=map&pt=${lng.trim()},${lat.trim()},pm2rdm`;

                    col.className = "col-md-4";
                    col.innerHTML = `
                      <div class="place-card d-flex align-items-center p-3 rounded shadow-sm tour-card-hover"
                           data-place="${place.placeName}">
                        <img src="${imageUrl}"
                             class="rounded me-3"
                             width="64" height="64"
                             alt="${place.placeName}" />
                        <div>
                          <h6 class="mb-1 fw-semibold">${place.placeName}</h6>
                          <small class="text-muted">Rating: ${place.rating}</small>
                        </div>
                      </div>`;

                    container.appendChild(col);

                    // Khi click card, auto-fill "Where" và submit form
                    const card = col.querySelector(".place-card");
                    card.addEventListener("click", () => {
                        const searchForm = document.querySelector(".booking-form form");
                        const whereInput = searchForm.querySelector('input[name="placeName"]');
                        whereInput.value = place.placeName;
                        searchForm.submit();
                    });
                });
            } else {
                container.innerHTML = `
                    <div class="col-12 text-center">
                      <p class="text-muted">No places found.</p>
                    </div>`;
            }

            renderPagination(pageData);
            setTimeout(() => container.classList.remove("loading"), 100);
        })
        .catch(err => {
            console.error("Fetch error:", err);
            container.innerHTML = `
                <div class="col-12 text-center">
                  <p class="text-danger">Failed to load places.</p>
                </div>`;
            container.classList.remove("loading");
        });
}

// Render pagination with chevron arrows
function renderPagination(pageData) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    // Prev
    const prevLi = document.createElement("li");
    prevLi.className = `page-item ${pageData.hasPrevious ? "" : "disabled"}`;
    prevLi.innerHTML = `
      <a class="page-link" href="#place"
         onclick="loadPlaces(${pageData.currentPage - 1})"
         ${pageData.hasPrevious ? "" : 'tabindex="-1" aria-disabled="true"'}>
        <i class="fas fa-chevron-left"></i>
      </a>`;
    pagination.appendChild(prevLi);

    // Pages
    const startPage = Math.max(0, pageData.currentPage - 2);
    const endPage = Math.min(pageData.totalPages - 1, pageData.currentPage + 2);
    for (let i = startPage; i <= endPage; i++) {
        const li = document.createElement("li");
        li.className = `page-item ${i === pageData.currentPage ? "active" : ""}`;
        li.innerHTML = `
          <a class="page-link" href="#place"
             onclick="loadPlaces(${i})">${i + 1}</a>`;
        pagination.appendChild(li);
    }

    // Next
    const nextLi = document.createElement("li");
    nextLi.className = `page-item ${pageData.hasNext ? "" : "disabled"}`;
    nextLi.innerHTML = `
      <a class="page-link" href="#place"
         onclick="loadPlaces(${pageData.currentPage + 1})"
         ${pageData.hasNext ? "" : 'tabindex="-1" aria-disabled="true"'}>
        <i class="fas fa-chevron-right"></i>
      </a>`;
    pagination.appendChild(nextLi);
}

// Khởi tạo lần đầu
document.addEventListener("DOMContentLoaded", () => {
    loadPlaces(0);
});



document.addEventListener("DOMContentLoaded", function () {
    let recommendTours = [];
    const PAGE_SIZE = 4;
    let currentPage = 1;

    loadRecommendedTours();

    function loadRecommendedTours() {
        const token = localStorage.getItem('accessToken');
        if (!token) return;

        const userId = parseJwt(token)?.userId;
        if (!userId) return;

        fetch(`/tourify/api/tours/recommend?userId=${userId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.result && data.result.length > 0) {
                    recommendTours = data.result;
                    currentPage = 1;
                    renderRecommendedTours();
                    document.getElementById('recommendSection').style.display = '';
                }
            })
            .catch(err => console.error('Error loading recommended tours:', err));
    }

    function renderRecommendedTours() {
        const container = document.getElementById('recommendToursContainer');
        container.innerHTML = '';

        // Paging logic
        const totalTours = recommendTours.length;
        const totalPages = Math.ceil(totalTours / PAGE_SIZE);
        const start = (currentPage - 1) * PAGE_SIZE;
        const end = Math.min(start + PAGE_SIZE, totalTours);
        const toursPage = recommendTours.slice(start, end);

        toursPage.forEach(tour => {
            const card = document.createElement('div');
            card.className = 'col-md-3';
            card.innerHTML = `
                <div class="card h-100 shadow">
                    <img src="${tour.imageUrl || 'https://via.placeholder.com/400x220?text=Tour+Image'}" class="card-img-top" style="height: 180px; object-fit: cover;">
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title">${tour.tourName}</h5>
                        <p class="card-text text-muted mb-2">${tour.placeName || ''}</p>
                        <div class="mt-auto d-flex justify-content-between align-items-center">
                            <span class="fw-bold text-primary">${tour.price ? tour.price.toLocaleString() + ' ₫' : ''}</span>
                            <a href="/tourify/tourDetail?id=${tour.tourId}" class="btn btn-outline-primary btn-sm">View</a>
                        </div>
                    </div>
                </div>
            `;
            container.appendChild(card);
        });

        renderRecommendPagination(totalPages);
    }

    function renderRecommendPagination(totalPages) {
        const pag = document.getElementById('recommendPagination');
        pag.innerHTML = '';
        if (totalPages <= 1) return; // Không cần phân trang nếu chỉ 1 trang

        // Prev button
        pag.innerHTML += `
          <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" aria-label="Previous" data-page="${currentPage - 1}">&lt;</a>
          </li>
        `;
        // Page numbers
        for (let i = 1; i <= totalPages; ++i) {
            pag.innerHTML += `
              <li class="page-item ${currentPage === i ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i}</a>
              </li>
            `;
        }
        // Next button
        pag.innerHTML += `
          <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link" href="#" aria-label="Next" data-page="${currentPage + 1}">&gt;</a>
          </li>
        `;

        // Add click events
        pag.querySelectorAll('.page-link').forEach(link => {
            link.addEventListener('click', function (e) {
                e.preventDefault();
                const page = Number(this.getAttribute('data-page'));
                if (!isNaN(page) && page !== currentPage && page >= 1 && page <= totalPages) {
                    currentPage = page;
                    renderRecommendedTours();
                }
            });
        });
    }

    function parseJwt(token) {
        if (!token) return null;
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    }
});

