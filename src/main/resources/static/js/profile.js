document.addEventListener("DOMContentLoaded", () => {
    // Check if user is logged in
    const accessToken = localStorage.getItem("accessToken");

    if (!accessToken) {
        // Redirect to login page if no access token found
        window.location.href = "/tourify/login";
        return;
    }

    // Fetch user data and populate profile
    fetchUserData(accessToken);

    const editIcon = document.getElementById("edit-icon");
    const fileInput = document.getElementById("avatar-input");
    const profilePic = document.getElementById("profile-pic");

    editIcon.addEventListener("click", () => {
        fileInput.click();
    });

    fileInput.addEventListener("change", async () => {
        const file = fileInput.files[0];
        if (!file) return;

        // Tạo URL tạm để preview ảnh mới
        const imageUrl = URL.createObjectURL(file);
        profilePic.src = imageUrl;

        // Giả sử bạn sẽ upload ảnh lên một dịch vụ lưu trữ và lấy về URL thực sự.
        // Trong ví dụ này ta dùng imageUrl làm demo
        const avatarUrl = imageUrl;

        const username = localStorage.getItem("username");
        if (!username) {
            alert("Username not found in localStorage");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/tourify/api/user/avatar?username=${username}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ avatar: avatarUrl })
            });

            if (response.ok) {
                alert("Avatar updated successfully!");
            } else {
                const errorText = await response.text();
                alert("Failed to update avatar: " + errorText);
            }
        } catch (error) {
            console.error("Error updating avatar:", error);
            alert("An error occurred while updating avatar.");
        }
    });
});



document.addEventListener("DOMContentLoaded", () => {
    const tabLinks = document.querySelectorAll("#profileTabs .nav-link");
    const tabPanes = document.querySelectorAll(".tab-pane");


    tabLinks.forEach((link) => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const tab = link.getAttribute("data-tab");


            tabLinks.forEach((l) => l.classList.remove("active"));
            link.classList.add("active");


            tabPanes.forEach((pane) => {
                pane.classList.remove("active");
                pane.style.display = "none";
            });


            const target = document.getElementById(`tab-${tab}`);
            target.classList.add("active");
            target.style.display = "block";
        });
    });
});

function editField(field) {
    document.getElementById(`${field}-display`).classList.add("d-none");
    document.getElementById(`${field}-input`).classList.remove("d-none");


    document.getElementById(`${field}-change`).classList.add("d-none");
    document
        .getElementById(`${field}-confirm`)
        .classList.remove("d-none");
    document.getElementById(`${field}-cancel`).classList.remove("d-none");
}


function confirmField(field) {
    const newValue = document.getElementById(`${field}-input`).value;

    // Check if it's firstName or lastName field that needs API call
    if (field === 'firstName' || field === 'lastName') {
        updateNameViaAPI(field, newValue);
    } else {
        // For other fields, call their respective APIs
        updateFieldViaAPI(field, newValue);
    }
}

