let toursData = []; // lưu toàn bộ danh sách tour
let currentPage = 1; // trang hiện tại
let sortField = null;
let sortDir = "asc"; // 'asc' hoặc 'desc'
let currentMediaType = null;

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
  { selector: "#startDate", label: "Start Date" },
  { selector: "#repeatTimes", label: "Repeat Times" },
  { selector: "#repeatCycle", label: "Repeat Cycle" },
];
const toastEl = document.getElementById("liveToast");
const toast = toastEl ? new bootstrap.Toast(toastEl) : null;
const pageSize = 6; // số tour mỗi trang
const pages = {
  /* === 1. DASHBOARD === */
  dashboard: {
    title: "Dashboard",
    breadcrumbs: [],
    content: `
    <div class="container-fluid py-4">
      <!-- ==== Time-range pills + Add-Tour ==== -->
      <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <button id="btnAddTour" class="btn btn-success">
          <i class="bi bi-plus-lg me-2"></i>Add Tour
        </button>
      </div>

      <!-- ==== KPI cards ==== -->
      <div class="row gy-4 gx-4">

        <!-- Total Booking -->
        <div class="col-xl-3 col-md-6">
          <div class="card-kpi h-100 p-4">
            <div class="icon-wrap bg-booking"><i class="bi bi-cart-check"></i></div>
            <div class="flex-grow-1">
              <span class="kpi-title">Total Booking</span>
              <h4 class="fw-bold mb-0" id="bookingCount"><span class="kpi-value"></span></h4>
            </div>
            <span class="badge-growth bg-success-subtle text-success" id="bookingGrowth"></span>
          </div>
        </div>

        <!-- Total Tour -->
        <div class="col-xl-3 col-md-6">
          <div class="card-kpi h-100 p-4">
            <div class="icon-wrap bg-tour">
              <i class="bi bi-compass"></i>
            </div>
            <div class="flex-grow-1">
              <span class="kpi-title">Total Tour</span>
              <h4 class="fw-bold mb-0" id="tourCount"><span class="kpi-value"></span></h4>
            </div>
            <span class="badge-growth bg-secondary-subtle text-secondary" id="tourGrowth"></span>
          </div>
        </div>
      </div>

      <!-- ==== Top Tour Booking + Sales by Company ==== -->
      <div class="row g-4 mt-1">
        <!-- left 8 cols -->
        <div class="col-lg-8">
          <div class="card-glass p-4 h-100">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="fw-bold mb-0">Top Tour Booking</h5>
              <button class="btn btn-outline-success btn-sm" id="tourFilterBtn">
                <i class="bi bi-sliders"></i> Filters
              </button>
            </div>

            <table class="table table-modern mb-2" id="topTourTable">
              <thead class="table-success text-nowrap">
                <tr>
                  <th>Tours</th><th>Booking</th><th>Price</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                <!-- JS render -->
              </tbody>
            </table>

            <!-- simple pagination -->
            <nav class="d-flex justify-content-end">
              <ul class="pagination pagination-sm mb-0" id="topTourPagination">
                <!-- JS render -->
              </ul>
            </nav>
          </div>
        </div>

        <!-- right 4 cols -->
        <div class="col-lg-4">
          <div class="card-glass p-4 h-100">
            <h5 class="fw-bold mb-3">Sales by Companies</h5>
            <small class="text-muted">Sales performance by company</small>
            <ul class="list-group list-group-flush mt-3" id="salesCompanyList">
              <!-- JS render -->
            </ul>
          </div>
        </div>
      </div>

      <!-- ==== Recent Bookings ==== -->
      <div class="card-glass p-4 mt-4">
        <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
          <h5 class="fw-bold mb-0">
            Recent Bookings
            <span class="badge bg-success-subtle text-success ms-2" id="recentOrderBadge"></span>
          </h5>
          <div class="d-flex gap-2">
            <button class="btn btn-outline-success btn-sm" id="bookingFilterBtn">
              <i class="bi bi-sliders"></i> Filters
            </button>
            <button class="btn btn-success btn-sm" id="seeMoreBookingBtn">See More</button>
          </div>
        </div>
        <table class="table table-modern" id="recentBookingTable">
          <thead class="table-success text-nowrap">
            <tr>
              <th><input type="checkbox"></th>
              <th>Booking ID</th>
              <th>Booking Tour</th>
              <th>Customer</th>
              <th>Total</th>
              <th>Payment</th>
              <th>Status</th>
              <th class="text-end">Action</th>
            </tr>
          </thead>
          <tbody>
            <!-- JS render -->
          </tbody>
        </table>
        <!-- pagination -->
        <nav class="d-flex justify-content-end">
          <ul class="pagination pagination-sm mb-0" id="recentBookingPagination">
            <!-- JS render -->
          </ul>
        </nav>
      </div>
    </div>
  `,
  },

  /* === 2. TOUR LIST === */
  tourList: {
    title: "Tour List",
    breadcrumbs: ["dashboard"],
    content: `         
          <div class="container py-4">
            <!-- Các button, search box đặt ở đây -->
            <div class="d-flex mb-3 align-items-center flex-wrap gap-3 justify-content-between">
              <!-- Search box bên trái -->
              <div class="flex-grow-1" style="min-width:240px;max-width:520px;">
                <input class="form-control-mint w-100" type="text" placeholder="Search tour. . ." />
              </div>
              <!-- 2 nút bên phải -->
              <div class="d-flex gap-3 flex-shrink-0">
                <button class="btn-mint-filter" type="button">
                  <i class="bi bi-funnel"></i> Filters
                </button>
                <button class="btn-mint-accent" type="button"
                        onclick="loadPage('addTour')">
                  <i class="bi bi-plus"></i> Add Tour
                </button>
                <button id="btnDeleteSelected" class="btn btn-danger" type="button">
                    <i class="bi bi-trash me-1"></i> Delete Selected
                </button>
              </div>
            </div>
        
            <!-- Đây là div cũ giữ nguyên để render JS vào -->
            <div id="tourListBody"></div>
          </div>
          `,
  },

  /* === 3. ADD TOUR (nguyên bản, đã có UI hiện đại) === */
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
                        <input type="number" id="basePrice" class="form-control" placeholder="Type base price here..." min="0" step="1">
                        </div>
                    </div>
        
        
                    <div class="card p-4 mt-4">
                        <div class="form-section-title">Customer</div>
                        <div class="row g-2">
                        <div class="col-md-6">
                            <label class="form-label">Min people</label>
                            <input class="form-control" id="minPeople" placeholder="Min people" type="number" min="1" step="1">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Max people</label>
                            <input class="form-control" id="maxPeople" placeholder="Max people" type="number" min="1" step="1">
                        </div>
                        </div>
                    </div>
        
        
                    <div class="card p-4 mt-4">
                        <div class="form-section-title">Duration & Place</div>
                        <div class="row g-2">
                        <div class="col-md-6">
                            <label class="form-label">Duration</label>
                            <input class="form-control" id="duration" placeholder="Duration (days)" type="number" min="1" step="1">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">Place</label>
                            <select id="place" class="form-select">
                              <option value="" disabled selected>Select a place</option>
                            </select>
        
                        </div>
                        </div>
                                                <div class="form-section-title mt-4">Tour Start Date & Repeat</div>
                                                <div class="row g-2">
                                                  <div class="col-md-6">
                                                      <label class="form-label">Start Date</label>
                                                      <input class="form-control" id="startDate" type="date" min="2025-07-15"/>
                                                  </div>
                                                  <div class="col-md-6">
                                                      <label class="form-label">Repeat Times</label>
                                                      <input class="form-control" id="repeatTimes" type="number" min="1" max="20" value="1" />
                                                      <small class="text-muted">Tour sẽ tự cập nhật ngày bắt đầu sau mỗi x ngày, tối đa số lần bạn chọn.</small>
                                                  </div>

                                                  <div class="col-md-6">
                                                      <label class="form-label">Repeat Cycle (days)</label>
                                                      <input class="form-control" id="repeatCycle" type="number" min="1" max="30" value="7" />
                                                      <small class="text-muted">Chu kỳ lặp ngày, ví dụ 1, 2, 7, ... (mặc định 7 ngày)</small>
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
        
                        <!-- Chọn Services -->
                        <div class="form-section-title mt-4">Services</div>
                        <select id="servicesSelect" class="form-select" multiple></select>

                        <!-- Chọn Activities -->
                        <div class="form-section-title mt-4">Activities</div>
                        <select id="activitiesSelect" class="form-select" multiple></select>

        
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
                    <button type="button" class="btn btn-primary" onclick="suggestTourWithAI()">
                        <i class="bi bi-robot"></i> AI Suggest
                      </button>
                </div>
            </div>
          </div>
          `,
  },

  /* === 4. CATEGORIES === */
  categories: {
    title: "Categories",
    breadcrumbs: ["dashboard"],
    content: `
          <div class="container py-4">
              <div class="admin-card p-4">
                <!-- Thanh công cụ trên đầu: Search + 2 button dàn ngang -->
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                  <!-- Search bên trái -->
                  <div class="flex-grow-1" style="min-width:220px; max-width:520px;">
                    <input
                      class="form-control-mint w-100"
                      type="text"
                      placeholder="Search category..."
                    />
                  </div>
                  <!-- 2 nút bên phải -->
                  <div class="d-flex gap-2 flex-shrink-0">
                    <button class="btn-mint-filter" type="button">
                      <i class="bi bi-funnel"></i> Filters
                    </button>
                    <button
                      class="btn-mint-accent"
                      data-bs-toggle="modal"
                      data-bs-target="#catModal"
                    >
                      <i class="bi bi-plus"></i> Add Category
                    </button>
                  </div>
                </div>
            
                <!-- Table -->
                <div class="table-responsive-mint">
                  <table class="mint-table w-100" id="catTable">
                    <thead>
                      <tr>
                        <th style="width:32px"><input type="checkbox" /></th>
                        <th style="min-width:180px">Category</th>
                        <th style="min-width:120px">Status</th>
                        <th style="min-width:110px">Update</th>
                        <th style="min-width:110px">Added</th>
                        <th style="min-width:90px">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      <!-- JS sẽ render dữ liệu vào đây -->
                    </tbody>
                  </table>
                </div>
            
                <!-- Pagination -->
                <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
                  <div class="small text-muted">Showing 1-10 from 15</div>
                  <ul class="pagination-mint mb-0">
                    <li class="page-item-mint disabled"><span>&lt;</span></li>
                    <li class="page-item-mint active"><span>1</span></li>
                    <li class="page-item-mint"><span>2</span></li>
                    <li class="page-item-mint"><span>&gt;</span></li>
                  </ul>
                </div>
              </div>
          </div>  
          `,
  },

  /* === 5. BOOKING === */
  booking: {
    title: "Booking",
    breadcrumbs: ["dashboard"],
    content: `
          <div class="container-fluid py-4">
              <div class="admin-card p-4">
                <!-- Time Filter + Filter Button -->
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                  <!-- Search bên trái -->
                  <div class="flex-grow-1" style="min-width:220px;max-width:520px;">
                    <input
                      class="form-control-mint w-100"
                      type="text"
                      placeholder="Search category..."
                    />
                  </div>
                  <!-- 2 nút bên phải -->
                  <div class="d-flex gap-2 flex-shrink-0">
                    <button class="btn-mint-filter" type="button">
                      <i class="bi bi-funnel"></i> Filters
                    </button>
                    <button
                      class="btn-mint-accent"
                      data-bs-toggle="modal"
                      data-bs-target="#catModal"
                    >
                      <i class="bi bi-plus"></i> Add Booking
                    </button>
                  </div>
                </div>
            
                <!-- Table -->
                <div class="table-responsive-mint">
                  <table class="mint-table w-100" id="bookingTable">
                    <thead>
                      <tr>
                        <th style="width:32px"><input type="checkbox" /></th>
                        <th style="min-width:100px">Booking ID</th>
                        <th style="min-width:170px">BookingTour</th>
                        <th style="min-width:160px">Customer</th>
                        <th style="min-width:90px">Total</th>
                        <th style="min-width:100px">Payment</th>
                        <th style="min-width:100px">Status</th>
                        <th style="min-width:80px">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      <!-- JS render booking rows here -->
                    </tbody>
                  </table>
                </div>
            
                <!-- Pagination -->
                <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
                  <div class="small text-muted">Showing 1-10 from 100</div>
                  <ul class="pagination-mint mb-0">
                    <li class="page-item-mint disabled"><span>&lt;</span></li>
                    <li class="page-item-mint active"><span>1</span></li>
                    <li class="page-item-mint"><span>2</span></li>
                    <li class="page-item-mint"><span>3</span></li>
                    <li class="page-item-mint"><span>4</span></li>
                    <li class="page-item-mint"><span>5</span></li>
                    <li class="page-item-mint"><span>...</span></li>
                    <li class="page-item-mint"><span>&gt;</span></li>
                  </ul>
                </div>
              </div>
          </div>
          `,
  },

  /* === 6. CUSTOMERS === */
  customers: {
    title: "Account List",
    breadcrumbs: ["dashboard"],
    content: `
          <div class="container-fluid py-4">
              <div class="admin-card p-4">
                  <h2 class="fw-bold text-success mb-1">Account List</h2>
                  <nav aria-label="breadcrumb">
                      <ol class="breadcrumb small">
                          <li class="breadcrumb-item">
                              <a class="text-decoration-none text-muted" href="#">Dashboard</a>
                          </li>
                          <li aria-current="page" class="breadcrumb-item active text-success">
                              Account List
                          </li>
                      </ol>
                  </nav>
                  <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                      <div class="flex-grow-1" style="min-width: 220px; max-width: 520px">
                          <input class="form-control-mint w-100" placeholder="Search account..." type="text"/>
                      </div>
                      <div class="d-flex gap-2 flex-shrink-0">
                          <button class="btn-mint-filter" type="button">
                              <i class="bi bi-funnel"></i> Filters
                          </button>
                          <button class="btn-mint-accent">
                              <i class="bi bi-plus"></i> Add Account
                          </button>
                      </div>
                  </div>
                  <div class="table-responsive-mint">
                      <table class="mint-table w-100" id="accountTable">
                          <thead>
                          <tr>
                              <th style="width: 1%"><input type="checkbox"/></th>
                              <th style="width: 1%">Role</th>
                              <th style="width: 1%">User Name</th>
                              <th style="width: 1%">Name</th>
                              <th style="width: 1%">Gender</th>
                              <th style="width: 1%">Phone Number</th>
                              <th style="width: 1%">Address</th>
                              <th style="width: 2%">Date of Birth</th>
                              <th style="width: 1%">Status</th>
                              <th style="width: 1%">Action</th>
                          </tr>
                          </thead>
                          <tbody>
                          <!-- JS render account rows here -->
                          </tbody>
                      </table>
                  </div>
                  <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
                      <div class="small text-muted">Showing 1-10 from 100</div>
                      <ul class="pagination-mint mb-0">
                          <li class="page-item-mint disabled"><span>&lt;</span></li>
                          <li class="page-item-mint active"><span>1</span></li>
                          <li class="page-item-mint"><span>2</span></li>
                          <li class="page-item-mint"><span>3</span></li>
                          <li class="page-item-mint"><span>4</span></li>
                          <li class="page-item-mint"><span>5</span></li>
                          <li class="page-item-mint"><span>...</span></li>
                          <li class="page-item-mint"><span>&gt;</span></li>
                      </ul>
                  </div>
              </div>
          </div>
        `,
  },

  /* === 7. SELLERS === */
  seller: {
    title: "List Tour Wait Approve",
    breadcrumbs: ["dashboard"],
    content: `
          <div class="container-fluid py-4">
              <div class="admin-card p-4">
                <!-- Search + Filter/Add buttons -->
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                  <!-- Search box bên trái -->
                  <div class="flex-grow-1" style="min-width:220px;max-width:520px;">
                    <input class="form-control-mint w-100" type="text" placeholder="Search tour..." id="sellerSearch" />
                  </div>
                  <!-- 2 nút bên phải -->
                  <div class="d-flex gap-2 flex-shrink-0">
                    <button class="btn-mint-filter" type="button">
                      <i class="bi bi-funnel"></i> Filters
                    </button>
                    <button class="btn-mint-accent" id="btnAddSeller">
                      <i class="bi bi-plus"></i> Add Seller
                    </button>
                  </div>
                </div>
                <!-- Table -->
                <div class="table-responsive-mint">
                  <table class="mint-table w-100" id="sellerTable">
                    <thead>
                      <tr>
                        <th style="width:32px"><input type="checkbox"></th>
                        <th style="min-width:180px">Thumbnail</th>
                        <th style="min-width:140px">Tour Name</th>
                        <th style="min-width:120px">Sub-Company</th>
                        <th style="min-width:110px">Price</th>
                        <th style="min-width:100px">Place</th>
                        <th style="min-width:110px">Created At</th>
                        <th style="min-width:90px">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      <!-- JS sẽ render seller rows ở đây -->
                    </tbody>
                  </table>
                </div>
                <!-- Pagination -->
                <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
                  <div class="small text-muted">Showing 1-10 from 100</div>
                  <ul class="pagination-mint mb-0">
                    <li class="page-item-mint disabled"><span>&lt;</span></li>
                    <li class="page-item-mint active"><span>1</span></li>
                    <li class="page-item-mint"><span>2</span></li>
                    <li class="page-item-mint"><span>3</span></li>
                    <li class="page-item-mint"><span>4</span></li>
                    <li class="page-item-mint"><span>5</span></li>
                    <li class="page-item-mint"><span>...</span></li>
                    <li class="page-item-mint"><span>&gt;</span></li>
                  </ul>
                </div>
              </div>
          </div>
          `,
  },

  /* === 8. ANALYTICS === */
  analytics: {
    title: "Analytics",
    breadcrumbs: ["dashboard"],
    content: `
          <div class="container-fluid py-4">
  <!-- Bộ lọc Khoảng Thời Gian -->
  <div class="d-flex align-items-end gap-3 mb-4 flex-wrap">
    <div class="form-group">
      <label for="startDate" class="form-label">Start Date</label>
      <input type="date" id="startDate" class="form-control" />
    </div>
    <div class="form-group">
      <label for="endDate" class="form-label">End Date</label>
      <input type="date" id="endDate" class="form-control" />
    </div>
    <button id="btnFilter" class="btn btn-primary mb-2">Apply</button>
  </div>
  
  <!-- === Thống kê bổ sung === -->
  <div class="row mb-4">
    <!-- Active Tours -->
    <div class="col-lg-4 col-md-6 mb-3">
      <div class="card-glass p-3 text-center shadow-sm">
        <h6 class="fw-semibold mb-2">Active Tours</h6>
        <h2 id="activeToursCount">0</h2>
      </div>
    </div>
    <!-- Top Booked Tours -->
    <div class="col-lg-8 col-md-6 mb-3">
      <div class="card-glass p-3 shadow-sm">
        <h6 class="fw-semibold mb-3">Top Booked Tours</h6>
        <div class="table-responsive">
          <table class="modern-table w-100" id="topBookedToursTable">
            <thead>
              <tr>
                <th>Tour Name</th>
                <th class="text-end">Bookings</th>
              </tr>
            </thead>
            <tbody id="topBookedToursTbody">
              <!-- JS sẽ đổ data vào đây -->
            </tbody>
          </table>
        </div>
        <div class="d-none" id="topBookedToursChartWrap">
          <canvas id="topBookedToursChart" height="100"></canvas>
        </div>
      </div>
    </div>
  </div>
  <!-- === Kết thúc Thống kê bổ sung === -->

  <!-- Tabs Filter: Day / Month / Year -->
  <ul class="nav nav-pills mb-3" id="revenue-range-tabs" role="tablist">
    <li class="nav-item" role="presentation">
      <button class="nav-link active" id="day-tab" data-bs-toggle="pill" data-bs-target="#revenue-day" type="button" role="tab">
        Day
      </button>
    </li>
    <li class="nav-item" role="presentation">
      <button class="nav-link" id="month-tab" data-bs-toggle="pill" data-bs-target="#revenue-month" type="button" role="tab">
        Month
      </button>
    </li>
    <li class="nav-item" role="presentation">
      <button class="nav-link" id="year-tab" data-bs-toggle="pill" data-bs-target="#revenue-year" type="button" role="tab">
        Year
      </button>
    </li>
  </ul>

  <div class="tab-content mb-4" id="revenue-range-tabs-content">
    <!-- ===== DAY ===== -->
    <div class="tab-pane fade show active" id="revenue-day" role="tabpanel">
      <div class="card-glass p-4 shadow-sm">
        <h5 class="fw-semibold mb-3">Revenue by Day</h5>
        <!-- System Day -->
        <div class="mb-3">
          <div class="d-flex justify-content-end gap-2 mb-2">
            <button type="button" class="btn btn-outline-secondary btn-sm d-none" id="btnShowTableSystemDay">Xem Bảng</button>
          </div>
          <div class="table-responsive" id="systemDayTableWrap">
            <table class="modern-table w-100" id="systemRevenueDayTable">
              <thead>
                <tr>
                  <th colspan="2" class="text-end py-2" style="background:transparent; border:none">
                    <button type="button" class="btn btn-outline-primary btn-sm" id="btnShowChartSystemDay">Xem Biểu Đồ</button>
                  </th>
                </tr>
                <tr>
                  <th style="min-width:120px">Date</th>
                  <th style="min-width:180px">Total Revenue (VND)</th>
                </tr>
              </thead>
              <tbody id="systemTbodyDay"></tbody>
            </table>
          </div>
          <div class="d-none" id="systemDayChartWrap">
            <canvas id="systemDayChart" height="110"></canvas>
          </div>
        </div>
        <!-- Company Day -->
        <div class="mb-3">
          <div class="d-flex justify-content-end gap-2 mb-2">
            <button type="button" class="btn btn-outline-secondary btn-sm d-none" id="btnShowTableCompanyDay">Xem Bảng</button>
          </div>
          <div class="table-responsive" id="companyDayTableWrap">
            <table class="modern-table w-100" id="companyRevenueDayTable">
              <thead>
                <tr>
                  <th colspan="3" class="text-end py-2" style="background:transparent; border:none">
                    <button type="button" class="btn btn-outline-primary btn-sm" id="btnShowChartCompanyDay">Xem Biểu Đồ</button>
                  </th>
                </tr>
                <tr>
                  <th style="min-width:120px">Date</th>
                  <th style="min-width:180px">Company</th>
                  <th style="min-width:120px">Revenue (VND)</th>
                </tr>
              </thead>
              <tbody id="tbodyDay"></tbody>
            </table>
          </div>
          <div class="d-none" id="companyDayChartWrap">
            <canvas id="companyDayChart" height="110"></canvas>
          </div>
        </div>
      </div>
    </div>
    <!-- ===== MONTH ===== -->
    <div class="tab-pane fade" id="revenue-month" role="tabpanel">
      <div class="card-glass p-4 shadow-sm">
        <h5 class="fw-semibold mb-3">Revenue by Month</h5>
        <!-- System Month -->
        <div class="mb-3">
          <div class="d-flex justify-content-end gap-2 mb-2">
            <button type="button" class="btn btn-outline-secondary btn-sm d-none" id="btnShowTableSystemMonth">Xem Bảng</button>
          </div>
          <div class="table-responsive" id="systemMonthTableWrap">
            <table class="modern-table w-100" id="systemRevenueMonthTable">
              <thead>
                <tr>
                  <th colspan="2" class="text-end py-2" style="background:transparent; border:none">
                    <button type="button" class="btn btn-outline-primary btn-sm" id="btnShowChartSystemMonth">Xem Biểu Đồ</button>
                  </th>
                </tr>
                <tr>
                  <th style="min-width:120px">Month</th>
                  <th style="min-width:180px">Total Revenue (VND)</th>
                </tr>
              </thead>
              <tbody id="systemTbodyMonth"></tbody>
            </table>
          </div>
          <div class="d-none" id="systemMonthChartWrap">
            <canvas id="systemMonthChart" height="110"></canvas>
          </div>
        </div>
        <!-- Company Month -->
        <div class="mb-3">
          <div class="d-flex justify-content-end gap-2 mb-2">
            <button type="button" class="btn btn-outline-secondary btn-sm d-none" id="btnShowTableCompanyMonth">Xem Bảng</button>
          </div>
          <div class="table-responsive" id="companyMonthTableWrap">
            <table class="modern-table w-100" id="companyRevenueMonthTable">
              <thead>
                <tr>
                  <th colspan="3" class="text-end py-2" style="background:transparent; border:none">
                    <button type="button" class="btn btn-outline-primary btn-sm" id="btnShowChartCompanyMonth">Xem Biểu Đồ</button>
                  </th>
                </tr>
                <tr>
                  <th style="min-width:120px">Month</th>
                  <th style="min-width:180px">Company</th>
                  <th style="min-width:120px">Revenue (VND)</th>
                </tr>
              </thead>
              <tbody id="tbodyMonth"></tbody>
            </table>
          </div>
          <div class="d-none" id="companyMonthChartWrap">
            <canvas id="companyMonthChart" height="110"></canvas>
          </div>
        </div>
      </div>
    </div>
    <!-- ===== YEAR ===== -->
    <div class="tab-pane fade" id="revenue-year" role="tabpanel">
      <div class="card-glass p-4 shadow-sm">
        <h5 class="fw-semibold mb-3">Revenue by Year</h5>
        <!-- System Year -->
        <div class="mb-3">
          <div class="d-flex justify-content-end gap-2 mb-2">
            <button type="button" class="btn btn-outline-secondary btn-sm d-none" id="btnShowTableSystemYear">Xem Bảng</button>
          </div>
          <div class="table-responsive" id="systemYearTableWrap">
            <table class="modern-table w-100" id="systemRevenueYearTable">
              <thead>
                <tr>
                  <th colspan="2" class="text-end py-2" style="background:transparent; border:none">
                    <button type="button" class="btn btn-outline-primary btn-sm" id="btnShowChartSystemYear">Xem Biểu Đồ</button>
                  </th>
                </tr>
                <tr>
                  <th style="min-width:120px">Year</th>
                  <th style="min-width:180px">Total Revenue (VND)</th>
                </tr>
              </thead>
              <tbody id="systemTbodyYear"></tbody>
            </table>
          </div>
          <div class="d-none" id="systemYearChartWrap">
            <canvas id="systemYearChart" height="110"></canvas>
          </div>
        </div>
        <!-- Company Year -->
        <div class="mb-3">
          <div class="d-flex justify-content-end gap-2 mb-2">
            <button type="button" class="btn btn-outline-secondary btn-sm d-none" id="btnShowTableCompanyYear">Xem Bảng</button>
          </div>
          <div class="table-responsive" id="companyYearTableWrap">
            <table class="modern-table w-100" id="companyRevenueYearTable">
              <thead>
                <tr>
                  <th colspan="3" class="text-end py-2" style="background:transparent; border:none">
                    <button type="button" class="btn btn-outline-primary btn-sm" id="btnShowChartCompanyYear">Xem Biểu Đồ</button>
                  </th>
                </tr>
                <tr>
                  <th style="min-width:120px">Year</th>
                  <th style="min-width:180px">Company</th>
                  <th style="min-width:120px">Revenue (VND)</th>
                </tr>
              </thead>
              <tbody id="tbodyYear"></tbody>
            </table>
          </div>
          <div class="d-none" id="companyYearChartWrap">
            <canvas id="companyYearChart" height="110"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

          `,
  },

  /* === 9. ADD PROMOTION === */
  addPromotion: {
    title: "Add Promotion",
    breadcrumbs: ["dashboard", "promotionList"],
    content: `
      <div class="container-fluid px-4 py-4">
        <div class="row">
          <div >
            <div class="card p-4">
              <div class="form-section-title">Promotion Information</div>
              <input type="text" id="promotionCode" class="form-control mb-3" placeholder="Promotion code...">
              <textarea id="promotionDescription" class="form-control mb-3" rows="2" placeholder="Description..."></textarea>
              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label">Quantity</label>
                  <input class="form-control" id="promotionQuantity" type="number" min="1" placeholder="Quantity">
                </div>
                <div class="col-md-6">
                  <label class="form-label">Min Purchase</label>
                  <input class="form-control" id="promotionMinPurchase" type="number" min="0" placeholder="Min purchase value">
                </div>
              </div>
              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label">Discount (%)</label>
                  <input class="form-control" id="promotionDiscountPercent" type="number" min="1" max="100" placeholder="e.g. 10">
                </div>
                <div class="col-md-6">
                  <label class="form-label">Min Purchase Description</label>
                  <input class="form-control" id="promotionMinPurchaseDescription" type="text" placeholder="Min Purchase Description">
                </div>
              </div>
              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label">Start Time</label>
                  <input class="form-control" id="promotionStartTime" type="date">
                </div>
                <div class="col-md-6">
                  <label class="form-label">End Time</label>
                  <input class="form-control" id="promotionEndTime" type="date">
                </div>
              </div>
              <div class="mb-3"></div>
                <label class="form-label">Apply to Tours</label>
                <div class="d-flex gap-2 mb-2">
                  <button type="button" class="btn btn-outline-primary btn-sm" id="addAllToursBtn">
                    <i class="bi bi-plus-circle me-1"></i>Add All Tours
                  </button>
                  <button type="button" class="btn btn-outline-danger btn-sm" id="clearAllToursBtn">
                    <i class="bi bi-x-circle me-1"></i>Clear All
                  </button>
                </div>
                <select id="promotionTourIds" class="form-select" multiple>
                <option value=""> Select Tour </option>
</select>
              </div>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-4">
              <div>
                <span class="fw-semibold text-danger">Promotion Completion</span>
                <span id="promotionCompletionBadge" class="badge bg-danger ms-1">0%</span>
                <div id="promotionMissingFieldsMsg" class="text-danger mt-1 small"></div>
              </div>
              <div>
                <button class="btn btn-outline-danger">Cancel</button>
                <button class="btn btn-success" id="addPromotionBtn" disabled>+ Add Promotion</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    `,
  },

  promotionList: {
    title: "Promotion List",
    breadcrumbs: ["dashboard"],
    content: `<div class='p-4'>Promotion List Page (TODO)</div>`,
  },
};

function loadPage(pageKey) {
  const page = pages[pageKey];
  if (!page) {
    document.getElementById("mainContent").innerHTML =
      "<h2>Page not found</h2>";
    return;
  }

  // --- ACTIVE NAV-LINK ---
  document
    .querySelectorAll(".sidebar .nav-link")
    .forEach((link) => link.classList.remove("active"));
  const linkId = pageKey + "Link";
  const activeLink = document.getElementById(linkId);
  if (activeLink) activeLink.classList.add("active");

  // Nếu là trang con của "Tour", mở submenu và active luôn menu cha
  const tourPages = ["tourList", "addTour"];
  if (tourPages.includes(pageKey)) {
    document.getElementById("tourMenuLink")?.classList.add("active");
    const submenu = document.getElementById("tourSubmenu");
    if (submenu && !submenu.classList.contains("show")) {
      new bootstrap.Collapse(submenu, { toggle: true });
    }
  } else {
    const submenu = document.getElementById("tourSubmenu");
    if (submenu && submenu.classList.contains("show")) {
      new bootstrap.Collapse(submenu, { toggle: true });
    }
    document.getElementById("tourMenuLink")?.classList.remove("active");
  }

  // Nếu là trang con của "Promotion", mở submenu và active luôn menu cha
  const promotionPages = ["promotionList", "addPromotion"];
  if (promotionPages.includes(pageKey)) {
    document.getElementById("promotionMenuLink")?.classList.add("active");
    const submenu = document.getElementById("promotionSubmenu");
    if (submenu && !submenu.classList.contains("show")) {
      new bootstrap.Collapse(submenu, { toggle: true });
    }
  } else {
    const submenu = document.getElementById("promotionSubmenu");
    if (submenu && submenu.classList.contains("show")) {
      new bootstrap.Collapse(submenu, { toggle: true });
    }
    document.getElementById("promotionMenuLink")?.classList.remove("active");
  }

  // --- HIỂN THỊ NỘI DUNG ---
  const breadcrumbHtml = `
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="fw-bold text-success mb-1">${page.title}</h2>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb small">
            ${page.breadcrumbs
              .map(
                (crumb) => `
              <li class="breadcrumb-item">
                <a href="javascript:void(0)" onclick="loadPage('${crumb}')"
                   class="text-decoration-none text-muted">${pages[crumb].title}</a>
              </li>`
              )
              .join("")}
            <li class="breadcrumb-item active text-success" aria-current="page">${
              page.title
            }</li>
          </ol>
        </nav>
      </div>
    </div>`;

  document.getElementById("mainContent").innerHTML =
    breadcrumbHtml + page.content;
  if (pageKey === "seller") {
    setTimeout(loadDraftToursForSeller, 0);
  }

  // Load các trang đặc biệt
  if (pageKey === "addTour") {
    setTimeout(() => initAddTourPage(), 0);
  } else if (pageKey === "tourList") {
    // 1️⃣ Load và render bảng
    loadTourList();

    // 2️⃣ Gắn listener cho nút Delete Selected
    setTimeout(() => {
      const delBtn = document.getElementById("btnDeleteSelected");
      if (!delBtn) return;

      delBtn.addEventListener("click", async () => {
        const checked = document.querySelectorAll(
          '#tourListBody tbody input[type="checkbox"]:checked'
        );
        const ids = Array.from(checked).map((cb) => cb.dataset.id);

        if (ids.length === 0) {
          showPopup("warning", "Chú ý", "Vui lòng chọn ít nhất 1 tour để xóa.");
          return;
        }
        if (!confirm(`Bạn có chắc muốn xóa ${ids.length} tour đã chọn?`))
          return;

        const token = localStorage.getItem("accessToken");
        let success = 0,
          fail = 0;
        await Promise.all(
          ids.map(async (id) => {
            try {
              const res = await fetch(`/tourify/api/tours/${id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` },
              });
              res.ok ? success++ : fail++;
            } catch {
              fail++;
            }
          })
        );

        showPopup(
          fail === 0 ? "success" : "warning",
          "Kết quả",
          `Xóa thành công ${success} tour${fail ? `, thất bại ${fail}` : ""}.`
        );
        loadTourList();
      });
    }, 0);

    // 3️⃣ Gắn listener cho nút Add Tour
    const btn = document.querySelector(".btn-mint-accent");
    if (btn) {
      btn.addEventListener("click", () => loadPage("addTour"));
    }

    // } else if (pageKey === "analytics") {
    //     // Khởi tạo analytics
    //     setTimeout(() => initAnalyticsPage(), 0);
    // } else if (pageKey === "analytics") {
    //     setTimeout(() => initAnalyticsPage(), 0); // ✅ THÊM DÒNG NÀY
    // }
  } else if (pageKey === "customers") {
    // ← Thêm block này
    // sau khi HTML inject xong, khởi tạo trang Customers
    setTimeout(() => initCustomersPage(), 0);
  } else if (pageKey === "analytics") {
    initAnalyticsPage();
  }
}

document.addEventListener("DOMContentLoaded", function () {
  loadPage("addTour");

  // Load avatar cho navbar ngay khi trang load
  loadUserAvatarForNavbar();

  const submenu = document.getElementById("tourSubmenu");
  const icon = document.querySelector("#tourToggle .toggle-icon");

  if (submenu && icon) {
    submenu.addEventListener("show.bs.collapse", () => {
      icon.classList.remove("fa-chevron-down");
      icon.classList.add("fa-chevron-up");
    });

    submenu.addEventListener("hide.bs.collapse", () => {
      icon.classList.remove("fa-chevron-up");
      icon.classList.add("fa-chevron-down");
    });
  }
});

// Đảm bảo avatar được load sau khi trang load hoàn toàn
window.addEventListener("load", function () {
  // Kiểm tra và load lại avatar nếu cần
  setTimeout(() => {
    if (!isAvatarLoaded()) {
      loadUserAvatarForNavbar();
    }
  }, 500);
});

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
        ? `<a href="${url}" target="_blank"><img src="${url}"  alt=""/></a>`
        : `<a href="${url}" target="_blank"><video src="${url}" controls muted></video></a>`;
  } else if (fileInput.files.length > 0) {
    const fileURL = URL.createObjectURL(fileInput.files[0]);
    mediaHTML =
      currentMediaType === "image"
        ? `<a href="${fileURL}" target="_blank"><img src="${fileURL}"  alt=""/></a>`
        : `<a href="${fileURL}" target="_blank"><video src="${fileURL}" controls muted></video></a>`;
  }

  // ✅ THÊM Ở ĐÂY
  if (mediaHTML) {
    preview.insertAdjacentHTML("beforeend", mediaHTML);
    calculateCompletion(); // ✅ Thêm dòng này để cập nhật phần trăm hoàn thành
  }

  bootstrap.Modal.getInstance(document.getElementById("mediaModal")).hide();
}

function isMediaValid() {
  const imageCount = document.querySelectorAll("#imagePreview img").length;
  const videoCount = document.querySelectorAll("#videoPreview video").length;
  return {
    valid: imageCount > 0 || videoCount > 0, // ✅ Chỉ cần 1 trong 2
    missing:
      imageCount === 0 && videoCount === 0
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
  badge.className = "badge ms-1 bg-" + (percent < 100 ? "danger" : "success");

  document.getElementById("addTourBtn").disabled = percent < 100;

  const msg = document.getElementById("missingFieldsMsg");
  if (missingFields.length > 0) {
    msg.innerHTML =
      "⚠️ Missing: " +
      missingFields
        .map(
          ({ label, scrollTo }) =>
            `<a href="javascript:void(0)" onclick="scrollToElement('${scrollTo}')" class="text-danger fw-semibold text-decoration-underline me-1">${label}</a>`
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
      el.classList.remove("border", "border-3", "border-danger", "rounded");
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
  observeMediaZone(document.querySelector("#imageDropzone"));
  observeMediaZone(document.querySelector("#videoDropzone"));

  calculateCompletion(); // Initial
});

function initAddTourPage() {
  // Gọi load data cho category & place mỗi lần vào Add Tour
  loadPlacesAndCategories();
  loadServicesAndActivities();
  // //Code P thêm
  // $("#categorySelect").select2({
  //     placeholder: "Select a category",
  //     width: "100%"
  // });
  // Select2 chỉ setup cho status ở đây
  // $("#statusSelect").select2({
  //     placeholder: "Select an option",
  //     width: "100%",
  //     allowClear: true
  // });

  if (typeof $.fn.select2 === "function" && $("#categorySelect").length) {
    $("#categorySelect").select2({
      placeholder: "Select a category",
      width: "100%",
    });
  }
  // Select2 chỉ setup cho status ở đây
  if (typeof $.fn.select2 === "function" && $("#statusSelect").length) {
    $("#statusSelect").select2({
      placeholder: "Select an option",
      width: "100%",
      allowClear: true,
    });
  }

  $("#statusSelect").on("change", function () {
    const selected = $(this).val();
    $("#statusBadge").text(selected);
  });

  $("#categorySelect, #place").on("change", function () {
    calculateCompletion();
  });

  requiredFields.forEach(({ selector }) => {
    const el = document.querySelector(selector);
    if (el) {
      el.addEventListener("input", calculateCompletion);
      el.addEventListener("change", calculateCompletion);
    }
  });

  observeMediaZone(document.querySelector("#imageDropzone"));
  observeMediaZone(document.querySelector("#videoDropzone"));

  setTimeout(() => {
    if (
      document.getElementById("completionBadge") &&
      document.getElementById("missingFieldsMsg")
    ) {
      calculateCompletion();
    }
  }, 100);
  // Gán lại sự kiện click cho nút Add Tour mỗi lần vào trang này
  const addBtn = document.getElementById("addTourBtn");
  if (addBtn) {
    addBtn.onclick = handleAddTour; // handleAddTour là hàm submit tour (async function ở addTour.js hoặc trong cùng file)
  }
}

function observeMediaZone(zone) {
  if (!zone) return;
  const observer = new MutationObserver(calculateCompletion);
  observer.observe(zone, { childList: true, subtree: true });
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

function statusBadge(status) {
  switch ((status || "").toUpperCase()) {
    case "PUBLISHED":
      return `<span class="badge bg-success-subtle text-success">Published</span>`;
    case "ACTIVE":
      return `<span class="badge bg-success-subtle text-success">Active</span>`;
    case "DRAFT":
      return `<span class="badge bg-secondary-subtle text-secondary">Draft</span>`;
    case "LOW_STOCK":
      return `<span class="badge bg-warning-subtle text-warning">Low Stock</span>`;
    case "OUT_OF_STOCK":
      return `<span class="badge bg-danger-subtle text-danger">Out of Stock</span>`;
    case "ARCHIVED":
      return `<span class="badge bg-light text-dark">Archived</span>`;
    default:
      return `<span class="badge bg-light text-dark">${status}</span>`;
  }
}

function sortBy(field) {
  // toggle nếu click lại cùng field
  if (sortField === field) {
    sortDir = sortDir === "asc" ? "desc" : "asc";
  } else {
    sortField = field;
    sortDir = "asc";
  }
  // sắp xếp toursData
  toursData.sort((a, b) => {
    let va = a[field],
      vb = b[field];
    if (field === "price") {
      va = Number(a.price) || 0;
      vb = Number(b.price) || 0;
    } else {
      va = (va || "").toString().toLowerCase();
      vb = (vb || "").toString().toLowerCase();
    }
    if (va < vb) return sortDir === "asc" ? -1 : 1;
    if (va > vb) return sortDir === "asc" ? 1 : -1;
    return 0;
  });
  currentPage = 1;
  renderTourList();
}

function renderTourList() {
  const body = document.getElementById("tourListBody");
  if (!body) return;

  body.innerHTML = "";

  // Build table
  const table = document.createElement("table");
  table.className = "table table-hover table-modern mb-0 w-100 align-middle";
  table.innerHTML = `
    <thead class="table-success text-nowrap">
      <tr>
            <th style="width:36px"><input type="checkbox" id="chkAll"></th>

            <th style="min-width:200px; cursor:pointer"
                onclick="sortBy('tourName')">
              Tour <i id="icon-tourName" class="bi"></i>
            </th>

            <th style="min-width:95px">ID</th>

            <th style="min-width:120px; cursor:pointer"
                onclick="sortBy('categoryName')">
              Category <i id="icon-categoryName" class="bi"></i>
            </th>

            <th style="min-width:110px; cursor:pointer"
                onclick="sortBy('price')">
              Price <i id="icon-price" class="bi"></i>
            </th>
        <th style="min-width:90px">Status</th>
        <th style="min-width:110px">Created By</th>
        <th class="text-center" style="width:90px">Action</th>
      </tr>
    </thead>
    <tbody></tbody>
  `;
  body.appendChild(table);

  const tbody = table.querySelector("tbody");
  if (!Array.isArray(toursData) || toursData.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center py-4">
          <div class="alert alert-warning mb-0">Chưa có tour nào.</div>
        </td>
      </tr>
    `;
    renderPagination();
    return;
  }

  // Paging
  const start = (currentPage - 1) * pageSize;
  const end = start + pageSize;
  const list = toursData.slice(start, end);

  // Use DocumentFragment for performance
  const frag = document.createDocumentFragment();

  for (const tour of list) {
    const thumb =
      tour.thumbnail || "https://via.placeholder.com/40x40?text=IMG";
    const price = (Number(tour.price) || 0).toLocaleString("vi-VN") + " ₫";
    const category = tour.categoryName || "-";
    const regs =
      typeof tour.bookedCustomerCount === "number" &&
      !isNaN(tour.bookedCustomerCount)
        ? tour.bookedCustomerCount
        : Number(tour.bookedCustomerCount) || 0;
    const creator = tour.createdByUserName || "-";
    const regText = `${regs} người đăng ký`;

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><input type="checkbox" data-id="${tour.tourId}"></td>
      <td>
        <div class="d-flex align-items-center gap-2">
          <img
            src="${thumb}"
            class="rounded"
            width="40"
            height="40"
            style="object-fit:cover; border:1.5px solid #ddd;"
          />
          <div>
            <div
              class="fw-semibold lh-sm mb-1"
              style="font-size:1.04rem;"
            >${tour.tourName || "-"}</div>
            <span
              class="text-muted small"
              style="font-size:0.97em;"
            >${regText}</span>
          </div>
        </div>
      </td>
      <td class="align-middle text-break">${tour.tourId}</td>
      <td class="align-middle">${category}</td>
      <td class="align-middle text-success fw-semibold">${price}</td>
      <td class="align-middle">${statusBadge(tour.status)}</td>
      <td class="align-middle">${creator}</td>
      <td class="align-middle text-center">
        <i
          class="bi bi-eye text-success me-2"
          title="View"
          onclick="viewTour('${tour.tourId}')"
        ></i>
        <i
          class="bi bi-pencil text-warning me-2"
          title="Edit"
          onclick="editTour('${tour.tourId}')"
        ></i>
        <i
          class="bi bi-trash text-danger"
          title="Delete"
          onclick="deleteTour('${tour.tourId}')"
        ></i>
      </td>
    `;
    frag.appendChild(tr);
  }

  tbody.appendChild(frag);
  // ⚙️ cập nhật icon mũi tên
  ["tourName", "categoryName", "price"].forEach((field) => {
    const icon = document.getElementById(`icon-${field}`);
    if (!icon) return;
    icon.className = "bi"; // reset classes
    if (sortField === field) {
      icon.classList.add(
        sortDir === "asc" ? "bi-caret-up-fill" : "bi-caret-down-fill"
      );
    } else {
      icon.classList.add("bi-caret-down");
    }
  });

  // === Select All Checkbox ===
  const chkAll = table.querySelector("#chkAll");
  if (chkAll) {
    chkAll.addEventListener("change", function () {
      const isChecked = this.checked;
      // đánh dấu hoặc bỏ đánh dấu tất cả các checkbox trong tbody
      table.querySelectorAll('tbody input[type="checkbox"]').forEach((cb) => {
        cb.checked = isChecked;
      });
    });
  }
  renderPagination();
}

function viewTour(tourId) {
  window.location.href = `/tourify/tourDetailDashboard?id=${tourId}`;
}

async function deleteTour(tourId) {
  if (!confirm("Bạn có chắc muốn xóa tour này?")) return;
  const token = localStorage.getItem("accessToken");

  try {
    const res = await fetch(`/tourify/api/tours/${tourId}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${token}` },
    });
    const result = await res.json();

    if (res.ok && result.code === 1000) {
      showPopup("success", "Thành công", "Xóa tour thành công");
      // → Reload lại danh sách (SPA) hoặc reload toàn bộ trang
      loadTourList(); // nếu bạn muốn refresh chỉ table
      // window.location.reload(); // nếu bạn muốn reload hẳn trang
    } else {
      showPopup("danger", "Thất bại", result.message || "Xóa tour thất bại");
    }
  } catch (err) {
    console.error(err);
    showPopup("danger", "Lỗi", "Không thể kết nối máy chủ");
  }
}

function renderPagination() {
  const totalPages = Math.ceil(toursData.length / pageSize);
  if (totalPages <= 1) return;

  const pagination = document.createElement("nav");
  pagination.className = "mt-4";
  pagination.innerHTML = `
    <ul class="pagination justify-content-center">
      <li class="page-item ${currentPage === 1 ? "disabled" : ""}">
        <a class="page-link" href="javascript:void(0)" onclick="changePage(${
          currentPage - 1
        })">Trang trước</a>
      </li>
      ${Array.from(
        { length: totalPages },
        (_, i) => `
        <li class="page-item ${currentPage === i + 1 ? "active" : ""}">
          <a class="page-link" href="javascript:void(0)" onclick="changePage(${
            i + 1
          })">${i + 1}</a>
        </li>`
      ).join("")}
      <li class="page-item ${currentPage === totalPages ? "disabled" : ""}">
        <a class="page-link" href="javascript:void(0)" onclick="changePage(${
          currentPage + 1
        })">Trang sau</a>
      </li>
    </ul>
  `;

  // Thêm vào đúng vị trí trong tourListBody
  const body = document.getElementById("tourListBody");
  if (body) body.appendChild(pagination);
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

document.addEventListener("DOMContentLoaded", function () {
  const token = localStorage.getItem("accessToken");
  const loginBtn = document.getElementById("login-link");
  const logoutBtn = document.getElementById("logout-link");

  if (token) {
    // Đã đăng nhập: Hiện Logout, ẩn Login
    if (logoutBtn) logoutBtn.style.display = "";
    if (loginBtn) loginBtn.style.display = "none";
  } else {
    // Chưa đăng nhập: Hiện Login, ẩn Logout
    if (logoutBtn) logoutBtn.style.display = "none";
    if (loginBtn) loginBtn.style.display = "";
  }

  // Gắn event login nếu muốn
  if (loginBtn) {
    loginBtn.onclick = function () {
      window.location.href = "/tourify/login"; // Đổi thành đường dẫn login của bạn
    };
  }
});

document.getElementById("logout-link")?.addEventListener("click", function () {
  localStorage.removeItem("accessToken");
  window.location.href = "/tourify/login"; // hoặc reload lại trang
});

// ==== TOAST POPUP - Hiển thị thông báo góc phải ====
/**
 * Hiển thị popup ở góc phải (Bootstrap Toast)
 * @param {'success'|'danger'} type
 * @param {string} title
 * @param {string} message
 */
function showPopup(type, title, message) {
  if (!toastEl) return;
  const header = toastEl.querySelector(".toast-header");
  header.className = `toast-header bg-${type} text-white`;
  document.getElementById("toastTitle").textContent = title;
  document.getElementById("toastBody").textContent = message;
  toast.show();
}

// ==== JWT decode helpers ====
function getRoleFromToken(token) {
  if (!token) return null;
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const payload = JSON.parse(jsonPayload);
    return (payload.role || "").toLowerCase();
  } catch {
    return null;
  }
}
function getSubCompanyIdFromToken(token) {
  if (!token) return null;
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    const payload = JSON.parse(jsonPayload);
    return payload.subCompanyId || payload.userId || null;
  } catch {
    return null;
  }
}

// ==== Format tiền tệ, ngày tháng ====
function formatVND(amount) {
  return (Number(amount) || 0).toLocaleString("vi-VN") + " ₫";
}
function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}
function formatAccountDate(isoString) {
  if (!isoString) return "";
  const d = new Date(isoString);
  return d.toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

// ================== ANALYTICS PAGE MAIN FUNCTION ===================
function initAnalyticsPage() {
  // --- Element reference ---
  const btnFilter = document.getElementById("btnFilter");
  const inpStart = document.getElementById("startDate");
  const inpEnd = document.getElementById("endDate");
  const tableDay = document.getElementById("tbodyDay");
  const tableMonth = document.getElementById("tbodyMonth");
  const tableYear = document.getElementById("tbodyYear");
  const sysTableDay = document.getElementById("systemTbodyDay");
  const sysTableMonth = document.getElementById("systemTbodyMonth");
  const sysTableYear = document.getElementById("systemTbodyYear");
  if (
    !btnFilter ||
    !inpStart ||
    !inpEnd ||
    !tableDay ||
    !tableMonth ||
    !tableYear
  )
    return;

  // --- Dữ liệu ---
  const revenueData = { day: [], month: [], year: [] };
  const sysRevenueData = { day: [], month: [], year: [] };
  const accessToken = localStorage.getItem("accessToken");

  // --- Chart object ---
  const chartObjects = {
    systemDay: null,
    systemMonth: null,
    systemYear: null,
    companyDay: null,
    companyMonth: null,
    companyYear: null,
  };

  // ========== RENDER TABLES ==========
  function renderSystemTable(type) {
    let data = sysRevenueData[type] || [];
    let tbody =
      type === "day"
        ? sysTableDay
        : type === "month"
        ? sysTableMonth
        : sysTableYear;
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!Array.isArray(data) || !data.length) {
      tbody.innerHTML = `<tr><td colspan="2" class="text-center text-muted">No data</td></tr>`;
      return;
    }
    for (const row of data) {
      let label = row.time || row.date || row.month || row.year || "-";
      let value = Number(row.totalRevenue) || 0;
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${label}</td><td>${formatVND(value)}</td>`;
      tbody.appendChild(tr);
    }
  }

  function renderCompanyTable(type) {
    let data = revenueData[type] || [];
    let tbody =
      type === "day" ? tableDay : type === "month" ? tableMonth : tableYear;
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!Array.isArray(data) || !data.length) {
      tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted">No data</td></tr>`;
      return;
    }
    for (const row of data) {
      let label = row.time || row.date || row.month || row.year || "-";
      let company = row.companyName || "-";
      let value = Number(row.totalRevenue) || 0;
      const tr = document.createElement("tr");
      tr.innerHTML = `<td>${label}</td><td>${company}</td><td>${formatVND(
        value
      )}</td>`;
      tbody.appendChild(tr);
    }
  }

  // ========== FETCH & RENDER ALL ==========
  async function fetchAndRenderAll() {
    const start = inpStart.value;
    const end = inpEnd.value;
    if (!start || !end) return;
    const role = getRoleFromToken(accessToken);

    // Ẩn/hiện các bảng tổng hệ thống cho đúng role
    document.querySelectorAll('[id^="systemRevenue"]').forEach((div) => {
      div.parentElement.style.display = role === "admin" ? "" : "none";
    });

    // Loading message
    [tableDay, tableMonth, tableYear].forEach(
      (tbody) =>
        (tbody.innerHTML = `<tr><td colspan="3" class="text-center text-secondary">Loading...</td></tr>`)
    );
    if (role === "admin") {
      [sysTableDay, sysTableMonth, sysTableYear].forEach(
        (tbody) =>
          (tbody.innerHTML = `<tr><td colspan="2" class="text-center text-secondary">Loading...</td></tr>`)
      );
    }

    try {
      const headers = { Authorization: "Bearer " + accessToken };
      if (role === "admin") {
        // ADMIN: gọi API tổng hệ thống và từng company
        const sysDayUrl = `/tourify/api/revenue/system/by-day?start=${start}&end=${end}`;
        const sysMonthUrl = `/tourify/api/revenue/system/by-month?start=${start}&end=${end}`;
        const sysYearUrl = `/tourify/api/revenue/system/by-year?start=${start}&end=${end}`;
        const cmpDayUrl = `/tourify/api/revenue/company/by-day?start=${start}&end=${end}`;
        const cmpMonthUrl = `/tourify/api/revenue/company/by-month?start=${start}&end=${end}`;
        const cmpYearUrl = `/tourify/api/revenue/company/by-year?start=${start}&end=${end}`;

        const [sysDay, sysMonth, sysYear, cmpDay, cmpMonth, cmpYear] =
          await Promise.all([
            fetch(sysDayUrl, { headers }).then((r) => r.json()),
            fetch(sysMonthUrl, { headers }).then((r) => r.json()),
            fetch(sysYearUrl, { headers }).then((r) => r.json()),
            fetch(cmpDayUrl, { headers }).then((r) => r.json()),
            fetch(cmpMonthUrl, { headers }).then((r) => r.json()),
            fetch(cmpYearUrl, { headers }).then((r) => r.json()),
          ]);
        sysRevenueData.day = Array.isArray(sysDay) ? sysDay : [];
        sysRevenueData.month = Array.isArray(sysMonth) ? sysMonth : [];
        sysRevenueData.year = Array.isArray(sysYear) ? sysYear : [];
        revenueData.day = Array.isArray(cmpDay) ? cmpDay : [];
        revenueData.month = Array.isArray(cmpMonth) ? cmpMonth : [];
        revenueData.year = Array.isArray(cmpYear) ? cmpYear : [];
      } else if (role === "sub_company") {
        const subCompanyId = getSubCompanyIdFromToken(accessToken);
        if (!subCompanyId) {
          [tableDay, tableMonth, tableYear].forEach(
            (tbody) =>
              (tbody.innerHTML = `<tr><td colspan="3" class="text-danger">Không lấy được subCompanyId từ token!</td></tr>`)
          );
          return;
        }
        const dayUrl = `/tourify/api/revenue/by-day?subCompanyId=${subCompanyId}&start=${start}&end=${end}`;
        const monthUrl = `/tourify/api/revenue/by-month?subCompanyId=${subCompanyId}&start=${start}&end=${end}`;
        const yearUrl = `/tourify/api/revenue/by-year?subCompanyId=${subCompanyId}&start=${start}&end=${end}`;
        const [d, m, y] = await Promise.all([
          fetch(dayUrl, { headers }).then((r) => r.json()),
          fetch(monthUrl, { headers }).then((r) => r.json()),
          fetch(yearUrl, { headers }).then((r) => r.json()),
        ]);
        revenueData.day = Array.isArray(d) ? d : [];
        revenueData.month = Array.isArray(m) ? m : [];
        revenueData.year = Array.isArray(y) ? y : [];
      } else {
        [tableDay, tableMonth, tableYear].forEach(
          (tbody) =>
            (tbody.innerHTML = `<tr><td colspan="3" class="text-danger">Không có quyền xem doanh thu!</td></tr>`)
        );
        [sysTableDay, sysTableMonth, sysTableYear].forEach(
          (tbody) =>
            (tbody.innerHTML = `<tr><td colspan="2" class="text-danger">Không có quyền xem doanh thu!</td></tr>`)
        );
        return;
      }

      // Render bảng
      const activeTab = document.querySelector(".tab-pane.active").id;
      if (activeTab === "revenue-day") {
        if (role === "admin") renderSystemTable("day");
        renderCompanyTable("day");
      }
      if (activeTab === "revenue-month") {
        if (role === "admin") renderSystemTable("month");
        renderCompanyTable("month");
      }
      if (activeTab === "revenue-year") {
        if (role === "admin") renderSystemTable("year");
        renderCompanyTable("year");
      }
      afterRender();
    } catch (err) {
      [tableDay, tableMonth, tableYear].forEach(
        (tbody) =>
          (tbody.innerHTML = `<tr><td colspan="3" class="text-danger">Lỗi tải dữ liệu!</td></tr>`)
      );
      if (role === "admin") {
        [sysTableDay, sysTableMonth, sysTableYear].forEach(
          (tbody) =>
            (tbody.innerHTML = `<tr><td colspan="2" class="text-danger">Lỗi tải dữ liệu!</td></tr>`)
        );
      }
      console.error(err);
    }
  }

  // ========== THỐNG KÊ HEADER ==========
  async function loadActiveToursCount() {
    try {
      const token = localStorage.getItem("accessToken");
      const role = getRoleFromToken(token)?.toLowerCase();
      let url = "/tourify/api/revenue/active-tours/count";

      // Nếu là sub_company thì truyền subCompanyId vào param (giả sử backend nhận param ?subCompanyId=)
      if (role === "sub_company") {
        const subCompanyId = getSubCompanyIdFromToken(token);
        if (!subCompanyId) throw new Error("Không lấy được subCompanyId!");
        url += `?subCompanyId=${encodeURIComponent(subCompanyId)}`;
      }

      const resp = await fetch(url, {
        headers: token ? { Authorization: "Bearer " + token } : {},
      });
      if (!resp.ok) throw new Error("Lỗi API active tours");
      const json = await resp.json();
      const count = json.result != null ? json.result : json.count || 0;
      document.getElementById("activeToursCount").textContent = count;
    } catch (err) {
      document.getElementById("activeToursCount").textContent = "—";
      console.error("Không lấy được số lượng active tours:", err);
    }
  }

  async function loadTopBookedTours(start, end) {
    try {
      const token = localStorage.getItem("accessToken");
      const role = getRoleFromToken(token)?.toLowerCase();
      let url = `/tourify/api/revenue/top-booked?limit=10`;

      // Nếu là sub_company thì truyền subCompanyId cho backend filter
      if (role === "sub_company") {
        const subCompanyId = getSubCompanyIdFromToken(token);
        if (!subCompanyId) throw new Error("Không lấy được subCompanyId!");
        url += `&subCompanyId=${encodeURIComponent(subCompanyId)}`;
      }
      // Nếu cần filter theo ngày thì truyền thêm start/end

      const resp = await fetch(url, {
        headers: token ? { Authorization: "Bearer " + token } : {},
      });
      if (!resp.ok) throw new Error("Lỗi API top booked tours");
      const json = await resp.json();
      const data = Array.isArray(json.result) ? json.result : [];
      const tbody = document.getElementById("topBookedToursTbody");
      tbody.innerHTML = "";
      if (data.length === 0) {
        tbody.innerHTML = `<tr><td colspan="2" class="text-center text-muted">No data</td></tr>`;
      } else {
        data.forEach((t) => {
          tbody.innerHTML += `
        <tr>
          <td>${t.tourName || "-"}</td>
          <td class="text-end">${t.bookingCount || 0}</td>
        </tr>`;
        });
      }
    } catch (err) {
      document.getElementById(
        "topBookedToursTbody"
      ).innerHTML = `<tr><td colspan="2" class="text-danger text-center">Lỗi tải dữ liệu</td></tr>`;
      console.error("Không lấy được top booked tours:", err);
    }
  }

  // **HÀM GỌI HEADER STATS**
  async function analyticsHeaderStats() {
    const start = inpStart.value;
    const end = inpEnd.value;
    await Promise.all([loadActiveToursCount(), loadTopBookedTours(start, end)]);
  }

  // ========== CHART RENDERING ==========
  function renderChart(type, isSystem) {
    const key = (isSystem ? "system" : "company") + capitalize(type);
    const chartId = `${key}Chart`;
    const chartWrapId = `${key}ChartWrap`;
    const tableWrapId = `${key}TableWrap`;
    const btnChartId = `btnShowChart${capitalize(key)}`;
    const btnTableId = `btnShowTable${capitalize(key)}`;

    let dataArr = isSystem ? sysRevenueData[type] : revenueData[type];
    let labels = [];
    let datasets = [];
    if (isSystem) {
      dataArr.forEach((row) => {
        let label = row.time || row.date || row.month || row.year || "-";
        labels.push(label);
      });
      datasets = [
        {
          label: "Total Revenue (VND)",
          data: dataArr.map((row) => Number(row.totalRevenue) || 0),
          backgroundColor: "#4097e3",
        },
      ];
    } else {
      // Multi company chart
      const companySet = new Set();
      const labelSet = new Set();
      dataArr.forEach((row) => {
        let label = row.time || row.date || row.month || row.year || "-";
        labelSet.add(label);
        companySet.add(row.companyName || "-");
      });
      labels = Array.from(labelSet);
      const companyMap = {};
      companySet.forEach((c) => (companyMap[c] = {}));
      dataArr.forEach((row) => {
        let label = row.time || row.date || row.month || row.year || "-";
        let company = row.companyName || "-";
        let value = Number(row.totalRevenue) || 0;
        companyMap[company][label] = value;
      });
      const colorList = [
        "#fd9134",
        "#00C49F",
        "#FFBB28",
        "#8884d8",
        "#d7c51b",
        "#ae31cf",
        "#24d463",
        "#e94560",
        "#497174",
      ];
      let colorIdx = 0;
      datasets = Array.from(companySet).map((company) => ({
        label: company,
        data: labels.map((label) => companyMap[company][label] || 0),
        backgroundColor: colorList[colorIdx++ % colorList.length],
      }));
    }
    document.getElementById(tableWrapId).classList.add("d-none");
    document.getElementById(chartWrapId).classList.remove("d-none");
    document.getElementById(btnChartId).classList.add("d-none");
    document.getElementById(btnTableId).classList.remove("d-none");

    if (chartObjects[key]) chartObjects[key].destroy();
    const ctx = document.getElementById(chartId).getContext("2d");
    chartObjects[key] = new Chart(ctx, {
      type: "bar",
      data: { labels: labels, datasets: datasets },
      options: {
        responsive: true,
        plugins: { legend: { display: true } },
        scales: {
          x: { title: { display: true, text: capitalize(type) } },
          y: {
            title: { display: true, text: "Revenue (VND)" },
            beginAtZero: true,
          },
        },
      },
    });
  }

  function showTable(type, isSystem) {
    const key = (isSystem ? "system" : "company") + capitalize(type);
    const chartWrapId = `${key}ChartWrap`;
    const tableWrapId = `${key}TableWrap`;
    const btnChartId = `btnShowChart${capitalize(key)}`;
    const btnTableId = `btnShowTable${capitalize(key)}`;
    document.getElementById(chartWrapId).classList.add("d-none");
    document.getElementById(tableWrapId).classList.remove("d-none");
    document.getElementById(btnChartId).classList.remove("d-none");
    document.getElementById(btnTableId).classList.add("d-none");
  }

  function addToggleEvents() {
    [
      ["day", true],
      ["month", true],
      ["year", true],
      ["day", false],
      ["month", false],
      ["year", false],
    ].forEach(([type, isSystem]) => {
      const key = (isSystem ? "system" : "company") + capitalize(type);
      const btnChartId = `btnShowChart${capitalize(key)}`;
      const btnTableId = `btnShowTable${capitalize(key)}`;
      if (document.getElementById(btnChartId)) {
        document.getElementById(btnChartId).onclick = () =>
          renderChart(type, isSystem);
      }
      if (document.getElementById(btnTableId)) {
        document.getElementById(btnTableId).onclick = () =>
          showTable(type, isSystem);
      }
    });
  }
  function afterRender() {
    addToggleEvents();
  }

  // ========== SỰ KIỆN UI ==========
  btnFilter.onclick = function () {
    fetchAndRenderAll();
    analyticsHeaderStats(); // ← Gọi lại header stats khi filter
  };
  document
    .getElementById("revenue-range-tabs")
    .addEventListener("click", function (e) {
      if (e.target.classList.contains("nav-link")) {
        setTimeout(() => {
          const activeTab = document.querySelector(".tab-pane.active").id;
          if (activeTab === "revenue-day") {
            if (getRoleFromToken(accessToken) === "admin")
              renderSystemTable("day");
            renderCompanyTable("day");
          }
          if (activeTab === "revenue-month") {
            if (getRoleFromToken(accessToken) === "admin")
              renderSystemTable("month");
            renderCompanyTable("month");
          }
          if (activeTab === "revenue-year") {
            if (getRoleFromToken(accessToken) === "admin")
              renderSystemTable("year");
            renderCompanyTable("year");
          }
          afterRender();
          analyticsHeaderStats(); // Gọi lại header stats khi đổi tab, đảm bảo filter đúng
        }, 50);
      }
    });

  // Set ngày mặc định cho input
  const today = new Date();
  inpEnd.value = today.toISOString().slice(0, 10);
  const lastMonth = new Date(
    today.getFullYear(),
    today.getMonth() - 1,
    today.getDate()
  );
  inpStart.value = lastMonth.toISOString().slice(0, 10);

  // Khởi động lần đầu
  fetchAndRenderAll();
  analyticsHeaderStats();
}

