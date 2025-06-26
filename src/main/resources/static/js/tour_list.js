document.addEventListener("DOMContentLoaded", function () {
    const priceInput = document.getElementById("priceRange");
    const priceValue = document.getElementById("priceValue");
    const durationInput = document.getElementById("durationRange");
    const durationValue = document.getElementById("durationValue");
    const ratingButtons = document.querySelectorAll(".rating-btn");
    const applyFilterBtn = document.getElementById("applyFilterBtn");

    const container = document.getElementById("tourResultsContainer");
    const noTourMsg = document.getElementById("noTourMsg");
    const loadingSpinner = document.getElementById("loadingSpinner");
    const pagination = document.getElementById("pagination");
    const paginationWrapper = pagination.closest(".text-center");

    let allTours = [];
    let filteredTours = [];
    const chunkSize = 4;

    priceInput.addEventListener("input", () => {
        priceValue.textContent = `$${priceInput.value}`;
    });

    durationInput.addEventListener("input", () => {
        durationValue.textContent = `${durationInput.value} days`;
    });

    ratingButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            ratingButtons.forEach((b) => b.classList.remove("active"));
            btn.classList.add("active");
        });
    });

    const params = new URLSearchParams(window.location.search);
    const requestBody = {
        placeName: params.get("placeName") || null,
        categoryName: params.get("categoryName") || null,
        duration: params.get("duration") ? parseInt(params.get("duration")) : null,
        touristNumberAssigned: params.get("touristNumberAssigned") ? parseInt(params.get("touristNumberAssigned")) : null
    };

    loadingSpinner.classList.remove("d-none");
    container.innerHTML = "";
    paginationWrapper.classList.add("d-none");

    fetch("http://localhost:8080/tourify/api/tours/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    })
        .then(response => response.json())
        .then(tours => {
            loadingSpinner.classList.add("d-none");
            allTours = tours;
            filteredTours = [...allTours];

            if (!tours || tours.length === 0) {
                showNoTourMessage();
                paginationWrapper.classList.add("d-none");
            } else {
                noTourMsg.style.display = "none";
                renderPagination();
                paginationWrapper.classList.remove("d-none");
            }
        })
        .catch(error => {
            loadingSpinner.classList.add("d-none");
            console.error("Error loading tours:", error);
            container.innerHTML = `<div class="text-danger text-center w-100 py-4">
                <i class="fas fa-exclamation-triangle me-2"></i>
                Failed to load tours. Please try again later.
            </div>`;
        });

    applyFilterBtn.addEventListener("click", () => {
        const maxPrice = parseFloat(priceInput.value);
        const minDuration = parseInt(durationInput.value);
        const selectedRating = parseInt(document.querySelector(".rating-btn.active")?.dataset.value || "0");
        const selectedCompanies = Array.from(document.querySelectorAll("input[type=checkbox]:checked"))
            .map(cb => cb.nextElementSibling.textContent.trim());

        filteredTours = allTours.filter(tour => {
            return (!maxPrice || tour.price <= maxPrice) &&
                (!minDuration || tour.duration >= minDuration) &&
                (!selectedRating || (tour.rating && Math.floor(tour.rating) >= selectedRating)) &&
                (selectedCompanies.length === 0 || selectedCompanies.includes(tour.createdByUserName));
        });

        if (filteredTours.length === 0) {
            showNoTourMessage();
            paginationWrapper.classList.add("d-none");
        } else {
            noTourMsg.style.display = "none";
            renderPagination();
            paginationWrapper.classList.remove("d-none");
        }
    });

    function generateStarRating(rating) {
        if (typeof rating !== 'number' || isNaN(rating) || rating <= 0) {
            return '<span class="text-muted small fst-italic">This tour has not been rated yet</span>';
        }

        // Clamp rating to a max of 5
        if (rating > 5) rating = 5;

        // Round to the nearest 0.5
        rating = Math.round(rating * 2) / 2;

        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 !== 0;
        const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

        let html = '';

        for (let i = 0; i < fullStars; i++) {
            html += '<i class="fas fa-star text-warning"></i>';
        }
        if (hasHalfStar) {
            html += '<i class="fas fa-star-half-alt text-warning"></i>';
        }
        for (let i = 0; i < emptyStars; i++) {
            html += '<i class="far fa-star text-warning"></i>';
        }

        html += ` <span class="ms-1 text-muted small">(${rating.toFixed(1)})</span>`;
        return html;
    }


    function renderToursPage(pageIndex) {
        container.innerHTML = "";
        const start = pageIndex * chunkSize;
        const end = start + chunkSize;
        const pageData = filteredTours.slice(start, end);

        pageData.forEach(tour => {
            const html = `
        <div class="col-md-12">
            <div class="card tour-card shadow-sm border-0 rounded-4 mb-3">
                <div class="row g-0 align-items-stretch">
                    <div class="col-md-4">
                        <div class="h-100">
                            <img src="${tour.thumbnail}" class="fixed-tour-thumbnail w-100 h-100" alt="Tour Thumbnail" />
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="card-body d-flex flex-column justify-content-between h-100">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h5 class="card-title fw-bold text-dark mb-0">${tour.tourName}</h5>
                                <div class="ms-2">${generateStarRating(tour.rating)}</div>
                            </div>
                            <div class="mb-2 text-muted small">
                                <i class="fas fa-location-dot me-1 text-success"></i> ${tour.placeName}
                                <span class="mx-2">|</span>
                                <i class="fas fa-tag me-1 text-success"></i> ${tour.categoryName}
                                <span class="mx-2">|</span>
                                <i class="fas fa-calendar-alt me-1 text-success"></i> ${tour.duration} days
                                <span class="mx-2">|</span>
                                <i class="fas fa-users me-1 text-success"></i> ${tour.touristNumberAssigned?.toLocaleString() || 0} booked
                            </div>

                            <div class="text-muted small">
                                <i class="fas fa-user-tie me-1" style="color: #0a6e4d"></i>
                                Tour by:
                                <a href="/company/${encodeURIComponent(tour.createdByUserName)}" class="fw-semibold text-decoration-none" style="color: #0a6e4d">
                                    ${tour.createdByUserName || "Unknown"}
                                </a>
                            </div>
                            <p class="card-text text-success fw-bold fs-5 mt-2">$${tour.price}</p>
                            <div class="d-flex justify-content-between align-items-center mt-3">
                                <button class="btn btn-outline-success d-flex align-items-center gap-2 px-3 rounded-pill">
                                    <i class="fas fa-plus"></i> Add to Favorites
                                </button>
                                <a href="/tourify/tourDetail?id=${tour.tourId}"  class="btn btn-success px-4 rounded-pill">View Details</a>
                                <button
                                      class="btn btn-outline-success d-flex align-items-center gap-2 px-3 rounded-pill"
                                      onclick="bookingNow('${tour.tourId}')"
                                    >
                                    <i class="fas fa-calendar-plus"></i> Book Now
                                    </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
            container.insertAdjacentHTML("beforeend", html);
        });
    }

    function renderPagination() {
        const totalPages = Math.ceil(filteredTours.length / chunkSize);
        pagination.innerHTML = "";

        for (let i = 0; i < totalPages; i++) {
            const li = document.createElement("li");
            li.className = "page-item";
            li.innerHTML = `<button class="page-link">${i + 1}</button>`;
            li.addEventListener("click", () => {
                renderToursPage(i);
                setActivePage(i);
            });
            pagination.appendChild(li);
        }

        setActivePage(0);
    }

    function setActivePage(index) {
        const buttons = document.querySelectorAll("#pagination .page-item");
        buttons.forEach((btn, i) => {
            btn.classList.toggle("active", i === index);
        });
        renderToursPage(index);
    }

    function showNoTourMessage() {
        noTourMsg.style.display = "block";
        container.innerHTML = `
            <div class="no-tour-message w-100">
                <div class="p-4 rounded-4 shadow-sm bg-light d-flex flex-column align-items-center justify-content-center">
                    <i class="fas fa-search-minus fa-3x text-secondary mb-3"></i>
                    <h5 class="text-secondary fw-semibold mb-1">No Tours Found</h5>
                    <p class="text-muted mb-0">We couldn't find any tours matching your search. Please try different keywords.</p>
                </div>
            </div>`;
    }
});
