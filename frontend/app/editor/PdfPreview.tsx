"use client";

import { useEffect, useRef, useState } from "react";
import type { PDFDocumentProxy } from "pdfjs-dist";

export default function PdfPreview({ pdfUrl }: { pdfUrl: string }) {
  const pagesRef = useRef<HTMLDivElement>(null);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    let documentProxy: PDFDocumentProxy | null = null;
    const renderTasks: { cancel: () => void }[] = [];

    const renderPdf = async () => {
      try {
        const pdfjs = await import("pdfjs-dist");
        pdfjs.GlobalWorkerOptions.workerSrc = new URL(
          "pdfjs-dist/build/pdf.worker.min.mjs",
          import.meta.url,
        ).toString();

        const response = await fetch(pdfUrl);
        if (!response.ok) throw new Error(`Could not load the PDF preview (${response.status}).`);

        const data = await response.arrayBuffer();
        documentProxy = await pdfjs.getDocument({ data }).promise;
        if (cancelled || !pagesRef.current) return;

        const pagesContainer = pagesRef.current;
        pagesContainer.replaceChildren();

        for (let pageNumber = 1; pageNumber <= documentProxy.numPages; pageNumber += 1) {
          if (cancelled) return;
          const page = await documentProxy.getPage(pageNumber);
          const baseViewport = page.getViewport({ scale: 1 });
          const cssWidth = Math.min(pagesContainer.clientWidth || 720, 720);
          const outputScale = Math.min(window.devicePixelRatio || 1, 2);
          const scale = (cssWidth / baseViewport.width) * outputScale;
          const viewport = page.getViewport({ scale });
          const canvas = window.document.createElement("canvas");
          const context = canvas.getContext("2d");
          if (!context) throw new Error("Your browser could not render the PDF preview.");

          canvas.className = "pdf-preview-page";
          canvas.width = Math.ceil(viewport.width);
          canvas.height = Math.ceil(viewport.height);
          canvas.style.width = `${cssWidth}px`;
          canvas.style.height = `${viewport.height / outputScale}px`;
          pagesContainer.append(canvas);

          const task = page.render({ canvasContext: context, viewport });
          renderTasks.push(task);
          await task.promise;
        }
        if (!cancelled) setIsLoading(false);
      } catch (renderError) {
        if (!cancelled) {
          setError(renderError instanceof Error ? renderError.message : "Unable to display the PDF preview.");
          setIsLoading(false);
        }
      }
    };

    void renderPdf();
    return () => {
      cancelled = true;
      renderTasks.forEach((task) => task.cancel());
      if (documentProxy) void documentProxy.destroy();
    };
  }, [pdfUrl]);

  return (
    <div className="pdf-preview-container" style={{ width: "100%", maxWidth: 720, minWidth: 0 }}>
      {isLoading && <p className="pdf-preview-loading" role="status">Rendering PDF preview…</p>}
      {error && <p className="pdf-preview-error" role="alert">{error}</p>}
      <div
        className="pdf-preview-pages"
        ref={pagesRef}
        aria-label="PDF pages"
        style={{ width: "100%", minWidth: 0 }}
      />
    </div>
  );
}