// Utility: capitalize
function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

// Utility: formatVND (nếu chưa có)
function formatVND(n) {
  return (n || 0).toLocaleString("vi-VN") + " ₫";
}

// Gọi khi HTML đã load xong
document.addEventListener("DOMContentLoaded", initAnalyticsPage);

// ===================== ACCOUNT LIST (CUSTOMERS) =====================
function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    return {};
  }
}

function isAdmin(role) {
  return role && role.toUpperCase() === "ADMIN";
}

function isActive(status) {
  return status && status.toUpperCase() === "ACTIVE";
}

async function loadAccounts(query = "") {
  const table = document.getElementById("accountTable");
  if (!table) return;
  const tbody = table.querySelector("tbody");
  if (!tbody) return;
  const token = localStorage.getItem("accessToken");
  if (!token) return;

  // Giải mã lấy role của người đăng nhập (không phân biệt hoa thường)
  const userInfo = parseJwt(token);
  const myRole = userInfo && userInfo.role ? userInfo.role.toUpperCase() : "";

  try {
    const resp = await fetch(
      "/tourify/api/user" +
        (query ? `?search=${encodeURIComponent(query)}` : ""),
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
      }
    );
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    const json = await resp.json();
    const users = json.result || [];
    tbody.innerHTML = "";
    users.forEach((u) => {
      let dob = u.dob ? formatAccountDate(u.dob) : "";
      const gender =
        u.gender === true || u.gender === "Male"
          ? "Male"
          : u.gender === false || u.gender === "Female"
          ? "Female"
          : "";
      const statusClass = isActive(u.status)
        ? "status-active"
        : "status-blocked";

      // Không phân biệt hoa thường khi kiểm tra role và status
      let lockBtn = "";
      const targetRole = u.role ? u.role.toUpperCase() : "";
      if (myRole === "ADMIN" && targetRole !== "ADMIN") {
        lockBtn = isActive(u.status)
          ? `<i class="fa fa-lock text-danger toggle-status-btn" title="Block Account" data-status="ACTIVE" data-userid="${u.userId}"></i>`
          : `<i class="fa fa-unlock text-success toggle-status-btn" title="Unblock Account" data-status="BLOCKED" data-userid="${u.userId}"></i>`;
      }

      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><input type="checkbox"></td>
        <td>${u.role || ""}</td>
        <td>
          <div class="d-flex align-items-center">
            <img src="${u.avatar || "/static/images/avatar_default.jpg"}"
                 style="width:36px;height:36px;border-radius:50%;object-fit:cover;margin-right:10px;border:2px solid #b7e4c7;">
            <div>
              <div style="font-weight:600; color:#22292f;">${
                u.userName || ""
              }</div>
              <div style="font-size:0.95em; color:#8b909a;">${
                u.email || ""
              }</div>
            </div>
          </div>
        </td>
        <td>${(u.firstName || "") + " " + (u.lastName || "")}</td>
        <td>${gender}</td>
        <td>${u.phoneNumber || ""}</td>
        <td>${u.address || ""}</td>
        <td>${dob}</td>
        <td><span class="${statusClass}">${u.status || ""}</span></td>
        <td class="action-btns">
          <i class="fa fa-pen text-primary" title="Edit"></i>
          ${lockBtn}
        </td>
      `;
      tbody.appendChild(tr);

      // Gắn sự kiện cho từng nút lock/unlock của từng user (nếu có)
      const toggleBtn = tr.querySelector(".toggle-status-btn");
      if (toggleBtn) {
        toggleBtn.addEventListener("click", async function () {
          const userId = this.getAttribute("data-userid");
          const status = this.getAttribute("data-status");
          if (!userId || !token) return;
          let url = "";
          let successMsg = "";
          if (status && status.toUpperCase() === "ACTIVE") {
            url = `/tourify/api/useradmins/${userId}/lock`;
            successMsg = "Khoá tài khoản thành công!";
          } else {
            url = `/tourify/api/useradmins/${userId}/unlock`;
            successMsg = "Mở khoá tài khoản thành công!";
          }
          try {
            const resp = await fetch(url, {
              method: "PUT",
              headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + token,
              },
            });
            if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
            alert(successMsg); // Có thể thay alert bằng toast UI đẹp hơn
            loadAccounts(query); // Reload lại list để update icon
          } catch (e) {
            alert("Có lỗi xảy ra khi đổi trạng thái tài khoản.");
            console.error(e);
          }
        });
      }
    });
  } catch (err) {
    console.error("Fetch users failed:", err);
  }
}

function initCustomersPage() {
  const table = document.getElementById("accountTable");
  if (!table) return;
  const tbody = table.querySelector("tbody");
  const searchInput = document.querySelector(
    'input[placeholder="Search account..."]'
  );
  if (!tbody || !searchInput) return;
  // Load lần đầu
  loadAccounts();
  // Debounce search
  searchInput.addEventListener("input", function () {
    clearTimeout(this._timeout);
    this._timeout = setTimeout(() => loadAccounts(this.value.trim()), 300);
  });
  // Action buttons (edit/delete) - có thể mở rộng sau
  tbody.addEventListener("click", function (e) {
    const btn = e.target.closest("i");
    if (!btn) return;
    // TODO: Gắn logic edit/delete nếu cần
    // Xử lý toggle block/unblock
    if (btn.classList.contains("toggle-status-btn")) {
      let status = btn.getAttribute("data-status");
      if (status === "ACTIVE") {
        btn.setAttribute("data-status", "BLOCKED");
        btn.classList.remove("fa-unlock", "text-success");
        btn.classList.add("fa-lock", "text-danger");
        btn.title = "Unblock Account";
      } else {
        btn.setAttribute("data-status", "ACTIVE");
        btn.classList.remove("fa-lock", "text-danger");
        btn.classList.add("fa-unlock", "text-success");
        btn.title = "Block Account";
      }
    }
  });
}

async function loadServicesAndActivities() {
  // Load Services
  try {
    const res = await fetch("/tourify/api/services");
    const arr = await res.json();
    const select = document.getElementById("servicesSelect");
    select.innerHTML = "";
    if (Array.isArray(arr)) {
      arr.forEach((s) => {
        const opt = document.createElement("option");
        opt.value = s.serviceId;
        opt.textContent = s.serviceName;
        select.appendChild(opt);
      });
    }
    if (typeof $ !== "undefined" && select)
      $(select).select2({ width: "100%", placeholder: "Select..." });
  } catch (e) {
    console.error("❌ Service fetch error:", e);
  }

  // Load Activities
  try {
    const res = await fetch("/tourify/api/activities");
    const arr = await res.json();
    const select = document.getElementById("activitiesSelect");
    select.innerHTML = "";
    if (Array.isArray(arr)) {
      arr.forEach((a) => {
        const opt = document.createElement("option");
        opt.value = a.activityId;
        opt.textContent = a.activityName;
        select.appendChild(opt);
      });
    }
    if (typeof $ !== "undefined" && select)
      $(select).select2({ width: "100%", placeholder: "Select..." });
  } catch (e) {
    console.error("❌ Activity fetch error:", e);
  }
}

async function loadDraftToursForSeller() {
  const token = localStorage.getItem("accessToken");
  try {
    const res = await fetch("/tourify/api/tours/all-draft", {
      headers: token ? { Authorization: "Bearer " + token } : {},
    });
    if (!res.ok) throw new Error("API error");
    const tours = await res.json();

    const tbody = document.querySelector("#sellerTable tbody");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (!Array.isArray(tours) || tours.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted">No draft tour found.</td></tr>`;
      return;
    }
    tours.forEach((tour, idx) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td><input type="checkbox" data-id="${tour.tourId}"></td>
        <td>
          <img src="${
            tour.thumbnail || "https://via.placeholder.com/60x40?text=IMG"
          }"
               alt="Thumb" style="width:60px;height:40px;object-fit:cover;border-radius:6px;border:1.5px solid #ddd;">
        </td>
        <td>
          <span class="fw-semibold">${tour.tourName || "-"}</span>
        </td>
        <td>
          <span>${tour.createdByUserName || "-"}</span>
        </td>
        <td class="text-success">${
          tour.price ? Number(tour.price).toLocaleString("vi-VN") + " ₫" : "-"
        }</td>
        <td>${tour.placeName || "-"}</td>
        <td>${
          tour.createdAt
            ? new Date(tour.createdAt).toLocaleDateString("vi-VN")
            : "-"
        }</td>
        <td>
          <button class="btn btn-outline-success btn-sm" title="Approve" onclick="approveTour('${
            tour.tourId
          }')">
            <i class="bi bi-check-lg"></i>
          </button>
          <button class="btn btn-outline-danger btn-sm" title="Delete" onclick="deleteTour('${
            tour.tourId
          }')">
            <i class="bi bi-trash"></i>
          </button>
        </td>
      `;
      tbody.appendChild(row);
    });
  } catch (e) {
    const tbody = document.querySelector("#sellerTable tbody");
    if (tbody)
      tbody.innerHTML = `<tr><td colspan="8" class="text-danger text-center">Error loading draft tours</td></tr>`;
    console.error(e);
  }
}

async function approveTour(tourId) {
  if (!confirm("Bạn chắc chắn muốn duyệt tour này?")) return;
  const token = localStorage.getItem("accessToken");
  try {
    const res = await fetch(`/tourify/api/tours/${tourId}/approve`, {
      method: "PUT",
      headers: { Authorization: "Bearer " + token },
    });
    if (!res.ok) throw new Error("Approve failed");
    // Thông báo
    showPopup && showPopup("success", "Thành công", "Tour đã được duyệt!");
    // Reload lại bảng
    loadDraftToursForSeller();
  } catch (e) {
    showPopup && showPopup("danger", "Thất bại", "Không thể duyệt tour!");
    console.error(e);
  }
}

async function deleteTour(tourId) {
  if (!confirm("Bạn chắc chắn muốn xóa tour này?")) return;
  const token = localStorage.getItem("accessToken");
  try {
    const res = await fetch(`/tourify/api/tours/${tourId}`, {
      method: "DELETE",
      headers: { Authorization: "Bearer " + token },
    });
    const data = await res.json();
    if (res.ok && data.code === 1000) {
      showPopup &&
        showPopup(
          "success",
          "Thành công",
          data.message || "Xóa tour thành công!"
        );
      loadDraftToursForSeller(); // Reload lại bảng, hoặc gọi hàm tương tự bạn đã dùng
    } else {
      showPopup &&
        showPopup("danger", "Thất bại", data.message || "Xóa tour thất bại!");
    }
  } catch (e) {
    showPopup && showPopup("danger", "Lỗi", "Không thể kết nối máy chủ!");
    console.error(e);
  }
}

// ==== ADD PROMOTION LOGIC ====
const promotionRequiredFields = [
  { selector: "#promotionCode", label: "Code" },
  { selector: "#promotionDescription", label: "Description" },
  { selector: "#promotionQuantity", label: "Quantity" },
  { selector: "#promotionMinPurchase", label: "Min Purchase" },
  { selector: "#promotionDiscountPercent", label: "Discount (%)" },
  {
    selector: "#promotionMinPurchaseDescription",
    label: "Min Purchase Description",
  },
  { selector: "#promotionStartTime", label: "Start Time" },
  { selector: "#promotionEndTime", label: "End Time" },
  { selector: "#promotionTourIds", label: "Apply to Tours", isSelect: true },
];

function calculatePromotionCompletion() {
  let filled = 0;
  const missingFields = [];
  promotionRequiredFields.forEach(({ selector, label, isSelect }) => {
    const el = document.querySelector(selector);
    if (el) {
      if (isSelect) {
        // Kiểm tra cho Select2 và select thường
        let hasSelection = false;

        // Kiểm tra Select2
        if (
          typeof $ !== "undefined" &&
          $(el).hasClass("select2-hidden-accessible")
        ) {
          const select2Data = $(el).select2("data");
          hasSelection = select2Data && select2Data.length > 0;
        } else {
          // Kiểm tra select thường
          hasSelection = el.selectedOptions && el.selectedOptions.length > 0;
        }

        if (hasSelection) {
          filled++;
        } else {
          missingFields.push({ label, scrollTo: selector });
        }
      } else if (el.value.trim() !== "") {
        filled++;
      } else {
        missingFields.push({ label, scrollTo: selector });
      }
    } else {
      missingFields.push({ label, scrollTo: selector });
    }
  });
  const totalFields = promotionRequiredFields.length;
  const percent = Math.round((filled / totalFields) * 100);
  const badge = document.getElementById("promotionCompletionBadge");
  if (badge) {
    badge.innerText = `${percent}%`;
    badge.className = "badge ms-1 bg-" + (percent < 100 ? "danger" : "success");
  }
  const btn = document.getElementById("addPromotionBtn");
  if (btn) btn.disabled = percent < 100;
  const msg = document.getElementById("promotionMissingFieldsMsg");
  if (msg) {
    if (missingFields.length > 0) {
      msg.innerHTML =
        "⚠️ Missing: " +
        missingFields
          .map(
            ({ label, scrollTo }) =>
              `<a href=\"javascript:void(0)\" onclick=\"scrollToPromotionElement('${scrollTo}')\" class=\"text-danger fw-semibold text-decoration-underline me-1\">${label}</a>`
          )
          .join(", ");
    } else {
      msg.innerHTML = "";
    }
  }
}

