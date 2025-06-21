// Map trạng thái UI -> giá trị backend
const statusMap = {
  "Draft": "DRAFT",
  "Published": "ACTIVE",
  "Pending Review": "INACTIVE",
  "Archived": "INACTIVE"
};

document.addEventListener("DOMContentLoaded", () => {
  const addBtn = document.getElementById("addTourBtn");
  if (addBtn) {
    addBtn.addEventListener("click", async () => {
      const tourData = {
        tourName: document.getElementById("productName").value.trim(),
        description: document.getElementById("productDescription").value.trim(),
        price: parseFloat(document.getElementById("basePrice").value),
        duration: parseInt(document.getElementById("duration").value),
        minPeople: parseInt(document.getElementById("minPeople").value),
        maxPeople: parseInt(document.getElementById("maxPeople").value),
        touristNumberAssigned: 0,
        status: statusMap[document.getElementById("statusSelect").value], // ✅ chuyển đổi về DRAFT/ACTIVE/INACTIVE
        place: document.getElementById("place").value,                   // ✅ dùng đúng key theo DTO
        category: document.getElementById("categorySelect").value,       // ✅ dùng đúng key theo DTO
        thumbnail: getFirstImageUrlOrNull()
      };

      const token = localStorage.getItem("accessToken");
      if (!token) {
        alert("Vui lòng đăng nhập.");
        return;
      }

      try {
        const response = await fetch("http://localhost:8080/tourify/api/tours", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          },
          body: JSON.stringify(tourData)
        });

        const result = await response.json();

        if (response.ok && result.code === 1000) {
          // ✅ Hiển thị toast thành công
          const toastEl = document.getElementById("successToast");
          const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
          toast.show();


          // ✅ Reset lại form sau một chút (sau khi toast hiển thị)
          setTimeout(() => {
            loadPage("addTour");
          }, 1500);
        }
         else {
          alert("❌ " + (result.message || "Tạo tour thất bại."));
        }

      } catch (error) {
        console.error("Lỗi khi gọi API:", error);
        alert("Lỗi kết nối đến máy chủ.");
      }
    });
  }

  // 🟢 GỌI NGAY TẠI ĐÂY
  loadPlacesAndCategories();
});

async function loadPlacesAndCategories() {
  const categorySelect = document.getElementById("categorySelect");
  try {
    const res = await fetch("http://localhost:8080/tourify/api/categories");
    const categories = await res.json();
    console.log("🎯 Categories fetched:", categories);

    if (Array.isArray(categories)) {
      categories.forEach((c) => {
        const opt = document.createElement("option");
        opt.value = c.categoryId;
        opt.textContent = c.categoryName;
        categorySelect.appendChild(opt);
      });
    }
  } catch (e) {
    console.error("❌ Category fetch error:", e);
  }

  const placeSelect = document.getElementById("place");
  try {
    const res = await fetch("http://localhost:8080/tourify/api/place");
    const result = await res.json();
    console.log("📍 Places fetched:", result.result);

    if (Array.isArray(result.result)) {
      result.result.forEach((p) => {
        const opt = document.createElement("option");
        opt.value = p.placeId;
        opt.textContent = p.placeName;
        placeSelect.appendChild(opt);
      });
    }
  } catch (e) {
    console.error("❌ Place fetch error:", e);
  }

  // Select2 setup
  $("#categorySelect, #place").select2({
    width: "100%",
    placeholder: "Select an option",
    allowClear: true,
  });
}

function getFirstImageUrlOrNull() {
  const img = document.querySelector("#imagePreview img");
  return img ? img.src : null;
}
