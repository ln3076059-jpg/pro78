/**
 * Quản lý tương tác kê đơn thuốc và tích hợp Data Mining gợi ý kết hợp thuốc (Co-prescription)
 */
document.addEventListener('DOMContentLoaded', function () {
    const medicineSelect = document.getElementById('medicineSelect');
    const addDrugBtn = document.getElementById('addDrugBtn');
    const prescriptionTableBody = document.getElementById('prescriptionTableBody');
    const emptyRow = document.getElementById('emptyRow');
    const recommendationContainer = document.getElementById('recommendationContainer');
    const recommendationSpinner = document.getElementById('recommendationSpinner');
    const recommendationList = document.getElementById('recommendationList');
    const noRecommendationText = document.getElementById('noRecommendationText');

    let currentSelectedMeds = new Map(); // id -> {name, dosage, frequency, duration, instructions}

    if (addDrugBtn) {
        addDrugBtn.addEventListener('click', function () {
            const medId = medicineSelect.value;
            if (!medId) {
                alert('Vui lòng chọn một loại thuốc từ danh mục!');
                return;
            }

            const selectedOption = medicineSelect.options[medicineSelect.selectedIndex];
            const genericName = selectedOption.getAttribute('data-generic') || selectedOption.text;
            const dosage = document.getElementById('inputDosage').value.trim() || 'Theo chỉ định';
            const frequency = document.getElementById('inputFrequency').value.trim() || '2 lần/ngày (sáng - chiều)';
            const duration = document.getElementById('inputDuration').value.trim() || 7;
            const instructions = document.getElementById('inputInstructions').value.trim() || 'Uống sau bữa ăn';

            addMedicineToPrescription(medId, genericName, dosage, frequency, duration, instructions);
        });
    }

    function addMedicineToPrescription(medId, name, dosage, frequency, duration, instructions) {
        if (currentSelectedMeds.has(medId.toString())) {
            alert('Thuốc này đã có trong đơn kê!');
            return;
        }

        currentSelectedMeds.set(medId.toString(), {
            id: medId,
            name: name,
            dosage: dosage,
            frequency: frequency,
            duration: duration,
            instructions: instructions
        });

        renderPrescriptionTable();
        fetchRecommendations();
    }

    function removeMedicine(medId) {
        currentSelectedMeds.delete(medId.toString());
        renderPrescriptionTable();
        fetchRecommendations();
    }

    function renderPrescriptionTable() {
        prescriptionTableBody.innerHTML = '';

        if (currentSelectedMeds.size === 0) {
            prescriptionTableBody.appendChild(emptyRow);
            return;
        }

        let index = 1;
        currentSelectedMeds.forEach((item, id) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="text-center font-monospace">${index++}</td>
                <td>
                    <strong>${escapeHtml(item.name)}</strong>
                    <input type="hidden" name="medicineIds" value="${id}">
                </td>
                <td>
                    <input type="text" name="dosages" class="form-control form-control-sm" value="${escapeHtml(item.dosage)}" required>
                </td>
                <td>
                    <input type="text" name="frequencies" class="form-control form-control-sm" value="${escapeHtml(item.frequency)}" required>
                </td>
                <td>
                    <input type="number" name="durations" class="form-control form-control-sm text-center" value="${item.duration}" min="1" max="90" required>
                </td>
                <td>
                    <input type="text" name="instructions" class="form-control form-control-sm" value="${escapeHtml(item.instructions)}">
                </td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger remove-btn" data-id="${id}">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
            `;
            prescriptionTableBody.appendChild(tr);
        });

        // Gắn sự kiện xóa
        document.querySelectorAll('.remove-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                const idToRemove = this.getAttribute('data-id');
                removeMedicine(idToRemove);
            });
        });
    }

    /**
     * Gọi API Data Mining để tìm các luật kết hợp phù hợp với đơn thuốc hiện tại
     */
    function fetchRecommendations() {
        if (currentSelectedMeds.size === 0) {
            recommendationList.innerHTML = '';
            noRecommendationText.classList.remove('d-none');
            return;
        }

        recommendationSpinner.classList.remove('d-none');
        noRecommendationText.classList.add('d-none');

        const medicineIds = Array.from(currentSelectedMeds.keys()).join(',');

        fetch(`/api/recommendations/drugs?drugIds=${medicineIds}&topN=4`)
            .then(res => res.json())
            .then(data => {
                recommendationSpinner.classList.add('d-none');
                renderRecommendations(data);
            })
            .catch(err => {
                recommendationSpinner.classList.add('d-none');
                console.error('Lỗi khi tải gợi ý luật kết hợp:', err);
            });
    }

    function renderRecommendations(recommendations) {
        recommendationList.innerHTML = '';

        if (!recommendations || recommendations.length === 0) {
            noRecommendationText.classList.remove('d-none');
            return;
        }

        noRecommendationText.classList.add('d-none');

        recommendations.forEach(rec => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'recommendation-item shadow-sm';

            let ddiBadge = '';
            if (rec.interactionStatus === 'KNOWN') {
                let severityClass = 'bg-warning text-dark';
                if (rec.interactionSeverity === 'Major') severityClass = 'bg-danger text-white';
                else if (rec.interactionSeverity === 'Minor') severityClass = 'bg-info text-dark';
                ddiBadge = `<span class="badge ${severityClass} ms-1" title="${escapeHtml(rec.interactionDescription || '')}"><i class="fas fa-exclamation-circle me-1"></i> FDA DDI: ${escapeHtml(rec.interactionSeverity || 'Known')}</span>`;
            } else if (rec.interactionStatus === 'NOT FOUND') {
                ddiBadge = `<span class="badge bg-success-subtle text-success border border-success-subtle ms-1" title="Không phát hiện tương tác nghiêm trọng trong FDA DDI"><i class="fas fa-check-circle me-1"></i> DDI: Không cảnh báo</span>`;
            }

            itemDiv.innerHTML = `
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <div class="d-flex align-items-center flex-wrap gap-2 mb-1">
                            <span class="badge bg-primary px-2 py-1"><i class="fas fa-pills me-1"></i> ${escapeHtml(rec.drug)}</span>
                            <span class="badge badge-support">Support: ${(rec.support * 100).toFixed(1)}%</span>
                            <span class="badge badge-confidence">Conf: ${(rec.confidence * 100).toFixed(1)}%</span>
                            <span class="badge badge-lift">Lift: ${rec.lift.toFixed(2)}</span>
                            ${ddiBadge}
                        </div>
                        <div class="text-muted small">
                            <i class="fas fa-link me-1"></i> Luật liên quan: <code>{${escapeHtml(rec.matchingAntecedent)}} &rarr; {${escapeHtml(rec.drug)}}</code>
                        </div>
                        ${rec.interactionStatus === 'KNOWN' && rec.interactionDescription ? `
                            <div class="mt-2 p-2 rounded small bg-warning-subtle text-dark border border-warning-subtle">
                                <i class="fas fa-exclamation-triangle text-danger me-1"></i>
                                <strong>Cảnh báo DDI:</strong> ${escapeHtml(rec.interactionDescription)}
                            </div>
                        ` : ''}
                    </div>
                    <div class="d-flex gap-1 flex-column flex-sm-row">
                        ${rec.medicineId ? `
                            <button type="button" class="btn btn-sm btn-outline-success add-recommended-btn"
                                data-id="${rec.medicineId}"
                                data-name="${escapeHtml(rec.drug)}">
                                <i class="fas fa-plus-circle me-1"></i> Thêm thuốc
                            </button>
                        ` : `
                            <span class="badge bg-secondary mb-1">Chưa có mã thuốc</span>
                        `}
                        <button type="button" class="btn btn-sm btn-outline-secondary dismiss-recommended-btn" title="Bỏ qua gợi ý này">
                            <i class="fas fa-times me-1"></i> Bỏ qua
                        </button>
                    </div>
                </div>
            `;
            recommendationList.appendChild(itemDiv);
        });

        // Gắn sự kiện thêm thuốc gợi ý vào đơn
        document.querySelectorAll('.add-recommended-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                const recId = this.getAttribute('data-id');
                const recName = this.getAttribute('data-name');
                addMedicineToPrescription(recId, recName, 'Theo chỉ định', '2 lần/ngày (sáng - chiều)', 7, 'Uống sau bữa ăn');
            });
        });

        // Gắn sự kiện Bỏ qua gợi ý
        document.querySelectorAll('.dismiss-recommended-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                const itemDiv = this.closest('.recommendation-item');
                if (itemDiv) {
                    itemDiv.style.opacity = '0.4';
                    itemDiv.style.transition = 'all 0.3s ease';
                    setTimeout(() => {
                        itemDiv.remove();
                        if (recommendationList.children.length === 0) {
                            noRecommendationText.classList.remove('d-none');
                        }
                    }, 300);
                }
            });
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