function scrollToPromotionElement(selector) {
  const el = document.querySelector(selector);
  if (el) {
    el.scrollIntoView({ behavior: "smooth", block: "center" });
    el.classList.add("border", "border-3", "border-danger", "rounded");
    if (["INPUT", "TEXTAREA", "SELECT"].includes(el.tagName)) {
      el.focus({ preventScroll: true });
    }
    setTimeout(() => {
      el.classList.remove("border", "border-3", "border-danger", "rounded");
    }, 2500);
  }
}

function initAddPromotionPage() {
  // Gắn sự kiện input/change cho các trường
  promotionRequiredFields.forEach(({ selector, isSelect }) => {
    const el = document.querySelector(selector);
    if (el) {
      el.addEventListener("input", calculatePromotionCompletion);
      el.addEventListener("change", calculatePromotionCompletion);
    }
  });
  setTimeout(() => {
    calculatePromotionCompletion();
  }, 100);
  // Gán sự kiện cho nút Add Promotion
  const addBtn = document.getElementById("addPromotionBtn");
  if (addBtn) {
    addBtn.onclick = handleAddPromotion;
  }

  // Gán sự kiện cho nút Add All Tours
  const addAllBtn = document.getElementById("addAllToursBtn");
  if (addAllBtn) {
    addAllBtn.onclick = handleAddAllTours;
  }

  // Gán sự kiện cho nút Clear All
  const clearAllBtn = document.getElementById("clearAllToursBtn");
  if (clearAllBtn) {
    clearAllBtn.onclick = handleClearAllTours;
  }

  // Load danh sách tour khi click vào select (chỉ load 1 lần)
  const select = document.getElementById("promotionTourIds");
  if (select) {
    let loaded = false;
    select.addEventListener("focus", function () {
      if (!loaded) {
        loadPromotionTours();
        loaded = true;
      }
    });
    // Nếu muốn reload mỗi lần click, bỏ biến loaded và chỉ giữ loadPromotionTours();
  }
}

