document.addEventListener("DOMContentLoaded", () => {
    const editIcon = document.getElementById("edit-icon");
    const fileInput = document.getElementById("avatar-input");
    const profilePic = document.getElementById("profile-pic");

    editIcon.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", async () => {
        const file = fileInput.files[0];
        if (!file) return;

        // Tạo URL tạm để preview ảnh mới
        const imageUrl = URL.createObjectURL(file);
        profilePic.src = imageUrl;

        // Giả sử bạn sẽ upload ảnh lên một dịch vụ lưu trữ và lấy về URL thực sự.
        // Trong ví dụ này ta dùng imageUrl làm demo
        const avatarUrl = imageUrl;

        const username = localStorage.getItem("username");
        if (!username) {
            alert("Username not found in localStorage");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/tourify/api/user/avatar?username=${username}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ avatar: avatarUrl })
            });

            if (response.ok) {
                alert("Avatar updated successfully!");
            } else {
                const errorText = await response.text();
                alert("Failed to update avatar: " + errorText);
            }
        } catch (error) {
            console.error("Error updating avatar:", error);
            alert("An error occurred while updating avatar.");
        }
    });
});
