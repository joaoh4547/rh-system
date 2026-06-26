package com.rhsystem.interfaces.ui.editor;

import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.RichTextEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "editor-demo", layout = MainLayout.class)
@PageTitle("Editor de Texto — Demo")
@PermitAll
public class RichTextEditorDemoPage extends VerticalLayout {

    private static final List<RichTextEditor.Variable> DEMO_VARIABLES = List.of(
            RichTextEditor.Variable.of("nome_completo",   "Nome Completo",    "Colaborador"),
            RichTextEditor.Variable.of("primeiro_nome",   "Primeiro Nome",    "Colaborador"),
            RichTextEditor.Variable.of("matricula",       "Matrícula",        "Colaborador"),
            RichTextEditor.Variable.of("cargo",           "Cargo",            "Colaborador"),
            RichTextEditor.Variable.of("departamento",    "Departamento",     "Colaborador"),
            RichTextEditor.Variable.of("data_admissao",   "Data de Admissão", "Colaborador"),
            RichTextEditor.Variable.of("empresa",         "Empresa",          "Empresa"),
            RichTextEditor.Variable.of("cnpj",            "CNPJ",             "Empresa"),
            RichTextEditor.Variable.of("endereco",        "Endereço",         "Empresa"),
            RichTextEditor.Variable.of("data_hoje",       "Data de Hoje",     "Datas"),
            RichTextEditor.Variable.of("mes_referencia",  "Mês de Referência","Datas")
    );

    private static final String DEMO_CONTENT = """
            <h2>Carta de Apresentação</h2>
            <p>Prezado(a) <span data-merge-field data-field-id="nome_completo" data-field-label="Nome Completo" class="merge-field" contenteditable="false">{{nome_completo}}</span>,</p>
            <p>Vimos por meio desta informar que, a partir desta data, o(a) colaborador(a) acima
            identificado(a) está formalmente vinculado(a) ao quadro de funcionários da empresa
            <span data-merge-field data-field-id="empresa" data-field-label="Empresa" class="merge-field" contenteditable="false">{{empresa}}</span>,
            no cargo de <span data-merge-field data-field-id="cargo" data-field-label="Cargo" class="merge-field" contenteditable="false">{{cargo}}</span>.</p>
            <h3>Detalhes do contrato</h3>
            <ul>
              <li><strong>Matrícula:</strong> <span data-merge-field data-field-id="matricula" data-field-label="Matrícula" class="merge-field" contenteditable="false">{{matricula}}</span></li>
              <li><strong>Departamento:</strong> <span data-merge-field data-field-id="departamento" data-field-label="Departamento" class="merge-field" contenteditable="false">{{departamento}}</span></li>
              <li><strong>Data de admissão:</strong> <span data-merge-field data-field-id="data_admissao" data-field-label="Data de Admissão" class="merge-field" contenteditable="false">{{data_admissao}}</span></li>
            </ul>
            <p>Emitido em <span data-merge-field data-field-id="data_hoje" data-field-label="Data de Hoje" class="merge-field" contenteditable="false">{{data_hoje}}</span>.</p>
            <p>Atenciosamente,<br><strong>Recursos Humanos</strong></p>
            """;

