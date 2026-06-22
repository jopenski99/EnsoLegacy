"use client";

import { useRef, useState } from "react";

export default function EmailForm() {
  const inputRef = useRef<HTMLInputElement>(null);
  const [status, setStatus] = useState<"idle" | "success" | "error">("idle");

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const email = inputRef.current?.value?.trim();
    if (!email) return;

    setStatus("idle");

    try {
      const res = await fetch("/api/waitlist", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      if (!res.ok) throw new Error("Failed");

      setStatus("success");
      if (inputRef.current) inputRef.current.value = "";
    } catch {
      setStatus("error");
    }
  }

  if (status === "success") {
    return (
      <p className="text-primary font-label-md text-label-md">
        You&rsquo;re on the list. We&rsquo;ll be in touch.
      </p>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-sm w-full max-w-md">
      <input
        ref={inputRef}
        type="email"
        required
        placeholder="your@email.com"
        className="flex-1 px-base py-base border border-outline-variant rounded-lg bg-surface-container-low text-on-surface font-body-md text-body-md placeholder:text-outline focus:outline-none focus:ring-2 focus:ring-primary"
      />
      <button
        type="submit"
        className="px-lg py-base bg-primary text-on-primary font-label-md text-label-md rounded-lg hover:brightness-110 active:scale-95 transition-all whitespace-nowrap"
      >
        Join Waitlist
      </button>
      {status === "error" && (
        <p className="text-error text-sm mt-xs">Something went wrong. Try again?</p>
      )}
    </form>
  );
}
