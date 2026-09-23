/**
 * MediMate Enterprise Frontend Controller
 * Connects directly to Spring Boot REST APIs
 */

document.addEventListener('DOMContentLoaded', () => {
  initDashboard();
  loadStalledShipments();
  loadStageLatency();
  loadBatchDropdowns();
  loadColdChainTable();
  verifyBatch(); // Initial verify for MED-2026-PF01
});

// Switch Tabs
function switchTab(tabId) {
  document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(content => content.style.display = 'none');

  event.currentTarget.classList.add('active');
  const target = document.getElementById(`tab-${tabId}`);
  if (target) {
    target.style.display = 'block';
  }
}

// 1. Initialize Dashboard KPI Metrics
async function initDashboard() {
  try {
    const res = await fetch('/api/analytics/dashboard');
    const json = await res.json();
    if (json.success && json.data) {
      const data = json.data;
      document.getElementById('kpi-active-batches').textContent = data.activeBatchesCount || 0;
      document.getElementById('kpi-compliance-rate').textContent = `${data.transitComplianceRatePercent}%`;
      document.getElementById('kpi-stalled-count').textContent = data.stalledShipmentsCount || 0;
      document.getElementById('kpi-expiring-count').textContent = data.expiringWithin60DaysCount || 0;

      const healthLabel = document.getElementById('system-health-label');
      if (data.supplyChainStatus === 'OPTIMAL') {
        healthLabel.textContent = 'System Healthy';
        healthLabel.style.color = 'var(--accent-emerald)';
      } else {
        healthLabel.textContent = 'Attention Required';
        healthLabel.style.color = 'var(--accent-amber)';
      }
    }
  } catch (err) {
    console.error('Error loading dashboard KPI:', err);
  }
}

// 2. Load In-Transit Stalled Shipments
async function loadStalledShipments() {
  const tbody = document.getElementById('stalled-shipments-table-body');
  try {
    const res = await fetch('/api/analytics/stalled-shipments');
    const json = await res.json();

    if (json.success && json.data && json.data.length > 0) {
      tbody.innerHTML = '';
      document.getElementById('stalled-badge').textContent = `${json.data.length} Delayed / At-Risk`;

      json.data.forEach(shipment => {
        const tempExcursion = shipment.temperatureExcursion;
        const tempText = shipment.lastRecordedTemperature != null 
          ? `<span style="color: ${tempExcursion ? 'var(--accent-crimson)' : 'var(--accent-emerald)'}; font-weight: 600;">
              ${shipment.lastRecordedTemperature}°C ${tempExcursion ? '⚠️ EXCURSION' : ''}
             </span>`
          : '<span style="color: var(--text-sub);">N/A</span>';

        const row = document.createElement('tr');
        row.innerHTML = `
          <td style="font-weight: 600; color: var(--accent-cyan);">${escapeHtml(shipment.shipmentNumber)}</td>
          <td><strong>${escapeHtml(shipment.medicineName)}</strong><br><span style="font-size: 0.75rem; color: var(--text-sub);">${escapeHtml(shipment.batchNumber)}</span></td>
          <td>${escapeHtml(shipment.destinationPharmacy)}</td>
          <td>${escapeHtml(shipment.carrierName)}</td>
          <td>
            <strong>${shipment.hoursInTransit}h</strong> 
            <span style="color: var(--accent-crimson); font-size: 0.78rem;">(+${shipment.hoursOverdue}h over ${shipment.transitSlaHours}h SLA)</span>
          </td>
          <td>${tempText}</td>
          <td>
            <span class="status-pill ${shipment.bottleneckSeverity === 'CRITICAL' ? 'badge-crimson' : 'badge-amber'}">
              ${shipment.bottleneckSeverity}
            </span>
          </td>
        `;
        tbody.appendChild(row);
      });
    } else {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--accent-emerald); padding: 1.5rem;">
        ✓ All active cold-chain shipments are operating on-schedule within safe temperature thresholds.
      </td></tr>`;
      document.getElementById('stalled-badge').textContent = '0 Delayed';
      document.getElementById('stalled-badge').className = 'panel-badge badge-emerald';
    }
  } catch (err) {
    console.error('Error loading stalled shipments:', err);
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--accent-crimson);">Failed to load telemetry data.</td></tr>`;
  }
}

