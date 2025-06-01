document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector("form");
    const emailInput = document.getElementById("email");

    form.addEventListener("submit", async function (event) {
        event.preventDefault(); // Ngăn form submit mặc định

        const email = emailInput.value.trim();

        // Validate định dạng email đơn giản
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            alert("Please enter a valid email address.");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/tourify/api/auth/forgot-password", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email })
            });

            if (response.ok) {
                const data = await response.json();
                alert("Reset password email sent successfully.");
                // Bạn có thể redirect hoặc hiển thị thông báo ở đây
            } else {
                const error = await response.json();
                alert("Error: " + (error.message || "Unable to send reset email."));
            }
        } catch (error) {
            console.error("Fetch error:", error);
            alert("An unexpected error occurred. Please try again later.");
        }
    });
});
