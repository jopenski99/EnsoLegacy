export default function HomePage() {
  return (
    <main
      style={{
        minHeight: "100dvh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        padding: "2rem",
        gap: "0.75rem",
      }}
    >
      <h1 style={{ fontSize: "2.5rem", fontWeight: 600, margin: 0 }}>
        Ensō Legacy
      </h1>
      <p style={{ fontSize: "1.125rem", opacity: 0.7, margin: 0 }}>
        Your collection. Its history. Forever.
      </p>
      {/* TODO (Phase 1): email waitlist capture form */}
    </main>
  );
}
