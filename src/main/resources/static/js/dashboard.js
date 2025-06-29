      let toursData = [];      // lưu toàn bộ danh sách tour
      let currentPage = 1;     // trang hiện tại
      const pageSize = 6;      // số tour mỗi trang


      const pages = {
        dashboard: {
          title: "Dashboard",
          breadcrumbs: [],
          content: `<p>Welcome to the dashboard.</p>`,
        },
        tourList: {
          title: "Tour List",
          breadcrumbs: ["dashboard"],
          content: `<p>Here is the list of tours.</p>`,
        },
        addTour: {
          title: "Add Tour",
          breadcrumbs: ["dashboard", "tourList"],
          content: `
        <div class="container-fluid px-4 py-4">
        <div class="row g-4">
            <div class="col-lg-9">
            <div class="card p-4">
                <div class="form-section-title">General Information</div>
                <input type="text" id="productName" class="form-control mb-3" placeholder="Type product name here...">
                <textarea id="productDescription" class="form-control mb-4" rows="3" placeholder="Type product description here..."></textarea>


                <div class="form-section-title">Media</div>
                <div class="dropzone mb-3" id="imageDropzone">
                <i class="fas fa-image mb-2 fa-2x text-success"></i><br>
                Drag and drop image here, or click add image
                <div class="mt-2">
                    <button class="btn btn-success btn-sm mt-2" data-bs-toggle="modal" data-bs-target="#mediaModal" onclick="setMediaType('image')">Add Image</button>
                </div>
                <div class="media-preview" id="imagePreview"></div>
                </div>


                <div class="dropzone mb-4" id="videoDropzone">
                <i class="fas fa-video mb-2 fa-2x text-success"></i><br>
                Drag and drop video here, or click add video
                <div class="mt-2">
                    <button class="btn btn-success btn-sm mt-2" data-bs-toggle="modal" data-bs-target="#mediaModal" onclick="setMediaType('video')">Add Video</button>
                </div>
                <div class="media-preview" id="videoPreview"></div>
                </div>


                <div class="form-section-title">Pricing</div>
                <div class="mb-3 input-group">
                <span class="input-group-text">$</span>
                <input type="number" id="basePrice" class="form-control" placeholder="Type base price here..." min="0">
                </div>
            </div>


            <div class="card p-4 mt-4">
                <div class="form-section-title">Customer</div>
                <div class="row g-2">
                <div class="col-md-6">
                    <label class="form-label">Min people</label>
                    <input class="form-control" id="minPeople" placeholder="Min people">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Max people</label>
                    <input class="form-control" id="maxPeople" placeholder="Max people">
                </div>
                </div>
            </div>


            <div class="card p-4 mt-4">
                <div class="form-section-title">Duration & Place</div>
                <div class="row g-2">
                <div class="col-md-6">
                    <label class="form-label">Duration</label>
                    <input class="form-control" id="duration" placeholder="Duration (days)">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Place</label>
                    <select id="place" class="form-select">
                      <option value="" disabled selected>Select a place</option>
                    </select>

                </div>
                </div>
            </div>
            </div>


            <div class="col-lg-3">
            <div class="card p-4 mb-3">
                <div class="form-section-title">Category</div>
                <label class="form-label mt-2">Category Name</label>
                <select id="categorySelect" class="form-select">
                  <option value="" disabled selected>Select a category</option>
                </select>



            </div>


            <div class="card p-4 mb-3">
                <div class="form-section-title">
                Status <span id="statusBadge" class="badge bg-success float-end">Draft</span>
                </div>
                <label class="form-label">Tour Status</label>
                <select id="statusSelect" class="form-select" onchange="document.getElementById('statusBadge').textContent = this.value">
                <option value="Draft">Draft</option>
                <option value="Published">Published</option>
                <option value="Pending Review">Pending Review</option>
                <option value="Archived">Archived</option>
                </select>
            </div>
            </div>
        </div>


        <div class="d-flex justify-content-between align-items-center mt-4">
            <div>
        <span class="fw-semibold text-danger">Product Completion</span>
        <span id="completionBadge" class="badge bg-danger ms-1">0%</span>
        <div id="missingFieldsMsg" class="text-danger mt-1 small"></div>
        </div>


            <div>
            <button class="btn btn-outline-danger">Cancel</button>
            <button class="btn btn-success" id="addTourBtn" disabled>+ Add Tour</button>
            </div>
        </div>
        </div>
        `,
        },


        categories: {
          title: "Categories",
          breadcrumbs: ["dashboard"],
          content: `<p>Manage categories here.</p>`,
        },
        booking: {
          title: "Booking",
          breadcrumbs: ["dashboard"],
          content: `<p>Booking management interface.</p>`,
        },
        customers: {
          title: "Customers",
          breadcrumbs: ["dashboard"],
          content: `<p>Customer information and actions.</p>`,
        },
        seller: {
          title: "Seller",
          breadcrumbs: ["dashboard"],
          content: `<p>Seller management area.</p>`,
        },
        analytics: {
          title: "Analytics",
          breadcrumbs: ["dashboard"],
          content: `<p>Performance and analytics dashboard.</p>`,
        },
      };


