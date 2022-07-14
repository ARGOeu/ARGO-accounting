---
id: authenticating_clients
title: Authenticating Clients
---

To access Accounting System API resources, you have to be authenticated by EOSC Core Infrastructure proxy. 
These resources are protected and can only be accessed if a client is sending a bearer token along with the request, which must be valid and trusted by the Accounting System API.

The EOSC Core Infrastructure Proxy offers various Identity Providers where the authentication process can be performed. Once you log in to your preferable Identity Provider, you obtain an access token. Using this token, you can access the available API operations.

When passing in the access token in an HTTP header, you should make a request like the following:

```bash
curl http://localhost:8080/accounting-system/metric-definitions
   -H "Authorization: Bearer {token}"
```

There is an ancillary web page at `{accounting_system_host}` where you can identify yourself. 

![Login Page](assets/accounting_system_web_page.png)

This page is responsible for :
-   redirecting a user to EOSC Core Infrastructure Proxy in order to be authenticated
    ![Login Page](assets/aai_login_page.png)    

-   displaying the obtained token
    ![Login Page](assets/obtained_token.png)    
