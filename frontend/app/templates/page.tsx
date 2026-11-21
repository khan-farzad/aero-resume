"use client";

import Image from "next/image";
import Link from "next/link";
import { ArrowLeft, ArrowRight, FileText, LayoutTemplate, LoaderCircle, Sparkles } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { API_BASE_URL, getApiErrorMessage } from "@/lib/api";
import AccountActions from "../_components/AccountActions";

type ResumeTemplate = {
  id: number;
  name: string;
  previewImageUrl?: string | null;
};

type ResumeSummary = {
  id: number;
  name: string;
  status?: ResumeStatus;
};

type ResumeStatus = "DRAFT" | "COMPILED" | "COMPLETED";

type ResumeDetails = ResumeSummary & {
  latexContent: string;
  templateId?: number;
};

function isResumeStatus(value: unknown): value is ResumeStatus {
  return value === "DRAFT" || value === "COMPILED" || value === "COMPLETED";
}

function isResumeSummary(value: unknown): value is ResumeSummary {
  if (typeof value !== "object" || value === null) return false;
  const resume = value as Record<string, unknown>;
  return (
    typeof resume.id === "number" &&
    Number.isSafeInteger(resume.id) &&
    resume.id > 0 &&
    typeof resume.name === "string" &&
    (resume.status === undefined || isResumeStatus(resume.status))
  );
}

function isResumeDetails(value: unknown): value is ResumeDetails {
  if (!isResumeSummary(value)) return false;
  const resume = value as ResumeSummary & Record<string, unknown>;
  return (
    typeof resume.latexContent === "string" &&
    Boolean(resume.latexContent.trim()) &&
    (resume.templateId === undefined ||
      (typeof resume.templateId === "number" && Number.isSafeInteger(resume.templateId)))
  );
}

function isResumeTemplate(value: unknown): value is ResumeTemplate {
  if (typeof value !== "object" || value === null) return false;
  const template = value as Record<string, unknown>;
  return (
    typeof template.id === "number" &&
    typeof template.name === "string" &&
    (template.previewImageUrl === undefined ||
      template.previewImageUrl === null ||
      typeof template.previewImageUrl === "string")
  );
}

function getPreviewUrl(path?: string | null) {
  if (!path) return null;
  try {
    return new URL(path, `${API_BASE_URL}/`).toString();
  } catch {
    return null;
  }
}

