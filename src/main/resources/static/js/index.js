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

    const username = localStorage.getItem("username");
    const userSpan = document.querySelector("#userMenu span span");
    if (userSpan) {
        userSpan.textContent = username ? username : "User";
    }
});