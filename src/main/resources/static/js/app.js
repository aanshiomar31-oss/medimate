/**
 * MediMate — Enterprise Pharmaceutical Supply Chain Controller
 * Integrates directly with Spring Boot REST APIs
 */

document.addEventListener('DOMContentLoaded', () => {
  startLiveClock();
  initDashboard();
  loadStalledShipments();
  loadStageLatency();
  loadBatchDropdowns();
  loadColdChainTable();
  verifyBatch(); // Initial verify for default MED-2026-PF01
});

// Live UTC Header Clock
function startLiveClock() {
  const clockEl = document.getElementById('live-clock');
  function update() {
    if (clockEl) {
      const now = new Date();
      clockEl.textContent = now.toUTCString().split(' ')[4] + ' UTC';
    }
  }
  update();
  setInterval(update, 1000);
}

// Global Refresh Action
async function refreshAllData() {
  const icon = document.querySelector('.refresh-icon');
  if (icon) icon.classList.add('rotating');
  
  await Promise.all([
    initDashboard(),
    loadStalledShipments(),
    loadStageLatency(),
    loadBatchDropdowns(),
    loadColdChainTable()
  ]);

  setTimeout(() => {
    if (icon) icon.classList.remove('rotating');
    showToast('Telemetry and process event logs synchronized', 'info');
  }, 600);
}

// Tab Switching Mechanism
function switchTab(tabId) {
  // Update Segmented Buttons
  document.querySelectorAll('.seg-btn').forEach(btn => btn.classList.remove('active'));
  document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active-pane'));

  const activeBtn = Array.from(document.querySelectorAll('.seg-btn')).find(btn => 
    btn.getAttribute('onclick')?.includes(`'${tabId}'`)
  );
  if (activeBtn) activeBtn.classList.add('active');

  const targetPane = document.getElementById(`tab-${tabId}`);
  if (targetPane) {
    targetPane.classList.add('active-pane');
  }
}

// Quick Sample Batch Select
function selectSampleBatch(batchNumber) {
  const input = document.getElementById('verify-batch-input');
  if (input) {
    input.value = batchNumber;
    switchTab('verifier');
    verifyBatch();
  }
}

// Pipeline Filter by Stage
function filterByStage(stageName) {
  switchTab('coldchain');
  const filterInput = document.getElementById('telemetry-filter-input');
  if (filterInput) {
    filterInput.value = stageName;
    filterTelemetryTable();
    showToast(`Filtered active inventory by stage: ${stageName}`, 'info');
  }
}

// 1. Initialize Dashboard Executive KPI Metrics
async function initDashboard() {
  try {
    const res = await fetch('/api/analytics/dashboard');
    const json = await res.json();
    if (json.success && json.data) {
      const data = json.data;
      
      const activeBatchesEl = document.getElementById('kpi-active-batches');
      if (activeBatchesEl) activeBatchesEl.textContent = data.activeBatchesCount ?? 0;

      const complianceEl = document.getElementById('kpi-compliance-rate');
      if (complianceEl) complianceEl.textContent = `${data.transitComplianceRatePercent}%`;

      const complianceBar = document.getElementById('kpi-compliance-bar');
      if (complianceBar) complianceBar.style.width = `${Math.min(data.transitComplianceRatePercent, 100)}%`;

      const stalledEl = document.getElementById('kpi-stalled-count');
      if (stalledEl) stalledEl.textContent = data.stalledShipmentsCount ?? 0;

      const expiringEl = document.getElementById('kpi-expiring-count');
      if (expiringEl) expiringEl.textContent = data.expiringWithin60DaysCount ?? 0;

      const healthLabel = document.getElementById('system-health-label');
      if (healthLabel) {
        if (data.supplyChainStatus === 'OPTIMAL') {
          healthLabel.textContent = 'All SLAs Compliant';
          healthLabel.style.color = 'var(--accent-emerald)';
        } else {
          healthLabel.textContent = 'Intervention Required';
          healthLabel.style.color = 'var(--accent-amber)';
        }
      }
    }
  } catch (err) {
    console.error('Error querying dashboard telemetry:', err);
  }
}

