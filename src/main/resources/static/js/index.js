document.addEventListener("DOMContentLoaded", function () {
    const navLinks = document.querySelectorAll(".nav-custom");
    const dropdownItems = document.querySelectorAll(
        ".dropdown-menu .dropdown-item"
    );
    const userMenu = document.getElementById("userMenu");
    const accessToken = localStorage.getItem("accessToken");


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

    const username = localStorage.getItem("username");
    const userSpan = document.querySelector("#userMenu span span");
    if (userSpan) {
        userSpan.textContent = username ? username : "User";
    }

    // Hiển thị Dashboard nếu role là ADMIN
    function parseJwt(token) {
        try { return JSON.parse(atob(token.split('.')[1])); }
        catch { return {}; }
    }
    const user = parseJwt(accessToken);
    const userRole = (user && user.role || '').toUpperCase();

    if (userRole === "ADMIN") {
        const dashboardLink = document.getElementById("dashboard-link");
        if (dashboardLink) dashboardLink.style.display = "block";
    }
});