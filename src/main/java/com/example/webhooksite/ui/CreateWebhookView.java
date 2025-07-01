package com.example.webhooksite.ui;

import com.example.webhooksite.model.Webhook;
import com.example.webhooksite.service.EndpointNameGenerator;
import com.example.webhooksite.service.WebhookService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("create")
@PageTitle("Create Webhook - WebhookSite")
public class CreateWebhookView extends VerticalLayout {
    private final WebhookService webhookService;
    private final EndpointNameGenerator nameGenerator;
    private final TextField pathField;

    public CreateWebhookView(WebhookService webhookService, EndpointNameGenerator nameGenerator) {
        this.webhookService = webhookService;
        this.nameGenerator = nameGenerator;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        add(new H2("Create New Webhook"));

        pathField = new TextField("Endpoint Path");
        pathField.setWidth("300px");
        pathField.setPlaceholder("Enter custom path or generate one");

        Button generateButton = new Button("Generate Random Path", e -> {
            pathField.setValue(nameGenerator.generateEndpointName());
        });
        generateButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button createButton = new Button("Create Webhook", e -> createWebhook());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(generateButton, createButton);
        buttonLayout.setSpacing(true);

        VerticalLayout formLayout = new VerticalLayout(pathField, buttonLayout);
        formLayout.setAlignItems(Alignment.CENTER);
        formLayout.setSpacing(true);
        formLayout.setPadding(true);

        add(formLayout);
    }

    private void createWebhook() {
        try {
            Webhook webhook = webhookService.createWebhook(pathField.getValue());
            String url = getUrl(webhook.getPath());
            Notification.show("Webhook created: " + url, 5000, Notification.Position.MIDDLE);
            pathField.clear();
            getUI().ifPresent(ui -> ui.navigate("webhook/" + webhook.getPath()));
        } catch (IllegalArgumentException e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private String getUrl(String path) {
        VaadinServletRequest request = (VaadinServletRequest) VaadinService.getCurrentRequest();
        return request.getRequestURL().toString().replaceAll("/create$", "") 
            + "/api/webhook/" + path;
    }
}
