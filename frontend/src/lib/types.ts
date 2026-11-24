export type Priority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED"
  | "CANCELLED";

export interface FieldErrorItem {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  timestamp?: string;
  status: number;
  code: string;
  message: string;
  path?: string;
  fieldErrors?: FieldErrorItem[];
}

export interface Comment {
  id: string;
  ticketId: string;
  content: string;
  author: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Ticket {
  id: string;
  title: string;
  description: string;
  priority: Priority;
  status: TicketStatus;
  assignee: string | null;
  createdAt: string;
  updatedAt: string;
  comments: Comment[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateTicketInput {
  title: string;
  description: string;
  priority: Priority;
  assignee?: string | null;
}

export interface UpdateTicketInput {
  title?: string;
  description?: string;
  priority?: Priority;
  assignee?: string | null;
}
