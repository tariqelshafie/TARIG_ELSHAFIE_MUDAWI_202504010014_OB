import { Storage } from "./storage.js";
import {
  DEMO_ACCOUNTS,
  ORGANIZATIONS,
  buildUserDirectory,
  authenticate,
  canAccess,
} from "./auth.js";
import { createSeedData, MEDICATIONS, DIAGNOSES } from "./seed.js";
import {
  effectiveOnline,
  enqueueChange,
  simulateSync,
  markPending,
  isBrowserOnline,
} from "./sync.js";

const $ = (sel, root = document) => root.querySelector(sel);
const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];

const state = {
  session: null,
  users: [],
  data: null,
  screen: "dashboard",
  regStep: 1,
  draftPatient: null,
  selectedPatientId: null,
  consultForm: null,
};

function toast(message, type = "info") {
  const host = $("#toast-host");
  const el = document.createElement("div");
  el.className = `toast ${type}`;
  el.textContent = message;
  host.appendChild(el);
  setTimeout(() => el.remove(), 3200);
}

function maskNationalId(id) {
  if (!id) return "—";
  if (id.length <= 4) return "••••";
  return "••••" + id.slice(-4);
}

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso.includes("T") ? iso : iso + "T00:00:00");
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString("en-GB");
}

function ageFromDob(dob) {
  if (!dob) return "—";
  const b = new Date(dob + "T00:00:00");
  const t = new Date();
  let age = t.getFullYear() - b.getFullYear();
  const m = t.getMonth() - b.getMonth();
  if (m < 0 || (m === 0 && t.getDate() < b.getDate())) age--;
  return age;
}

function onlineStatus() {
  return effectiveOnline(state.data);
}

async function persist() {
  await Storage.saveState({
    data: state.data,
    session: state.session,
  });
}

async function initData() {
  const saved = await Storage.loadState();
  state.users = await buildUserDirectory();
  if (saved?.data) {
    state.data = saved.data;
    state.session = saved.session || null;
  } else {
    state.data = createSeedData();
    state.session = null;
    await persist();
  }
}

function setOnlineUi() {
  const on = onlineStatus();
  $$(".conn-badge").forEach((el) => {
    el.className = `badge conn-badge ${on ? "badge-online" : "badge-offline"}`;
    el.innerHTML = on
      ? `<span>●</span> Online`
      : `<span>●</span> Offline`;
  });
  const loginStatus = $("#login-status");
  if (loginStatus) {
    loginStatus.className = `login-status ${on ? "" : "offline"}`;
    loginStatus.innerHTML = on
      ? `<span aria-hidden="true">✓</span> Online: Data will sync automatically`
      : `<span aria-hidden="true">⚠</span> Offline: Changes will queue until connection returns`;
  }
  const force = $("#force-offline");
  if (force) force.checked = !!state.data?.forceOffline;
}

function showView(id) {
  $$(".view").forEach((v) => v.classList.add("hidden"));
  $(`#view-${id}`)?.classList.remove("hidden");
}

function renderLogin() {
  showView("login");
  const org = $("#login-org");
  org.innerHTML = ORGANIZATIONS.map(
    (o) => `<option value="${o}">${o}</option>`
  ).join("");
  org.value = "Humanitarian Relief Org.";
  $("#login-drn").value = "";
  $("#login-password").value = "";
  $("#login-error").textContent = "";
  $("#demo-accounts").innerHTML = DEMO_ACCOUNTS.filter(
    (a, i, arr) =>
      arr.findIndex((x) => x.drn === a.drn && a.org === "Humanitarian Relief Org.") === i ||
      a.org !== "Humanitarian Relief Org."
  )
    .filter((a) => a.org === "Humanitarian Relief Org.")
    .map(
      (a) =>
        `<li><strong>${a.title}</strong> — DRN <code>${a.drn}</code> / password <code>${a.password}</code> (${a.role})</li>`
    )
    .join("");
  setOnlineUi();
}

