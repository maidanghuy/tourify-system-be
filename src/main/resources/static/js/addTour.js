document.addEventListener("DOMContentLoaded", () => {
  loadPlacesAndCategories();

  const addBtn = document.getElementById("addTourBtn");
  if (addBtn) {
    addBtn.addEventListener("click", handleAddTour);
  }
});

const statusMap = {
  "Draft": "DRAFT",
  "Published": "ACTIVE",
  "Pending Review": "INACTIVE",
  "Archived": "INACTIVE"
};

async function handleAddTour(e) {
  e && e.preventDefault && e.preventDefault();

  const minPeople = parseFloat(document.getElementById("minPeople").value);
  const maxPeople = parseFloat(document.getElementById("maxPeople").value);
  const duration = parseFloat(document.getElementById("duration").value);
  const price = parseFloat(document.getElementById("basePrice").value);

  // Validate các trường số cơ bản
  if (isNaN(minPeople) || isNaN(maxPeople)) {
    Swal.fire({ icon: 'warning', title: 'Please enter min and max people!' });
    return;
  }
  if (minPeople > maxPeople) {
    Swal.fire({
      icon: 'warning',
      title: 'Invalid Input',
      text: 'Min people cannot be greater than Max people.'
    });
    return;
  }
  if (isNaN(duration) || duration < 1) {
    Swal.fire({ icon: 'warning', title: 'Duration must be at least 1 day!' });
    return;
  }
  if (!Number.isInteger(price) || price < 0) {
    Swal.fire({
      icon: 'warning',
      title: 'Base Price phải là số nguyên ≥ 0!'
    });
    return;
  }
  if (!Number.isInteger(duration) || duration < 1) {
    Swal.fire({
      icon: 'warning',
      title: 'Duration phải là số nguyên ≥ 1!'
    });
    return;
  }
  if (!Number.isInteger(minPeople) || minPeople < 1) {
    Swal.fire({
      icon: 'warning',
      title: 'Min People phải là số nguyên ≥ 1!'
    });
    return;
  }
  if (!Number.isInteger(maxPeople) || maxPeople < 1) {
    Swal.fire({
      icon: 'warning',
      title: 'Max People phải là số nguyên ≥ 1!'
    });
    return;
  }
  if (minPeople > maxPeople) {
    Swal.fire({
      icon: 'warning',
      title: 'Min People không được lớn hơn Max People!'
    });
    return;
  }

  // Validate trường ngày bắt đầu và repeat times
  const startDateRaw = document.getElementById("startDate").value;
  const repeatTimes = parseInt(document.getElementById("repeatTimes").value);
  const repeatCycle = parseInt(document.getElementById("repeatCycle").value);
  const serviceIds = Array.from(document.getElementById("servicesSelect").selectedOptions).map(o => o.value);
  const activityIds = Array.from(document.getElementById("activitiesSelect").selectedOptions).map(o => o.value);

  // Thêm đoạn validate này ngay sau khi lấy serviceIds, activityIds:
  if (serviceIds.length < 0) {
    Swal.fire({
      icon: 'warning',
      title: 'Vui lòng chọn ít nhất 0 Services!'
    });
    return;
  }

  if (activityIds.length < 0) {
    Swal.fire({
      icon: 'warning',
      title: 'Vui lòng chọn ít nhất 0 Activities!'
    });
    return;
  }


  if (!startDateRaw) {
    Swal.fire({ icon: 'warning', title: 'Vui lòng chọn ngày bắt đầu tour!' });
    return;
  }
  if (isNaN(repeatTimes) || repeatTimes < 1) {
    Swal.fire({ icon: 'warning', title: 'Repeat Times phải >= 1!' });
    return;
  }
  if (isNaN(repeatCycle) || repeatCycle < 1) {
    Swal.fire({ icon: 'warning', title: 'Repeat Cycle phải ≥ 1!' });
    return;
  }

  // Format về yyyy-MM-dd HH:mm:ss nếu backend cần, còn không thì giữ yyyy-MM-dd
  const startDate = startDateRaw + " 08:00:00";

  // Chuẩn bị dữ liệu gửi lên backend
  const tourData = {
    tourName: document.getElementById("productName").value.trim(),
    description: document.getElementById("productDescription").value.trim(),
    price,
    duration,
    minPeople,
    maxPeople,
    touristNumberAssigned: 0,
    status: statusMap[document.getElementById("statusSelect").value],
    place: document.getElementById("place").value,
    category: document.getElementById("categorySelect").value,
    thumbnail: getFirstImageUrlOrNull(),
    startDate,
    repeatTimes,
    serviceIds,
    activityIds,
    repeatCycle
  };

  const token = localStorage.getItem("accessToken");
  if (!token) {
    Swal.fire({ icon: 'warning', title: 'Please login to continue!' });
    return;
  }

  try {
    const response = await fetch("/tourify/api/tours", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(tourData)
    });

    const result = await response.json();

    if (response.ok && result.code === 1000) {
      Swal.fire({
        icon: 'success',
        title: 'Tour created successfully!',
        timer: 1600,
        showConfirmButton: false,
        toast: true,
        position: 'top-end'
      }).then(() => {
        loadPage("addTour");
        setTimeout(() => {
          window.scrollTo({ top: 0, behavior: "smooth" });
        }, 400);
      });
    }
    else {
      Swal.fire({
        icon: 'error',
        title: 'Create tour failed!',
        text: result.message || 'Unknown error!'
      });
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Network error!',
      text: 'Could not connect to server.'
    });
    Swal.fire({
      icon: 'error',
      title: 'Network Error',
      text: 'Lỗi kết nối đến máy chủ.'
    });
  }
}



