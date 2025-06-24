// js/tour_list.js
document.addEventListener("DOMContentLoaded", () => {
    const priceInput     = document.getElementById("priceRange");
    const priceValue     = document.getElementById("priceValue");
    const durationInput  = document.getElementById("durationRange");
    const durationValue  = document.getElementById("durationValue");
    const ratingButtons  = document.querySelectorAll(".rating-btn");
    const companyFilterContainer = document.getElementById("companyFilterContainer");
    const container      = document.getElementById("tourResultsContainer");
    const noTourMsg      = document.getElementById("noTourMsg");
    const loadingSpinner = document.getElementById("loadingSpinner");
    const pagination     = document.getElementById("pagination");
    const paginationWrapper = pagination.closest(".text-center");
    const scrollTarget   = document.getElementById("tourListScrollable");

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
            (!price    || tour.price <= price) &&
            (!duration || tour.duration >= duration) &&
            (!rating   || Math.floor(tour.rating) === rating) &&
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
                <div class="col-md-12 mb-3">
                  <div class="card tour-card shadow-sm border-0 rounded-4">
                    <div class="row g-0 align-items-stretch">
                      <div class="col-md-4">
                        <img src="${tour.thumbnail}" class="w-100 h-100 object-fit-cover" alt="Thumbnail" />
                      </div>
                      <div class="col-md-8">
                        <div class="card-body d-flex flex-column justify-content-between h-100">
                          <div class="d-flex justify-content-between align-items-center mb-2">
                            <h5 class="fw-bold mb-0">${tour.tourName}</h5>
                            <div>${generateStarRating(tour.rating)}</div>
                          </div>
                          <div class="text-muted small mb-2">
                            <i class="fas fa-location-dot text-success me-1"></i>${tour.placeName}
                            <span class="mx-2">|</span>
                            <i class="fas fa-tag text-success me-1"></i>${tour.categoryName}
                            <span class="mx-2">|</span>
                            <i class="fas fa-calendar-alt text-success me-1"></i>${tour.duration} days
                            <span class="mx-2">|</span>
                            <i class="fas fa-users text-success me-1"></i>${tour.touristNumberAssigned?.toLocaleString()||0} booked
                          </div>
                          <div class="text-muted small mb-3">
                            <i class="fas fa-user-tie me-1" style="color:#0a6e4d"></i>
                            By <a href="/company/${encodeURIComponent(tour.createdByUserName)}" class="fw-semibold text-decoration-none" style="color:#0a6e4d">
                              ${tour.createdByUserName || "Unknown"}
                            </a>
                          </div>
                          <div class="d-flex justify-content-between align-items-center">
                            <span class="text-success fw-bold fs-5">$${tour.price}</span>
                            <div class="btn-group">
                              <button class="btn btn-light border favorite-btn">
                                <i class="fas fa-heart text-danger"></i>
                              </button>
                              <a href="/tourify/tourDetail?id=${tour.tourId}" class="btn btn-success">
                                <i class="fas fa-eye"></i>
                              </a>
                              <a href="/tourify/tourBooking?id=${tour.tourId}" class="btn btn-primary">
                                <i class="fas fa-calendar-check"></i>
                              </a>
                            </div>
                          </div>
                        </div>
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
        btn.addEventListener("click", () => {
            const val = parseInt(btn.dataset.value, 10);
            if (selectedFilters.rating === val) {
                // click 2nd time: off
                selectedFilters.rating = null;
                btn.classList.remove("active");
            } else {
                // click 1st time: on
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
            priceInput.max   = maxPrice;
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
});