function navButtonsForRole(role) {
  const items = [
    { id: "dashboard", label: "Home", icon: homeIcon },
    { id: "register", label: "Register", icon: userPlusIcon },
    { id: "patients", label: "Patients", icon: usersIcon },
    { id: "queue", label: "Queue", icon: listIcon },
    { id: "consultation", label: "Consult", icon: stethoscopeIcon },
    { id: "sync", label: "Sync", icon: syncIcon },
    { id: "admin", label: "Admin", icon: shieldIcon },
  ];
  return items
    .filter((i) => canAccess(role, i.id))
    .map(
      (i) =>
        `<button type="button" class="nav-btn ${state.screen === i.id ? "active" : ""}" data-nav="${i.id}" title="${i.label}" aria-label="${i.label}">${i.icon}</button>`
    )
    .join("");
}

function renderAppShell() {
  showView("app");
  const s = state.session;
  $("#sidebar-nav").innerHTML =
    navButtonsForRole(s.role) +
    `<div class="sidebar-spacer"></div>
     <button type="button" class="nav-btn" data-nav="logout" title="Logout" aria-label="Logout">${logoutIcon}</button>`;
  $("#topbar-user").textContent = `${s.displayName} (DRN: ${s.drn}) · ${s.role}`;
  setOnlineUi();
  navigate(state.screen, { replace: true });
}

function navigate(screen, { replace = false } = {}) {
  if (screen === "logout") {
    logout();
    return;
  }
  if (!canAccess(state.session.role, screen)) {
    toast("You do not have access to that screen.", "error");
    screen = "dashboard";
  }
  state.screen = screen;
  $$("#sidebar-nav .nav-btn").forEach((b) => {
    b.classList.toggle("active", b.dataset.nav === screen);
  });
  $$(".screen").forEach((el) => el.classList.add("hidden"));
  $(`#screen-${screen}`)?.classList.remove("hidden");
  const titles = {
    dashboard: "Mobile Clinic System",
    register: "Patient Registration",
    patients: "Patient Records",
    queue: "Waiting Queue",
    consultation: "Doctor Consultation",
    sync: "Sync Status",
    admin: "Administration",
  };
  $("#topbar-title").textContent = titles[screen] || "Mobile Clinic System";
  if (screen === "dashboard") renderDashboard();
  if (screen === "register") renderRegister();
  if (screen === "patients") renderPatients();
  if (screen === "queue") renderQueue();
  if (screen === "consultation") renderConsultation();
  if (screen === "sync") renderSync();
  if (screen === "admin") renderAdmin();
  if (!replace) window.scrollTo(0, 0);
}

async function logout() {
  state.session = null;
  await persist();
  renderLogin();
  toast("Signed out.", "info");
}

function renderDashboard() {
  const patients = state.data.patients;
  const pending = state.data.syncQueue.length;
  const waiting = state.data.queue.filter((q) => q.status === "waiting").length;
  const encounters = state.data.consultations.length;
  $("#dash-stats").innerHTML = `
    <div class="stat"><div class="label">Patients</div><div class="value">${patients.length}</div></div>
    <div class="stat"><div class="label">Consultations</div><div class="value">${encounters}</div></div>
    <div class="stat"><div class="label">Waiting queue</div><div class="value">${waiting}</div></div>
    <div class="stat"><div class="label">Pending sync</div><div class="value">${pending}</div></div>
  `;
  const role = state.session.role;
  const actions = [];
  if (canAccess(role, "register"))
    actions.push(`<button class="btn btn-primary" data-go="register">Register patient</button>`);
  if (canAccess(role, "queue"))
    actions.push(`<button class="btn btn-secondary" data-go="queue">Open queue</button>`);
  if (canAccess(role, "consultation"))
    actions.push(`<button class="btn btn-accent" data-go="consultation">Start consultation</button>`);
  if (canAccess(role, "sync"))
    actions.push(`<button class="btn btn-secondary" data-go="sync">View sync status</button>`);
  $("#dash-actions").innerHTML = actions.join("");
  $("#dash-role-note").textContent =
    role === "registration"
      ? "Registration role: create patients and manage the waiting queue. Clinical notes are restricted."
      : role === "doctor"
        ? "Doctor role: review patient history and record consultations. Registration is restricted."
        : "Admin role: full prototype access including all records and sync oversight.";
}

function emptyDraft() {
  return {
    fullName: "",
    nationalId: "",
    dob: "",
    address: "",
    phone: "",
  };
}

