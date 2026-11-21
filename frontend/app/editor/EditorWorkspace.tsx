"use client";

import {
  ArrowDownToLine,
  Bot,
  Code2,
  FileText,
  MessageSquare,
  MoreHorizontal,
  Paperclip,
  Play,
  Send,
  Sparkles,
  UserRound,
  WandSparkles,
} from "lucide-react";
import Link from "next/link";
import AccountActions from "../_components/AccountActions";
import {
  FormEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useSyncExternalStore,
  useState,
} from "react";
import { API_BASE_URL, getApiErrorMessage } from "@/lib/api";
import PdfPreview from "./PdfPreview";

type ChatMessage = {
  role: "assistant" | "user";
  text: string;
};

type ResumeDraft = {
  latexString: string;
  resumeName: string;
  status?: "DRAFT" | "COMPILED" | "COMPLETED";
};

type ResumeStatus = "DRAFT" | "COMPILED" | "COMPLETED";

function isResumeStatus(value: unknown): value is ResumeStatus {
  return value === "DRAFT" || value === "COMPILED" || value === "COMPLETED";
}

export default function EditorWorkspace({
  templateId,
  resumeId,
}: {
  templateId?: string;
  resumeId: number;
}) {
  const draftStorageKey = `aero-resume-draft-${resumeId}`;
  const storedDraft = useSyncExternalStore(
    (onStoreChange) => {
      window.addEventListener("storage", onStoreChange);
      window.addEventListener("aero-resume-draft", onStoreChange);
      return () => {
        window.removeEventListener("storage", onStoreChange);
        window.removeEventListener("aero-resume-draft", onStoreChange);
      };
    },
    () => window.sessionStorage.getItem(draftStorageKey),
    () => undefined,
  );
  const draft = useMemo<ResumeDraft | null>(() => {
    if (!storedDraft) return null;
    try {
      const value: unknown = JSON.parse(storedDraft);
      if (typeof value !== "object" || value === null) return null;
      const record = value as Record<string, unknown>;
      if (
        typeof record.latexString !== "string" ||
        typeof record.resumeName !== "string"
      ) {
        return null;
      }
      return {
        latexString: record.latexString,
        resumeName: record.resumeName,
        status: isResumeStatus(record.status) ? record.status : undefined,
      };
    } catch {
      return null;
    }
  }, [storedDraft]);
  const [editedLatex, setEditedLatex] = useState<string | null>(null);
  const [editedResumeName, setEditedResumeName] = useState<string | null>(null);
  const [editedStatus, setEditedStatus] = useState<ResumeStatus | null>(null);
  const [isPreviewOutdated, setIsPreviewOutdated] = useState(false);
  const latex = editedLatex ?? draft?.latexString ?? "";
  const resumeName = editedResumeName ?? draft?.resumeName ?? "";
  const resumeStatus = editedStatus ?? draft?.status ?? "DRAFT";
  const isLoadingLatex = storedDraft === undefined;
  const isLatexLoaded = Boolean(draft?.latexString.trim());
  const sourceUnavailableError =
    storedDraft !== undefined && !isLatexLoaded
      ? "The draft source is not available or invalid in this browser tab. Return to the template gallery and start the project again."
      : "";
  const [pdfUrl, setPdfUrl] = useState("");
  const [isCompiling, setIsCompiling] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [compileError, setCompileError] = useState("");
  const displayError = compileError || sourceUnavailableError;
  const [resumeNameError, setResumeNameError] = useState("");
  const [chatInput, setChatInput] = useState("");
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    {
      role: "assistant",
      text: "Hi Alex! I’m your resume copilot. Ask me to improve a bullet, tailor your resume, or make your experience stand out.",
    },
  ]);
  const [chatNotice, setChatNotice] = useState("");
  const gutterRef = useRef<HTMLDivElement>(null);
  const resumeNameInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    return () => {
      if (pdfUrl) URL.revokeObjectURL(pdfUrl);
    };
  }, [pdfUrl]);

  const compileResume = useCallback(async (source: string, isFinalExport = false) => {
    const trimmedResumeName = resumeName.trim();
    if (!trimmedResumeName) {
      setResumeNameError(`Enter a resume name before ${isFinalExport ? "downloading" : "compiling"}.`);
      resumeNameInputRef.current?.focus();
      return;
    }
    const token = window.localStorage.getItem("token");
    if (!token) {
      setCompileError("Sign in first to compile your resume.");
      return;
    }

    if (isFinalExport) {
      setIsExporting(true);
    } else {
      setIsCompiling(true);
      setPdfUrl("");
    }
    setCompileError("");
    setResumeNameError("");

    try {
      const requestBody: Record<string, unknown> = {
        resumeName: trimmedResumeName,
        latexString: source,
        isFinalExport,
        resumeId,
      };
      if (templateId && /^\d+$/.test(templateId)) {
        requestBody.templateId = Number(templateId);
      }

      const response = await fetch(
        `${API_BASE_URL}/api/resume/compile`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestBody),
        },
      );

      if (!response.ok) {
        throw new Error(
          await getApiErrorMessage(response, `Compilation failed (${response.status}).`),
        );
      }

      const responseBlob = await response.blob();
      const header = new TextDecoder().decode(
        await responseBlob.slice(0, 5).arrayBuffer(),
      );
      if (!header.startsWith("%PDF-")) {
        const detail = await responseBlob.text();
        throw new Error(detail || "The server did not return a valid PDF.");
      }

      const pdf = new Blob([responseBlob], { type: "application/pdf" });
      setPdfUrl(URL.createObjectURL(pdf));
      setEditedStatus(isFinalExport ? "COMPLETED" : "COMPILED");
      setIsPreviewOutdated(false);
      if (isFinalExport) {
        const link = document.createElement("a");
        const filename = trimmedResumeName.replace(/[<>:"/\\|?*\u0000-\u001F]/g, "-");
        const downloadUrl = URL.createObjectURL(pdf);
        link.href = downloadUrl;
        link.download = `${filename || "resume"}.pdf`;
        link.click();
        window.setTimeout(() => URL.revokeObjectURL(downloadUrl), 1000);
      }
    } catch (error) {
      setCompileError(
        error instanceof Error
          ? error.message
          : "Unable to compile the resume. Please try again.",
      );
    } finally {
      setIsCompiling(false);
      setIsExporting(false);
    }
  }, [resumeId, resumeName, templateId]);

  const sendChatMessage = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const message = chatInput.trim();
    if (!message) return;

    setChatMessages((messages) => [...messages, { role: "user", text: message }]);
    setChatInput("");
    setChatNotice(
      "The AI service isn’t connected yet. Your message is ready for when it’s enabled.",
    );
  };

  const lineCount = latex.split("\n").length;

  return (
    <main className="workspace">
      <header className="topbar">
        <Link className="brand" href="/" aria-label="AeroResume home">
          <span className="brand-mark">
            <Sparkles size={17} strokeWidth={2.2} />
          </span>
          <span>Aero<span className="brand-light">Resume</span></span>
        </Link>
        <div className="topbar-divider" />
        <Link className="document-name editor-back-link" href="/templates">
          <FileText size={15} />
          <span>Back to templates</span>
        </Link>
        <label className="resume-name-field">
          <FileText size={15} aria-hidden="true" />
          <input
            ref={resumeNameInputRef}
            aria-label="Resume name"
            aria-invalid={Boolean(resumeNameError)}
            aria-describedby={resumeNameError ? "resume-name-error" : undefined}
            placeholder="Name your resume"
            value={resumeName}
            onChange={(event) => {
              setEditedResumeName(event.target.value);
              setEditedStatus("DRAFT");
              setIsPreviewOutdated(true);
              if (event.target.value.trim()) setResumeNameError("");
            }}
            maxLength={120}
          />
        </label>
        <div className="topbar-spacer" />
        <AccountActions />
        <div className="save-state">
          <span
            className={`status-dot ${
              resumeStatus === "COMPLETED"
                ? "status-dot-complete"
                : resumeStatus === "COMPILED"
                  ? "status-dot-compiled"
                  : "status-dot-draft"
            }`}
          />
          {isLoadingLatex
            ? "Loading resume"
            : resumeStatus === "COMPLETED"
              ? "Completed"
              : resumeStatus === "COMPILED"
                ? "Compiled"
                : "Draft in editor"}
        </div>
      </header>
      {resumeNameError && (
        <p className="resume-name-error" id="resume-name-error" role="alert">
          {resumeNameError}
        </p>
      )}

      <div className="panels">
        <section className="left-column" aria-label="Resume editor and AI assistant">
          <section className="editor-panel panel">
            <div className="panel-heading editor-heading">
              <div className="panel-title-wrap">
                <span className="section-icon code-icon"><Code2 size={16} /></span>
                <div>
                  <h1>LaTeX editor</h1>
                  <p>Edit your source and compile a live preview</p>
                </div>
              </div>
              <div className="editor-mode"><span /> Source</div>
            </div>

            <div className="editor-toolbar">
              <span className="file-tab"><FileText size={14} /> resume.tex</span>
              <span className="editor-language">LaTeX</span>
              <button className="icon-button" type="button" aria-label="More editor options">
                <MoreHorizontal size={18} />
              </button>
            </div>

            <div className="code-editor">
              <div className="line-gutter" ref={gutterRef} aria-hidden="true">
                {Array.from({ length: lineCount }, (_, index) => (
                  <div key={index}>{index + 1}</div>
                ))}
              </div>
              <textarea
                aria-label="LaTeX source editor"
                disabled={isLoadingLatex || !isLatexLoaded}
                autoCapitalize="off"
                autoComplete="off"
                autoCorrect="off"
                spellCheck={false}
                value={latex}
                onChange={(event) => {
                  setEditedLatex(event.target.value);
                  setEditedStatus("DRAFT");
                  setIsPreviewOutdated(true);
                }}
                onScroll={(event) => {
                  if (gutterRef.current) {
                    gutterRef.current.scrollTop = event.currentTarget.scrollTop;
                  }
                }}
              />
            </div>
            <div className="editor-footer">
              <span>
                <span className="status-dot" />
                {isLoadingLatex ? "Loading" : isLatexLoaded ? "Ready" : "Unavailable"}
              </span>
              <span>LaTeX&nbsp; · &nbsp;{lineCount} lines</span>
            </div>
          </section>

          <section className="chat-panel panel" aria-label="AI resume assistant">
            <div className="panel-heading chat-heading">
              <div className="panel-title-wrap">
                <span className="section-icon ai-icon"><WandSparkles size={16} /></span>
                <div>
                  <h2>AI resume copilot</h2>
                  <p>Make your next edit a great one</p>
                </div>
              </div>
              <span className="coming-soon"><span /> AI assistant</span>
            </div>

            <div className="chat-messages" aria-live="polite">
              {chatMessages.map((message, index) => (
                <div
                  className={`chat-message ${message.role}`}
                  key={`${message.role}-${index}`}
                >
                  <span className="message-avatar">
                    {message.role === "assistant" ? (
                      <Bot size={15} />
                    ) : (
                      <UserRound size={14} />
                    )}
                  </span>
                  <p>{message.text}</p>
                </div>
              ))}
              {chatNotice && <p className="chat-notice">{chatNotice}</p>}
            </div>

            <form className="chat-composer" onSubmit={sendChatMessage}>
              <input
                aria-label="Message the AI resume copilot"
                placeholder="Ask about your resume..."
                value={chatInput}
                onChange={(event) => {
                  setChatInput(event.target.value);
                  setChatNotice("");
                }}
              />
              <button className="attach-button" type="button" aria-label="Attach a file">
                <Paperclip size={17} />
              </button>
              <button
                className="send-button"
                type="submit"
                aria-label="Send message"
                disabled={!chatInput.trim()}
              >
                <Send size={15} />
              </button>
            </form>
            <div className="chat-hint"><MessageSquare size={12} /> AI suggestions will appear here</div>
          </section>
        </section>

        <section className="preview-panel panel" aria-label="Compiled resume preview">
          <div className="panel-heading preview-heading">
            <div className="panel-title-wrap">
              <span className="section-icon preview-icon"><FileText size={16} /></span>
              <div>
                <h2>Resume preview</h2>
                <p>
                  {pdfUrl
                    ? isPreviewOutdated
                      ? "Preview is out of date"
                      : "Compiled preview"
                    : "Compile to see your latest changes"}
                </p>
              </div>
            </div>
            <div className="preview-actions">
              <button
                className="download-button"
                type="button"
                onClick={() => void compileResume(latex, true)}
                disabled={isCompiling || isExporting || !isLatexLoaded}
              >
                <ArrowDownToLine size={15} />
                <span>{isExporting ? "Exporting..." : "Download"}</span>
              </button>
              <button
                className="compile-button"
                type="button"
                onClick={() => void compileResume(latex, false)}
                disabled={isCompiling || isExporting || !isLatexLoaded}
              >
                <Play size={14} fill="currentColor" />
                <span>{isCompiling ? "Compiling..." : "Compile"}</span>
              </button>
            </div>
          </div>
          <div className="preview-toolbar">
            <span className="preview-file">
              <FileText size={14} />
              {resumeName.trim() ? `${resumeName.trim()}.pdf` : "Resume preview"}
            </span>
            <div className="preview-toolbar-right">
              <span>{pdfUrl ? "PDF ready" : "Preview"}</span>
              <span className="toolbar-divider" />
              <span>100%</span>
              <button className="icon-button" type="button" aria-label="More preview options">
                <MoreHorizontal size={18} />
              </button>
            </div>
          </div>

          <div className="pdf-stage">
            {pdfUrl ? (
              <PdfPreview key={pdfUrl} pdfUrl={pdfUrl} />
            ) : (
              <div className="pdf-empty-state" role="status">
                {isLoadingLatex
                  ? "Loading the selected resume template…"
                  : isCompiling
                    ? "Compiling your resume…"
                    : displayError
                      ? "The PDF preview will appear here after compilation succeeds."
                      : "Your compiled PDF preview will appear here."}
              </div>
            )}
          </div>
          <div className="preview-footer">
            <span className={displayError ? "preview-error" : ""} role={displayError ? "alert" : undefined}>
              {displayError || "Your compiled resume will appear here."}
            </span>
            <span>{pdfUrl ? "PDF" : "Waiting for compile"}</span>
          </div>
        </section>
      </div>
    </main>
  );
}
