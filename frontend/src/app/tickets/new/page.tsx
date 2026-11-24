"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { createTicket } from "@/lib/api";
import { ApiError, fieldErrorMap, formatApiError } from "@/lib/errors";
import { PRIORITIES } from "@/lib/status";
import type { Priority } from "@/lib/types";
import { ErrorBanner } from "@/components/ErrorBanner";

export default function CreateTicketPage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<Priority>("MEDIUM");
  const [assignee, setAssignee] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [networkMessage, setNetworkMessage] = useState<string | null>(null);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setFieldErrors({});
    setNetworkMessage(null);
    try {
      const ticket = await createTicket({
        title,
        description,
        priority,
        assignee: assignee.trim() === "" ? undefined : assignee.trim(),
      });
      router.push(`/tickets/${ticket.id}`);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err);
        setFieldErrors(fieldErrorMap(err));
      } else {
        setNetworkMessage(formatApiError(err));
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1>Create ticket</h1>
      <ErrorBanner error={error} fieldErrors={fieldErrors} />
      {networkMessage ? (
        <div className="error-banner" role="alert">
          <p>{networkMessage}</p>
        </div>
      ) : null}
      <form className="panel" onSubmit={onSubmit}>
        <div className="field">
          <label htmlFor="title">Title</label>
          <input
            id="title"
            value={title}
            maxLength={200}
            required
            onChange={(e) => setTitle(e.target.value)}
          />
          {fieldErrors.title ? <span className="hint">{fieldErrors.title}</span> : null}
        </div>
        <div className="field">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={description}
            maxLength={5000}
            required
            onChange={(e) => setDescription(e.target.value)}
          />
          {fieldErrors.description ? (
            <span className="hint">{fieldErrors.description}</span>
          ) : null}
        </div>
        <div className="field">
          <label htmlFor="priority">Priority</label>
          <select
            id="priority"
            value={priority}
            onChange={(e) => setPriority(e.target.value as Priority)}
          >
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
          {fieldErrors.priority ? <span className="hint">{fieldErrors.priority}</span> : null}
        </div>
        <div className="field">
          <label htmlFor="assignee">Assignee (optional)</label>
          <input
            id="assignee"
            value={assignee}
            maxLength={100}
            onChange={(e) => setAssignee(e.target.value)}
          />
          {fieldErrors.assignee ? <span className="hint">{fieldErrors.assignee}</span> : null}
        </div>
        <div className="actions">
          <button type="submit" disabled={submitting}>
            {submitting ? "Creating…" : "Create ticket"}
          </button>
        </div>
      </form>
    </div>
  );
}