function loadPage(pageKey) {
  const page = pages[pageKey];
  if (!page) {
    document.getElementById("mainContent").innerHTML = "<h2>Page not found</h2>";
    return;
  }

  // ✅ RESET class active cho tất cả nav-link
  document.querySelectorAll(".nav-link").forEach(link => {
    link.classList.remove("active");
  });

  // ✅ THÊM class active cho đúng link
  const linkId = pageKey + "Link"; // Ví dụ: "tourList" -> "tourListLink"
  const activeLink = document.getElementById(linkId);
  if (activeLink) {
    activeLink.classList.add("active");
  }

  // ✅ HIỂN THỊ nội dung chính
  const breadcrumbHtml = `
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="fw-bold text-success mb-1">${page.title}</h2>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb small">
            ${page.breadcrumbs.map(crumb => `
              <li class="breadcrumb-item">
                <a href="#" onclick="loadPage('${crumb}')"
                   class="text-decoration-none text-muted">${pages[crumb].title}</a>
              </li>`).join("")}
            <li class="breadcrumb-item active text-success" aria-current="page">${page.title}</li>
          </ol>
        </nav>
      </div>
    </div>`;

  document.getElementById("mainContent").innerHTML = breadcrumbHtml + page.content;

  // ✅ Load các trang đặc biệt
  if (pageKey === "addTour") {
    setTimeout(() => initAddTourPage(), 0);
  } else if (pageKey === "tourList") {
    loadTourList();
  }
}



      document.addEventListener("DOMContentLoaded", function () {
        loadPage("addTour");
        const submenu = document.getElementById("tourSubmenu");
        const icon = document.querySelector("#tourToggle .toggle-icon");


        submenu.addEventListener("show.bs.collapse", () => {
          icon.classList.remove("fa-chevron-down");
          icon.classList.add("fa-chevron-up");
        });


        submenu.addEventListener("hide.bs.collapse", () => {
          icon.classList.remove("fa-chevron-up");
          icon.classList.add("fa-chevron-down");
        });
      });




      let currentMediaType = null;


      function setMediaType(type) {
        currentMediaType = type;
        document.getElementById("mediaModalLabel").textContent =
          type === "image" ? "Add Image" : "Add Video";
        document.getElementById("mediaUrlInput").value = "";
        document.getElementById("mediaFileInput").value = "";
      }


      function addMedia() {
        const url = document.getElementById("mediaUrlInput").value.trim();
        const fileInput = document.getElementById("mediaFileInput");
        const preview = document.getElementById(
          currentMediaType === "image" ? "imagePreview" : "videoPreview"
        );

        let mediaHTML = "";
        if (url) {
          mediaHTML =
            currentMediaType === "image"
              ? `<a href="${url}" target="_blank"><img src="${url}" /></a>`
              : `<a href="${url}" target="_blank"><video src="${url}" controls muted></video></a>`;
        } else if (fileInput.files.length > 0) {
          const fileURL = URL.createObjectURL(fileInput.files[0]);
          mediaHTML =
            currentMediaType === "image"
              ? `<a href="${fileURL}" target="_blank"><img src="${fileURL}" /></a>`
              : `<a href="${fileURL}" target="_blank"><video src="${fileURL}" controls muted></video></a>`;
        }

        // ✅ THÊM Ở ĐÂY
        if (mediaHTML) {
          preview.insertAdjacentHTML("beforeend", mediaHTML);
          calculateCompletion(); // ✅ Thêm dòng này để cập nhật phần trăm hoàn thành
        }

        bootstrap.Modal.getInstance(
          document.getElementById("mediaModal")
        ).hide();
      }





      $(document).ready(function () {
        $("#categorySelect").select2({
          placeholder: "Select a category",
          width: "100%", // đảm bảo vừa khung
        });
      });




      $(document).ready(function () {
        // Select2 for Category and Status
        $("#categorySelect, #statusSelect").select2({
          placeholder: "Select an option",
          width: "100%",
        });


        // Cập nhật badge khi chọn Status
        $("#statusSelect").on("change", function () {
          const selected = $(this).val();
          $("#statusBadge").text(selected);
        });
      });




      const requiredFields = [
        { selector: "#productName", label: "Product Name" },
        { selector: "#productDescription", label: "Description" },
        { selector: "#basePrice", label: "Base Price" },
        { selector: "#minPeople", label: "Min People" },
        { selector: "#maxPeople", label: "Max People" },
        { selector: "#duration", label: "Duration" },
        { selector: "#place", label: "Place" },
        { selector: "#categorySelect", label: "Category" },
        { selector: "#statusSelect", label: "Status" },
      ];


      function isMediaValid() {
        const imageCount = document.querySelectorAll("#imagePreview img").length;
        const videoCount = document.querySelectorAll("#videoPreview video").length;
        return {
          valid: imageCount > 0 || videoCount > 0, // ✅ Chỉ cần 1 trong 2
          missing: imageCount === 0 && videoCount === 0
            ? [{ label: "Image or Video", scrollTo: "#imageDropzone" }]
            : [],
        };
      }




      function calculateCompletion() {
        let filled = 0;
        const missingFields = [];


        requiredFields.forEach(({ selector, label }) => {
          const el = document.querySelector(selector);
          if (el && el.value.trim() !== "") {
            filled++;
          } else {
            missingFields.push({ label, scrollTo: selector });
          }
        });


        const media = isMediaValid();
        if (media.valid) {
          filled++;
        } else {
          missingFields.push(...media.missing);
        }


        const totalFields = requiredFields.length + 1;
        const percent = Math.round((filled / totalFields) * 100);


        const badge = document.getElementById("completionBadge");
        badge.innerText = `${percent}%`;
        badge.className =
          "badge ms-1 bg-" + (percent < 100 ? "danger" : "success");


        document.getElementById("addTourBtn").disabled = percent < 100;


        const msg = document.getElementById("missingFieldsMsg");
        if (missingFields.length > 0) {
          msg.innerHTML =
            "⚠️ Missing: " +
            missingFields
              .map(
                ({ label, scrollTo }) =>
                  `<a href="#" onclick="scrollToElement('${scrollTo}')" class="text-danger fw-semibold text-decoration-underline me-1">${label}</a>`
              )
              .join(", ");
        } else {
          msg.innerHTML = "";
        }
      }


      function scrollToElement(selector) {
        const el = document.querySelector(selector);
        if (el) {
          // Scroll with center alignment
          el.scrollIntoView({ behavior: "smooth", block: "center" });


          // Add flashing red border
          el.classList.add("border", "border-3", "border-danger", "rounded");


          // Optional focus
          if (["INPUT", "TEXTAREA", "SELECT"].includes(el.tagName)) {
            el.focus({ preventScroll: true });
          }


          // Remove after 2.5 seconds
          setTimeout(() => {
            el.classList.remove(
              "border",
              "border-3",
              "border-danger",
              "rounded"
            );
          }, 2500);
        }
      }


      document.addEventListener("DOMContentLoaded", () => {
        requiredFields.forEach(({ selector }) => {
          const el = document.querySelector(selector);
          if (el) {
            el.addEventListener("input", calculateCompletion);
            el.addEventListener("change", calculateCompletion);
          }
        });


        // Live watch for media zone
        const imageZone = document.querySelector("#imageDropzone");
        const videoZone = document.querySelector("#videoDropzone");
        imageZone.addEventListener("DOMSubtreeModified", calculateCompletion);
        videoZone.addEventListener("DOMSubtreeModified", calculateCompletion);


        calculateCompletion(); // Initial
      });




      function initAddTourPage() {
        // ✅ Khởi tạo Select2 cho các dropdown
        $("#categorySelect, #statusSelect, #place").select2({
          placeholder: "Select an option",
          width: "100%",
          allowClear: true
        });

        // ✅ Cập nhật badge khi chọn Status
        $("#statusSelect").on("change", function () {
          const selected = $(this).val();
          $("#statusBadge").text(selected);
        });

        // ✅ Trigger cập nhật Completion khi chọn Category hoặc Place
        $("#categorySelect, #place").on("change", function () {
          calculateCompletion();
        });

        // ✅ Gán lại sự kiện input/change cho tất cả các trường required
        requiredFields.forEach(({ selector }) => {
          const el = document.querySelector(selector);
          if (el) {
            el.addEventListener("input", calculateCompletion);
            el.addEventListener("change", calculateCompletion);
          }
        });

        // ✅ Gắn lại listener cho media zones (ảnh, video)
        const imageZone = document.querySelector("#imageDropzone");
        const videoZone = document.querySelector("#videoDropzone");
        if (imageZone) {
          imageZone.addEventListener("DOMSubtreeModified", calculateCompletion);
        }
        if (videoZone) {
          videoZone.addEventListener("DOMSubtreeModified", calculateCompletion);
        }

        // ✅ Gọi ngay khi DOM đã hoàn chỉnh
        setTimeout(() => {
          if (
            document.getElementById("completionBadge") &&
            document.getElementById("missingFieldsMsg")
          ) {
            calculateCompletion();
          }
        }, 100);
      }

