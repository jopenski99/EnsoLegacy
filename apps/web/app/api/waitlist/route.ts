import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const { email } = (await request.json()) as { email?: string };

  if (!email || typeof email !== "string") {
    return NextResponse.json({ error: "Email is required" }, { status: 400 });
  }

  console.log(`[waitlist] ${email}`);

  return NextResponse.json({ ok: true });
}
