import {
  pgTable,
  serial,
  text,
  varchar,
  timestamp,
  boolean,
  integer,
  jsonb,
  unique,
  customType,
} from "drizzle-orm/pg-core";

const pgVector = customType<{ data: number[] }>({
  dataType() {
    return "vector(768)";
  },
});

export const technologies = pgTable("technologies", {
  id: serial("id").primaryKey(),
  name: varchar("name", { length: 255 }).unique().notNull(),
  slug: varchar("slug", { length: 100 }).unique().notNull(),
});

export const trackedRepositories = pgTable(
  "tracked_repositories",
  {
    id: serial("id").primaryKey(),
    owner: varchar("owner", { length: 255 }).notNull(),
    name: varchar("name", { length: 255 }).notNull(),
    isActive: boolean("is_active").default(true),
    techId: integer("tech_id").references(() => technologies.id),
    createdAt: timestamp("created_at").defaultNow(),
    lastSyncedAt: timestamp("last_synced_at", { withTimezone: true }),
  },
  (t) => ({
    unq: unique().on(t.owner, t.name),
  }),
);

export const artifacts = pgTable(
  "artifacts",
  {
    id: serial("id").primaryKey(),
    repoId: integer("repo_id").references(() => trackedRepositories.id),
    techId: integer("tech_id").references(() => technologies.id),
    source: varchar("source", { length: 50 }).default("github"),
    externalId: varchar("external_id", { length: 255 }).notNull(),
    type: varchar("type", { length: 50 }).notNull(),
    title: text("title"),
    body: text("body"),
    url: text("url"),
    author: varchar("author", { length: 255 }),
    state: varchar("state", { length: 50 }),
    sourceCreatedAt: timestamp("source_created_at", { withTimezone: true }),
    sourceUpdatedAt: timestamp("source_updated_at", { withTimezone: true }),
    processedAt: timestamp("processed_at").defaultNow(),
  },
  (t) => ({
    unq: unique().on(t.source, t.type, t.repoId, t.externalId),
  }),
);

export const artifactPayloads = pgTable("artifact_payloads", {
  artifactId: integer("artifact_id")
    .primaryKey()
    .references(() => artifacts.id, { onDelete: "cascade" }),
  rawPayload: jsonb("raw_payload"),
});

export const artifactIntelligence = pgTable("artifact_intelligence", {
  artifactId: integer("artifact_id")
    .primaryKey()
    .references(() => artifacts.id, { onDelete: "cascade" }),
  changeType: varchar("change_type", { length: 50 }),
  riskLevel: varchar("risk_level", { length: 50 }),
  confidence: varchar("confidence", { length: 20 }),
  embeddingModel: varchar("embedding_model", { length: 50 }),
  summarizerModel: varchar("summarizer_model", { length: 50 }),
  aiSummary: jsonb("ai_summary"),
  embedding: pgVector("embedding"),
  processingStatus: varchar("processing_status", { length: 20 }).default(
    "PENDING",
  ),
  enrichedAt: timestamp("enriched_at", { withTimezone: true }).defaultNow(),
});
