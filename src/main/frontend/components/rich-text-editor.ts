import { LitElement, html, css, nothing, type TemplateResult } from 'lit';
import { customElement, property, state, query } from 'lit/decorators.js';
import { unsafeHTML } from 'lit/directives/unsafe-html.js';
import { Editor, Node, mergeAttributes } from '@tiptap/core';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import Table from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import TextAlign from '@tiptap/extension-text-align';
import Highlight from '@tiptap/extension-highlight';
import TextStyle from '@tiptap/extension-text-style';
import { Color } from '@tiptap/extension-color';
import Placeholder from '@tiptap/extension-placeholder';
import CharacterCount from '@tiptap/extension-character-count';
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight';
import { createLowlight } from 'lowlight';
import langJs   from 'highlight.js/lib/languages/javascript';
import langTs   from 'highlight.js/lib/languages/typescript';
import langJava from 'highlight.js/lib/languages/java';
import langPy   from 'highlight.js/lib/languages/python';
import langXml  from 'highlight.js/lib/languages/xml';
import langCss  from 'highlight.js/lib/languages/css';
import langSql  from 'highlight.js/lib/languages/sql';
import langBash from 'highlight.js/lib/languages/bash';
import langJson from 'highlight.js/lib/languages/json';

const lowlight = createLowlight();
lowlight.register('javascript', langJs);
lowlight.register('js',         langJs);
lowlight.register('typescript', langTs);
lowlight.register('ts',         langTs);
lowlight.register('java',       langJava);
lowlight.register('python',     langPy);
lowlight.register('py',         langPy);
lowlight.register('html',       langXml);
lowlight.register('xml',        langXml);
lowlight.register('css',        langCss);
lowlight.register('sql',        langSql);
lowlight.register('bash',       langBash);
lowlight.register('sh',         langBash);
lowlight.register('json',       langJson);

export interface RteVariable {
  id: string;
  label: string;
  group?: string;
}

// ── Inline SVG icon helpers ────────────────────────────────────────────────────

const SVG_BASE = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">`;
const ico = (paths: string): string => `${SVG_BASE}${paths}</svg>`;

const ICONS = {
  undo:         ico('<path d="M9 14 4 9l5-5"/><path d="M4 9h10.5a5.5 5.5 0 0 1 0 11H11"/>'),
  redo:         ico('<path d="m15 14 5-5-5-5"/><path d="M20 9H9.5a5.5 5.5 0 0 0 0 11H13"/>'),
  bold:         ico('<path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/><path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/>'),
  italic:       ico('<line x1="19" x2="10" y1="4" y2="4"/><line x1="14" x2="5" y1="20" y2="20"/><line x1="15" x2="9" y1="4" y2="20"/>'),
  underline:    ico('<path d="M6 4v6a6 6 0 0 0 12 0V4"/><line x1="4" x2="20" y1="20" y2="20"/>'),
  strike:       ico('<path d="M16 4H9a3 3 0 0 0-2.83 4"/><path d="M14 12a4 4 0 0 1 0 8H6"/><line x1="4" x2="20" y1="12" y2="12"/>'),
  highlight:    ico('<path d="m9 11-6 6v3h3l6-6"/><path d="m22 12-4.6 4.6a2 2 0 0 1-2.8 0l-5.2-5.2a2 2 0 0 1 0-2.8L14 4"/>'),
  code:         ico('<polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/>'),
  paragraph:    ico('<path d="M13 4v16"/><path d="M17 4v16"/><path d="M19 4H9.5a4.5 4.5 0 0 0 0 9H13"/>'),
  bulletList:   ico('<line x1="8" x2="21" y1="6" y2="6"/><line x1="8" x2="21" y1="12" y2="12"/><line x1="8" x2="21" y1="18" y2="18"/><circle cx="3" cy="6" r="1" fill="currentColor"/><circle cx="3" cy="12" r="1" fill="currentColor"/><circle cx="3" cy="18" r="1" fill="currentColor"/>'),
  orderedList:  ico('<line x1="10" x2="21" y1="6" y2="6"/><line x1="10" x2="21" y1="12" y2="12"/><line x1="10" x2="21" y1="18" y2="18"/><path d="M4 6h1v4"/><path d="M4 10h2"/><path d="M6 18H4c0-1 2-2 2-3s-1-1.5-2-1"/>'),
  blockquote:   ico('<path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V20c0 1 0 1 1 1z"/><path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v3c0 1 0 1 1 1z"/>'),
  alignLeft:    ico('<line x1="21" x2="3" y1="6" y2="6"/><line x1="15" x2="3" y1="12" y2="12"/><line x1="17" x2="3" y1="18" y2="18"/>'),
  alignCenter:  ico('<line x1="21" x2="3" y1="6" y2="6"/><line x1="17" x2="7" y1="12" y2="12"/><line x1="19" x2="5" y1="18" y2="18"/>'),
  alignRight:   ico('<line x1="21" x2="3" y1="6" y2="6"/><line x1="21" x2="9" y1="12" y2="12"/><line x1="21" x2="7" y1="18" y2="18"/>'),
  alignJustify: ico('<line x1="21" x2="3" y1="6" y2="6"/><line x1="21" x2="3" y1="12" y2="12"/><line x1="21" x2="3" y1="18" y2="18"/>'),
  table:        ico('<rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M3 15h18M9 3v18M15 3v18"/>'),
  link:         ico('<path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>'),
  unlink:       ico('<path d="m18.84 12.25 1.72-1.71h-.02a5.004 5.004 0 0 0-.12-7.07 5.006 5.006 0 0 0-6.95 0l-1.72 1.71"/><path d="m5.17 11.75-1.71 1.71a5.004 5.004 0 0 0 .12 7.07 5.006 5.006 0 0 0 6.95 0l1.71-1.71"/><line x1="8" x2="8.01" y1="2" y2="2"/><line x1="2" x2="2.01" y1="8" y2="8"/>'),
  eraser:       ico('<path d="m7 21-4.3-4.3c-1-1-1-2.5 0-3.4l9.6-9.6c1-1 2.5-1 3.4 0l5.6 5.6c1 1 1 2.5 0 3.4L13 19"/><path d="M22 21H7"/><path d="m5 11 4 4"/>'),
  codeBlock:    ico('<rect x="3" y="3" width="18" height="18" rx="2"/><path d="m9 9-3 3 3 3"/><path d="m15 9 3 3-3 3"/>'),
};