async function loadTourList() {
  const token = localStorage.getItem("accessToken");
  if (!token) {
    alert("Bạn chưa đăng nhập.");
    return;
  }

  try {
    const res = await fetch("/tourify/api/tours/my-tours", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const result = await res.json();
    if (res.ok && result.code === 1000) {
      toursData = result.result || []; // ✅ lưu dữ liệu vào biến toàn cục
      currentPage = 1;
      renderTourList(); // ✅ gọi hàm mới (không truyền biến)
    } else {
      document.getElementById("mainContent").innerHTML =
        "<div class='alert alert-warning'>Không có tour nào được tìm thấy.</div>";
    }
  } catch (error) {
    console.error("❌ Lỗi khi lấy danh sách tour:", error);
    document.getElementById("mainContent").innerHTML =
      "<div class='alert alert-danger'>Không thể kết nối máy chủ.</div>";
  }
}

function renderTourList() {
  const wrapper = document.getElementById("mainContent");
  wrapper.innerHTML = `
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="fw-bold text-success mb-0">Tour List</h2>
    </div>
  `;

  const container = document.createElement("div");
  container.className = "row g-3";

  if (!Array.isArray(toursData) || toursData.length === 0) {
    container.innerHTML = `<div class='alert alert-warning'>Chưa có tour nào.</div>`;
  } else {
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    const paginatedTours = toursData.slice(start, end); // ✅ chỉ lấy 6 tour

    paginatedTours.forEach((tour) => {
      const col = document.createElement("div");
      col.className = "col-md-6 col-lg-4";
      col.innerHTML = `
        <div class="card h-100 shadow-sm">
          <img src="${tour.thumbnail || 'https://via.placeholder.com/400x200?text=No+Image'}" class="card-img-top" style="height: 200px; object-fit: cover;">
          <div class="card-body d-flex flex-column justify-content-between">
            <div>
              <h5 class="card-title">${tour.tourName}</h5>
              <p class="card-text small text-muted">${tour.description || "No description provided."}</p>
              <p class="mb-1"><i class="fas fa-tag text-success"></i> <strong>$${tour.price}</strong></p>
              <p class="mb-1"><i class="fas fa-clock text-warning"></i> ${tour.duration} days</p>
              <p class="mb-1"><i class="fas fa-users text-primary"></i> ${tour.minPeople}–${tour.maxPeople} people</p>
              <span class="badge bg-${tour.status === 'DRAFT' ? 'secondary' : tour.status === 'ACTIVE' ? 'success' : 'warning'}">${tour.status}</span>
            </div>
            <div class="mt-3 d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm w-100" onclick="viewTour('${tour.tourId}')">
                <i class="fas fa-eye"></i> View
              </button>
              <button class="btn btn-outline-warning btn-sm w-100" onclick="editTour('${tour.tourId}')">
                <i class="fas fa-edit"></i> Edit
              </button>
              <button class="btn btn-outline-danger btn-sm w-100" onclick="deleteTour('${tour.tourId}')">
                <i class="fas fa-trash"></i> Delete
              </button>
            </div>
          </div>
        </div>`;
      container.appendChild(col);
    });
  }

  wrapper.appendChild(container);
  renderPagination(); // ✅ gọi thêm phân trang ở dưới
}

function renderPagination() {
  const totalPages = Math.ceil(toursData.length / pageSize);
  if (totalPages <= 1) return;

  const pagination = document.createElement("nav");
  pagination.className = "mt-4";
  pagination.innerHTML = `
    <ul class="pagination justify-content-center">
      <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="changePage(${currentPage - 1})">Trang trước</a>
      </li>
      ${Array.from({ length: totalPages }, (_, i) => `
        <li class="page-item ${currentPage === i + 1 ? 'active' : ''}">
          <a class="page-link" href="#" onclick="changePage(${i + 1})">${i + 1}</a>
        </li>`).join('')}
      <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
        <a class="page-link" href="#" onclick="changePage(${currentPage + 1})">Trang sau</a>
      </li>
    </ul>
  `;

  document.getElementById("mainContent").appendChild(pagination);
}


function changePage(newPage) {
  const totalPages = Math.ceil(toursData.length / pageSize);
  if (newPage >= 1 && newPage <= totalPages) {
    currentPage = newPage;
    renderTourList();
  }
}

  document.addEventListener("DOMContentLoaded", function () {
    const username = localStorage.getItem("username") || "Guest";
    document.getElementById("usernameDisplay").textContent = username;
  });




