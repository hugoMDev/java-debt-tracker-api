/**
 * ==============================================================================
 * DebtTracker - Modern Frontend Client for Spring Boot API
 * Communication via Fetch API (GET, POST, DELETE)
 * ==============================================================================
 */

// ==============================================================================
// API Endpoints Configuration (Dinâmico para Localhost e GitHub Pages)
// ==============================================================================
const DEFAULT_LOCAL_API = 'http://localhost:8080';
const STORAGE_KEY_API = 'debt_tracker_api_url';

/**
 * Retorna a URL base configurada (do localStorage ou fallback apropriado).
 */
function getApiBaseUrl() {
    const saved = localStorage.getItem(STORAGE_KEY_API);
    if (saved) return saved.trim().replace(/\/+$/, '');

    const isHttps = window.location.protocol === 'https:';
    const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

    // No GitHub Pages sem configuração manual prévia
    if (isHttps && !isLocalhost) {
        return '';
    }
    return DEFAULT_LOCAL_API;
}

function getDebtsEndpoint() {
    const base = getApiBaseUrl();
    return base ? `${base}/api/debts` : '';
}

function getTotalEndpoint() {
    const base = getApiBaseUrl();
    return base ? `${base}/api/debts/total` : '';
}

// Application State
const state = {
    debts: [],
    currentFilter: 'ALL', // 'ALL' | 'FIXA' | 'PARCELADA'
    searchTerm: '',
    deleteCandidate: null, // { id: number, description: string }
    isSubmitting: false,
    isLoading: false
};

// DOM Elements Cache
const elements = {
    // Top Server Status
    serverStatus: document.getElementById('serverStatus'),
    statusLabel: document.getElementById('statusLabel'),

    // KPI Summary Cards
    totalAccumulated: document.getElementById('totalAccumulated'),
    totalDebtsCount: document.getElementById('totalDebtsCount'),
    fixedDebtsCount: document.getElementById('fixedDebtsCount'),
    installmentDebtsCount: document.getElementById('installmentDebtsCount'),

    // Form
    debtForm: document.getElementById('debtForm'),
    descriptionInput: document.getElementById('description'),
    amountInput: document.getElementById('amount'),
    totalInstallmentsInput: document.getElementById('totalInstallments'),
    currentInstallmentInput: document.getElementById('currentInstallment'),
    installmentFields: document.getElementById('installmentFields'),
    labelTypeFixed: document.getElementById('labelTypeFixed'),
    labelTypeInstallment: document.getElementById('labelTypeInstallment'),
    btnSubmit: document.getElementById('btnSubmit'),
    btnText: document.querySelector('#btnSubmit .btn-text'),
    btnSpinner: document.querySelector('#btnSubmit .btn-spinner'),

    // Error Fields
    descriptionError: document.getElementById('descriptionError'),
    amountError: document.getElementById('amountError'),
    totalInstallmentsError: document.getElementById('totalInstallmentsError'),
    currentInstallmentError: document.getElementById('currentInstallmentError'),

    // Table & States
    debtsTable: document.getElementById('debtsTable'),
    debtsTableBody: document.getElementById('debtsTableBody'),
    loadingState: document.getElementById('loadingState'),
    emptyState: document.getElementById('emptyState'),
    emptyDescription: document.getElementById('emptyDescription'),
    itemsCountLabel: document.getElementById('itemsCountLabel'),

    // Search and Filters
    searchInput: document.getElementById('searchInput'),
    clearSearchBtn: document.getElementById('clearSearchBtn'),
    filterPills: document.querySelectorAll('.filter-pill'),
    btnRefresh: document.getElementById('btnRefresh'),

    // Delete Modal
    deleteModal: document.getElementById('deleteModal'),
    deleteDebtName: document.getElementById('deleteDebtName'),
    btnCancelDelete: document.getElementById('btnCancelDelete'),
    btnConfirmDelete: document.getElementById('btnConfirmDelete'),

    // API Config Modal (GitHub Pages / Remote)
    apiModal: document.getElementById('apiModal'),
    btnConfigApi: document.getElementById('btnConfigApi'),
    apiUrlInput: document.getElementById('apiUrlInput'),
    btnSaveApiUrl: document.getElementById('btnSaveApiUrl'),
    btnResetApiUrl: document.getElementById('btnResetApiUrl'),
    mixedContentWarning: document.getElementById('mixedContentWarning'),

    // Toast Container
    toastContainer: document.getElementById('toastContainer')
};