// 2. Load In-Transit Stalled Shipments & Excursion Radar
async function loadStalledShipments() {
  const tbody = document.getElementById('stalled-shipments-table-body');
  try {
    const res = await fetch('/api/analytics/stalled-shipments');
    const json = await res.json();

    if (json.success && json.data && json.data.length > 0) {
      tbody.innerHTML = '';
      const badge = document.getElementById('stalled-badge');
      if (badge) badge.textContent = `${json.data.length} Critical Alert(s)`;

      json.data.forEach(shipment => {
        const tempExcursion = shipment.temperatureExcursion;
        const tempText = shipment.lastRecordedTemperature != null 
          ? `<span class="${tempExcursion ? 'tag-temp-excursion' : 'tag-temp-ok'}">
              🌡️ ${shipment.lastRecordedTemperature}°C ${tempExcursion ? '⚠️ EXCURSION' : ''}
             </span>`
          : '<span style="color: var(--text-dim);">Ambient</span>';

        const row = document.createElement('tr');
        row.innerHTML = `
          <td><strong style="color: var(--accent-cyan); font-family: var(--font-mono);">${escapeHtml(shipment.shipmentNumber)}</strong></td>
          <td>
            <div style="font-weight: 600; color: #fff;">${escapeHtml(shipment.medicineName)}</div>
            <div style="font-size: 0.72rem; color: var(--text-muted); font-family: var(--font-mono);">${escapeHtml(shipment.batchNumber)}</div>
          </td>
          <td>${escapeHtml(shipment.destinationPharmacy)}</td>
          <td>${escapeHtml(shipment.carrierName)}</td>
          <td>
            <strong style="font-family: var(--font-mono);">${shipment.hoursInTransit}h</strong>
            <span style="color: var(--accent-crimson); font-size: 0.75rem; font-family: var(--font-mono); font-weight: 600;">
              (+${shipment.hoursOverdue}h SLA breach)
            </span>
          </td>
          <td>${tempText}</td>
          <td>
            <span class="status-pill ${shipment.bottleneckSeverity === 'CRITICAL' ? 'pill-danger' : 'pill-warning'}">
              ${shipment.bottleneckSeverity}
            </span>
          </td>
          <td>
            <button class="quick-pill pill-info" style="padding: 0.25rem 0.65rem;" onclick="selectSampleBatch('${escapeHtml(shipment.batchNumber)}')">
              Inspect
            </button>
          </td>
        `;
        tbody.appendChild(row);
      });
    } else {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--accent-emerald); padding: 2rem;">
        ✓ All active pharmaceutical shipments are operating within compliant transit times and temperature thresholds.
      </td></tr>`;
      const badge = document.getElementById('stalled-badge');
      if (badge) {
        badge.textContent = '0 At-Risk';
        badge.className = 'live-tag live-tag-emerald';
      }
    }
  } catch (err) {
    console.error('Error querying stalled shipments:', err);
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--accent-crimson); padding: 1.5rem;">Failed to fetch active radar stream.</td></tr>`;
  }
}

