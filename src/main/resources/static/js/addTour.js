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
  if (serviceIds.length < 4) {
    Swal.fire({
      icon: 'warning',
      title: 'Vui lòng chọn ít nhất 4 Services!'
    });
    return;
  }

  if (activityIds.length < 4) {
    Swal.fire({
      icon: 'warning',
      title: 'Vui lòng chọn ít nhất 4 Activities!'
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
    alert("Lỗi kết nối đến máy chủ.");
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


if (document.getElementById("startDate"))
  document.getElementById("startDate").addEventListener("input", previewStartDates);
if (document.getElementById("repeatTimes"))
  document.getElementById("repeatTimes").addEventListener("input", previewStartDates);
if (document.getElementById("repeatCycle"))
  document.getElementById("repeatCycle").addEventListener("input", previewStartDates);


