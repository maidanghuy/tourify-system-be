// 1) Toggle show/hide bằng class .show
function toggleChat() {
  const chatContainer = document.getElementById("chatContainer");
  chatContainer.classList.toggle("show");
}

// 2) Gửi tin nhắn
async function sendMessage() {
  const inputEl = document.getElementById("userInput");
  const userInput = inputEl.value.trim();
  if (!userInput) {
    alert("Vui lòng nhập câu hỏi!");
    return;
  }
  inputEl.value = ""; // xóa input ngay

  const chatBox = document.getElementById("chatBox");
  chatBox.insertAdjacentHTML(
    "beforeend",
    `<div class="message-container user-message">
       <div class="message"><strong>Bạn:</strong> ${userInput}</div>
     </div>`
  );
  chatBox.scrollTop = chatBox.scrollHeight;

  try {
    // LẤY token từ localStorage (chắc bạn đã lưu sau khi login)
    const token = localStorage.getItem("accessToken");
    if (!token) {
      throw new Error("Chưa đăng nhập hoặc không tìm thấy token");
    }

    // GỌI đến đúng URL có context-path /tourify
    const response = await fetch("/tourify/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ message: userInput })
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }

    const botText = await response.text();
    chatBox.insertAdjacentHTML(
      "beforeend",
      `<div class="message-container bot-message">
         <div class="message"><strong>Chatbot:</strong> ${botText}</div>
       </div>`
    );
    chatBox.scrollTop = chatBox.scrollHeight;

  } catch (err) {
    console.error("Chat error:", err);
    chatBox.insertAdjacentHTML(
      "beforeend",
      `<div class="message-container bot-message">
         <div class="message">Đã xảy ra lỗi: ${err.message}</div>
       </div>`
    );
    chatBox.scrollTop = chatBox.scrollHeight;
  }
}

// 3) Bind tất cả event
document.querySelector(".chat-button").onclick = toggleChat;  // Nút nổi
document.querySelector(".close-btn").onclick = toggleChat;    // Nút “×” header
document.querySelector(".send-btn").onclick = sendMessage;    // Nút gửi

// 4) Bắn sendMessage() khi nhấn Enter (không Shift+Enter)
document.getElementById("userInput").addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
});
