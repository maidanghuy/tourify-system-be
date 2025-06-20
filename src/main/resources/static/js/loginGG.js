function loginWithGoogle() {
        window.location.href = "/tourify/oauth2/authorization/google";
    }

    window.addEventListener('DOMContentLoaded', async () => {
        if (window.location.pathname.includes("/oauth/success")) {
            try {
                const res = await fetch("/tourify/api/auth/oauth2/success", {
                    credentials: "include"
                });
                const data = await res.json();
                if (data.code === 1000) {
                    localStorage.setItem("access_token", data.result.token);
                    localStorage.setItem("username", data.result.userName);
                    alert("✅ Đăng nhập bằng Google thành công!");
                    window.location.href = "/tourify"; // 🎯 Chuyển trang landing
                } else {
                    alert("❌ Đăng nhập thất bại: " + data.message);
                }
            } catch (e) {
                console.error("❌ Lỗi khi gọi API lấy token:", e);
                alert("❌ Lỗi không xác định khi lấy token.");
            }
        }
    });