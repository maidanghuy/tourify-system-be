var isCardRevealed = false;


document.querySelectorAll(".plus").forEach((btn) => {
    btn.addEventListener("click", () => {
        const input = document.getElementById(btn.dataset.target);
        let value = parseInt(input.value);
        input.value = value + 1;
    });
});


const minusButtons = document.querySelectorAll(".minus");

minusButtons.forEach((btn, index) => {
    btn.addEventListener("click", () => {
        const input = document.getElementById(btn.dataset.target);
        let value = parseInt(input.value);
        if ((index === 0 && value > 1) || (index !== 0 && value > 0)) {
            input.value = value - 1;
        }
    });
});



document.querySelectorAll(".payment-card").forEach((card) => {
    card.addEventListener("click", () => {
        // Remove 'active' from all
        document
            .querySelectorAll(".payment-card")
            .forEach((el) => el.classList.remove("active"));


        // Add to current
        card.classList.add("active");


        // Set radio checked manually
        const radio = card.querySelector("input[type='radio']");
        if (radio) radio.checked = true;
    });
});


let isCardNumberVisible = false;


function toggleReveal(headerEl) {
    const fields = document.getElementById("accountFields");
    const checkIcon = document.getElementById("checkIcon");
    const checkoutBtn = document.getElementById("checkoutBtn");


    isCardRevealed = !isCardRevealed;


    fields.classList.toggle("d-none", !isCardRevealed);
    checkIcon.classList.toggle("text-muted", !isCardRevealed);
    checkIcon.classList.toggle("text-success", isCardRevealed);


    headerEl.classList.toggle("clicked", isCardRevealed);
    headerEl.classList.toggle("bg-light", !isCardRevealed);
    headerEl.classList.toggle("bg-success-subtle", isCardRevealed);


    // Enable checkout only when card is revealed
    checkoutBtn.disabled = !isCardRevealed;
}


function toggleCardNumber(event) {
    event.stopPropagation();
    const display = document.getElementById("cardNumber");
    const eye = document.getElementById("eyeIcon");


    if (isCardNumberVisible) {
        display.innerHTML = "**** <span class='fw-bold'>4321</span>";
        eye.classList.replace("fa-eye-slash", "fa-eye");
    } else {
        display.innerHTML = "<span class='fw-bold'>1234 5678 9012 4321</span>";
        eye.classList.replace("fa-eye", "fa-eye-slash");
    }


    isCardNumberVisible = !isCardNumberVisible;
}


function editField(field) {
    document.getElementById(field + "View").classList.add("d-none");
    document.getElementById(field + "Edit").classList.remove("d-none");
}


function cancelField(field) {
    document.getElementById(field + "Edit").classList.add("d-none");
    document.getElementById(field + "View").classList.remove("d-none");
}


function confirmField(field) {
    const value = document.getElementById(field + "Input").value;
    document.getElementById(field + "Display").textContent = value;
    cancelField(field);
}


function toggleReveal(headerEl) {
    const fields = document.getElementById("accountFields");
    const checkIcon = document.getElementById("checkIcon");


    isCardRevealed = !isCardRevealed;


    fields.classList.toggle("d-none", !isCardRevealed);
    checkIcon.classList.toggle("text-muted", !isCardRevealed);
    checkIcon.classList.toggle("text-success", isCardRevealed);


    // toggle glow
    headerEl.classList.toggle("clicked", isCardRevealed);
    headerEl.classList.toggle("bg-light", !isCardRevealed);
    headerEl.classList.toggle("bg-success-subtle", isCardRevealed);
}


function showLoading(btn) {
    btn.disabled = true;
    btn.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Processing...`;
    // Simulate submit
    setTimeout(() => (btn.innerHTML = `<i class="fas fa-check me-2"></i>Paid`), 1500);
}


function handleCheckout(btn) {
    btn.disabled = true;
    btn.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Processing...`;


    setTimeout(() => {
        btn.innerHTML = `<i class="fas fa-credit-card me-2"></i>Check Out`;
        btn.disabled = false;


        const qrModal = new bootstrap.Modal(document.getElementById("qrModal"));
        qrModal.show();
    }, 1500);
}


function reloadQRCode() {
    // Nếu muốn đổi ảnh thì bạn có thể gọi lại ảnh khác tại đây
    alert("QR Code reloaded!");
}