// ── MergeField custom node (template variable chip) ───────────────────────────

const MergeFieldExtension = Node.create({
  name: 'mergeField',
  group: 'inline',
  inline: true,
  atom: true,
  selectable: true,
  draggable: false,

  addAttributes() {
    return {
      id:    { default: '' },
      label: { default: '' },
    };
  },

  parseHTML() {
    return [{
      tag: 'span[data-merge-field]',
      getAttrs: (el) => ({
        id:    (el as HTMLElement).getAttribute('data-field-id')    ?? '',
        label: (el as HTMLElement).getAttribute('data-field-label') ?? '',
      }),
    }];
  },

  renderHTML({ node }) {
    return [
      'span',
      {
        'data-merge-field':  '',
        'data-field-id':     node.attrs.id    as string,
        'data-field-label':  node.attrs.label as string,
        class:               'merge-field',
        contenteditable:     'false',
      },
      `{{${node.attrs.id as string}}}`,
    ];
  },

  addNodeView() {
    return ({ node }) => {
      const dom = document.createElement('span');
      dom.className = 'merge-field';
      dom.setAttribute('contenteditable', 'false');
      dom.setAttribute('data-merge-field',  '');
      dom.setAttribute('data-field-id',     node.attrs.id    as string);
      dom.setAttribute('data-field-label',  node.attrs.label as string);
      dom.title      = node.attrs.label as string;
      dom.textContent = `{{${node.attrs.id as string}}}`;
      return { dom };
    };
  },
});

// ── Component ─────────────────────────────────────────────────────────────────

@customElement('rich-text-editor')
export class RichTextEditorElement extends LitElement {

  // ── Public properties (synced with Java server) ──────────────────────────

  @property({ type: String })  value         = '';
  /** JSON string sent by the Java server via {@code setVariables()}. */
  @property({ type: String })  variablesJson = '[]';
  @property({ type: String })  placeholder   = '';
  @property({ type: Boolean, reflect: true }) readonly      = false;
  @property({ type: Number })                 minHeight     = 260;
  @property({ type: String })                 label         = '';
  @property({ type: Boolean, reflect: true }) invalid       = false;
  @property({ type: String })                 errorMessage  = '';
  @property({ type: Boolean, reflect: true }) required      = false;

  // ── Internal state ────────────────────────────────────────────────────────

  @state() private _showVarPicker  = false;
  @state() private _varSearch      = '';
  @state() private _showLinkDialog = false;
  @state() private _linkUrl        = '';
  @state() private _tableActive    = false;
  @state() private _codeBlockLang  = '';        // language attribute of the active code block
  @state() private _rev            = 0;         // bumped on transactions to refresh toolbar

  @query('#editor-mount') private _mount!: HTMLDivElement;

  private _editor?: Editor;
  private _editorHtml = '';                     // last HTML set BY the editor (not by server)
  private _vars: RteVariable[] = [];            // parsed from variablesJson

  // ── Styles ────────────────────────────────────────────────────────────────

