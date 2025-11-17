// Check which courses student is enrolled in and update UI
        function updateEnrollmentStatus() {
            // Get all enrolled course IDs
            const enrolledCourseIds = new Set();
            document.querySelectorAll('#registeredCourses .course-card').forEach(card => {
                const courseId = card.getAttribute('data-course-id');
                if (courseId) {
                    enrolledCourseIds.add(courseId);
                }
            });

            // Update enroll buttons in the available courses table
            document.querySelectorAll('.btn-enroll').forEach(button => {
                const courseId = button.getAttribute('data-course-id');
                if (enrolledCourseIds.has(courseId)) {
                    button.textContent = 'Enrolled';
                    button.classList.add('btn-enrolled');
                    button.disabled = true;
                    button.type = 'button'; // Prevent form submission
                }
            });
        }

        // Search functionality
        const searchInput = document.getElementById('searchInput');
        const courseRows = document.querySelectorAll('.course-row');
        const noResults = document.getElementById('noResults');
        const coursesTable = document.getElementById('coursesTable');

        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            let visibleCount = 0;

            courseRows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(searchTerm)) {
                    row.style.display = '';
                    visibleCount++;
                } else {
                    row.style.display = 'none';
                }
            });

            if (visibleCount === 0) {
                coursesTable.style.display = 'none';
                noResults.style.display = 'block';
            } else {
                coursesTable.style.display = 'table';
                noResults.style.display = 'none';
            }
        });

        // Update statistics
        function updateStats() {
            const registeredCards = document.querySelectorAll('.course-card');
            const availableCourses = document.querySelectorAll('.course-row');
            
            let totalCredits = 0;
            registeredCards.forEach(card => {
                const creditsText = card.querySelector('.badge-credits').textContent;
                const credits = parseInt(creditsText.match(/\d+/)[0]);
                totalCredits += credits;
            });

            document.getElementById('enrolledCount').textContent = registeredCards.length;
            document.getElementById('totalCredits').textContent = totalCredits;
            document.getElementById('availableCount').textContent = availableCourses.length;
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

        // Convert day names to abbreviations
        function abbreviateDays() {
            const dayMap = {
                'MONDAY': 'Mon',
                'TUESDAY': 'Tue',
                'WEDNESDAY': 'Wed',
                'THURSDAY': 'Thu',
                'FRIDAY': 'Fri',
                'SATURDAY': 'Sat',
                'SUNDAY': 'Sun'
            };

            document.querySelectorAll('.badge-days').forEach(badge => {
                const fullDay = badge.textContent.trim();
                if (dayMap[fullDay]) {
                    badge.textContent = dayMap[fullDay];
                }
            });
        }

        // Initialize on page load
        updateEnrollmentStatus();
        updateStats();
        abbreviateDays();