async function loadPromotionTours() {
  try {
    const select = document.getElementById("promotionTourIds");
    if (!select) return;
    select.innerHTML = "<option disabled>Loading...</option>";
    const token = localStorage.getItem("accessToken");
    let url = "";
    let headers = {};
    const role = getRoleFromToken(token);
    if (role === "admin") {
      url = "/tourify/api/tours";
      headers = { Authorization: `Bearer ${token}` };
    } else if (role === "sub_company") {
      const subCompanyId = getSubCompanyIdFromToken(token);
      url = `/tourify/api/tours/my-tours`;
      headers = { Authorization: `Bearer ${token}` };
    } else {
      select.innerHTML = "<option disabled>No permission</option>";
      return;
    }
    const res = await fetch(url, { headers });
    const result = await res.json();
    select.innerHTML = "";
    // Xử lý nhiều kiểu trả về
    let tours = [];
    if (Array.isArray(result.result)) {
      tours = result.result;
    } else if (Array.isArray(result.data)) {
      tours = result.data;
    } else if (Array.isArray(result)) {
      tours = result;
    } else {
      console.warn(
        "Không tìm thấy danh sách tour phù hợp trong API trả về",
        result
      );
    }
    if (Array.isArray(tours)) {
      tours
        .filter((tour) => (tour.status || "").toUpperCase() === "ACTIVE")
        .forEach((tour) => {
          const opt = document.createElement("option");
          opt.value = tour.tourId;
          opt.textContent = tour.tourName || tour.tourId;
          select.appendChild(opt);
        });
    }
    if (typeof $ !== "undefined" && select) {
      $(select).select2({
        width: "100%",
        placeholder: "Select tours...",
        dropdownParent: $(select).parent(),
        dropdownPosition: "below",
      });

      // Thêm sự kiện change cho Select2 để tính lại completion
      $(select).on("change", function () {
        calculatePromotionCompletion();
      });
    }
  } catch (e) {
    const select = document.getElementById("promotionTourIds");
    if (select)
      select.innerHTML = "<option disabled>Error loading tours</option>";
    console.error("❌ Promotion tour fetch error:", e);
  }
}