  static styles = css`
    :host { display: block; font-family: inherit; }
    :host([readonly]) .toolbar { display: none; }

    /* ── Label ───────────────────────────────────────────────────── */
    .rte-label {
      display: block; font-size: 13px; font-weight: 600;
      color: #374151; margin-bottom: 4px; cursor: default;
    }
    .rte-required { color: #ef4444; margin-left: 1px; }

    /* ── Validation ──────────────────────────────────────────────── */
    .wrapper.invalid { border-color: #ef4444; }
    .wrapper.invalid:focus-within { border-color: #ef4444; box-shadow: 0 0 0 3px rgba(239,68,68,.14); }
    .rte-error {
      display: flex; align-items: center; gap: 4px;
      font-size: 12px; color: #ef4444; margin-top: 4px;
    }

    /* Wrapper */
    .wrapper {
      position: relative;
      display: flex;
      flex-direction: column;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      background: #fff;
      overflow: visible;
      transition: border-color .15s, box-shadow .15s;
    }
    .wrapper:focus-within { border-color: #475569; box-shadow: 0 0 0 3px rgba(71,85,105,.14); }

    /* ── Toolbar ─────────────────────────────────────────────────── */
    .toolbar {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 1px;
      padding: 5px 6px;
      border-bottom: 1px solid #e5e7eb;
      background: #f8fafc;
      border-radius: 6px 6px 0 0;
      user-select: none;
    }
    .tb-group { display: flex; align-items: center; gap: 1px; }
    .tb-sep   { width: 1px; height: 18px; background: #d1d5db; margin: 0 4px; flex-shrink: 0; }
    .tb-spacer { flex: 1; }

    .tb-btn {
      display: inline-flex; align-items: center; justify-content: center;
      min-width: 26px; height: 26px; padding: 0 4px;
      border: 1px solid transparent; border-radius: 4px;
      background: transparent; cursor: pointer;
      font-family: inherit; font-size: 11.5px; font-weight: 700;
      color: #374151; white-space: nowrap;
      transition: background .1s, color .1s, border-color .1s;
      line-height: 1;
    }
    .tb-btn:hover:not(:disabled) { background: #e2e8f0; color: #0f172a; }
    .tb-btn.active { background: #334155; color: #fff; border-color: #334155; }
    .tb-btn.active:hover { background: #1e293b; }
    .tb-btn:disabled { opacity: .38; cursor: not-allowed; }
    .tb-btn svg { pointer-events: none; }

    .var-btn {
      gap: 4px; padding: 0 8px; height: 24px; font-size: 11px;
      background: #f0fdf4; border-color: #86efac; color: #15803d;
      border-radius: 999px;
    }
    .var-btn:hover:not(:disabled) { background: #dcfce7; color: #166534; }
    .var-btn.active { background: #16a34a; border-color: #16a34a; color: #fff; }

    /* ── Editor mount ────────────────────────────────────────────── */
    #editor-mount {
      flex: 1;
      padding: 14px 16px;
      cursor: text;
      overflow-y: auto;
    }

    /* ProseMirror base */
    .ProseMirror {
      outline: none;
      font-family: inherit;
      font-size: 14px;
      line-height: 1.65;
      color: #1e293b;
      caret-color: #334155;
    }
    .ProseMirror > * + * { margin-top: .55em; }
    .ProseMirror p { margin: 0; }

    /* Headings */
    .ProseMirror h1 { font-size: 1.65em; font-weight: 800; color: #0f172a; line-height: 1.2; margin: .4em 0 .15em; }
    .ProseMirror h2 { font-size: 1.3em;  font-weight: 700; color: #0f172a; line-height: 1.25; margin: .4em 0 .15em; }
    .ProseMirror h3 { font-size: 1.1em;  font-weight: 700; color: #1e293b; line-height: 1.3;  margin: .35em 0 .1em; }

    /* Lists */
    .ProseMirror ul,
    .ProseMirror ol { padding-left: 1.5em; margin: .25em 0; }
    .ProseMirror li { margin: .1em 0; }
    .ProseMirror ul { list-style-type: disc; }
    .ProseMirror ol { list-style-type: decimal; }
    .ProseMirror li > p { margin: 0; }

    /* Blockquote */
    .ProseMirror blockquote {
      border-left: 3px solid #94a3b8;
      margin: .4em 0;
      padding: .25em .8em;
      color: #64748b;
      font-style: italic;
    }

    /* Code */
    .ProseMirror code {
      background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 3px;
      padding: .1em .35em; font-size: .84em; color: #0f172a;
      font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
    }
    .ProseMirror pre {
      background: #1e293b; border-radius: 6px;
      padding: 2em 1em .75em 1em; overflow-x: auto; margin: .4em 0;
      position: relative;
    }
    .ProseMirror pre code {
      background: none; border: none; color: #e2e8f0; padding: 0; font-size: .85em;
      font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
    }

    /* Language label — shown as a header strip when a language class is set */
    .ProseMirror pre > code[class^="language-"]::before {
      position: absolute; top: 0; left: 0; right: 0;
      padding: 3px 12px;
      font-size: 10px; font-weight: 700; letter-spacing: .06em; text-transform: uppercase;
      color: #64748b;
      background: rgba(255,255,255,.05);
      border-bottom: 1px solid rgba(255,255,255,.07);
      border-radius: 6px 6px 0 0;
      font-family: inherit; pointer-events: none;
    }
    .ProseMirror pre > code.language-javascript::before,
    .ProseMirror pre > code.language-js::before         { content: 'JavaScript'; }
    .ProseMirror pre > code.language-typescript::before,
    .ProseMirror pre > code.language-ts::before         { content: 'TypeScript'; }
    .ProseMirror pre > code.language-java::before       { content: 'Java'; }
    .ProseMirror pre > code.language-python::before,
    .ProseMirror pre > code.language-py::before         { content: 'Python'; }
    .ProseMirror pre > code.language-html::before       { content: 'HTML'; }
    .ProseMirror pre > code.language-xml::before        { content: 'XML'; }
    .ProseMirror pre > code.language-css::before        { content: 'CSS'; }
    .ProseMirror pre > code.language-sql::before        { content: 'SQL'; }
    .ProseMirror pre > code.language-bash::before,
    .ProseMirror pre > code.language-sh::before         { content: 'Shell'; }
    .ProseMirror pre > code.language-json::before       { content: 'JSON'; }

    /* Syntax highlighting — highlight.js tokens (GitHub Dark palette) */
    .ProseMirror pre .hljs-comment,
    .ProseMirror pre .hljs-quote          { color: #8b949e; font-style: italic; }
    .ProseMirror pre .hljs-keyword,
    .ProseMirror pre .hljs-selector-tag   { color: #ff7b72; }
    .ProseMirror pre .hljs-string,
    .ProseMirror pre .hljs-attr           { color: #a5d6ff; }
    .ProseMirror pre .hljs-number,
    .ProseMirror pre .hljs-literal        { color: #79c0ff; }
    .ProseMirror pre .hljs-title          { color: #d2a8ff; }
    .ProseMirror pre .hljs-type           { color: #79c0ff; }
    .ProseMirror pre .hljs-variable,
    .ProseMirror pre .hljs-template-variable,
    .ProseMirror pre .hljs-built_in       { color: #ffa657; }
    .ProseMirror pre .hljs-name           { color: #7ee787; }
    .ProseMirror pre .hljs-symbol         { color: #79c0ff; }
    .ProseMirror pre .hljs-meta           { color: #79c0ff; font-style: italic; }
    .ProseMirror pre .hljs-params         { color: #e6edf3; }
    .ProseMirror pre .hljs-subst          { color: #e6edf3; }
    .ProseMirror pre .hljs-addition       { color: #aff5b4; background: #033a16; }
    .ProseMirror pre .hljs-deletion       { color: #ffdcd7; background: #67060c; }
    .ProseMirror pre .hljs-emphasis       { font-style: italic; }
    .ProseMirror pre .hljs-strong         { font-weight: 700; }

    /* Toolbar language selector */
    .tb-lang-select {
      height: 22px; font-size: 11px; font-family: inherit;
      border: 1px solid #d1d5db; border-radius: 4px;
      padding: 0 6px; background: #fff; color: #374151;
      cursor: pointer; outline: none;
    }
    .tb-lang-select:focus { border-color: #475569; }

    /* HR */
    .ProseMirror hr { border: none; border-top: 2px solid #e2e8f0; margin: .8em 0; }

    /* Links */
    .ProseMirror a { color: #2563eb; text-decoration: underline; }
    .ProseMirror a:hover { color: #1d4ed8; }

    /* Highlight */
    .ProseMirror mark { background: #fef08a; border-radius: 2px; padding: 0 2px; color: inherit; }

    /* Selection */
    .ProseMirror ::selection { background: rgba(51,65,85,.18); }

    /* Placeholder */
    .ProseMirror .is-empty::before,
    .ProseMirror .is-editor-empty::before {
      content: attr(data-placeholder);
      float: left; height: 0;
      color: #94a3b8; pointer-events: none; font-style: italic;
    }

    /* ── Table ───────────────────────────────────────────────────── */
    .ProseMirror .tableWrapper { overflow-x: auto; margin: .5em 0; }
    .ProseMirror table {
      border-collapse: collapse;
      width: 100%;
      table-layout: fixed;
    }
    .ProseMirror th,
    .ProseMirror td {
      border: 1px solid #d1d5db;
      padding: 6px 10px;
      vertical-align: top;
      min-width: 60px;
      position: relative;
      font-size: 13.5px;
    }
    .ProseMirror th {
      background: #f8fafc;
      font-weight: 700; color: #374151;
      text-align: left;
    }
    .ProseMirror td { background: #fff; }
    .ProseMirror .selectedCell::after {
      content: ''; position: absolute; inset: 0;
      background: rgba(51,65,85,.1); pointer-events: none;
    }
    .ProseMirror .column-resize-handle {
      position: absolute; right: -2px; top: 0; bottom: 0;
      width: 4px; background: #94a3b8; cursor: col-resize; z-index: 10;
    }
    .ProseMirror.resize-cursor { cursor: col-resize; }

    /* ── Merge-field chip ────────────────────────────────────────── */
    .ProseMirror .merge-field {
      display: inline-flex; align-items: center;
      background: #eff6ff; border: 1px solid #93c5fd;
      border-radius: 999px; padding: 0 8px; height: 19px;
      font-size: 11.5px; font-weight: 700; color: #1d4ed8;
      font-family: 'JetBrains Mono', Consolas, monospace;
      cursor: default; user-select: none; white-space: nowrap;
      vertical-align: text-bottom; line-height: 1;
    }
    .ProseMirror .merge-field.ProseMirror-selectednode {
      background: #dbeafe; border-color: #3b82f6;
      box-shadow: 0 0 0 2px rgba(59,130,246,.3);
    }

    /* ── Status bar ──────────────────────────────────────────────── */
    .status-bar {
      display: flex; justify-content: flex-end; gap: 12px;
      padding: 3px 10px; border-top: 1px solid #f1f5f9;
      background: #f8fafc; border-radius: 0 0 6px 6px;
      font-size: 11px; color: #94a3b8; font-family: inherit;
    }

    /* ── Variable picker popup ───────────────────────────────────── */
    .var-picker {
      position: absolute; top: 40px; right: 6px; z-index: 200;
      background: #fff; border: 1px solid #e2e8f0;
      border-radius: 8px; box-shadow: 0 8px 28px rgba(0,0,0,.13);
      width: 290px; display: flex; flex-direction: column; overflow: hidden;
    }
    .var-picker-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 10px 12px 6px;
      font-size: 12px; font-weight: 700; color: #374151;
      border-bottom: 1px solid #f1f5f9;
    }
    .var-picker-close {
      background: none; border: none; cursor: pointer;
      color: #94a3b8; padding: 2px 5px; border-radius: 3px; font-size: 13px;
      line-height: 1;
    }
    .var-picker-close:hover { background: #f1f5f9; color: #374151; }
    .var-search {
      margin: 8px 8px 4px; padding: 6px 10px;
      border: 1px solid #e2e8f0; border-radius: 5px;
      font-size: 13px; font-family: inherit; outline: none;
    }
    .var-search:focus { border-color: #475569; }
    .var-list { max-height: 240px; overflow-y: auto; padding: 2px 4px 6px; }
    .var-group-label {
      font-size: 10px; font-weight: 700; color: #94a3b8;
      text-transform: uppercase; letter-spacing: .06em;
      padding: 6px 8px 2px;
    }
    .var-item {
      display: flex; align-items: center; justify-content: space-between;
      width: 100%; padding: 6px 10px; border: none; background: transparent;
      cursor: pointer; border-radius: 5px; text-align: left;
      font-family: inherit; transition: background .1s; gap: 8px;
    }
    .var-item:hover { background: #f1f5f9; }
    .var-item-label { font-size: 13px; font-weight: 500; color: #1e293b; flex: 1; }
    .var-item-chip {
      font-size: 10.5px; color: #1d4ed8; background: #eff6ff;
      border: 1px solid #93c5fd; border-radius: 4px;
      padding: 1px 6px; font-family: 'JetBrains Mono', Consolas, monospace;
      white-space: nowrap; flex-shrink: 0;
    }
    .var-empty { padding: 16px; text-align: center; color: #94a3b8; font-size: 13px; }

    /* ── Link dialog ─────────────────────────────────────────────── */
    .link-dialog {
      position: absolute; top: 40px; left: 50%; transform: translateX(-50%);
      z-index: 200; background: #fff; border: 1px solid #e2e8f0;
      border-radius: 8px; box-shadow: 0 8px 28px rgba(0,0,0,.13);
      width: 340px; padding: 14px;
    }
    .link-dialog-title { font-size: 13px; font-weight: 700; color: #374151; margin-bottom: 8px; }
    .link-input {
      width: 100%; box-sizing: border-box; padding: 7px 10px;
      border: 1px solid #e2e8f0; border-radius: 5px;
      font-size: 13px; font-family: inherit; outline: none; margin-bottom: 10px;
    }
    .link-input:focus { border-color: #475569; }
    .link-dialog-actions { display: flex; justify-content: flex-end; gap: 6px; }
    .link-btn {
      padding: 5px 14px; border-radius: 5px; cursor: pointer;
      font-size: 12px; font-family: inherit; font-weight: 600; border: none;
    }
    .link-btn-cancel { background: #f1f5f9; color: #374151; border: 1px solid #e2e8f0; }
    .link-btn-cancel:hover { background: #e2e8f0; }
    .link-btn-ok { background: #334155; color: #fff; }
    .link-btn-ok:hover { background: #475569; }
  `;

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  disconnectedCallback() {
    super.disconnectedCallback();
    this._editor?.destroy();
  }

