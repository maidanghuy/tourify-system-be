(async () => {
      try {
        const res = await fetch("/tourify/api/auth/oauth2/success", {
          credentials: "include"
        });
        const data = await res.json();
        if (data.code === 1000) {
          localStorage.setItem("accessToken", data.result.token);
          localStorage.setItem("username", data.result.userName);
          Swal.fire({
            icon: 'success',
            title: 'Đăng nhập thành công!',
            text: 'Chào mừng bạn quay trở lại, ' + data.result.userName + '!',
            timer: 2000,
            showConfirmButton: false,
            timerProgressBar: true,
            didClose: () => {
              window.location.href = "/tourify/landing";
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