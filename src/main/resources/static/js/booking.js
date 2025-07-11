var isCardRevealed = false;


// Xử lý áp dụng mã khuyến mãi khi nhấn nút "Áp dụng"
document.getElementById("applyPromotionBtn").addEventListener("click", () => {
    const code = document.getElementById("promo-code").textContent;
    const promo = loadedPromotions.find(p => p.code === code);
    const originalPrice = calculateOriginalPrice();

    if (originalPrice < promo.minPurchase) {
        Swal.fire({
            icon: 'error',
            title: 'Không đủ điều kiện',
            text: 'Original Price không đủ để áp dụng mã khuyến mãi.',
            confirmButtonColor: '#d33'
        });
        return;
    }

    // Gán mã đã chọn
    const dropdownBtn = document.getElementById("promotionDropdownBtn");
    const hiddenInput = document.getElementById("selectedPromotionCode");

    dropdownBtn.innerHTML = `✔ ${promo.code} - Giảm ${promo.discountPercent}%`;
    hiddenInput.value = promo.code;
    selectedCode = promo.code;
    currentMinPurchase = promo.minPurchase;

    // Đánh dấu checkbox tương ứng
    const checkboxes = document.querySelectorAll("#promotionDropdownList input[type='checkbox']");
    checkboxes.forEach(cb => {
        cb.checked = (cb.value === promo.code);
    });

    // Đóng modal
    const modal = bootstrap.Modal.getInstance(document.getElementById("promotionModal"));
    modal.hide();

    // Cuộn đến phần thanh toán
    document.getElementById("checkoutBtn")?.scrollIntoView({behavior: "smooth"});

    updateDiscountAmount();
});


// // Tăng/giảm số lượng người lớn và trẻ em - đang bị lặp
// document.querySelectorAll(".plus").forEach((btn) => {
//     btn.addEventListener("click", () => {
//         const input = document.getElementById(btn.dataset.target);
//         let value = parseInt(input.value);
//         input.value = value + 1;
//     });
// });
//
//
// const minusButtons = document.querySelectorAll(".minus");
//
// minusButtons.forEach((btn, index) => {
//     btn.addEventListener("click", () => {
//         const input = document.getElementById(btn.dataset.target);
//         let value = parseInt(input.value);
//         if ((index === 0 && value > 1) || (index !== 0 && value > 0)) {
//             input.value = value - 1;
//         }
//     });
// });


//Chọn phương thức thanh toán
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

// Hiện/ẩn thông tin tài khoản (card info)
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

//Hiện/ẩn số thẻ (card number)
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

//Chỉnh sửa các trường thông tin người dùng
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

//Hiệu ứng loading khi thanh toán
function showLoading(btn) {
    function showLoading(btn) {
        btn.disabled = true;
        btn.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Processing...`;
        // Simulate submit
        setTimeout(
            () => (btn.innerHTML = `<i class="fas fa-check me-2"></i>Paid`),
            1500
        );
    }
}

//Xử lý thanh toán (QR Code)
function handleCheckout(btn) {
    // Code
    const adultCount = parseInt(document.getElementById("adultInput").value) || 0;
    const childCount = parseInt(document.getElementById("childInput").value) || 0;
    const totalPeople = adultCount + childCount;

    if (totalPeople < window.minPeople) {
        Swal.fire({
            icon: 'warning',
            title: 'Không đủ số người',
            html: `Cần ít nhất <strong>${window.minPeople}</strong> người để đặt tour.`,
            confirmButtonColor: '#f59e0b'
        });
        return;
    }

    if (totalPeople > window.maxPeople) {
        Swal.fire({
            icon: 'error',
            title: 'Vượt quá số người cho phép',
            html: `Tối đa chỉ được <strong>${window.maxPeople}</strong> người trong 1 lượt đặt.`,
            confirmButtonColor: '#d33'
        });
        return;
    }

    btn.disabled = true;
    btn.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Processing...`;

    // Show QR code modal
    handlerevealQRCodeModal();

    setTimeout(() => {
        btn.innerHTML = `<i class="fas fa-credit-card me-2"></i>Check Out`;
        btn.disabled = false;


        const qrModal = new bootstrap.Modal(document.getElementById("qrModal"));
        qrModal.show();
    }, 1500);
}


function reloadQRCode() {
    handlerevealQRCodeModal();
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

//Tùy biến chọn ngày (ngày đi)
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
let currentMinPurchase = 0;

//Tải thông tin tour từ API
document.addEventListener("DOMContentLoaded", function () {
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
            document.getElementById("original-price").textContent = Math.round(tour.price).toLocaleString("vi-VN") + " VND";
            document.getElementById("tour-sub-title").textContent = tour.tourName;
            document.getElementById("tour-title-short-link").textContent = tour.tourName;
            // Gắn thêm nếu cần
            document.getElementById("place-name").textContent = tour.placeName;
            tourPrice = typeof tour.price === 'string'
                ? parseInt(tour.price.replace(/[^\d]/g, ''))
                : tour.price;
            const minPeople = tour.minPeople || 1; // fallback nếu API trả về null
            adultInput.value = minPeople;
            updatePrice();
            window.minPeople = minPeople;
            window.maxPeople = tour.maxPeople || 50;
        })
        .catch(error => {
            console.error("Đã xảy ra lỗi:", error);
        });

});