// Function to update name via API
async function updateNameViaAPI(field, newValue) {
    const accessToken = localStorage.getItem("accessToken");
    const username = localStorage.getItem("username");

    if (!accessToken || !username) {
        alert("Authentication required. Please login again.");
        return;
    }

    try {
        // Get current values for both firstName and lastName
        const currentFirstName = document.getElementById("firstName-display").textContent;
        const currentLastName = document.getElementById("lastName-display").textContent;

        // Prepare the request body
        const requestBody = {
            firstName: field === 'firstName' ? newValue : currentFirstName,
            lastName: field === 'lastName' ? newValue : currentLastName
        };

        const response = await fetch(`http://localhost:8080/tourify/api/user/name?username=${username}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.code === 1000) {
                // Update the display
                document.getElementById(`${field}-display`).textContent = newValue;
                document.getElementById(`${field}-display`).classList.remove("d-none");
                document.getElementById(`${field}-input`).classList.add("d-none");
                document.getElementById(`${field}-confirm`).classList.add("d-none");
                document.getElementById(`${field}-cancel`).classList.add("d-none");
                document.getElementById(`${field}-change`).classList.remove("d-none");

                // Update the user name in header
                const fullName = `${requestBody.firstName} ${requestBody.lastName}`.trim();
                document.querySelector(".user-name").textContent = fullName;

                alert("Name updated successfully!");
            } else {
                alert("Failed to update name: " + (data.message || "Unknown error"));
            }
        } else {
            const errorText = await response.text();
            alert("Failed to update name: " + errorText);
        }
    } catch (error) {
        console.error("Error updating name:", error);
        alert("An error occurred while updating name.");
    }
}

// Function to update other fields via API
async function updateFieldViaAPI(field, newValue) {
    const accessToken = localStorage.getItem("accessToken");
    const username = localStorage.getItem("username");

    if (!accessToken || !username) {
        alert("Authentication required. Please login again.");
        return;
    }

    try {
        // Map field names to API endpoints
        const fieldMapping = {
            'email': 'email',
            'phone': 'phone',
            'address': 'address',
            'dob': 'dob',
            'password': 'password'
        };

        const apiEndpoint = fieldMapping[field];
        if (!apiEndpoint) {
            alert("Unknown field: " + field);
            return;
        }

        // Prepare request body based on field type
        let requestBody = {};
        if (field === 'password') {
            requestBody = { password: newValue };
        } else if (field === 'dob') {
            // Convert date format if needed
            requestBody = { dob: newValue };
        } else {
            requestBody = { [field]: newValue };
        }

        const response = await fetch(`http://localhost:8080/tourify/api/user/${apiEndpoint}?username=${username}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(requestBody)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.code === 1000) {
                // Update the display
                document.getElementById(`${field}-display`).textContent = newValue;
                document.getElementById(`${field}-display`).classList.remove("d-none");
                document.getElementById(`${field}-input`).classList.add("d-none");
                document.getElementById(`${field}-confirm`).classList.add("d-none");
                document.getElementById(`${field}-cancel`).classList.add("d-none");
                document.getElementById(`${field}-change`).classList.remove("d-none");

                // Special handling for password field
                if (field === 'password') {
                    document.getElementById(`${field}-display`).textContent = '************';
                    document.getElementById(`${field}-input`).value = '';
                }

                alert(`${field.charAt(0).toUpperCase() + field.slice(1)} updated successfully!`);
            } else {
                alert(`Failed to update ${field}: ` + (data.message || "Unknown error"));
            }
        } else {
            const errorText = await response.text();
            alert(`Failed to update ${field}: ` + errorText);
        }
    } catch (error) {
        console.error(`Error updating ${field}:`, error);
        alert(`An error occurred while updating ${field}.`);
    }
}

function cancelField(field) {
    const currentValue = document.getElementById(
        `${field}-display`
    ).textContent;
    document.getElementById(`${field}-input`).value = currentValue;


    document
        .getElementById(`${field}-display`)
        .classList.remove("d-none");
    document.getElementById(`${field}-input`).classList.add("d-none");
    document.getElementById(`${field}-confirm`).classList.add("d-none");
    document.getElementById(`${field}-cancel`).classList.add("d-none");
    document.getElementById(`${field}-change`).classList.remove("d-none");
}

function showCoverModal() {
    const modal = new bootstrap.Modal(
        document.getElementById("coverModal")
    );
    modal.show();
}

function updateCoverImage() {
    const urlInput = document.getElementById("coverImageUrlInput").value;
    const fileInput = document.getElementById("coverFileInput").files[0];


    if (fileInput) {
        const reader = new FileReader();
        reader.onload = function (e) {
            document.querySelector(
                ".profile-header"
            ).style.backgroundImage = `url('${e.target.result}')`;
        };
        reader.readAsDataURL(fileInput);
    } else if (urlInput) {
        document.querySelector(
            ".profile-header"
        ).style.backgroundImage = `url('${urlInput}')`;
    }


    // Đóng modal
    const modal = bootstrap.Modal.getInstance(
        document.getElementById("coverModal")
    );
    modal.hide();
}

function showFullAvatar() {
    const modal = document.getElementById("avatarFullModal");
    const fullImg = document.getElementById("full-avatar-image");
    fullImg.src = document.getElementById("profile-pic").src;
    modal.style.display = "flex"; // <-- hiển thị ở đây thôi
}


