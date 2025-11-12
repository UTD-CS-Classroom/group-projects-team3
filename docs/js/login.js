document.addEventListener("DOMContentLoaded", () =>
{
    const loginButton = document.getElementById("login_button");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const message = document.getElementById("login_message");

    login_button.addEventListener("click", () =>
    {
        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();

        // Hardcoded credentials
        if (email === "student@uni.edu" && password === "student123")
        {
            window.location.href = "student.html";
        }
        else if (email === "teacher@uni.edu" && password === "teacher123")
        {
            window.location.href = "teacher.html";
        }
        else if (email === "admin@uni.edu" && password === "admin123")
        {
            window.location.href = "admin.html";
        }
        else
        {
            message.style.visibility = 'visible';
            message.style.top = (loginButton.getBoundingClientRect().bottom + 20) + 'px';
        }
    });
});