export default function TemplatesPage() {
  const router = useRouter();
  const [templates, setTemplates] = useState<ResumeTemplate[]>([]);
  const [resumes, setResumes] = useState<ResumeSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingResumes, setIsLoadingResumes] = useState(true);
  const [creatingTemplateId, setCreatingTemplateId] = useState<number | null>(null);
  const [openingResumeId, setOpeningResumeId] = useState<number | null>(null);
  const [needsSignIn, setNeedsSignIn] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [resumeListError, setResumeListError] = useState("");
  const [createError, setCreateError] = useState("");
  const [resumeName, setResumeName] = useState("");
  const [resumeNameError, setResumeNameError] = useState("");
  const resumeNameInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const controller = new AbortController();

    const loadTemplates = async () => {
      const token = window.localStorage.getItem("token");
      if (!token) {
        setNeedsSignIn(true);
        setIsLoading(false);
        return;
      }

      try {
        const response = await fetch(`${API_BASE_URL}/api/template/`, {
          headers: { Authorization: `Bearer ${token}` },
          signal: controller.signal,
        });
        if (!response.ok) {
          throw new Error(await getApiErrorMessage(response, `Could not load templates (${response.status}).`));
        }

        const data: unknown = await response.json();
        if (!Array.isArray(data)) {
          throw new Error("The template service returned an unexpected response.");
        }
        if (!data.every(isResumeTemplate)) {
          throw new Error("The template service returned invalid template details.");
        }
        setTemplates(data);
      } catch (loadError) {
        if (loadError instanceof DOMException && loadError.name === "AbortError") return;
        setLoadError(
          loadError instanceof Error ? loadError.message : "Unable to load templates.",
        );
      } finally {
        if (!controller.signal.aborted) setIsLoading(false);
      }
    };

    void loadTemplates();
    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const loadResumes = async () => {
      const token = window.localStorage.getItem("token");
      if (!token) {
        setIsLoadingResumes(false);
        return;
      }

      try {
        const response = await fetch(`${API_BASE_URL}/api/resume/`, {
          headers: { Authorization: `Bearer ${token}` },
          signal: controller.signal,
        });
        if (!response.ok) {
          throw new Error(await getApiErrorMessage(response, `Could not load your resumes (${response.status}).`));
        }
        const data: unknown = await response.json();
        if (!Array.isArray(data) || !data.every(isResumeSummary)) {
          throw new Error("The resume service returned invalid resume details.");
        }
        setResumes(data);
      } catch (error) {
        if (error instanceof DOMException && error.name === "AbortError") return;
        setResumeListError(error instanceof Error ? error.message : "Unable to load your saved resumes.");
      } finally {
        if (!controller.signal.aborted) setIsLoadingResumes(false);
      }
    };

    void loadResumes();
    return () => controller.abort();
  }, []);

  const openResume = async (resume: ResumeSummary) => {
    if (openingResumeId !== null || creatingTemplateId !== null) return;
    setOpeningResumeId(resume.id);
    setResumeListError("");

    try {
      const token = window.localStorage.getItem("token");
      if (!token) {
        setNeedsSignIn(true);
        return;
      }
      const response = await fetch(`${API_BASE_URL}/api/resume/${resume.id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) {
        throw new Error(await getApiErrorMessage(response, `Could not open this resume (${response.status}).`));
      }
      const payload: unknown = await response.json();
      if (!isResumeDetails(payload)) {
        throw new Error("The resume service did not return valid resume source data.");
      }

      window.sessionStorage.setItem(
        `aero-resume-draft-${payload.id}`,
        JSON.stringify({
          latexString: payload.latexContent,
          resumeName: payload.name,
          status: payload.status ?? "DRAFT",
        }),
      );
      const query = new URLSearchParams({ resumeId: String(payload.id) });
      if (payload.templateId !== undefined) query.set("templateId", String(payload.templateId));
      router.push(`/editor?${query}`);
    } catch (error) {
      setResumeListError(error instanceof Error ? error.message : "Unable to open this resume.");
    } finally {
      setOpeningResumeId(null);
    }
  };

  const startProject = async (template: ResumeTemplate) => {
    if (creatingTemplateId !== null || openingResumeId !== null) return;
    const trimmedResumeName = resumeName.trim();
    if (!trimmedResumeName) {
      setResumeNameError("Enter a name for your resume before choosing a template.");
      resumeNameInputRef.current?.focus();
      return;
    }

    setCreatingTemplateId(template.id);
    setCreateError("");
    setResumeNameError("");

    try {
      const token = window.localStorage.getItem("token");
      if (!token) {
        setNeedsSignIn(true);
        return;
      }

      const response = await fetch(`${API_BASE_URL}/api/resume/`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          templateId: template.id,
          resumeName: trimmedResumeName,
        }),
      });
      if (!response.ok) {
        throw new Error(
          await getApiErrorMessage(response, `Could not create a resume (${response.status}).`),
        );
      }

      const payload: unknown = await response.json();
      if (typeof payload !== "object" || payload === null) {
        throw new Error("The resume service returned an unexpected response.");
      }
      const createdResume = payload as Record<string, unknown>;
      const resumeId = createdResume.id;
      const latexString = createdResume.latexContent;
      const createdName = createdResume.name;
      if (
        typeof resumeId !== "number" ||
        !Number.isSafeInteger(resumeId) ||
        resumeId <= 0 ||
        typeof latexString !== "string" ||
        !latexString.trim() ||
        typeof createdName !== "string" ||
        !createdName.trim()
      ) {
        throw new Error("The resume service returned an invalid resume response.");
      }

      window.sessionStorage.setItem(
        `aero-resume-draft-${resumeId}`,
        JSON.stringify({
          latexString,
          resumeName: createdName,
          status: isResumeStatus(createdResume.status) ? createdResume.status : "DRAFT",
        }),
      );
      router.push(`/editor?templateId=${template.id}&resumeId=${resumeId}`);
    } catch (createError) {
      setCreateError(
        createError instanceof Error
          ? createError.message
          : "Unable to start the resume project. Please try again.",
      );
    } finally {
      setCreatingTemplateId(null);
    }
  };

  return (
    <main className="templates-page">
      <header className="landing-header templates-header">
        <Link className="brand" href="/" aria-label="AeroResume home">
          <span className="brand-mark"><Sparkles size={17} strokeWidth={2.2} /></span>
          <span>Aero<span className="brand-light">Resume</span></span>
        </Link>
        <Link className="templates-back" href="/"><ArrowLeft size={15} /> Home</Link>
        <AccountActions />
      </header>

      <section className="templates-content">
        <div className="templates-intro">
          <span className="landing-eyebrow"><LayoutTemplate size={14} /> TEMPLATE GALLERY</span>
          <h1>Choose your starting point</h1>
          <p>Name your resume, then select a template to open the editor.</p>
        </div>

        <section className="saved-resumes-section" aria-labelledby="saved-resumes-heading">
          <div className="saved-resumes-heading">
            <div>
              <span className="landing-eyebrow"><FileText size={14} /> YOUR WORK</span>
              <h2 id="saved-resumes-heading">Your resumes</h2>
              <p>Continue editing a saved resume or start a new project below.</p>
            </div>
          </div>
          {isLoadingResumes && <p className="template-state">Loading your resumes…</p>}
          {resumeListError && <p className="template-state template-error" role="alert">{resumeListError}</p>}
          {!isLoadingResumes && !resumeListError && resumes.length === 0 && (
            <p className="saved-resumes-empty">No saved resumes yet. Choose a template below to create your first one.</p>
          )}
          {resumes.length > 0 && (
            <div className="saved-resume-list">
              {resumes.map((resume) => (
                <button
                  className="saved-resume-card"
                  type="button"
                  key={resume.id}
                  onClick={() => void openResume(resume)}
                  disabled={openingResumeId !== null || creatingTemplateId !== null}
                >
                  <span className="saved-resume-icon"><FileText size={18} /></span>
                  <span className="saved-resume-copy">
                    <strong>{resume.name || "Untitled resume"}</strong>
                    <small>Resume #{resume.id}{resume.status ? ` · ${resume.status.toLowerCase()}` : ""}</small>
                  </span>
                  <span className="saved-resume-action">
                    {openingResumeId === resume.id
                      ? <LoaderCircle size={17} className="saved-resume-spinner" />
                      : <ArrowRight size={17} />}
                  </span>
                </button>
              ))}
            </div>
          )}
        </section>

        <div className="templates-section-heading">
          <span className="landing-eyebrow"><LayoutTemplate size={14} /> NEW PROJECT</span>
          <h2>Start with a template</h2>
        </div>

        <label className="new-resume-name-field">
          <span>Resume name</span>
          <input
            ref={resumeNameInputRef}
            aria-label="Resume name"
            aria-invalid={Boolean(resumeNameError)}
            aria-describedby={resumeNameError ? "new-resume-name-error" : undefined}
            placeholder="For example, Product Designer Resume"
            value={resumeName}
            onChange={(event) => {
              setResumeName(event.target.value);
              if (event.target.value.trim()) setResumeNameError("");
            }}
            maxLength={120}
          />
        </label>
        {resumeNameError && (
          <p className="template-error template-name-error" id="new-resume-name-error" role="alert">
            {resumeNameError}
          </p>
        )}

        {isLoading && <p className="template-state">Loading available templates…</p>}

        {needsSignIn && (
          <div className="template-state template-state-card">
            <h2>Sign in to start a project</h2>
            <p>Your templates and resume data are connected to your account.</p>
            <Link className="landing-cta" href="/auth">Sign in <ArrowRight size={16} /></Link>
          </div>
        )}

        {loadError && <p className="template-state template-error" role="alert">{loadError}</p>}
        {createError && <p className="template-state template-error" role="alert">{createError}</p>}

        {!isLoading && !needsSignIn && !loadError && templates.length === 0 && (
          <p className="template-state">No resume templates are available yet.</p>
        )}

        {!isLoading && !needsSignIn && !loadError && templates.length > 0 && (
          <div className="template-grid">
            {templates.map((template) => {
              const previewUrl = getPreviewUrl(template.previewImageUrl);
              return (
                <button
                  className="template-card"
                  type="button"
                  onClick={() => void startProject(template)}
                  disabled={creatingTemplateId !== null || openingResumeId !== null}
                  key={template.id}
                >
                  <div className="template-image">
                    {previewUrl ? (
                      <Image
                        src={previewUrl}
                        alt={`${template.name} resume template preview`}
                        fill
                        sizes="(max-width: 720px) 100vw, (max-width: 1050px) 50vw, 33vw"
                        unoptimized
                      />
                    ) : (
                      <div className="template-image-placeholder"><FileText size={38} /></div>
                    )}
                  </div>
                  <div className="template-card-details">
                    <span>
                      <strong>{template.name}</strong>
                      <small>
                        {creatingTemplateId === template.id
                          ? "Starting project…"
                          : "Resume template"}
                      </small>
                    </span>
                    <span className="template-card-arrow">
                      {creatingTemplateId === template.id ? "…" : <ArrowRight size={17} />}
                    </span>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </section>
    </main>
  );
}
