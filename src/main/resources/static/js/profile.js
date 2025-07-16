document.addEventListener("DOMContentLoaded", () => {
    // Check if user is logged in
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
        // Redirect to login page if no access token found
        window.location.href = "/tourify/login";
        return;
    }

    // Fetch user data and populate profile
    fetchUserData(accessToken);

    const editIcon = document.getElementById("edit-icon");
    const fileInput = document.getElementById("avatar-input");
    const profilePic = document.getElementById("profile-pic");

    fileInput.addEventListener("change", async () => {
        const file = fileInput.files[0];
        if (!file) return;

        // Tạo URL tạm để preview ảnh mới
        const imageUrl = URL.createObjectURL(file);
        profilePic.src = imageUrl;

        // Giả sử bạn sẽ upload ảnh lên một dịch vụ lưu trữ và lấy về URL thực sự.
        // Trong ví dụ này ta dùng imageUrl làm demo
        const avatarUrl = imageUrl;

        const username = localStorage.getItem("username");
        if (!username) {
            showToast("Username not found in localStorage", "danger");
            return;
        }

        try {
            const response = await fetch(`/tourify/api/user/avatar?username=${username}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ avatar: avatarUrl })
            });

            if (response.ok) {
                showToast("Avatar updated successfully!", "success");
            } else {
                const errorText = await response.text();
                showToast("Failed to update avatar: " + errorText, "danger");
            }
        } catch (error) {
            console.error("Error updating avatar:", error);
            showToast("An error occurred while updating avatar.", "warning");
        }
    });
});



document.addEventListener("DOMContentLoaded", () => {
    const tabLinks = document.querySelectorAll("#profileTabs .nav-link");
    const tabPanes = document.querySelectorAll(".tab-pane");


    tabLinks.forEach((link) => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const tab = link.getAttribute("data-tab");


            tabLinks.forEach((l) => l.classList.remove("active"));
            link.classList.add("active");


            tabPanes.forEach((pane) => {
                pane.classList.remove("active");
                pane.style.display = "none";
            });


            const target = document.getElementById(`tab-${tab}`);
            target.classList.add("active");
            target.style.display = "block";
        });
    });
});

function editField(field) {
    document.getElementById(`${field}-display`).classList.add("d-none");
    if (field === 'password') {
        document.getElementById("password-edit-fields").classList.remove("d-none");
    } else {
        document.getElementById(`${field}-input`).classList.remove("d-none");
    }
    document.getElementById(`${field}-change`).classList.add("d-none");
    document.getElementById(`${field}-confirm`).classList.remove("d-none");
    document.getElementById(`${field}-cancel`).classList.remove("d-none");
}


function confirmField(field) {
    if (field === 'firstName' || field === 'lastName') {
        const newValue = document.getElementById(`${field}-input`).value;
        updateNameViaAPI(field, newValue);
    } else if (field === 'password') {
        changePasswordAPI();
    } else {
        const newValue = document.getElementById(`${field}-input`).value;
        updateFieldViaAPI(field, newValue);
    }
}

async function changePasswordAPI() {
    const accessToken = localStorage.getItem("accessToken");
    const username = localStorage.getItem("username");
    const oldPassword = document.getElementById("current-password-input").value;
    const newPassword = document.getElementById("new-password-input").value;
    const confirmPassword = document.getElementById("confirm-password-input").value;

    // Validate input
    if (!oldPassword || !newPassword || !confirmPassword) {
        showToast("Please fill in all password fields!", "info");
        return;
    }
    if (newPassword !== confirmPassword) {
        showToast("New password and confirm password do not match!", "warning");
        return;
    }
    if (newPassword.length < 6) {
        showToast("New password must be at least 6 characters.", "warning");
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/tourify/api/user/change-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${accessToken}`
            },
            body: JSON.stringify({
                username,
                oldPassword,
                newPassword,
                confirmPassword
            })
        });
        const data = await response.json();
        if (data.code === 200) {
            showToast("Password changed successfully! Logging out...", "success");
            setTimeout(() => {
                logoutAndRedirect();
            }, 1600); // chờ toast hiện xong rồi logout (1.6 giây, đủ đẹp)
            // Không cần cancelField('password') nữa, vì logout rồi
        } else {
            showToast(data.message || "Failed to change password.", "danger");
        }
    } catch (error) {
        showToast("Error changing password. Try again!", "danger");
        console.error(error);
    }
}

