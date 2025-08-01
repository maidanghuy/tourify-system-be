document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");

    if (tourId) {
        // Lấy danh sách ID đã xem từ localStorage (nếu có)
        let viewedTourIds = JSON.parse(localStorage.getItem("viewedTourIds")) || [];

        const MAX_VIEWED = 7;

        if (!viewedTourIds.includes(tourId)) {
            viewedTourIds.push(tourId);
            if (viewedTourIds.length > MAX_VIEWED) {
                viewedTourIds.shift();
            }
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
    const bookingEL = document.getElementById('link-booking');

    if (bookingEL && tourId) {
        bookingEL.href = `/tourify/tour/booking?id=${tourId}`;
    }

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
    if (data.code === 1000 && data.result) {
      const tour = data.result;

      // Các phần gán text khác...
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
        document.title = `Tourify - ${tour.placeName} - ${tour.tourName}`;
      // --- XỬ LÝ START DAY & END DAY ---
      const startDayEl = document.querySelector('.start-day');
      const endDayEl = document.querySelector('.end-day');

      if (startDayEl && endDayEl && tour.startDate && tour.duration) {
        const startDateObj = new Date(tour.startDate);
        const duration = tour.duration;

        // Format Start Day
        const startFormatted = startDateObj.toLocaleDateString('en-US', {
          weekday: 'long', month: 'long', day: 'numeric'
        });

        // Tính End Day = startDate + duration - 1 ngày
        const endDateObj = new Date(startDateObj);
        endDateObj.setDate(endDateObj.getDate() + duration - 1);

        const endFormatted = endDateObj.toLocaleDateString('en-US', {
          weekday: 'long', month: 'long', day: 'numeric'
        });

        startDayEl.textContent = startFormatted;
        endDayEl.textContent = endFormatted;
      } else {
        if (startDayEl) startDayEl.textContent = "";
        if (endDayEl) endDayEl.textContent = "";
      }

      // ===============================
      //  Render 4 activities & 4 services
      // ===============================
      const activitiesCol = document.getElementById("activitiesCol");
      const servicesCol = document.getElementById("servicesCol");

      // Helper function
      function renderList(arr, iconClass) {
        if (!arr || arr.length === 0)
          return '<div class="text-muted">No data.</div>';
        return arr.slice(0, 4).map(item => `
          <div class="activity-box d-flex align-items-start mb-3 p-3 rounded-3 shadow-sm bg-white">
            <i class="${iconClass} fs-5 me-3 mt-1"></i>
            <div>
              <div class="fw-semibold">${item.name || ""}</div>
              ${item.description ? `<div class="small text-secondary">${item.description}</div>` : ""}
            </div>
          </div>
        `).join("");
      }

      // Kiểm tra dữ liệu trả về, nếu tên field khác thì sửa lại cho đúng!
      if (activitiesCol) {
        activitiesCol.innerHTML = renderList(tour.activities, "fa-solid fa-circle-check text-success");
      }
      if (servicesCol) {
        servicesCol.innerHTML = renderList(tour.services, "fa-solid fa-star text-warning");
      }
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
document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");

    const btnOpenReportModal = document.getElementById("btnOpenReportModal");
    const btnCancelReport = document.getElementById("btnCancelReport"); // nút hủy report mới thêm
    const btnSubmitReport = document.getElementById("btnSubmitReport");
    const reportTourModal = document.getElementById('reportTourModal');
    const reportForm = document.getElementById("reportTourForm");
    const msgDiv = document.getElementById("reportSuccessMsg");

    // Lấy token từ localStorage
    function getToken() {
        return localStorage.getItem("accessToken") || "";
    }

    // UI: đã report (ẩn nút Report, hiện nút Cancel)
    function setUIReported() {
        if (btnOpenReportModal) btnOpenReportModal.classList.add('d-none');
        if (btnCancelReport) btnCancelReport.classList.remove('d-none');
        if (msgDiv) msgDiv.classList.remove('d-none');
    }

    // UI: chưa report (hiện nút Report, ẩn nút Cancel)
    function setUIUnreported() {
        if (btnOpenReportModal) {
            btnOpenReportModal.classList.remove('d-none');
            btnOpenReportModal.disabled = false;
            btnOpenReportModal.classList.add('btn-outline-danger');
            btnOpenReportModal.classList.remove('btn-success');
            btnOpenReportModal.innerHTML = `<i class="fas fa-flag"></i> Report`;
            btnOpenReportModal.title = "Báo cáo tour vi phạm";
        }
        if (btnCancelReport) btnCancelReport.classList.add('d-none');
        if (msgDiv) msgDiv.classList.add('d-none');
    }

    // UI: không được phép báo cáo (chưa login hoặc lỗi tourId)
    function setUIDisabled(reason) {
        if (btnOpenReportModal) {
            btnOpenReportModal.disabled = true;
            btnOpenReportModal.classList.remove('btn-success');
            btnOpenReportModal.classList.add('btn-outline-danger');
            btnOpenReportModal.innerHTML = `<i class="fas fa-flag"></i> Report`;
            btnOpenReportModal.title = reason || "Bạn cần đăng nhập để báo cáo tour";
            btnOpenReportModal.classList.remove('d-none');
        }
        if (btnCancelReport) btnCancelReport.classList.add('d-none');
        if (msgDiv) msgDiv.classList.add('d-none');
    }

    // Check đã report tour này chưa
    function checkReported() {
        const token = getToken();

        if (!tourId) {
            setUIDisabled("Tour không xác định");
            return;
        }
        if (!token) {
            setUIDisabled("Bạn cần đăng nhập để báo cáo tour");
            return;
        }

        fetch(`/tourify/api/report-tours/check?tourId=${encodeURIComponent(tourId)}`, {
            headers: { "Authorization": "Bearer " + token }
        })
            .then(res => {
                if (!res.ok) throw new Error("API lỗi");
                return res.json();
            })
            .then(alreadyReported => {
                if (alreadyReported === true) {
                    setUIReported();
                } else {
                    setUIUnreported();
                }
            })
            .catch(() => {
                setUIUnreported();
            });
    }

    checkReported();

    // Mở modal báo cáo
    if (btnOpenReportModal) {
        btnOpenReportModal.onclick = function () {
            if (btnOpenReportModal.disabled) return;
            let modal = new bootstrap.Modal(reportTourModal);
            modal.show();
        };
    }

    // Submit report
    if (btnSubmitReport) {
        btnSubmitReport.onclick = function () {
            const reasonCode = document.getElementById("reasonCode").value;
            const description = document.getElementById("description").value.trim();
            const token = getToken();

            if (!token) {
                Swal.fire("Please log in to report!", "", "warning");
                return;
            }
            if (!reasonCode) {
                Swal.fire("Please select a reason!", "", "warning");
                return;
            }

            // Có thể thêm kiểm tra từ cấm nếu muốn

            Swal.fire({
                title: "Are you sure you want to report this tour?",
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: "Yes, Report!",
                cancelButtonText: "Cancel",
                confirmButtonColor: "#d33"
            }).then((result) => {
                if (result.isConfirmed) {
                    fetch(`/tourify/api/report-tours`, {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": "Bearer " + token
                        },
                        body: JSON.stringify({
                            tourId,
                            reasonCode,
                            description
                        })
                    })
                        .then(res => {
                            if (!res.ok) {
                                if (res.status === 409) throw new Error("Bạn đã báo cáo tour này trước đó.");
                                if (res.status === 403) throw new Error("Bạn không có quyền thực hiện.");
                                return res.json().then(data => { throw new Error(data.message || "Failed to send report"); });
                            }
                            return res.json();
                        })
                        .then(() => {
                            Swal.fire("Đã báo cáo!", "Báo cáo của bạn đã được gửi.", "success");
                            bootstrap.Modal.getInstance(reportTourModal).hide();
                            reportForm.reset();
                            checkReported();
                        })
                        .catch(err => {
                            Swal.fire("Không thành công!", err.message, "error");
                            if (err.message.includes("đã báo cáo")) checkReported();
                        });
                }
            });
        }
    }

    // Xử lý nút Hủy báo cáo
    if (btnCancelReport) {
        btnCancelReport.onclick = function () {
            const token = getToken();
            if (!token) {
                Swal.fire("Bạn cần đăng nhập để hủy báo cáo!", "", "warning");
                return;
            }
            Swal.fire({
                title: "Bạn có chắc muốn hủy báo cáo tour này?",
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: "Có, hủy báo cáo",
                cancelButtonText: "Hủy",
                confirmButtonColor: "#d33"
            }).then((result) => {
                if (result.isConfirmed) {
                    fetch(`/tourify/api/report-tours/cancel?tourId=${encodeURIComponent(tourId)}`, {
                        method: "DELETE",
                        headers: {
                            "Authorization": "Bearer " + token
                        }
                    })
                        .then(res => {
                            if (!res.ok) throw new Error("Không thể hủy báo cáo");
                            return res.json().catch(() => ({}));
                        })
                        .then(() => {
                            Swal.fire("Đã hủy báo cáo!", "Bạn có thể báo cáo lại nếu muốn.", "success");
                            checkReported();
                        })
                        .catch(err => {
                            Swal.fire("Lỗi!", err.message || "Không thể hủy báo cáo", "error");
                        });
                }
            });
        }
    }

    // Lắng nghe sự kiện login/logout để cập nhật UI nút report
    window.addEventListener('storage', function(e) {
        if (e.key === "accessToken") {
            setTimeout(checkReported, 100);
        }
    });
});





