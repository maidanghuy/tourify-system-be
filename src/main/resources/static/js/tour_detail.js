document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");

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
