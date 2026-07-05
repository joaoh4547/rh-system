package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import lombok.Getter;

/**
 * A Vaadin component that renders a Lucide icon using React.
 */
@Getter
@Tag("div")
@JsModule("./components/lucide-icon-renderer.tsx")
@NpmPackage(value = "lucide-react", version = "1.22.0")
public class LucideIcon extends Component implements HasStyle, HasSize {

    private String iconName;

    public LucideIcon(String iconName) {
        this.iconName = iconName;
        // Mirrors the box vaadin-icon actually gets inside a Lumo button (see
        // @vaadin/vaadin-lumo-styles button.css: width/height 1.5em + 0.25em
        // padding), since that CSS only targets the "vaadin-icon" tag and
        // doesn't apply to this component. Without the matching padding, the
        // button's compensating "-0.25em" prefix margin pushes it off-center.
        getStyle().set("display", "inline-flex");
        getStyle().set("align-items", "center");
        getStyle().set("justify-content", "center");
        getStyle().set("flex", "none");
        getStyle().set("box-sizing", "border-box");
        getStyle().set("padding", "0.25em");
        setWidth("calc(var(--lumo-icon-size-m) * 1.15)");
        setHeight("calc(var(--lumo-icon-size-m) * 1.15)");
        getElement().executeJs("window.LucideIcons.render($0, $1, {})", iconName, getElement());
    }

    public void setIcon(String iconName) {
        this.iconName = iconName;
        getElement().executeJs("window.LucideIcons.render($0, $1, {})", iconName, getElement());
    }


    /**
     * Creates a new "edit" icon instance. Each call returns a fresh component.
     */
    public static LucideIcon edit() {
        return new LucideIcon("SquarePen");
    }

    /**
     * Creates a new "delete" icon instance. Each call returns a fresh component.
     */
    public static LucideIcon delete() {
        return new LucideIcon("Trash2");
    }

    public static LucideIcon add() {
        return new LucideIcon("CirclePlus");
    }

    public static LucideIcon functionalities(){
        return new LucideIcon("Layers");
    }

    public static LucideIcon check(){
        return new LucideIcon("Check");
    }

    public static LucideIcon lock(){
        return new LucideIcon("Lock");
    }

    public static LucideIcon unLock(){
        return new LucideIcon("LockOpen");
    }

    public static LucideIcon chevronRight(){
        return new LucideIcon("ChevronRight");
    }

    public static LucideIcon chevronLeft(){
        return new LucideIcon("ChevronLeft");
    }

    public static LucideIcon chevronsRight(){
        return new LucideIcon("ChevronsRight");
    }

    public static LucideIcon chevronsLeft(){
        return new LucideIcon("ChevronsLeft");
    }
}
