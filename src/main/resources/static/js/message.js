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

    // ------ 1. Load user list từ API ------
    function loadUserList() {
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
        if (usersArr.length > 0) startChat(usersArr[0].id);
    });
}

    function renderUserList(users) {
        const el = document.getElementById('userList');
        el.innerHTML = users.map(u => `
          <div class="user-item" ...>
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
    }
    window.startChat = startChat; // expose for HTML

    // ------ 2. Khi chọn user, load lịch sử chat ------
    function startChat(userId) {
        if (!userMap[userId]) return;
        currentChatUser = userMap[userId];
        localStorage.setItem('lastChatUserId', userId); // Lưu lại cho lần reload sau

        // Đánh dấu active
        document.querySelectorAll('.user-item').forEach(e => e.classList.remove('active'));
        const activeEl = document.querySelector(`.user-item[data-id="${userId}"]`);
        if(activeEl) activeEl.classList.add('active');
        // Cập nhật header
        document.getElementById('chatAvatar').src = currentChatUser.avatar;
        document.getElementById('chatUserName').textContent = currentChatUser.name;
        document.getElementById('chatUserStatus').textContent = currentChatUser.online ? 'Đang hoạt động' : '';
        // Tải history (API call)
        document.getElementById('chatMessages').innerHTML = '';
        loadHistory(userId);
    }

    // ------ 3. Load lịch sử chat từ API ------
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

    // ------ 4. Thêm tin nhắn vào khung chat ------
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

    // ------ 5. Kết nối Websocket ------
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
                        // Có thể thêm badge chưa đọc ở sidebar nếu muốn
                    }
                });
            }
        );
    }

    // ------ 6. Gửi tin nhắn ------
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

    // ------ 7. Khởi tạo ------
    loadUserList();
    connect();
