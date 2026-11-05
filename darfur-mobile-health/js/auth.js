/**
 * Prototype authentication — seeded demo accounts only.
 * PLACEHOLDER: Real auth would use MoH/org IdP, MFA, short-lived sessions.
 * Passwords are stored as SHA-256 hashes (not plain text).
 */

async function sha256(text) {
  const data = new TextEncoder().encode(text);
  const buf = await crypto.subtle.digest("SHA-256", data);
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

/** Demo credentials shown on login screen */
export const DEMO_ACCOUNTS = [
  {
    org: "Humanitarian Relief Org.",
    drn: "REG001",
    password: "register123",
    role: "registration",
    displayName: "Sara Hassan",
    title: "Registration Officer",
  },
  {
    org: "Humanitarian Relief Org.",
    drn: "10234",
    password: "doctor123",
    role: "doctor",
    displayName: "Dr. Ahmed",
    title: "Clinician",
  },
  {
    org: "Humanitarian Relief Org.",
    drn: "ADMIN01",
    password: "admin123",
    role: "admin",
    displayName: "Fatima Ali",
    title: "Organization Admin",
  },
  {
    org: "Red Crescent Mobile Clinic",
    drn: "10234",
    password: "doctor123",
    role: "doctor",
    displayName: "Dr. Ahmed",
    title: "Clinician",
  },
];

export const ORGANIZATIONS = [
  "Humanitarian Relief Org.",
  "Red Crescent Mobile Clinic",
  "Darfur Community Health Network",
];

export async function buildUserDirectory() {
  const users = [];
  for (const a of DEMO_ACCOUNTS) {
    users.push({
      org: a.org,
      drn: a.drn,
      role: a.role,
      displayName: a.displayName,
      title: a.title,
      passwordHash: await sha256(a.password),
    });
  }
  return users;
}

export async function authenticate(users, { org, drn, password }) {
  const hash = await sha256(password);
  const user = users.find(
    (u) =>
      u.org === org &&
      u.drn.toLowerCase() === String(drn).trim().toLowerCase() &&
      u.passwordHash === hash
  );
  if (!user) return null;
  return {
    org: user.org,
    drn: user.drn,
    role: user.role,
    displayName: user.displayName,
    title: user.title,
    loggedInAt: new Date().toISOString(),
  };
}

/** Role → permitted screen ids */
export const ROLE_SCREENS = {
  registration: ["dashboard", "register", "patients", "queue"],
  doctor: ["dashboard", "patients", "queue", "consultation"],
  admin: ["dashboard", "register", "patients", "queue", "consultation", "admin", "sync"],
};

export function canAccess(role, screen) {
  return (ROLE_SCREENS[role] || []).includes(screen);
}