function renderRegister() {
  if (!state.draftPatient) state.draftPatient = emptyDraft();
  const step = state.regStep;
  $$(".step").forEach((el) => {
    const n = Number(el.dataset.step);
    el.classList.toggle("active", n === step);
    el.classList.toggle("done", n < step);
  });
  $$(".reg-pane").forEach((p) => p.classList.add("hidden"));
  $(`#reg-pane-${step}`)?.classList.remove("hidden");

  if (step === 1) {
    const d = state.draftPatient;
    $("#reg-fullname").value = d.fullName;
    $("#reg-nationalId").value = d.nationalId;
    $("#reg-dob").value = d.dob;
    $("#reg-address").value = d.address;
    $("#reg-phone").value = d.phone;
    clearRegErrors();
  }
  if (step === 2) {
    const d = state.draftPatient;
    $("#reg-review").innerHTML = `
      <div class="summary-list">
        <div class="summary-row"><span>Full name</span><span>${esc(d.fullName)}</span></div>
        <div class="summary-row"><span>Passport / National ID</span><span>${esc(d.nationalId || "(none provided)")}</span></div>
        <div class="summary-row"><span>Date of birth</span><span>${formatDate(d.dob)} (age ${ageFromDob(d.dob)})</span></div>
        <div class="summary-row"><span>Address / Location</span><span>${esc(d.address)}</span></div>
        <div class="summary-row"><span>Phone</span><span>${esc(d.phone || "—")}</span></div>
        <div class="summary-row"><span>Organization</span><span>${esc(state.session.org)}</span></div>
      </div>
      <p style="margin-top:1rem;color:var(--muted);font-size:0.85rem;">Confirm details, then save. Care is not denied if ID documents are missing.</p>
    `;
  }
  if (step === 3) {
    const p = state.data.patients.find((x) => x.id === state.selectedPatientId);
    $("#reg-summary").innerHTML = p
      ? `<div class="summary-list">
          <div class="summary-row"><span>System patient ID</span><span>${esc(p.id)}</span></div>
          <div class="summary-row"><span>Name</span><span>${esc(p.fullName)}</span></div>
          <div class="summary-row"><span>Sync status</span><span class="pill ${p.syncStatus === "synced" ? "pill-synced" : "pill-pending"}">${p.syncStatus}</span></div>
        </div>
        <div class="form-actions">
          <button type="button" class="btn btn-secondary" id="reg-another">Register another</button>
          <button type="button" class="btn btn-accent" id="reg-to-queue">Add to waiting queue →</button>
        </div>`
      : `<p>No patient saved.</p>`;
  }
}

function clearRegErrors() {
  ["fullname", "nationalId", "dob", "address"].forEach((f) => {
    $(`#err-${f}`).textContent = "";
  });
}

function validateRegistration() {
  clearRegErrors();
  const d = {
    fullName: $("#reg-fullname").value.trim(),
    nationalId: $("#reg-nationalId").value.trim(),
    dob: $("#reg-dob").value,
    address: $("#reg-address").value.trim(),
    phone: $("#reg-phone").value.trim(),
  };
  let ok = true;
  if (!d.fullName) {
    $("#err-fullname").textContent = "Full name is required.";
    ok = false;
  }
  if (!d.nationalId) {
    $("#err-nationalId").textContent =
      "Passport / National ID is required for this prototype form. (Production: optional — never deny care.)";
    ok = false;
  }
  if (!d.dob) {
    $("#err-dob").textContent = "Date of birth is required.";
    ok = false;
  } else if (new Date(d.dob) > new Date()) {
    $("#err-dob").textContent = "Date of birth cannot be in the future.";
    ok = false;
  }
  if (!d.address) {
    $("#err-address").textContent = "Address / location is required.";
    ok = false;
  }
  if (ok) state.draftPatient = d;
  return ok;
}

async function savePatient() {
  const d = state.draftPatient;
  const id = `PAT-${state.data.nextPatientSeq++}`;
  const online = onlineStatus();
  const patient = {
    id,
    fullName: d.fullName,
    nationalId: d.nationalId,
    dob: d.dob,
    address: d.address,
    phone: d.phone,
    org: state.session.org,
    createdAt: new Date().toISOString(),
    createdBy: state.session.drn,
    syncStatus: online ? "synced" : "pending",
  };
  state.data.patients.unshift(patient);
  if (!online) {
    markPending(patient);
    enqueueChange(state.data, {
      entity: "patient",
      entityId: id,
      action: "create",
    });
  }
  state.selectedPatientId = id;
  state.regStep = 3;
  await persist();
  renderRegister();
  toast(online ? "Patient saved and synced (simulated)." : "Patient saved offline — queued for sync.", "success");
}

