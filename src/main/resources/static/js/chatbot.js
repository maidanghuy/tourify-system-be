// 1) Toggle show/hide bằng class .show
function toggleChat() {
  const chatContainer = document.getElementById("chatContainer");
  chatContainer.classList.toggle("show");
}

// 0) References preview
const fileEl    = document.getElementById("imageInput");
const previewC  = document.getElementById("imagePreviewContainer");
const previewImg= document.getElementById("imagePreview");

// Khi chọn file → hiện preview
fileEl.addEventListener("change", () => {
  const f = fileEl.files[0];
  if (!f) {
    previewC.style.display = "none";
    return;
  }
  const reader = new FileReader();
  reader.onload = e => {
    previewImg.src = e.target.result;
    previewC.style.display = "block";
  };
  reader.readAsDataURL(f);
});


// 2) Gửi tin nhắn
async function sendMessage() {
  const inputEl   = document.getElementById("userInput");
  const userInput = inputEl.value.trim();
  const file      = fileEl.files[0];      // vẫn còn giữ file để gửi

  if (!userInput && !file) {
    alert("Vui lòng nhập câu hỏi hoặc chọn hình ảnh!");
    return;
  }

  // 1) Append ngay user bubble (chưa reset file)
  appendUserBubble(userInput, file);

  // 2) Reset text input, nhưng ĐỪNG xóa fileEl.value ở đây!
  inputEl.value = "";

  try {
    const token = localStorage.getItem("accessToken");
    if (!token) throw new Error("Chưa đăng nhập hoặc không tìm thấy token");

    let response, botText;
    if (file) {
      const formData = new FormData();
      formData.append("file", file);
      formData.append("message", userInput || "");
      response = await fetch("/tourify/chat-with-image", {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}` },
        body: formData
      });
      botText = await response.text();
    } else {
      response = await fetch("/tourify/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ message: userInput })
      });
      botText = await response.text();
    }

    appendBotBubble(botText);

  } catch (err) {
    console.error("Chat error:", err);
    appendBotBubble(`Đã xảy ra lỗi: ${err.message}`);
  }

  // 3) Sau khi gửi xong, xóa file và ẩn preview
  fileEl.value = null;
  previewC.style.display = "none";
}

function appendUserBubble(text, file) {
  const chatBox = document.getElementById("chatBox");
  let html = `<div class="message-container user-message"><div class="message">`;

  if (file) {
    const url = URL.createObjectURL(file);
    html += `<div><img src="${url}" style="max-width:120px; margin-bottom:4px; border-radius:8px;"></div>`;
  }
  if (text) {
    html += `${text}`;
  }
  html += `</div></div>`;

  chatBox.insertAdjacentHTML("beforeend", html);
  chatBox.scrollTop = chatBox.scrollHeight;
}


function appendBotBubble(text) {
  const chatBox = document.getElementById("chatBox");
  chatBox.insertAdjacentHTML(
    "beforeend",
    `<div class="message-container bot-message">
       <div class="message"><strong>Chatbot:</strong> ${text}</div>
     </div>`
  );
  chatBox.scrollTop = chatBox.scrollHeight;
}

// Bind events
document.querySelector(".send-btn").onclick = sendMessage;
document.getElementById("userInput").addEventListener("keydown", e => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    sendMessage();
  }
});

