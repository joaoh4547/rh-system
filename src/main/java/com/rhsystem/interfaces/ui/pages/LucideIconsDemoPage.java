package com.rhsystem.interfaces.ui.pages;

import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "lucide-demo", layout = MainLayout.class)
@PageTitle("Lucide Icons Demo")
@PermitAll
public class LucideIconsDemoPage extends VerticalLayout {

    public LucideIconsDemoPage() {
        setPadding(true);
        setSpacing(true);

        add(new H2("Lucide Icons Integration Demo"));
        add(new Span("Estes ícones estão sendo renderizados via React (lucide-react) dentro do Vaadin."));

        HorizontalLayout iconRow = new HorizontalLayout();
        iconRow.setSpacing(true);
        iconRow.setAlignItems(Alignment.CENTER);

        iconRow.add(createIconWithLabel("User", "User"));
        iconRow.add(createIconWithLabel("Settings", "Settings"));
        iconRow.add(createIconWithLabel("Home", "Home"));
        iconRow.add(createIconWithLabel("Mail", "Mail"));
        iconRow.add(createIconWithLabel("Calendar", "Calendar"));
        iconRow.add(createIconWithLabel("Trash2", "Trash2"));
        iconRow.add(createIconWithLabel("Search", "Search"));
        iconRow.add(createIconWithLabel("CheckCircle", "CheckCircle"));

        add(iconRow);
    }

    private VerticalLayout createIconWithLabel(String iconName, String label) {
        LucideIcon icon = new LucideIcon(iconName);
        icon.getStyle().set("color", "var(--lumo-primary-color)");
        
        VerticalLayout layout = new VerticalLayout(icon, new Span(label));
        layout.setAlignItems(Alignment.CENTER);
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setWidth("100px");
        return layout;
    }
}