function renderPatients() {
  const q = ($("#patient-search")?.value || "").trim().toLowerCase();
  let rows = state.data.patients;
  if (state.session.role !== "admin") {
    rows = rows.filter((p) => p.org === state.session.org);
  }
  if (q) {
    rows = rows.filter(
      (p) =>
        p.fullName.toLowerCase().includes(q) ||
        p.id.toLowerCase().includes(q) ||
        (p.nationalId || "").toLowerCase().includes(q) ||
        (p.address || "").toLowerCase().includes(q)
    );
  }
  const detailId = state.selectedPatientId;
  const detail = state.data.patients.find((p) => p.id === detailId);

  $("#patients-table").innerHTML = rows.length
    ? `<div class="table-wrap"><table class="data">
      <thead><tr>
        <th>Patient ID</th><th>Name</th><th>Age</th><th>ID (masked)</th><th>Location</th><th>Sync</th><th></th>
      </tr></thead>
      <tbody>
        ${rows
          .map(
            (p) => `<tr>
            <td>${esc(p.id)}</td>
            <td>${esc(p.fullName)}</td>
            <td>${ageFromDob(p.dob)}</td>
            <td>${esc(maskNationalId(p.nationalId))}</td>
            <td>${esc(p.address)}</td>
            <td><span class="pill ${p.syncStatus === "synced" ? "pill-synced" : "pill-pending"}">${p.syncStatus}</span></td>
            <td><button type="button" class="btn btn-secondary" data-view-patient="${p.id}" style="padding:0.35rem 0.6rem;font-size:0.8rem;">View</button></td>
          </tr>`
          )
          .join("")}
      </tbody></table></div>`
    : `<div class="empty-state">No patients match your search.</div>`;

  if (detail) {
    const enc = state.data.consultations
      .filter((c) => c.patientId === detail.id)
      .sort((a, b) => b.visitDate.localeCompare(a.visitDate));
    const showClinical = state.session.role !== "registration";
    $("#patient-detail").classList.remove("hidden");
    $("#patient-detail").innerHTML = `
      <h2>${esc(detail.fullName)}</h2>
      <div class="summary-list">
        <div class="summary-row"><span>System ID</span><span>${esc(detail.id)}</span></div>
        <div class="summary-row"><span>Passport / National ID</span><span>${esc(detail.nationalId || "(none)")}</span></div>
        <div class="summary-row"><span>Date of birth</span><span>${formatDate(detail.dob)}</span></div>
        <div class="summary-row"><span>Address</span><span>${esc(detail.address)}</span></div>
        <div class="summary-row"><span>Phone</span><span>${esc(detail.phone || "—")}</span></div>
        <div class="summary-row"><span>Organization</span><span>${esc(detail.org)}</span></div>
      </div>
      <h2 style="margin-top:1.25rem;">Consultations</h2>
      ${
        !showClinical
          ? `<p style="color:var(--muted);font-size:0.9rem;">Clinical notes are restricted for the registration role.</p>`
          : enc.length
            ? `<div class="table-wrap"><table class="data"><thead><tr><th>Date</th><th>Temp</th><th>Diagnosis</th><th>Medication</th><th>Notes</th></tr></thead>
              <tbody>${enc
                .map(
                  (c) => `<tr>
                  <td>${formatDate(c.visitDate)}</td>
                  <td>${c.temperature ?? "—"}</td>
                  <td>${esc(c.diagnoses.join(", "))}${c.otherDiagnosis ? " — " + esc(c.otherDiagnosis) : ""}</td>
                  <td>${esc(c.medication)}</td>
                  <td>${esc(c.clinicalNotes)}</td>
                </tr>`
                )
                .join("")}</tbody></table></div>`
            : `<p style="color:var(--muted);">No consultations yet.</p>`
      }
      <div class="form-actions">
        ${
          canAccess(state.session.role, "consultation")
            ? `<button type="button" class="btn btn-accent" data-consult-patient="${detail.id}">Open consultation</button>`
            : ""
        }
        ${
          canAccess(state.session.role, "queue")
            ? `<button type="button" class="btn btn-secondary" data-queue-patient="${detail.id}">Add to queue</button>`
            : ""
        }
      </div>`;
  } else {
    $("#patient-detail").classList.add("hidden");
    $("#patient-detail").innerHTML = "";
  }
}

