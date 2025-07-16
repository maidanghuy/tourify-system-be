document.addEventListener("DOMContentLoaded", function () {
    // 1. Lấy userId từ query string (?userId=...)
    const userId = new URLSearchParams(window.location.search).get("userId");
    console.log("[SubcompanyDetail] userId from query:", userId);

    if (!userId) {
        console.error("❌ Không tìm thấy userId trên URL. Vui lòng kiểm tra link hoặc truyền đúng userId!");
        return;
    }

    // 2. Gọi API lấy chi tiết SubCompany
    fetch(`/tourify/api/user/subcompanies/${userId}`, {
        headers: {
            "Content-Type": "application/json"
            // Nếu cần token:
            // "Authorization": "Bearer " + localStorage.getItem("access_token")
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("❌ Không thể fetch dữ liệu từ API (status " + res.status + ")");
            return res.json();
        })
        .then(response => {
            const data = response.result; // Chuẩn response trả về từ APIResponse
            console.log("[SubcompanyDetail] API data:", data);
            if (!data) {
                console.error("❌ API không trả về dữ liệu SubCompany hoặc trả về null!");
                return;
            }

            // === Cập nhật UI (nên kiểm tra element trước khi gán) ===
            const setText = (id, value) => {
                const el = document.getElementById(id);
                if (el) el.textContent = value ?? "";
            };
            const setHref = (id, value, prefix) => {
                const el = document.getElementById(id);
                if (el) el.href = value ? (prefix ? `${prefix}${value}` : value) : "#";
            };

            setText("companyName", data.companyName);
            setText("followerCount", `${data.followerCount ?? 0} người theo dõi`);
            setText("contactName", data.contactName);
            setText("dob", data.dob ?? "");
            setText("hotline", data.hotline);
            setText("address", data.address);
            setText("description", data.description);
            setText("totalTours", data.totalTours ?? "0");
            setText("totalCustomers", `${data.totalCustomersServed ?? 0}+`);
            setText("websiteLink", data.website ?? "");
            setHref("websiteLink", data.website);

            setText("emailLink", data.email ?? "");
            setHref("emailLink", data.email, "mailto:");
        })
        .catch(err => {
            console.error("Lỗi khi tải dữ liệu SubCompany:", err);
        });
});

document.addEventListener("DOMContentLoaded", function () {
    // Lấy subCompanyId từ query string (?userId=...)
    const subCompanyId = new URLSearchParams(window.location.search).get("userId");
    const followBtn = document.getElementById("followBtn");
    const followerCountEl = document.getElementById("followerCount");
    const token = localStorage.getItem("accessToken");

    // Hàm hiển thị số follower từ API
    function refreshFollowerCount() {
        fetch(`/tourify/api/follow/sub-company/${subCompanyId}/followers/count`)
            .then(res => res.json())
            .then(data => {
                setFollowerCount(data.count || 0);
            });
    }

    // Lấy số từ thẻ
    function parseFollowerCount() {
        return parseInt((followerCountEl?.textContent || "0").replace(/\D/g, "")) || 0;
    }

    // Set lại số follower trên giao diện
    function setFollowerCount(count) {
        followerCountEl.textContent = `${count} người theo dõi`;
    }

    // Kiểm tra trạng thái đã follow chưa
    function checkFollowStatus() {
        fetch(`/tourify/api/follow/sub-company/${subCompanyId}/is-followed`, {
            method: "GET",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            }
        })
            .then(res => res.json())
            .then(data => {
                if (data.isFollowed) {
                    setUnfollowUI();
                } else {
                    setFollowUI();
                }
            });
    }

    function setFollowUI() {
        followBtn.innerHTML = '<i class="bi bi-person-plus"></i> Follow';
        followBtn.classList.remove("btn-danger");
        followBtn.classList.add("btn-outline-secondary");
        followBtn.onclick = handleFollow;
    }

    function setUnfollowUI() {
        followBtn.innerHTML = '<i class="bi bi-person-dash"></i> Unfollow';
        followBtn.classList.remove("btn-outline-secondary");
        followBtn.classList.add("btn-danger");
        followBtn.onclick = handleUnfollow;
    }

    // Khi nhấn Follow
    function handleFollow() {
        fetch(`/tourify/api/follow/sub-company/${subCompanyId}`, {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            }
        })
            .then(res => {
                if (res.ok) {
                    setUnfollowUI();
                    setFollowerCount(parseFollowerCount() + 1); // Cập nhật UI ngay
                    // Có thể gọi lại refreshFollowerCount() sau 0.5-1s để sync số thật nếu muốn
                    // setTimeout(refreshFollowerCount, 800);
                }
            });
    }

    // Khi nhấn Unfollow
    function handleUnfollow() {
        fetch(`/tourify/api/follow/sub-company/${subCompanyId}`, {
            method: "DELETE",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            }
        })
            .then(res => {
                if (res.ok) {
                    setFollowUI();
                    setFollowerCount(Math.max(0, parseFollowerCount() - 1)); // Cập nhật UI ngay
                    // Có thể gọi lại refreshFollowerCount() sau 0.5-1s để sync số thật nếu muốn
                    // setTimeout(refreshFollowerCount, 800);
                }
            });
    }

    // Khi trang load
    if (token && subCompanyId) {
        refreshFollowerCount();
        checkFollowStatus();
    } else {
        followBtn.disabled = true;
        followBtn.innerHTML = "Login to follow";
        setFollowerCount(0);
    }
});


