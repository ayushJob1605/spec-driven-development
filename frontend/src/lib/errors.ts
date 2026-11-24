import type { ApiErrorBody, FieldErrorItem } from "./types";

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly fieldErrors: FieldErrorItem[];
  readonly body: ApiErrorBody | null;

  constructor(status: number, body: ApiErrorBody | null, fallbackMessage: string) {
    super(body?.message || fallbackMessage);
    this.name = "ApiError";
    this.status = status;
    this.code = body?.code || "UNKNOWN";
    this.fieldErrors = body?.fieldErrors ?? [];
    this.body = body;
  }
}

export function fieldErrorMap(error: ApiError): Record<string, string> {
  const map: Record<string, string> = {};
  for (const item of error.fieldErrors) {
    if (item.field && item.message) {
      map[item.field] = item.message;
    }
  }
  return map;
}

export function formatApiError(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof TypeError) {
    return "Unable to reach the server. Check that the backend is running.";
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Something went wrong.";
}