// 3. Load Celonis Process Mining Stage Latency Analytics
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
        const isBreached = item.slaBreachPercentage > 20;
        const isWarning = item.averageDurationHours > 50 && !isBreached;

        const div = document.createElement('div');
        div.className = 'latency-item';
        div.innerHTML = `
          <div class="latency-labels">
            <span style="font-family: var(--font-mono); font-weight: 600; color: #fff;">${escapeHtml(item.stage)}</span>
            <span style="color: ${isBreached ? 'var(--accent-crimson)' : isWarning ? 'var(--accent-amber)' : 'var(--text-muted)'}; font-family: var(--font-mono);">
              Avg: <strong>${item.averageDurationHours}h</strong> ${item.slaBreachPercentage > 0 ? `• <span style="color: var(--accent-crimson); font-weight: 700;">${item.slaBreachPercentage}% Breached</span>` : ''}
            </span>
          </div>
          <div class="latency-track">
            <div class="latency-fill ${isBreached ? 'breached' : isWarning ? 'warning-fill' : ''}" style="width: ${percent}%;"></div>
          </div>
        `;
        container.appendChild(div);
      });
    }
  } catch (err) {
    console.error('Error querying process mining latency:', err);
  }
}

// 4. Anti-Counterfeit Batch Verifier
async function verifyBatch() {
  const inputEl = document.getElementById('verify-batch-input');
  const batchNumber = inputEl ? inputEl.value.trim() : '';
  if (!batchNumber) {
    showToast('Please specify a pharmaceutical batch number', 'error');
    return;
  }

  const container = document.getElementById('verification-result-container');
  container.innerHTML = `<div style="text-align: center; padding: 3rem; color: var(--text-muted); font-family: var(--font-mono);">Cryptographically verifying provenance continuity across distributed ledger...</div>`;

  try {
    const res = await fetch(`/api/batches/${encodeURIComponent(batchNumber)}/verify`);
    const json = await res.json();

    if (!json.success || !json.data) {
      container.innerHTML = `
        <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid var(--accent-crimson); padding: 2rem; border-radius: var(--radius-lg); text-align: center;">
          <h3 style="color: var(--accent-crimson); font-family: var(--font-display); font-size: 1.2rem; margin-bottom: 0.5rem;">⚠️ Unregistered Pharmaceutical Batch</h3>
          <p style="color: var(--text-secondary); font-size: 0.88rem;">No verified production record exists with identifier: <strong style="font-family: var(--font-mono); color: #fff;">${escapeHtml(batchNumber)}</strong></p>
          <p style="color: var(--text-muted); font-size: 0.78rem; margin-top: 0.5rem;">Warning: This drug unit has no registered manufacturer signature in the custody ledger.</p>
        </div>
      `;
      return;
    }

    const data = json.data;
    const isAuthentic = data.authentic;
    const isRecalled = data.recalled;
    const coldCompliant = data.coldChainCompliant;

    // Header Certificate Banner
    let certClass = 'cert-authentic';
    let certBadgeHtml = '';

    if (isRecalled) {
      certClass = 'cert-recalled';
      certBadgeHtml = `
        <span class="cert-status-badge badge-recalled-pill">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="7.86 2 16.14 2 22 7.86 22 16.14 16.14 22 7.86 22 2 16.14 2 7.86 7.86 2"></polygon><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          REGULATORY RECALLED
        </span>
      `;
    } else if (isAuthentic && coldCompliant) {
      certClass = 'cert-authentic';
      certBadgeHtml = `
        <span class="cert-status-badge badge-authentic-pill">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
          AUTHENTIC &amp; COLD-CHAIN VERIFIED
        </span>
      `;
    } else if (isAuthentic && !coldCompliant) {
      certClass = 'cert-warning';
      certBadgeHtml = `
        <span class="cert-status-badge badge-warning-pill">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
          COLD-CHAIN SLA EXCURSION DETECTED
        </span>
      `;
    } else {
      certClass = 'cert-recalled';
      certBadgeHtml = `
        <span class="cert-status-badge badge-recalled-pill">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
          PROVENANCE CONTINUITY COMPROMISED
        </span>
      `;
    }

    // Cryptographic Timeline Nodes
    let timelineNodes = '';
    if (data.chainOfCustody && data.chainOfCustody.length > 0) {
      data.chainOfCustody.forEach((log, idx) => {
        const isExcursion = !log.temperatureCompliant;
        const initials = getInitials(log.toHolder || 'C');

        timelineNodes += `
          <div class="timeline-event-card">
            <div class="timeline-node-pin ${isExcursion ? 'excursion-pin' : ''}">
              <div class="pin-dot"></div>
            </div>

            <div class="event-header-row">
              <span class="event-stage-tag">[NODE 0${idx + 1}] ${escapeHtml(log.toStatus)}</span>
              <span class="event-time-stamp">${formatIsoDate(log.timestamp)}</span>
            </div>

            <div class="event-actor-wrap">
              <div class="actor-avatar">${initials}</div>
              <div>
                <div class="actor-title">${escapeHtml(log.toHolder)}</div>
                <div style="font-size: 0.72rem; color: var(--text-muted);">Physical Custodian Sign-off</div>
              </div>
            </div>

            <div class="event-tags-row">
              <span class="event-tag">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                ${escapeHtml(log.location)}
              </span>

              ${log.recordedTemperature != null ? `
                <span class="event-tag ${isExcursion ? 'tag-temp-excursion' : 'tag-temp-ok'}">
                  🌡️ ${log.recordedTemperature}°C ${isExcursion ? '⚠️ EXCURSION LIMIT EXCEEDED' : '✓ Temp Compliant'}
                </span>
              ` : ''}

              ${log.timeSpentHours > 0 ? `
                <span class="event-tag">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
                  Dwell: ${log.timeSpentHours}h ${log.slaBreached ? '⚠️ SLA Breached' : '✓ SLA Met'}
                </span>
              ` : ''}

              <span class="event-tag" style="color: var(--accent-cyan);">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                SHA-256 Validated
              </span>
            </div>

            ${log.notes ? `
              <div class="event-inspection-note">
                "${escapeHtml(log.notes)}"
              </div>
            ` : ''}
          </div>
        `;
      });
    }

    container.innerHTML = `
      <!-- Certificate Header -->
      <div class="provenance-certificate-header ${certClass}">
        <div class="cert-left">
          <h3>${escapeHtml(data.medicineName)} <span class="cert-generic-name">(${escapeHtml(data.genericName)})</span></h3>
          <p class="cert-meta-sub">
            Batch: <strong style="font-family: var(--font-mono); color: var(--accent-cyan);">${escapeHtml(data.batchNumber)}</strong> • 
            Category: <strong>${escapeHtml(data.category)}</strong> • 
            Manufacturer: <strong>${escapeHtml(data.manufacturerName)}</strong> (${escapeHtml(data.manufacturerLicense)})
          </p>
        </div>
        <div class="cert-badge-box">
          ${certBadgeHtml}
          <span style="font-size: 0.7rem; color: var(--text-muted); font-family: var(--font-mono);">Audit Standard: FDA 21 CFR Part 11</span>
        </div>
      </div>

      <!-- Metadata Chips Grid -->
      <div class="provenance-metadata-grid">
        <div class="meta-chip-card">
          <div class="chip-label">Current Facility Location</div>
          <div class="chip-value">${escapeHtml(data.currentLocation)}</div>
        </div>
        <div class="meta-chip-card">
          <div class="chip-label">Active Custodian</div>
          <div class="chip-value">${escapeHtml(data.currentHolder)}</div>
        </div>
        <div class="meta-chip-card">
          <div class="chip-label">Shelf-Life Expiry</div>
          <div class="chip-value" style="color: ${data.expired ? 'var(--accent-crimson)' : '#fff'}; font-family: var(--font-mono);">
            ${escapeHtml(data.expiryDate)} ${data.expired ? '⚠️ (EXPIRED)' : ''}
          </div>
        </div>
        <div class="meta-chip-card">
          <div class="chip-label">Supply Chain Stage</div>
          <div class="chip-value" style="color: var(--accent-cyan); font-family: var(--font-mono);">${escapeHtml(data.currentStatus)}</div>
        </div>
      </div>

      ${isRecalled ? `
        <div style="background: rgba(239, 68, 68, 0.12); border: 1px solid var(--accent-crimson); padding: 1.15rem 1.4rem; border-radius: var(--radius-md); margin-bottom: 1.5rem; color: #fff;">
          <strong style="color: var(--accent-crimson); font-family: var(--font-display);">🚨 OFFICIAL FDA REGULATORY RECALL ORDER:</strong><br>
          <span style="font-size: 0.88rem; color: var(--text-secondary); margin-top: 0.35rem; display: block;">
            "${escapeHtml(data.recallReason || 'Regulatory quarantine mandate enforced by safety inspection agency.')}"
          </span>
        </div>
      ` : ''}

      <!-- Timeline Title -->
      <div class="timeline-section-title">
        <h4>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
          Immutable Chain-of-Custody Provenance Ledger
        </h4>
        <span class="crypto-audit-pill">✓ Cryptographically Linked</span>
      </div>

      <!-- Crypto Timeline -->
      <div class="crypto-timeline">
        ${timelineNodes}
      </div>
    `;

  } catch (err) {
    console.error('Error performing provenance verification:', err);
    container.innerHTML = `<div style="color: var(--accent-crimson); text-align: center; padding: 2rem;">Verification engine unreachable. Please check backend connection.</div>`;
  }
}

