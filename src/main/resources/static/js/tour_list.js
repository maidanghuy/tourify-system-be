document.addEventListener('DOMContentLoaded', () => {
    // --- Elements ---
    const priceInput = document.getElementById('priceRange');
    const priceValue = document.getElementById('priceValue');
    const durationInput = document.getElementById('durationRange');
    const durationValue = document.getElementById('durationValue');
    const ratingButtons = document.querySelectorAll('.rating-btn');
    const companyFilterContainer = document.getElementById('companyFilterContainer');
    const container = document.getElementById('tourResultsContainer');
    const noTourMsg = document.getElementById('noTourMsg');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const pagination = document.getElementById('pagination');
    const paginationWrapper = pagination.closest('.text-center');
    const scrollTarget = document.getElementById('tourListScrollable');
    // Compare modal elements
    const compareHeader = document.getElementById('compareHeader');
    const compareBody = document.getElementById('compareBody');
    const doCompareBtn = document.getElementById('doCompareBtn');
    const compareModalEl = document.getElementById('compareModal');
    const compareModal = new bootstrap.Modal(compareModalEl);
    // Dropdown
    const compareDropdownWrap = document.getElementById('compareDropdownWrap');
    const compareDropdownSelect = document.getElementById('compareDropdownSelect');
    const addDropdownTourBtn = document.getElementById('addDropdownTourBtn');
    // Show only differences checkbox (chỉ lấy từ DOM 1 lần)
    const showDiffCheckbox = document.getElementById('showDifferencesOnly');

    // Chunk size for paging
    const chunkSize = 4;

    let allTours = [];
    let filteredTours = [];
    let compareArray = [];
    let compareMainTour = null;
    let favoriteTourIds = [];

    const selectedFilters = {
        price: null,
        duration: parseInt(durationInput.value, 10),
        rating: null,
        companies: []
    };

    const compareFields = [
        ['Tour Name', 'tourName'],
        ['Description', 'description'],
        ['Duration (days)', 'duration'],
        ['Price (VND)', 'price'],
        ['Min People', 'minPeople'],
        ['Max People', 'maxPeople'],
        ['Assigned', 'touristNumberAssigned'],
        ['Place', 'placeName'],
        ['Category', 'categoryName'],
        ['Rating', 'rating'],
        ['Created By', 'createdByUserName'],
        ['Booked Count', 'bookedCustomerCount']
    ];

    // --- Helpers ---
    function updateDisplayCount() {
        priceValue.textContent = `${parseInt(priceInput.value, 10).toLocaleString()} VND`;
        durationValue.textContent = `${parseInt(durationInput.value, 10)} day(s)`;
    }

    function generateStarRating(rating) {
        if (!rating || isNaN(rating)) {
            return `<span class="text-muted small fst-italic">Unrated</span>`;
        }
        rating = Math.min(Math.round(rating * 2) / 2, 5);
        const full = Math.floor(rating);
        const half = rating % 1 !== 0;
        const empty = 5 - full - (half ? 1 : 0);
        return (
            '<i class="fas fa-star text-warning"></i>'.repeat(full) +
            (half ? '<i class="fas fa-star-half-alt text-warning"></i>' : '') +
            '<i class="far fa-star text-warning"></i>'.repeat(empty) +
            ` <span class="ms-1 text-muted small">(${rating.toFixed(1)})</span>`
        );
    }

    function showNoTourMessage() {
        noTourMsg.classList.remove('d-none');
        container.innerHTML = '';
        paginationWrapper.classList.add('d-none');
    }

    function renderToursPage(pageIndex) {
        container.innerHTML = '';
        const start = pageIndex * chunkSize;
        const pageData = filteredTours.slice(start, start + chunkSize);

        pageData.forEach(tour => {
            // Log để kiểm tra structure
            console.log('Tour object:', tour);

            // Lấy userId subcompany từ managementBy
            const userId = tour.managementBy?.userId || '';
            container.insertAdjacentHTML('beforeend', `
    <div class="tour-luxury-card d-flex align-items-stretch mb-4"
         style="background: linear-gradient(135deg, #e5f9ff 0%, #fffbe7 100%);
                border-radius: 24px; box-shadow: 0 8px 32px rgba(24,70,130,0.09);">
      <div class="position-relative tour-img-wrap"
           style="min-width:220px; max-width:220px; border-radius: 20px 0 0 20px;
                  overflow: hidden;">
        <img src="${tour.thumbnail}"
     class="w-100 object-fit-cover"
     alt="Thumbnail"
     style="height:250px; object-fit:cover;">

        <span class="badge tour-type-badge position-absolute"
              style="top:16px; left:16px;">
          ${tour.categoryName || 'Unknown'}
        </span>
        <span class="badge tour-rating-badge position-absolute"
              style="top:16px; right:16px;">
          <i class="fas fa-star me-1"></i>${tour.rating ?? 'N/A'}
        </span>
      </div>
      <div class="flex-grow-1 d-flex flex-column p-4">
        <div class="d-flex justify-content-between align-items-start mb-2">
          <div>
            <h5 class="fw-bold mb-2" style="font-size:1.3rem;">
              ${tour.tourName}
            </h5>
            <div class="text-muted" style="font-size:1rem;">
              <i class="fas fa-location-dot me-1"></i>${tour.placeName}
              <span class="mx-2">·</span>
              <i class="fas fa-calendar-alt me-1"></i>${tour.duration} days
              <span class="mx-2">·</span>
              <i class="fas fa-users me-1"></i>
              ${tour.touristNumberAssigned?.toLocaleString() || 0} booked
            </div>
          </div>
          <div class="tour-company-badge px-3 py-2 ms-2 rounded-pill subcompany-link"
               tabindex="0"
               data-user-id="${userId}"
               style="background:#f3faf5;color:#139169;cursor:pointer;">
            <i class="fa fa-user-circle me-2" style="font-size:1.22em;"></i>
            ${tour.createdByUserName || 'Unknown'}
          </div>
        </div>
        <div class="tour-desc p-2 px-3 rounded-3 bg-white shadow-sm mb-3" style="font-size:1rem;color:#566478;">
          <div class="tour-desc-text line-clamp">
            ${tour.description || 'Discover an amazing journey with Tourify'}
          </div>
        </div>


        <div class="d-flex justify-content-between align-items-center mt-auto">
          <div class="fw-bold" style="font-size:1.5rem;color:#139169;">
            ${tour.price.toLocaleString()}
            <span class="fs-6 fw-normal" style="color:#a0b0c2;">VND</span>
          </div>
          <div class="d-flex gap-2">
            <button class="action-btn btn-compare"
                    data-tour-id="${tour.tourId}"
                    title="Compare tours">
              <i class="fas fa-exchange-alt"></i>
            </button>
            <button class="action-btn btn-favorite ${favoriteTourIds.includes(tour.tourId) ? 'active' : ''}"
                    data-tour-id="${tour.tourId}"
                    title="${favoriteTourIds.includes(tour.tourId) ? 'Remove from favorites' : 'Add to favorites'}">
              <i class="fa fa-heart ${favoriteTourIds.includes(tour.tourId) ? 'text-danger' : ''}"></i>
            </button>
            <a href="/tourify/tourDetail?id=${tour.tourId}"
               class="action-btn"
               title="View details">
              <i class="fa fa-eye"></i>
            </a>
            <a href="/tourify/tour/booking?id=${tour.tourId}"
               class="action-btn"
               title="Book this tour">
              <i class="fa fa-plane-departure"></i>
            </a>
          </div>
        </div>
      </div>
    </div>
  `);
        });

        // --- Thêm sự kiện click chuyển hướng sang subcompanyDetail ---
        container.querySelectorAll('.subcompany-link').forEach(div => {
            div.addEventListener('click', function () {
                const userId = this.getAttribute('data-user-id');
                console.log('[Badge Click] userId:', userId);
                if (userId) {
                    window.location.href = `/tourify/subcompanyDetail?userId=${userId}`;
                }
            });
        });

        scrollTarget.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function renderPagination(currentPage) {
        const totalPages = Math.ceil(filteredTours.length / chunkSize);
        pagination.innerHTML = '';

        function mkBtn(label, idx, { active = false, disabled = false } = {}) {
            const li = document.createElement('li');
            li.className = `page-item${active ? ' active' : ''}${disabled ? ' disabled' : ''}`;
            li.innerHTML = `<button class="page-link">${label}</button>`;
            if (!disabled && idx !== null) {
                li.firstElementChild.addEventListener('click', () => {
                    renderPagination(idx);
                    renderToursPage(idx);
                });
            }
            return li;
        }
        // Prev
        pagination.appendChild(mkBtn('←', currentPage - 1, { disabled: currentPage === 0 }));
        // Pages window
        const start = Math.max(0, currentPage - 1);
        const end = Math.min(totalPages, start + 3);

        if (start > 0) {
            pagination.appendChild(mkBtn('1', 0));
            if (start > 1) pagination.appendChild(mkBtn('...', null, { disabled: true }));
        }
        for (let i = start; i < end; i++) {
            pagination.appendChild(mkBtn(i + 1, i, { active: i === currentPage }));
        }
        if (end < totalPages) {
            if (end < totalPages - 1) pagination.appendChild(mkBtn('...', null, { disabled: true }));
            pagination.appendChild(mkBtn(totalPages, totalPages - 1));
        }
        // Next
        pagination.appendChild(mkBtn('→', currentPage + 1, { disabled: currentPage === totalPages - 1 }));
        renderToursPage(currentPage);
    }

    // --- Filters & apply ---
    function renderCompanyFilters(tours) {
        const names = [...new Set(tours.map(t => t.createdByUserName))];
        companyFilterContainer.innerHTML = names.map((n, i) => `
      <div class="form-check">
        <input class="form-check-input company-checkbox"
               type="checkbox"
               id="comp-${i}"
               value="${n}">
        <label class="form-check-label" for="comp-${i}">${n}</label>
      </div>
    `).join('');
        document.querySelectorAll('.company-checkbox').forEach(cb => {
            cb.addEventListener('change', () => {
                selectedFilters.companies = Array.from(
                    document.querySelectorAll('.company-checkbox:checked')
                ).map(x => x.value);
                applyFilters();
            });
        });
    }

    function applyFilters() {
        const { price, duration, rating, companies } = selectedFilters;
        filteredTours = allTours.filter(tour =>
            (!price || tour.price <= price) &&
            (!duration || tour.duration >= duration) &&
            (!rating || Math.floor(tour.rating) === rating) &&
            (companies.length === 0 || companies.includes(tour.createdByUserName))
        );
        if (filteredTours.length === 0) {
            showNoTourMessage();
        } else {
            noTourMsg.classList.add('d-none');
            paginationWrapper.classList.remove('d-none');
            renderPagination(0);
        }
    }

    // --- Event listeners ---
    priceInput.addEventListener('input', e => {
        selectedFilters.price = parseInt(e.target.value, 10);
        updateDisplayCount();
        applyFilters();
    });
    durationInput.addEventListener('input', e => {
        selectedFilters.duration = parseInt(e.target.value, 10);
        updateDisplayCount();
        applyFilters();
    });
    ratingButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const val = parseInt(btn.dataset.value, 10);
            if (selectedFilters.rating === val) {
                selectedFilters.rating = null;
                ratingButtons.forEach(b => b.classList.remove('active'));
            } else {
                ratingButtons.forEach(b => b.classList.remove('active'));
                selectedFilters.rating = val;
                btn.classList.add('active');
            }
            applyFilters();
        });
    });

    // --- Favorite (Toastify) & auth check ---
    document.addEventListener('click', e => {
        const favBtn = e.target.closest('.btn-favorite');
        if (!favBtn) return;

        const tourId = favBtn.dataset.tourId;
        const token = localStorage.getItem('accessToken');
        if (!token) {
            Toastify({ text: "Please login first", className: "bg-danger" }).showToast();
            return;
        }

        favBtn.disabled = true;
        const isFavorite = favoriteTourIds.includes(tourId);

        if (isFavorite) {
            // Xóa khỏi favorites
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
                        // Xóa khỏi mảng
                        favoriteTourIds = favoriteTourIds.filter(id => id !== tourId);
                        favBtn.classList.remove('active');
                        favBtn.querySelector('i').classList.remove('text-danger');
                        Toastify({ text: "Removed from favorites", className: "bg-success" }).showToast();
                    } else {
                        Toastify({ text: "Failed to remove favorite", className: "bg-danger" }).showToast();
                    }
                })
                .catch(() => {
                    favBtn.disabled = false;
                    Toastify({ text: "Network error", className: "bg-danger" }).showToast();
                });
        } else {
            // Thêm vào favorites
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
                        favoriteTourIds.push(tourId);
                        favBtn.classList.add('active');
                        favBtn.querySelector('i').classList.add('text-danger');
                        Toastify({ text: "Added to favorites", className: "bg-success" }).showToast();
                    } else {
                        Toastify({ text: "Failed to add favorite", className: "bg-danger" }).showToast();
                    }
                })
                .catch(() => {
                    favBtn.disabled = false;
                    Toastify({ text: "Network error", className: "bg-danger" }).showToast();
                });
        }
    });

    // --- Compare modal logic ---
    container.addEventListener('click', e => {
        const cmpBtn = e.target.closest('.btn-compare');
        if (!cmpBtn) return;
        const id = cmpBtn.dataset.tourId;
        const tourObj = allTours.find(t => t.tourId === id);
        if (!compareMainTour || compareMainTour.tourId !== id) {
            compareMainTour = tourObj;
            compareArray = [tourObj];
        }
        renderCompareControls();
        compareModal.show();
    });

    compareHeader.addEventListener('click', e => {
        const btnRemove = e.target.closest('.btn-remove-compare');
        if (btnRemove) {
            const id = btnRemove.dataset.tourId;
            compareArray = compareArray.filter(t => t.tourId !== id);
            renderCompareControls();
            hideCompareDropdown();
            return;
        }
        const btnAdd = e.target.closest('.btn-add-compare');
        if (btnAdd) {
            showCompareDropdown();
        }
        const btnView = e.target.closest('.btn-view-detail');
        if (btnView) {
            const tourId = btnView.dataset.tourId;
            window.location.href = `/tourify/tourDetail?id=${tourId}`;
        }
    });

    if (addDropdownTourBtn) {
        addDropdownTourBtn.onclick = () => {
            const tourId = compareDropdownSelect.value;
            if (!tourId) return;
            if (compareArray.length >= 4 || compareArray.some(t => t.tourId === tourId)) return;
            const tour = allTours.find(t => t.tourId === tourId);
            if (tour) {
                compareArray.push(tour);
                renderCompareControls();
            }
            hideCompareDropdown();
        };
    }
    document.addEventListener('mousedown', function (e) {
        if (
            compareDropdownWrap &&
            compareDropdownWrap.style.display === 'block' &&
            !compareDropdownWrap.contains(e.target) &&
            !e.target.classList.contains('btn-add-compare')
        ) {
            hideCompareDropdown();
        }
    });
    function showCompareDropdown() {
        if (!compareDropdownWrap) return;
        const otherTours = allTours.filter(t => !compareArray.some(c => c.tourId === t.tourId));
        compareDropdownSelect.innerHTML = otherTours.map(t => `<option value="${t.tourId}">${t.tourName}</option>`).join('');
        if (otherTours.length === 0) {
            hideCompareDropdown();
            return;
        }
        compareDropdownWrap.style.display = 'block';
        compareDropdownSelect.selectedIndex = 0;
    }
    function hideCompareDropdown() {
        if (!compareDropdownWrap) return;
        compareDropdownWrap.style.display = 'none';
    }

    // Highlight differences and filter rows
    function highlightDifferences() {
        // Xóa class highlight cũ
        compareBody.querySelectorAll('td').forEach(td => td.classList.remove('compare-diff'));
        compareBody.querySelectorAll('tr').forEach(tr => {
            const cells = Array.from(tr.querySelectorAll('td'));
            if (!cells.length) return;
            const values = cells.map(td => td.innerText.trim());
            const allSame = values.every(val => val === values[0]);
            if (!allSame) {
                cells.forEach(td => td.classList.add('compare-diff'));
            }
        });
    }

    function filterShowDiffRows() {
        if (!showDiffCheckbox) return;
        const checked = showDiffCheckbox.checked;
        if (!checked) {
            Array.from(compareBody.querySelectorAll('tr')).forEach(tr => tr.style.display = '');
        } else {
            Array.from(compareBody.querySelectorAll('tr')).forEach(tr => {
                const hasDiff = tr.querySelector('.compare-diff');
                tr.style.display = hasDiff ? '' : 'none';
            });
        }
    }
    doCompareBtn.addEventListener('click', () => {
        highlightDifferences();
        filterShowDiffRows();
    });

    if (showDiffCheckbox) {
        showDiffCheckbox.onchange = function () {
            highlightDifferences();
            filterShowDiffRows();
        };
    }

    compareModalEl.addEventListener('hidden.bs.modal', () => {
        compareArray = [];
        compareMainTour = null;
        compareHeader.innerHTML = '';
        compareBody.innerHTML = '';
        doCompareBtn.disabled = true;
        hideCompareDropdown();
        if (showDiffCheckbox) showDiffCheckbox.checked = false;
    });

    // --- Render Compare Controls ---
    function renderCompareControls() {
        compareArray = compareArray.slice(0, 4);
        compareArray = compareArray.filter(t => allTours.some(x => x.tourId === t.tourId));
        if (!compareArray.length && compareMainTour) compareArray = [compareMainTour];

        // Header render: Each tour has View Details and Remove (except first)
        let headerHtml = `<th>Field</th>`;
        compareArray.forEach((t, idx) => {
            headerHtml += `<th>
            <div class="d-flex align-items-center gap-2">
              <span>${t.tourName}</span>
              <button class="btn btn-link btn-sm px-1 btn-view-detail"
                      data-tour-id="${t.tourId}"
                      title="View details">
                  <i class="fa fa-eye"></i>
              </button>
              ${idx === 0 ? '' : `
                <button class="btn btn-sm btn-outline-danger ms-1 btn-remove-compare"
                        data-tour-id="${t.tourId}" title="Remove from comparison">
                    <i class="fas fa-minus"></i>
                </button>
              `}
            </div>
        </th>`;
        });
        const otherTours = allTours.filter(t => !compareArray.some(c => c.tourId === t.tourId));
        if (compareArray.length < 4 && otherTours.length > 0) {
            headerHtml += `<th>
            <button class="btn btn-sm btn-outline-success btn-add-compare" title="Add">
                <i class="fas fa-plus"></i>
            </button>
        </th>`;
        }
        compareHeader.innerHTML = headerHtml;

        // Render body
        compareBody.innerHTML = compareFields.map(([label, key]) => {
            let row = `<tr><th>${label}</th>`;
            compareArray.forEach(t => {
                row += `<td>${t[key] ?? ''}</td>`;
            });
            if (compareArray.length < 4 && otherTours.length > 0) row += `<td></td>`;
            row += `</tr>`;
            return row;
        }).join('');

        doCompareBtn.disabled = compareArray.length < 2;

        // Clean previous state
        compareBody.querySelectorAll('td').forEach(td => td.classList.remove('compare-diff'));
        compareBody.querySelectorAll('tr').forEach(tr => tr.style.display = '');
        if (showDiffCheckbox) showDiffCheckbox.checked = false;
        hideCompareDropdown();
    }

    // --- Initial fetch tours ---
    (function fetchTours() {
        const params = new URLSearchParams(window.location.search);
        const requestBody = {
            placeName: params.get('placeName'),
            categoryName: params.get('categoryName'),
            duration: params.get('duration')
                ? parseInt(params.get('duration'), 10)
                : null,
            touristNumberAssigned: params.get('touristNumberAssigned')
                ? parseInt(params.get('touristNumberAssigned'), 10)
                : null
        };

        loadingSpinner.classList.remove('d-none');
        fetch('/tourify/api/tours/search', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody)
        })
            .then(res => res.json())
            .then(data => {
                allTours = data || [];
                const maxPrice = Math.max(0, ...allTours.map(t => t.price || 0));
                priceInput.max = maxPrice;
                priceInput.value = maxPrice;
                selectedFilters.price = maxPrice;
                updateDisplayCount();

                loadingSpinner.classList.add('d-none');
                const token = localStorage.getItem('accessToken');
                if (token) {
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
                                favoriteTourIds = data.result.map(t => t.tourId);
                            } else {
                                favoriteTourIds = [];
                            }
                            renderCompanyFilters(allTours);
                            applyFilters();
                        });
                } else {
                    favoriteTourIds = [];
                    renderCompanyFilters(allTours);
                    applyFilters();
                }
            })
            .catch(err => {
                console.error('Failed to load tours', err);
                loadingSpinner.classList.add('d-none');
                showNoTourMessage();
            });
    })();
});