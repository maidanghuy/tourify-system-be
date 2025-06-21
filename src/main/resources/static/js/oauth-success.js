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

(async () => {
  try {
    const res = await fetch("/tourify/api/auth/oauth2/success", {
      credentials: "include"
    });
    const data = await res.json();

    if (data.code === 1000 && data.result.token) {
      const token = data.result.token;
      const decoded = parseJwt(token);

      if (!decoded || !decoded.role) {
        throw new Error("Không thể lấy role từ token");
      }

      localStorage.setItem("accessToken", token);
      localStorage.setItem("username", data.result.userName);
      localStorage.setItem("role", decoded.role); // ✅ Bổ sung

      Swal.fire({
        icon: 'success',
        title: 'Đăng nhập thành công!',
        text: 'Chào mừng bạn đến Tourify, ' + data.result.userName + '!',
        timer: 2000,
        showConfirmButton: false,
        timerProgressBar: true,
        didClose: () => {
          // ✅ Điều hướng theo role
          if (decoded.role === 'SUB_COMPANY') {
            window.location.href = "/tourify/dashboard";
          } else {
            window.location.href = "/tourify/landing";
          }
        }
      });
    } else {
      Swal.fire({
        icon: 'error',
        title: 'Đăng nhập thất bại',
        text: data.message,
        confirmButtonText: 'Thử lại'
      }).then(() => {
        window.location.href = "/tourify/login";
      });
    }
  } catch (err) {
    console.error("❌ Lỗi khi xử lý OAuth2:", err);
    Swal.fire({
      icon: 'error',
      title: 'Lỗi kết nối',
      text: 'Không thể xử lý đăng nhập. Vui lòng thử lại.',
      confirmButtonText: 'Quay lại đăng nhập'
    }).then(() => {
      window.location.href = "/tourify/login";
    });
  }
})();
