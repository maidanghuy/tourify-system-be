(function () {
    const form = document.getElementById("registerForm");
    const messageDiv = document.getElementById("message");

    function showValidation(input, message) {
        input.classList.add("is-invalid");
        input.nextElementSibling.textContent = message;
    }

    function clearValidation(input) {
        input.classList.remove("is-invalid");
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        messageDiv.textContent = "";
        messageDiv.className = "text-center";

        [...form.elements].forEach((el) => clearValidation(el));

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        const password = form.password.value;
        const passwordConfirm = form.passwordConfirm.value;

        if (password !== passwordConfirm) {
            showValidation(form.passwordConfirm, "Passwords do not match.");
            messageDiv.textContent = "Passwords do not match.";
            messageDiv.classList.add("text-danger");
            return;
        }

        const data = {
            firstName: form.firstName.value.trim(),
            lastName: form.lastName.value.trim(),
            userName: form.userName.value.trim(),
            email: form.email.value.trim(),
            passwordConfirm: passwordConfirm,
            password: password,
        };

        console.log("Registering with data:", data);

        try {
            const response = await fetch("http://localhost:8080/tourify/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data),
            });
            if (response.ok) {
                // Lưu username vào localStorage
                localStorage.setItem("username", data.userName);

                messageDiv.textContent =
                    "We have sent a confirmation email. Please verify it to create your account.";
                messageDiv.classList.add("text-success");
                form.reset();
            } else {
                let errorText = "Registration failed.";
                try {
                    const json = await response.json();
                    if (typeof json === "string") {
                        errorText = json;
                    } else if (json.message) {
                        errorText = json.message;
                    } else if (typeof json === "object") {
                        const msg = JSON.stringify(json);
                        if (msg.includes("Email already exists")) {
                            showValidation(form.email, "Email already exists.");
                            errorText = "Email already exists.";
                        } else if (msg.includes("Username already exists")) {
                            showValidation(form.userName, "Username already exists.");
                            errorText = "Username already exists.";
                        }
                    }
                } catch {
                    errorText = await response.text();
                }
                messageDiv.textContent = errorText;
                messageDiv.classList.add("text-danger");
            }
        } catch (error) {
            messageDiv.textContent = "An error occurred. Please try again later.";
            messageDiv.classList.add("text-danger");
        }
    });
})();


document.querySelectorAll(".toggle-password").forEach((icon) => {
    icon.addEventListener("click", function () {
        const passwordInput = document.getElementById("password");
        const passwordConfirmInput = document.getElementById("passwordConfirm");
        const isPassword = passwordInput.getAttribute("type") === "password";

        // Toggle both password fields together
        passwordInput.setAttribute("type", isPassword ? "text" : "password");
        passwordConfirmInput.setAttribute("type", isPassword ? "text" : "password");

        // Toggle both eye icons together
        document.querySelectorAll(".toggle-password").forEach((eyeIcon) => {
            eyeIcon.classList.toggle("fa-eye");
            eyeIcon.classList.toggle("fa-eye-slash");
        });
    });
});