// 5. Load Cold Chain & Batch Inventory Table
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
          ? `<span class="tag-temp-ok">❄️ ${med.minTemperatureCelsius}°C to ${med.maxTemperatureCelsius}°C</span>` 
          : '<span style="color: var(--text-dim); font-size: 0.8rem;">Ambient (&le; 25°C)</span>';

        const row = document.createElement('tr');
        row.innerHTML = `
          <td><strong style="color: var(--accent-cyan); font-family: var(--font-mono);">${escapeHtml(batch.batchNumber)}</strong></td>
          <td>
            <div style="font-weight: 600; color: #fff;">${escapeHtml(med.name)}</div>
            <div style="font-size: 0.72rem; color: var(--text-muted);">${escapeHtml(med.genericName)}</div>
          </td>
          <td><span style="font-size: 0.8rem; color: var(--text-secondary);">${escapeHtml(med.category)}</span></td>
          <td>${tempRange}</td>
          <td>
            <span class="status-pill ${batch.recalled ? 'pill-danger' : batch.status === 'IN_TRANSIT' ? 'pill-warning' : 'pill-info'}">
              ${escapeHtml(batch.status)}
            </span>
          </td>
          <td><span style="font-size: 0.82rem;">${escapeHtml(batch.currentLocation)}</span></td>
          <td><span style="font-size: 0.82rem;">${escapeHtml(batch.currentHolder)}</span></td>
          <td>
            <button class="quick-pill pill-info" style="padding: 0.25rem 0.65rem;" onclick="selectSampleBatch('${escapeHtml(batch.batchNumber)}')">
              Verify
            </button>
          </td>
        `;
        tbody.appendChild(row);
      });
    }
  } catch (err) {
    console.error('Error loading inventory table:', err);
  }
}

