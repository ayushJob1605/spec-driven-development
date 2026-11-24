"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import {
  addComment,
  changeTicketStatus,
  getTicket,
  updateTicket,
} from "@/lib/api";
import { ApiError, fieldErrorMap, formatApiError } from "@/lib/errors";
import { PRIORITIES, allowedTransitions, isTerminal } from "@/lib/status";
import type { Priority, Ticket, TicketStatus } from "@/lib/types";
import { ErrorBanner } from "@/components/ErrorBanner";

export default function TicketDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params.id;

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [networkMessage, setNetworkMessage] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<Priority>("MEDIUM");
  const [assignee, setAssignee] = useState("");
  const [saving, setSaving] = useState(false);
  const [statusBusy, setStatusBusy] = useState(false);

  const [commentContent, setCommentContent] = useState("");
  const [commentAuthor, setCommentAuthor] = useState("");
  const [commentBusy, setCommentBusy] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    setNetworkMessage(null);
    try {
      const data = await getTicket(id);
      setTicket(data);
      setTitle(data.title);
      setDescription(data.description);
      setPriority(data.priority);
      setAssignee(data.assignee ?? "");
    } catch (err) {
      setTicket(null);
      if (err instanceof ApiError) {
        setError(err);
      } else {
        setNetworkMessage(formatApiError(err));
      }
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  async function onSave(event: FormEvent) {
    event.preventDefault();
    if (!ticket || isTerminal(ticket.status)) {
      return;
    }
    setSaving(true);
    setError(null);
    setFieldErrors({});
    setNetworkMessage(null);
    try {
      const updated = await updateTicket(ticket.id, {
        title,
        description,
        priority,
        assignee: assignee.trim() === "" ? null : assignee.trim(),
      });
      setTicket(updated);
      setAssignee(updated.assignee ?? "");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err);
        setFieldErrors(fieldErrorMap(err));
      } else {
        setNetworkMessage(formatApiError(err));
      }
    } finally {
      setSaving(false);
    }
  }

  async function onTransition(next: TicketStatus) {
    if (!ticket) {
      return;
    }
    setStatusBusy(true);
    setError(null);
    setNetworkMessage(null);
    try {
      const updated = await changeTicketStatus(ticket.id, next);
      setTicket(updated);
      setTitle(updated.title);
      setDescription(updated.description);
      setPriority(updated.priority);
      setAssignee(updated.assignee ?? "");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err);
      } else {
        setNetworkMessage(formatApiError(err));
      }
    } finally {
      setStatusBusy(false);
    }
  }

  async function onAddComment(event: FormEvent) {
    event.preventDefault();
    if (!ticket || isTerminal(ticket.status)) {
      return;
    }
    setCommentBusy(true);
    setError(null);
    setFieldErrors({});
    setNetworkMessage(null);
    try {
      await addComment(
        ticket.id,
        commentContent,
        commentAuthor.trim() === "" ? undefined : commentAuthor.trim(),
      );
      setCommentContent("");
      setCommentAuthor("");
      await load();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err);
        setFieldErrors(fieldErrorMap(err));
      } else {
        setNetworkMessage(formatApiError(err));
      }
    } finally {
      setCommentBusy(false);
    }
  }

  if (loading) {
    return <p className="muted">Loading ticket…</p>;
  }

  if (!ticket) {
    return (
      <div>
        <ErrorBanner error={error} />
        {networkMessage ? (
          <div className="error-banner" role="alert">
            <p>{networkMessage}</p>
          </div>
        ) : null}
        <p>
          <Link href="/">Back to ticket list</Link>
        </p>
      </div>
    );
  }

  const terminal = isTerminal(ticket.status);
  const nextStatuses = allowedTransitions(ticket.status);

  return (
    <div className="stack">
      <div>
        <p>
          <Link href="/">← Back to list</Link>
        </p>
        <h1>{ticket.title}</h1>
        <div className="meta-row">
          <span>
            Status: <strong className="badge">{ticket.status}</strong>
          </span>
          <span>Priority: {ticket.priority}</span>
          <span>Assignee: {ticket.assignee || "Unassigned"}</span>
          <span>Created: {new Date(ticket.createdAt).toLocaleString()}</span>
        </div>
      </div>

      <ErrorBanner error={error} fieldErrors={fieldErrors} />
      {networkMessage ? (
        <div className="error-banner" role="alert">
          <p>{networkMessage}</p>
        </div>
      ) : null}

      {!terminal ? (
        <section className="panel">
          <h2>Edit ticket</h2>
          <form onSubmit={onSave}>
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
            </div>
            <div className="field">
              <label htmlFor="assignee">Assignee (empty to unassign)</label>
              <input
                id="assignee"
                value={assignee}
                maxLength={100}
                onChange={(e) => setAssignee(e.target.value)}
              />
            </div>
            <button type="submit" disabled={saving}>
              {saving ? "Saving…" : "Save changes"}
            </button>
          </form>
        </section>
      ) : (
        <section className="panel">
          <h2>Details</h2>
          <p className="muted">This ticket is {ticket.status} and is read-only.</p>
          <p>{ticket.description}</p>
        </section>
      )}

      {!terminal && nextStatuses.length > 0 ? (
        <section className="panel">
          <h2>Change status</h2>
          <div className="actions">
            {nextStatuses.map((status) => (
              <button
                key={status}
                type="button"
                className="secondary"
                disabled={statusBusy}
                onClick={() => void onTransition(status)}
              >
                Move to {status}
              </button>
            ))}
          </div>
        </section>
      ) : null}

      <section className="panel">
        <h2>Comments</h2>
        {ticket.comments.length === 0 ? (
          <p className="muted">No comments yet.</p>
        ) : (
          ticket.comments.map((comment) => (
            <div key={comment.id} className="comment">
              <p>{comment.content}</p>
              <p className="muted">
                {comment.author || "Anonymous"} ·{" "}
                {new Date(comment.createdAt).toLocaleString()}
              </p>
            </div>
          ))
        )}

        {!terminal ? (
          <form onSubmit={onAddComment} style={{ marginTop: "1rem" }}>
            <div className="field">
              <label htmlFor="content">Add comment</label>
              <textarea
                id="content"
                value={commentContent}
                maxLength={2000}
                required
                onChange={(e) => setCommentContent(e.target.value)}
              />
              {fieldErrors.content ? (
                <span className="hint">{fieldErrors.content}</span>
              ) : null}
            </div>
            <div className="field">
              <label htmlFor="author">Author (optional)</label>
              <input
                id="author"
                value={commentAuthor}
                maxLength={100}
                onChange={(e) => setCommentAuthor(e.target.value)}
              />
            </div>
            <button type="submit" disabled={commentBusy}>
              {commentBusy ? "Posting…" : "Add comment"}
            </button>
          </form>
        ) : null}
      </section>
    </div>
  );
}