// khai báo biến để + và - adult và child
let adultCount = 0;
let childCount = 0;

const adultPlus = document.getElementById("adult-plus");
const adultMinus = document.getElementById("adult-minus");
const childPlus = document.getElementById("child-plus");
const childMinus = document.getElementById("child-minus");
const originalPriceElement = document.getElementById("original-price");
const adultInput = document.getElementById("adultInput");
const childInput = document.getElementById("childInput");
// console.log(originalPriceElement);


//Tính toán giá tiền & điều kiện áp dụng khuyến mãi
function updatePrice() {
    adultCount = Math.max(1, parseInt(document.getElementById("adultInput").value));
    childCount = Math.max(0, parseInt(document.getElementById("childInput").value));

    const totalPeople = adultCount + childCount;

    // ✅ Cảnh báo nếu tổng người < minPeople
    if (totalPeople < window.minPeople) {
        Swal.fire({
            icon: 'warning',
            title: 'Không đủ số người',
            html: `Cần ít nhất <strong>${window.minPeople}</strong> người để đặt tour.`,
            confirmButtonColor: '#f59e0b'
        });
    }

    // ✅ Cảnh báo nếu tổng người > maxPeople
    if (totalPeople > window.maxPeople) {
        Swal.fire({
            icon: 'error',
            title: 'Vượt quá số người cho phép',
            html: `Tối đa chỉ được <strong>${window.maxPeople}</strong> người trong 1 lượt đặt.`,
            confirmButtonColor: '#d33'
        });
    }

    let price = Math.round(tourPrice + (adultCount * tourPrice * 0.2) + (childCount * tourPrice * 0.15));
    originalPriceElement.textContent = price.toLocaleString("vi-VN") + " VND";

    checkMinPurchaseCondition();
    updateDiscountAmount();
    updateTotalAmount();
}

// Tính original price
function calculateOriginalPrice() {
    const adultCount = Math.max(1, parseInt(document.getElementById("adultInput").value));
    const childCount = Math.max(0, parseInt(document.getElementById("childInput").value));
    return Math.round(tourPrice + (adultCount * tourPrice * 0.2) + (childCount * tourPrice * 0.15));
}


// Các sự kiện + và - people
adultPlus.addEventListener("click", () => {
    adultInput.value = parseInt(adultInput.value) + 1;
    updatePrice();
});

adultMinus.addEventListener("click", () => {
    if (parseInt(adultInput.value) > 1) {
        adultInput.value = parseInt(adultInput.value) - 1;
        updatePrice();
    }
});

childPlus.addEventListener("click", () => {
    childInput.value = parseInt(childInput.value) + 1;
    updatePrice();
});

childMinus.addEventListener("click", () => {
    if (parseInt(childInput.value) > 0) {
        childInput.value = parseInt(childInput.value) - 1;
        updatePrice();
    }
});