async function loadPlacesAndCategories() {
  const categorySelect = document.getElementById("categorySelect");
  const placeSelect = document.getElementById("place");

  // XÓA hết option cũ
  if (categorySelect)
    categorySelect.innerHTML = '<option value="" disabled selected>Select a category</option>';
  if (placeSelect)
    placeSelect.innerHTML = '<option value="" disabled selected>Select a place</option>';

  // Fetch categories
  try {
    const res = await fetch("/tourify/api/categories");
    const categories = await res.json();
    if (Array.isArray(categories) && categorySelect) {
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

  // Fetch places
  try {
    const res = await fetch("/tourify/api/place");
    const result = await res.json();
    if (Array.isArray(result.result) && placeSelect) {
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

  // Setup lại Select2 (sau khi đã append option)
  if (window.$ && $("#categorySelect").length && $("#place").length) {
    $("#categorySelect, #place").select2({
      width: "100%",
      placeholder: "Select an option",
      allowClear: true,
    });
  }
}

function getFirstImageUrlOrNull() {
  const img = document.querySelector("#imagePreview img");
  return img ? img.src : null;
}

function previewStartDates() {
  const startDateRaw = document.getElementById("startDate").value;
  const repeatTimes = parseInt(document.getElementById("repeatTimes").value);
  const repeatCycle = parseInt(document.getElementById("repeatCycle").value);

  if (!startDateRaw || isNaN(repeatTimes) || repeatTimes < 1 || isNaN(repeatCycle) || repeatCycle < 1) {
    document.getElementById("previewDates").innerHTML = "";
    return;
  }

  let dates = [];
  let d = new Date(startDateRaw + "T08:00:00");

  for (let i = 0; i < repeatTimes; i++) {
    const day = String(d.getDate()).padStart(2, "0");
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const year = d.getFullYear();
    dates.push(`${day}/${month}/${year}`);
    d.setDate(d.getDate() + repeatCycle);  // dùng biến repeatCycle thay cho 7
  }

  document.getElementById("previewDates").innerHTML =
    "<strong>Các ngày bắt đầu sẽ tự tạo:</strong> <ul class='mb-1'>" +
    dates.map(date => `<li>${date}</li>`).join('') +
    "</ul>";
}

async function suggestTourWithAI() {
  const placeText = document.getElementById("place").selectedOptions[0]?.textContent;
  const categoryText = document.getElementById("categorySelect").selectedOptions[0]?.textContent;
  const duration = document.getElementById("duration").value;

  if (!placeText || !categoryText || !duration) {
    Swal.fire({
      icon: "warning",
      title: "Please choose Place, Category and Duration first!"
    });
    return;
  }

  try {
    const res = await fetch("/tourify/api/ai/suggest-tour", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        place: placeText,
        category: categoryText,
        duration: parseInt(duration)
      })
    });

    if (!res.ok) {
      throw new Error("Failed to fetch AI suggestion");
    }

    const aiData = await res.json();

    // Đổ dữ liệu vào form
    document.getElementById("productName").value = aiData.tourName;
    document.getElementById("productDescription").value = aiData.description;
    document.getElementById("basePrice").value = aiData.price;

    // Auto chọn services
    const serviceSelect = document.getElementById("servicesSelect");
    Array.from(serviceSelect.options).forEach(opt => {
      opt.selected = aiData.serviceIds.includes(opt.value);
    });
    $(serviceSelect).trigger("change");

    // Auto chọn activities
    const activitySelect = document.getElementById("activitiesSelect");
    Array.from(activitySelect.options).forEach(opt => {
      opt.selected = aiData.activityIds.includes(opt.value);
    });
    $(activitySelect).trigger("change");

    calculateCompletion();

    Swal.fire({
      icon: "success",
      title: "AI Suggestion Completed",
      text: "Form has been filled with AI suggestion."
    });

  } catch (err) {
    console.error(err);
    Swal.fire({
      icon: "error",
      title: "AI Suggestion Failed",
      text: err.message
    });
  }
}

function openImageSuggestModal() {
  const modal = new bootstrap.Modal(document.getElementById('imageSuggestModal'));
  modal.show();
}


async function suggestTourFromImage() {
  const fileInput = document.getElementById("aiImageFile");
  if (!fileInput.files.length) {
    alert("Hãy chọn một hình ảnh!");
    return;
  }

  const formData = new FormData();
  formData.append("image", fileInput.files[0]);

  const res = await fetch("/tourify/api/ai/suggest-tour-from-image", {
    method: "POST",
    body: formData
  });

  if (!res.ok) {
    alert("AI image analysis failed");
    return;
  }

  const aiData = await res.json();

  // Gán dữ liệu chung
  document.getElementById("productName").value = aiData.tourName;
  document.getElementById("productDescription").value = aiData.description;
  document.getElementById("basePrice").value = aiData.price;

  // Auto chọn place nếu trả về
  if (aiData.place) {
    const placeSelect = document.getElementById("place");
    const normalizedAI = aiData.place.trim().toLowerCase();

    let matched = false;
    Array.from(placeSelect.options).forEach(opt => {
      const text = opt.textContent.trim().toLowerCase();
      if (text.includes(normalizedAI)) { // so sánh gần đúng
        opt.selected = true;
        matched = true;
      }
    });

    if (!matched) {
      console.warn("Không tìm thấy place khớp với:", aiData.place);
    }

    placeSelect.dispatchEvent(new Event("change"));
  }

  // Auto chọn category nếu trả về
  if (aiData.category) {
    const categorySelect = document.getElementById("categorySelect");
    Array.from(categorySelect.options).forEach(opt => {
      if (opt.textContent.trim().toLowerCase() === aiData.category.trim().toLowerCase()) {
        opt.selected = true;
      }
    });
    categorySelect.dispatchEvent(new Event("change"));
  }

  // Auto chọn services
  const serviceSelect = document.getElementById("servicesSelect");
  Array.from(serviceSelect.options).forEach(opt => {
    opt.selected = aiData.serviceIds.includes(opt.value);
  });
  $(serviceSelect).trigger("change");

  // Auto chọn activities
  const activitySelect = document.getElementById("activitiesSelect");
  Array.from(activitySelect.options).forEach(opt => {
    opt.selected = aiData.activityIds.includes(opt.value);
  });
  $(activitySelect).trigger("change");

  calculateCompletion();
  bootstrap.Modal.getInstance(document.getElementById('imageSuggestModal')).hide();
}

async function generateItineraryWithAI() {
  const placeText = document.getElementById("place").selectedOptions[0]?.textContent;
  const duration = parseInt(document.getElementById("duration").value);
  const servicesSelect = document.getElementById("servicesSelect");
  const services = Array.from(servicesSelect.selectedOptions).map(opt => opt.textContent);

  if (!placeText || !duration) {
    Swal.fire({ icon: "warning", title: "Hãy chọn Place và Duration trước!" });
    return;
  }

  const res = await fetch("/tourify/api/ai/generate-itinerary", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ place: placeText, duration, services })
  });

  if (!res.ok) {
    Swal.fire({ icon: "error", title: "AI Itinerary Failed" });
    return;
  }

  const aiData = await res.json();

  // Gán giá vào basePrice
  document.getElementById("basePrice").value = aiData.estimatedPrice;

  // Hiển thị lịch trình ra UI
  const container = document.getElementById("itineraryContainer");
  container.innerHTML = "";
  aiData.itinerary.forEach(day => {
    const div = document.createElement("div");
    div.className = "card mb-2";
    div.innerHTML = `
      <div class="card-header">Day ${day.day}</div>
      <div class="card-body">
        <ul>${day.activities.map(a => `<li>${a}</li>`).join("")}</ul>
      </div>`;
    container.appendChild(div);
  });

  Swal.fire({
    icon: "success",
    title: "AI Lịch Trình Hoàn Tất",
    text: "Lịch trình và giá tour đã được đề xuất!"
  });
}

