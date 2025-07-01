# Webhook Site

A Spring Boot application similar to webhooks.site that allows you to create custom webhook endpoints and inspect incoming webhook requests. The application provides a Vaadin-based UI for creating and managing webhook endpoints, and viewing the received webhook payloads.

## System Design

### Architecture

The application is built using:
- Spring Boot 3.2.2
- Vaadin Flow 24.3.3 for the UI
- H2 Database for persistence
- JPA/Hibernate for data access

### Components

1. **Data Models**:
   - `Webhook`: Represents a webhook endpoint with a unique path
   - `WebhookRequest`: Stores webhook request details including headers and payload

2. **Core Components**:
   - `WebhookService`: Manages webhook creation and request handling
   - `WebhookRepository`: JPA repository for data persistence
   - `WebhookController`: REST controller for handling incoming webhook requests
   - `MainView`: Lists all webhook endpoints
   - `CreateWebhookView`: UI for creating new webhook endpoints
   - `WebhookDetailsView`: Split-panel view for inspecting webhook requests

### Database Schema

The application uses three main tables:
- `webhook`: Stores webhook endpoints
- `webhook_request`: Stores webhook requests
- `webhook_request_headers`: Stores headers for each webhook request

## Building and Running

### Prerequisites

- Java 21 or later
- Maven 3.8 or later

### Build

```bash
./mvnw clean install
```

### Run

```bash
./mvnw spring-boot:run
```

The application will be available at:
- Web UI: http://localhost:8082
- H2 Console: http://localhost:8082/h2-console (if needed)

## Usage

### Creating a Webhook

1. Open http://localhost:8082/create in your browser
2. Enter a custom path or click "Generate Random Path" for a word-pair based path (e.g., "cat-magnet")
3. Click "Create Webhook"
4. The webhook URL will be displayed and can be copied to clipboard

### Sending Webhook Requests

You can send webhook requests to your endpoint using any HTTP method. The base URL format is:
```
http://localhost:8082/api/webhook/{webhook-path}
```

#### Example Requests

1. Simple POST request:
```bash
curl -X POST "http://localhost:8082/api/webhook/test-webhook" \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, webhook!"}'
```

2. Request with custom headers:
```bash
curl -X POST "http://localhost:8082/api/webhook/test-webhook" \
  -H "Content-Type: application/json" \
  -H "X-Custom-Header: custom-value" \
  -H "Authorization: Bearer test-token" \
  -d '{"event": "user.created", "data": {"id": 123, "name": "Test User"}}'
```

3. Different HTTP methods:
```bash
# GET request
curl -X GET "http://localhost:8082/api/webhook/test-webhook?param=value"

# PUT request
curl -X PUT "http://localhost:8082/api/webhook/test-webhook" \
  -H "Content-Type: application/json" \
  -d '{"status": "updated"}'

# DELETE request
curl -X DELETE "http://localhost:8082/api/webhook/test-webhook"
```

### Viewing Webhook Requests

1. All webhook endpoints are listed on the main page
2. Click on a webhook to navigate to its details page (http://localhost:8082/webhook/your-path)
3. The details page shows:
   - Left panel: List of all requests with method and timestamp
   - Right panel: Detailed view of the selected request
     - Request method and timestamp
     - Headers tab: All request headers in a grid view
     - Body tab: Pretty-printed JSON if valid, raw text otherwise
4. Real-time updates:
   - New requests appear automatically in the left panel
   - Notification shown when new requests arrive
   - Latest request is automatically selected

## Features

- Create webhook endpoints with custom or word-pair based paths
- Support for all HTTP methods
- Modern split-panel interface for request inspection
- Pretty-printing of JSON payloads
- Real-time UI updates using Vaadin Push
- Copy webhook URL to clipboard
- Persistent storage using H2 database
- Separate pages for endpoint creation and request inspection

## Technical Notes

- The application uses an H2 file-based database located at `./webhookdb`
- Request bodies are stored with a maximum length of 10,000 characters
- CORS is enabled for all origins to allow testing from any domain
- The application runs in development mode by default

## Limitations

- No authentication/authorization
- No request payload validation
- Limited to single instance deployment (no load balancing)
- Request body size limited to 10,000 characters
