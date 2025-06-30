// ✅ Giải mã JWT để lấy thông tin role, sub (username)
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

// 🔒 Ẩn/hiện mật khẩu
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

// ✅ Khi DOM đã sẵn sàng
document.addEventListener('DOMContentLoaded', () => {

  // Nếu đã có token → decode & redirect theo role
  const existingToken = localStorage.getItem('accessToken');
  if (existingToken) {
    const decoded = parseJwt(existingToken);
    if (decoded && decoded.role === 'SUB_COMPANY') {
      window.location.href = '/tourify/dashboard';
    } else {
      window.location.href = '/tourify/landing';
    }
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
    messageContainer.classList.add(type === 'error' ? 'text-danger' : 'text-success');
  }

  if (loginForm) {
    loginForm.addEventListener('submit', async (event) => {
      event.preventDefault();

      const username = usernameInput.value.trim();
      const password = passwordInput.value.trim();

      showMessage(''); // Clear old message

      // ⚠️ Validation cơ bản
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

      const loginData = { username, password };

      try {
        const response = await fetch('/tourify/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(loginData)
        });

        const data = await response.json();

        if (response.ok && data.result && data.result.token) {
          showMessage('Đăng nhập thành công!', 'success');
          const token = data.result.token;
          const decoded = parseJwt(token);

          if (!decoded || !decoded.role) {
            showMessage('Không xác định được vai trò người dùng.', 'error');
            return;
          }

          localStorage.setItem('accessToken', token);
          localStorage.setItem('username', decoded.sub);
          localStorage.setItem('role', decoded.role);

          if (decoded.role === 'SUB_COMPANY') {
            window.location.href = '/tourify/dashboard';
          } else {
            window.location.href = '/tourify/landing';
          }

        } else {
          // Xử lý lỗi logic và token null
          let errorMessage = data.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
          if (data.token === null && data.message === "Invalid username or password") {
            errorMessage = 'Tên đăng nhập hoặc mật khẩu không đúng.';
          }
          showMessage(errorMessage, 'error');
          console.error('Login failed:', data);
        }

      } catch (error) {
        showMessage('Đã xảy ra lỗi khi kết nối đến máy chủ. Vui lòng thử lại sau.', 'error');
        console.error('Lỗi mạng hoặc không xác định:', error);
      }
    });
  }
});