function renderQueue() {
  const items = state.data.queue
    .filter((q) => q.status === "waiting")
    .map((q) => {
      const p = state.data.patients.find((x) => x.id === q.patientId);
      return { ...q, patient: p };
    })
    .filter((x) => x.patient);

  $("#queue-list").innerHTML = items.length
    ? items
        .map(
          (q) => `<div class="queue-item">
          <div>
            <div class="name">${esc(q.patient.fullName)}</div>
            <div class="meta">${esc(q.patient.id)} · ${esc(maskNationalId(q.patient.nationalId))} · queued ${formatDate(q.queuedAt)}</div>
          </div>
          <div style="display:flex;gap:0.45rem;flex-wrap:wrap;">
            ${
              canAccess(state.session.role, "consultation")
                ? `<button type="button" class="btn btn-accent" data-consult-patient="${q.patient.id}" style="padding:0.45rem 0.8rem;">Consult</button>`
                : ""
            }
            <button type="button" class="btn btn-secondary" data-dequeue="${q.patientId}" style="padding:0.45rem 0.8rem;">Remove</button>
          </div>
        </div>`
        )
        .join("")
    : `<div class="empty-state">Queue is empty.</div>`;
}

function emptyConsultForm(patientId) {
  const today = new Date().toISOString().slice(0, 10);
  return {
    patientId,
    visitDate: today,
    temperature: "",
    medication: "Paracetamol 500mg",
    diagnoses: [],
    otherDiagnosis: "",
    clinicalNotes: "",
  };
}

function renderConsultation() {
  const patients = state.data.patients.filter(
    (p) => state.session.role === "admin" || p.org === state.session.org
  );
  if (!state.selectedPatientId && patients[0]) {
    state.selectedPatientId = patients[0].id;
  }
  if (!state.consultForm || state.consultForm.patientId !== state.selectedPatientId) {
    state.consultForm = emptyConsultForm(state.selectedPatientId);
  }
  const f = state.consultForm;
  const patient = patients.find((p) => p.id === f.patientId);

  $("#consult-patient-select").innerHTML = patients
    .map(
      (p) =>
        `<option value="${p.id}" ${p.id === f.patientId ? "selected" : ""}>${esc(p.fullName)} (${p.id})</option>`
    )
    .join("");

  $("#consult-meta").innerHTML = patient
    ? `<div class="patient-meta">
        <div><strong>Patient:</strong> ${esc(patient.fullName)}</div>
        <div><strong>Visit Date:</strong> <input type="date" id="consult-date" value="${f.visitDate}" style="padding:0.25rem 0.4rem;border:1px solid var(--border);border-radius:6px;"></div>
        <div><strong>ID:</strong> ${esc(maskNationalId(patient.nationalId))}</div>
      </div>`
    : `<p class="empty-state">Select a patient to begin.</p>`;

  $("#consult-temp").value = f.temperature;
  $("#consult-med").innerHTML = MEDICATIONS.map(
    (m) => `<option value="${m}" ${m === f.medication ? "selected" : ""}>${m}</option>`
  ).join("");

  $("#consult-dx").innerHTML = DIAGNOSES.map(
    (d) => `<label class="check-item">
      <input type="checkbox" value="${d}" ${f.diagnoses.includes(d) ? "checked" : ""} data-dx>
      ${d}
      ${
        d === "Other"
          ? `<input type="text" id="consult-other-dx" placeholder="Specify…" value="${esc(f.otherDiagnosis)}" style="flex:1;margin-left:0.35rem;padding:0.25rem 0.45rem;border:1px solid var(--border);border-radius:6px;">`
          : ""
      }
    </label>`
  ).join("");

  $("#consult-notes").value = f.clinicalNotes;
}

function readConsultForm() {
  const diagnoses = $$("[data-dx]:checked").map((el) => el.value);
  state.consultForm = {
    patientId: $("#consult-patient-select").value,
    visitDate: $("#consult-date")?.value || state.consultForm.visitDate,
    temperature: $("#consult-temp").value,
    medication: $("#consult-med").value,
    diagnoses,
    otherDiagnosis: $("#consult-other-dx")?.value.trim() || "",
    clinicalNotes: $("#consult-notes").value.trim(),
  };
  return state.consultForm;
}