// Function to update name via API
async function updateNameViaAPI(field, newValue) {
    const accessToken = localStorage.getItem("accessToken");
    const username = localStorage.getItem("username");

    if (!accessToken || !username) {
        showToast("Authentication required. Please login again.", "warning");
        return;
    }

    try {
        // Get current values for both firstName and lastName
        const currentFirstName = document.getElementById("firstName-display").textContent;
        const currentLastName = document.getElementById("lastName-display").textContent;

        // Prepare the request body
        const requestBody = {
            firstName: field === 'firstName' ? newValue : currentFirstName,
            lastName: field === 'lastName' ? newValue : currentLastName
        };

        const response = await fetch(`/tourify/api/user/name?username=${username}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.code === 1000) {
                // Update the display
                document.getElementById(`${field}-display`).textContent = newValue;
                document.getElementById(`${field}-display`).classList.remove("d-none");
                document.getElementById(`${field}-input`).classList.add("d-none");
                document.getElementById(`${field}-confirm`).classList.add("d-none");
                document.getElementById(`${field}-cancel`).classList.add("d-none");
                document.getElementById(`${field}-change`).classList.remove("d-none");

                // Update the user name in header
                const fullName = `${requestBody.firstName} ${requestBody.lastName}`.trim();
                document.querySelector(".user-name").textContent = fullName;

                showToast("Name updated successfully!", "success");
            } else {
                showToast("Failed to update name: " + (data.message || "Unknown error"), "danger");
            }
        } else {
            const errorText = await response.text();
            showToast("Failed to update name: " + errorText, "danger");
        }
    } catch (error) {
        console.error("Error updating name:", error);
        showToast("An error occurred while updating name.", "danger");
    }
}

// Function to update other fields via API
async function updateFieldViaAPI(field, newValue) {
    const accessToken = localStorage.getItem("accessToken");
    const username = localStorage.getItem("username");

    if (!accessToken || !username) {
        showToast("Authentication required. Please login again.", "warning");
        return;
    }

    try {
        // Map field names to API endpoints
        const fieldMapping = {
            'email': 'email',
            'phone': 'phone',
            'address': 'address',
            'dob': 'dob',
            'password': 'password'
        };

        const apiEndpoint = fieldMapping[field];
        if (!apiEndpoint) {
            showToast("Unknown field: " + field, "info");
            return;
        }

        // Prepare request body based on field type
        let requestBody = {};
        if (field === 'password') {
            requestBody = { password: newValue };
        } else if (field === 'dob') {
            // Convert date format if needed
            requestBody = { dob: newValue };
        } else {
            requestBody = { [field]: newValue };
        }

        const response = await fetch(`/tourify/api/user/${apiEndpoint}?username=${username}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.code === 1000) {
                // Update the display
                document.getElementById(`${field}-display`).textContent = newValue;
                document.getElementById(`${field}-display`).classList.remove("d-none");
                document.getElementById(`${field}-input`).classList.add("d-none");
                document.getElementById(`${field}-confirm`).classList.add("d-none");
                document.getElementById(`${field}-cancel`).classList.add("d-none");
                document.getElementById(`${field}-change`).classList.remove("d-none");

                // Special handling for password field
                if (field === 'password') {
                    document.getElementById(`${field}-display`).textContent = '************';
                    document.getElementById(`${field}-input`).value = '';
                }

                showToast(`${field.charAt(0).toUpperCase() + field.slice(1)} updated successfully!`, "success");
            } else {
                showToast(`Failed to update ${field}: ` + (data.message || "Unknown error"), "warning");
            }
        } else {
            const errorText = await response.text();
            showToast(`Failed to update ${field}: ` + errorText, "danger");
        }
    } catch (error) {
        console.error(`Error updating ${field}:`, error);
        showToast(`An error occurred while updating ${field}.`, "warning");
    }
}

function cancelField(field) {
    if (field === 'password') {
        document.getElementById("password-edit-fields").classList.add("d-none");
        // Xóa sạch value các ô password
        document.getElementById("current-password-input").value = "";
        document.getElementById("new-password-input").value = "";
        document.getElementById("confirm-password-input").value = "";
    } else {
        document.getElementById(`${field}-input`).classList.add("d-none");
        document.getElementById(`${field}-input`).value = document.getElementById(`${field}-display`).textContent;
    }
    document.getElementById(`${field}-display`).classList.remove("d-none");
    document.getElementById(`${field}-confirm`).classList.add("d-none");
    document.getElementById(`${field}-cancel`).classList.add("d-none");
    document.getElementById(`${field}-change`).classList.remove("d-none");
}

function showCoverModal() {
    const modal = new bootstrap.Modal(
        document.getElementById("coverModal")
    );
    modal.show();
}

