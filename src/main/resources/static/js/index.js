document.addEventListener("DOMContentLoaded", () => {
    const editIcon = document.getElementById("edit-icon");
    const fileInput = document.getElementById("avatar-input");
    const profilePic = document.getElementById("profile-pic");


    editIcon.addEventListener("click", () => {
        fileInput.click();
    });


    document.addEventListener("DOMContentLoaded", function () {
        const navItems = document.querySelectorAll("#navMenu .nav-custom");


        navItems.forEach((item) => {
            item.addEventListener("click", function (e) {
                e.preventDefault();
                navItems.forEach((el) => el.classList.remove("active"));
                this.classList.add("active");
            });
        });
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
            const response = await fetch(
                `http://localhost:8080/tourify/api/user/avatar?username=${username}`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ avatar: avatarUrl }),
                }
            );


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


document.addEventListener("DOMContentLoaded", function () {
    const navLinks = document.querySelectorAll(".nav-custom");
    const dropdownItems = document.querySelectorAll(
        ".dropdown-menu .dropdown-item"
    );
    const userMenu = document.getElementById("userMenu");


    navLinks.forEach((link) => {
        link.addEventListener("click", function () {
            navLinks.forEach((l) => l.classList.remove("active"));
            this.classList.add("active");
            userMenu.classList.remove("active");
        });
    });


    dropdownItems.forEach((item) => {
        item.addEventListener("click", function () {
            dropdownItems.forEach((i) => i.classList.remove("active"));
            this.classList.add("active");
            userMenu.classList.add("active");
        });
    });
});


document.addEventListener("DOMContentLoaded", () => {
    const tabLinks = document.querySelectorAll("#profileTabs .nav-link");
    const tabPanes = document.querySelectorAll(".tab-pane");


    tabLinks.forEach((link) => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const tab = link.getAttribute("data-tab");


            tabLinks.forEach((l) => l.classList.remove("active"));
            link.classList.add("active");


            tabPanes.forEach((pane) => {
                pane.classList.remove("active");
                pane.style.display = "none";
            });


            const target = document.getElementById(`tab-${tab}`);
            target.classList.add("active");
            target.style.display = "block";
        });
    });
});