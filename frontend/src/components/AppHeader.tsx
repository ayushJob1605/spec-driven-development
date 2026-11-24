import Link from "next/link";

export function AppHeader() {
  return (
    <header className="app-header">
      <div className="app-header-inner">
        <Link href="/" className="brand">
          Support Tickets
        </Link>
        <nav>
          <Link href="/">Tickets</Link>
          <Link href="/tickets/new" className="button">
            New ticket
          </Link>
        </nav>
      </div>
    </header>
  );
}
