function parseJwt(token) {
  try {
    const base64Payload = token.split('.')[1];
    const payload = atob(base64Payload);
    return JSON.parse(payload);
  } catch (e) {
    console.error('Lỗi parse JWT:', e);
    return null;
  }
}

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

      if (data.code === 1000 && data.result.token) {
        const token = data.result.token;
        const decoded = parseJwt(token);

        if (!decoded || !decoded.role) {
          alert("❌ Không xác định được vai trò người dùng.");
          return;
        }

        localStorage.setItem("accessToken", token);
        localStorage.setItem("username", data.result.userName);
        localStorage.setItem("role", decoded.role); // ✅ lưu role

        alert("✅ Đăng nhập bằng Google thành công!");

        // ✅ Redirect theo role
        if (decoded.role === 'SUB_COMPANY') {
          window.location.href = "/tourify/dashboard";
        } else {
          window.location.href = "/tourify/landing";
        }

      } else {
        alert("❌ Đăng nhập thất bại: " + data.message);
      }
    } catch (e) {
      console.error("❌ Lỗi khi gọi API lấy token:", e);
      alert("❌ Lỗi không xác định khi lấy token.");
    }
  }
});