document.addEventListener("DOMContentLoaded", function() {
    // Lấy section và step
    const infoSection = document.getElementById('info-section');
    const planSection = document.getElementById('plan-section');
    const locationSection = document.getElementById('location-section');
    const stepInfo = document.getElementById('step-info');
    const stepPlan = document.getElementById('step-plan');
    const stepLocation = document.getElementById('step-location');

    // Hàm remove/toggle active class
    function setActiveStep(step) {
        [stepInfo, stepPlan, stepLocation].forEach(s => s.classList.remove('active'));
        if (step) step.classList.add('active');
    }

    // Xử lý scroll để đổi tab active
    window.addEventListener('scroll', function() {
        // Tính vị trí từng section
        const scrollY = window.scrollY || window.pageYOffset;
        const buffer = 100; // (tùy chỉnh nếu có header)
        const infoTop = infoSection.offsetTop - buffer;
        const planTop = planSection.offsetTop - buffer;
        const locationTop = locationSection.offsetTop - buffer;

        // Tìm section đang nhìn thấy
        if (scrollY >= locationTop) {
            setActiveStep(stepLocation);
        } else if (scrollY >= planTop) {
            setActiveStep(stepPlan);
        } else {
            setActiveStep(stepInfo);
        }
    });

    // Xử lý click để scroll (nếu chưa có)
    stepInfo.addEventListener('click', () => {
        infoSection.scrollIntoView({behavior: 'smooth'});
    });
    stepPlan.addEventListener('click', () => {
        planSection.scrollIntoView({behavior: 'smooth'});
    });
    stepLocation.addEventListener('click', () => {
        locationSection.scrollIntoView({behavior: 'smooth'});
    });
});


