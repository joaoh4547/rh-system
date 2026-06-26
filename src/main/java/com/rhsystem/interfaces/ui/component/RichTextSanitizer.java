package com.rhsystem.interfaces.ui.component;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

/**
 * Strict whitelist sanitizer for HTML produced by {@link RichTextEditor}.
 *
 * <p><strong>Security model:</strong> only elements and attributes
 * explicitly listed below are kept — everything else (including
 * {@code <script>}, {@code <iframe>}, {@code <object>}, event handlers,
 * {@code javascript:} URLs, arbitrary {@code style} values, etc.) is
 * stripped unconditionally.</p>
 *
 * <p>The CSS {@code style} attribute is accepted <em>only</em> on block elements
 * and only when its value is exactly {@code text-align: left|center|right|justify},
 * which is the only inline style Tiptap's TextAlign extension produces.</p>
 */
final class RichTextSanitizer {

    private RichTextSanitizer() {}

    /** Matches the only inline style Tiptap produces: {@code text-align: <value>}. */
    private static final Pattern TEXT_ALIGN =
            Pattern.compile("text-align\\s*:\\s*(left|right|center|justify)\\s*;?",
                    Pattern.CASE_INSENSITIVE);

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()

            // ── Block elements ────────────────────────────────────────────
            .allowElements("p", "h1", "h2", "h3",
                           "blockquote", "pre",
                           "ul", "ol", "li",
                           "hr", "br")

            // ── Inline formatting ─────────────────────────────────────────
            .allowElements("strong", "b", "em", "i", "u", "s", "code", "mark")

            // ── Links — http / https / mailto only ────────────────────────
            .allowElements("a")
            .allowStandardUrlProtocols()
            .allowAttributes("href").onElements("a")
            .allowAttributes("target")
                    .matching(Pattern.compile("_blank|_self"))
                    .onElements("a")
            .allowAttributes("rel")
                    .matching(Pattern.compile("[a-zA-Z ]+"))
                    .onElements("a")

            // ── Tables ────────────────────────────────────────────────────
            .allowElements("table", "thead", "tbody", "tfoot", "tr", "th", "td")
            .allowAttributes("colspan", "rowspan")
                    .matching(Pattern.compile("[0-9]+"))
                    .onElements("th", "td")

            // ── Merge-field chips: <span data-merge-field data-field-id="…"> ──
            // Only these specific attributes are whitelisted on <span>;
            // no other attribute (e.g. onclick, style) is allowed on span.
            .allowElements("span")
            .allowAttributes("data-merge-field", "data-field-id",
                             "data-field-label", "contenteditable", "class")
                    .onElements("span")

            // ── text-align only (strict regex, no other CSS) ──────────────
            .allowAttributes("style")
                    .matching(TEXT_ALIGN)
                    .onElements("p", "h1", "h2", "h3", "li", "td", "th")

            .toFactory();

    /**
     * Returns a sanitized copy of {@code html}, safe for persistence and display.
     * Returns an empty string when the input is {@code null} or blank.
     *
     * <p><strong>Order:</strong> HTML entities are decoded <em>first</em> so the OWASP
     * whitelist sanitizer sees the real HTML structure — {@code &lt;script&gt;} is
     * decoded to {@code <script>} and then stripped, instead of passing through as
     * visible text. {@code &amp;} is decoded last to prevent double-decoding sequences
     * such as {@code &amp;lt;}.</p>
     *
     * <p>After sanitization, {@code &gt;} is decoded back to {@code >} — HTML5 permits
     * literal {@code >} in text content and it is more readable in stored templates.</p>
     */
    static String sanitize(String html) {
        if (html == null || html.isBlank()) return "";
        // Decode entities before sanitizing so OWASP can see the real structure.
        // &amp; must be decoded last to avoid double-decoding (e.g. &amp;lt; → &lt; → <).
        String decoded = html
                .replace("&lt;",   "<")
                .replace("&gt;",   ">")
                .replace("&quot;", "\"")
                .replace("&#39;",  "'")
                .replace("&amp;",  "&");
        // Sanitize the decoded HTML; OWASP re-encodes > in text as &gt; — undo that.
        return POLICY.sanitize(decoded).replace("&gt;", ">");
    }
}