// 3. Load Process Mining Stage Latency
async function loadStageLatency() {
  const container = document.getElementById('latency-bars-container');
  try {
    const res = await fetch('/api/analytics/latency');
    const json = await res.json();

    if (json.success && json.data && json.data.length > 0) {
      container.innerHTML = '';
      const maxAverage = Math.max(...json.data.map(d => d.averageDurationHours), 1);

      json.data.forEach(item => {
        const percent = Math.min(Math.round((item.averageDurationHours / maxAverage) * 100), 100);
        const isHigh = item.slaBreachPercentage > 25 || item.averageDurationHours > 40;

        const div = document.createElement('div');
        div.className = 'latency-item';
        div.innerHTML = `
          <div class="latency-labels">
            <span style="font-weight: 600; color: var(--text-main); font-size: 0.82rem;">${escapeHtml(item.stage)}</span>
            <span style="color: ${isHigh ? 'var(--accent-amber)' : 'var(--text-muted)'}; font-size: 0.8rem;">
              Avg: <strong>${item.averageDurationHours}h</strong> (Max: ${item.maxDurationHours}h) ${item.slaBreachPercentage > 0 ? `• <span style="color: var(--accent-crimson);">${item.slaBreachPercentage}% Breached</span>` : ''}
            </span>
          </div>
          <div class="latency-bar-container">
            <div class="latency-bar ${isHigh ? 'high' : ''}" style="width: ${percent}%;"></div>
          </div>
        `;
        container.appendChild(div);
      });
    }
  } catch (err) {
    console.error('Error loading stage latency:', err);
  }
}