// Bóa lỗi min_purchase xem chưa đủ dkien sử dụng promotion
function checkMinPurchaseCondition() {
    const priceText = document.getElementById("original-price").textContent;
    const price = parseInt(priceText.replace(/[^\d]/g, '')); // bỏ dấu . và VND

    const applyBtn = document.getElementById("applyPromotionBtn");
    if (!applyBtn) return;

    // Nếu không có mã nào đang áp dụng → chỉ cần cập nhật nút
    if (!selectedCode) {
        applyBtn.disabled = price < currentMinPurchase;
        return;
    }

    // Tìm thông tin mã hiện tại
    const promo = loadedPromotions.find(p => p.code === selectedCode);

    if (!promo) return;

    // Nếu không còn đủ điều kiện áp dụng mã
    if (price < promo.minPurchase) {
        Swal.fire({
            icon: 'error',
            title: 'Không đủ điều kiện áp dụng mã',
            html: `❌ <strong>Giá gốc (${price.toLocaleString()} VND)</strong> không đủ để sử dụng mã <strong>"${promo.code}"</strong>.<br>Yêu cầu tối thiểu: <strong>${promo.minPurchase.toLocaleString()} VND</strong>.`,
            confirmButtonColor: '#d33'
        });
        // Gỡ mã giảm giá
        const dropdownBtn = document.getElementById("promotionDropdownBtn");
        const hiddenInput = document.getElementById("selectedPromotionCode");

        dropdownBtn.innerHTML = "Chọn mã giảm giá";
        hiddenInput.value = "";
        selectedCode = null;
        currentMinPurchase = 0;

        // Bỏ tick tất cả checkbox trong dropdown
        const checkboxes = document.querySelectorAll("#promotionDropdownList input[type='checkbox']");
        checkboxes.forEach(cb => cb.checked = false);

        updateDiscountAmount();
    }

    // Cập nhật lại trạng thái nút
    applyBtn.disabled = price < currentMinPurchase;
}

// QR của Huy
function handlerevealQRCodeModal() {
    const token = localStorage.getItem('accessToken');
    const username = localStorage.getItem('username');
    const qrCanvas = document.getElementById("qr");


    if (!token || !username) {
        alert("Bạn chưa đăng nhập.");
        return;
    }


    // Thêm hiệu ứng loading
    qrCanvas.classList.add("qr-loading");


    // Lấy các giá trị từ HTML
    const tourTitle = document.querySelector('.card-summary h6').textContent.trim();
    const totalText = document.querySelector('.total-row span:last-child').textContent.trim();
    const amount = parseInt(totalText.replace(/[^\d]/g, ''));


    const body = {
        amount: amount,
        description: `Dat tour`
    };

    console.log(body);

    fetch('/tourify/api/payment/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify(body)
    })
        .then(res => res.json())
        .then(data => {
            // Xóa hiệu ứng loading
            qrCanvas.classList.remove("qr-loading");
            if (data.code === 1000 && data.result?.qrCode) {
                console.log(data);
                const qrCode = data.result.qrCode;
                // Tạo mã QR bằng QRious
                const qr = new QRious({
                    element: qrCanvas,
                    value: qrCode,
                    size: 256,
                    level: 'H'
                });
            } else {
                alert('Không thể tạo thanh toán. Vui lòng thử lại.');
            }
        })
        .catch(err => {
            qrCanvas.classList.remove("qr-loading");
            alert('Có lỗi xảy ra trong quá trình thanh toán.');
        });
}

function formatDateTime(isoString) {
    const date = new Date(isoString);
    return date.toLocaleString('vi-VN');
}


// Promotion
let loadedPromotions = [];
let selectedCode = null;

