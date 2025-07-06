      let toursData = [];      // lưu toàn bộ danh sách tour
      let currentPage = 1;     // trang hiện tại
      const pageSize = 6;      // số tour mỗi trang

      /* modern-ui pages */
      const pages = {
        /* === 1. DASHBOARD === */
        dashboard: {
          title: "Dashboard",
          breadcrumbs: [],
          content: `
          <div class="container-fluid py-4">
            <!-- ==== Time-range pills + Add-Tour ==== -->
            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
              <div id="rangeFilter" class="btn-group" role="group">
                <button class="btn btn-outline-success active">All Time</button>
                <button class="btn btn-outline-success">12 Months</button>
                <button class="btn btn-outline-success">30 Days</button>
                <button class="btn btn-outline-success">7 Days</button>
                <button class="btn btn-outline-success">24 Hour</button>
              </div>
          
              <button id="btnAddTour" class="btn btn-success">
                <i class="bi bi-plus-lg me-2"></i>Add Tour
              </button>
            </div>
          
            <!-- ==== KPI cards ==== -->
            <div class="row gy-4 gx-4">
              <!-- Total Revenue -->
              <div class="col-xl-3 col-md-6">
                <div class="card-kpi h-100 p-4">
                  <div class="icon-wrap bg-revenue"><i class="bi bi-wallet2"></i></div>
                  <div class="flex-grow-1">
                    <span class="kpi-title">Total Revenue</span>
                    <h4 class="fw-bold mb-0" id="revenueCount">$75,500</h4>
                  </div>
                  <span class="badge-growth bg-success-subtle text-success">+10%</span>
                </div>
              </div>
          
              <!-- Total Booking -->
              <div class="col-xl-3 col-md-6">
                <div class="card-kpi h-100 p-4">
                  <div class="icon-wrap bg-booking"><i class="bi bi-cart-check"></i></div>
                  <div class="flex-grow-1">
                    <span class="kpi-title">Total Booking</span>
                    <h4 class="fw-bold mb-0" id="bookingCount">31,500</h4>
                  </div>
                  <span class="badge-growth bg-success-subtle text-success">+15%</span>
                </div>
              </div>
          
              <!-- Total Tour -->
              <div class="col-xl-3 col-md-6">
                <div class="card-kpi h-100 p-4">
                  <!-- 1️⃣ thêm icon vào đây -->
                  <div class="icon-wrap bg-tour">
                    <i class="bi bi-compass"></i>
                  </div>
                  <div class="flex-grow-1">
                    <span class="kpi-title">Total Tour</span>
                    <h4 class="fw-bold mb-0" id="tourCount">247</h4>
                  </div>
                  <!-- 2️⃣ badge 0 % giữ nguyên (màu xám) -->
                  <span class="badge-growth bg-secondary-subtle text-secondary">0%</span>
                </div>
              </div>
          
              <!-- Balance -->
              <div class="col-xl-3 col-md-6">
                <div class="card-kpi h-100 p-4">
                  <div class="icon-wrap bg-balance"><i class="bi bi-cash-coin"></i></div>
                  <div class="flex-grow-1">
                    <span class="kpi-title">Balance</span>
                    <h4 class="fw-bold mb-0" id="balanceCount">$24,500</h4>
                  </div>
                  <span class="badge-growth bg-danger-subtle text-danger">-25%</span>
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
                    <tbody><!-- JS render --></tbody>
                  </table>
          
                  <!-- simple pagination -->
                  <nav class="d-flex justify-content-end">
                    <ul class="pagination pagination-sm mb-0">
                      <li class="page-item disabled"><span class="page-link">&lt;</span></li>
                      <li class="page-item active"><span class="page-link">1</span></li>
                      <li class="page-item"><a class="page-link" href="#">2</a></li>
                      <li class="page-item"><a class="page-link" href="#">3</a></li>
                      <li class="page-item"><a class="page-link" href="#">&gt;</a></li>
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
                <h5 class="fw-bold mb-0">Recent Bookings
                  <span class="badge bg-success-subtle text-success ms-2">+2 Orders</span>
                </h5>
                <div class="d-flex gap-2">
                  <button class="btn btn-outline-success btn-sm" id="bookingFilterBtn">
                    <i class="bi bi-sliders"></i> Filters
                  </button>
                  <button class="btn btn-success btn-sm">See More</button>
                </div>
              </div>
          
              <table class="table table-modern" id="recentBookingTable">
                <thead class="table-success text-nowrap">
                  <tr>
                    <th><input type="checkbox"></th>
                    <th>Booking ID</th><th>Booking Tour</th><th>Customer</th>
                    <th>Total</th><th>Payment</th><th>Status</th><th class="text-end">Action</th>
                  </tr>
                </thead>
                <tbody><!-- JS render --></tbody>
              </table>
          
              <!-- pagination -->
              <nav class="d-flex justify-content-end">
                <ul class="pagination pagination-sm mb-0" id="recentBookingPagination"><!-- JS render --></ul>
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
                <button class="btn-mint-accent">
                  <i class="bi bi-plus"></i> Add Tour
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
          title: "Customers",
          breadcrumbs: ["dashboard"],
          content: `
          <div class="container-fluid py-4">
              <div class="admin-card p-4">
                <!-- Time Filter + Filter Button -->
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                  <!-- Search bên trái -->
                  <div class="flex-grow-1" style="min-width:220px;max-width:520px;">
                    <input class="form-control-mint w-100" type="text" placeholder="Search category..." />
                  </div>
                  <!-- 2 nút bên phải -->
                  <div class="d-flex gap-2 flex-shrink-0">
                    <button class="btn-mint-filter" type="button">
                      <i class="bi bi-funnel"></i> Filters
                    </button>
                    <button class="btn-mint-accent" data-bs-toggle="modal" data-bs-target="#catModal">
                      <i class="bi bi-plus"></i> Add Customer
                    </button>
                  </div>
                </div>
                <!-- Table -->
                <div class="table-responsive-mint">
                  <table class="mint-table w-100" id="bookingTable">
                    <thead>
                      <tr>
                        <th style="width:32px"><input type="checkbox"></th>
                        <th style="min-width:180px">Customer Name</th>
                        <th style="min-width:120px">Phone</th>
                        <th style="min-width:80px">Orders</th>
                        <th style="min-width:110px">Balance</th>
                        <th style="min-width:110px">Status</th>
                        <th style="min-width:110px">Created</th>
                        <th style="min-width:90px">Action</th>
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

        /* === 7. SELLERS === */
        seller: {
          title: "Seller",
          breadcrumbs: ["dashboard"],
          content: `
          <div class="container-fluid py-4">
              <div class="admin-card p-4">
                <!-- Search + Filter/Add buttons -->
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                  <!-- Search box bên trái -->
                  <div class="flex-grow-1" style="min-width:220px;max-width:520px;">
                    <input class="form-control-mint w-100" type="text" placeholder="Search seller..." id="sellerSearch" />
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
                        <th style="min-width:180px">Seller Name</th>
                        <th style="min-width:140px">Email</th>
                        <th style="min-width:120px">Phone</th>
                        <th style="min-width:110px">Joined</th>
                        <th style="min-width:100px">Products</th>
                        <th style="min-width:110px">Status</th>
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
  <!-- Total Revenue -->
              <div class="revenue-summary-card p-4 mb-4 d-flex align-items-center gap-4 shadow-sm flex-wrap">
                <div>
                  <div class="summary-label mb-1">Total Platform Revenue</div>
                  <div class="summary-value" id="totalRevenue">₫0</div>
                </div>
                <div class="ms-auto text-muted small">
                  Last updated: <span id="revUpdateTime">--</span>
                </div>
              </div>
            
              <!-- Time-Range Tabs -->
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
            
              <!-- Tab Content -->
              <div class="tab-content mb-4" id="revenue-range-tabs-content">
                <!-- Day View -->
                <div class="tab-pane fade show active" id="revenue-day" role="tabpanel">
                  <div class="card-glass p-4 shadow-sm">
                    <h5 class="fw-semibold mb-3">Revenue by Day</h5>
                    <div class="table-responsive">
                      <table class="modern-table w-100" id="companyRevenueDayTable">
                        <thead>
                          <tr>
                            <th style="min-width:120px">Date</th>
                            <th style="min-width:180px">Company</th>
                            <th style="min-width:120px">Revenue (VND)</th>
                          </tr>
                        </thead>
                        <tbody>
                          <!-- JS will render daily rows -->
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
            
                <!-- Month View -->
                <div class="tab-pane fade" id="revenue-month" role="tabpanel">
                  <div class="card-glass p-4 shadow-sm">
                    <h5 class="fw-semibold mb-3">Revenue by Month</h5>
                    <div class="table-responsive">
                      <table class="modern-table w-100" id="companyRevenueMonthTable">
                        <thead>
                          <tr>
                            <th style="min-width:120px">Month</th>
                            <th style="min-width:180px">Company</th>
                            <th style="min-width:120px">Revenue (VND)</th>
                          </tr>
                        </thead>
                        <tbody>
                          <!-- JS will render monthly rows -->
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
            
                <!-- Year View -->
                <div class="tab-pane fade" id="revenue-year" role="tabpanel">
                  <div class="card-glass p-4 shadow-sm">
                    <h5 class="fw-semibold mb-3">Revenue by Year</h5>
                    <div class="table-responsive">
                      <table class="modern-table w-100" id="companyRevenueYearTable">
                        <thead>
                          <tr>
                            <th style="min-width:120px">Year</th>
                            <th style="min-width:180px">Company</th>
                            <th style="min-width:120px">Revenue (VND)</th>
                          </tr>
                        </thead>
                        <tbody>
                          <!-- JS will render yearly rows -->
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>
            
              <!-- Overall Company Revenue (unchanged) -->
              <div class="card-glass p-4 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-3">
                  <h5 class="fw-bold mb-0">Company Revenue Statistics</h5>
                  <button class="btn-success-soft export-btn" onclick="exportTableToCSV('company-revenue.csv')">
                    <i class="bi bi-download me-2"></i>Export
                  </button>
                </div>
                <div class="table-responsive">
                  <table class="modern-table w-100" id="companyRevenueTable">
                    <thead>
                      <tr>
                        <th style="width:42px">#</th>
                        <th>Company Name</th>
                        <th>Bookings</th>
                        <th>Revenue (VND)</th>
                        <th>% of Platform</th>
                        <th>Ranking</th>
                      </tr>
                    </thead>
                    <tbody>
                      <!-- JS render rows -->
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          `,
        },
      };

      function loadPage(pageKey) {
      const page = pages[pageKey];
      if (!page) {
        document.getElementById("mainContent").innerHTML = "<h2>Page not found</h2>";
        return;
      }

      // --- ACTIVE NAV-LINK ---
      // Bỏ active ở mọi nav-link
      document.querySelectorAll('.sidebar .nav-link').forEach(link => link.classList.remove('active'));

      // Đặt active cho link hiện tại
      const linkId = pageKey + "Link";
      const activeLink = document.getElementById(linkId);
      if (activeLink) activeLink.classList.add('active');

      // Nếu là trang con của "Tour", mở submenu và active luôn menu cha
      const tourPages = ['tourList', 'addTour'];
      if (tourPages.includes(pageKey)) {
        // Active menu cha "Tour"
        document.getElementById('tourMenuLink')?.classList.add('active');

        // Mở submenu (nếu chưa mở)
        const submenu = document.getElementById("tourSubmenu");
        if (submenu && !submenu.classList.contains('show')) {
          new bootstrap.Collapse(submenu, { toggle: true });
        }
      } else {
        // Nếu sang trang khác, đóng submenu nếu đang mở
        const submenu = document.getElementById("tourSubmenu");
        if (submenu && submenu.classList.contains('show')) {
          new bootstrap.Collapse(submenu, { toggle: true });
        }
        // Bỏ active menu cha
        document.getElementById('tourMenuLink')?.classList.remove('active');
      }

  // --- HIỂN THỊ NỘI DUNG ---
  const breadcrumbHtml = `
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="fw-bold text-success mb-1">${page.title}</h2>
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb small">
            ${page.breadcrumbs.map(crumb => `
              <li class="breadcrumb-item">
                <a href="javascript:void(0)" onclick="loadPage('${crumb}')"
                   class="text-decoration-none text-muted">${pages[crumb].title}</a>
              </li>`).join("")}
            <li class="breadcrumb-item active text-success" aria-current="page">${page.title}</li>
          </ol>
        </nav>
      </div>
    </div>`;

  document.getElementById("mainContent").innerHTML = breadcrumbHtml + page.content;

  // Load các trang đặc biệt
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
        observeMediaZone(document.querySelector("#imageDropzone"));
        observeMediaZone(document.querySelector("#videoDropzone"));


        calculateCompletion(); // Initial
      });

      function initAddTourPage() {
  // Gọi load data cho category & place mỗi lần vào Add Tour
  loadPlacesAndCategories();

  // Select2 chỉ setup cho status ở đây
  $("#statusSelect").select2({
    placeholder: "Select an option",
    width: "100%",
    allowClear: true
  });

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
      function statusBadge(status){
        switch((status||"").toUpperCase()){
          case "PUBLISHED":    return `<span class="badge bg-success-subtle text-success">Published</span>`;
          case "ACTIVE":       return `<span class="badge bg-success-subtle text-success">Active</span>`;
          case "DRAFT":        return `<span class="badge bg-secondary-subtle text-secondary">Draft</span>`;
          case "LOW_STOCK":    return `<span class="badge bg-warning-subtle text-warning">Low Stock</span>`;
          case "OUT_OF_STOCK": return `<span class="badge bg-danger-subtle text-danger">Out of Stock</span>`;
          case "ARCHIVED":     return `<span class="badge bg-light text-dark">Archived</span>`;
          default:             return `<span class="badge bg-light text-dark">${status}</span>`;
        }
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
        <th style="min-width:200px">Tour</th>
        <th style="min-width:95px">ID</th>
        <th style="min-width:120px">Category</th>
        <th style="min-width:110px">Price</th>
        <th style="min-width:90px">Status</th>
        <th style="min-width:110px">Created By</th>
        <th class="text-end" style="width:90px">Action</th>
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
          const end   = start + pageSize;
          const list  = toursData.slice(start, end);

          // Use DocumentFragment for performance
          const frag = document.createDocumentFragment();

          for (const tour of list) {
              const thumb    = tour.thumbnail || "https://via.placeholder.com/40x40?text=IMG";
              const price    = (Number(tour.price) || 0).toLocaleString("vi-VN") + " ₫";
              const category = tour.categoryName || "-";
              const regs     = (typeof tour.bookedCustomerCount === "number" && !isNaN(tour.bookedCustomerCount))
                  ? tour.bookedCustomerCount
                  : Number(tour.bookedCustomerCount) || 0;
              const creator  = tour.createdByUserName || "-";
              const regText  = `${regs} người đăng ký`;

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
      <td class="align-middle text-end">
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
          renderPagination();
      }

      function renderPagination() {
  const totalPages = Math.ceil(toursData.length / pageSize);
  if (totalPages <= 1) return;

  const pagination = document.createElement("nav");
  pagination.className = "mt-4";
  pagination.innerHTML = `
    <ul class="pagination justify-content-center">
      <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
        <a class="page-link" href="javascript:void(0)" onclick="changePage(${currentPage - 1})">Trang trước</a>
      </li>
      ${Array.from({ length: totalPages }, (_, i) => `
        <li class="page-item ${currentPage === i + 1 ? 'active' : ''}">
          <a class="page-link" href="javascript:void(0)" onclick="changePage(${i + 1})">${i + 1}</a>
        </li>`).join('')}
      <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
        <a class="page-link" href="javascript:void(0)" onclick="changePage(${currentPage + 1})">Trang sau</a>
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
        loginBtn.onclick = function() {
            window.location.href = "/tourify/login"; // Đổi thành đường dẫn login của bạn
        }
    }
});

      document.getElementById("logout-link")?.addEventListener("click", function() {
    localStorage.removeItem("accessToken");
    window.location.href = "/tourify/login"; // hoặc reload lại trang
});

      function exportTableToCSV(filename) {
          let csv = [];
          const rows = document.querySelectorAll("#companyRevenueTable tr");
          for (let row of rows) {
              let cols = Array.from(row.querySelectorAll("th,td"));
              csv.push(cols.map(x => `"${x.innerText.replace(/"/g, '""')}"`).join(","));
          }
          const blob = new Blob([csv.join("\n")], {type:'text/csv'});
          const link = document.createElement("a");
          link.href = URL.createObjectURL(blob);
          link.download = filename;
          link.click();
      }
