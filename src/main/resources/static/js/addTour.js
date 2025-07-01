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

    const minPeople = parseInt(document.getElementById("minPeople").value);
      const maxPeople = parseInt(document.getElementById("maxPeople").value);
      const duration = parseInt(document.getElementById("duration").value);

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

  const tourData = {
    tourName: document.getElementById("productName").value.trim(),
    description: document.getElementById("productDescription").value.trim(),
    price: parseFloat(document.getElementById("basePrice").value),
    duration,
    minPeople,
    maxPeople,
    touristNumberAssigned: 0,
    status: statusMap[document.getElementById("statusSelect").value],
    place: document.getElementById("place").value,
    category: document.getElementById("categorySelect").value,
    thumbnail: getFirstImageUrlOrNull()
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
  categorySelect.innerHTML = '<option value="" disabled selected>Select a category</option>';
  placeSelect.innerHTML = '<option value="" disabled selected>Select a place</option>';

  // Fetch categories
  try {
    const res = await fetch("/tourify/api/categories");
    const categories = await res.json();
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

  // Fetch places
  try {
    const res = await fetch("/tourify/api/place");
    const result = await res.json();
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

  // Setup lại Select2 (sau khi đã append option)
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


