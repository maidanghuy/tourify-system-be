
    // ==== 1. Đọc tourId từ URL ====
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");
    const token = localStorage.getItem('accessToken');

    // --- Lấy user info từ token ---
    function parseJwt(token) {
        if (!token) return {};
        try {
            return JSON.parse(atob(token.split('.')[1]));
        } catch (e) {
            return {};
        }
    }
    const jwtPayload = parseJwt(token);
    const userId = jwtPayload.userId;
    const userRole = jwtPayload.role;

    // ==== 2. Gọi API lấy thông tin tour & render ====
    fetch(`/tourify/api/tours/${tourId}`, {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(res => res.json())
      .then(data => {
        if (data.code !== 1000) throw Error('Not found');
        const tour = data.result;
        document.getElementById('tourTitle').textContent = tour.tourName || '';
        document.getElementById('breadcrumbTourTitle').textContent = tour.tourName || '';
        document.querySelector('.tour-price').textContent = tour.price ? tour.price.toLocaleString() + " ₫" : "";
        document.querySelector('.tour-duration').textContent = tour.duration ? tour.duration + " days" : "";
        document.querySelector('.tour-place').textContent = tour.placeName || "";
        document.querySelector('.tour-category').textContent = tour.categoryName || "";
        document.querySelector('.tour-main-img').src = tour.thumbnail || "https://via.placeholder.com/800x320?text=No+Image";
        document.querySelectorAll('.tour-thumb').forEach(el => el.src = tour.thumbnail || "https://via.placeholder.com/320x240?text=No+Image");
        document.getElementById('tourDesc').textContent = tour.description || '';
        document.querySelector('.tour-start').textContent = tour.startDate ? new Date(tour.startDate).toLocaleDateString() : "N/A";
        // Status badge
        const badge = document.getElementById('tourStatusBadge');
        if (tour.status === "ACTIVE") {
          badge.className = "badge status-badge bg-success";
          badge.textContent = "Active";
        } else {
          badge.className = "badge status-badge bg-secondary";
          badge.textContent = tour.status || "Draft";
        }
            // Ẩn/hiện nút theo status
            if (tour.status === "ACTIVE") {
                document.getElementById('disableTourBtn').classList.remove('d-none');
                document.getElementById('enableTourBtn').classList.add('d-none');
            } else if (tour.status === "INACTIVE") {
                document.getElementById('enableTourBtn').classList.remove('d-none');
                document.getElementById('disableTourBtn').classList.add('d-none');
            } else { // Nếu là DRAFT hoặc trạng thái khác, ẩn cả hai nút
                document.getElementById('enableTourBtn').classList.add('d-none');
                document.getElementById('disableTourBtn').classList.add('d-none');
            }

      });

    // ==== 3. Gọi API danh sách booking ====
    fetch(`/tourify/api/tours/${tourId}/bookings`, {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
      .then(res => res.json())
      .then(data => {
        if (data.code === 1000 && data.result) {
          const tbody = document.querySelector('#bookingTable tbody');
          tbody.innerHTML = "";
          document.getElementById('tourBookings').textContent = data.result.length;
          data.result.forEach((bk, idx) => {
            tbody.innerHTML += `
              <tr>
                <td>${idx + 1}</td>
                <td>${bk.customerName}</td>
                <td>${new Date(bk.bookingDate).toLocaleDateString()}</td>
                <td>${bk.status}</td>
                <td>${bk.contact}</td>
              </tr>
            `;
          });
        }
      });

    let feedbackApi = `/tourify/api/feedbacks/${tourId}`;
    if (userRole === "SUB_COMPANY" && userId) {
        feedbackApi += `?userId=${userId}`;
    }

    const FEEDBACKS_PER_PAGE = 2;
    let feedbacks = [];
    let currentPage = 1;

    // Fetch và khởi tạo phân trang
    fetch(feedbackApi)
        .then(res => res.json())
        .then(data => {
            feedbacks = Array.isArray(data) ? data : data.feedbacks;
            renderFeedbackList(currentPage);
        });

    // Render danh sách feedback theo trang
    function renderFeedbackList(page) {
        const start = (page - 1) * FEEDBACKS_PER_PAGE;
        const end = start + FEEDBACKS_PER_PAGE;
        const pageFeedbacks = feedbacks.slice(start, end);

        const listBody = document.getElementById('feedbackListBody');
        if (pageFeedbacks.length === 0) {
            listBody.innerHTML = `<div class="text-muted text-center py-4"><i class="bi bi-chat-left-text fs-2"></i><br>No feedback yet.</div>`;
        } else {
            listBody.innerHTML = pageFeedbacks.map(fb => {
                // Lấy đúng role, không phụ thuộc viết hoa/thường, ưu tiên userRole, fallback role
                const roleValue = fb.userRole || fb.role || '';
                return `
                <div class="mb-3 p-3 border rounded-3 shadow-sm position-relative bg-light">
                  <div class="d-flex align-items-center gap-3 mb-2">
                    <img src="${fb.avatar || '/img/default-avatar.png'}" class="rounded-circle border" width="48" height="48"/>
                    <div>
                      <b>${fb.userFullName}</b>
                      ${getRoleBadge(roleValue)}
                      <div class="small text-muted">${formatDateTime(fb.createdAt)}</div>
                    </div>
                    <span class="badge bg-light text-dark ms-2 px-2 py-1 d-flex align-items-center">
                      <i class="bi bi-star-fill text-warning me-1"></i> ${fb.rating}
                    </span>
                    <span class="ms-2">${getStatusBadge(fb.status)}</span>
                    <div class="ms-auto d-flex gap-1">
                      <button class="btn btn-sm btn-outline-warning rounded-pill" onclick="editFeedback('${fb.feedbackId}')">
                        <i class="bi bi-pencil"></i> Edit
                      </button>
                      <button class="btn btn-sm btn-outline-danger rounded-pill" onclick="deleteFeedback('${fb.feedbackId}')">
                        <i class="bi bi-trash"></i> Delete
                      </button>
                    </div>
                  </div>
                  <p class="fst-italic mb-0 ps-5">${fb.content}</p>
                </div>
            `;
            }).join('');
        }
        renderPagination(page);
    }

    // Phân trang Bootstrap 5 hiện đại
    function renderPagination(page) {
        const totalPages = Math.ceil(feedbacks.length / FEEDBACKS_PER_PAGE);
        const container = document.getElementById('feedbackPagination');
        if (totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        let html = `<nav aria-label="Page navigation"><ul class="pagination pagination-sm mb-0">`;

        html += `<li class="page-item${page === 1 ? ' disabled' : ''}">
        <a class="page-link" href="javascript:void(0)" onclick="gotoFeedbackPage(${page - 1})" aria-label="Previous"><span aria-hidden="true">&laquo;</span></a>
    </li>`;

        for (let i = 1; i <= totalPages; i++) {
            html += `<li class="page-item${page === i ? ' active' : ''}">
            <a class="page-link" href="javascript:void(0)" onclick="gotoFeedbackPage(${i})">${i}</a>
        </li>`;
        }

        html += `<li class="page-item${page === totalPages ? ' disabled' : ''}">
        <a class="page-link" href="javascript:void(0)" onclick="gotoFeedbackPage(${page + 1})" aria-label="Next"><span aria-hidden="true">&raquo;</span></a>
    </li>`;

        html += `</ul></nav>`;
        container.innerHTML = html;
    }

    // Điều khiển chuyển trang
    window.gotoFeedbackPage = function(page) {
        currentPage = page;
        renderFeedbackList(page);
    };

    // ================== Helpers ================
    function formatDateTime(str) {
        const d = new Date(str);
        if (isNaN(d)) return '';
        return d.toLocaleTimeString('vi-VN', {hour: '2-digit', minute:'2-digit', second:'2-digit'})
            + ' ' + d.toLocaleDateString('vi-VN');
    }

    function getStatusBadge(statusRaw) {
        const status = (statusRaw ?? '').toString().trim().toUpperCase();
        switch (status) {
            case "APPROVED":
                return `<span class="badge bg-success d-flex align-items-center gap-1"><i class="bi bi-check-circle"></i> Approved</span>`;
            case "REJECTED":
                return `<span class="badge bg-danger d-flex align-items-center gap-1"><i class="bi bi-x-circle"></i> Rejected</span>`;
            case "PENDING":
                return `<span class="badge bg-warning text-dark d-flex align-items-center gap-1"><i class="bi bi-hourglass-split"></i> Pending</span>`;
            default:
                return `<span class="badge bg-secondary">Unknown</span>`;
        }
    }

    function getRoleBadge(roleRaw) {
        const role = (roleRaw ?? '').toString().trim().toUpperCase();
        switch (role) {
            case "ADMIN":
                return `<span class="badge bg-danger ms-2">Admin</span>`;
            case "SUB_COMPANY":
                return `<span class="badge bg-primary ms-2">Sub-Company</span>`;
            case "CUSTOMER":
                return `<span class="badge bg-info text-dark ms-2">Customer</span>`;
            default:
                return `<span class="badge bg-secondary ms-2">User</span>`;
        }
    }

    function editFeedback(feedbackId) {
        alert("Edit feedback: " + feedbackId);
    }
    function deleteFeedback(feedbackId) {
        if (confirm("Are you sure to delete this feedback?")) {
            alert("Deleted feedback: " + feedbackId);
        }
    }




    // ==== 5. Sự kiện Edit/Disable ====
    document.getElementById('disableTourBtn').onclick = function() {
      if (!confirm("Are you sure you want to disable this tour?")) return;

      fetch(`/tourify/api/tours/${tourId}/disable`, {
        method: "PUT",
        headers: {
          'Authorization': 'Bearer ' + token,
          'Content-Type': 'application/json'
        }
      })
      .then(res => res.json())
      .then(data => {
        if (data.code === 1000) {
          alert('Tour has been disabled!');
          location.reload();
        } else {
          alert(data.message || "Disable failed!");
        }
      })
      .catch(() => {
        alert("Network error!");
      });
    };

// Enable tour
document.getElementById('enableTourBtn').onclick = function() {
    if (!confirm("Are you sure you want to enable this tour?")) return;

    fetch(`/tourify/api/tours/${tourId}/enable`, {
        method: "PUT",
        headers: {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.code === 1000) {
            alert('Tour has been enabled!');
            location.reload();
        } else {
            alert(data.message || "Enable failed!");
        }
    })
    .catch(() => {
        alert("Network error!");
    });
};
// === Load Category Dropdown khi vào trang hoặc khi cần, trả về Promise ===
function loadCategoriesDropdown(selectedCategoryId) {
    return fetch("/tourify/api/categories")
        .then(res => res.json())
        .then(data => {
            // Nếu API trả về object { result: [...] }
            const categories = Array.isArray(data.result) ? data.result : (Array.isArray(data) ? data : []);
            const catSelect = document.getElementById('editCategory');
            // Nếu chưa có select, báo lỗi dev
            if (!catSelect) {
                console.error("Không tìm thấy select#editCategory trên DOM!");
                return;
            }
            catSelect.innerHTML = categories.map(c =>
                `<option value="${c.categoryId}">${c.categoryName}</option>`
            ).join('');
            // Nếu có truyền selectedCategoryId thì chọn đúng option
            if (selectedCategoryId) {
                catSelect.value = selectedCategoryId;
            }
        });
}

// --- Khi bấm Edit, mở modal và fill data ---
document.getElementById("editTourBtn").addEventListener("click", function () {
    fetch(`/tourify/api/tours/${tourId}`, {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    })
    .then(res => res.json())
    .then(data => {
        if (data.code === 1000) {
            const t = data.result;
            document.getElementById('editTourName').value = t.tourName || "";
            document.getElementById('editPrice').value = t.price || "";
            document.getElementById('editDescription').value = t.description || "";
            document.getElementById('editStartDate').value = t.startDate ? t.startDate.slice(0, 16) : "";
            document.getElementById('editDuration').value = t.duration || "";
            document.getElementById('editStatus').value = t.status || "DRAFT";
            document.getElementById('editMinPeople').value = t.minPeople || "";
            document.getElementById('editMaxPeople').value = t.maxPeople || "";
            document.getElementById('editThumbnail').value = t.thumbnail || "";
            document.getElementById('editPlace').value = t.placeName || "";

            // Lưu ý: categoryId có thể nằm ở t.categoryId hoặc t.category.categoryId
            const selectedCategoryId = t.categoryId || (t.category && t.category.categoryId);

            // Load category xong mới set selected value và show modal
            loadCategoriesDropdown(selectedCategoryId)
                .then(() => {
                    new bootstrap.Modal(document.getElementById('editTourModal')).show();
                });
        }
    });
});

// --- Submit form: Cập nhật tour (KHÔNG gửi place) ---
document.getElementById("editTourForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const req = {
        tourName: document.getElementById('editTourName').value,
        price: document.getElementById('editPrice').value,
        description: document.getElementById('editDescription').value,
        startDate: document.getElementById('editStartDate').value,
        duration: document.getElementById('editDuration').value,
        status: document.getElementById('editStatus').value,
        minPeople: document.getElementById('editMinPeople').value,
        maxPeople: document.getElementById('editMaxPeople').value,
        thumbnail: document.getElementById('editThumbnail').value,
        category: document.getElementById('editCategory').value
        // KHÔNG gửi place!
    };

    fetch(`/tourify/api/tours/${tourId}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify(req)
    })
    .then(res => res.json())
    .then(data => {
        if (data.code === 1000) {
            bootstrap.Modal.getInstance(document.getElementById('editTourModal')).hide();
            showToast("Cập nhật thành công!", "success");
            setTimeout(() => location.reload(), 1500); // Đợi toast rồi reload
        } else {
            showToast("Update thất bại: " + (data.message || ""), "error");
        }
    })
    .catch(() => showToast("Network error", "error"));
});

function showToast(message, type = 'success') {
    const toastEl = document.getElementById('notifyToast');
    const toastMsg = document.getElementById('toastMsg');
    toastMsg.textContent = message;

    // Đổi màu toast dựa vào type
    toastEl.classList.remove('bg-success', 'bg-danger', 'bg-primary');
    if (type === 'success') toastEl.classList.add('bg-success');
    else if (type === 'error') toastEl.classList.add('bg-danger');
    else toastEl.classList.add('bg-primary');

    const toast = new bootstrap.Toast(toastEl);
    toast.show();
}
