document.addEventListener("DOMContentLoaded", () =>
{
    const dropdown = document.querySelector(".dropdown");
    const selected = dropdown.querySelector(".dropdown_selected");
    const menu = dropdown.querySelector(".dropdown_menu");
    const items = dropdown.querySelectorAll(".dropdown_item");
    const consoles = document.querySelectorAll(".wrapper_console");

    // Toggle menu
    selected.addEventListener("click", (e) =>
    {
        menu.style.display = menu.style.display === "block" ? "none" : "block";
        e.stopPropagation();
    });

    // Update selected when item clicked
    items.forEach(item =>
    {
        item.addEventListener("click", (e) =>
        {
            selected.textContent = item.textContent;
            menu.style.display = "none";

            // Hide all consoles
            consoles.forEach(c =>
            {
                c.style.display = "none";
            });

            // Show selected console
            const targetId = item.getAttribute("data-target");
            const targetConsole = document.getElementById(targetId);
            if (targetConsole)
            {
                targetConsole.style.display = "grid";
            }
        });
    });

    // Hide menu when outside clicked
    document.addEventListener("click", () =>
    {
        menu.style.display = "none";
    });
});