// ==============================================================================
// Utility Functions
// ==============================================================================

/**
 * Format a numeric value into Brazilian Real (BRL) currency format.
 * @param {number|string} value
 * @returns {string} e.g. "R$ 1.250,50"
 */
function formatCurrency(value) {
    const num = Number(value) || 0;
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL',
        minimumFractionDigits: 2
    }).format(num);
}

/**
 * Displays a non-intrusive floating toast notification.
 * @param {string} message
 * @param {'success'|'error'|'info'} type
 * @param {number} duration Duration in ms
 */
function showToast(message, type = 'info', duration = 3500) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const iconMap = {
        success: 'fa-solid fa-circle-check',
        error: 'fa-solid fa-circle-exclamation',
        info: 'fa-solid fa-circle-info'
    };

    toast.innerHTML = `
        <i class="${iconMap[type] || iconMap.info} toast-icon"></i>
        <div class="toast-message">${escapeHtml(message)}</div>
    `;

    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('toast-closing');
        toast.addEventListener('transitionend', () => toast.remove(), { once: true });
    }, duration);
}

/**
 * Escape HTML special characters to prevent XSS injection.
 * @param {string} str
 * @returns {string}
 */
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/**
 * Update the visual status badge indicating backend connectivity.
 * @param {boolean} isOnline
 * @param {string} label
 */
function updateServerStatus(isOnline, label) {
    elements.serverStatus.classList.remove('online', 'offline');
    elements.serverStatus.classList.add(isOnline ? 'online' : 'offline');
    elements.statusLabel.textContent = label;
}

// ==============================================================================
// API Communications (Fetch API)
// ==============================================================================

/**
 * Fetch all debts from Spring Boot API (GET /api/debts).
 */
async function fetchDebts() {
    const endpoint = getDebtsEndpoint();

    // Se estiver no GitHub Pages (HTTPS) sem backend configurado
    if (!endpoint) {
        updateServerStatus(false, 'Configurar API');
        elements.loadingState.classList.add('hidden');
        elements.emptyState.classList.remove('hidden');
        elements.emptyDescription.innerHTML = 
            'Você está no <strong>GitHub Pages</strong>. O navegador bloqueia requisições diretas para <code>http://localhost:8080</code> por segurança (Mixed Content).<br><br>' +
            'Clique no botão <strong><i class="fa-solid fa-gear"></i></strong> no topo para configurar a URL HTTPS da sua API (ex: Render, Railway ou túnel ngrok).';
        elements.itemsCountLabel.textContent = 'Aguardando configuração de API';
        showToast('Configure a URL HTTPS do seu backend para conectar no GitHub Pages.', 'info', 6000);
        return;
    }

    state.isLoading = true;
    renderLoading(true);

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Falha HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        state.debts = Array.isArray(data) ? data : [];

        updateServerStatus(true, 'API Online');
        
        // Also fetch official summary from /api/debts/total
        await fetchSummary();
        
        renderTable();
    } catch (error) {
        console.error('Erro ao buscar dívidas:', error);
        updateServerStatus(false, 'API Indisponível');

        const isHttpsPage = window.location.protocol === 'https:';
        if (isHttpsPage && endpoint.startsWith('http://')) {
            showToast('Bloqueado por Mixed Content: Páginas HTTPS não podem acessar APIs HTTP (localhost). Use HTTPS na URL da API.', 'error', 7000);
        } else {
            showToast('Não foi possível conectar à API Spring Boot. Verifique se o servidor está rodando e com CORS liberado.', 'error', 5000);
        }
        renderTable();
    } finally {
        state.isLoading = false;
        renderLoading(false);
    }
}

/**
 * Fetch total monetary amount and count statistics (GET /api/debts/total).
 * Falls back to local memory calculation if endpoint is unavailable.
 */
