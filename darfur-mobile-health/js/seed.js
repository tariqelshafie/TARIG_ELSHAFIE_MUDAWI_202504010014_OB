/**
 * Seed sample patients + consultations for a realistic first load.
 */

function daysAgo(n) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}

export const MEDICATIONS = [
  "Paracetamol 500mg",
  "Amoxicillin 250mg",
  "ORS sachets",
  "Artemether-Lumefantrine",
  "Iron + Folic Acid",
  "Vitamin A capsule",
  "Ibuprofen 400mg",
  "Salbutamol inhaler",
  "Metronidazole 400mg",
  "No medication prescribed",
];

export const DIAGNOSES = [
  "Malaria",
  "Respiratory Infection",
  "Diarrhoeal Disease",
  "Measles",
  "Anaemia",
  "Other",
];

export function createSeedData() {
  const patients = [
    {
      id: "PAT-1001",
      fullName: "Ahmed Mohamed",
      nationalId: "SD-P-88442119",
      dob: "1992-04-12",
      address: "Abu Shouk Camp, Sector B",
      phone: "+249 91 234 5678",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(14) + "T09:00:00.000Z",
      createdBy: "REG001",
      syncStatus: "synced",
    },
    {
      id: "PAT-1002",
      fullName: "Aisha Osman",
      nationalId: "SD-N-55219003",
      dob: "2018-09-03",
      address: "Zamzam Camp, Block 4",
      phone: "",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(7) + "T11:20:00.000Z",
      createdBy: "REG001",
      syncStatus: "synced",
    },
    {
      id: "PAT-1003",
      fullName: "Hassan Ibrahim",
      nationalId: "SD-P-22019877",
      dob: "1985-01-28",
      address: "El Fasher Town, Market Area",
      phone: "+249 92 111 4455",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(3) + "T08:45:00.000Z",
      createdBy: "REG001",
      syncStatus: "synced",
    },
    {
      id: "PAT-1004",
      fullName: "Mariam Yusuf",
      nationalId: "SD-N-77881234",
      dob: "1999-12-01",
      address: "Tawila IDP Settlement",
      phone: "+249 90 556 7788",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(1) + "T15:10:00.000Z",
      createdBy: "REG001",
      syncStatus: "synced",
    },
    {
      id: "PAT-1005",
      fullName: "Omar Khalid",
      nationalId: "",
      dob: "2015-06-20",
      address: "Nyala Host Community",
      phone: "",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(0) + "T07:30:00.000Z",
      createdBy: "REG001",
      syncStatus: "synced",
    },
  ];

  const consultations = [
    {
      id: "ENC-2001",
      patientId: "PAT-1001",
      visitDate: daysAgo(14),
      temperature: 36.8,
      medication: "Paracetamol 500mg",
      diagnoses: ["Malaria", "Anaemia", "Other"],
      otherDiagnosis: "Mild dehydration",
      clinicalNotes: "Patient shows mild symptoms. Prescribed medication.",
      clinicianDrn: "10234",
      clinicianName: "Dr. Ahmed",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(14) + "T10:15:00.000Z",
      syncStatus: "synced",
    },
    {
      id: "ENC-2002",
      patientId: "PAT-1002",
      visitDate: daysAgo(7),
      temperature: 38.4,
      medication: "Amoxicillin 250mg",
      diagnoses: ["Respiratory Infection"],
      otherDiagnosis: "",
      clinicalNotes: "Cough and fever for 3 days. No danger signs. Advise return if worsens.",
      clinicianDrn: "10234",
      clinicianName: "Dr. Ahmed",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(7) + "T12:00:00.000Z",
      syncStatus: "synced",
    },
    {
      id: "ENC-2003",
      patientId: "PAT-1003",
      visitDate: daysAgo(3),
      temperature: 37.1,
      medication: "Iron + Folic Acid",
      diagnoses: ["Anaemia"],
      otherDiagnosis: "",
      clinicalNotes: "Fatigue and pallor. Start iron supplementation. Follow up in 2 weeks.",
      clinicianDrn: "10234",
      clinicianName: "Dr. Ahmed",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(3) + "T09:30:00.000Z",
      syncStatus: "synced",
    },
    {
      id: "ENC-2004",
      patientId: "PAT-1004",
      visitDate: daysAgo(1),
      temperature: 39.0,
      medication: "ORS sachets",
      diagnoses: ["Diarrhoeal Disease"],
      otherDiagnosis: "",
      clinicalNotes: "Acute watery diarrhoea. Rehydration started. Monitor urine output.",
      clinicianDrn: "10234",
      clinicianName: "Dr. Ahmed",
      org: "Humanitarian Relief Org.",
      createdAt: daysAgo(1) + "T16:00:00.000Z",
      syncStatus: "synced",
    },
  ];

  const queue = [
    { patientId: "PAT-1005", queuedAt: new Date().toISOString(), status: "waiting" },
    { patientId: "PAT-1001", queuedAt: new Date(Date.now() - 20 * 60000).toISOString(), status: "waiting" },
  ];

  return {
    patients,
    consultations,
    queue,
    syncQueue: [],
    nextPatientSeq: 1006,
    nextEncounterSeq: 2005,
    forceOffline: false,
    lastSyncAt: daysAgo(0) + "T06:00:00.000Z",
  };
}