// 4. Anti-Counterfeit Batch Verifier
async function verifyBatch() {
  const batchNumber = document.getElementById('verify-batch-input').value.trim();
  if (!batchNumber) {
    showToast('Please enter or select a batch number', 'error');
    return;
  }

  const container = document.getElementById('verification-result-container');
  container.innerHTML = `<div style="text-align: center; padding: 2rem; color: var(--text-muted);">Verifying batch provenance across supply chain...</div>`;

  try {
    const res = await fetch(`/api/batches/${encodeURIComponent(batchNumber)}/verify`);
    const json = await res.json();

    if (!json.success || !json.data) {
      container.innerHTML = `
        <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid var(--accent-crimson); padding: 1.5rem; border-radius: var(--radius-md); text-align: center;">
          <h3 style="color: var(--accent-crimson); font-family: var(--font-heading); margin-bottom: 0.5rem;">⚠️ Batch Not Found</h3>
          <p style="color: var(--text-muted); font-size: 0.88rem;">No registered pharmaceutical batch exists with identifier: <strong>${escapeHtml(batchNumber)}</strong></p>
        </div>
      `;
      return;
    }

    const data = json.data;
    const isAuthentic = data.authentic;
    const isRecalled = data.recalled;
    const coldCompliant = data.coldChainCompliant;

    let badgeHtml = '';
    if (isRecalled) {
      badgeHtml = `<div class="verified-badge compromised">🚨 REGULATORY RECALLED</div>`;
    } else if (isAuthentic && coldCompliant) {
      badgeHtml = `<div class="verified-badge authentic">✓ AUTHENTIC &amp; COLD-CHAIN VERIFIED</div>`;
    } else if (isAuthentic && !coldCompliant) {
      badgeHtml = `<div class="verified-badge" style="background: rgba(245, 158, 11, 0.15); border: 1px solid var(--accent-amber); color: var(--accent-amber);">⚠️ AUTHENTIC (TEMPERATURE EXCURSION DETECTED)</div>`;
    } else {
      badgeHtml = `<div class="verified-badge compromised">❌ PROVENANCE BROKEN — SUSPECTED COUNTERFEIT</div>`;
    }

    let timelineHtml = '';
    if (data.chainOfCustody && data.chainOfCustody.length > 0) {
      data.chainOfCustody.forEach((event, idx) => {
        const isExcursion = !event.temperatureCompliant;
        const dotClass = isExcursion ? 'excursion' : 'compliant';

        timelineHtml += `
          <div class="timeline-item">
            <div class="timeline-dot ${dotClass}"></div>
            <div class="timeline-card">
              <div class="timeline-title">
                <span class="timeline-stage">${idx + 1}. ${escapeHtml(event.toStatus)}</span>
                <span class="timeline-time">${formatDate(event.timestamp)}</span>
              </div>
              <p style="font-size: 0.85rem; color: var(--text-main); margin-bottom: 0.4rem;">
                <strong>Custodian:</strong> ${escapeHtml(event.toHolder)}
              </p>
              <div class="timeline-meta">
                <span class="meta-tag">📍 ${escapeHtml(event.location)}</span>
                ${event.recordedTemperature != null 
                  ? `<span class="meta-tag" style="color: ${isExcursion ? 'var(--accent-crimson)' : 'var(--accent-emerald)'}; font-weight: 600;">
                      🌡️ ${event.recordedTemperature}°C ${isExcursion ? '(LIMIT EXCEEDED)' : '(Compliant)'}
                     </span>` 
                  : ''}
                ${event.timeSpentHours > 0 ? `<span class="meta-tag">⏱️ Dwell: ${event.timeSpentHours}h</span>` : ''}
              </div>
              ${event.notes ? `<p style="font-size: 0.78rem; color: var(--text-sub); margin-top: 0.4rem; font-style: italic;">Note: ${escapeHtml(event.notes)}</p>` : ''}
            </div>
          </div>
        `;
      });
    }

    container.innerHTML = `
      <div class="verification-header">
        <div>
          <h3 style="font-family: var(--font-heading); font-size: 1.25rem; font-weight: 700; margin-bottom: 0.2rem;">
            ${escapeHtml(data.medicineName)} <span style="font-weight: 400; color: var(--text-muted); font-size: 0.95rem;">(${escapeHtml(data.genericName)})</span>
          </h3>
          <p style="font-size: 0.82rem; color: var(--text-muted);">
            Batch: <strong style="color: var(--accent-cyan);">${escapeHtml(data.batchNumber)}</strong> • 
            Manufacturer: <strong>${escapeHtml(data.manufacturerName)}</strong> (${escapeHtml(data.manufacturerLicense)})
          </p>
        </div>
        ${badgeHtml}
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
        <div style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-color); padding: 0.85rem 1rem; border-radius: var(--radius-md);">
          <div style="font-size: 0.75rem; color: var(--text-sub); text-transform: uppercase;">Current Location</div>
          <div style="font-weight: 600; font-size: 0.88rem; color: var(--text-main); margin-top: 0.2rem;">${escapeHtml(data.currentLocation)}</div>
        </div>
        <div style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-color); padding: 0.85rem 1rem; border-radius: var(--radius-md);">
          <div style="font-size: 0.75rem; color: var(--text-sub); text-transform: uppercase;">Current Custodian</div>
          <div style="font-weight: 600; font-size: 0.88rem; color: var(--text-main); margin-top: 0.2rem;">${escapeHtml(data.currentHolder)}</div>
        </div>
        <div style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-color); padding: 0.85rem 1rem; border-radius: var(--radius-md);">
          <div style="font-size: 0.75rem; color: var(--text-sub); text-transform: uppercase;">Expiry Date</div>
          <div style="font-weight: 600; font-size: 0.88rem; color: ${data.expired ? 'var(--accent-crimson)' : 'var(--text-main)'}; margin-top: 0.2rem;">
            ${escapeHtml(data.expiryDate)} ${data.expired ? '(EXPIRED)' : ''}
          </div>
        </div>
        <div style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-color); padding: 0.85rem 1rem; border-radius: var(--radius-md);">
          <div style="font-size: 0.75rem; color: var(--text-sub); text-transform: uppercase;">Current Status</div>
          <div style="font-weight: 600; font-size: 0.88rem; color: var(--accent-cyan); margin-top: 0.2rem;">${escapeHtml(data.currentStatus)}</div>
        </div>
      </div>

      ${isRecalled ? `
        <div style="background: rgba(239, 68, 68, 0.15); border: 1px solid var(--accent-crimson); padding: 1rem 1.25rem; border-radius: var(--radius-md); margin-bottom: 1.5rem; color: var(--accent-crimson);">
          <strong>⚠️ RECALL NOTICE:</strong> ${escapeHtml(data.recallReason || 'Regulatory quarantine active')}
        </div>
      ` : ''}

      <h4 style="font-family: var(--font-heading); font-size: 1rem; margin-top: 1.5rem; margin-bottom: 0.75rem; color: var(--text-main);">
        📜 Complete Immutable Chain-of-Custody (Audit Trail)
      </h4>
      <div class="timeline">
        ${timelineHtml}
      </div>
    `;
  } catch (err) {
    console.error('Error verifying batch:', err);
    container.innerHTML = `<div style="color: var(--accent-crimson); text-align: center; padding: 2rem;">Error connecting to verification service.</div>`;
  }
}

