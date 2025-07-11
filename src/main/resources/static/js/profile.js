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
                <div class="credit-card-ui">
  <div class="logo"></div>
  <div class="chip">
    <!-- Chip SVG -->
    <svg viewBox="0 0 40 26" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="40" height="26" rx="6" fill="#eee9" />
      <rect x="7" y="7" width="26" height="12" rx="3" fill="#ccc9" />
      <rect x="13" y="11" width="14" height="4" rx="1" fill="#bbb7" />
    </svg>
  </div>
  <div class="cc-type">
    <i class="bi bi-credit-card"></i>
    ${card.cardType || "Khác"}
    <span class="wave ms-1"><i class="bi bi-wifi"></i></span>
  </div>
  <div class="cc-label">Số thẻ</div>
  <div class="cc-number">${formatCardNumber(card.cardNumber)}</div>
  <div class="row" style="margin-top: 10px;">
    <div>
      <div class="cc-label">Chủ thẻ</div>
      <div class="cc-holder">${card.cardHolderName}</div>
    </div>
    <div class="text-start">
      <div class="cc-label">Hết hạn</div>
      <div class="cc-expiry">${card.expiryTime ? formatExpiry(card.expiryTime) : "Không có"}</div>
</div>
  </div>
  <div class="cc-icon"><i class="bi bi-shield-lock"></i></div>
</div>
            `).join("");

            listDiv.innerHTML += `
                            <div class="credit-card-ui add-credit-card-card" onclick="showAddCreditCardModal()" style="cursor: pointer; background: #aaa;">
                    <div class="cc-type">
                        <i class="bi bi-plus-circle"></i> Thêm thẻ mới
                    </div>
                    <div class="cc-label">Số thẻ</div>
                    <div class="cc-number text-muted">•••• •••• •••• ••••</div>
                    <div class="row" style="margin-top: 35px">
                        <div class="col-7">
                            <div class="cc-label">Chủ thẻ</div>
                            <div class="cc-holder text-muted">Chưa có</div>
                        </div>
                        <div class="col-5 text-start">
                            <div class="cc-label">Hết hạn</div>
                            <div class="cc-expiry text-muted">__/__</div>
                        </div>
                    </div>
                    <div class="cc-icon"><i class="bi bi-credit-card-2-front"></i></div>
                </div>

            `
        } else {
            listDiv.innerHTML = `
                            <div class="credit-card-ui add-credit-card-card" onclick="showAddCreditCardModal()" style="cursor: pointer; background: #aaa;">
                    <div class="cc-type">
                        <i class="bi bi-plus-circle"></i> Thêm thẻ mới
                    </div>
                    <div class="cc-label">Số thẻ</div>
                    <div class="cc-number text-muted">•••• •••• •••• ••••</div>
                    <div class="row" style="margin-top: 35px">
                        <div class="col-7">
                            <div class="cc-label">Chủ thẻ</div>
                            <div class="cc-holder text-muted">Chưa có</div>
                        </div>
                        <div class="col-5 text-start">
                            <div class="cc-label">Hết hạn</div>
                            <div class="cc-expiry text-muted">__/__</div>
                        </div>
                    </div>
                    <div class="cc-icon"><i class="bi bi-credit-card-2-front"></i></div>
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
