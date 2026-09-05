// Global Application State Management
let currentUser = null;
let activeTab = 'dashboard';
let stockChartInstance = null;
let currentDeleteTarget = null; // { url, callback }

// DOM Load Event
document.addEventListener('DOMContentLoaded', () => {
    updateHeaderClock();
    checkSession();
    setupEventListeners();
});

// Update Header Real-time Calendar
function updateHeaderClock() {
    const el = document.getElementById('header-date-clock');
    if (el) {
        const now = new Date();
        const options = { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' };
        el.textContent = now.toLocaleDateString('en-US', options);
    }
}

// Quick Autofill for Demo Access
function autofillDemo() {
    document.getElementById('login-username').value = 'admin';
    document.getElementById('login-password').value = 'admin123';
}

// Setup Form Submissions and Global Event Listeners
function setupEventListeners() {
    // 1. Login Form Submit
    document.getElementById('login-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        const submitBtn = e.target.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        submitBtn.disabled = true;
        submitBtn.textContent = 'Authenticating...';
        
        fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        })
        .then(res => {
            if (!res.ok) throw new Error('Invalid username or password credentials');
            return res.json();
        })
        .then(data => {
            currentUser = data;
            showApp();
            showToast('Authentication Successful', `Welcome to AnnaSahay, ${data.schoolName}.`, 'success');
        })
        .catch(err => {
            const errEl = document.getElementById('login-error');
            errEl.textContent = err.message;
            errEl.classList.remove('d-none');
        })
        .finally(() => {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        });
    });

    // 2. Attendance Roster Form Submit
    document.getElementById('attendance-roster-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const date = document.getElementById('att-date').value;
        const payload = [];
        for (let c = 1; c <= 8; c++) {
            const input = document.getElementById(`att-class-${c}`);
            payload.push({
                date: date,
                className: `Class ${c}`,
                presentStudents: parseInt(input.value) || 0
            });
        }

        fetch('/api/attendance', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (!res.ok) throw new Error('Failed to persist attendance roster');
            return res.json();
        })
        .then(() => {
            showToast('Records Saved', 'Daily classroom attendance stored successfully.', 'success');
            loadAttendanceRoster();
            if (activeTab === 'dashboard') loadDashboardTab();
        })
        .catch(err => showToast('Operation Failed', err.message, 'danger'));
    });

    // 3. Weekly Menu Form Save
    document.getElementById('menu-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('menu-id').value;
        const payload = {
            day: document.getElementById('menu-day').value,
            mealName: document.getElementById('menu-meal').value
        };

        const url = id ? `/api/menu/${id}` : '/api/menu';
        const method = id ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (!res.ok) throw new Error('Failed to save menu schedule');
            return res.json();
        })
        .then(() => {
            bootstrap.Modal.getInstance(document.getElementById('menuModal')).hide();
            showToast('Success', 'Weekly meal schedule updated successfully.', 'success');
            loadMenuTab();
        })
        .catch(err => showToast('Error', err.message, 'danger'));
    });

    // 4. Ingredient Form Save
    document.getElementById('ingredient-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('ingredient-id').value;
        const payload = {
            ingredientName: document.getElementById('ingredient-name').value,
            unit: document.getElementById('ingredient-unit').value
        };

        const url = id ? `/api/ingredients/${id}` : '/api/ingredients';
        const method = id ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (!res.ok) throw new Error('Failed to save ingredient');
            return res.json();
        })
        .then(() => {
            bootstrap.Modal.getInstance(document.getElementById('ingredientModal')).hide();
            showToast('Success', 'Ingredient catalog entry saved.', 'success');
            loadIngredientsTab();
        })
        .catch(err => showToast('Error', err.message, 'danger'));
    });

    // 5. Stock Form Save
    document.getElementById('stock-form-el').addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('stock-id').value;
        const payload = {
            ingredientId: document.getElementById('stock-ingredient-id').value,
            receivedQuantity: parseFloat(document.getElementById('stock-qty').value),
            receivedDate: document.getElementById('stock-date').value
        };

        if (id) {
            const fullPayload = {
                id: id,
                receivedQuantity: payload.receivedQuantity,
                availableQuantity: payload.receivedQuantity,
                receivedDate: payload.receivedDate,
                ingredient: { id: payload.ingredientId }
            };
            fetch(`/api/stock/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(fullPayload)
            })
            .then(res => {
                if (!res.ok) throw new Error('Failed to update stock consignment');
                return res.json();
            })
            .then(() => {
                bootstrap.Modal.getInstance(document.getElementById('stockModal')).hide();
                showToast('Success', 'Stock consignment updated.', 'success');
                loadStockTab();
            })
            .catch(err => showToast('Error', err.message, 'danger'));
        } else {
            fetch('/api/stock', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => {
                if (!res.ok) throw new Error('Failed to register stock inward consignment');
                return res.json();
            })
            .then(() => {
                bootstrap.Modal.getInstance(document.getElementById('stockModal')).hide();
                showToast('Success', 'Inward supply processed and ledger recorded.', 'success');
                loadStockTab();
            })
            .catch(err => showToast('Error', err.message, 'danger'));
        }
    });

    // 6. Daily Consumption Form Save
    document.getElementById('consumption-form-el').addEventListener('submit', (e) => {
        e.preventDefault();
        const payload = {
            ingredientId: document.getElementById('cons-ingredient-id').value,
            quantityUsed: parseFloat(document.getElementById('cons-qty').value),
            date: document.getElementById('cons-date').value
        };

        fetch('/api/consumption', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (!res.ok) return res.json().then(e => { throw new Error(e.error || 'Failed to log daily usage'); });
            return res.json();
        })
        .then(() => {
            bootstrap.Modal.getInstance(document.getElementById('consumptionModal')).hide();
            showToast('Consumption Recorded', 'Kitchen usage logged and stock deducted via FIFO.', 'success');
            loadConsumptionTab();
        })
        .catch(err => showToast('Operation Failed', err.message, 'danger'));
    });

    // 7. Generic Confirm Delete Action
    document.getElementById('confirm-delete-btn').addEventListener('click', () => {
        if (currentDeleteTarget) {
            fetch(currentDeleteTarget.url, { method: 'DELETE' })
            .then(res => {
                if (!res.ok) return res.json().then(e => { throw new Error(e.error || 'Failed to delete record'); });
                bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal')).hide();
                showToast('Success', 'Record deleted successfully.', 'success');
                if (currentDeleteTarget.callback) currentDeleteTarget.callback();
                currentDeleteTarget = null;
            })
            .catch(err => {
                bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal')).hide();
                showToast('Deletion Failed', err.message, 'danger');
                currentDeleteTarget = null;
            });
        }
    });
}

// Session Validation
function checkSession() {
    fetch('/api/auth/check')
    .then(res => {
        if (!res.ok) throw new Error('Unauthenticated');
        return res.json();
    })
    .then(data => {
        currentUser = data;
        showApp();
    })
    .catch(() => {
        showLogin();
    });
}

function showLogin() {
    document.getElementById('login-screen').classList.remove('d-none');
    document.getElementById('app-container').classList.add('d-none');
}

function showApp() {
    document.getElementById('login-screen').classList.add('d-none');
    document.getElementById('app-container').classList.remove('d-none');
    document.getElementById('school-name').textContent = currentUser.schoolName;
    document.getElementById('print-school-display').textContent = currentUser.schoolName;
    switchTab('dashboard');
}

function logout() {
    fetch('/api/auth/logout', { method: 'POST' })
    .then(() => {
        currentUser = null;
        showLogin();
        showToast('Logged Out', 'Your session has ended securely.', 'info');
    });
}

// Tab Switcher
function switchTab(tabId) {
    activeTab = tabId;
    
    // Update active class on sidebar
    const links = document.querySelectorAll('.sidebar-nav .nav-link');
    links.forEach(link => {
        if (link.getAttribute('onclick') && link.getAttribute('onclick').includes(`'${tabId}'`)) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Hide all tab panes
    const panes = document.querySelectorAll('.tab-pane-content');
    panes.forEach(pane => pane.classList.add('d-none'));

    // Reveal target pane
    const targetPane = document.getElementById(`tab-${tabId}`);
    if (targetPane) {
        targetPane.classList.remove('d-none');
        loadTabData(tabId);
    }
}

// Data Dispatcher for Selected Tab
function loadTabData(tabId) {
    if (tabId === 'dashboard') {
        loadDashboardTab();
    } else if (tabId === 'attendance') {
        document.getElementById('att-date').value = new Date().toISOString().substring(0, 10);
        loadAttendanceRoster();
    } else if (tabId === 'menu') {
        loadMenuTab();
    } else if (tabId === 'ingredients') {
        loadIngredientsTab();
    } else if (tabId === 'stock') {
        loadStockTab();
    } else if (tabId === 'consumption') {
        loadConsumptionTab();
    } else if (tabId === 'reports') {
        document.getElementById('report-start-date').value = new Date().toISOString().substring(0, 10);
        document.getElementById('report-end-date').value = new Date().toISOString().substring(0, 10);
        document.getElementById('report-month').value = new Date().toISOString().substring(0, 7);
        toggleReportFilter();
        generateReport();
    }
}

// 1. Dashboard Tab Data
function loadDashboardTab() {
    fetch('/api/dashboard/summary')
    .then(res => res.json())
    .then(data => {
        const total = data.totalStudents || 240;
        const present = data.todayAttendance || 0;
        const pct = total > 0 ? ((present / total) * 100).toFixed(1) : 0;

        document.getElementById('dash-total-students').textContent = total;
        document.getElementById('dash-today-attendance').textContent = present;
        document.getElementById('dash-attendance-percent').textContent = `${pct}% Present`;
        document.getElementById('dash-today-menu').textContent = data.todayMenu || 'None Scheduled';
        document.getElementById('dash-low-stock-count').textContent = data.lowStockCount;

        const alertFooter = document.getElementById('dash-stock-alert-footer');
        if (alertFooter) {
            if (data.lowStockCount > 0) {
                alertFooter.textContent = `${data.lowStockCount} ingredient(s) critical`;
                alertFooter.style.color = 'var(--rose-text)';
            } else {
                alertFooter.textContent = 'All stock levels optimal';
                alertFooter.style.color = 'var(--emerald-text)';
            }
        }

        // Render stock balance table
        const tbody = document.getElementById('dash-stock-tbody');
        tbody.innerHTML = '';
        
        const labels = [];
        const stockValues = [];
        const alertBannerEl = document.getElementById('dashboard-alerts');
        alertBannerEl.innerHTML = '';

        data.stocks.forEach(stock => {
            labels.push(stock.ingredientName);
            stockValues.push(stock.availableQuantity);

            const isLow = stock.isLowStock;
            const statusBadge = isLow 
                ? '<span class="status-pill status-pill-danger"><span class="status-dot-sm dot-danger"></span>Low Stock</span>'
                : '<span class="status-pill status-pill-success"><span class="status-dot-sm dot-success"></span>Adequate</span>';

            tbody.innerHTML += `
                <tr>
                    <td class="fw-semibold text-dark">${stock.ingredientName}</td>
                    <td><span class="fw-bold">${stock.availableQuantity}</span> <span class="text-secondary small">${stock.unit}</span></td>
                    <td class="text-end">${statusBadge}</td>
                </tr>
            `;

            if (isLow) {
                alertBannerEl.innerHTML += `
                    <div class="alert-ribbon" role="alert">
                        <div class="alert-ribbon-content">
                            <strong>Inventory Alert:</strong> <strong>${stock.ingredientName}</strong> has fallen below statutory buffer threshold with only <strong>${stock.availableQuantity} ${stock.unit}</strong> remaining.
                        </div>
                        <button class="btn btn-sm btn-outline-dark fw-semibold" onclick="switchTab('stock')">
                            Replenish Supplies &rarr;
                        </button>
                    </div>
                `;
            }
        });

        if (data.stocks.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">No ingredients registered in catalog.</td></tr>';
        }

        renderStockChart(labels, stockValues);
    })
    .catch(err => console.error('Dashboard load error:', err));
}

// Chart.js Doughnut Renderer with Enterprise Aesthetics
function renderStockChart(labels, values) {
    const canvas = document.getElementById('stockChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (stockChartInstance) {
        stockChartInstance.destroy();
    }

    stockChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    '#2563eb', // Royal Blue
                    '#059669', // Emerald
                    '#d97706', // Amber Gold
                    '#dc2626', // Crimson
                    '#4f46e5', // Indigo
                    '#0891b2', // Cyan
                    '#7c3aed'  // Purple
                ],
                borderWidth: 2,
                borderColor: '#ffffff',
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '68%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 10,
                        padding: 12,
                        font: { family: "'Inter', sans-serif", size: 11, weight: '500' }
                    }
                },
                tooltip: {
                    backgroundColor: '#0f172a',
                    padding: 10,
                    cornerRadius: 6
                }
            }
        }
    });
}

// 2. Attendance Tab Roster
function loadAttendanceRoster() {
    const date = document.getElementById('att-date').value;
    if (!date) return;

    fetch(`/api/attendance/by-date?date=${date}`)
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('attendance-roster-tbody');
        tbody.innerHTML = '';
        
        const recordMap = {};
        data.forEach(att => {
            recordMap[att.className] = att.presentStudents;
        });

        const enrolledStrength = [30, 30, 30, 30, 30, 30, 30, 30];

        for (let c = 1; c <= 8; c++) {
            const className = `Class ${c}`;
            const present = recordMap[className] !== undefined ? recordMap[className] : 0;
            const maxVal = enrolledStrength[c - 1];
            const pct = maxVal > 0 ? Math.round((present / maxVal) * 100) : 0;
            const barColor = pct >= 80 ? 'bg-success' : pct >= 50 ? 'bg-warning' : 'bg-danger';

            tbody.innerHTML += `
                <tr>
                    <td>
                        <span class="badge-code me-2">G-${c}</span>
                        <strong class="text-dark">${className}</strong>
                    </td>
                    <td class="text-secondary">${maxVal} Students</td>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <input type="number" id="att-class-${c}" class="form-control form-control-sm text-center fw-bold" 
                                min="0" max="${maxVal}" value="${present}" style="max-width: 90px;" 
                                oninput="updateClassProgress(${c}, ${maxVal})" required>
                            <span class="text-secondary small">/ ${maxVal}</span>
                        </div>
                    </td>
                    <td>
                        <div class="d-flex align-items-center gap-2" style="min-width: 140px;">
                            <div class="progress flex-grow-1" style="height: 6px; background-color: var(--border-subtle);">
                                <div id="prog-bar-${c}" class="progress-bar ${barColor}" role="progressbar" style="width: ${pct}%"></div>
                            </div>
                            <span id="prog-txt-${c}" class="small fw-semibold text-secondary" style="width: 38px;">${pct}%</span>
                        </div>
                    </td>
                </tr>
            `;
        }
    });
}

function fillAttendanceAll(val) {
    for (let c = 1; c <= 8; c++) {
        const input = document.getElementById(`att-class-${c}`);
        if (input) {
            input.value = val;
            updateClassProgress(c, 30);
        }
    }
}

function updateClassProgress(c, maxVal) {
    const input = document.getElementById(`att-class-${c}`);
    const bar = document.getElementById(`prog-bar-${c}`);
    const txt = document.getElementById(`prog-txt-${c}`);
    if (!input || !bar || !txt) return;
    const val = parseInt(input.value) || 0;
    const pct = Math.min(100, Math.max(0, Math.round((val / maxVal) * 100)));
    bar.style.width = pct + '%';
    txt.textContent = pct + '%';
    bar.className = 'progress-bar ' + (pct >= 80 ? 'bg-success' : pct >= 50 ? 'bg-warning' : 'bg-danger');
}

// 3. Weekly Meal Menu
function loadMenuTab() {
    fetch('/api/menu')
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('menu-tbody');
        tbody.innerHTML = '';
        
        const daysOrder = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        data.sort((a, b) => daysOrder.indexOf(a.day) - daysOrder.indexOf(b.day));

        data.forEach(menu => {
            tbody.innerHTML += `
                <tr>
                    <td><span class="status-pill status-pill-neutral fw-bold">${menu.day}</span></td>
                    <td class="fw-semibold text-dark">${menu.mealName}</td>
                    <td class="text-end">
                        <button class="btn btn-sm-action me-1" onclick="editMenu(${menu.id}, '${menu.day}', '${escapeString(menu.mealName)}')">
                            Edit
                        </button>
                        <button class="btn btn-sm-action btn-sm-danger" onclick="confirmDelete('/api/menu/${menu.id}', loadMenuTab)">
                            Delete
                        </button>
                    </td>
                </tr>
            `;
        });

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-4">No meal menus configured. Click "Add Menu Day" to configure one.</td></tr>';
        }
    });
}

function openMenuModal() {
    document.getElementById('menu-id').value = '';
    document.getElementById('menu-day').value = 'Monday';
    document.getElementById('menu-day').disabled = false;
    document.getElementById('menu-meal').value = '';
    new bootstrap.Modal(document.getElementById('menuModal')).show();
}

function editMenu(id, day, mealName) {
    document.getElementById('menu-id').value = id;
    document.getElementById('menu-day').value = day;
    document.getElementById('menu-day').disabled = true;
    document.getElementById('menu-meal').value = mealName;
    new bootstrap.Modal(document.getElementById('menuModal')).show();
}

// 4. Ingredients Catalog
function loadIngredientsTab() {
    fetch('/api/ingredients')
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('ingredients-tbody');
        tbody.innerHTML = '';
        data.forEach(ing => {
            tbody.innerHTML += `
                <tr>
                    <td><span class="badge-code">#ING-${String(ing.id).padStart(3, '0')}</span></td>
                    <td class="fw-semibold text-dark">${ing.ingredientName}</td>
                    <td><span class="status-pill status-pill-neutral">${ing.unit}</span></td>
                    <td class="text-end">
                        <button class="btn btn-sm-action me-1" onclick="editIngredient(${ing.id}, '${escapeString(ing.ingredientName)}', '${ing.unit}')">
                            Edit
                        </button>
                        <button class="btn btn-sm-action btn-sm-danger" onclick="confirmDelete('/api/ingredients/${ing.id}', loadIngredientsTab)">
                            Delete
                        </button>
                    </td>
                </tr>
            `;
        });

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No ingredients registered. Click "Add New Ingredient" to start.</td></tr>';
        }
    });
}

function openIngredientModal() {
    document.getElementById('ingredient-id').value = '';
    document.getElementById('ingredient-name').value = '';
    document.getElementById('ingredient-unit').value = 'kg';
    new bootstrap.Modal(document.getElementById('ingredientModal')).show();
}

function editIngredient(id, name, unit) {
    document.getElementById('ingredient-id').value = id;
    document.getElementById('ingredient-name').value = name;
    document.getElementById('ingredient-unit').value = unit;
    new bootstrap.Modal(document.getElementById('ingredientModal')).show();
}

// 5. Inward Stock Ledger
function loadStockTab() {
    fetch('/api/stock')
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('stock-tbody');
        tbody.innerHTML = '';
        data.sort((a, b) => b.id - a.id);

        data.forEach(stock => {
            const avail = stock.availableQuantity;
            const statusPill = avail === 0 
                ? '<span class="status-pill status-pill-neutral"><span class="status-dot-sm dot-neutral"></span>Depleted</span>'
                : avail < 10 
                ? '<span class="status-pill status-pill-danger"><span class="status-dot-sm dot-danger"></span>Low</span>'
                : '<span class="status-pill status-pill-success"><span class="status-dot-sm dot-success"></span>Available</span>';

            tbody.innerHTML += `
                <tr class="stock-row-item">
                    <td><span class="badge-code">#STK-${String(stock.id).padStart(3, '0')}</span></td>
                    <td class="fw-semibold text-dark stock-ing-name">${stock.ingredient.ingredientName}</td>
                    <td>${stock.receivedQuantity} <span class="text-secondary small">${stock.ingredient.unit}</span></td>
                    <td>
                        <div class="d-flex align-items-center gap-2">
                            <span class="fw-bold">${avail}</span> <span class="text-secondary small">${stock.ingredient.unit}</span>
                            ${statusPill}
                        </div>
                    </td>
                    <td class="text-secondary">${stock.receivedDate}</td>
                    <td class="text-end">
                        <button class="btn btn-sm-action me-1" onclick="editStock(${stock.id}, ${stock.ingredient.id}, ${stock.receivedQuantity}, '${stock.receivedDate}')">
                            Edit
                        </button>
                        <button class="btn btn-sm-action btn-sm-danger" onclick="confirmDelete('/api/stock/${stock.id}', loadStockTab)">
                            Delete
                        </button>
                    </td>
                </tr>
            `;
        });

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">No inward stock receipts recorded.</td></tr>';
        }
    });
}

function filterStockTable() {
    const filter = document.getElementById('stock-search').value.toLowerCase();
    const rows = document.querySelectorAll('.stock-row-item');
    rows.forEach(row => {
        const name = row.querySelector('.stock-ing-name').textContent.toLowerCase();
        if (name.includes(filter)) {
            row.classList.remove('d-none');
        } else {
            row.classList.add('d-none');
        }
    });
}

function populateIngredientDropdowns() {
    return fetch('/api/ingredients')
    .then(res => res.json())
    .then(ingredients => {
        const stockSelect = document.getElementById('stock-ingredient-id');
        const consSelect = document.getElementById('cons-ingredient-id');
        
        stockSelect.innerHTML = '';
        consSelect.innerHTML = '';

        ingredients.forEach(ing => {
            const opt = `<option value="${ing.id}">${ing.ingredientName} (${ing.unit})</option>`;
            stockSelect.innerHTML += opt;
            consSelect.innerHTML += opt;
        });
    });
}

function openStockModal() {
    populateIngredientDropdowns().then(() => {
        document.getElementById('stock-id').value = '';
        document.getElementById('stock-qty').value = '';
        document.getElementById('stock-date').value = new Date().toISOString().substring(0, 10);
        new bootstrap.Modal(document.getElementById('stockModal')).show();
    });
}

function editStock(id, ingredientId, qty, date) {
    populateIngredientDropdowns().then(() => {
        document.getElementById('stock-id').value = id;
        document.getElementById('stock-ingredient-id').value = ingredientId;
        document.getElementById('stock-qty').value = qty;
        document.getElementById('stock-date').value = date;
        new bootstrap.Modal(document.getElementById('stockModal')).show();
    });
}

// 6. Daily Consumption Logger
function loadConsumptionTab() {
    fetch('/api/consumption')
    .then(res => res.json())
    .then(data => {
        const tbody = document.getElementById('consumption-tbody');
        tbody.innerHTML = '';
        data.sort((a, b) => b.id - a.id);

        data.forEach(cons => {
            tbody.innerHTML += `
                <tr>
                    <td><span class="badge-code">#USE-${String(cons.id).padStart(3, '0')}</span></td>
                    <td class="text-secondary">${cons.date}</td>
                    <td class="fw-semibold text-dark">${cons.ingredient.ingredientName}</td>
                    <td><span class="fw-bold text-dark">${cons.quantityUsed}</span> <span class="text-secondary small">${cons.ingredient.unit}</span></td>
                    <td class="text-end">
                        <button class="btn btn-sm-action btn-sm-danger" onclick="confirmDelete('/api/consumption/${cons.id}', loadConsumptionTab)">
                            Revert &amp; Delete
                        </button>
                    </td>
                </tr>
            `;
        });

        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">No meal consumption recorded.</td></tr>';
        }
    });
}

function openConsumptionModal() {
    populateIngredientDropdowns().then(() => {
        document.getElementById('cons-qty').value = '';
        document.getElementById('cons-date').value = new Date().toISOString().substring(0, 10);
        new bootstrap.Modal(document.getElementById('consumptionModal')).show();
    });
}

// 7. Reports & Auditing
function toggleReportFilter() {
    const reportType = document.getElementById('report-type').value;
    const dateFilters = document.querySelectorAll('.report-date-filter');
    const monthFilter = document.querySelector('.report-month-filter');

    if (reportType === 'monthly') {
        dateFilters.forEach(f => f.classList.add('d-none'));
        monthFilter.classList.remove('d-none');
    } else if (reportType === 'inventory') {
        dateFilters.forEach(f => f.classList.add('d-none'));
        monthFilter.classList.add('d-none');
    } else {
        dateFilters.forEach(f => f.classList.remove('d-none'));
        monthFilter.classList.add('d-none');
    }
}

function generateReport() {
    const reportType = document.getElementById('report-type').value;
    const start = document.getElementById('report-start-date').value;
    const end = document.getElementById('report-end-date').value;
    const month = document.getElementById('report-month').value;
    
    const container = document.getElementById('report-table-container');
    container.innerHTML = '<div class="text-center my-5 text-secondary"><p>Generating regulatory report...</p></div>';
    
    const printTitle = document.getElementById('print-report-title');
    const todayStr = new Date().toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
    document.getElementById('print-generated-date').textContent = todayStr;

    if (reportType === 'attendance') {
        printTitle.textContent = `Daily Classroom Attendance Log (${start} to ${end})`;
        document.getElementById('report-title-label').textContent = 'Attendance Register';
        
        fetch(`/api/reports/attendance?startDate=${start}&endDate=${end}`)
        .then(res => res.json())
        .then(data => {
            let html = `
                <table class="table table-custom table-bordered">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Class Level</th>
                            <th class="text-end">Present Student Count</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            data.forEach(item => {
                html += `
                    <tr>
                        <td class="text-secondary">${item.date}</td>
                        <td class="fw-semibold text-dark">${item.className}</td>
                        <td class="text-end fw-bold">${item.presentStudents} Students</td>
                    </tr>
                `;
            });
            if (data.length === 0) {
                html += '<tr><td colspan="3" class="text-center text-muted py-4">No attendance logs found in this date range.</td></tr>';
            }
            html += '</tbody></table>';
            container.innerHTML = html;
        });

    } else if (reportType === 'usage') {
        printTitle.textContent = `Pantry Consumption Ledger (${start} to ${end})`;
        document.getElementById('report-title-label').textContent = 'Grain & Supply Usage Details';
        
        fetch(`/api/reports/stock-usage?startDate=${start}&endDate=${end}`)
        .then(res => res.json())
        .then(data => {
            let html = `
                <table class="table table-custom table-bordered">
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Ingredient Name</th>
                            <th class="text-end">Quantity Consumed</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            data.forEach(item => {
                html += `
                    <tr>
                        <td class="text-secondary">${item.date}</td>
                        <td class="fw-semibold text-dark">${item.ingredient.ingredientName}</td>
                        <td class="text-end fw-bold">${item.quantityUsed} ${item.ingredient.unit}</td>
                    </tr>
                `;
            });
            if (data.length === 0) {
                html += '<tr><td colspan="3" class="text-center text-muted py-4">No consumption logs found in this date range.</td></tr>';
            }
            html += '</tbody></table>';
            container.innerHTML = html;
        });

    } else if (reportType === 'inventory') {
        printTitle.textContent = 'Current Stock Inventory Status';
        document.getElementById('report-title-label').textContent = 'Available Stock Balances';

        fetch('/api/reports/inventory')
        .then(res => res.json())
        .then(data => {
            let html = `
                <table class="table table-custom table-bordered">
                    <thead>
                        <tr>
                            <th>Ingredient Catalog ID</th>
                            <th>Ingredient Name</th>
                            <th>Available Balance</th>
                            <th class="text-end">Threshold Status</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            data.forEach(item => {
                const isLow = item.availableQuantity < 10.0;
                const statusBadge = isLow 
                    ? '<span class="status-pill status-pill-danger"><span class="status-dot-sm dot-danger"></span>Low Stock</span>'
                    : '<span class="status-pill status-pill-success"><span class="status-dot-sm dot-success"></span>Adequate</span>';
                
                html += `
                    <tr>
                        <td><span class="badge-code">#ING-${String(item.ingredientId).padStart(3, '0')}</span></td>
                        <td class="fw-semibold text-dark">${item.ingredientName}</td>
                        <td><span class="fw-bold">${item.availableQuantity}</span> <span class="text-secondary small">${item.unit}</span></td>
                        <td class="text-end">${statusBadge}</td>
                    </tr>
                `;
            });
            if (data.length === 0) {
                html += '<tr><td colspan="4" class="text-center text-muted py-4">No inventory records available.</td></tr>';
            }
            html += '</tbody></table>';
            container.innerHTML = html;
        });

    } else if (reportType === 'monthly') {
        printTitle.textContent = `Monthly Audit Summary Report (${month})`;
        document.getElementById('report-title-label').textContent = 'Audit Summary Metrics';

        fetch(`/api/reports/monthly-summary?yearMonth=${month}`)
        .then(res => res.json())
        .then(data => {
            let html = `
                <div class="row g-3 mb-4">
                    <div class="col-md-6">
                        <div class="p-3 border rounded-3 bg-light">
                            <span class="small text-secondary fw-semibold text-uppercase">Cumulative Student Meals Served</span>
                            <h3 class="fw-bold text-dark mt-1 mb-0">${data.totalAttendance} Meals</h3>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="p-3 border rounded-3 bg-light">
                            <span class="small text-secondary fw-semibold text-uppercase">Daily Average Attendance</span>
                            <h3 class="fw-bold text-dark mt-1 mb-0">${data.avgAttendance} Students/Day</h3>
                        </div>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-md-6">
                        <h6 class="fw-bold text-dark mb-2">Total Inward Consignments</h6>
                        <table class="table table-sm table-custom table-bordered">
                            <thead>
                                <tr>
                                    <th>Ingredient</th>
                                    <th class="text-end">Total Refilled</th>
                                </tr>
                            </thead>
                            <tbody>
            `;
            
            let refillRows = '';
            for (const [key, val] of Object.entries(data.refills)) {
                refillRows += `<tr><td class="fw-semibold text-dark">${key}</td><td class="text-end fw-bold">${val}</td></tr>`;
            }
            html += refillRows || '<tr><td colspan="2" class="text-center text-muted py-3">No inward consignments.</td></tr>';
            
            html += `
                            </tbody>
                        </table>
                    </div>
                    <div class="col-md-6">
                        <h6 class="fw-bold text-dark mb-2">Total Kitchen Consumption</h6>
                        <table class="table table-sm table-custom table-bordered">
                            <thead>
                                <tr>
                                    <th>Ingredient</th>
                                    <th class="text-end">Total Consumed</th>
                                </tr>
                            </thead>
                            <tbody>
            `;
            
            let consRows = '';
            for (const [key, val] of Object.entries(data.consumption)) {
                consRows += `<tr><td class="fw-semibold text-dark">${key}</td><td class="text-end fw-bold">${val}</td></tr>`;
            }
            html += consRows || '<tr><td colspan="2" class="text-center text-muted py-3">No consumption logged.</td></tr>';
            
            html += `
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
            container.innerHTML = html;
        });
    }
}

// Enterprise Toast Helper
function showToast(title, message, type = 'primary') {
    const container = document.getElementById('toast-container');
    const toastId = 'toast-' + Date.now();
    
    let borderClass = 'border-success';
    if (type === 'danger') {
        borderClass = 'border-danger';
    } else if (type === 'info') {
        borderClass = 'border-primary';
    }

    const toastHTML = `
        <div id="${toastId}" class="toast toast-enterprise show bg-white ${borderClass} mb-2" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex align-items-center p-3">
                <div class="flex-grow-1">
                    <strong class="d-block text-dark fw-bold mb-1" style="font-size: 0.9rem;">${title}</strong>
                    <span class="text-secondary" style="font-size: 0.825rem;">${message}</span>
                </div>
                <button type="button" class="btn-close ms-2" data-bs-dismiss="toast" aria-label="Close" onclick="document.getElementById('${toastId}').remove()"></button>
            </div>
        </div>
    `;
    container.innerHTML += toastHTML;
    
    setTimeout(() => {
        const el = document.getElementById(toastId);
        if (el) el.remove();
    }, 4500);
}

// Confirm Delete Dialog Helper
function confirmDelete(url, callback) {
    currentDeleteTarget = { url, callback };
    new bootstrap.Modal(document.getElementById('deleteConfirmModal')).show();
}

function escapeString(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
