package com.example.webhooksite.ui;

import com.example.webhooksite.model.Webhook;
import com.example.webhooksite.model.WebhookRequest;
import com.example.webhooksite.service.WebhookService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.details.Details;

@Route("")
@RouteAlias("list")
@PageTitle("WebhookSite - Inspect HTTP Requests")
public class MainView extends VerticalLayout {
    private final WebhookService webhookService;
    private final Grid<Webhook> webhookGrid = new Grid<>(Webhook.class);
    private final Grid<WebhookRequest> requestGrid = new Grid<>(WebhookRequest.class);

    public MainView(WebhookService webhookService) {
        this.webhookService = webhookService;
        setSizeFull();

        // Create webhook section
        H2 createTitle = new H2("Create New Webhook");
        TextField pathField = new TextField("Custom Path (optional)");
        Button createButton = new Button("Create Webhook", e -> createWebhook(pathField.getValue()));
        HorizontalLayout createLayout = new HorizontalLayout(pathField, createButton);

        // Webhooks grid
        webhookGrid.setColumns("path", "createdAt");
        webhookGrid.addItemClickListener(e -> showWebhookDetails(e.getItem()));
        webhookGrid.setHeight("300px");

        // Requests grid
        requestGrid.setColumns("method", "receivedAt");
        requestGrid.setHeight("300px");

        add(
            createTitle,
            createLayout,
            new H2("Your Webhooks"),
            webhookGrid
        );

        refreshWebhooks();
    }

    private void createWebhook(String path) {
        try {
            Webhook webhook = webhookService.createWebhook(path);
            Notification.show("Webhook created: /api/webhook/" + webhook.getPath());
            refreshWebhooks();
        } catch (IllegalArgumentException e) {
            Notification.show("Error: " + e.getMessage());
        }
    }

    private void showWebhookDetails(Webhook webhook) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        H2 title = new H2("Webhook Details: " + webhook.getPath());
        content.add(title);

        // Add requests grid
        Grid<WebhookRequest> requestsGrid = new Grid<>(WebhookRequest.class);
        requestsGrid.setItems(webhook.getRequests());
        requestsGrid.setColumns("method", "receivedAt");
        requestsGrid.addItemClickListener(e -> showRequestDetails(e.getItem()));

        content.add(requestsGrid);
        dialog.add(content);
        dialog.open();
    }

    private void showRequestDetails(WebhookRequest request) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        content.add(new H2("Request Details"));
        
        // Headers
        Details headers = new Details("Headers", 
            new VerticalLayout(
                request.getHeaders().entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .map(s -> new com.vaadin.flow.component.html.Paragraph(s))
                    .toArray(com.vaadin.flow.component.Component[]::new)
            )
        );
        
        // Body
        Details body = new Details("Body", 
            new com.vaadin.flow.component.html.Pre(request.getBody())
        );

        content.add(headers, body);
        dialog.add(content);
        dialog.open();
    }

    private void refreshWebhooks() {
        webhookGrid.setItems(webhookService.getAllWebhooks());
    }
}
