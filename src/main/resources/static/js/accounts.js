document.addEventListener('DOMContentLoaded', () => {
    const table = document.getElementById('accountTable');
    if (!table) {
        console.warn('AccountList: #accountTable not found, skip.');
        return;
    }
    const tbody = table.querySelector('tbody');
    if (!tbody) {
        console.error('Table body not found');
        return;
    }
    // 1. Lấy token từ localStorage
    const token = localStorage.getItem('accessToken');
    if (!token) {
        console.error('No access token found');
        return;
    }

    // 2. Gọi API
    fetch('/tourify/api/user', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            return response.json();
        })
        .then(json => {
            // APIResponse thứ tự: { code?, message?, result: [...] }
            const users = json.result || [];
            const tbody = document.querySelector('#accountTable tbody');
            if (!tbody) {
                console.error('Table body not found');
                return;
            }
            tbody.innerHTML = '';  // xóa hàng mẫu

            users.forEach(u => {
                // Format ngày sinh (ISO → DD/MM/YYYY)
                let dob = '';
                if (u.dob) {
                    const d = new Date(u.dob);
                    dob = d.toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric'
                    });
                }

                // Giới tính
                const gender = u.gender === true || u.gender === 'Male'
                    ? 'Male' : u.gender === false || u.gender === 'Female'
                        ? 'Female' : '';

                // Trạng thái
                const statusClass = u.status && u.status.toLowerCase() === 'active'
                    ? 'status-active' : 'status-blocked';

                // Tạo 1 row mới
                const tr = document.createElement('tr');
                tr.innerHTML = `
        <td><input type="checkbox"></td>
        <td>${u.role || ''}</td>
        <td>
          <div class="d-flex align-items-center">
            <img src="${u.avatar || '/static/images/avatar_default.jpg'}"
                 style="width:36px;height:36px;border-radius:50%;object-fit:cover;margin-right:10px;border:2px solid #b7e4c7;">
            <div>
              <div style="font-weight:600; color:#22292f;">
                ${u.userName || ''}
              </div>
              <div style="font-size:0.95em; color:#8b909a;">
                ${u.email || ''}
              </div>
            </div>
          </div>
        </td>
        <td>${(u.firstName||'') + ' ' + (u.lastName||'')}</td>
        <td>${gender}</td>
        <td>${u.phoneNumber || ''}</td>
        <td>${u.address || ''}</td>
        <td>${dob}</td>
        <td><span class="${statusClass}">${u.status || ''}</span></td>
        <td class="action-btns">
          <i class="fa fa-pen text-primary" title="Edit"></i>
          <i class="fa fa-trash text-danger" title="Delete"></i>
        </td>
      `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Fetch users failed:', err));
});