async function fetchSummary() {
    const totalUrl = getTotalEndpoint();
    if (totalUrl) {
        try {
            const response = await fetch(totalUrl, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });

            if (response.ok) {
                const summary = await response.json();
                updateSummaryCards(
                    summary.totalAmount,
                    summary.totalDebtsCount,
                    summary.fixedDebtsCount,
                    summary.installmentDebtsCount
                );
                return;
            }
        } catch {
            // Fallback: calculate locally from loaded state.debts
        }
    }

    // Local calculation fallback
    const totalAmount = state.debts.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    const fixedCount = state.debts.filter(d => d.type === 'FIXA').length;
    const installmentCount = state.debts.filter(d => d.type === 'PARCELADA').length;

    updateSummaryCards(totalAmount, state.debts.length, fixedCount, installmentCount);
}

/**
 * Update the DOM KPI cards with current numbers.
 */
function updateSummaryCards(totalAmount, totalDebts, fixedDebts, installmentDebts) {
    elements.totalAccumulated.textContent = formatCurrency(totalAmount);
    elements.totalDebtsCount.textContent = totalDebts ?? 0;
    elements.fixedDebtsCount.textContent = fixedDebts ?? 0;
    elements.installmentDebtsCount.textContent = installmentDebts ?? 0;
}

/**
 * Submit new debt payload to Spring Boot API (POST /api/debts).
 * @param {object} payload
 */
async function createDebt(payload) {
    const endpoint = getDebtsEndpoint();
    if (!endpoint) {
        showToast('Configure a URL da API antes de cadastrar dívidas.', 'error');
        openApiModal();
        return;
    }

    setSubmitting(true);

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.status === 201) {
            const created = await response.json();
            showToast(`Dívida "${created.description}" cadastrada com sucesso!`, 'success');
            resetForm();
            await fetchDebts();
            return;
        }

        // Handle validation or business logic errors (400, 422)
        const errorData = await response.json().catch(() => null);
        let errorMsg = 'Erro ao cadastrar dívida.';

        if (errorData) {
            if (errorData.message) {
                errorMsg = errorData.message;
            } else if (Array.isArray(errorData.errors)) {
                errorMsg = errorData.errors.join(', ');
            }
        }

        showToast(errorMsg, 'error', 4500);
    } catch (error) {
        console.error('Erro na requisição POST:', error);
        showToast('Falha na comunicação com o servidor ao cadastrar dívida.', 'error');
    } finally {
        setSubmitting(false);
    }
}

/**
 * Remove a registered debt by ID (DELETE /api/debts/{id}).
 * @param {number} id
 */
async function deleteDebt(id) {
    const endpoint = getDebtsEndpoint();
    if (!endpoint) return;

    try {
        const response = await fetch(`${endpoint}/${id}`, {
            method: 'DELETE'
        });

        if (response.status === 204 || response.ok) {
            showToast('Dívida excluída com sucesso!', 'success');
            closeDeleteModal();
            await fetchDebts();
        } else {
            const errorData = await response.json().catch(() => null);
            const msg = errorData?.message || 'Falha ao excluir a dívida selecionada.';
            showToast(msg, 'error');
        }
    } catch (error) {
        console.error('Erro ao excluir dívida:', error);
        showToast('Erro de conexão ao tentar excluir a dívida.', 'error');
    }
}

// ==============================================================================
// UI Rendering & State Updates
// ==============================================================================

/**
 * Render debts table based on active filter and search text.
 */
