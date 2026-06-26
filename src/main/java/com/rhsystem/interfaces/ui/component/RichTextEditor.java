package com.rhsystem.interfaces.ui.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

import java.util.Arrays;
import java.util.List;

/**
 * Rich Text Editor powered by Tiptap — Vaadin Flow wrapper.
 *
 * <h3>Features</h3>
 * <ul>
 *   <li>Text formatting: bold, italic, underline, strikethrough, highlight, inline code</li>
 *   <li>Headings H1–H3, paragraph, blockquote, bullet/ordered lists</li>
 *   <li>Text alignment: left, center, right, justify</li>
 *   <li>Tables with resizable columns and row/column management toolbar</li>
 *   <li>Hyperlinks (insert, edit, remove)</li>
 *   <li>Dynamic merge-field chips: non-editable inline tokens rendered as {@code {{id}}}</li>
 *   <li>Character and word count in the status bar</li>
 * </ul>
 *
 * <h3>Quickstart</h3>
 * <pre>{@code
 * var editor = new RichTextEditor();
 * editor.setPlaceholder("Escreva o conteúdo aqui...");
 * editor.setVariables(
 *     Variable.of("nome",    "Nome Completo"),
 *     Variable.of("empresa", "Empresa", "Dados da empresa"),
 *     Variable.of("cargo",   "Cargo",   "Dados da empresa")
 * );
 * editor.addValueChangeListener(e -> save(e.getValue()));
 * }</pre>
 */
@Tag("rich-text-editor")
@JsModule("./components/rich-text-editor.ts")
@NpmPackage(value = "@tiptap/core",                      version = "^2.27.2")
@NpmPackage(value = "@tiptap/starter-kit",               version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-underline",       version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-link",            version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-table",           version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-table-row",       version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-table-cell",      version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-table-header",    version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-text-align",      version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-highlight",       version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-text-style",      version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-color",           version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-placeholder",     version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-character-count",     version = "^2.27.2")
@NpmPackage(value = "@tiptap/extension-code-block-lowlight", version = "^2.27.2")
@NpmPackage(value = "lowlight",                              version = "^3.0.0")
@NpmPackage(value = "highlight.js",                          version = "^11.0.0")
public class RichTextEditor extends AbstractSinglePropertyField<RichTextEditor, String>
        implements HasSize, HasValidation, HasLabel {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * A named template variable that the user can insert as a non-editable chip.
     * <p>
     * Rendered in the picker and in the output HTML as {@code {{id}}}.
     * The optional {@code group} field groups variables under a labelled section
     * in the picker dropdown.
     */
    public record Variable(String id, String label, String group) {

        /** Creates a variable without a group. */
        public Variable(String id, String label) {
            this(id, label, null);
        }

        /** Factory — no group. */
        public static Variable of(String id, String label) {
            return new Variable(id, label, null);
        }

        /** Factory — with group header in the picker. */
        public static Variable of(String id, String label, String group) {
            return new Variable(id, label, group);
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public RichTextEditor() {
        super("value", "", false);
        setWidth("100%");
    }

    /**
     * Intercepts every value change — whether it originates from the browser or from a
     * server-side {@link #setValue} call — and sanitizes the HTML before storing it.
     * This guarantees that {@link #getValue()} always returns safe, sanitized content
     * and that malicious markup can never be persisted or re-emitted via Binder.
     */
    @Override
    protected void setModelValue(String newModelValue, boolean fromClient) {
        super.setModelValue(RichTextSanitizer.sanitize(newModelValue), fromClient);
    }

    // ── Variables ─────────────────────────────────────────────────────────────

    /**
     * Replaces the variable list shown in the picker.
     *
     * @param variables list of {@link Variable} objects
     */
    public void setVariables(List<Variable> variables) {
        try {
            getElement().setProperty("variablesJson", MAPPER.writeValueAsString(variables));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize variables", e);
        }
    }

    /**
     * Varargs overload — avoids wrapping in a {@code List}:
     * <pre>{@code editor.setVariables(Variable.of("nome","Nome"), Variable.of("cargo","Cargo")); }</pre>
     */
    public void setVariables(Variable... variables) {
        setVariables(Arrays.asList(variables));
    }

    /**
     * Programmatically inserts a variable chip at the current editor cursor.
     * Useful for external palette buttons outside the editor toolbar.
     *
     * @param id    the variable id, rendered as {@code {{id}}}
     * @param label human-readable label shown as the chip title tooltip
     */
    public void insertVariable(String id, String label) {
        getElement().callJsFunction("insertVariable", id, label);
    }

    // ── Configuration ─────────────────────────────────────────────────────────

    /** Sets the grey placeholder text shown when the editor is empty. */
    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder", placeholder != null ? placeholder : "");
    }

    public String getPlaceholder() {
        return getElement().getProperty("placeholder", "");
    }

    /**
     * Overrides the minimum height of the content area (default: 260 px).
     *
     * @param px minimum height in pixels
     */
    public void setMinHeight(int px) {
        getElement().setProperty("minHeight", px);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        getElement().setProperty("readonly", readOnly);
    }

    // ── HasLabel ──────────────────────────────────────────────────────────────

    @Override
    public void setLabel(String label) {
        getElement().setProperty("label", label != null ? label : "");
    }

    @Override
    public String getLabel() {
        return getElement().getProperty("label", "");
    }

    // ── HasValidation ─────────────────────────────────────────────────────────

    @Override
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage", errorMessage != null ? errorMessage : "");
    }

    @Override
    public String getErrorMessage() {
        return getElement().getProperty("errorMessage", "");
    }

    @Override
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    @Override
    public boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }

    // ── Required indicator ────────────────────────────────────────────────────

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        super.setRequiredIndicatorVisible(visible);
        getElement().setProperty("required", visible);
    }

    // ── Sanitization ──────────────────────────────────────────────────────────

    /**
     * Returns the editor's current HTML content.
     *
     * <p>Equivalent to {@link #getValue()} — sanitization is applied automatically
     * in {@link #setModelValue} so every value stored in this field is already safe.</p>
     */
    public String getSanitizedValue() {
        return getValue();
    }

    /**
     * Sanitizes an arbitrary HTML string using the same policy applied by
     * {@link #getSanitizedValue()}.  Useful when sanitizing stored content
     * before displaying it outside the editor.
     */
    public static String sanitize(String html) {
        return RichTextSanitizer.sanitize(html);
    }
}