function updateCoverImage() {
    const urlInput = document.getElementById("coverImageUrlInput").value;
    const fileInput = document.getElementById("coverFileInput").files[0];


    if (fileInput) {
        const reader = new FileReader();
        reader.onload = function (e) {
            document.querySelector(
                ".profile-header"
            ).style.backgroundImage = `url('${e.target.result}')`;
        };
        reader.readAsDataURL(fileInput);
    } else if (urlInput) {
        document.querySelector(
            ".profile-header"
        ).style.backgroundImage = `url('${urlInput}')`;
    }


    // Đóng modal
    const modal = bootstrap.Modal.getInstance(
        document.getElementById("coverModal")
    );
    modal.hide();
}

function showFullAvatar() {
    const modal = document.getElementById("avatarFullModal");
    const fullImg = document.getElementById("full-avatar-image");
    fullImg.src = document.getElementById("profile-pic").src;
    modal.style.display = "flex"; // <-- hiển thị ở đây thôi
}


function closeFullAvatar() {
    document.getElementById("avatarFullModal").style.display = "none";
}


let avatarFromFile = false;


function showAvatarModal() {
    avatarFromFile = false;
    document.getElementById("avatarImageUrlInput").value = "";
    document.getElementById("avatarPreview").style.display = "none";
    document.getElementById("fileInputWrapper").innerHTML = "";


    const modal = new bootstrap.Modal(
        document.getElementById("avatarModal")
    );
    modal.show();
}


function triggerAvatarFileInput() {
    const wrapper = document.getElementById("fileInputWrapper");
    wrapper.innerHTML = ""; // Xóa input cũ nếu có
    wrapper.style.display = "block";

    const input = document.createElement("input");
    input.type = "file";
    input.accept = "image/*";
    input.id = "avatarFileInput";           // <-- Gán id ở đây
    input.style.display = "none";           // Ẩn input

    input.addEventListener("change", loadAvatarFromFile);

    wrapper.appendChild(input);
    input.click(); // Mở dialog chọn file
}

function loadAvatarFromFile(event) {
    const file = event.target.files[0];
    // console.log(file);
    if (file && file.type.startsWith("image/")) {
        avatarFromFile = true;
        const reader = new FileReader();
        reader.onload = function (e) {
            const img = e.target.result;
            // console.log(img);
            document.getElementById("avatarPreview").src = img;
            document.getElementById("avatarPreview").style.display = "block";
            document.getElementById("profile-pic").src = img;
        };
        reader.readAsDataURL(file);
    }
}


function updateAvatar() {
    const url = document.getElementById("avatarImageUrlInput").value.trim();
    const preview = document.getElementById("avatarPreview");
    const fileInput = document.getElementById("avatarFileInput");
    const token = localStorage.getItem("accessToken");

    function setAvatarAndClose(newUrl) {
        // console.log(newUrl);
        document.getElementById("profile-pic").src = newUrl;
        preview.src = newUrl;
        preview.style.display = "block";
        // Gọi API đổi avatar
        fetch("/tourify/api/user/avatar", {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ avatar: newUrl })
        })
            .then(res => res.json())
            .then(() => {
                // Đóng modal sau khi cập nhật
                const modal = bootstrap.Modal.getInstance(document.getElementById("avatarModal"));
                modal.hide();
            });
    }

    // console.log(fileInput.files[0]);
    if (fileInput && fileInput.files && fileInput.files[0]) {
        // Nếu có file, upload lên Cloudinary trước
        const formData = new FormData();
        formData.append("file", fileInput.files[0]);
        // console.log(formData);
        fetch("/tourify/api/upload", {
            method: "POST",
            body: formData
        })
            .then(res => res.json())
            .then(data => {
                if (data && data.result && data.result.url) {
                    setAvatarAndClose(data.result.url);
                }
            });
    } else if (url) {
        setAvatarAndClose(url);
    }
}

// Function to fetch user data from API
async function fetchUserData(accessToken) {
    try {
        const response = await fetch('/tourify/api/auth/me', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user data');
        }

        const data = await response.json();

        if (data.code === 1000 && data.result) {
            const user = data.result;

            // Populate user information
            populateUserProfile(user);
        } else {
            console.error('Failed to get user data:', data.message);
        }
    } catch (error) {
        console.error('Error fetching user data:', error);
    }
}