function attachExcelImportEvents() {
  const input = document.getElementById("excelFileInput");
  const button = document.getElementById("btnImportExcel");

  if (!input || !button) {
    console.warn("Không tìm thấy input hoặc button Excel.");
    return;
  }

  button.addEventListener("click", () => input.click());

  input.addEventListener("change", async function () {
    const file = this.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await fetch("/tourify/api/ai/parse-excel", {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        Swal.fire({ icon: "error", title: "Import failed", text: "Failed to parse Excel file!" });
        return;
      }

      const tours = await res.json();
      console.log("Excel tours:", tours);

      if (!Array.isArray(tours) || tours.length === 0) {
        Swal.fire({ icon: "warning", title: "No data", text: "Không có tour nào trong file Excel!" });
        return;
      }

      // Import lần lượt từng tour
      await importMultipleTours(tours);

      Swal.fire({
        icon: "success",
        title: "Completed!",
        text: `Đã import ${tours.length} tours thành công`
      });

    } catch (err) {
      console.error("Excel import error", err);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "Có lỗi khi đọc file Excel."
      });
    }
  });
}

// Hàm fill form với 1 tour
function fillTourForm(t) {
  document.getElementById("productName").value = t.tourName || "";
  document.getElementById("productDescription").value = t.description || "";
  document.getElementById("basePrice").value = t.price || "";
  document.getElementById("minPeople").value = t.minPeople || "";
  document.getElementById("maxPeople").value = t.maxPeople || "";
  document.getElementById("duration").value = t.duration || "";

  // Chuyển đổi startDate
  if (t.startDate) {
    let raw = t.startDate.trim();
    if (raw.includes("thg")) {
      // "15-thg 8-2025" -> [15, thg, 8, 2025]
      const parts = raw.split(/[\s-]+/);
      const day = parts[0];
      const month = parts[2];
      const year = parts[3];
      const m = month.toString().padStart(2, "0");
      raw = `${year}-${m}-${day.padStart(2,"0")}`;
    } else if (raw.includes("/")) {
      const parts = raw.split("/");
      raw = `${parts[2]}-${parts[1].padStart(2,"0")}-${parts[0].padStart(2,"0")}`;
    }
    document.getElementById("startDate").value = raw;
  }

  // Nếu có imageUrl
  if (t.imageUrl) {
    const preview = document.getElementById("imagePreview");
    preview.innerHTML = `<img src="${t.imageUrl}" style="width:100%;max-height:150px;object-fit:cover;border-radius:8px;" />`;
  }

  // Chọn Place
  if (t.place) {
    const placeSelect = document.getElementById("place");
    const normalized = t.place.trim().toLowerCase();
    Array.from(placeSelect.options).forEach(opt => {
      if (opt.textContent.trim().toLowerCase() === normalized) {
        opt.selected = true;
      }
    });
    placeSelect.dispatchEvent(new Event("change"));
  }

  // Chọn Category
  if (t.category) {
    const categorySelect = document.getElementById("categorySelect");
    const normalized = t.category.trim().toLowerCase();
    Array.from(categorySelect.options).forEach(opt => {
      if (opt.textContent.trim().toLowerCase() === normalized) {
        opt.selected = true;
      }
    });
    categorySelect.dispatchEvent(new Event("change"));
  }

  calculateCompletion();
}

// Import nhiều tour
async function importMultipleTours(tours) {
  for (let i = 0; i < tours.length; i++) {
    fillTourForm(tours[i]);

    // Gọi trực tiếp hàm handleAddTour (không cần click nút)
    await handleAddTour();

    // Đợi 1 chút để server xử lý
    await new Promise(r => setTimeout(r, 5000));
  }
}


if (document.getElementById("startDate"))
  document.getElementById("startDate").addEventListener("input", previewStartDates);
if (document.getElementById("repeatTimes"))
  document.getElementById("repeatTimes").addEventListener("input", previewStartDates);
if (document.getElementById("repeatCycle"))
  document.getElementById("repeatCycle").addEventListener("input", previewStartDates);


