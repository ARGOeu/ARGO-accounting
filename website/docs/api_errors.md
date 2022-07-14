---
id: api_errors
title: API Errors
---

## Errors

In case of Error during handling user’s request the API responds using the following schema:

```json
{
  "code": 500,
  "message": "Internal Server Error"
}
```
## Error Codes

The following error codes are the possible errors of all methods

Error | Code | Status | Related Requests
------|------|----------|------------------
Bad Request | 400 | BAD_REQUEST | All POST, PATCH, and PUT requests
Unauthorized | 401 | UNAUTHORIZED | All requests _(if a user is not authenticated)_
Forbidden Access to Resource  | 403 | FORBIDDEN | All requests _(if a user is forbidden to access the resource)_
Entity Not Found | 404 | NOT_FOUND | All GET requests
Entity already exists | 409 | CONFLICT | All POST, PATCH, and PUT requests
Cannot consume content type | 415 | UNSUPPORTED_MEDIA_TYPE | All POST, PATCH, and PUT requests
Internal Server Error | 500 | INTERNAL_SERVER_ERROR | All requests