function parseJwt(token) {
    try { return JSON.parse(atob(token.split('.')[1])); } catch { return {}; }
}
const accessToken = localStorage.getItem('accessToken');
const user = parseJwt(accessToken);
const myUserId = user && user.userId;
const myUserName = user && user.userName;

let stompClient = null;
let currentChatUser = null;
let userMap = {}; // id => user

// ==== Khi vào trang, ưu tiên chat với lastChatUserId nếu có ====
document.addEventListener('DOMContentLoaded', function() {
    const lastChatUserId = localStorage.getItem('lastChatUserId');
    loadUserList(lastChatUserId); // truyền id này vào để auto chọn
    connect();
});

// ==== 1. Load user list từ API ====
// Sửa hàm này nhận tham số preferedUserId
function loadUserList(preferedUserId) {
    fetch('/tourify/api/messages/users', {
        headers: { Authorization: 'Bearer ' + accessToken }
    })
    .then(res => res.json())
    .then(users => {
        const usersArr = users.map(u => ({
            id: u.userId,
            name: u.userName,
            avatar: u.avatar || 'https://via.placeholder.com/44',
            online: u.online
        }));
        renderUserList(usersArr);

        // Nếu preferedUserId (ví dụ khoanguyen1) không có trong usersArr,
        // => Fetch info của userId này từ API profile để hiển thị và cho phép chat mới!
        if (preferedUserId && usersArr.some(u => u.id === preferedUserId)) {
            startChat(preferedUserId);
        } else if (preferedUserId) {
            // Tự động gọi API lấy thông tin user muốn chat và add vào danh sách
            fetch(`/tourify/api/user/${preferedUserId}`, {
                headers: { Authorization: 'Bearer ' + accessToken }
            })
            .then(res => res.json())
            .then(userRes => {
                // Giả sử APIResponse trả về result chứa info user
                const user = userRes.result || userRes;
                const newUser = {
                    id: user.userId,
                    name: user.userName,
                    avatar: user.avatar || 'https://via.placeholder.com/44',
                    online: user.online
                };
                usersArr.push(newUser);
                userMap[newUser.id] = newUser;
                renderUserList(usersArr); // render lại user list
                startChat(newUser.id);    // mở khung chat luôn
            });

        } else if (usersArr.length > 0) {
            startChat(usersArr[0].id);
        }
    });
}


// ==== 2. Render user list và setup click ====
function renderUserList(users) {
    const el = document.getElementById('userList');
    el.innerHTML = users.map(u => `
        <div class="user-item" data-id="${u.id}">
            <img src="${u.avatar}" class="avatar"/>
            <div>
                <div class="fw-bold">${u.name}</div>
                <div style="font-size:12px; color:${u.online ? '#20b876' : '#bbb'}">
                    ${u.online ? 'Online' : 'Offline'}
                </div>
            </div>
        </div>
    `).join('');
    users.forEach(u => userMap[u.id] = u);

    // Gắn sự kiện click cho từng user-item
    document.querySelectorAll('.user-item').forEach(item => {
        item.onclick = function() {
            const userId = item.getAttribute('data-id');
            startChat(userId);
        }
    });
}
window.startChat = startChat; // expose for HTML if cần

// ==== 3. Khi chọn user, load lịch sử chat ====
function startChat(userId) {
    if (!userMap[userId]) return;
    currentChatUser = userMap[userId];
    localStorage.setItem('lastChatUserId', userId); // Lưu lại cho lần reload sau

    // Đánh dấu active user trong danh sách
    document.querySelectorAll('.user-item').forEach(e => e.classList.remove('active'));
    const activeEl = document.querySelector(`.user-item[data-id="${userId}"]`);
    if (activeEl) activeEl.classList.add('active');

    // Cập nhật header chat
    document.getElementById('chatAvatar').src = currentChatUser.avatar;
    document.getElementById('chatUserName').textContent = currentChatUser.name;
    document.getElementById('chatUserStatus').textContent = currentChatUser.online ? 'Đang hoạt động' : '';

    // Tải lịch sử tin nhắn
    document.getElementById('chatMessages').innerHTML = '';
    loadHistory(userId);
}

// ==== 4. Load lịch sử chat từ API ====
function loadHistory(userId) {
    fetch(`/tourify/api/messages/history?with=${userId}`, {
        headers: { Authorization: 'Bearer ' + accessToken }
    })
    .then(res => res.json())
    .then(history => {
        document.getElementById('chatMessages').innerHTML = '';
        history.forEach(m => addMessage(m, m.senderId === myUserId));
    });
}

// ==== 5. Thêm tin nhắn vào khung chat ====
function addMessage(msg, mine = false) {
    const chatMessages = document.getElementById('chatMessages');
    chatMessages.innerHTML += `
        <div class="msg-row${mine ? ' mine' : ''}">
            <div class="msg-meta">${mine ? 'Bạn' : (currentChatUser ? currentChatUser.name : '')}</div>
            <div class="msg-bubble">${msg.content}</div>
        </div>
    `;
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// ==== 6. Kết nối Websocket để nhận tin nhắn realtime ====
function connect() {
    const socket = new SockJS('/tourify/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect(
        { Authorization: 'Bearer ' + accessToken },
        function (frame) {
            stompClient.subscribe(`/user/queue/messages`, function (message) {
                const msg = JSON.parse(message.body);
                // Nếu đang chat với user này thì show lên
                if (currentChatUser && msg.senderId === currentChatUser.id) {
                    addMessage(msg, false);
                } else {
                    // Nếu muốn, thêm badge chưa đọc ở sidebar
                }
            });
        }
    );
}

// ==== 7. Gửi tin nhắn ====
function sendMessage(e) {
    e.preventDefault();
    if (!currentChatUser) return;
    const input = document.getElementById('msgInput');
    const content = input.value.trim();
    if (!content) return;
    stompClient.send(
        "/app/chat.private.send",
        { Authorization: "Bearer " + accessToken },
        JSON.stringify({
            receiverId: currentChatUser.id,
            content: content
        })
    );
    addMessage({ senderId: myUserId, content: content }, true);
    input.value = '';
}

// ==== 8. Khởi tạo gửi tin nhắn (bắt sự kiện submit form) ====
document.querySelector('.chat-input-bar').onsubmit = sendMessage;

// ==== 9. Quay về trang chủ (đã phân quyền) ====
document.getElementById("goHomeBtn").addEventListener("click", function(e) {
    e.preventDefault();
    const role = localStorage.getItem('role');
    if (role === 'SUB_COMPANY') {
        window.location.href = "http://localhost:8080/tourify/dashboard";
    } else {
        window.location.href = "http://localhost:8080/tourify/landing";
    }
});
