package com.rhsystem.interfaces.ui.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RichTextSanitizerTest {

    @Test
    void nullOrBlankInputBecomesEmptyString() {
        assertEquals("", RichTextSanitizer.sanitize(null));
        assertEquals("", RichTextSanitizer.sanitize("   "));
    }

    @Test
    void keepsWhitelistedFormattingElements() {
        String html = "<p><strong>negrito</strong> e <em>itálico</em></p><ul><li>item</li></ul>";
        String out = RichTextSanitizer.sanitize(html);
        assertTrue(out.contains("<strong>negrito</strong>"));
        assertTrue(out.contains("<em>itálico</em>"));
        assertTrue(out.contains("<li>item</li>"));
    }

    @Test
    void stripsScriptTags() {
        String out = RichTextSanitizer.sanitize("<p>ok</p><script>alert('xss')</script>");
        assertFalse(out.contains("<script"));
        assertFalse(out.contains("alert"));
        assertTrue(out.contains("<p>ok</p>"));
    }

    @Test
    void stripsEncodedScriptInsteadOfLettingItThrough() {
        // Entidades são decodificadas ANTES da sanitização
        String out = RichTextSanitizer.sanitize("&lt;script&gt;alert(1)&lt;/script&gt;<p>x</p>");
        assertFalse(out.contains("<script"));
        assertFalse(out.contains("alert"));
    }

    @Test
    void stripsEventHandlersAndIframes() {
        String out = RichTextSanitizer.sanitize(
                "<p onclick=\"evil()\">x</p><iframe src=\"https://evil\"></iframe>");
        assertFalse(out.contains("onclick"));
        assertFalse(out.contains("<iframe"));
        assertTrue(out.contains("x"));
    }

    @Test
    void blocksJavascriptUrls() {
        String out = RichTextSanitizer.sanitize("<a href=\"javascript:alert(1)\">link</a>");
        assertFalse(out.contains("javascript:"));
    }

    @Test
    void keepsHttpsLinks() {
        String out = RichTextSanitizer.sanitize("<a href=\"https://example.com\">link</a>");
        assertTrue(out.contains("https://example.com"));
    }

    @Test
    void allowsOnlyTextAlignStyle() {
        String aligned = RichTextSanitizer.sanitize("<p style=\"text-align: center\">x</p>");
        assertTrue(aligned.contains("text-align"));

        String colored = RichTextSanitizer.sanitize("<p style=\"color: red\">x</p>");
        assertFalse(colored.contains("color"));
    }

    @Test
    void keepsTablesWithNumericSpansOnly() {
        String out = RichTextSanitizer.sanitize(
                "<table><tr><td colspan=\"2\">a</td><td colspan=\"x\">b</td></tr></table>");
        assertTrue(out.contains("colspan=\"2\""));
        assertFalse(out.contains("colspan=\"x\""));
    }
}