function toggleReveal(headerEl) {
    const fields = document.getElementById("accountFields");
    const checkIcon = document.getElementById("checkIcon");
    const checkoutBtn = document.getElementById("checkoutBtn"); // nút nằm ngoài


    // Toggle trạng thái
    isCardRevealed = !isCardRevealed;


    // Ẩn/hiện thông tin
    fields.classList.toggle("d-none", !isCardRevealed);


    // Đổi màu icon
    checkIcon.classList.toggle("text-muted", !isCardRevealed);
    checkIcon.classList.toggle("text-success", isCardRevealed);


    // Đổi màu header
    headerEl.classList.toggle("clicked", isCardRevealed);
    headerEl.classList.toggle("bg-light", !isCardRevealed);
    headerEl.classList.toggle("bg-success-subtle", isCardRevealed);


    // ✅ Bật/tắt nút Check Out
    if (checkoutBtn) {
        checkoutBtn.disabled = !isCardRevealed;
    }
}


document.querySelectorAll('.date-container').forEach(container => {
    const raw = container.querySelector('.date-raw');
    const disp = container.querySelector('.date-display');

    // Khi click vào ô hiển thị, mở picker của input type=date
    disp.addEventListener('click', () => {
        raw.showPicker?.(); // Chrome/Edge
        raw.click();       // fallback
    });

    // Khi chọn ngày xong, định dạng lại và gán vào ô hiển thị
    raw.addEventListener('change', () => {
        if (!raw.value) return;
        const [year, month, day] = raw.value.split('-');
        disp.value = `${day}-${month}-${year}`;
    });
});

let tourPrice;

document.addEventListener("DOMContentLoaded",  function () {
    // 1. Lấy tourId từ URL
    const urlParams = new URLSearchParams(window.location.search);
    const tourId = urlParams.get("id");

    if (!tourId) {
        console.error("Không tìm thấy tourId trong URL.");
        return;
    }

    // 2. Gọi API để lấy thông tin Tour
    fetch(`/tourify/api/tours/${tourId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Lỗi khi gọi API.");
            }
            return response.json();
        })
        .then(data => {
            if (data.code !== 1000) {
                throw new Error("Lỗi từ server: " + data.code);
            }

            const tour = data.result;

            // 3. Hiển thị thông tin tour (tùy chỉnh phần này theo giao diện của bạn)
            document.getElementById("tour-title").textContent = tour.tourName;
            document.getElementById("tour-price").textContent = tour.price.toLocaleString() + " VND";
            // document.getElementById("tour-description").textContent = tour.description;
            //document.getElementById("tour-duration").textContent = tour.duration + " ngày";
            //document.getElementById("tour-image").src = tour.thumbnail;
            document.getElementById("original-price").textContent = tour.price.toLocaleString() + " VND";
            document.getElementById("tour-sub-title").textContent = tour.tourName;
            document.getElementById("tour-title-short-link").textContent = tour.tourName;
            // Gắn thêm nếu cần
            document.getElementById("place-name").textContent = tour.placeName;
            tourPrice = tour.price;
        })
        .catch(error => {
            console.error("Đã xảy ra lỗi:", error);
        });

});
    let adultCount = 0;
    let childCount = 0;

    const adultPlus = document.getElementById("adult-plus");
    const adultMinus = document.getElementById("adult-minus");
    const childPlus = document.getElementById("child-plus");
    const childMinus = document.getElementById("child-minus");
    const originalPriceElement = document.getElementById("original-price");
    // console.log(originalPriceElement);

    function updatePrice() {
        adultCount = Math.max(1, parseInt(adultCount));
        childCount = Math.max(0, parseInt(childCount));
        let price = tourPrice + (adultCount) * tourPrice * 0.2 + childCount * tourPrice * 0.15;
        console.log(tourPrice);
        originalPriceElement.innerText = price.toLocaleString() + " VND";
    }

    adultPlus.addEventListener("click", () => {
        adultCount++;
        updatePrice();
    });

    adultMinus.addEventListener("click", () => {
        if (adultCount > 1) {
            adultCount--;
            updatePrice();
        }
    });

    childPlus.addEventListener("click", () => {
        childCount++;
        updatePrice();
    });

    childMinus.addEventListener("click", () => {
        if (childCount > 0) {
            childCount--;
            updatePrice();
        }
    });
