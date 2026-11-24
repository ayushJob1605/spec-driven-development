import type { ApiError } from "@/lib/errors";

type Props = {
  error: ApiError | null;
  fieldErrors?: Record<string, string>;
};

export function ErrorBanner({ error, fieldErrors }: Props) {
  if (!error) {
    return null;
  }

  const extras = Object.entries(fieldErrors ?? {});

  return (
    <div className="error-banner" role="alert">
      <strong>
        {error.status === 404
          ? "Not found"
          : error.status === 409
            ? "Invalid transition"
            : error.status === 422
              ? "Validation error"
              : error.status === 400
                ? "Bad request"
                : "Error"}
      </strong>
      <p>{error.message}</p>
      {extras.length > 0 ? (
        <ul>
          {extras.map(([field, message]) => (
            <li key={field}>
              <code>{field}</code>: {message}
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
