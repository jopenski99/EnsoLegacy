/**
 * Shared domain types for Ensō Legacy.
 *
 * These are the single source of truth for entities that cross the web and
 * mobile boundary (e.g. anything serialized through Supabase or rendered on a
 * public QR tree-history page). Keep them framework-agnostic — no React, no
 * Android, no DB-driver imports.
 */

/** Life stage of a tree. Stage transitions are tracked events. */
export type BonsaiStage = "juvenile" | "in_training" | "mature";

/**
 * Care status of a tree, surfaced on the Health view.
 * `needs_care` = wants attention soon; `critical` = at real risk.
 */
export type HealthStatus = "healthy" | "needs_care" | "critical";

/** A tree in a user's collection. */
export interface Bonsai {
  id: string;
  ownerId: string;
  name: string;
  species: string;
  stage: BonsaiStage;
  health: HealthStatus;
  /** Whether the owner has set up care reminders for this tree yet. */
  careScheduleSet: boolean;
  /** Year the tree entered the collection or training began. */
  acquiredYear?: number;
  /** Public slug used by the QR tree-history page, if shared. */
  publicSlug?: string;
  createdAt: string; // ISO-8601
  updatedAt: string; // ISO-8601
}

/** A documented event in a tree's life. Supports up to a 10-photo flow. */
export interface Milestone {
  id: string;
  bonsaiId: string;
  title: string;
  notes?: string;
  occurredAt: string; // ISO-8601
  photoUrls: string[]; // max MAX_MILESTONE_PHOTOS
  createdAt: string;
}

/** A recorded move between life stages. */
export interface StageTransition {
  id: string;
  bonsaiId: string;
  fromStage: BonsaiStage;
  toStage: BonsaiStage;
  occurredAt: string; // ISO-8601
  notes?: string;
}

export type PassportEdition = "basic" | "legacy";

/** Exportable history of a single tree. Basic PDF is free; Legacy is paid. */
export interface TreePassport {
  bonsaiId: string;
  edition: PassportEdition;
  includesQr: boolean;
  includesProvenance: boolean;
  generatedAt: string; // ISO-8601
}

/** Future hardware reading. Shape mirrors the planned `sensor_reading` table. */
export interface SensorReading {
  id: string;
  bonsaiId: string;
  readingDate: number; // epoch millis
  soilMoisture: number; // 0–100 %
  soilTemp: number; // °C
  ambientTemp: number; // °C
  lightLux?: number;
  soilPH?: number;
  sensorId: string; // device MAC or UUID
}

export type SupportTier = "free" | "donor" | "mid" | "legacy";

/** Quiet, private profile mark earned by supporting the project. */
export interface GratitudeLeaf {
  userId: string;
  shoots: number; // grows with repeated support
  isFounderMark: boolean; // permanent early-adopter distinction
}
