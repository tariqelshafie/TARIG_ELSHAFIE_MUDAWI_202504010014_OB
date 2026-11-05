/**
 * Offline-first storage layer using IndexedDB with localStorage fallback.
 * PLACEHOLDER: Replace with encrypted on-device store + server API in production.
 */
const DB_NAME = "darfur_mhrs_v1";
const DB_VERSION = 1;
const STORE = "kv";

function openDb() {
  return new Promise((resolve, reject) => {
    if (!window.indexedDB) {
      reject(new Error("IndexedDB unavailable"));
      return;
    }
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains(STORE)) {
        db.createObjectStore(STORE);
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function idbGet(key) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readonly");
    const req = tx.objectStore(STORE).get(key);
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

async function idbSet(key, value) {
  const db = await openDb();
  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE, "readwrite");
    tx.objectStore(STORE).put(value, key);
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

const lsPrefix = "darfur_mhrs_";

async function getItem(key) {
  try {
    const v = await idbGet(key);
    if (v !== undefined) return v;
  } catch (_) {
    /* fall through */
  }
  const raw = localStorage.getItem(lsPrefix + key);
  return raw ? JSON.parse(raw) : undefined;
}

async function setItem(key, value) {
  try {
    await idbSet(key, value);
  } catch (_) {
    /* IndexedDB may fail in private mode */
  }
  localStorage.setItem(lsPrefix + key, JSON.stringify(value));
}

export const Storage = {
  async loadState() {
    const state = await getItem("state");
    return state || null;
  },
  async saveState(state) {
    await setItem("state", state);
  },
  async clearAll() {
    try {
      const db = await openDb();
      await new Promise((resolve, reject) => {
        const tx = db.transaction(STORE, "readwrite");
        tx.objectStore(STORE).clear();
        tx.oncomplete = () => resolve();
        tx.onerror = () => reject(tx.error);
      });
    } catch (_) {
      /* ignore */
    }
    Object.keys(localStorage)
      .filter((k) => k.startsWith(lsPrefix))
      .forEach((k) => localStorage.removeItem(k));
  },
};