function closeFullAvatar() {
    document.getElementById("avatarFullModal").style.display = "none";
}


let avatarFromFile = false;


function showAvatarModal() {
    avatarFromFile = false;
    document.getElementById("avatarImageUrlInput").value = "";
    document.getElementById("avatarPreview").style.display = "none";
    document.getElementById("fileInputWrapper").innerHTML = "";


    const modal = new bootstrap.Modal(
        document.getElementById("avatarModal")
    );
    modal.show();
}


function triggerAvatarFileInput() {
    const wrapper = document.getElementById("fileInputWrapper");
    wrapper.innerHTML = ""; // Xóa input cũ nếu có
    wrapper.style.display = "block";


    const input = document.createElement("input");
    input.type = "file";
    input.accept = "image/*";
    input.style.display = "none"; // không hiện, chỉ để gọi .click()


    input.addEventListener("change", loadAvatarFromFile);


    wrapper.appendChild(input);
    input.click(); // Tới đây mới mở dialog chọn file
}


function loadAvatarFromFile(event) {
    const file = event.target.files[0];
    if (file && file.type.startsWith("image/")) {
        avatarFromFile = true;
        const reader = new FileReader();
        reader.onload = function (e) {
            const img = e.target.result;
            document.getElementById("avatarPreview").src = img;
            document.getElementById("avatarPreview").style.display = "block";
            document.getElementById("profile-pic").src = img;


            // Tự động đóng modal sau khi chọn ảnh
            const modal = bootstrap.Modal.getInstance(
                document.getElementById("avatarModal")
            );
            modal.hide();
        };
        reader.readAsDataURL(file);
    }
}


function updateAvatar() {
    const url = document
        .getElementById("avatarImageUrlInput")
        .value.trim();
    const preview = document.getElementById("avatarPreview");


    if (url) {
        document.getElementById("profile-pic").src = url;
        preview.src = url;
        preview.style.display = "block";


        // Đóng modal sau khi cập nhật từ URL
        const modal = bootstrap.Modal.getInstance(
            document.getElementById("avatarModal")
        );
        modal.hide();
    }
}

// Function to fetch user data from API
async function fetchUserData(accessToken) {
    try {
        const response = await fetch('http://localhost:8080/tourify/api/auth/me', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user data');
        }

        const data = await response.json();

        if (data.code === 1000 && data.result) {
            const user = data.result;

            // Populate user information
            populateUserProfile(user);
        } else {
            console.error('Failed to get user data:', data.message);
        }
    } catch (error) {
        console.error('Error fetching user data:', error);
    }
}

// Function to populate profile with user data
function populateUserProfile(user) {
    // Update profile picture
    if (user.avatar) {
        document.getElementById("profile-pic").src = user.avatar;
    }

    // Update user name in header
    const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.userName;
    document.querySelector(".user-name").textContent = fullName;

    // Update navbar user name
    const navbarUserSpan = document.querySelector("#userMenu span span");
    if (navbarUserSpan) {
        navbarUserSpan.textContent = user.userName || "User";
    }

    // Update account fields
    updateField('firstName', user.firstName || 'Not set');
    updateField('lastName', user.lastName || 'Not set');
    updateField('email', user.email || '');
    updateField('phone', user.phoneNumber || 'Not set');
    updateField('address', user.address || 'Not set');

    // Update DOB if available
    if (user.dob) {
        const dobDate = new Date(user.dob);
        const formattedDob = dobDate.toLocaleDateString('en-GB'); // DD/MM/YYYY format
        const inputDob = dobDate.toISOString().split('T')[0]; // YYYY-MM-DD format for input
        updateField('dob', formattedDob, inputDob);
    } else {
        updateField('dob', 'Not set', '');
    }
}

// Function to update a field with new value
function updateField(fieldName, displayValue, inputValue = null) {
    const displayElement = document.getElementById(`${fieldName}-display`);
    const inputElement = document.getElementById(`${fieldName}-input`);

    if (displayElement) {
        displayElement.textContent = displayValue;
    }

    if (inputElement && inputValue !== null) {
        inputElement.value = inputValue;
    } else if (inputElement) {
        inputElement.value = displayValue;
    }
}