function renderTable() {
    const filtered = state.debts.filter(debt => {
        // Type filter match
        const matchesType = state.currentFilter === 'ALL' || debt.type === state.currentFilter;

        // Search text match (description or ID)
        const term = state.searchTerm.toLowerCase().trim();
        const matchesSearch = !term ||
            debt.description.toLowerCase().includes(term) ||
            String(debt.id).includes(term);

        return matchesType && matchesSearch;
    });

    elements.debtsTableBody.innerHTML = '';

    if (filtered.length === 0) {
        elements.debtsTable.classList.add('hidden');
        elements.emptyState.classList.remove('hidden');

        if (state.searchTerm || state.currentFilter !== 'ALL') {
            elements.emptyDescription.textContent = 'Nenhuma dívida corresponde aos filtros ou busca selecionados.';
        } else {
            elements.emptyDescription.textContent = 'Nenhuma dívida cadastrada no momento. Preencha o formulário para adicionar a primeira!';
        }

        elements.itemsCountLabel.textContent = 'Mostrando 0 registros';
        return;
    }

    elements.debtsTable.classList.remove('hidden');
    elements.emptyState.classList.add('hidden');
    elements.itemsCountLabel.textContent = `Mostrando ${filtered.length} de ${state.debts.length} registro(s)`;

    filtered.forEach(debt => {
        const tr = document.createElement('tr');

        // Type Badge
        const isFixa = debt.type === 'FIXA';
        const typeBadge = isFixa
            ? `<span class="badge-type badge-fixa"><i class="fa-solid fa-repeat"></i> Fixa</span>`
            : `<span class="badge-type badge-parcelada"><i class="fa-solid fa-layer-group"></i> Parcelada</span>`;

        // Installment Details
        let installmentInfo = '<span class="text-muted">—</span>';
        let remainingInfo = '<span class="text-muted">—</span>';

        if (!isFixa && debt.totalInstallments) {
            const current = debt.currentInstallment || 1;
            const total = debt.totalInstallments;
            const remaining = debt.remainingInstallments !== null && debt.remainingInstallments !== undefined
                ? debt.remainingInstallments
                : Math.max(0, total - current);

            installmentInfo = `<span class="installment-progress-text">${current} / ${total}</span>`;
            
            const isLast = remaining <= 1;
            remainingInfo = `<span class="remaining-tag ${isLast ? 'urgent' : ''}">
                ${remaining === 0 ? 'Quitada' : `${remaining} restante(s)`}
            </span>`;
        }

        tr.innerHTML = `
            <td class="td-id">#${debt.id}</td>
            <td class="td-description">${escapeHtml(debt.description)}</td>
            <td>${typeBadge}</td>
            <td class="td-amount">${formatCurrency(debt.amount)}</td>
            <td>${installmentInfo}</td>
            <td>${remainingInfo}</td>
            <td class="text-right">
                <button 
                    type="button" 
                    class="btn-icon-danger btn-delete-action" 
                    data-id="${debt.id}" 
                    data-name="${escapeHtml(debt.description)}"
                    title="Excluir dívida"
                >
                    <i class="fa-regular fa-trash-can"></i>
                </button>
            </td>
        `;

        elements.debtsTableBody.appendChild(tr);
    });

    // Attach click events to dynamic delete buttons
    document.querySelectorAll('.btn-delete-action').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const id = Number(btn.getAttribute('data-id'));
            const name = btn.getAttribute('data-name');
            openDeleteModal(id, name);
        });
    });
}

/**
 * Toggle table loading spinner.
 * @param {boolean} show
 */
function renderLoading(show) {
    if (show) {
        elements.loadingState.classList.remove('hidden');
        elements.debtsTable.classList.add('hidden');
        elements.emptyState.classList.add('hidden');
    } else {
        elements.loadingState.classList.add('hidden');
    }
}

/**
 * Handle submit button spinner and disabled state.
 * @param {boolean} submitting
 */
function setSubmitting(submitting) {
    state.isSubmitting = submitting;
    elements.btnSubmit.disabled = submitting;

    if (submitting) {
        elements.btnText.classList.add('hidden');
        elements.btnSpinner.classList.remove('hidden');
    } else {
        elements.btnText.classList.remove('hidden');
        elements.btnSpinner.classList.add('hidden');
    }
}

// ==============================================================================
// Form Validation and Handling
// ==============================================================================

/**
 * Clear form validation errors.
 */
function clearValidationErrors() {
    elements.descriptionError.textContent = '';
    elements.amountError.textContent = '';
    elements.totalInstallmentsError.textContent = '';
    elements.currentInstallmentError.textContent = '';

    elements.descriptionInput.classList.remove('input-invalid');
    elements.amountInput.classList.remove('input-invalid');
    elements.totalInstallmentsInput.classList.remove('input-invalid');
    elements.currentInstallmentInput.classList.remove('input-invalid');
}

/**
 * Reset form inputs to default values.
 */
function resetForm() {
    elements.debtForm.reset();
    clearValidationErrors();
    selectDebtType('FIXA');
}

/**
 * Switch selected debt type between 'FIXA' and 'PARCELADA'.
 * @param {'FIXA'|'PARCELADA'} type
 */