async function saveConsultation() {
  const f = readConsultForm();
  if (!f.patientId) {
    toast("Select a patient.", "error");
    return;
  }
  if (!f.visitDate) {
    toast("Visit date is required.", "error");
    return;
  }
  const temp = f.temperature === "" ? null : Number(f.temperature);
  if (f.temperature !== "" && (Number.isNaN(temp) || temp < 30 || temp > 45)) {
    toast("Temperature must be between 30 and 45 °C.", "error");
    return;
  }
  if (!f.diagnoses.length) {
    toast("Select at least one diagnosis.", "error");
    return;
  }
  if (f.diagnoses.includes("Other") && !f.otherDiagnosis) {
    toast("Please specify the Other diagnosis.", "error");
    return;
  }

  const online = onlineStatus();
  const id = `ENC-${state.data.nextEncounterSeq++}`;
  const record = {
    id,
    patientId: f.patientId,
    visitDate: f.visitDate,
    temperature: temp,
    medication: f.medication,
    diagnoses: f.diagnoses,
    otherDiagnosis: f.otherDiagnosis,
    clinicalNotes: f.clinicalNotes,
    clinicianDrn: state.session.drn,
    clinicianName: state.session.displayName,
    org: state.session.org,
    createdAt: new Date().toISOString(),
    syncStatus: online ? "synced" : "pending",
  };
  if (!online) {
    markPending(record);
    enqueueChange(state.data, {
      entity: "consultation",
      entityId: id,
      action: "create",
    });
  }
  state.data.consultations.unshift(record);
  // remove from queue
  state.data.queue = state.data.queue.filter(
    (q) => !(q.patientId === f.patientId && q.status === "waiting")
  );
  await persist();
  toast(online ? "Consultation saved (simulated sync)." : "Consultation saved offline — queued for sync.", "success");
}

function clearConsultation() {
  state.consultForm = emptyConsultForm(state.selectedPatientId);
  renderConsultation();
}

function nextPatient() {
  const waiting = state.data.queue.find((q) => q.status === "waiting");
  if (waiting) {
    state.selectedPatientId = waiting.patientId;
    state.consultForm = emptyConsultForm(waiting.patientId);
    renderConsultation();
    toast("Loaded next queued patient.", "info");
  } else {
    toast("No more patients in the queue.", "info");
  }
}

function renderSync() {
  const on = onlineStatus();
  const pending = state.data.syncQueue;
  $("#sync-stats").innerHTML = `
    <div class="stat"><div class="label">Connectivity</div><div class="value" style="font-size:1.15rem;">${on ? "Online" : "Offline"}</div></div>
    <div class="stat"><div class="label">Pending queue</div><div class="value">${pending.length}</div></div>
    <div class="stat"><div class="label">Last sync</div><div class="value" style="font-size:1rem;">${state.data.lastSyncAt ? formatDate(state.data.lastSyncAt) + " " + new Date(state.data.lastSyncAt).toLocaleTimeString() : "Never"}</div></div>
    <div class="stat"><div class="label">Browser online</div><div class="value" style="font-size:1.15rem;">${isBrowserOnline() ? "Yes" : "No"}</div></div>
  `;
  $("#sync-queue-table").innerHTML = pending.length
    ? `<div class="table-wrap"><table class="data"><thead><tr><th>Queue ID</th><th>Entity</th><th>Record</th><th>Action</th><th>Queued at</th></tr></thead>
      <tbody>${pending
        .map(
          (i) => `<tr>
          <td>${esc(i.id)}</td>
          <td>${esc(i.entity)}</td>
          <td>${esc(i.entityId)}</td>
          <td>${esc(i.action)}</td>
          <td>${esc(new Date(i.queuedAt).toLocaleString())}</td>
        </tr>`
        )
        .join("")}</tbody></table></div>`
    : `<div class="empty-state">Sync queue is empty — all local changes are marked synced.</div>`;
  setOnlineUi();
}

