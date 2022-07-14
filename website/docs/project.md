---
id: project
title: Project
---

A Project is the main resource of the Accounting System. The first step a user must follow is to create a project under which the metric data will belong.

Currently, we support only projects that are available in the EU database.  Basically, you can register information about a particular Project in our system using the unique ID that every European Project has for its identification.

### [POST] - Register a Project

In order to register your project, one needs to just send a request to the API with the  unique ID that every European Project has for its identification. So you may register an European Project into the Accounting System via the following action:


```
POST /accounting-system/projects/{project_id}

Authorization: Bearer {token}
```

Once the above request is executed, the Accounting System communicates with OpenAire. From the response we receive, we keep specific information which is stored in the collection named Project. The collection has the following structure:


| Field          	| Description   	                      | 
|------------------	|---------------------------------------- |
| id             	| Project ID             |
| acronym       	| Project Acronym |
| title      	    | Project Title |
| start_date      	| Project Start Date |
| end_date      	| Project End Date |
| call_identifier      	| Project Unique Identifier |

For instance, if you want to register the EGI-ACE project, you have to execute the following request where 101017567 is the ID of the project.

```
POST /accounting-system/projects/101017567

Authorization: Bearer {token}
```

The response is as follows:

Success Response `200 OK`

```
{
   "id": "101017567",
   "acronym": "EGI-ACE",
   "title": "EGI Advanced Computing for EOSC",
   "start_date": "2021-01-01",
   "end_date": "2023-06-30",
   "call_identifier": "H2020-INFRAEOSC-2020-2"
}
```

### [POST] - Associate Providers with a specific Project

To be able to register Metrics in an installation, first you must associate a Project with one or more  Providers. The following action is responsible for generating a hierarchical relationship between a Project and one or more Providers:

```
POST /accounting-system/projects/{project_id}/associate

Content-Type: application-json
Authorization: Bearer {token}

{
   "providers":[
      "bioexcel",
      "osmooc",
      "grnet",
      "sites"
   ]
}
```

The response is:

Success Response `200 OK`

```
{
   "code": 200,
   "message": "The following providers [bioexcel, osmooc, grnet, sites] have been associated with Project {project_id}"
}
```

### [POST] - Dissociate Providers from a Project

You can also dissociate one or more Providers from a Project:

```
POST /accounting-system/projects/{project_id}/dissociate

Content-Type: application-json
Authorization: Bearer {token}

{
   "providers":[
      "grnet"
   ]
}
```

The response is :

Success Response `200 OK`

```
{
   "code": 200,
   "message": "The following providers [grnet] have been dissociated from Project {project_id}"
}
```

If there are Installations registered to Provider, the dissociation is not allowed:

Error Response `409 CONFLICT`

```
{
   "code": 409,
   "message": "Dissociation is not allowed. There are registered Installations to {project_id, provider_id}"
}
```

### [GET] - Project Hierarchical Structure

You can retrieve the Providers and Installations associated with a specific project:

```
GET /accounting-system/projects/{project_id}

Authorization: Bearer {token}
```

Basically, the hierarchical structure of a Project is returned:

Success Response `200 OK`
```
{
   "project_id": "101017567",
   "acronym": "EGI-ACE",
   "title": "EGI Advanced Computing for EOSC",
   "providers": [
       {
           "provider_id": "bioexcel",
           "name": "BioExcel Centre of Excellence",
           "installations": []
       },
       {
           "provider_id": "grnet",
           "name": "National Infrastructures for Research and Technology",
           "installations": [
               {
                   "installation_id": "62a075dddc4e0770e91f651a",
                   "installation": "GRNET-KNS",
                   "infrastructure": "okeanos-knossos"
               }
           ]
       },
       {
           "provider_id": "osmooc",
           "name": "Open Science MOOC",
           "installations": []
       },
       {
           "provider_id": "sites",
           "name": "Swedish Infrastructure for Ecosystem Science",
           "installations": []
       }
   ]
}
```


### [GET] - Fetch all Projects

You can retrieve all Projects assigned to you by executing the following GET request:

```
GET /accounting-system/projects

Authorization: Bearer {token}
```

By default, the first page of 10 Projects will be returned. You can tune the default values by using the query parameters page and size as shown in the example below.

```
GET /accounting-system/projects?page=2&size=5

Authorization: Bearer {token}
```

Success Response `200 OK`

```
{
  "size_of_page": 3,
  "number_of_page": 1,
  "total_elements": 9,
  "total_pages": 3,
  "content": [
    {
      "project_id": "1214-032274",
      "acronym": "",
      "title": "Die biblischen Orte und Regionen im Norden Israels/Palästinas in griech.-röm. Zeit: Topographie, Archäologie, literarische Quellen und heutiger Zustand.",
      "providers": []
    },
    {
      "project_id": "1700115839",
      "acronym": "",
      "title": "Code as Law on Cyberspace",
      "providers": []
    },
    {
      "project_id": "2159864",
      "acronym": "",
      "title": "Separate, Combined or Parallel Routes? An Interdisciplinary Approach to Complex Word Processing",
      "providers": []
    }
  ],
  "links": [
    {
      "href": "http://localhost:8080/accounting-system/projects?page=1&size=3",
      "rel": "first"
    },
    {
      "href": "http://localhost:8080/accounting-system/projects?page=3&size=3",
      "rel": "last"
    },
    {
      "href": "http://localhost:8080/accounting-system/projects?page=1&size=3",
      "rel": "self"
    },
    {
      "href": "http://localhost:8080/accounting-system/projects?page=2&size=3",
      "rel": "next"
    }
  ]
}
```


### [POST] - Access Control Entry for a particular Project

The general endpoint that is responsible for creating an Access Control entry for a Project is as follows:

```
POST /accounting-system/projects/{project_id}/acl/{who}

Content-Type: application/json
Authorization: Bearer {token}
 
{
  "roles":[
     {role_name}
  ]
}
```

where {who} is the client ID in which the roles will be assigned.

The response is :

Success Response `200 OK`

```
{
   "code": 200,
   "message": "Access Control entry has been created successfully."
}
```

One client can have different roles at different Projects. For instance, in one Project can be an admin executing all the Project operations while in another it can only read the Project Metrics.
Consequently, any client can have different responsibilities at different Projects. The actions the client can perform at each Project are determined by the role, and the permissions it has.

**Keep in mind that** to execute the above operation, you must have been assigned a role containing the Project Acl permission.


### Errors

Please refer to section [Errors](./api_errors) to see all possible Errors.