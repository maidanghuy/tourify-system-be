
    // ==== 1. Đọc tourId từ URL ====
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");
    const token = localStorage.getItem('accessToken');

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

    // ==== 4. Gọi API danh sách feedback ====
    fetch(`/tourify/api/feedbacks/${tourId}`)
      .then(res => res.json())
      .then(data => {
        if (data.feedbacks && data.feedbacks.length > 0) {
          document.getElementById('feedbackListBody').innerHTML =
            data.feedbacks.map(fb => `
              <div class="mb-3 p-3 border rounded shadow-sm">
                <div class="d-flex align-items-center gap-3 mb-2">
                  <img src="${fb.avatar || '/img/default-avatar.png'}" class="rounded-circle" width="40" height="40"/>
                  <b>${fb.userFullName}</b>
                  <span class="badge bg-light text-dark ms-2">${fb.rating}★</span>
                  <small class="text-muted ms-auto">${(new Date(fb.createdAt)).toLocaleDateString()}</small>
                </div>
                <p class="fst-italic mb-0">${fb.content}</p>
              </div>
            `).join('');
        } else {
          document.getElementById('feedbackListBody').innerHTML = '<div class="text-muted">No feedback yet.</div>';
        }
      });

    // ==== 5. Sự kiện Edit/Disable ====
    document.getElementById('editTourBtn').onclick = function() {
      window.location.href = `/tourify/tourEdit?id=${tourId}`;
    };
    document.getElementById('disableTourBtn').onclick = function() {
      // TODO: Gọi API đổi status, confirm, toast...
      alert('Feature coming soon!');
    };
