document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");

    if (tourId) {
        // Lấy danh sách ID đã xem từ localStorage (nếu có)
        let viewedTourIds = JSON.parse(localStorage.getItem("viewedTourIds")) || [];

        // Kiểm tra tourId chưa tồn tại trong danh sách
        if (!viewedTourIds.includes(tourId)) {
            viewedTourIds.push(tourId); // Thêm tourId mới vào cuối mảng
            localStorage.setItem("viewedTourIds", JSON.stringify(viewedTourIds));
        }
    }

    // Sử dụng class hoặc id phù hợp, chỉ nên 1 thẻ tourTitle trên trang
    const titleEl = document.getElementById("tourTitle"); // hoặc getElementById("tourTitle")
    const priceEl = document.querySelector(".tour-price");
    const descEl = document.querySelector(".tour-desc");
    const placeEl = document.querySelector(".tour-place");
    const durationEl = document.querySelector(".tour-duration");
    const categoryEl = document.querySelector(".tour-category");
    const thumbnailEl = document.querySelector(".thumbnail");
    const breadcrumbTitleEl = document.getElementById("breadcrumbTourTitle");
    const breadcrumbPlace = document.getElementById("breadcrumbPlace");
    const favBtn = document.querySelector('.btn-favorite');
    const favIcon = favBtn ? favBtn.querySelector('i') : null;
    let isFavorite = false;
    const token = localStorage.getItem('accessToken');

    if (!tourId) {
        if (titleEl) titleEl.textContent = "Tour Not Found";
        return;
    }

    function setHeart(active) {
        if (!favBtn || !favIcon) return;
        if (active) {
            favBtn.classList.add('active');
            favIcon.className = 'bi bi-heart-fill text-danger';
        } else {
            favBtn.classList.remove('active');
            favIcon.className = 'bi bi-heart';
        }
    }

    // Check favorite status on load
    if (token && tourId) {
        fetch('/tourify/api/user/favorites', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        })
            .then(res => res.json())
            .then(data => {
                if (data.code === 1000 && Array.isArray(data.result)) {
                    isFavorite = data.result.some(t => String(t.tourId) === String(tourId));
                    setHeart(isFavorite);
                }
            });
    }

    fetch(`/tourify/api/tours/${tourId}`)
        .then(res => res.json())
        .then(data => {
            console.log("Tour detail API result:", data); // DEBUG
            // Đảm bảo data.code và data.result tồn tại
            if (data.code === 1000 && data.result) {
                const tour = data.result;
                let link_booking = document.getElementById("link-booking");
                if (link_booking) {
                    link_booking.href = "/tourify/tour/booking?id=" + tourId;
                }
                if (titleEl) titleEl.textContent = tour.tourName || "No Name";
                if (categoryEl) categoryEl.textContent = tour.categoryName || "";
                if (priceEl) priceEl.textContent = tour.price ? tour.price.toLocaleString() + " VND" : "";
                if (descEl) descEl.textContent = tour.description || "";
                if (placeEl) placeEl.textContent = tour.placeName || "";
                if (breadcrumbTitleEl) breadcrumbTitleEl.textContent = tour.tourName || "No Name";
                if (breadcrumbPlace) breadcrumbPlace.textContent = tour.placeName || "";
                if (durationEl) durationEl.textContent = tour.duration ? `${tour.duration} days` : "";
                if (thumbnailEl) thumbnailEl.src = tour.thumbnail || "default.jpg";
                document.querySelectorAll(".thumbnail").forEach(el => {
                    el.src = tour.thumbnail || "default.jpg";
                });
            } else {
                if (titleEl) titleEl.textContent = "Tour Not Found";
            }
        })
        .catch(err => {
            if (titleEl) titleEl.textContent = "Tour Not Found";
            console.error(err);
        });

    // Remove Toastify and implement custom showToast
    function showToast(message, type = 'success') {
        // Remove any existing toast
        const oldToast = document.getElementById('custom-toast');
        if (oldToast) oldToast.remove();

        // Create toast container
        const toast = document.createElement('div');
        toast.id = 'custom-toast';
        toast.innerText = message;
        toast.style.position = 'fixed';
        toast.style.top = '32px';
        toast.style.right = '32px';
        toast.style.zIndex = '9999';
        toast.style.padding = '16px 32px';
        toast.style.borderRadius = '8px';
        toast.style.fontWeight = 'bold';
        toast.style.fontSize = '1rem';
        toast.style.color = '#fff';
        toast.style.boxShadow = '0 2px 16px rgba(0,0,0,0.10)';
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        toast.style.cursor = 'pointer';
        toast.style.background = type === 'success'
            ? 'linear-gradient(90deg, #43e97b 0%, #38f9d7 100%)'
            : '#ff4d4f';
        document.body.appendChild(toast);
        setTimeout(() => { toast.style.opacity = '1'; }, 10);
        // Auto remove after 2.5s or on click
        toast.addEventListener('click', () => toast.remove());
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 2500);
    }

    // Heart click event
    if (favBtn) {
        favBtn.addEventListener('click', function () {
            if (!token) {
                showToast("Please login first", 'error');
                return;
            }
            favBtn.disabled = true;
            if (isFavorite) {
                // Remove favorite
                fetch(`/tourify/api/user/favorites/${tourId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                })
                    .then(res => res.json())
                    .then(data => {
                        favBtn.disabled = false;
                        if (data.code === 1000) {
                            isFavorite = false;
                            setHeart(false);
                            showToast("Removed from favorites", 'success');
                        } else {
                            showToast("Failed to remove favorite", 'error');
                        }
                    })
                    .catch(() => {
                        favBtn.disabled = false;
                        showToast("Network error", 'error');
                    });
            } else {
                // Add favorite
                fetch(`/tourify/api/user/favorites/${tourId}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                })
                    .then(res => res.json())
                    .then(data => {
                        favBtn.disabled = false;
                        if (data.code === 1000) {
                            isFavorite = true;
                            setHeart(true);
                            showToast("Added to favorites", 'success');
                        } else {
                            showToast("Failed to add favorite", 'error');
                        }
                    })
                    .catch(() => {
                        favBtn.disabled = false;
                        showToast("Network error", 'error');
                    });
            }
        });
    }
});

