#Centralized REST API documenation using SpringDoc's built-in multi-service aggregation, which fetches API from different technologies 

```
┌─────────────────────────────────────────────────┐
│   Centralized Documentation Portal             │
│   https://api.company.com/docs                  │
│                                                 │
│   Spring Boot Aggregator Service                │
│   - Fetches specs via HTTP                     │
│   - Merges into single OpenAPI doc              │
│   - Serves Swagger UI                          │
└────────────┬────────────────────────────────────┘
             │
    ┌────────┴────────┬──────────────┬────────────┐
    │                 │              │            │
    ↓                 ↓              ↓            ↓
┌─────────┐     ┌──────────┐   ┌─────────┐  ┌─────────┐
│Service 1│     │Service 2 │   │Service 3│  │Service N│
│         │     │          │   │         │  │         │
│Spring   │     │Spring 5.3│   │Jakarta  │  │Any Tech │
│Boot     │     │+ JAX-RS  │   │EE 10    │  │         │
│         │     │2.2       │   │         │  │         │
│/v3/api- │     │/openapi  │   │/openapi │  │/openapi │
│docs     │     │          │   │         │  │         │
└─────────┘     └──────────┘   └─────────┘  └─────────┘
```
