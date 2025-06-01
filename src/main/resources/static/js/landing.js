
    document.addEventListener("DOMContentLoaded", function () {
    const username = localStorage.getItem("username");
    const greetingEl = document.getElementById("greeting");
    if (username && greetingEl) {
    greetingEl.textContent = `Hello, ${username}`;
}
});