---
id: metric_definition
title: Metric Definition
---

Metrics are measures of quantitative assessment commonly used for assessing, comparing, and tracking usage or performance of a service. They are the main indicators.

A metric definition is the way to represent and describe the type of the metrics.  A Metric Definition consists of the metadata describing a Metric. The client can interact through several operations with the API in order to create, update, delete or fetch a Metric Definition. The aforementioned operations are described below.

The Metric Definition can be expressed by the following structure:

| Field          	| Description   	                      | 
|------------------	|---------------------------------------- |
| id             	| Metric Definition unique id             |
| metric_name      	| Metric Name to be used for presentation |
| metric_description      	| Short Description of how the metric is collected |
| unit_type      	| Predefined List of Unit Types |
| metric_type      	| Predefined List of Metric Types |

### [POST] - Create a Metric Definition

Upon creating a new Metric Definition, the client should be able to forward a Metric to the Accounting System API.
The client can submit a new Metric Definition by executing the following POST request:

```
POST /accounting-system/metric-definitions

Content-type: application/json
Authorization: Bearer {token}

{
"metric_name" : "number_of_users",
"metric_description" : "Number of users",
"unit_type" : "#",
"metric_type" : "aggregated"
}
```

Upon inserting the record into the database, the API returns the Metric Definition enhanced with the metric_definition_id :

Success Response `201 CREATED`

```
{
  "metric_definition_id": "61dc142f6a278e43e8d6b3be",
  "metric_name" : "number_of_users",
  "metric_description" : "Number of users",
  "unit_type" : "#",
  "metric_type" : "aggregated"
}
```

### [GET] - Fetch a Metric Definition

The client should be able to fetch an already created Metric Definition. Having the id of a Metric Definition, the client can request the relevant to that id Metric Definition by executing the following request:

```
GET /accounting-system/metric-definitions/{metric_definition_id}

Authorization: Bearer {token}
```

The response returned to the client is the following:

Success Response `200 OK`
```
{
  "id" : "61dff744ba5b5f60791bd09d",
  "metric_name" : "number_of_users",
  "metric_description" : "Number of users",
  "unit_type" : "#",
  "metric_type" : "aggregated"
}
```

In order to get all records, the following request should be executed:

```
GET /accounting-system/metric-definitions

Authorization: Bearer {token}
```

### [PATCH] - Update a Metric Definition

The client can update a Metric Definition using the following PATCH request. The URL should be filled in with the metric definition id to be updated.

```
PATCH /accounting-system/metric-definitions/{metric_definition_id}

Content-type: application/json
Authorization: Bearer {token}

{
  attributes_to_be_updated
}
```

The response will be the updated entity :

Success Response `200 OK`

```
{
   "updated_entity"
}
```

In order to update the resource properties, the body of the request must contain an updated representation of Metric Definition. For example, to edit the metric type, send the following request:

```
PATCH /accounting-system/metric-definitions/{metric_definition_id}

Content-type: application/json
Authorization: Bearer {token}

{  
"metric_type": "metric_type_to_be_updated"
}
```

You can update a part or all attributes of the Metric Definition. The empty or null values are ignored.

### [DELETE] - Delete a Metric Definition

You can only delete a Metric Definition that does not have any Metrics assigned to it. If the Metric Definition has no Metrics, you can safely delete it.

Metric Definition can be deleted by executing the following request:

```
DELETE /accounting-system/metric-definitions/{metric-definition-id}

Authorization: Bearer {token}
```

If the operation is successful, you get the following response:

Success Response `200 OK`
```
{
   "code": 200,
   "message": "The Metric Definition has been deleted successfully."
}
```

### Errors

Please refer to section [Errors](./api_errors) to see all possible Errors.