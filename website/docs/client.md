---
id: client
title: Client
---

As a client we define either a user or a service that communicates and interacts with the Accounting System API. 
In order for a client to be able to interact with the API, first it has to register itself into the Accounting System. Based on the Client **_voperson_id_** the Accounting System API assigns different Roles to different Clients.
Consequently, a client cannot get a Role unless registration has been completed first.

### [POST] - Client Registration

One client can register itself into the Accounting System by executing the following request:

```
POST /accounting-system/clients

Authorization: Bearer {token}
```

Once the above request is executed, we extract the following information from the token:

- voperson_id
- name
- email

Then we store it into the database collection Client:

| Field          	| Description   	                      | 
|------------------	|---------------------------------------- |
| voperson_id             	| An identifier for the client which is globally unique and not reassignable. |
| name       	| The client's full name. |
| email      	    | The client’s email. |

### [GET] - Read the registered Clients

You can read the registered clients by executing the following request:

```
GET /accounting-system/clients

Authorization: Bearer {token}
```

### Errors

Please refer to section [Errors](./api_errors) to see all possible Errors.