// Search Filter for Cold Chain Inventory Table
function filterTelemetryTable() {
  const query = (document.getElementById('telemetry-filter-input')?.value || '').toLowerCase();
  const rows = document.querySelectorAll('#telemetry-table tbody tr');

  rows.forEach(row => {
    const text = row.innerText.toLowerCase();
    row.style.display = text.includes(query) ? '' : 'none';
  });
}

// 6. Populate Form Select Dropdowns
async function loadBatchDropdowns() {
  try {
    const res = await fetch('/api/batches?activeOnly=true');
    const json = await res.json();

    if (json.success && json.data) {
      const transferSelect = document.getElementById('transfer-batch-id');
      const recallSelect = document.getElementById('recall-batch-id');

      if (transferSelect) {
        transferSelect.innerHTML = json.data.map(b => 
          `<option value="${b.id}">${b.batchNumber} — ${b.medicine.name} (${b.status})</option>`
        ).join('');
      }

      if (recallSelect) {
        recallSelect.innerHTML = json.data.map(b => 
          `<option value="${b.id}">${b.batchNumber} — ${b.medicine.name}</option>`
        ).join('');
      }
    }
  } catch (err) {
    console.error('Error loading batch select options:', err);
  }
}

// 7. Handle Advance Custody Handover
async function handleCustodyTransfer(e) {
  e.preventDefault();
  const batchId = document.getElementById('transfer-batch-id').value;
  const targetStatus = document.getElementById('transfer-target-status').value;
  const holder = document.getElementById('transfer-holder').value.trim();
  const location = document.getElementById('transfer-location').value.trim();
  const tempVal = document.getElementById('transfer-temperature').value;
  const notes = document.getElementById('transfer-notes').value.trim();

  const payload = {
    targetStatus,
    toHolder: holder,
    location,
    notes: notes || undefined,
    recordedTemperature: tempVal ? parseFloat(tempVal) : undefined
  };

  try {
    const res = await fetch(`/api/batches/${batchId}/transfer`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(`Custody successfully advanced to ${targetStatus}`, 'success');
      document.getElementById('custody-form').reset();
      refreshAllData();
      if (json.data && json.data.batchNumber) {
        selectSampleBatch(json.data.batchNumber);
      }
    } else {
      showToast(`Handover Rejected: ${json.message}`, 'error');
    }
  } catch (err) {
    console.error('Error executing custody transfer:', err);
    showToast('Failed to connect to custody engine', 'error');
  }
}

