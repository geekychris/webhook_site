package com.example.webhooksite.ui;

import com.example.webhooksite.model.Webhook;
import com.example.webhooksite.model.WebhookRequest;
import com.example.webhooksite.service.WebhookService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import org.atmosphere.cpr.AtmosphereResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.communication.PushMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Route(value = "webhook")
@AnonymousAllowed
@PageTitle("Webhook Details - WebhookSite")
public class WebhookDetailsView extends VerticalLayout implements HasUrlParameter<String> {
    private final WebhookService webhookService;
    private final Grid<WebhookRequest> requestGrid;
    private final VerticalLayout requestDetails;
    private final ObjectMapper objectMapper;
    private Webhook currentWebhook;
    private Registration broadcasterRegistration;

    public WebhookDetailsView(WebhookService webhookService) {
        this.webhookService = webhookService;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header with copy URL button
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setSpacing(true);
        header.setAlignItems(Alignment.CENTER);

        // Request list
        requestGrid = new Grid<>();
        requestGrid.addColumn(req -> req.getMethod())
                .setHeader("Method")
                .setWidth("100px");
        requestGrid.addColumn(req -> req.getReceivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setHeader("Time")
                .setWidth("200px");
        requestGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        // Request details
        requestDetails = new VerticalLayout();
        requestDetails.setSizeFull();
        requestDetails.setPadding(true);
        requestDetails.setSpacing(true);

        // Split layout
        SplitLayout splitLayout = new SplitLayout(requestGrid, requestDetails);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(30);

        add(header, splitLayout);

        // Setup push mode for real-time updates
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        requestGrid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(this::showRequestDetails);
        });
    }

    private void showRequestDetails(WebhookRequest request) {
        requestDetails.removeAll();

        // Method and time
        H3 methodTime = new H3(request.getMethod() + " - " + 
            request.getReceivedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        requestDetails.add(methodTime);

        // Headers
        Tabs tabs = new Tabs(
            new Tab("Headers"),
            new Tab("Body")
        );
        tabs.addSelectedChangeListener(event -> {
            requestDetails.remove(requestDetails.getComponentAt(2));
            if (event.getSelectedTab().getLabel().equals("Headers")) {
                requestDetails.add(createHeadersView(request));
            } else {
                requestDetails.add(createBodyView(request));
            }
        });

        requestDetails.add(tabs);
        requestDetails.add(createHeadersView(request));
    }

    private Component createHeadersView(WebhookRequest request) {
        Grid<Map.Entry<String, String>> headerGrid = new Grid<>();
        headerGrid.setItems(request.getHeaders().entrySet());
        
        headerGrid.addColumn(Map.Entry::getKey)
                .setHeader("Header")
                .setFlexGrow(1);
        headerGrid.addColumn(Map.Entry::getValue)
                .setHeader("Value")
                .setFlexGrow(2);

        return headerGrid;
    }

    private Component createBodyView(WebhookRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        try {
            // Try to parse and pretty print JSON
            Object json = objectMapper.readValue(request.getBody(), Object.class);
            String prettyJson = objectMapper.writeValueAsString(json);
            
            Pre pre = new Pre(prettyJson);
            pre.getStyle()
               .set("background-color", "var(--lumo-contrast-5pct)")
               .set("padding", "1em")
               .set("border-radius", "var(--lumo-border-radius)")
               .set("overflow", "auto")
               .set("max-width", "100%");
            
            layout.add(pre);
        } catch (Exception e) {
            // If not JSON, show raw body
            TextArea bodyArea = new TextArea();
            bodyArea.setValue(request.getBody());
            bodyArea.setReadOnly(true);
            bodyArea.setWidthFull();
            layout.add(bodyArea);
        }

        return layout;
    }

    @Override
    public void setParameter(BeforeEvent event, String path) {
        Optional<Webhook> webhook = webhookService.getWebhook(path);
        if (webhook.isPresent()) {
            currentWebhook = webhook.get();
            setupWebhook(currentWebhook);
        } else {
            Notification.show("Webhook not found", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("");
        }
    }

    private void setupWebhook(Webhook webhook) {
        // Update title
        H2 title = new H2("Webhook: " + webhook.getPath());
        
        // Add copy URL button
        Button copyButton = new Button(
            "Copy URL", 
            new Icon(VaadinIcon.COPY),
            e -> {
                String url = getUrl(webhook.getPath());
                UI.getCurrent().getPage().executeJs(
                    "navigator.clipboard.writeText($0)", url);
                Notification.show("URL copied to clipboard", 
                    2000, Notification.Position.MIDDLE);
            });

        HorizontalLayout header = (HorizontalLayout) getComponentAt(0);
        header.removeAll();
        header.add(title, copyButton);

        // Update request grid
        refreshRequests();

        // Setup push updates
        setupPushUpdates(webhook);
    }

    private void setupPushUpdates(Webhook webhook) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
        }

        broadcasterRegistration = webhookService.registerWebhookListener(
            webhook.getPath(),
            request -> getUI().ifPresent(ui -> 
                ui.access(() -> {
                    refreshRequests();
                    Notification.show("New webhook request received", 
                        2000, Notification.Position.BOTTOM_END);
                }))
        );
    }

    private void refreshRequests() {
        if (currentWebhook != null) {
            webhookService.getWebhook(currentWebhook.getPath())
                .ifPresent(webhook -> {
                    requestGrid.setItems(webhook.getRequests());
                    if (!webhook.getRequests().isEmpty()) {
                        requestGrid.select(webhook.getRequests().get(webhook.getRequests().size() - 1));
                    }
                });
        }
    }

    private String getUrl(String path) {
        VaadinServletRequest request = (VaadinServletRequest) VaadinService.getCurrentRequest();
        return request.getRequestURL().toString().replaceAll("/webhook/.*$", "") 
            + "/api/webhook/" + path;
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
        }
        super.onDetach(detachEvent);
    }
}
