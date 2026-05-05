import { ArtifactIntelligence } from "./artifact-intelligence";

export interface Artifact {
  id: string;
  title: string;
  body: string;
  url: string;
  author: string;
  createdAt: string;
  state?: string;

  repoName: string;
  repoId: string;

  techName: string;
  techId: string;

  intelligence?: ArtifactIntelligence;

  similarityScore?: number;
}