// Hàm xử lý nút Add All Tours
function handleAddAllTours() {
  const select = document.getElementById("promotionTourIds");
  if (!select) return;

  // Nếu đang sử dụng Select2
  if (
    typeof $ !== "undefined" &&
    $(select).hasClass("select2-hidden-accessible")
  ) {
    // Lấy tất cả options có sẵn
    const allOptions = $(select).find("option:not(:disabled)");
    const allValues = allOptions
      .map(function () {
        return this.value;
      })
      .get();

    // Chọn tất cả
    $(select).val(allValues).trigger("change");
  } else {
    // Cho select thường
    const options = select.querySelectorAll("option:not([disabled])");
    options.forEach((option) => {
      option.selected = true;
    });
    // Trigger change event
    select.dispatchEvent(new Event("change"));
  }

  // Tính lại completion
  calculatePromotionCompletion();

  // Hiển thị thông báo
  showPopup("success", "Success", "All tours have been added to the promotion");
}

// Hàm xử lý nút Clear All
function handleClearAllTours() {
  const select = document.getElementById("promotionTourIds");
  if (!select) return;

  // Nếu đang sử dụng Select2
  if (
    typeof $ !== "undefined" &&
    $(select).hasClass("select2-hidden-accessible")
  ) {
    $(select).val(null).trigger("change");
  } else {
    // Cho select thường
    const options = select.querySelectorAll("option");
    options.forEach((option) => {
      option.selected = false;
    });
    // Trigger change event
    select.dispatchEvent(new Event("change"));
  }

  // Tính lại completion
  calculatePromotionCompletion();

  // Hiển thị thông báo
  showPopup(
    "success",
    "Success",
    "All tours have been cleared from the promotion"
  );
}