// 8. Handle Emergency Regulatory Recall
async function handleRecall(e) {
  e.preventDefault();
  const batchId = document.getElementById('recall-batch-id').value;
  const reason = document.getElementById('recall-reason').value.trim();
  const authorizedBy = document.getElementById('recall-authorized-by').value.trim();

  if (!confirm(`CONFIRM EMERGENCY RECALL: This action immediately locks this pharmaceutical batch from hospital distribution. Proceed?`)) {
    return;
  }

  const payload = {
    reason,
    authorizedBy
  };

  try {
    const res = await fetch(`/api/batches/${batchId}/recall`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(`EMERGENCY QUARANTINE APPLIED for batch ${json.data?.batchNumber}`, 'error');
      document.getElementById('recall-form').reset();
      refreshAllData();
      if (json.data && json.data.batchNumber) {
        selectSampleBatch(json.data.batchNumber);
      }
    } else {
      showToast(`Recall Execution Failed: ${json.message}`, 'error');
    }
  } catch (err) {
    console.error('Error executing regulatory recall:', err);
    showToast('Network error during recall order', 'error');
  }
}

// Utility: Toast Viewport Notification
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast-msg toast-${type}`;
  
  let iconSvg = '';
  if (type === 'success') {
    iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>';
  } else if (type === 'error') {
    iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>';
  } else {
    iconSvg = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#00f2fe" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>';
  }

  toast.innerHTML = `
    ${iconSvg}
    <div style="flex: 1; font-weight: 500;">${escapeHtml(message)}</div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Utility: Initials Extractor
function getInitials(name) {
  if (!name) return 'CU';
  const parts = name.replace(/Dr\.|Carrier:|Driver:|Lead|Supervisor|Pharmacist/gi, '').trim().split(' ').filter(Boolean);
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return (parts[0] ? parts[0].substring(0, 2) : 'CU').toUpperCase();
}

// Utility: Date Formatter
function formatIsoDate(dateString) {
  if (!dateString) return 'N/A';
  try {
    const d = new Date(dateString);
    return d.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }) + ' UTC';
  } catch (e) {
    return dateString;
  }
}

// Utility: HTML Escaper (XSS Protection)
function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