function renderAdmin() {
  const allPatients = state.data.patients;
  const allEnc = state.data.consultations;
  $("#admin-overview").innerHTML = `
    <div class="sync-panel">
      <div class="stat"><div class="label">All patient records</div><div class="value">${allPatients.length}</div></div>
      <div class="stat"><div class="label">All consultations</div><div class="value">${allEnc.length}</div></div>
      <div class="stat"><div class="label">Pending sync items</div><div class="value">${state.data.syncQueue.length}</div></div>
      <div class="stat"><div class="label">Demo users</div><div class="value">${state.users.length}</div></div>
    </div>
    <div class="table-wrap"><table class="data">
      <thead><tr><th>Patient</th><th>Org</th><th>National ID</th><th>Visits</th><th>Sync</th></tr></thead>
      <tbody>
        ${allPatients
          .map((p) => {
            const visits = allEnc.filter((c) => c.patientId === p.id).length;
            return `<tr>
              <td>${esc(p.fullName)} <span style="color:var(--muted);font-size:0.8rem;">${esc(p.id)}</span></td>
              <td>${esc(p.org)}</td>
              <td>${esc(p.nationalId || "—")}</td>
              <td>${visits}</td>
              <td><span class="pill ${p.syncStatus === "synced" ? "pill-synced" : "pill-pending"}">${p.syncStatus}</span></td>
            </tr>`;
          })
          .join("")}
      </tbody>
    </table></div>
  `;
}

async function addToQueue(patientId) {
  const exists = state.data.queue.some(
    (q) => q.patientId === patientId && q.status === "waiting"
  );
  if (exists) {
    toast("Patient is already in the queue.", "info");
    return;
  }
  state.data.queue.push({
    patientId,
    queuedAt: new Date().toISOString(),
    status: "waiting",
  });
  await persist();
  toast("Added to waiting queue.", "success");
  if (state.screen === "queue") renderQueue();
}

async function runSync() {
  const result = await simulateSync(state.data);
  await persist();
  if (!result.ok) {
    toast("Cannot sync while offline. Toggle connectivity or wait for network.", "error");
  } else {
    toast(
      result.synced
        ? `Simulated sync complete — ${result.synced} item(s) uploaded.`
        : "Nothing to sync.",
      "success"
    );
  }
  setOnlineUi();
  if (state.screen === "sync") renderSync();
  if (state.screen === "admin") renderAdmin();
  if (state.screen === "dashboard") renderDashboard();
}