async function handleAddPromotion() {
  try {
    // Lấy dữ liệu từ form
    const tourSelect = document.getElementById("promotionTourIds");
    const tourIds = Array.from(tourSelect.selectedOptions).map(
      (opt) => opt.value
    );

    // Validate required fields
    const code = document.getElementById("promotionCode").value.trim();
    const description = document
      .getElementById("promotionDescription")
      .value.trim();
    const quantity = document.getElementById("promotionQuantity").value;
    const minPurchase = document.getElementById("promotionMinPurchase").value;
    const discountPercent = document.getElementById(
      "promotionDiscountPercent"
    ).value;
    const minPurchaseDescription = document
      .getElementById("promotionMinPurchaseDescription")
      .value.trim();
    const startTime = document.getElementById("promotionStartTime").value;
    const endTime = document.getElementById("promotionEndTime").value;

    // Validation
    if (!code) {
      showPopup("error", "Error", "Promotion code is required");
      return;
    }
    if (!quantity || quantity <= 0) {
      showPopup("error", "Error", "Quantity must be greater than 0");
      return;
    }
    if (!discountPercent || discountPercent <= 0 || discountPercent > 100) {
      showPopup("error", "Error", "Discount percent must be between 1 and 100");
      return;
    }
    if (!startTime || !endTime) {
      showPopup("error", "Error", "Start time and end time are required");
      return;
    }
    if (new Date(startTime) >= new Date(endTime)) {
      showPopup("error", "Error", "End time must be after start time");
      return;
    }
    if (tourIds.length === 0) {
      showPopup("error", "Error", "Please select at least one tour");
      return;
    }

    // Prepare request data
    const requestData = {
      code: code,
      description: description,
      quantity: parseInt(quantity),
      minPurchase: minPurchase ? parseInt(minPurchase) : null,
      discountPercent: parseInt(discountPercent),
      startTime: startTime + "T00:00:00", // Thêm giờ 00:00:00 cho ngày bắt đầu
      endTime: endTime + "T23:59:59", // Thêm giờ 23:59:59 cho ngày kết thúc
      conditions: minPurchaseDescription || null,
      tourIds: tourIds,
    };

    // Get token
    const token = localStorage.getItem("accessToken");
    if (!token) {
      showPopup("error", "Error", "Authentication required");
      return;
    }

    // Call API
    const response = await fetch("/tourify/api/promotions", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(requestData),
    });

    const result = await response.json();

    if (response.ok) {
      showPopup(
        "success",
        "Success",
        result.message || "Promotion created successfully"
      );
      // Reset form
      document.getElementById("promotionCode").value = "";
      document.getElementById("promotionDescription").value = "";
      document.getElementById("promotionQuantity").value = "";
      document.getElementById("promotionMinPurchase").value = "";
      document.getElementById("promotionDiscountPercent").value = "";
      document.getElementById("promotionMinPurchaseDescription").value = "";
      document.getElementById("promotionStartTime").value = "";
      document.getElementById("promotionEndTime").value = "";
      if (tourSelect) {
        tourSelect.innerHTML = "";
      }
      // Recalculate completion
      calculatePromotionCompletion();
    } else {
      showPopup(
        "error",
        "Error",
        result.message || "Failed to create promotion"
      );
    }
  } catch (error) {
    console.error("Error creating promotion:", error);
    showPopup("error", "Error", "An error occurred while creating promotion");
  }
}

