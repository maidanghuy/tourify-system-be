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

    if (!tourId) {
        if (titleEl) titleEl.textContent = "Tour Not Found";
        return;
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
                if(breadcrumbPlace) breadcrumbPlace.textContent = tour.placeName || "";
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
