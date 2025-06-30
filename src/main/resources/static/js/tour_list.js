// js/tour_list.js
document.addEventListener("DOMContentLoaded", () => {
    const priceInput = document.getElementById("priceRange");
    const priceValue = document.getElementById("priceValue");
    const durationInput = document.getElementById("durationRange");
    const durationValue = document.getElementById("durationValue");
    const ratingButtons = document.querySelectorAll(".rating-btn");
    const companyFilterContainer = document.getElementById("companyFilterContainer");
    const container = document.getElementById("tourResultsContainer");
    const noTourMsg = document.getElementById("noTourMsg");
    const loadingSpinner = document.getElementById("loadingSpinner");
    const pagination = document.getElementById("pagination");
    const paginationWrapper = pagination.closest(".text-center");
    const scrollTarget = document.getElementById("tourListScrollable");

    const chunkSize = 4;
    let allTours = [];
    let filteredTours = [];

    const selectedFilters = {
        price: null,
        duration: parseInt(durationInput.value, 10),
        rating: null,
        companies: []
    };

    function updateDisplayCount() {
        priceValue.textContent = `${parseInt(priceInput.value, 10).toLocaleString()} VND`;
        durationValue.textContent = `${parseInt(durationInput.value, 10)} day(s)`;
    }

    function generateStarRating(rating) {
        if (!rating || isNaN(rating)) return `<span class="text-muted small fst-italic">Unrated</span>`;
        rating = Math.min(Math.round(rating * 2) / 2, 5);
        const full = Math.floor(rating),
            half = rating % 1 !== 0,
            empty = 5 - full - (half ? 1 : 0);
        return (
            '<i class="fas fa-star text-warning"></i>'.repeat(full) +
            (half ? '<i class="fas fa-star-half-alt text-warning"></i>' : '') +
            '<i class="far fa-star text-warning"></i>'.repeat(empty) +
            ` <span class="ms-1 text-muted small">(${rating.toFixed(1)})</span>`
        );
    }

    function renderFilteredTours() {
        const { price, duration, rating, companies } = selectedFilters;
        filteredTours = allTours.filter(tour => (
            (!price || tour.price <= price) &&
            (!duration || tour.duration >= duration) &&
            (!rating || Math.floor(tour.rating) === rating) &&
            (companies.length === 0 || companies.includes(tour.createdByUserName))
        ));

        if (filteredTours.length === 0) {
            showNoTourMessage();
            paginationWrapper.classList.add("d-none");
        } else {
            noTourMsg.classList.add("d-none");
            paginationWrapper.classList.remove("d-none");
            renderPagination(0);
        }
    }

    function renderToursPage(pageIndex) {
        container.innerHTML = "";
        const start = pageIndex * chunkSize;
        const pageData = filteredTours.slice(start, start + chunkSize);

        pageData.forEach(tour => {
            container.insertAdjacentHTML("beforeend", `
    <div class="tour-luxury-card d-flex align-items-stretch mb-4" style="background: linear-gradient(135deg, #e5f9ff 0%, #fffbe7 100%); border-radius: 24px; box-shadow: 0 8px 32px rgba(24,70,130,0.09);">
  <!-- Image + badges -->
  <div class="position-relative tour-img-wrap" style="min-width:220px; max-width:220px; border-radius: 20px 0 0 20px; overflow: hidden;">
    <img src="${tour.thumbnail}" class="w-100 h-100 object-fit-cover" alt="Thumbnail" style="min-height: 200px; object-fit: cover;">
    <!-- Category badge -->
    <span class="badge tour-type-badge position-absolute"
          style="top:16px; left:16px;">
      ${tour.categoryName || "Unknown"}
    </span>
    <!-- Rating badge -->
    <span class="badge tour-rating-badge position-absolute"
          style="top:16px; right:16px;">
      <i class="fas fa-star me-1"></i>
      ${(tour.rating !== undefined && tour.rating !== null) ? tour.rating : "N/A"}
    </span>
  </div>
  <!-- Info -->
  <div class="flex-grow-1 d-flex flex-column p-4" style="background: transparent;">
    <div class="d-flex justify-content-between align-items-start">
      <div>
        <h5 class="fw-bold mb-2" style="font-size: 1.3rem;">${tour.tourName}</h5>
        <div class="text-muted mb-2" style="font-size:1rem;">
          <i class="fas fa-location-dot me-1"></i>${tour.placeName}
          <span class="mx-2">·</span>
          <i class="fas fa-calendar-alt me-1"></i>${tour.duration} days
          <span class="mx-2">·</span>
          <i class="fas fa-users me-1"></i>${tour.touristNumberAssigned?.toLocaleString() || 0} booked
        </div>
      </div>
      <div class="d-flex align-items-center tour-company-badge px-3 py-2 ms-2 rounded-pill"
           tabindex="0"
           style="background: #f3faf5; color: #139169; font-weight: 500; cursor: pointer; transition: background 0.15s, color 0.12s;">
        <i class="fa fa-user-circle me-2" style="font-size: 1.22em;"></i>
        ${tour.createdByUserName || "Unknown"}
      </div>
    </div>
    <div class="mb-3">
      <div class="tour-desc p-2 px-3 rounded-3 bg-white shadow-sm" style="font-size:1rem; color:#566478;">
        ${tour.description || "Discover an amazing journey with Tourify"}
      </div>
    </div>
    <div class="d-flex justify-content-between align-items-center mt-auto">
      <div class="fw-bold" style="font-size:1.5rem; color:#139169;">
        ${tour.price.toLocaleString()} <span class="fs-6 fw-normal" style="color:#a0b0c2;">VND</span>
      </div>
      <div class="d-flex gap-2 tour-actions">
        <button class="action-btn btn-favorite" data-tour-id="${tour.tourId}" title="Add to favorites"><i class="fa fa-heart"></i></button>
        <a href="/tourify/tourDetail?id=${tour.tourId}" class="action-btn" title="View details"><i class="fa fa-eye"></i></a>
        <a href="/tourify/tourBooking?id=${tour.tourId}" class="action-btn" title="Book this tour"><i class="fa fa-plane-departure"></i></a>
      </div>
    </div>
  </div>
</div>

    `);
        });


        scrollTarget.scrollTo({ top: 0, behavior: "smooth" });
    }

    function renderPagination(currentPage) {
        const totalPages = Math.ceil(filteredTours.length / chunkSize);
        pagination.innerHTML = "";

        const mkBtn = (label, idx, opts = {}) => {
            const li = document.createElement("li");
            li.className = `page-item${opts.active ? " active" : ""}${opts.disabled ? " disabled" : ""}`;
            li.innerHTML = `<button class="page-link">${label}</button>`;
            if (!opts.disabled && idx !== null) {
                li.firstElementChild.addEventListener("click", () => {
                    renderPagination(idx);
                    renderToursPage(idx);
                });
            }
            return li;
        };

        pagination.appendChild(mkBtn("←", currentPage - 1, { disabled: currentPage === 0 }));

        const start = Math.max(0, currentPage - 1), end = Math.min(totalPages, start + 3);
        if (start > 0) {
            pagination.appendChild(mkBtn("1", 0));
            if (start > 1) pagination.appendChild(mkBtn("...", null, { disabled: true }));
        }
        for (let i = start; i < end; i++) {
            pagination.appendChild(mkBtn(i + 1, i, { active: i === currentPage }));
        }
        if (end < totalPages) {
            if (end < totalPages - 1) pagination.appendChild(mkBtn("...", null, { disabled: true }));
            pagination.appendChild(mkBtn(totalPages, totalPages - 1));
        }

        pagination.appendChild(mkBtn("→", currentPage + 1, { disabled: currentPage === totalPages - 1 }));

        // <-- CHỈNH THÊM DÒNG NÀY để tự vẽ page đầu tiên -->
        renderToursPage(currentPage);
    }

    function showNoTourMessage() {
        noTourMsg.classList.remove("d-none");
        container.innerHTML = "";
    }

    function renderCompanyFilters(tours) {
        const names = [...new Set(tours.map(t => t.createdByUserName))];
        companyFilterContainer.innerHTML = names.map((n, i) => `
            <div class="form-check">
              <input type="checkbox" class="form-check-input company-checkbox" id="comp-${i}" value="${n}">
              <label class="form-check-label" for="comp-${i}">${n}</label>
            </div>
        `).join("");

        document.querySelectorAll(".company-checkbox").forEach(cb => {
            cb.addEventListener("change", () => {
                selectedFilters.companies = Array.from(
                    document.querySelectorAll(".company-checkbox:checked")
                ).map(x => x.value);
                renderFilteredTours();
            });
        });
    }

    // Event listeners
    priceInput.addEventListener("input", e => {
        selectedFilters.price = parseInt(e.target.value, 10);
        updateDisplayCount();
        renderFilteredTours();
    });

    durationInput.addEventListener("input", e => {
        selectedFilters.duration = parseInt(e.target.value, 10);
        updateDisplayCount();
        renderFilteredTours();
    });

    ratingButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            const val = parseInt(btn.dataset.value, 10);
            if (selectedFilters.rating === val) {
                // Click lần 2: OFF
                selectedFilters.rating = null;
                ratingButtons.forEach(b => b.classList.remove("active"));
            } else {
                // Click sang rating khác: ON
                ratingButtons.forEach(b => b.classList.remove("active"));
                selectedFilters.rating = val;
                btn.classList.add("active");
            }
            renderFilteredTours();
        });
    });



    // Initial fetch
    const params = new URLSearchParams(window.location.search);
    const requestBody = {
        placeName: params.get("placeName"),
        categoryName: params.get("categoryName"),
        duration: params.get("duration") ? parseInt(params.get("duration"), 10) : null,
        touristNumberAssigned: params.get("touristNumberAssigned")
            ? parseInt(params.get("touristNumberAssigned"), 10)
            : null
    };

    loadingSpinner.classList.remove("d-none");
    fetch("/tourify/api/tours/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    })
        .then(res => res.json())
        .then(data => {
            allTours = data || [];
            const maxPrice = Math.max(0, ...allTours.map(t => t.price));
            priceInput.max = maxPrice;
            priceInput.value = maxPrice;
            selectedFilters.price = maxPrice;
            updateDisplayCount();

            loadingSpinner.classList.add("d-none");
            renderCompanyFilters(allTours);
            renderFilteredTours();
        })
        .catch(err => {
            console.error("Failed to load tours", err);
            loadingSpinner.classList.add("d-none");
            showNoTourMessage();
        });

    // Thêm sau khi render danh sách tour:
    document.addEventListener('click', function (e) {
        if (e.target.closest('.btn-favorite')) {
            const btn = e.target.closest('.btn-favorite');
            const tourId = btn.getAttribute('data-tour-id');
            const token = localStorage.getItem('accessToken');
            if (!token) {
                Toastify({
                    text: "<i class='fas fa-exclamation-circle me-2'></i>Please login to add favorite!",
                    duration: 2500,
                    close: true,
                    gravity: "top",
                    position: "right",
                    style: {
                        background: "#fff3cd",
                        color: "#856404",
                        border: "1.5px solid #ffeeba",
                        borderRadius: "0.7rem",
                        fontSize: "1rem",
                        fontWeight: 500,
                        boxShadow: "0 2px 8px #ffeeba77",
                        padding: "0.7rem 1.2rem"
                    },
                    escapeMarkup: false,
                    offset: { x: 20, y: 20 },
                    avatar: false
                }).showToast();
                return;
            }
            btn.disabled = true;
            fetch(`/tourify/api/user/favorites/${tourId}`, {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 1000) {
                        btn.classList.add('active');
                        btn.querySelector('i').classList.add('text-danger');
                        Toastify({
                            text: "<i class='fas fa-check-circle me-2'></i>Added to favorites!",
                            duration: 2000,
                            close: true,
                            gravity: "top",
                            position: "right",
                            style: {
                                background: "#fff",
                                color: "#16b686",
                                border: "1.5px solid #b8ead2",
                                borderRadius: "0.7rem",
                                fontSize: "1rem",
                                fontWeight: 500,
                                boxShadow: "0 2px 8px rgba(34,197,94,0.07)",
                                padding: "0.7rem 1.2rem"
                            },
                            escapeMarkup: false,
                            offset: { x: 20, y: 20 },
                            avatar: false
                        }).showToast();
                    } else {
                        btn.disabled = false;
                        Toastify({
                            text: `<i class='fas fa-exclamation-triangle me-2'></i>${data.message || 'Failed to add favorite!'}`,
                            duration: 2500,
                            close: true,
                            gravity: "top",
                            position: "right",
                            style: {
                                background: "#fff3cd",
                                color: "#856404",
                                border: "1.5px solid #ffeeba",
                                borderRadius: "0.7rem",
                                fontSize: "1rem",
                                fontWeight: 500,
                                boxShadow: "0 2px 8px #ffeeba77",
                                padding: "0.7rem 1.2rem"
                            },
                            escapeMarkup: false,
                            offset: { x: 20, y: 20 },
                            avatar: false
                        }).showToast();
                    }
                })
                .catch(() => {
                    btn.disabled = false;
                    Toastify({
                        text: "<i class='fas fa-exclamation-triangle me-2'></i>Failed to add favorite!",
                        duration: 2500,
                        close: true,
                        gravity: "top",
                        position: "right",
                        style: {
                            background: "#fff3cd",
                            color: "#856404",
                            border: "1.5px solid #ffeeba",
                            borderRadius: "0.7rem",
                            fontSize: "1rem",
                            fontWeight: 500,
                            boxShadow: "0 2px 8px #ffeeba77",
                            padding: "0.7rem 1.2rem"
                        },
                        escapeMarkup: false,
                        offset: { x: 20, y: 20 },
                        avatar: false
                    }).showToast();
                });
        }
    });
});
