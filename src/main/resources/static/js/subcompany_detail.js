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

            // ==== HIỂN THỊ AVATAR ====
            const avatarEl = document.querySelector(".profile-avatar");
            if (avatarEl) {
                avatarEl.src = data.avatar || "https://randomuser.me/api/portraits/men/32.jpg";
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
