// Function để ẩn/hiện mật khẩu
function togglePassword() {
  const passwordField = document.getElementById('password');
  const eyeIcon = document.getElementById('eyeIcon');
  if (passwordField.type === 'password') {
    passwordField.type = 'text';
    eyeIcon.classList.remove('fa-eye-slash');
    eyeIcon.classList.add('fa-eye');
  } else {
    passwordField.type = 'password';
    eyeIcon.classList.remove('fa-eye');
    eyeIcon.classList.add('fa-eye-slash');
  }
}

// Lắng nghe sự kiện DOMContentLoaded để đảm bảo DOM đã được tải hoàn chỉnh
document.addEventListener('DOMContentLoaded', () => {
// ✅ Nếu đã có accessToken thì tự động chuyển đến landing page
  const existingToken = localStorage.getItem('accessToken');
  if (existingToken) {
    window.location.href = '/tourify/landing';
    return;
  }
  const loginForm = document.querySelector('form');
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');

  const messageContainer = document.createElement('div');
  messageContainer.id = 'messageContainer';
  messageContainer.classList.add('mt-3', 'text-center');
  loginForm.parentNode.insertBefore(messageContainer, loginForm.nextSibling);

  function showMessage(message, type = 'error') {
    messageContainer.textContent = message;
    messageContainer.classList.remove('text-danger', 'text-success');
    if (type === 'error') {
      messageContainer.classList.add('text-danger');
    } else {
      messageContainer.classList.add('text-success');
    }
  }

  if (loginForm) {
    loginForm.addEventListener('submit', async (event) => {
      event.preventDefault();

      const username = usernameInput.value.trim();
      const password = passwordInput.value.trim();

      showMessage(''); // Xóa thông báo cũ

      // --- Validation cơ bản ---
      if (!username) {
        showMessage('Username không được để trống.');
        usernameInput.focus();
        return;
      }

      if (!password) {
        showMessage('Mật khẩu không được để trống.');
        passwordInput.focus();
        return;
      }

      if (password.length < 6) {
        showMessage('Mật khẩu phải có ít nhất 6 ký tự.');
        passwordInput.focus();
        return;
      }

      const loginData = {
        username: username,
        password: password
      };

      try {
        const response = await fetch('http://localhost:8080/tourify/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(loginData)
        });

        const data = await response.json();

        if (response.ok) {
          showMessage('Đăng nhập thành công!', 'success');
          console.log('Login successful:', data);

          if (data && data.result.token) {
            localStorage.setItem('accessToken', data.result.token);
            localStorage.setItem('username', username);
            window.location.href = '/tourify/landing';
          } else {
            // Trường hợp API trả về 200 OK nhưng body không có token hợp lệ
            showMessage('Đăng nhập không thành công. Vui lòng thử lại.', 'error');
            console.error('Login successful but no token received:', data);
          }

        } else {
          // Đây là trường hợp đăng nhập thất bại (HTTP status code không phải 2xx)
          // `data` ở đây chứa thông tin lỗi từ server
          let errorMessage = data.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';

          // Cụ thể hóa thông báo nếu API trả về token là null cho trường hợp lỗi
          if (data && data.token === null && data.message === "Invalid username or password") {
             errorMessage = 'Tên đăng nhập hoặc mật khẩu không đúng.';
          } else if (data && data.token === null) {
              errorMessage = 'Đăng nhập không thành công. Vui lòng thử lại.';
          }

          showMessage(errorMessage, 'error'); // Hiển thị thông báo lỗi trên giao diện
          console.error('Login failed:', data);
        }
      } catch (error) {
        // Xử lý lỗi mạng hoặc lỗi không mong muốn khác
        showMessage('Đã xảy ra lỗi khi kết nối đến máy chủ. Vui lòng thử lại sau.', 'error');
        console.error('Network error or unexpected error:', error);
      }
    });
  }
});