// ==== LOAD USER AVATAR FOR NAVBAR ====
async function loadUserAvatarForNavbar() {
  try {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      console.log("Không có token, không thể load avatar");
      // Vẫn cập nhật username từ JWT nếu có
      updateNavbarAvatar("/static/images/avatar_default.jpg");
      return;
    }

    const response = await fetch("/tourify/api/user/info", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const result = await response.json();

    if (response.ok && result.result) {
      // Cập nhật avatar trên navbar
      const avatarUrl =
        result.result.avatar || "/static/images/avatar_default.jpg";
      updateNavbarAvatar(avatarUrl);

      // Lưu thông tin user vào localStorage để sử dụng sau
      localStorage.setItem("userInfo", JSON.stringify(result.result));
    } else {
      // Sử dụng avatar mặc định nếu không có
      updateNavbarAvatar("/static/images/avatar_default.jpg");
    }
  } catch (error) {
    console.error("Lỗi load avatar:", error);
    // Sử dụng avatar mặc định khi có lỗi
    updateNavbarAvatar("/static/images/avatar_default.jpg");
  }
}

function updateNavbarAvatar(avatarUrl) {
  // Tìm và cập nhật avatar trên navbar - sử dụng selector cụ thể hơn
  const navbarAvatars = document.querySelectorAll(
    '.topbar img.rounded-circle, .topbar img[width="32"], .topbar img[height="32"], .navbar img, .top-navbar img'
  );

  navbarAvatars.forEach((avatar) => {
    if (avatar.tagName === "IMG") {
      avatar.src = avatarUrl;
      // Thêm alt text cho accessibility
      avatar.alt = "User Avatar";
    } else {
      avatar.style.backgroundImage = `url(${avatarUrl})`;
    }
  });

  // Cập nhật username từ JWT token
  const token = localStorage.getItem("accessToken");
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      const username = payload.userName || payload.username || "User";

      const usernameElements = document.querySelectorAll(
        "#usernameDisplay, .username-display, .user-name"
      );
      usernameElements.forEach((element) => {
        element.textContent = username;
      });
    } catch (error) {
      console.error("Lỗi parse JWT token:", error);
    }
  }
}

// Tích hợp vào loadPage
const oldLoadPage = loadPage;
loadPage = function (pageKey) {
  oldLoadPage(pageKey);
  if (pageKey === "addPromotion") {
    setTimeout(initAddPromotionPage, 0);
  }

  // Load avatar cho navbar
  setTimeout(loadUserAvatarForNavbar, 100);
};

// ==== UTILITY FUNCTIONS FOR AVATAR ====
// Hàm để refresh avatar (có thể gọi từ console hoặc khi cần)
function refreshNavbarAvatar() {
  loadUserAvatarForNavbar();
}

// Hàm để kiểm tra xem avatar đã được load chưa
function isAvatarLoaded() {
  const avatarElements = document.querySelectorAll(
    ".topbar img.rounded-circle"
  );
  return (
    avatarElements.length > 0 &&
    avatarElements[0].src !== "https://randomuser.me/api/portraits/men/32.jpg"
  );
}
