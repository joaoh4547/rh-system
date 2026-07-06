package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.Span;

import java.time.Year;

/**
 * Application footer rendered at the bottom of the drawer.
 *
 * <p>Shows the current year, the serving instance address (IP/hostname, useful
 * to locate logs when running behind the load balancer) and the live
 * {@link SessionTimer}.</p>
 */
public class AppFooter extends Footer {

    public AppFooter(String serverAddress, SessionTimer sessionTimer) {
        addClassName("app-footer");

        Span copyright = new Span(getTranslation("footer.copyright", Year.now().getValue()));
        copyright.addClassName("footer-copyright");

        Span server = new Span(getTranslation("footer.server", serverAddress));
        server.addClassName("footer-server");

        Div info = new Div(copyright, server);
        info.addClassName("footer-info");

        add(info, sessionTimer);
    }
}
