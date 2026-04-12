const form = document.getElementById('uploadForm');
const fileInput = document.getElementById('fileInput');
const dropArea = document.getElementById('dropArea');
const fileMsg = document.querySelector('.file-msg');
const submitBtn = document.getElementById('submitBtn');
const statusMessage = document.getElementById('statusMessage');
const resultArea = document.getElementById('resultArea');
const emptyState = document.getElementById('emptyState');
const fileGrid = document.getElementById('fileGrid');
const fileCountLabel = document.getElementById('fileCount');
const downloadAllBtn = document.getElementById('downloadAllBtn');
const toast = document.getElementById('toast');
const currentDateSpan = document.getElementById('currentDate');

// Set current date in header
const options = { year: 'numeric', month: 'long', day: 'numeric' };
currentDateSpan.textContent = new Date().toLocaleDateString(undefined, options);

// Modal Elements
const previewModal = document.getElementById('previewModal');
const closeModalBtn = document.getElementById('closeModalBtn');
const pdfFrame = document.getElementById('pdfFrame');
const modalTitle = document.getElementById('modalTitle');

// --- Drag & Drop ---
['dragenter', 'dragover', 'dragleave', 'drop'].forEach(ev => {
    dropArea.addEventListener(ev, (e) => { e.preventDefault(); e.stopPropagation(); });
});

['dragenter', 'dragover'].forEach(ev => {
    dropArea.addEventListener(ev, () => dropArea.classList.add('dragover'));
});

['dragleave', 'drop'].forEach(ev => {
    dropArea.addEventListener(ev, () => dropArea.classList.remove('dragover'));
});

fileInput.addEventListener('change', () => updateFileSelection());
dropArea.addEventListener('drop', (e) => {
    fileInput.files = e.dataTransfer.files;
    updateFileSelection();
});

function updateFileSelection() {
    if(fileInput.files.length > 0) {
        fileMsg.innerHTML = `<strong>Selected:</strong> ${fileInput.files[0].name}`;
        statusMessage.classList.add('hidden');
    }
}

// --- Upload Handler ---
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if(fileInput.files.length === 0) return;

    // UI Loading State (Enhanced with Spinner)
    submitBtn.disabled = true;
    const originalBtnContent = submitBtn.innerHTML;
    submitBtn.innerHTML = '<div class="spinner"></div> Processing Batch...';
    statusMessage.classList.add('hidden');

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch('/api/upload', { method: 'POST', body: formData });
        const result = await response.json();

        if (response.ok) {
            const files = result.data || [];
            if(files.length > 0) {
                renderFileCards(files);
                emptyState.classList.add('hidden');
                resultArea.classList.remove('hidden');
                showStatus(`Success! ${files.length} documents generated and ready for review.`, 'success');
            } else {
                showStatus('The excel file was processed but no records were found.', 'error');
            }
            
            // Reset input
            fileInput.value = '';
            fileMsg.textContent = 'Drag & Drop Excel file (.xlsx)';
        } else {
            showStatus(result.message || 'Error processing file. Please check the Excel format.', 'error');
        }
    } catch (err) {
        showStatus('Network error. Check server connectivity.', 'error');
        console.error(err);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Process Batch';
    }
});

function renderFileCards(files) {
    fileGrid.innerHTML = '';
    fileCountLabel.textContent = `${files.length} Files`;

    files.forEach(filename => {
        // Parse filename: Employee_Name_ID.pdf
        const cleanName = filename.replace('.pdf', '');
        const parts = cleanName.split('_');
        const id = parts.pop();
        const displayName = parts.join(' ');

        const card = document.createElement('div');
        card.className = 'pdf-card';
        card.innerHTML = `
            <div class="pdf-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            </div>
            <div class="card-info">
                <span class="card-name">${displayName}</span>
                <span class="card-id">Employee ID: ${id}</span>
            </div>
            <div class="card-actions">
                <button class="card-btn btn-preview">Preview</button>
                <button class="card-btn btn-download">Download</button>
            </div>
        `;

        card.querySelector('.btn-preview').onclick = () => openPreview(filename);
        card.querySelector('.btn-download').onclick = () => downloadFile(filename);
        fileGrid.appendChild(card);
    });
}

// --- Actions ---
function downloadFile(filename) {
    showToast(`Downloading: ${filename}`);
    window.location.href = `/api/download/${encodeURIComponent(filename)}`;
}

downloadAllBtn.onclick = () => {
    const originalText = downloadAllBtn.innerHTML;
    downloadAllBtn.disabled = true;
    downloadAllBtn.innerHTML = '<div class="spinner" style="border-top-color: var(--primary); width:16px; height:16px;"></div> Preparing ZIP...';
    
    showToast('Compressing documents into a single archive...');
    window.location.href = '/api/download-all';
    
    setTimeout(() => { 
        downloadAllBtn.disabled = false; 
        downloadAllBtn.innerHTML = originalText;
    }, 3000);
};

function openPreview(filename) {
    modalTitle.textContent = filename;
    pdfFrame.src = `/api/pdf/${encodeURIComponent(filename)}`;
    previewModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closePreview() {
    previewModal.classList.add('hidden');
    pdfFrame.src = '';
    document.body.style.overflow = 'auto';
}

closeModalBtn.onclick = closePreview;
previewModal.onclick = (e) => { if(e.target.classList.contains('modal-backdrop')) closePreview(); };

// --- Utils ---
function showStatus(msg, type) {
    statusMessage.textContent = msg;
    statusMessage.className = `status-box ${type}`;
    statusMessage.classList.remove('hidden');
    // Scroll to status if it's an error
    if(type === 'error') {
        statusMessage.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}

function showToast(msg) {
    toast.textContent = msg;
    toast.classList.remove('hidden');
    // Auto-hide toast after 4 seconds
    setTimeout(() => toast.classList.add('hidden'), 4000);
}