    public RichTextEditorDemoPage() {
        addClassName("view");
        setPadding(true);
        setSpacing(true);

        // ── Cabeçalho ─────────────────────────────────────────────────────────
        var title = new H2("Editor de Texto Rico");
        title.getStyle().set("margin", "0");
        var subtitle = new Span("Demonstração do componente RichTextEditor com Tiptap");
        subtitle.addClassName("page-subtitle");
        var header = new Div(title, subtitle);
        header.addClassName("page-header");

        // ── Editor ────────────────────────────────────────────────────────────
        var editor = new RichTextEditor();
        editor.setVariables(DEMO_VARIABLES);
        editor.setMinHeight(320);
        editor.setValue(DEMO_CONTENT);

        var editorCard = card("Documento", editor);

        // ── Controles externos ────────────────────────────────────────────────
        var varSelect = new Select<RichTextEditor.Variable>();
        varSelect.setLabel("Inserir variável via Java");
        varSelect.setItems(DEMO_VARIABLES);
        varSelect.setItemLabelGenerator(RichTextEditor.Variable::label);
        varSelect.setPlaceholder("Selecione...");
        varSelect.setWidth("240px");

        var btnInsert = new Button("Inserir", VaadinIcon.PLUS.create(), e -> {
            var v = varSelect.getValue();
            if (v != null) {
                editor.insertVariable(v.id(), v.label());
                varSelect.clear();
            }
        });
        btnInsert.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        var btnReadonly = new Button("Somente leitura", VaadinIcon.LOCK.create());
        btnReadonly.addThemeVariants(ButtonVariant.LUMO_SMALL);
        final boolean[] readonlyState = {false};
        btnReadonly.addClickListener(e -> {
            readonlyState[0] = !readonlyState[0];
            editor.setReadOnly(readonlyState[0]);
            btnReadonly.setText(readonlyState[0] ? "Editar" : "Somente leitura");
            btnReadonly.setIcon(readonlyState[0] ? VaadinIcon.EDIT.create() : VaadinIcon.LOCK.create());
        });

        var btnClear = new Button("Limpar", VaadinIcon.TRASH.create(), e -> editor.setValue(""));
        btnClear.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        var btnSave = new Button("Salvar (ver HTML)", VaadinIcon.DOWNLOAD.create(), e -> {
            var notif = Notification.show("Conteúdo capturado — veja o painel abaixo.", 3000, Notification.Position.TOP_END);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        btnSave.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        var toolbar = new HorizontalLayout(varSelect, btnInsert, btnReadonly, btnClear, btnSave);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.getStyle().set("flex-wrap", "wrap");

        var controlsCard = card("Controles externos (API Java)", toolbar);

        // ── Saída HTML ────────────────────────────────────────────────────────
        var htmlOutput = new Pre();
        htmlOutput.getStyle()
                .set("font-size", "12px")
                .set("background", "#1e293b")
                .set("color", "#e2e8f0")
                .set("padding", "12px")
                .set("border-radius", "6px")
                .set("overflow-x", "auto")
                .set("max-height", "260px")
                .set("white-space", "pre-wrap")
                .set("word-break", "break-all")
                .set("margin", "0");

        htmlOutput.setText(decodeForDisplay(editor.getValue()));

        editor.addValueChangeListener(e -> htmlOutput.setText(decodeForDisplay(e.getValue())));

        var htmlCard = card("HTML gerado (atualiza em tempo real)", htmlOutput);

        // ── Info sobre variáveis ──────────────────────────────────────────────
        var infoText = new Paragraph(
                "As variáveis inseridas no editor são armazenadas como " +
                "<span data-merge-field> no HTML e podem ser substituídas no back-end " +
                "antes da renderização/impressão do documento.");
        infoText.getStyle().set("font-size", "13px").set("color", "#64748b").set("margin", "0");

        var varsTitle = new H4("Variáveis disponíveis");
        varsTitle.getStyle().set("margin", "0 0 8px 0").set("font-size", "14px");

        var varChips = new Div();
        varChips.getStyle().set("display", "flex").set("flex-wrap", "wrap").set("gap", "6px");
        for (var v : DEMO_VARIABLES) {
            var chip = new Span("{{" + v.id() + "}}");
            chip.getStyle()
                    .set("background", "#eff6ff")
                    .set("border", "1px solid #93c5fd")
                    .set("border-radius", "999px")
                    .set("padding", "2px 10px")
                    .set("font-size", "12px")
                    .set("font-family", "monospace")
                    .set("color", "#1d4ed8")
                    .set("font-weight", "700");
            var tooltip = new Span(" " + v.label());
            tooltip.getStyle().set("font-size", "11px").set("color", "#64748b");
            var row = new Div(chip, tooltip);
            row.getStyle().set("display", "flex").set("align-items", "center").set("gap", "4px");
            varChips.add(row);
        }

        var infoCard = card("Referência", new Div(infoText, varsTitle, varChips));

        add(header, editorCard, controlsCard, htmlCard, infoCard);
        setMaxWidth("960px");
    }

    /**
     * Decodes HTML entities so the Pre panel shows human-readable characters.
     * Safe here because {@code Pre.setText()} writes via {@code textContent}
     * (not {@code innerHTML}), so the decoded {@code <} and {@code >} are never
     * parsed as tags by the browser.
     */
    private static String decodeForDisplay(String html) {
        if (html == null || html.isBlank()) return html == null ? "" : html;
        return html
                .replace("&lt;",   "<")
                .replace("&gt;",   ">")
                .replace("&quot;", "\"")
                .replace("&#39;",  "'")
                .replace("&amp;",  "&");   // must be last
    }

    private static Div card(String title, com.vaadin.flow.component.Component content) {
        var titleSpan = new Span(title);
        titleSpan.addClassName("card-title");
        var toolbar = new Div(titleSpan);
        toolbar.addClassName("card-toolbar");

        var card = new Div(toolbar, content);
        card.addClassName("card");
        card.setWidthFull();
        return card;
    }
}