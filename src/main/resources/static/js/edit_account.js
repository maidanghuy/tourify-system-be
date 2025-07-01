// edit_account.js

// Hiện toast (dùng chung, style giống profile.html)
// Toast đẹp hơn!
function showToast(message, type = "success") {
    const toast = document.getElementById("toastNotify");
    const toastBody = document.getElementById("toastNotifyBody");

    // Reset class
    toast.className = "toast align-items-center border-0 shadow-lg show";
    toastBody.className = "d-flex align-items-center gap-2";

    // Icon by type
    let icon = "";
    let bg = "";
    let color = "#fff";
    switch (type) {
        case "success":
            icon = `<i class="fas fa-check-circle me-2" style="color:#20e38c"></i>`;
            bg = "linear-gradient(90deg,#e3fbe8 60%,#16b686 100%)";
            color = "#13795b";
            break;
        case "danger":
            icon = `<i class="fas fa-times-circle me-2" style="color:#ff5e57"></i>`;
            bg = "linear-gradient(90deg,#fde3e3 60%,#fc5858 100%)";
            color = "#ad1d23";
            break;
        case "warning":
            icon = `<i class="fas fa-exclamation-triangle me-2" style="color:#ffd234"></i>`;
            bg = "linear-gradient(90deg,#fff9e0 60%,#ffe072 100%)";
            color = "#a88504";
            break;
        case "info":
            icon = `<i class="fas fa-info-circle me-2" style="color:#32cafe"></i>`;
            bg = "linear-gradient(90deg,#e3f0fb 60%,#67bfff 100%)";
            color = "#226ad9";
            break;
        default:
            icon = `<i class="fas fa-bell me-2"></i>`;
            bg = "#f3f3f3";
            color = "#222";
    }

    toast.style.background = bg;
    toast.style.color = color;
    toast.style.borderRadius = "1.1rem";
    toast.style.fontWeight = 500;
    toast.style.padding = "0.95rem 1.6rem";
    toast.style.fontSize = "1.1rem";
    toast.style.boxShadow = "0 8px 28px #b8ead2a0";

    // Add fade-in animation
    toast.style.opacity = "0";
    toast.style.transition = "opacity 0.18s";
    setTimeout(() => { toast.style.opacity = "1"; }, 10);

    // Hiện nội dung toast
    toastBody.innerHTML = `${icon}${message}`;

    // Hiện toast Bootstrap (ẩn sau 2.3s)
    const bsToast = new bootstrap.Toast(toast, { delay: 2300 });
    bsToast.show();

    // Ẩn dần fade-out sau khi hide
    setTimeout(() => {
        toast.style.opacity = "0";
    }, 2200);
}


// Check login
if (!localStorage.getItem("accessToken")) {
    window.location.href = "/tourify/login";
}

// ==================== ĐỔI MẬT KHẨU ======================
document.getElementById("changePasswordForm").onsubmit = async function (e) {
    e.preventDefault();
    const oldPassword = document.getElementById("current-password").value.trim();
    const newPassword = document.getElementById("new-password").value.trim();
    const confirmPassword = document.getElementById("confirm-password").value.trim();
    const token = localStorage.getItem("accessToken");
    const username = localStorage.getItem("username");

    if (!oldPassword || !newPassword || !confirmPassword) {
        showToast("Please fill in all password fields!", "warning");
        return;
    }
    if (newPassword.length < 6) {
        showToast("New password must be at least 6 characters.", "warning");
        return;
    }
    if (newPassword !== confirmPassword) {
        showToast("New passwords do not match!", "danger");
        return;
    }

    try {
        const res = await fetch("/tourify/api/user/change-password", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username, oldPassword, newPassword, confirmPassword })
        });
        const data = await res.json();
        if (data.code === 200 || data.code === 1000) {
            showToast("Password changed successfully! Logging out...", "success");
            setTimeout(() => {
                localStorage.clear();
                window.location.href = "/tourify/login";
            }, 1800);
        } else {
            showToast(data.message || "Failed to change password!", "danger");
        }
    } catch (err) {
        showToast("Error changing password!", "danger");
    }
};

// ==================== KHÓA TÀI KHOẢN ======================
document.getElementById("lockAccountBtn").onclick = async function () {
    if (!confirm("Do you really want to lock your account?")) return;

    const token = localStorage.getItem("accessToken");
    try {
        const res = await fetch("/tourify/api/user/lock", {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
            showToast("Account locked. Logging out...", "info");
            setTimeout(() => {
                localStorage.clear();
                window.location.href = "/tourify/login";
            }, 1800);
        } else {
            showToast("Failed to lock account!", "danger");
        }
    } catch (err) {
        showToast("Error locking account!", "danger");
    }
};

// ==================== XÓA TÀI KHOẢN ======================
document.getElementById("deleteAccountBtn").onclick = function () {
    document.getElementById("delete-password").value = "";
    new bootstrap.Modal(document.getElementById("deleteAccountModal")).show();
};

document.getElementById("confirmDeleteBtn").onclick = async function () {
    const token = localStorage.getItem("accessToken");
    const password = document.getElementById("delete-password").value.trim();
    if (!password) {
        showToast("Please enter your password!", "warning");
        return;
    }

    try {
        const res = await fetch("/tourify/api/user/delete", {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ password })
        });
        if (res.ok) {
            showToast("Account deleted. Goodbye!", "success");
            setTimeout(() => {
                localStorage.clear();
                window.location.href = "/tourify/register";
            }, 1800);
        } else {
            const data = await res.json().catch(() => ({}));
            showToast(data.message || "Failed to delete account!", "danger");
        }
    } catch (err) {
        showToast("Error deleting account!", "danger");
    }
};