// Function to populate profile with user data
function populateUserProfile(user) {
    // Update profile picture
    if (user.avatar) {
        document.getElementById("profile-pic").src = user.avatar;
    }

    // Update user name in header
    const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.userName;
    document.querySelector(".user-name").textContent = fullName;

    // Update navbar user name
    const navbarUserSpan = document.querySelector("#userMenu span span");
    if (navbarUserSpan) {
        navbarUserSpan.textContent = user.userName || "User";
    }

    // Update account fields
    updateField('firstName', user.firstName || 'Not set');
    updateField('lastName', user.lastName || 'Not set');
    updateField('email', user.email || '');
    updateField('phone', user.phoneNumber || 'Not set');
    updateField('address', user.address || 'Not set');

    // Update DOB if available
    if (user.dob) {
        const dobDate = new Date(user.dob);
        const formattedDob = dobDate.toLocaleDateString('en-GB'); // DD/MM/YYYY format
        const inputDob = dobDate.toISOString().split('T')[0]; // YYYY-MM-DD format for input
        updateField('dob', formattedDob, inputDob);
    } else {
        updateField('dob', 'Not set', '');
    }
}

// Function to update a field with new value
function updateField(fieldName, displayValue, inputValue = null) {
    const displayElement = document.getElementById(`${fieldName}-display`);
    const inputElement = document.getElementById(`${fieldName}-input`);

    if (displayElement) {
        displayElement.textContent = displayValue;
    }

    if (inputElement && inputValue !== null) {
        inputElement.value = inputValue;
    } else if (inputElement) {
        inputElement.value = displayValue;
    }
}

//Load CreditCard
document.addEventListener("DOMContentLoaded", () => {
    loadCreditCards();

    // Xử lý submit form thêm thẻ
    document.getElementById("addCreditCardForm").addEventListener("submit", async function (e) {
        e.preventDefault();
        await addCreditCard();
    });
});

async function loadCreditCards() {
    const accessToken = localStorage.getItem("accessToken");
    const listDiv = document.getElementById("creditcard-list");
    listDiv.innerHTML = '<div class="text-center text-muted">Đang tải...</div>';

    try {
        const res = await fetch("/tourify/api/user/creditcard", {
            headers: { "Authorization": `Bearer ${accessToken}` }
        });
        const data = await res.json();

        if (data.result && data.result.length > 0) {
            listDiv.innerHTML = data.result.map(card => `
                <div class="credit-card-ui position-relative shadow rounded-4 mb-4 p-3" style="background: linear-gradient(135deg, #e0eafc 0% 0%, #2d7d6f 100%); min-width: 320px; max-width: 400px; margin: 0 auto;">
                  <button class="btn btn-danger btn-sm position-absolute top-0 end-0 m-2 delete-card-btn" title="Xóa thẻ" onclick="deleteCreditCard('${card.cardID}')">
                    <i class="bi bi-trash"></i>
                  </button>
                  <div class="logo"></div>
                  <div class="chip">
                    <svg viewBox="0 0 40 26" fill="none" xmlns="http://www.w3.org/2000/svg" width="40" height="26">
                      <rect width="40" height="26" rx="6" fill="#eee9" />
                      <rect x="7" y="7" width="26" height="12" rx="3" fill="#ccc9" />
                      <rect x="13" y="11" width="14" height="4" rx="1" fill="#bbb7" />
                    </svg>
                  </div>
                  <div class="cc-type mb-2">
                    <i class="bi bi-credit-card"></i>
                    ${card.cardType || "Khác"}
                    <span class="wave ms-1"><i class="bi bi-wifi"></i></span>
                  </div>
                  <div class="cc-label">Số thẻ</div>
                  <div class="cc-number fs-5 fw-bold">${formatCardNumber(card.cardNumber)}</div>
                  <div class="row mt-2">
                    <div class="col-7">
                      <div class="cc-label">Chủ thẻ</div>
                      <div class="cc-holder">${card.cardHolderName}</div>
                    </div>
                    <div class="col-5 text-start">
                      <div class="cc-label">Hết hạn</div>
                      <div class="cc-expiry">${card.expiryTime ? formatExpiry(card.expiryTime) : "Không có"}</div>
                    </div>
                  </div>
                  <div class="cc-icon position-absolute bottom-0 end-0 m-2"><i class="bi bi-shield-lock"></i></div>
                </div>
            `).join("");

            listDiv.innerHTML += `
                <div class="credit-card-ui add-credit-card-card shadow rounded-4 mb-4 p-3 d-flex flex-column align-items-center justify-content-center" onclick="showAddCreditCardModal()" style="cursor: pointer; background: #f8fafc; min-width: 320px; max-width: 400px; margin: 0 auto; border: 2px dashed #b6c2d1;">
                    <div class="cc-type mb-2">
                        <i class="bi bi-plus-circle"></i> <span class="fw-semibold">Thêm thẻ mới</span>
                    </div>
                    <div class="cc-label">Số thẻ</div>
                    <div class="cc-number text-muted">•••• •••• •••• ••••</div>
                    <div class="row mt-4 w-100">
                        <div class="col-7">
                            <div class="cc-label">Chủ thẻ</div>
                            <div class="cc-holder text-muted">Chưa có</div>
                        </div>
                        <div class="col-5 text-start">
                            <div class="cc-label">Hết hạn</div>
                            <div class="cc-expiry text-muted">__/__</div>
                        </div>
                    </div>
                    <div class="cc-icon position-absolute bottom-0 end-0 m-2"><i class="bi bi-credit-card-2-front"></i></div>
                </div>
            `
        } else {
            listDiv.innerHTML = `
                <div class="credit-card-ui add-credit-card-card shadow rounded-4 mb-4 p-3 d-flex flex-column align-items-center justify-content-center" onclick="showAddCreditCardModal()" style="cursor: pointer; background: #f8fafc; min-width: 320px; max-width: 400px; margin: 0 auto; border: 2px dashed #b6c2d1;">
                    <div class="cc-type mb-2">
                        <i class="bi bi-plus-circle"></i> <span class="fw-semibold">Thêm thẻ mới</span>
                    </div>
                    <div class="cc-label">Số thẻ</div>
                    <div class="cc-number text-muted">•••• •••• •••• ••••</div>
                    <div class="row mt-4 w-100">
                        <div class="col-7">
                            <div class="cc-label">Chủ thẻ</div>
                            <div class="cc-holder text-muted">Chưa có</div>
                        </div>
                        <div class="col-5 text-start">
                            <div class="cc-label">Hết hạn</div>
                            <div class="cc-expiry text-muted">__/__</div>
                        </div>
                    </div>
                    <div class="cc-icon position-absolute bottom-0 end-0 m-2"><i class="bi bi-credit-card-2-front"></i></div>
                </div>
            `
        }
    } catch (err) {
        listDiv.innerHTML = '<div class="text-danger">Không thể tải danh sách thẻ.</div>';
    }
}

