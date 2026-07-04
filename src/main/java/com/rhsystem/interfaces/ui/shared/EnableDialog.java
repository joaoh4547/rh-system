package com.rhsystem.interfaces.ui.shared;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.function.Function;

public class EnableDialog<T> extends ConfirmDialog {

    public EnableDialog(T object, EnableAction<T> enableAction,
                        Function<T, String> entityText, String entityName, String entityArticle, boolean enable) {


        setHeader(enable ? getTranslation("enable.entity.title", entityName) : getTranslation("disable.entity.title", entityName));

        String message = null;

        if (enable) {
            message = getTranslation("enable.entity.message", entityArticle, entityName, entityText.apply(object));
        } else {
            message = getTranslation("disable.entity.message", entityArticle, entityName, entityText.apply(object));
        }
        setText(message);

        setCancelable(true);

        addConfirmListener(e -> {
            enableAction.perform(object);
        });

        setConfirmText(getTranslation("actions.confirm"));
        setCancelText(getTranslation("actions.cancel"));
    }


    @FunctionalInterface
    public interface EnableAction<T> {

        void perform(T obj);

    }

}
