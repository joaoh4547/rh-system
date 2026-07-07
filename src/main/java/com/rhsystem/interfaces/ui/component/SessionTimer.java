package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Live session countdown displayed in the footer.
 *
 * <p>The countdown is driven <b>client-side</b> for a smooth per-second display
 * and reset on real user activity — clicks, keystrokes, scroll and touch. Bare
 * mouse movement is intentionally ignored (so the timer keeps ticking while the
 * pointer merely hovers), and interactions <i>inside</i> a dialog overlay are
 * ignored too (so closing the warning doesn't count as activity). It calls back
 * to the server at these moments:</p>
 * <ul>
 *   <li>{@link #keepAlive()} — throttled (once per minute) on activity, so the
 *       HTTP session on the server is refreshed while the user is active;</li>
 *   <li>{@link #showWarning()} — when only {@code warningMinutes} remain, opening
 *       a <b>non-modal</b> warning. "Continuar" resets the timer; "Fechar"/Esc just
 *       dismisses it and lets the countdown continue to expiration;</li>
 *   <li>{@link #closeWarning()} — when the user resumes real work, the pending
 *       warning is dismissed and the timer resets;</li>
 *   <li>{@link #expire()} — when the countdown reaches zero, opening a blocking
 *       dialog whose only action logs the user out.</li>
 * </ul>
 *
 * <p>Why the warning is <b>non-modal</b>: a modal dialog makes the rest of the UI
 * inert on the server, which would swallow this component's {@code expire()} RPC —
 * the session would hit zero and nothing would happen. Keeping it non-modal lets
 * the countdown keep driving the server. The server-side session timeout
 * ({@code server.servlet.session.timeout}) is the authoritative backstop.</p>
 */
public class SessionTimer extends Span {

    private static final String TIMER_JS = """
            const el = this;
            const total = $0, warnAt = $1;
            let remaining = total, warned = false;
            const valueEl = el.querySelector('.session-timer-value');
            const fmt = s => { s = s > 0 ? s : 0; const m = Math.floor(s / 60), ss = s % 60;
                return (m < 10 ? '0' : '') + m + ':' + (ss < 10 ? '0' : '') + ss; };
            const render = () => { if (valueEl) valueEl.textContent = fmt(remaining); };
            const tick = () => {
                remaining--;
                if (remaining <= warnAt && !warned) { warned = true; el.$server.showWarning(); }
                if (remaining <= 0) {
                    clearInterval(el.__sessionInterval); el.__sessionInterval = null;
                    render(); el.$server.expire(); return;
                }
                render();
            };
            const startTick = () => {
                if (el.__sessionInterval) clearInterval(el.__sessionInterval);
                el.__sessionInterval = setInterval(tick, 1000);
            };
            // Reset chamado pelo servidor (botão "Continuar"): volta ao tempo cheio
            // e RELIGA a contagem (o intervalo pode ter sido parado ao zerar).
            el.__sessionReset = () => { remaining = total; warned = false; render(); startTick(); };
            let lastPing = Date.now();
            // Ignora eventos originados dentro de um dialog (botões do aviso/expiração),
            // para que "Fechar" não conte como atividade e não reinicie o timer.
            // Usa composedPath() para enxergar através do shadow DOM/slot do overlay
            // (closest() não atravessa essa fronteira e falharia nos botões do dialog).
            const inOverlay = e => {
                const path = (e && e.composedPath) ? e.composedPath() : [];
                return path.some(n => n && n.nodeType === 1 && n.localName
                    && n.localName.endsWith('-overlay'));
            };
            const onActivity = e => {
                if (inOverlay(e)) return;
                const wasWarned = warned;
                remaining = total; warned = false; render(); startTick();
                if (wasWarned) el.$server.closeWarning();
                const now = Date.now();
                if (now - lastPing > 60000) { lastPing = now; el.$server.keepAlive(); }
            };
            const events = ['mousedown', 'keydown', 'scroll', 'touchstart'];
            events.forEach(ev => document.addEventListener(ev, onActivity, { passive: true }));
            startTick();
            el.__sessionCleanup = () => {
                if (el.__sessionInterval) clearInterval(el.__sessionInterval);
                events.forEach(ev => document.removeEventListener(ev, onActivity));
            };
            render();
            """;

    private final int timeoutSeconds;
    private final int warningSeconds;
    private final Runnable onLogout;

    /** The current, non-modal warning dialog (if showing) so it can be closed later. */
    private Dialog warningDialog;

    public SessionTimer(int timeoutMinutes, int warningMinutes, Runnable onLogout) {
        this.timeoutSeconds = timeoutMinutes * 60;
        this.warningSeconds = warningMinutes * 60;
        this.onLogout = onLogout;

        addClassName("session-timer");

        Span icon = new Span(VaadinIcon.CLOCK.create());
        icon.addClassName("session-timer-icon");

        Span label = new Span(getTranslation("session.timer.label"));
        label.addClassName("session-timer-label");

        Span value = new Span(format(timeoutSeconds));
        value.addClassName("session-timer-value");

        add(icon, label, value);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getElement().executeJs(TIMER_JS, timeoutSeconds, warningSeconds);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        getElement().executeJs("if (this.__sessionCleanup) this.__sessionCleanup();");
        super.onDetach(detachEvent);
    }

    /** No-op: the round-trip itself refreshes the server-side HTTP session. */
    @ClientCallable
    public void keepAlive() {
        // intentionally empty
    }

    /** Opens the non-modal "about to expire" warning. */
    @ClientCallable
    public void showWarning() {
        if (warningDialog != null && warningDialog.isOpened()) {
            return;
        }
        Dialog dialog = new Dialog();
        warningDialog = dialog;
        dialog.setHeaderTitle(getTranslation("session.warning.title"));
        // Não-modal: não bloqueia o SessionTimer, senão o expire() nunca é processado.
        dialog.setModal(false);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.add(new Span(getTranslation("session.warning.message", warningSeconds / 60)));

        // "Fechar"/Esc apenas dispensa o aviso; a contagem continua até expirar.
        Button close = new Button(getTranslation("session.warning.cancel"), e -> dialog.close());

        // "Continuar conectado" reinicia o timer para o tempo cheio.
        Button stay = new Button(getTranslation("session.warning.confirm"), e -> {
            dialog.close();
            getElement().executeJs("if (this.__sessionReset) this.__sessionReset();");
        });
        stay.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(close, stay);
        dialog.open();
    }

    /** Dismisses the pending warning when the user resumes activity. */
    @ClientCallable
    public void closeWarning() {
        if (warningDialog != null) {
            warningDialog.close();
            warningDialog = null;
        }
    }

    /** Opens the blocking "session expired" dialog; the only action logs out. */
    @ClientCallable
    public void expire() {
        if (warningDialog != null) {
            warningDialog.close();
            warningDialog = null;
        }
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("session.expired.title"));
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.add(new Span(getTranslation("session.expired.message")));

        Button confirm = new Button(getTranslation("session.expired.confirm"), e -> {
            dialog.close();
            onLogout.run();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(confirm);

        dialog.open();
    }

    private static String format(int totalSeconds) {
        int safe = Math.max(totalSeconds, 0);
        return String.format("%02d:%02d", safe / 60, safe % 60);
    }
}
