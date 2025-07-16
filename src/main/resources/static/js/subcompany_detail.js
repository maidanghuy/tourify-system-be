    // Nút chuyển đổi folow và unfolow
    const followBtn = document.getElementById("followBtn");
    let isFollowing = false; // Giả định ban đầu chưa theo dõi

    followBtn.addEventListener("click", () => {
    isFollowing = !isFollowing;

    if (isFollowing) {
    followBtn.innerHTML = `<i class="bi bi-person-dash"></i> Unfollow`;
    followBtn.classList.remove("btn-outline-secondary");
    followBtn.classList.add("btn-outline-danger");
} else {
    followBtn.innerHTML = `<i class="bi bi-person-plus"></i> Follow`;
    followBtn.classList.remove("btn-outline-danger");
    followBtn.classList.add("btn-outline-secondary");
}

    // TODO: Gọi API follow/unfollow nếu bạn cần lưu vào backend
    // fetch("/api/follow", { method: "POST", body: JSON.stringify({ follow: isFollowing }) })
});