// Hiệu ứng layer, giữ nguyên
window.addEventListener("DOMContentLoaded", () => {
    const layers = document.querySelectorAll(".position-absolute");
    layers.forEach((el, index) => {
        setTimeout(() => {
            el.style.transform = `translateX(${index * 10}px)`;
        }, index * 80);
    });
});

// Tab step chuyển tab, giữ nguyên
document.querySelectorAll(".tour-step").forEach((step) => {
    step.addEventListener("click", () => {
        document
            .querySelectorAll(".tour-step")
            .forEach((s) => s.classList.remove("active"));
        step.classList.add("active");
        // Thêm logic scroll/hiện section tương ứng
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");
    const token = localStorage.getItem('accessToken');
    const BASE_URL = "/tourify/api/feedbacks";

    // Parse JWT lấy userId và role
    function parseJwt(token) {
        if (!token) return {};
        try {
            return JSON.parse(atob(token.split('.')[1]));
        } catch {
            return {};
        }
    }
    const userInfo = parseJwt(token);
    const userId = userInfo.userId || "";
    const userRole = (userInfo.role || "").toUpperCase();

    const pageSize = 2;
    let currentPage = 1;
    let totalPages = 1;
    let feedbackList = [];

    // Helper render
    function renderStars(rating) {
        rating = Math.round(rating || 5);
        return "★".repeat(rating) + "<span class='text-muted'>" + "★".repeat(5 - rating) + "</span>";
    }
    function timeAgo(isoDate) {
        if (!isoDate) return "";
        const date = new Date(isoDate);
        const now = new Date();
        const diff = Math.floor((now - date) / 60000);
        if (diff < 1) return "just now";
        if (diff < 60) return `${diff} minutes ago`;
        const diffH = Math.floor(diff / 60);
        if (diffH < 24) return `${diffH} hours ago`;
        const diffD = Math.floor(diffH / 24);
        return `${diffD} days ago`;
    }
    function roleBadge(role) {
        if (!role) return `<span class="badge bg-secondary ms-2 role-badge">User</span>`;
        const r = role.toUpperCase();
        if (r === "ADMIN") return `<span class="badge bg-danger ms-2 role-badge">Admin</span>`;
        if (r === "SUB_COMPANY") return `<span class="badge bg-info ms-2 role-badge">Tour-Company</span>`;
        return `<span class="badge bg-secondary ms-2 role-badge">User</span>`;
    }
    // Điều kiện hiển thị status:
    // - ADMIN/SUB_COMPANY: thấy mọi status (hiện luôn badge)
    // - USER: chỉ thấy Approved (và có thể hiện nhỏ hoặc không hiện badge)
    function renderStatusBadge(status) {
        if (!status) return '';
        const st = status.toString().trim().toUpperCase();
        if (userRole === "ADMIN" || userRole === "SUB_COMPANY") {
            if (st === "APPROVED")
                return `<span class="badge bg-success fw-bold rounded-pill d-inline-flex align-items-center px-3" style="font-size:1rem">
                <i class="bi bi-check-circle me-1"></i> Approved
            </span>`;
            if (st === "PENDING")
                return `<span class="badge bg-warning text-dark fw-bold rounded-pill d-inline-flex align-items-center px-3" style="font-size:1rem">
                <i class="bi bi-hourglass-split me-1"></i> Pending
            </span>`;
            if (st === "REJECTED")
                return `<span class="badge bg-danger fw-bold rounded-pill d-inline-flex align-items-center px-3" style="font-size:1rem">
                <i class="bi bi-x-circle me-1"></i> Rejected
            </span>`;
            return `<span class="badge bg-secondary rounded-pill d-inline-flex align-items-center px-3">${st}</span>`;
        } else {
            // User thường: chỉ hiện Approved hoặc không hiện
            if (st === "APPROVED")
                return `<span class="badge bg-success fw-bold rounded-pill d-inline-flex align-items-center px-3" style="font-size:1rem">
                <i class="bi bi-check-circle me-1"></i> Approved
            </span>`;
            return "";
        }
    }

    // 1. Feedback mới nhất
    fetch(`${BASE_URL}/${tourId}/latest`, {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
        .then(res => res.ok ? res.json() : null)
        .then(feedback => {
            if (!feedback) throw 0;
            document.getElementById('feedbackUserName').textContent = feedback.userFullName ?? 'User';
            document.getElementById('feedbackAvatar').src = feedback.avatar || "/img/default-avatar.png";
            document.getElementById('feedbackUserInfo').innerHTML = `${roleBadge(feedback.role)} · ${timeAgo(feedback.createdAt)}`;
            document.getElementById('feedbackContent').textContent = `"${feedback.content}"`;
            document.getElementById('feedbackRating').innerHTML = renderStars(feedback.rating || 5);
        })
        .catch(() => {
            document.getElementById('feedbackUserName').textContent = "No feedback available";
            document.getElementById('feedbackAvatar').src = "/img/default-avatar.png";
            document.getElementById('feedbackUserInfo').innerHTML = "";
            document.getElementById('feedbackContent').textContent = "";
            document.getElementById('feedbackRating').innerHTML = "";
        });

    // 2. Phân trang & render feedback
    function renderPagination() {
        const pageNumbersContainer = document.getElementById('pageNumbersContainer');
        pageNumbersContainer.innerHTML = "";
        const maxButtons = 5;
        let startPage = Math.max(1, currentPage - Math.floor(maxButtons / 2));
        let endPage = startPage + maxButtons - 1;
        if (endPage > totalPages) {
            endPage = totalPages;
            startPage = Math.max(1, endPage - maxButtons + 1);
        }
        for (let i = startPage; i <= endPage; i++) {
            const btn = document.createElement('button');
            btn.className = 'btn btn-success btn-sm';
            btn.textContent = i;
            if (i === currentPage) btn.classList.add('active');
            btn.addEventListener('click', () => loadFeedbackPage(i));
            pageNumbersContainer.appendChild(btn);
        }
        document.getElementById('prevPageBtn').disabled = currentPage === 1;
        document.getElementById('nextPageBtn').disabled = currentPage === totalPages || totalPages === 0;
    }

    function loadFeedbackPage(page) {
        if (!feedbackList || feedbackList.length === 0) {
            document.getElementById('feedbackListBody').innerHTML = "<div class='text-center text-muted'>This tour has no feedback.</div>";
            document.getElementById('feedbackCount').textContent = "0";
            renderPagination();
            return;
        }
        totalPages = Math.ceil(feedbackList.length / pageSize);
        currentPage = page;
        const startIndex = (currentPage - 1) * pageSize;
        const endIndex = Math.min(startIndex + pageSize, feedbackList.length);
        const currentPageFeedbacks = feedbackList.slice(startIndex, endIndex);

        document.getElementById('feedbackCount').textContent = feedbackList.length > 99 ? "99+" : feedbackList.length;

        document.getElementById('feedbackListBody').innerHTML = currentPageFeedbacks.map(fb => `
            <div class="feedback-item mb-4 p-3 rounded shadow-sm border bg-white">
                <div class="d-flex align-items-center mb-2 gap-3">
                    <img src="${fb.avatar || '/img/default-avatar.png'}" alt="Avatar" width="48" height="48" class="rounded-circle shadow-sm" />
                    <div class="flex-grow-1">
                        <div class="d-flex align-items-center gap-2">
                            <strong class="text-dark">${fb.userFullName}</strong>
                            ${roleBadge(fb.role)}
                            ${renderStatusBadge(fb.status)}
                        </div>
                        <small class="text-muted">${timeAgo(fb.createdAt)}</small>
                        <div class="mt-1 text-warning fs-6">${renderStars(fb.rating)}</div>
                    </div>
                </div>
                <p class="mt-3 fst-italic text-secondary mb-0" style="font-size: 1rem;">"${fb.content}"</p>
            </div>
        `).join('');

        renderPagination();
    }

    // Lấy feedbacks
    const headers = {};
    if (token && token.trim() !== '') {
        headers['Authorization'] = 'Bearer ' + token;
    }
    let apiUrl = `${BASE_URL}/${tourId}`;
    if ((userRole === "SUB_COMPANY" || userRole === "ADMIN") && userId) {
        apiUrl += `?userId=${userId}`;
    }

    fetch(apiUrl, { headers })
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            feedbackList = Array.isArray(data) ? data : (data.feedbacks || []);
            // Lấy feedback mới nhất riêng biệt
            fetch(`${BASE_URL}/${tourId}/latest`, { headers })
                .then(res => res.ok ? res.json() : null)
                .then(latestFeedback => {
                    if (latestFeedback && !feedbackList.some(fb => fb.feedbackId === latestFeedback.feedbackId)) {
                        feedbackList.unshift(latestFeedback);
                    }
                })
                .finally(() => {
                    totalPages = Math.ceil(feedbackList.length / pageSize);
                    currentPage = 1;
                    loadFeedbackPage(currentPage);
                });
        })
        .catch(() => {
            feedbackList = [];
            totalPages = 1;
            currentPage = 1;
            document.getElementById('feedbackListBody').innerHTML = "<div class='text-center text-muted'>This tour has no feedback.</div>";
            document.getElementById('feedbackCount').textContent = "0";
            renderPagination();
        });

    // Sự kiện Prev / Next
    document.getElementById('prevPageBtn').addEventListener('click', () => {
        if (currentPage > 1) loadFeedbackPage(currentPage - 1);
    });
    document.getElementById('nextPageBtn').addEventListener('click', () => {
        if (currentPage < totalPages) loadFeedbackPage(currentPage + 1);
    });
});