function esc(str) {
  return String(str ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/* Icons */
const homeIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 10.5 12 3l9 7.5V21a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1v-10.5z"/></svg>`;
const userPlusIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M19 8v6M22 11h-6"/></svg>`;
const usersIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`;
const listIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/></svg>`;
const stethoscopeIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 4v6a6 6 0 0 0 12 0V4"/><path d="M6 4H4a2 2 0 0 0-2 2v1a3 3 0 0 0 3 3"/><path d="M18 4h2a2 2 0 0 1 2 2v1a3 3 0 0 1-3 3"/><circle cx="20" cy="18" r="2"/><path d="M18 18h-1a5 5 0 0 1-5-5"/></svg>`;
const syncIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 0 0-15.5-6.4L3 8"/><path d="M3 3v5h5"/><path d="M3 12a9 9 0 0 0 15.5 6.4L21 16"/><path d="M21 21v-5h-5"/></svg>`;
const shieldIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3 4 6v6c0 5 3.5 8.5 8 9 4.5-.5 8-4 8-9V6l-8-3z"/></svg>`;
const logoutIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></svg>`;

function bindEvents() {
  $("#login-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const org = $("#login-org").value;
    const drn = $("#login-drn").value.trim();
    const password = $("#login-password").value;
    $("#login-error").textContent = "";
    if (!drn || !password) {
      $("#login-error").textContent = "DRN and password are required.";
      return;
    }
    const user = await authenticate(state.users, { org, drn, password });
    if (!user) {
      $("#login-error").textContent = "Invalid organization, DRN, or password.";
      return;
    }
    state.session = user;
    state.screen = "dashboard";
    state.regStep = 1;
    state.draftPatient = null;
    await persist();
    renderAppShell();
    toast(`Welcome, ${user.displayName}.`, "success");
  });

  $("#toggle-password").addEventListener("click", () => {
    const input = $("#login-password");
    const show = input.type === "password";
    input.type = show ? "text" : "password";
    $("#toggle-password").setAttribute("aria-label", show ? "Hide password" : "Show password");
  });

  $("#login-force-offline").addEventListener("change", async (e) => {
    if (!state.data) return;
    state.data.forceOffline = e.target.checked;
    await persist();
    setOnlineUi();
  });

  document.body.addEventListener("click", async (e) => {
    const nav = e.target.closest("[data-nav]");
    if (nav) {
      navigate(nav.dataset.nav);
      return;
    }
    const go = e.target.closest("[data-go]");
    if (go) {
      navigate(go.dataset.go);
      return;
    }
    const viewP = e.target.closest("[data-view-patient]");
    if (viewP) {
      state.selectedPatientId = viewP.dataset.viewPatient;
      renderPatients();
      return;
    }
    const consultP = e.target.closest("[data-consult-patient]");
    if (consultP) {
      state.selectedPatientId = consultP.dataset.consultPatient;
      state.consultForm = emptyConsultForm(state.selectedPatientId);
      navigate("consultation");
      return;
    }
    const queueP = e.target.closest("[data-queue-patient]");
    if (queueP) {
      await addToQueue(queueP.dataset.queuePatient);
      return;
    }
    const deq = e.target.closest("[data-dequeue]");
    if (deq) {
      state.data.queue = state.data.queue.filter(
        (q) => !(q.patientId === deq.dataset.dequeue && q.status === "waiting")
      );
      await persist();
      renderQueue();
      toast("Removed from queue.", "info");
      return;
    }
    if (e.target.id === "reg-another") {
      state.regStep = 1;
      state.draftPatient = emptyDraft();
      state.selectedPatientId = null;
      renderRegister();
      return;
    }
    if (e.target.id === "reg-to-queue") {
      if (state.selectedPatientId) await addToQueue(state.selectedPatientId);
      navigate("queue");
    }
  });

  $("#reg-next").addEventListener("click", () => {
    if (!validateRegistration()) return;
    state.regStep = 2;
    renderRegister();
  });

  $("#reg-save").addEventListener("click", () => {
    savePatient();
  });

  $("#reg-back").addEventListener("click", () => {
    if (state.regStep > 1) {
      state.regStep -= 1;
      renderRegister();
    }
  });

  $("#reg-cancel").addEventListener("click", () => {
    state.regStep = 1;
    state.draftPatient = emptyDraft();
    navigate("dashboard");
  });

  $("#patient-search").addEventListener("input", () => renderPatients());

  $("#consult-patient-select").addEventListener("change", (e) => {
    state.selectedPatientId = e.target.value;
    state.consultForm = emptyConsultForm(e.target.value);
    renderConsultation();
  });

  $("#consult-save").addEventListener("click", () => saveConsultation());
  $("#consult-clear").addEventListener("click", () => clearConsultation());
  $("#consult-next").addEventListener("click", () => nextPatient());

  $("#force-offline").addEventListener("change", async (e) => {
    state.data.forceOffline = e.target.checked;
    await persist();
    setOnlineUi();
    if (state.screen === "sync") renderSync();
    if (onlineStatus() && state.data.syncQueue.length) {
      toast("Back online — syncing pending changes…", "info");
      await runSync();
    }
  });

  $("#btn-sync-now").addEventListener("click", () => runSync());

  $("#btn-reset-demo").addEventListener("click", async () => {
    if (!confirm("Reset all local prototype data to seed defaults? You will stay signed in.")) return;
    const session = state.session;
    state.data = createSeedData();
    state.data.forceOffline = false;
    state.session = session;
    state.regStep = 1;
    state.draftPatient = null;
    state.selectedPatientId = null;
    await persist();
    toast("Demo data reset.", "success");
    navigate(state.screen);
  });

  window.addEventListener("online", async () => {
    setOnlineUi();
    if (state.data && onlineStatus() && state.data.syncQueue.length) {
      toast("Connection restored — syncing…", "info");
      await runSync();
    }
  });
  window.addEventListener("offline", () => {
    setOnlineUi();
    toast("Browser reports offline.", "info");
  });
}

async function boot() {
  await initData();
  bindEvents();
  if (state.session) {
    renderAppShell();
  } else {
    renderLogin();
  }
  // sync login offline toggle with state
  const fo = $("#login-force-offline");
  if (fo && state.data) fo.checked = !!state.data.forceOffline;
  setOnlineUi();
}

boot();
