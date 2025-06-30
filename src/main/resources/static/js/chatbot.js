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
    `<div class="message-container user-message"><div class="message">Bạn: ${userInput}</div></div>`
  );
  chatBox.scrollTop = chatBox.scrollHeight;

  try {
    const response = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyAC1nL5pcvvkrFpuZSkrsSixD5El4qr7yY",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ parts: [{ text: userInput }] }],
        }),
      }
    );
    const data = await response.json();

    let botText = "Không thể trả lời câu hỏi của bạn. Vui lòng thử lại sau.";
    if (data.candidates && data.candidates.length) {
      botText = data.candidates[0].content.parts[0].text;
    }
    chatBox.insertAdjacentHTML(
      "beforeend",
      `<div class="message-container bot-message"><div class="message">Chatbot: ${botText}</div></div>`
    );
    chatBox.scrollTop = chatBox.scrollHeight;
  } catch (err) {
    console.error(err);
    chatBox.insertAdjacentHTML(
      "beforeend",
      `<div class="message-container bot-message"><div class="message">Đã xảy ra lỗi khi kết nối tới API. Vui lòng thử lại sau.</div></div>`
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