function selectDebtType(type) {
    const isParcelada = type === 'PARCELADA';

    // Update Radio Checked
    const radioToSelect = document.querySelector(`input[name="debtType"][value="${type}"]`);
    if (radioToSelect) radioToSelect.checked = true;

    // Update active label styles
    elements.labelTypeFixed.classList.toggle('active', !isParcelada);
    elements.labelTypeInstallment.classList.toggle('active', isParcelada);

    // Toggle Installment Fields Container
    if (isParcelada) {
        elements.installmentFields.classList.remove('hidden');
        elements.totalInstallmentsInput.required = true;
        elements.currentInstallmentInput.required = true;
        if (!elements.currentInstallmentInput.value) {
            elements.currentInstallmentInput.value = '1';
        }
    } else {
        elements.installmentFields.classList.add('hidden');
        elements.totalInstallmentsInput.required = false;
        elements.currentInstallmentInput.required = false;
        elements.totalInstallmentsInput.value = '';
        elements.currentInstallmentInput.value = '1';
        elements.totalInstallmentsError.textContent = '';
        elements.currentInstallmentError.textContent = '';
        elements.totalInstallmentsInput.classList.remove('input-invalid');
        elements.currentInstallmentInput.classList.remove('input-invalid');
    }
}

/**
 * Client-side validation prior to sending to Spring Boot.
 * @returns {object|null} Valid payload or null if invalid
 */
function validateAndBuildPayload() {
    clearValidationErrors();
    let isValid = true;

    // 1. Description
    const description = elements.descriptionInput.value.trim();
    if (!description) {
        elements.descriptionError.textContent = 'Informe uma descrição para a dívida.';
        elements.descriptionInput.classList.add('input-invalid');
        isValid = false;
    } else if (description.length > 150) {
        elements.descriptionError.textContent = 'A descrição deve ter no máximo 150 caracteres.';
        elements.descriptionInput.classList.add('input-invalid');
        isValid = false;
    }

    // 2. Amount
    const amountVal = parseFloat(elements.amountInput.value);
    if (isNaN(amountVal) || amountVal <= 0) {
        elements.amountError.textContent = 'O valor deve ser maior que zero (R$ 0,01).';
        elements.amountInput.classList.add('input-invalid');
        isValid = false;
    }

    // 3. Debt Type
    const selectedTypeRadio = document.querySelector('input[name="debtType"]:checked');
    const type = selectedTypeRadio ? selectedTypeRadio.value : 'FIXA';

    let totalInstallments = null;
    let currentInstallment = null;

    // 4. Installments validation if PARCELADA
    if (type === 'PARCELADA') {
        const total = parseInt(elements.totalInstallmentsInput.value, 10);
        const current = parseInt(elements.currentInstallmentInput.value, 10);

        if (isNaN(total) || total < 1) {
            elements.totalInstallmentsError.textContent = 'Total de parcelas deve ser no mínimo 1.';
            elements.totalInstallmentsInput.classList.add('input-invalid');
            isValid = false;
        }

        if (isNaN(current) || current < 1) {
            elements.currentInstallmentError.textContent = 'Parcela atual deve ser no mínimo 1.';
            elements.currentInstallmentInput.classList.add('input-invalid');
            isValid = false;
        }

        if (!isNaN(total) && !isNaN(current) && current > total) {
            elements.currentInstallmentError.textContent = `A parcela atual (${current}) não pode ser maior que o total (${total}).`;
            elements.currentInstallmentInput.classList.add('input-invalid');
            isValid = false;
        }

        totalInstallments = total;
        currentInstallment = current;
    }

    if (!isValid) return null;

    const payload = {
        description,
        amount: amountVal,
        type
    };

    if (type === 'PARCELADA') {
        payload.totalInstallments = totalInstallments;
        payload.currentInstallment = currentInstallment;
    }

    return payload;
}

// ==============================================================================
// Modal Actions
// ==============================================================================

function openDeleteModal(id, description) {
    state.deleteCandidate = { id, description };
    elements.deleteDebtName.textContent = `"${description}" (#${id})`;
    elements.deleteModal.classList.remove('hidden');
}

function closeDeleteModal() {
    state.deleteCandidate = null;
    elements.deleteModal.classList.add('hidden');
}

function openApiModal() {
    const currentUrl = localStorage.getItem(STORAGE_KEY_API) || (window.location.protocol === 'https:' ? '' : DEFAULT_LOCAL_API);
    elements.apiUrlInput.value = currentUrl;
    checkMixedContentWarning();
    elements.apiModal.classList.remove('hidden');
    elements.apiUrlInput.focus();
}

