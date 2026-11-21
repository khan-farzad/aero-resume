"use client";

import { LogOut, UserRound } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useSyncExternalStore } from "react";

const authChangeEvent = "aero-auth-change";

function isActiveToken(token: string | null) {
  if (!token) return false;

  try {
    const [, encodedPayload] = token.split(".");
    if (!encodedPayload) return false;
    const base64Payload = encodedPayload.replace(/-/g, "+").replace(/_/g, "/");
    const decodedPayload = window.atob(
      base64Payload.padEnd(Math.ceil(base64Payload.length / 4) * 4, "="),
    );
    const payload: unknown = JSON.parse(decodedPayload);
    if (typeof payload !== "object" || payload === null) return false;
    const expiration = (payload as Record<string, unknown>).exp;
    return typeof expiration === "number" && expiration * 1000 > Date.now();
  } catch {
    return false;
  }
}

function subscribeToAuth(onStoreChange: () => void) {
  const handleVisibilityChange = () => {
    if (document.visibilityState === "visible") onStoreChange();
  };
  window.addEventListener("storage", onStoreChange);
  window.addEventListener(authChangeEvent, onStoreChange);
  window.addEventListener("focus", onStoreChange);
  document.addEventListener("visibilitychange", handleVisibilityChange);
  const expiryCheck = window.setInterval(onStoreChange, 30_000);
  return () => {
    window.removeEventListener("storage", onStoreChange);
    window.removeEventListener(authChangeEvent, onStoreChange);
    window.removeEventListener("focus", onStoreChange);
    document.removeEventListener("visibilitychange", handleVisibilityChange);
    window.clearInterval(expiryCheck);
  };
}

function getAuthSnapshot() {
  return isActiveToken(window.localStorage.getItem("token"));
}

export default function AccountActions() {
  const router = useRouter();
  const isAuthenticated = useSyncExternalStore(
    subscribeToAuth,
    getAuthSnapshot,
    () => false,
  );

  useEffect(() => {
    const token = window.localStorage.getItem("token");
    if (token && !isActiveToken(token)) {
      window.localStorage.removeItem("token");
      window.dispatchEvent(new Event(authChangeEvent));
    }
  }, []);

  const signOut = () => {
    window.localStorage.removeItem("token");
    for (const key of Object.keys(window.sessionStorage)) {
      if (key.startsWith("aero-resume-draft-")) {
        window.sessionStorage.removeItem(key);
      }
    }
    window.dispatchEvent(new Event(authChangeEvent));
    router.push("/");
    router.refresh();
  };

  if (!isAuthenticated) {
    return (
      <div className="account-actions">
        <Link className="account-signin" href="/auth">
          Sign in
        </Link>
      </div>
    );
  }

  return (
    <div className="account-actions">
      <Link className="account-profile" href="/profile">
        <span className="account-profile-icon"><UserRound size={15} /></span>
        <span>Profile</span>
      </Link>
      <button className="account-logout" type="button" onClick={signOut}>
        <LogOut size={15} />
        <span>Log out</span>
      </button>
    </div>
  );
}
