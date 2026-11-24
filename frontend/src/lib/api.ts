import { ApiError } from "./errors";
import type {
  ApiErrorBody,
  Comment,
  CreateTicketInput,
  PageResponse,
  Ticket,
  TicketStatus,
  UpdateTicketInput,
} from "./types";

const DEFAULT_BASE = "http://localhost:8080";

function apiBase(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") || DEFAULT_BASE;
}

async function parseError(response: Response): Promise<ApiError> {
  let body: ApiErrorBody | null = null;
  try {
    body = (await response.json()) as ApiErrorBody;
  } catch {
    body = null;
  }
  const fallback =
    response.status === 404
      ? "Ticket not found"
      : response.status === 409
        ? "Invalid status transition"
        : response.status === 422
          ? "Validation failed"
          : response.status === 400
            ? "Malformed request"
            : `Request failed (${response.status})`;
  return new ApiError(response.status, body, fallback);
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${apiBase()}${path}`, {
      ...init,
      headers: {
        Accept: "application/json",
        ...(init?.body ? { "Content-Type": "application/json" } : {}),
        ...init?.headers,
      },
    });
  } catch (error) {
    throw error instanceof TypeError
      ? error
      : new TypeError("Unable to reach the server. Check that the backend is running.");
  }

  if (!response.ok) {
    throw await parseError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function listTickets(params: {
  page?: number;
  size?: number;
  status?: TicketStatus | "";
  q?: string;
}): Promise<PageResponse<Ticket>> {
  const query = new URLSearchParams();
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 20));
  if (params.status) {
    query.set("status", params.status);
  }
  if (params.q && params.q.trim()) {
    query.set("q", params.q.trim());
  }
  return request(`/api/v1/tickets?${query.toString()}`);
}

export function getTicket(id: string): Promise<Ticket> {
  return request(`/api/v1/tickets/${id}`);
}

export function createTicket(input: CreateTicketInput): Promise<Ticket> {
  const body: Record<string, unknown> = {
    title: input.title,
    description: input.description,
    priority: input.priority,
  };
  if (input.assignee != null && input.assignee !== "") {
    body.assignee = input.assignee;
  }
  return request("/api/v1/tickets", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function updateTicket(id: string, input: UpdateTicketInput): Promise<Ticket> {
  return request(`/api/v1/tickets/${id}`, {
    method: "PATCH",
    body: JSON.stringify(input),
  });
}

export function changeTicketStatus(id: string, status: TicketStatus): Promise<Ticket> {
  return request(`/api/v1/tickets/${id}/status`, {
    method: "POST",
    body: JSON.stringify({ status }),
  });
}

export function addComment(
  id: string,
  content: string,
  author?: string,
): Promise<Comment> {
  const body: Record<string, unknown> = { content };
  if (author != null && author !== "") {
    body.author = author;
  }
  return request(`/api/v1/tickets/${id}/comments`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}