function closeApiModal() {
    elements.apiModal.classList.add('hidden');
}

function checkMixedContentWarning() {
    const val = elements.apiUrlInput.value.trim();
    if (window.location.protocol === 'https:' && val.startsWith('http://')) {
        elements.mixedContentWarning.style.display = 'block';
    } else {
        elements.mixedContentWarning.style.display = 'none';
    }
}

// ==============================================================================
// Event Listeners Initialization
// ==============================================================================

function setupEventListeners() {
    // Form Submit
    elements.debtForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (state.isSubmitting) return;

        const payload = validateAndBuildPayload();
        if (payload) {
            await createDebt(payload);
        }
    });

    // Debt Type Radios
    elements.labelTypeFixed.addEventListener('click', () => selectDebtType('FIXA'));
    elements.labelTypeInstallment.addEventListener('click', () => selectDebtType('PARCELADA'));

    // Search Input
    elements.searchInput.addEventListener('input', (e) => {
        state.searchTerm = e.target.value;
        elements.clearSearchBtn.classList.toggle('hidden', !state.searchTerm);
        renderTable();
    });

    elements.clearSearchBtn.addEventListener('click', () => {
        elements.searchInput.value = '';
        state.searchTerm = '';
        elements.clearSearchBtn.classList.add('hidden');
        renderTable();
    });

    // Filter Pills (Todas, Fixas, Parceladas)
    elements.filterPills.forEach(pill => {
        pill.addEventListener('click', () => {
            elements.filterPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            state.currentFilter = pill.getAttribute('data-filter');
            renderTable();
        });
    });

    // Refresh Button
    elements.btnRefresh.addEventListener('click', () => {
        showToast('Atualizando dados da API...', 'info', 1500);
        fetchDebts();
    });

    // API Config Modal Events
    elements.btnConfigApi.addEventListener('click', openApiModal);
    elements.apiModal.addEventListener('click', (e) => {
        if (e.target === elements.apiModal) closeApiModal();
    });
    elements.apiUrlInput.addEventListener('input', checkMixedContentWarning);

    elements.btnSaveApiUrl.addEventListener('click', () => {
        const url = elements.apiUrlInput.value.trim().replace(/\/+$/, '');
        if (url) {
            if (window.location.protocol === 'https:' && url.startsWith('http://')) {
                showToast('Aviso: URLs HTTP são bloqueadas em páginas HTTPS (Mixed Content). Recomendamos usar HTTPS.', 'error', 6000);
            }
            localStorage.setItem(STORAGE_KEY_API, url);
            showToast(`URL da API salva: ${url}`, 'success');
        } else {
            localStorage.removeItem(STORAGE_KEY_API);
            showToast('URL redefinida para o padrão.', 'info');
        }
        closeApiModal();
        fetchDebts();
    });

    elements.btnResetApiUrl.addEventListener('click', () => {
        localStorage.removeItem(STORAGE_KEY_API);
        elements.apiUrlInput.value = DEFAULT_LOCAL_API;
        checkMixedContentWarning();
        showToast('Restaurado para http://localhost:8080', 'info');
        closeApiModal();
        fetchDebts();
    });

    // Delete Modal Events
    elements.btnCancelDelete.addEventListener('click', closeDeleteModal);
    elements.deleteModal.addEventListener('click', (e) => {
        if (e.target === elements.deleteModal) closeDeleteModal();
    });

    elements.btnConfirmDelete.addEventListener('click', async () => {
        if (state.deleteCandidate) {
            await deleteDebt(state.deleteCandidate.id);
        }
    });

    // Escape key to close modals
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (!elements.deleteModal.classList.contains('hidden')) closeDeleteModal();
            if (!elements.apiModal.classList.contains('hidden')) closeApiModal();
        }
    });

    // Input blur real-time validation clearing
    elements.descriptionInput.addEventListener('input', () => {
        if (elements.descriptionInput.value.trim()) {
            elements.descriptionError.textContent = '';
            elements.descriptionInput.classList.remove('input-invalid');
        }
    });

    elements.amountInput.addEventListener('input', () => {
        if (parseFloat(elements.amountInput.value) > 0) {
            elements.amountError.textContent = '';
            elements.amountInput.classList.remove('input-invalid');
        }
    });
}

// ==============================================================================
// App Initialization
// ==============================================================================

document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    fetchDebts();
});