function formatCardNumber(num) {
    if (!num) return '';
    return num.replace(/(.{4})/g, '$1 ').trim();
}

function formatExpiry(expiry) {
    // Nếu là yyyy-MM-ddTHH:mm:ss thì lấy yyyy-MM
    if (expiry.length >= 7) return expiry.slice(0, 7);
    return expiry;
}

function showAddCreditCardModal() {
    const modal = new bootstrap.Modal(document.getElementById("addCreditCardModal"));
    document.getElementById("addCreditCardForm").reset();
    modal.show();
}

async function addCreditCard() {
    const accessToken = localStorage.getItem("accessToken");
    const form = document.getElementById("addCreditCardForm");
    const cardNumber = form.cardNumber.value;
    const cardHolderName = form.cardHolderName.value;
    const expiryTime = form.expiryTime.value ? form.expiryTime.value : null;
    const cardType = form.cardType.value;

    try {
        const res = await fetch("/tourify/api/user/creditcard", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${accessToken}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ cardNumber, cardHolderName, expiryTime, cardType })
        });
        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById("addCreditCardModal")).hide();
            await loadCreditCards();
        } else {
            showToast("Thêm thẻ thất bại!", "info");
        }
    } catch (err) {
        showToast("Có lỗi khi thêm thẻ!", "warning");
    }
}

function showToast(message, type = "success") {
    // type: "success", "danger", "info", "warning"
    const toast = document.getElementById("toastNotify");
    const toastBody = document.getElementById("toastNotifyBody");

    // Icon cho từng loại
    let icon = "";
    if (type === "success") icon = "✅ ";
    if (type === "danger") icon = "❌ ";
    if (type === "info") icon = "ℹ️ ";
    if (type === "warning") icon = "⚠️ ";

    toastBody.innerHTML = `<span style="font-size:1.2em;margin-right:6px;">${icon}</span>${message}`;

    // Đổi màu nền
    toast.className = "toast align-items-center text-white border-0";
    if (type === "success") toast.classList.add("bg-success");
    if (type === "danger") toast.classList.add("bg-danger");
    if (type === "info") toast.classList.add("bg-info");
    if (type === "warning") toast.classList.add("bg-warning");

    // Hiện toast
    const bsToast = new bootstrap.Toast(toast, { delay: 2500 });
    bsToast.show();
}

function logoutAndRedirect() {
    // Xóa localStorage hoặc token tuỳ dự án
    localStorage.removeItem("accessToken");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    localStorage.removeItem("tourify_chat_history");
    // ...xóa các key khác nếu có
    window.location.href = "/tourify/login";
}

