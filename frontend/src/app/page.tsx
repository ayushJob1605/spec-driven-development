"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useState } from "react";
import { listTickets } from "@/lib/api";
import { ApiError, formatApiError } from "@/lib/errors";
import { ALL_STATUSES } from "@/lib/status";
import type { PageResponse, Ticket, TicketStatus } from "@/lib/types";
import { ErrorBanner } from "@/components/ErrorBanner";

export default function TicketListPage() {
  const [q, setQ] = useState("");
  const [status, setStatus] = useState<TicketStatus | "">("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const [networkMessage, setNetworkMessage] = useState<string | null>(null);
  const [data, setData] = useState<PageResponse<Ticket> | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    setNetworkMessage(null);
    try {
      const result = await listTickets({
        page,
        size: 20,
        status,
        q,
      });
      setData(result);
    } catch (err) {
      setData(null);
      if (err instanceof ApiError) {
        setError(err);
      } else {
        setNetworkMessage(formatApiError(err));
      }
    } finally {
      setLoading(false);
    }
  }, [page, q, status]);

  useEffect(() => {
    void load();
  }, [load]);

  function onSearch(event: FormEvent) {
    event.preventDefault();
    if (page !== 0) {
      setPage(0);
    } else {
      void load();
    }
  }

  return (
    <div>
      <h1>Tickets</h1>

      <form className="toolbar panel" onSubmit={onSearch}>
        <div className="field" style={{ marginBottom: 0, minWidth: "220px", flex: 1 }}>
          <label htmlFor="q">Search</label>
          <input
            id="q"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Keyword in title or description"
          />
        </div>
        <div className="field" style={{ marginBottom: 0, minWidth: "180px" }}>
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as TicketStatus | "");
              setPage(0);
            }}
          >
            <option value="">All</option>
            {ALL_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
        <button type="submit">Apply</button>
      </form>

      <ErrorBanner error={error} />
      {networkMessage ? (
        <div className="error-banner" role="alert">
          <p>{networkMessage}</p>
        </div>
      ) : null}

      <div className="panel">
        {loading ? <p className="muted">Loading tickets…</p> : null}
        {!loading && data && data.content.length === 0 ? (
          <p className="muted">No tickets found.</p>
        ) : null}
        {!loading && data && data.content.length > 0 ? (
          <>
            <table className="ticket-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Status</th>
                  <th>Priority</th>
                  <th>Assignee</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((ticket) => (
                  <tr key={ticket.id}>
                    <td>
                      <Link href={`/tickets/${ticket.id}`}>{ticket.title}</Link>
                    </td>
                    <td>
                      <span className="badge">{ticket.status}</span>
                    </td>
                    <td>{ticket.priority}</td>
                    <td>{ticket.assignee || "—"}</td>
                    <td className="muted">{new Date(ticket.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="pagination">
              <button
                type="button"
                className="secondary"
                disabled={page <= 0 || loading}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Previous
              </button>
              <span className="muted">
                Page {data.page + 1} of {Math.max(data.totalPages, 1)} ({data.totalElements}{" "}
                total)
              </span>
              <button
                type="button"
                className="secondary"
                disabled={loading || data.page + 1 >= data.totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}
