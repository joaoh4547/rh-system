package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Live session countdown displayed in the footer.
 *
 * <p>The countdown is driven <b>client-side</b> for a smooth per-second display
 * and reset on any user activity (mouse, keyboard, scroll, touch). It calls back
 * to the server at three moments:</p>
 * <ul>
 *   <li>{@link #keepAlive()} — throttled (once per minute) on activity, so the
 *       HTTP session on the server is refreshed while the user is active;</li>
 *   <li>{@link #showWarning()} — when only {@code warningMinutes} remain, opening
 *       a confirmation dialog that resets the timer if the user chooses to stay;</li>
 *   <li>{@link #expire()} — when the countdown reaches zero, opening a blocking
 *       dialog whose only action logs the user out.</li>
 * </ul>
 *
 * <p>The server-side session timeout ({@code server.servlet.session.timeout}) acts
 * as the authoritative backstop; this component provides the UX around it.</p>
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
            el.__sessionReset = () => { remaining = total; warned = false; render(); };
            let lastPing = Date.now();
            const onActivity = () => {
                remaining = total; warned = false; render();
                const now = Date.now();
                if (now - lastPing > 60000) { lastPing = now; el.$server.keepAlive(); }
            };
            const events = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'click'];
            events.forEach(ev => document.addEventListener(ev, onActivity, { passive: true }));
            if (el.__sessionInterval) clearInterval(el.__sessionInterval);
            el.__sessionInterval = setInterval(() => {
                remaining--;
                if (remaining <= warnAt && !warned) { warned = true; el.$server.showWarning(); }
                if (remaining <= 0) { clearInterval(el.__sessionInterval); render(); el.$server.expire(); return; }
                render();
            }, 1000);
            el.__sessionCleanup = () => {
                if (el.__sessionInterval) clearInterval(el.__sessionInterval);
                events.forEach(ev => document.removeEventListener(ev, onActivity));
            };
            render();
            """;

    private final int timeoutSeconds;
    private final int warningSeconds;
    private final Runnable onLogout;

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

    /** Opens the "about to expire" dialog; confirming restarts the client countdown. */
    @ClientCallable
    public void showWarning() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(getTranslation("session.warning.title"));
        dialog.setText(getTranslation("session.warning.message", warningSeconds / 60));
        dialog.setConfirmText(getTranslation("session.warning.confirm"));
        dialog.setCloseOnEsc(false);
        dialog.addConfirmListener(e -> getElement().executeJs("if (this.__sessionReset) this.__sessionReset();"));
        dialog.open();
    }

    /** Opens the blocking "session expired" dialog; the only action logs out. */
    @ClientCallable
    public void expire() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("session.expired.title"));
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
