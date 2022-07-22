---
id: metric
title: Metric
---

In order to register a new Metric in the Accounting System, first you have to complete the following steps:

- Create a new Metric Definition
- Register your Project
- Correlate Providers with the registered Project
- Use a Provider from EOSC-Portal or create a new Provider through AS API
- Create a new Installation

Once you have completed the above steps, you can submit a new metric in the Accounting System.

The Metric consists of the following attributes:

| Field          	| Description   	                      | 
|------------------	|---------------------------------------- |
| id             	| Unique ID of the Metric             |
| metric_definition_id       	| Reference Id from the metric definition |
| time_period_start      	    | Timestamp of the starting date time (Zulu timestamp) |
| time_period_end      	|Timestamp of the end date time (Zulu timestamp)|
| value      	| Value of the metric for the given period (decimal value) |

### [POST] - Create a new Metric

You can create a new Metric by executing the following POST request:

```
POST /accounting-system/installations/{installation_id}/metrics

Content-type: application/json
Authorization: Bearer {token}

{
"metric_definition_id" : "62973fea0f41a20c683e9014",
"time_period_start": "2020-12-20T09:13:07Z",
"time_period_end": "2020-12-25T11:14:07Z",  
"value": 700
}
```

Upon inserting the record into the database, the API returns the Metric enhanced with the metric id :

Success Response `201 CREATED`

```
{
    "metric_id": "72983kea0h41a20c683e3589",
    "metric_definition_id": "62973fea0f41a20c683e9014",
    "time_period_start": "2020-12-20T09:13:07Z",
    "time_period_end": "2020-12-25T11:14:07Z",
    "value": 700
}
```


### [DELETE] - Delete an existing Metric

The client should be able to delete an existing Metric. As a result, the Accounting System API has to offer an operation deleting the Metric that the client wants.
Subsequently, the registered Metric can be deleted by executing the following request:

```
DELETE /accounting-system/installations/{installation_id}/metrics/{metric_id}

Authorization: Bearer {token}
```

Upon the successful deletion, the API will return the following informative response:

Success Response `200 OK`

```
{
   "code": 200,
   "message: "The Metric has been deleted successfully." 
}
```

### [PATCH] - Update an existing Metric

The following endpoint is used to update the values of an existing Metric:

```
PATCH /accounting-system/installations/{installation_id}/metrics/{id}

Content-type: application/json
Authorization: Bearer {token}

{
   "time_period_start": "time_period_start_to_be_updated",
   "time_period_end": "time_period_end_to_be_updated",
   "value": value_to_be_updated
}
```

In order to update the resource properties, the body of the request must contain an updated representation of Metric. You can update a part or all attributes of the Metric. The empty or null values are ignored.
The response will be the updated entity :

Success Response `200 OK`

```
{
   "updated_entity"
}
```

### [GET] - Fetch an existing Metric

Having the id of a Metric, the client can request the relevant to that id Metric by executing the following request:

```
GET /accounting-system/installations/{installation_id}/metrics/{metric_id}

Authorization: Bearer {token}
```

The response is as follows:

Success Response `200 OK`

```
{    
"metric_id": "72983kea0h41a20c683e3589",
"metric_definition_id": "62973fea0f41a20c683e9014", 
"time_period_start": "2020-12-20T09:13:07Z", 
"time_period_end": "2020-12-25T11:14:07Z",   
"value": 55
}
```

### Errors

Please refer to section [Errors](./api_errors) to see all possible Errors.