// Hàm xóa thẻ
window.deleteCreditCard = async function (cardId) {
    const accessToken = localStorage.getItem("accessToken");
    if (!cardId) return;
    const confirm = await Swal.fire({
        title: 'Xóa thẻ này?',
        text: 'Bạn có chắc chắn muốn xóa thẻ này không?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });
    if (confirm.isConfirmed) {
        try {
            const res = await fetch(`/tourify/api/user/creditcard/${cardId}`, {
                method: 'DELETE',
                headers: { "Authorization": `Bearer ${accessToken}` }
            });
            if (res.ok) {
                Swal.fire('Đã xóa!', 'Thẻ đã được xóa thành công.', 'success');
                loadCreditCards();
            } else {
                Swal.fire('Lỗi', 'Không thể xóa thẻ. Vui lòng thử lại.', 'error');
            }
        } catch (err) {
            Swal.fire('Lỗi', 'Không thể xóa thẻ. Vui lòng thử lại.', 'error');
        }
    }
}

// ========== BOOKING HISTORY FILTER =============
let allBookings = [];
let bookingFilter = 'upcoming'; // 'upcoming' hoặc 'past'

document.addEventListener("DOMContentLoaded", () => {
    const historyTab = document.querySelector('[data-tab="history"]');
    if (historyTab) {
        historyTab.addEventListener("shown.bs.tab", () => {
            fetchAndRenderBookings();
        });
        historyTab.addEventListener("click", () => {
            setTimeout(fetchAndRenderBookings, 100);
        });
    }
    // Filter button group
    const filterGroup = document.getElementById('bookingFilterGroup');
    if (filterGroup) {
        filterGroup.addEventListener('click', function (e) {
            if (e.target.closest('button[data-filter]')) {
                const btn = e.target.closest('button[data-filter]');
                filterGroup.querySelectorAll('button').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                bookingFilter = btn.getAttribute('data-filter');
                renderFilteredBookings();
            }
        });
    }
});

async function fetchAndRenderBookings() {
    const accessToken = localStorage.getItem("accessToken");
    const bookingListDiv = document.getElementById("booking-list");
    const noBookingDiv = document.getElementById("no-booking-message");
    bookingListDiv.innerHTML = '';
    noBookingDiv.style.display = 'none';
    allBookings = [];
    try {
        const response = await fetch("/tourify/api/user/booking", {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });
        const data = await response.json();
        if (data.code === 1000 && Array.isArray(data.result)) {
            allBookings = data.result;
        }
    } catch (err) {
        // Nếu lỗi thì allBookings vẫn rỗng
    }
    bookingFilter = 'upcoming'; // reset về upcoming mỗi lần vào tab
    // Set lại active cho filter
    const filterGroup = document.getElementById('bookingFilterGroup');
    if (filterGroup) {
        filterGroup.querySelectorAll('button').forEach(b => b.classList.remove('active'));
        filterGroup.querySelector('button[data-filter="upcoming"]').classList.add('active');
    }
    renderFilteredBookings();
}

function renderFilteredBookings() {
    const bookingListDiv = document.getElementById("booking-list");
    const noBookingDiv = document.getElementById("no-booking-message");
    bookingListDiv.innerHTML = '';
    noBookingDiv.style.display = 'none';
    const now = new Date();
    let filtered = [];
    if (bookingFilter === 'upcoming') {
        filtered = allBookings.filter(b => {
            const start = new Date(b.dayStart);
            return start > now && b.status.toUpperCase() !== 'CANCELLED' && b.status.toUpperCase() !== 'SUCCESS';
        });
        // Sắp xếp tăng dần theo dayStart (gần nhất lên đầu)
        filtered.sort((a, b) => new Date(a.dayStart) - new Date(b.dayStart));
    } else {
        filtered = allBookings.filter(b => {
            const end = new Date(b.dayEnd);
            const status = b.status.toUpperCase();
            return status === 'CANCELLED' || status === 'SUCCESS' || end < now;
        });
        // Sắp xếp giảm dần theo dayEnd (gần nhất lên đầu)
        filtered.sort((a, b) => new Date(b.dayEnd) - new Date(a.dayEnd));
    }
    if (filtered.length > 0) {
        filtered.forEach(booking => {
            const card = createBookingCard(booking);
            bookingListDiv.appendChild(card);
        });
    } else {
        // Render UI đẹp tuỳ filter
        if (bookingFilter === 'upcoming') {
            noBookingDiv.innerHTML = `
                <i class="fas fa-suitcase-rolling fa-3x mb-3" style="color:var(--primary-color)"></i>
                <h5 class="mb-2" style="color:var(--primary-color)">Bạn chưa có chuyến đi sắp tới</h5>
                <p class="text-muted mb-3">Khám phá các tour hấp dẫn và lên kế hoạch cho chuyến đi tiếp theo của bạn!</p>
                <a href="/tourify/tour-list" class="btn btn-success px-4 py-2" style="background: linear-gradient(90deg, #16b686 60%, #b8ead2 100%); border:none; font-weight:600;">Khám phá tour ngay</a>
            `;
        } else {
            noBookingDiv.innerHTML = `
                <i class="fas fa-history fa-3x mb-3" style="color:var(--secondary-color)"></i>
                <h5 class="mb-2" style="color:var(--secondary-color)">Chưa có lịch sử chuyến đi</h5>
                <p class="text-muted mb-3">Các chuyến đi đã hoàn thành hoặc bị huỷ sẽ hiển thị tại đây.</p>
            `;
        }
        noBookingDiv.style.display = 'block';
    }
}

function createBookingCard(booking) {
    const start = new Date(booking.dayStart);
    const end = new Date(booking.dayEnd);
    const created = new Date(booking.createdAt);
    const dateOptions = { year: 'numeric', month: 'short', day: 'numeric' };
    const startStr = start.toLocaleDateString(undefined, dateOptions);
    const endStr = end.toLocaleDateString(undefined, dateOptions);
    const createdStr = created.toLocaleDateString(undefined, dateOptions);

    let statusColor = 'secondary';
    if (booking.status.toUpperCase() === 'PAID') statusColor = 'success';
    else if (booking.status.toUpperCase() === 'PENDING') statusColor = 'warning';
    else if (booking.status.toUpperCase() === 'CANCELLED') statusColor = 'danger';
    else if (booking.status.toUpperCase() === 'SUCCESS') statusColor = 'success';

    // Nếu SUCCESS thì thêm 2 nút: Feedback + Re-book
    let extraButtonsHTML = '';
    let feedbackModalHTML = '';
    if (booking.status.toUpperCase() === 'SUCCESS') {
        extraButtonsHTML = `
            <div class="d-flex justify-content-between mt-3">
                <button type="button"
                    class="btn btn-outline-primary btn-sm"
                    data-bs-toggle="modal"
                    data-bs-target="#feedbackModal-${booking.bookingId}">
                    <i class="fas fa-comment-dots me-1"></i> Feedback
                </button>
                <a href="/tourify/tour-detail/${booking.tourId}" class="btn btn-outline-success btn-sm">
                    <i class="fas fa-redo me-1"></i> Re-book
                </a>
            </div>
        `;
        feedbackModalHTML = `
        <!-- Modal feedback riêng cho booking này -->
<div class="modal fade" id="feedbackModal-${booking.bookingId}" tabindex="-1" aria-labelledby="feedbackModalLabel-${booking.bookingId}" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <form class="modal-content feedback-form" data-tourid="${booking.tourId}">
      <div class="modal-header">
        <h5 class="modal-title" id="feedbackModalLabel-${booking.bookingId}">Feedback for ${booking.tourName}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <div class="mb-3">
          <label class="form-label">Title</label>
          <input type="text" name="title" class="form-control" required minlength="5" maxlength="100">
        </div>
        <div class="mb-3">
          <label class="form-label">Content</label>
          <textarea name="content" class="form-control" required minlength="10" maxlength="1000"></textarea>
        </div>
        <div class="mb-3">
          <label class="form-label">Rating</label>
          <div class="star-rating d-flex align-items-center gap-1" style="font-size:2em;">
              <i class="fa-regular fa-star" data-value="1"></i>
              <i class="fa-regular fa-star" data-value="2"></i>
              <i class="fa-regular fa-star" data-value="3"></i>
              <i class="fa-regular fa-star" data-value="4"></i>
              <i class="fa-regular fa-star" data-value="5"></i>
              <input type="hidden" name="rating" value="0" required>
              <span class="ms-2 rating-label text-muted" style="font-size:1em;"></span>
            </div>
        </div>
        <div class="alert alert-success d-none"></div>
        <div class="alert alert-danger d-none"></div>
      </div>
      <div class="modal-footer">
        <button type="submit" class="btn btn-primary">Submit Feedback</button>
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
      </div>
    </form>
  </div>
</div>

        `;
    }

    const col = document.createElement('div');
    col.className = 'col-12 col-md-6 col-lg-4';
    col.innerHTML = `
    <div class="card h-100 shadow-sm border-0" style="border-radius: 18px;">
        <img src="${booking.thumbnail}" class="card-img-top" alt="Tour Thumbnail" style="height:180px;object-fit:cover;border-radius: 18px 18px 0 0;">
        <div class="card-body d-flex flex-column">
            <h5 class="card-title mb-2" style="color: var(--primary-color); font-weight:700;">${booking.tourName}</h5>
            <div class="mb-2"><span class="badge bg-${statusColor}" style="font-size:0.95em;">${booking.status.toUpperCase()}</span></div>
            <div class="mb-2"><i class="fas fa-calendar-alt me-1" style="color:var(--secondary-color)"></i> <span>${startStr} - ${endStr}</span></div>
            <div class="mb-2"><i class="fas fa-users me-1" style="color:var(--secondary-color)"></i> <span>${booking.adultNumber} adults, ${booking.childNumber} children</span></div>
            <div class="mb-2"><i class="fas fa-money-bill-wave me-1" style="color:var(--secondary-color)"></i> <span style="font-weight:600; color:var(--secondary-color)">${booking.totalPrice.toLocaleString()} đ</span></div>
            <div class="mt-auto text-end"><small class="text-muted">Booked: ${createdStr}</small></div>
            ${extraButtonsHTML}
        </div>
        ${feedbackModalHTML}
    </div>
    `;
    return col;
}


document.addEventListener('DOMContentLoaded', function() {
    // Chờ render xong booking cards thì mới gắn sự kiện
    document.body.addEventListener('submit', async function(e) {
        if (e.target.matches('.feedback-form')) {
            e.preventDefault();
            const form = e.target;
            const tourId = form.getAttribute('data-tourid');
            const title = form.querySelector('[name="title"]').value.trim();
            const content = form.querySelector('[name="content"]').value.trim();
            const rating = form.querySelector('[name="rating"]').value;
            const token = localStorage.getItem('accessToken');
            const successMsg = form.querySelector('.alert-success');
            const errorMsg = form.querySelector('.alert-danger');
            successMsg.classList.add('d-none');
            errorMsg.classList.add('d-none');

            if (!token) {
                errorMsg.textContent = "Bạn phải đăng nhập để gửi feedback.";
                errorMsg.classList.remove('d-none');
                return;
            }
            try {
                const resp = await fetch('/tourify/api/feedbacks', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({ tourId, title, content, rating })
                });
                if (resp.ok) {
                    successMsg.textContent = "Gửi feedback thành công! Cảm ơn bạn đã đánh giá.";
                    successMsg.classList.remove('d-none');
                    setTimeout(() => {
                        const modal = bootstrap.Modal.getInstance(form.closest('.modal'));
                        modal.hide();
                        form.reset();
                    }, 1000);
                } else {
                    const errorData = await resp.json();
                    errorMsg.textContent = errorData.message || "Gửi feedback thất bại!";
                    errorMsg.classList.remove('d-none');
                }
            } catch (err) {
                errorMsg.textContent = "Lỗi kết nối server, vui lòng thử lại!";
                errorMsg.classList.remove('d-none');
            }
        }
    });
});

function initStarRating(modal) {
    modal.querySelectorAll('.star-rating').forEach(starDiv => {
        const stars = starDiv.querySelectorAll('i[data-value]');
        const input = starDiv.querySelector('input[name="rating"]');
        const label = starDiv.querySelector('.rating-label');
        const ratingTexts = ["", "Rất tệ", "Tệ", "Bình thường", "Tốt", "Xuất sắc"];
        let selected = 0;

        stars.forEach(star => {
            // Hover effect
            star.addEventListener('mouseenter', () => {
                const val = parseInt(star.getAttribute('data-value'));
                stars.forEach((s, idx) => {
                    s.className = idx < val ? 'fa-solid fa-star text-warning' : 'fa-regular fa-star';
                });
                label.textContent = ratingTexts[val];
            });
            // Rời khỏi
            star.addEventListener('mouseleave', () => {
                stars.forEach((s, idx) => {
                    s.className = idx < selected ? 'fa-solid fa-star text-warning' : 'fa-regular fa-star';
                });
                label.textContent = selected > 0 ? ratingTexts[selected] : "";
            });
            // Click chọn
            star.addEventListener('click', () => {
                selected = parseInt(star.getAttribute('data-value'));
                input.value = selected;
                stars.forEach((s, idx) => {
                    s.className = idx < selected ? 'fa-solid fa-star text-warning' : 'fa-regular fa-star';
                });
                label.textContent = ratingTexts[selected];
            });
        });
    });
}

// Gọi hàm này mỗi khi mở modal feedback (hoặc sau khi render modal động)
document.addEventListener('shown.bs.modal', function(e) {
    if (e.target.classList.contains('modal')) {
        initStarRating(e.target);
    }
});