document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const tourId_booking = urlParams.get("id");

    fetch(`/tourify/api/promotions/${tourId_booking}`)
        .then(response => {
            if (!response.ok) throw new Error("Không thể fetch promotion.");
            return response.json();
        })
        .then(data => {
            if (data.code === 1000 && data.result.length > 0) {
                loadedPromotions = data.result;
                renderPromotionDropdown(loadedPromotions);
            } else {
                const btn = document.getElementById("promotionDropdownBtn");
                if (btn) btn.textContent = "Không có mã khuyến mãi";
            }
        })
        .catch(err => {
            console.error("Lỗi khi load promotion:", err);
            const btn = document.getElementById("promotionDropdownBtn");
            if (btn) btn.textContent = "Không thể tải mã";
        });

    // Kiểm tra nếu discountSelect tồn tại trước khi dùng
    const discountSelect = document.getElementById("discountSelect");
    if (discountSelect) {
        discountSelect.addEventListener("change", function () {
            const selectedCode = this.value;
            const promo = loadedPromotions.find(p => p.code === selectedCode);

            if (promo) {
                const currentMinPurchase = promo.minPurchase || 0;

                document.getElementById("promo-code").textContent = promo.code;
                document.getElementById("promo-discount").textContent = promo.discountPercent;
                document.getElementById("promo-quantity").textContent = promo.quantity;
                document.getElementById("promo-conditions").textContent = promo.conditions || "Không có";
                document.getElementById("promo-description").textContent = promo.description || "Không có";
                document.getElementById("promo-min-purchase").textContent = promo.minPurchase?.toLocaleString() || "0";
                document.getElementById("promo-time").textContent =
                    `${formatDateTime(promo.startTime)} → ${formatDateTime(promo.endTime)}`;

                const applyBtn = document.getElementById("applyPromotionBtn");
                const originalPrice = calculateOriginalPrice();

                if (applyBtn) {
                    if (originalPrice >= promo.minPurchase) {
                        applyBtn.disabled = false;
                        applyBtn.classList.remove("disabled", "btn-secondary");
                        applyBtn.classList.add("btn-danger");
                        applyBtn.title = "✨ Ưu đãi sắp biến mất – Dùng mã và đặt tour ngay!";
                    } else {
                        applyBtn.disabled = true;
                        applyBtn.classList.remove("btn-danger");
                        applyBtn.classList.add("disabled", "btn-secondary");
                        applyBtn.title = `❌ Cần tối thiểu ${promo.minPurchase.toLocaleString()} VND để dùng mã này`;
                    }
                }

                const modal = new bootstrap.Modal(document.getElementById("promotionModal"));
                modal.show();
            }
        });
    }

    //  Gán sự kiện cho nút "Áp dụng mã"
    const applyBtn = document.getElementById("applyPromotionBtn");
    if (applyBtn) {
        applyBtn.addEventListener("click", () => {
            const code = document.getElementById("promo-code").textContent;
            const promo = loadedPromotions.find(p => p.code === code);
            const originalPrice = calculateOriginalPrice();

            if (promo && originalPrice >= promo.minPurchase) {
                const select = document.getElementById("discountSelect");
                if (select) {
                    for (let i = 0; i < select.options.length; i++) {
                        if (select.options[i].value === code) {
                            select.selectedIndex = i;
                            break;
                        }
                    }

                    document.getElementById("checkoutBtn")?.scrollIntoView({behavior: "smooth"});

                    const modal = bootstrap.Modal.getInstance(document.getElementById("promotionModal"));
                    modal?.hide();

                    select.classList.add("border", "border-success", "border-2");
                    setTimeout(() => {
                        select.classList.remove("border", "border-success", "border-2");
                    }, 1500);
                }
            }
        });
    }
});


