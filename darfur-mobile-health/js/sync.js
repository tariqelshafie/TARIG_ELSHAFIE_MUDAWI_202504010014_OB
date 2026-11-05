/**
 * Simulated sync engine for offline-first prototype.
 * PLACEHOLDER: Real sync would push to MoH/org FHIR server with conflict resolution.
 */

export function isBrowserOnline() {
  return navigator.onLine !== false;
}

export function effectiveOnline(state) {
  return isBrowserOnline() && !state.forceOffline;
}

export function enqueueChange(state, change) {
  const item = {
    id: `SYNC-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
    ...change,
    queuedAt: new Date().toISOString(),
  };
  state.syncQueue.push(item);
  return item;
}

/**
 * Simulate uploading the pending queue when connectivity returns.
 * Returns { synced: number }.
 */
export async function simulateSync(state, { delayMs = 900 } = {}) {
  if (!effectiveOnline(state)) {
    return { ok: false, reason: "offline", synced: 0 };
  }
  await new Promise((r) => setTimeout(r, delayMs));

  const pending = [...state.syncQueue];
  for (const item of pending) {
    if (item.entity === "patient") {
      const p = state.patients.find((x) => x.id === item.entityId);
      if (p) p.syncStatus = "synced";
    }
    if (item.entity === "consultation") {
      const c = state.consultations.find((x) => x.id === item.entityId);
      if (c) c.syncStatus = "synced";
    }
  }
  state.syncQueue = [];
  state.lastSyncAt = new Date().toISOString();
  return { ok: true, synced: pending.length };
}

export function markPending(record) {
  record.syncStatus = "pending";
  return record;
}
