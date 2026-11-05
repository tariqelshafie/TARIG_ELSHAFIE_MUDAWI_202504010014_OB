# Darfur Mobile Health Record System — Browser Prototype

Offline-first, role-based patient registration and doctor consultation UI for humanitarian mobile clinics. **No real backend, Ministry of Health connection, or production authentication.**

Inspired by the project concept proposal and UI mockup (teal + orange branding, medical-cross heart logo).

## How to run

ES modules require a local HTTP server (opening `index.html` via `file://` may block scripts).

```bash
cd darfur-mobile-health
python3 -m http.server 8080
```

Then open [http://localhost:8080](http://localhost:8080).

Any static server works (`npx serve`, VS Code Live Server, etc.).

## Demo accounts

Use organization **Humanitarian Relief Org.** unless noted.

| Role | DRN | Password | Access |
|------|-----|----------|--------|
| Registration officer | `REG001` | `register123` | Dashboard, Register, Patients, Queue |
| Doctor | `10234` | `doctor123` | Dashboard, Patients, Queue, Consultation |
| Admin | `ADMIN01` | `admin123` | All screens + Sync + Admin |

Passwords are stored as SHA-256 hashes in the seeded user directory (never plain text). The password field is masked; use the eye toggle to reveal.

## What is mocked / placeholder

| Area | Behavior in this prototype |
|------|----------------------------|
| Auth | Local demo accounts only; no MFA, IdP, or session server |
| Storage | IndexedDB (+ localStorage fallback); not encrypted at rest |
| Sync | Offline changes enqueue; “Sync now” / reconnect **simulates** upload and clears the queue |
| MoH / FHIR | Clearly labeled **PLACEHOLDER** stubs on Dashboard, Consultation, Sync, Admin |
| Clinical safety | Forms document care only — no decision support or prescribing engine |
| National ID | Masked (`••••` + last 4) in lists/queue; full value only on record detail |

Use **Simulate offline** on the login screen or Sync page to force the offline path without disconnecting the browser.

## Features covered

1. **Login** — org dropdown, DRN, password, online/offline status line, motto, demo credentials  
2. **Patient registration** — multi-step form with required fields and inline validation  
3. **Doctor consultation** — vitals, medication list, diagnosis checkboxes, clinical notes, save to patient record  
4. **Roles** — Registration / Doctor / Admin see only permitted screens  
5. **Offline-first** — pending sync queue; auto-simulated sync when connectivity returns  
6. **Seed data** — sample patients, consultations, and waiting queue on first load  
7. **Admin** — all records + sync status; reset demo data  

## Folder layout

```
darfur-mobile-health/
  index.html
  README.md
  assets/logo.svg
  css/styles.css
  js/
    app.js        # UI + flows
    auth.js       # Demo users, roles, password hashing
    storage.js    # IndexedDB / localStorage
    sync.js       # Offline queue + simulated sync
    seed.js       # Sample patients & consultations
```

## Disclaimer

Prototype for stakeholder demonstration and workflow review. Not approved for real patient data, clinical deployment, or Ministry of Health production use.