//Xử lý danh sách mã khuyến mãi (dropdown + modal)
function renderPromotionDropdown(promos) {
    const dropdownList = document.getElementById("promotionDropdownList");
    const dropdownBtn = document.getElementById("promotionDropdownBtn");
    const hiddenInput = document.getElementById("selectedPromotionCode");

    dropdownList.innerHTML = ""; // Clear
    hiddenInput.value = "";
    selectedCode = null;
    dropdownBtn.innerHTML = "Chọn mã giảm giá";

    promos.forEach((promo, index) => {
        const li = document.createElement("li");
        li.classList.add("dropdown-item", "d-flex", "justify-content-between", "align-items-center", "gap-2");

        // Tạo checkbox và label
        const checkboxId = `promo-check-${index}`;
        const checkboxHTML = `
            <div class="form-check mb-0 flex-grow-1">
                <input class="form-check-input" type="checkbox" value="${promo.code}" id="${checkboxId}">
                <label class="form-check-label w-100" for="${checkboxId}">
                    ${promo.code} - Giảm ${promo.discountPercent}%
                </label>
            </div>
        `;

        // Tạo nút xem chi tiết
        const detailBtn = document.createElement("button");
        detailBtn.classList.add("btn", "btn-outline-secondary", "btn-sm", "px-2", "py-1");
        detailBtn.innerHTML = `<i class="fas fa-info-circle"></i>`;
        detailBtn.title = "Xem chi tiết";

        // Xử lý hiển thị modal khi bấm nút "Chi tiết"
        detailBtn.addEventListener("click", (e) => {
            e.stopPropagation();

            // Gán thông tin vào modal
            document.getElementById("promo-code").textContent = promo.code;
            document.getElementById("promo-discount").textContent = promo.discountPercent;
            document.getElementById("promo-quantity").textContent = promo.quantity;
            document.getElementById("promo-conditions").textContent = promo.conditions || "Không có";
            document.getElementById("promo-description").textContent = promo.description || "Không có";
            document.getElementById("promo-min-purchase").textContent = promo.minPurchase?.toLocaleString() || "0";
            document.getElementById("promo-time").textContent =
                `${formatDateTime(promo.startTime)} → ${formatDateTime(promo.endTime)}`;

            const applyBtn = document.getElementById("applyPromotionBtn");
            const originalPrice = calculateOriginalPrice();

            // ✅ Kiểm tra min_purchase
            if (originalPrice >= promo.minPurchase) {
                applyBtn.disabled = false;
                applyBtn.title = "";
                applyBtn.classList.remove("disabled", "btn-secondary");
                applyBtn.classList.add("btn-danger");
            } else {
                applyBtn.disabled = true;
                applyBtn.title = `Cần tối thiểu ${promo.minPurchase.toLocaleString()} VND để dùng mã này`;
                applyBtn.classList.remove("btn-danger");
                applyBtn.classList.add("disabled", "btn-secondary");
            }

            // Mở modal
            const modal = new bootstrap.Modal(document.getElementById("promotionModal"));
            modal.show();
        });

        // Gắn HTML
        li.innerHTML = checkboxHTML;
        li.appendChild(detailBtn);

        // Checkbox logic
        const checkbox = li.querySelector("input");
        checkbox.addEventListener("change", function () {
            const originalPrice = calculateOriginalPrice();

            // Nếu không đạt điều kiện min_purchase
            if (originalPrice < promo.minPurchase) {
                Swal.fire({
                    icon: 'warning',
                    title: 'Không đủ điều kiện',
                    html: `Cần ít nhất <strong>${promo.minPurchase.toLocaleString()} VND</strong> để áp dụng mã này.`,
                    confirmButtonColor: '#f59e0b'
                });
                checkbox.checked = false;
                return;
            }

            // Nếu tick mã này
            if (checkbox.checked) {
                dropdownList.querySelectorAll("input[type='checkbox']").forEach(cb => {
                    if (cb !== checkbox) cb.checked = false;
                });

                dropdownBtn.innerHTML = `✔ ${promo.code} - Giảm ${promo.discountPercent}%`;
                hiddenInput.value = promo.code;
                selectedCode = promo.code;
                currentMinPurchase = promo.minPurchase || 0;

                checkMinPurchaseCondition();
                updateDiscountAmount();
            } else {
                dropdownBtn.innerHTML = "Chọn mã giảm giá";
                hiddenInput.value = "";
                selectedCode = null;
                currentMinPurchase = 0;
                checkMinPurchaseCondition();
                updateDiscountAmount();
            }
        });

        dropdownList.appendChild(li);
    });
}

//Cập nhật giá discount
function updateDiscountAmount() {
    const discountText = document.getElementById("promotionDropdownBtn")?.textContent;
    const discountPercentMatch = discountText?.match(/Giảm\s+(\d+)%/);
    const discountPercent = discountPercentMatch ? parseInt(discountPercentMatch[1]) : 0;

    const originalPrice = calculateOriginalPrice();
    const discountAmount = Math.round(originalPrice * discountPercent / 100);
    document.getElementById("discount-amount").textContent = "-" + discountAmount.toLocaleString("vi-VN") + " VND";
}

//Cập nhật giá total
function updateTotalAmount() {
    const originalText = document.getElementById("original-price").textContent;
    const discountText = document.getElementById("discount-amount").textContent;

    const original = parseInt(originalText.replace(/[^\d]/g, '')) || 0;
    const discount = parseInt(discountText.replace(/[^\d]/g, '')) || 0;

    const total = Math.max(0, Math.round(original - discount));
    document.getElementById("total-amount").textContent = total.toLocaleString("vi-VN") + " VND";

    console.log("🔍 originalText:", originalText);
    console.log("🔍 original:", original);
    console.log("🔍 discount:", discount);
    console.log("🔍 total:", total);


}