// 5. Load Cold Chain Table
async function loadColdChainTable() {
  const tbody = document.getElementById('cold-chain-table-body');
  try {
    const res = await fetch('/api/batches');
    const json = await res.json();

    if (json.success && json.data) {
      tbody.innerHTML = '';
      json.data.forEach(batch => {
        const med = batch.medicine;
        const tempRange = med.requiresColdChain 
          ? `<span style="color: var(--accent-cyan); font-weight: 600;">${med.minTemperatureCelsius}°C to ${med.maxTemperatureCelsius}°C</span>` 
          : '<span style="color: var(--text-sub);">Ambient (&le; 25°C)</span>';

        const row = document.createElement('tr');
        row.innerHTML = `
          <td style="font-weight: 600; color: var(--accent-cyan);">${escapeHtml(batch.batchNumber)}</td>
          <td><strong>${escapeHtml(med.name)}</strong> (${escapeHtml(med.genericName)})</td>
          <td>${escapeHtml(med.category)}</td>
          <td>${tempRange}</td>
          <td><span class="status-pill badge-cyan">${escapeHtml(batch.status)}</span></td>
          <td>${escapeHtml(batch.currentLocation)}</td>
          <td>${escapeHtml(batch.currentHolder)}</td>
        `;
        tbody.appendChild(row);
      });
    }
  } catch (err) {
    console.error('Error loading cold chain table:', err);
  }
}

// 6. Load Batch Dropdowns for Actions
async function loadBatchDropdowns() {
  const transferSelect = document.getElementById('transfer-batch-id');
  const recallSelect = document.getElementById('recall-batch-id');

  try {
    const res = await fetch('/api/batches?activeOnly=true');
    const json = await res.json();

    if (json.success && json.data) {
      transferSelect.innerHTML = '';
      recallSelect.innerHTML = '';

      json.data.forEach(batch => {
        const opt1 = document.createElement('option');
        opt1.value = batch.id;
        opt1.textContent = `${batch.batchNumber} — ${batch.medicine.name} (${batch.status})`;
        transferSelect.appendChild(opt1);

        const opt2 = document.createElement('option');
        opt2.value = batch.id;
        opt2.textContent = `${batch.batchNumber} — ${batch.medicine.name} (${batch.currentLocation})`;
        recallSelect.appendChild(opt2);
      });
    }
  } catch (err) {
    console.error('Error loading batch options:', err);
  }
}

// 7. Handle Custody Transfer
async function handleCustodyTransfer(e) {
  e.preventDefault();
  const batchId = document.getElementById('transfer-batch-id').value;
  const targetStatus = document.getElementById('transfer-target-status').value;
  const toHolder = document.getElementById('transfer-holder').value.trim();
  const newLocation = document.getElementById('transfer-location').value.trim();
  const tempVal = document.getElementById('transfer-temperature').value;
  const notes = document.getElementById('transfer-notes').value.trim();

  const payload = {
    targetStatus: targetStatus,
    toHolder: toHolder,
    newLocation: newLocation,
    recordedTemperature: tempVal ? parseFloat(tempVal) : null,
    notes: notes
  };

  try {
    const res = await fetch(`/api/batches/${batchId}/transfer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(`Custody successfully transferred to ${toHolder}!`, 'success');
      document.getElementById('custody-form').reset();
      initDashboard();
      loadStageLatency();
      loadBatchDropdowns();
      loadColdChainTable();
      loadStalledShipments();
    } else {
      showToast(json.message || 'Custody transfer failed', 'error');
    }
  } catch (err) {
    showToast('Failed to connect to backend service', 'error');
  }
}

// 8. Handle Recall
async function handleRecall(e) {
  e.preventDefault();
  const batchId = document.getElementById('recall-batch-id').value;
  const authorizedBy = document.getElementById('recall-authorized-by').value.trim();
  const recallReason = document.getElementById('recall-reason').value.trim();

  if (!confirm(`Are you sure you want to execute an emergency regulatory recall on this batch? This will lock it across the supply network.`)) {
    return;
  }

  const payload = {
    authorizedBy: authorizedBy,
    recallReason: recallReason
  };

  try {
    const res = await fetch(`/api/batches/${batchId}/recall`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(`EMERGENCY RECALL executed: Batch quarantined.`, 'error');
      document.getElementById('recall-form').reset();
      initDashboard();
      loadBatchDropdowns();
      loadColdChainTable();
    } else {
      showToast(json.message || 'Recall execution failed', 'error');
    }
  } catch (err) {
    showToast('Failed to connect to backend service', 'error');
  }
}

// Helpers
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${type === 'success' ? '✓' : '⚠️'}</span> <span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function formatDate(isoStr) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
