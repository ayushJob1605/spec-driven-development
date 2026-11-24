import type { TicketStatus } from "./types";

const TRANSITIONS: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
  CANCELLED: [],
};

export function isTerminal(status: TicketStatus): boolean {
  return status === "CLOSED" || status === "CANCELLED";
}

export function allowedTransitions(status: TicketStatus): TicketStatus[] {
  return TRANSITIONS[status] ?? [];
}

export const ALL_STATUSES: TicketStatus[] = [
  "OPEN",
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
  "CANCELLED",
];

export const PRIORITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const;
