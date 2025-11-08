// Update statistics
function updateStats() {
    const courseCards = document.querySelectorAll('.course-card');
    let totalCredits = 0;

    courseCards.forEach(card => {
        const creditsText = card.querySelector('.badge-credits').textContent;
        const credits = parseInt(creditsText.match(/\d+/)[0]);
        totalCredits += credits;
    });

    document.getElementById('totalCourses').textContent = courseCards.length;
    document.getElementById('totalCredits').textContent = totalCredits;
}

// Search functionality
const searchInput = document.getElementById('searchInput');
const courseCards = document.querySelectorAll('.course-card');
const noResults = document.getElementById('noResults');
const courseGrid = document.getElementById('courseGrid');

if (searchInput) {
    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        let visibleCount = 0;

        courseCards.forEach(card => {
            const text = card.textContent.toLowerCase();
            if (text.includes(searchTerm)) {
                card.style.display = '';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        // Show/hide no results message
        if (visibleCount === 0 && courseCards.length > 0) {
            noResults.style.display = 'block';
        } else {
            noResults.style.display = 'none';
        }
    });
}

// Auto-dismiss alerts after 5 seconds
setTimeout(() => {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        alert.style.transition = 'opacity 0.5s ease';
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 500);
    });
}, 5000);

// Enhance delete confirmation
document.querySelectorAll('.delete-confirm').forEach(form => {
    form.addEventListener('submit', function(e) {
        const courseName = this.closest('.course-card').querySelector('.course-name').textContent;
        const confirmed = confirm(`Are you sure you want to delete "${courseName}"?\n\nThis will also remove all student enrollments for this course. This action cannot be undone.`);
                
        if (!confirmed) {
            e.preventDefault();
        }
    });
});

// Initialize on page load
updateStats();