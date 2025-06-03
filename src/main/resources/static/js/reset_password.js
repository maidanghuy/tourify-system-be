document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");

    // Lấy token từ URL
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get("token");

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const newPassword = passwordInput.value.trim();
        const confirmPassword = confirmPasswordInput.value.trim();

        // Kiểm tra token
        if (!token) {
            alert("Token không hợp lệ hoặc đã hết hạn.");
            return;
        }

        // Validate mật khẩu trùng khớp
        if (newPassword !== confirmPassword) {
            alert("Mật khẩu và xác nhận mật khẩu không khớp.");
            return;
        }

        // Validate độ mạnh mật khẩu (ví dụ đơn giản)
        if (newPassword.length < 6) {
            alert("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/tourify/api/auth/reset-password", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    token: token,
                    newPassword: newPassword,
                    confirmPassword: confirmPassword
                })
            });

            if (response.ok) {
                alert("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập lại.");
                // Ví dụ: chuyển đến trang đăng nhập
                window.location.href = "/tourify/login";
            } else {
                const error = await response.json();
                alert("Lỗi: " + (error.message || "Không thể đặt lại mật khẩu."));
            }
        } catch (error) {
            console.error("Lỗi kết nối:", error);
            alert("Đã xảy ra lỗi, vui lòng thử lại sau.");
        }
    });
});