  firstUpdated() {
    this._initEditor();
  }

  updated(changed: Map<string, unknown>) {
    const ed = this._editor;
    if (!ed) return;

    if (changed.has('variablesJson')) {
      try { this._vars = JSON.parse(this.variablesJson || '[]') as RteVariable[]; }
      catch { this._vars = []; }
    }

    if (changed.has('value')) {
      // Only update editor when the change came from the server (differs from last editor output)
      if (this.value !== this._editorHtml) {
        this._editorHtml = this.value;
        ed.commands.setContent(this.value || '', false);
      }
    }
    if (changed.has('readonly')) {
      ed.setEditable(!this.readonly);
    }
    if (changed.has('placeholder')) {
      // Force ProseMirror to re-render with new placeholder
      ed.view.dispatch(ed.state.tr);
    }
  }

  // ── Editor initialisation ─────────────────────────────────────────────────

  private _initEditor() {
    this._editor = new Editor({
      element: this._mount,
      extensions: [
        StarterKit.configure({ heading: { levels: [1, 2, 3] }, codeBlock: false }),
        Underline,
        Link.configure({ openOnClick: false, autolink: true }),
        Table.configure({ resizable: true }),
        TableRow,
        TableCell,
        TableHeader,
        TextAlign.configure({ types: ['heading', 'paragraph'] }),
        Highlight.configure({ multicolor: false }),
        TextStyle,
        Color,
        Placeholder.configure({
          placeholder: () => this.placeholder,
          showOnlyCurrent: false,
        }),
        CharacterCount,
        CodeBlockLowlight.configure({ lowlight }),
        MergeFieldExtension,
      ],
      content:  this.value || '',
      editable: !this.readonly,

      editorProps: {
        handleKeyDown: (_view, event) => {
          // Ctrl/Cmd+Enter inside a table → insert paragraph below and move cursor there
          if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
            if (this._editor?.isActive('table')) {
              this._exitTable();
              return true;
            }
          }
          return false;
        },
      },

      onTransaction: () => {
        this._tableActive   = this._editor?.isActive('table') ?? false;
        this._codeBlockLang = (this._editor?.getAttributes('codeBlock')?.language as string) ?? '';
        this._rev++;
        this.requestUpdate();
      },

      onUpdate: ({ editor }) => {
        const html = editor.getHTML();
        this._editorHtml = html;
        this.value = html;
        this.dispatchEvent(new CustomEvent('value-changed', {
          detail: { value: html },
          bubbles: true,
          composed: true,
        }));
      },
    });
  }

  // ── Public API (callable from Java via callJsFunction) ────────────────────

  /**
   * Moves the cursor to the first node after the enclosing table.
   * If the table is the last node in the document, a new paragraph is inserted first.
   * Bound to Ctrl/Cmd+Enter when the cursor is inside a table.
   */
  private _exitTable() {
    const editor = this._editor;
    if (!editor) return;

    const { state } = editor;
    const { $from } = state.selection;

    for (let depth = $from.depth; depth >= 0; depth--) {
      const node = $from.node(depth);
      if (node.type.name !== 'table') continue;

      const tableEnd = $from.before(depth) + node.nodeSize;

      if (tableEnd < state.doc.content.size) {
        // Something already exists after the table — just move the cursor there
        editor.chain().setTextSelection(tableEnd + 1).focus().run();
      } else {
        // Table is the last node — insert a paragraph and move into it
        editor.chain()
          .insertContentAt(tableEnd, { type: 'paragraph' })
          .setTextSelection(tableEnd + 1)
          .focus()
          .run();
      }
      return;
    }
  }

  insertVariable(id: string, label: string) {
    this._editor?.chain().focus().insertContent({
      type: 'mergeField',
      attrs: { id, label },
    }).run();
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private _btn(
    icon:     unknown,
    title:    string,
    onClick:  () => void,
    active  = false,
    disabled = false,
  ): TemplateResult {
    return html`
      <button
        class=${`tb-btn${active ? ' active' : ''}`}
        title=${title}
        .disabled=${disabled}
        @mousedown=${(e: MouseEvent) => { e.preventDefault(); onClick(); }}
      >${icon}</button>
    `;
  }

  private _insertVarFromPicker(v: RteVariable) {
    this.insertVariable(v.id, v.label);
    this._showVarPicker = false;
    this._varSearch = '';
  }

  private _openLinkDialog() {
    this._linkUrl = this._editor?.getAttributes('link').href ?? '';
    this._showLinkDialog = true;
  }

  private _confirmLink() {
    if (this._linkUrl) {
      this._editor?.chain().focus().setLink({ href: this._linkUrl }).run();
    }
    this._showLinkDialog = false;
    this._linkUrl = '';
  }

  // ── Render ────────────────────────────────────────────────────────────────

  render() {
    const e = this._editor;
    const chars = e?.storage.characterCount.characters() ?? 0;
    const words = e?.storage.characterCount.words()      ?? 0;
    const filteredVars = this._filterVars(this._vars);

    return html`
      ${this.label ? html`
        <label class="rte-label">
          ${this.label}
          ${this.required ? html`<span class="rte-required" aria-hidden="true">*</span>` : nothing}
        </label>` : nothing}
      <div class="wrapper${this.invalid ? ' invalid' : ''}">
        ${this.readonly ? nothing : this._renderToolbar()}
        <div id="editor-mount" style="min-height:${this.minHeight}px"></div>
        <div class="status-bar">
          <span>${chars} caractere${chars !== 1 ? 's' : ''}</span>
          <span>${words} palavra${words !== 1 ? 's' : ''}</span>
        </div>
        ${this._showVarPicker  ? this._renderVarPicker(filteredVars)  : nothing}
        ${this._showLinkDialog ? this._renderLinkDialog()             : nothing}
      </div>
      ${this.invalid && this.errorMessage ? html`
        <div class="rte-error" role="alert">
          <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          ${this.errorMessage}
        </div>` : nothing}
    `;
  }

  private _renderToolbar(): TemplateResult {
    const e = this._editor;
    if (!e) return html`<div class="toolbar"></div>`;

    return html`
      <div class="toolbar" @keydown=${(ev: KeyboardEvent) => ev.stopPropagation()}>

        <!-- History -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.undo), 'Desfazer (Ctrl+Z)', () => e.chain().focus().undo().run(), false, !e.can().undo())}
          ${this._btn(unsafeHTML(ICONS.redo), 'Refazer (Ctrl+Y)', () => e.chain().focus().redo().run(), false, !e.can().redo())}
        </div>

        <div class="tb-sep"></div>

        <!-- Text format -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.bold),      'Negrito (Ctrl+B)',   () => e.chain().focus().toggleBold().run(),      e.isActive('bold'))}
          ${this._btn(unsafeHTML(ICONS.italic),    'Itálico (Ctrl+I)',   () => e.chain().focus().toggleItalic().run(),    e.isActive('italic'))}
          ${this._btn(unsafeHTML(ICONS.underline), 'Sublinhado (Ctrl+U)',() => e.chain().focus().toggleUnderline().run(), e.isActive('underline'))}
          ${this._btn(unsafeHTML(ICONS.strike),    'Tachado',            () => e.chain().focus().toggleStrike().run(),    e.isActive('strike'))}
          ${this._btn(unsafeHTML(ICONS.highlight), 'Realçar',            () => e.chain().focus().toggleHighlight().run(),   e.isActive('highlight'))}
          ${this._btn(unsafeHTML(ICONS.code),      'Código inline',      () => e.chain().focus().toggleCode().run(),       e.isActive('code'))}
          ${this._btn(unsafeHTML(ICONS.codeBlock), 'Bloco de código',    () => e.chain().focus().toggleCodeBlock().run(),  e.isActive('codeBlock'))}
        </div>

        <div class="tb-sep"></div>

        <!-- Headings + paragraph -->
        <div class="tb-group">
          ${this._btn('H1', 'Título 1', () => e.chain().focus().toggleHeading({ level: 1 }).run(), e.isActive('heading', { level: 1 }))}
          ${this._btn('H2', 'Título 2', () => e.chain().focus().toggleHeading({ level: 2 }).run(), e.isActive('heading', { level: 2 }))}
          ${this._btn('H3', 'Título 3', () => e.chain().focus().toggleHeading({ level: 3 }).run(), e.isActive('heading', { level: 3 }))}
          ${this._btn(unsafeHTML(ICONS.paragraph), 'Parágrafo', () => e.chain().focus().setParagraph().run(), e.isActive('paragraph'))}
        </div>

        <div class="tb-sep"></div>

        <!-- Lists + blockquote -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.bulletList),  'Lista com marcadores', () => e.chain().focus().toggleBulletList().run(),  e.isActive('bulletList'))}
          ${this._btn(unsafeHTML(ICONS.orderedList), 'Lista numerada',       () => e.chain().focus().toggleOrderedList().run(), e.isActive('orderedList'))}
          ${this._btn(unsafeHTML(ICONS.blockquote),  'Citação',              () => e.chain().focus().toggleBlockquote().run(),  e.isActive('blockquote'))}
        </div>

        <div class="tb-sep"></div>

        <!-- Alignment -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.alignLeft),    'Alinhar à esquerda', () => e.chain().focus().setTextAlign('left').run(),    e.isActive({ textAlign: 'left' }))}
          ${this._btn(unsafeHTML(ICONS.alignCenter),  'Centralizar',        () => e.chain().focus().setTextAlign('center').run(),  e.isActive({ textAlign: 'center' }))}
          ${this._btn(unsafeHTML(ICONS.alignRight),   'Alinhar à direita',  () => e.chain().focus().setTextAlign('right').run(),   e.isActive({ textAlign: 'right' }))}
          ${this._btn(unsafeHTML(ICONS.alignJustify), 'Justificar',         () => e.chain().focus().setTextAlign('justify').run(), e.isActive({ textAlign: 'justify' }))}
        </div>

        <div class="tb-sep"></div>

        <!-- Table -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.table), 'Inserir tabela 3×3', () =>
            e.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
          )}
          ${this._tableActive ? html`
            ${this._btn('↵',  'Sair da tabela (Ctrl+Enter)', () => this._exitTable())}
            <div class="tb-sep"></div>
            ${this._btn('+↑', 'Inserir linha acima',  () => e.chain().focus().addRowBefore().run())}
            ${this._btn('+↓', 'Inserir linha abaixo', () => e.chain().focus().addRowAfter().run())}
            ${this._btn('−↕', 'Remover linha',        () => e.chain().focus().deleteRow().run())}
            <div class="tb-sep"></div>
            ${this._btn('+←', 'Inserir coluna antes', () => e.chain().focus().addColumnBefore().run())}
            ${this._btn('+→', 'Inserir coluna depois',() => e.chain().focus().addColumnAfter().run())}
            ${this._btn('−↔', 'Remover coluna',       () => e.chain().focus().deleteColumn().run())}
            <div class="tb-sep"></div>
            ${this._btn('✕⊞', 'Excluir tabela',       () => e.chain().focus().deleteTable().run())}
          ` : nothing}
        </div>

        <!-- Code block language selector (shown when cursor is inside a code block) -->
        ${e.isActive('codeBlock') ? html`
          <div class="tb-sep"></div>
          <div class="tb-group">
            <select
              class="tb-lang-select"
              title="Linguagem do bloco de código"
              .value=${this._codeBlockLang || ''}
              @mousedown=${(ev: MouseEvent) => ev.stopPropagation()}
              @change=${(ev: Event) => {
                const lang = (ev.target as HTMLSelectElement).value;
                e.chain().focus().updateAttributes('codeBlock', { language: lang || null }).run();
              }}
            >
              <option value="">— linguagem —</option>
              <option value="javascript">JavaScript</option>
              <option value="typescript">TypeScript</option>
              <option value="java">Java</option>
              <option value="python">Python</option>
              <option value="html">HTML</option>
              <option value="xml">XML</option>
              <option value="css">CSS</option>
              <option value="sql">SQL</option>
              <option value="bash">Shell / Bash</option>
              <option value="json">JSON</option>
            </select>
          </div>
        ` : nothing}

        <div class="tb-sep"></div>

        <!-- Link -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.link), e.isActive('link') ? 'Editar link' : 'Inserir link',
            () => this._openLinkDialog(), e.isActive('link')
          )}
          ${e.isActive('link')
            ? this._btn(unsafeHTML(ICONS.unlink), 'Remover link', () => e.chain().focus().unsetLink().run())
            : nothing
          }
        </div>

        ${this._vars.length > 0 ? html`
          <div class="tb-sep"></div>
          <div class="tb-group">
            <button
              class=${`tb-btn var-btn${this._showVarPicker ? ' active' : ''}`}
              title="Inserir variável dinâmica"
              @mousedown=${(e: MouseEvent) => { e.preventDefault(); this._showVarPicker = !this._showVarPicker; }}
            >
              <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M8 3H5a2 2 0 0 0-2 2v3"/><path d="M21 8V5a2 2 0 0 0-2-2h-3"/>
                <path d="M3 16v3a2 2 0 0 0 2 2h3"/><path d="M16 21h3a2 2 0 0 0 2-2v-3"/>
                <path d="M10 10h4"/><path d="M12 8v8"/>
              </svg>
              Variáveis
            </button>
          </div>
        ` : nothing}

        <div class="tb-spacer"></div>

        <!-- Clear formatting -->
        <div class="tb-group">
          ${this._btn(unsafeHTML(ICONS.eraser), 'Limpar formatação',
            () => e.chain().focus().clearNodes().unsetAllMarks().run()
          )}
        </div>
      </div>
    `;
  }

  private _filterVars(vars: RteVariable[]): RteVariable[] {
    if (!this._varSearch) return vars;
    const q = this._varSearch.toLowerCase();
    return vars.filter(v =>
      v.label.toLowerCase().includes(q) || v.id.toLowerCase().includes(q)
    );
  }

  private _renderVarPicker(vars: RteVariable[]): TemplateResult {
    // Group by optional `group` field
    const grouped = new Map<string, RteVariable[]>();
    for (const v of vars) {
      const g = v.group ?? '';
      if (!grouped.has(g)) grouped.set(g, []);
      grouped.get(g)!.push(v);
    }

    return html`
      <div class="var-picker" @keydown=${(e: KeyboardEvent) => { if (e.key === 'Escape') this._showVarPicker = false; }}>
        <div class="var-picker-header">
          <span>Variáveis dinâmicas</span>
          <button class="var-picker-close" @mousedown=${() => { this._showVarPicker = false; }}>✕</button>
        </div>
        <input
          class="var-search"
          type="text"
          placeholder="Buscar variável..."
          .value=${this._varSearch}
          @input=${(e: Event) => { this._varSearch = (e.target as HTMLInputElement).value; }}
        />
        <div class="var-list">
          ${vars.length === 0
            ? html`<div class="var-empty">Nenhuma variável encontrada.</div>`
            : [...grouped.entries()].map(([group, items]) => html`
                ${group ? html`<div class="var-group-label">${group}</div>` : nothing}
                ${items.map(v => html`
                  <button class="var-item" @mousedown=${() => { this._insertVarFromPicker(v); }}>
                    <span class="var-item-label">${v.label}</span>
                    <code class="var-item-chip">{{${v.id}}}</code>
                  </button>
                `)}
              `)
          }
        </div>
      </div>
    `;
  }

  private _renderLinkDialog(): TemplateResult {
    return html`
      <div class="link-dialog">
        <div class="link-dialog-title">Inserir / editar link</div>
        <input
          class="link-input"
          type="url"
          placeholder="https://exemplo.com"
          .value=${this._linkUrl}
          @input=${(e: Event) => { this._linkUrl = (e.target as HTMLInputElement).value; }}
          @keydown=${(e: KeyboardEvent) => {
            e.stopPropagation();
            if (e.key === 'Enter')  this._confirmLink();
            if (e.key === 'Escape') { this._showLinkDialog = false; this._linkUrl = ''; }
          }}
        />
        <div class="link-dialog-actions">
          <button class="link-btn link-btn-cancel" @mousedown=${() => { this._showLinkDialog = false; this._linkUrl = ''; }}>Cancelar</button>
          <button class="link-btn link-btn-ok"     @mousedown=${() => this._confirmLink()}>Confirmar</button>
        </div>
      </div>
    `;
  }
}