---
id: collect_metrics
title: Collecting Metrics from different levels
---

The Accounting System offers the possibility to collect Metrics from different levels of the hierarchical structure Project -> Provider -> Installation. 
You can select the level you want, and the API returns the Metrics under it. Returned Metrics also contain the hierarchical structure they belong to, for example:
```
{
   "id": "6297459e0f41a20c683e9019",
   "metric_definition_id": "62973fea0f41a20c683e9014",
   "time_period_start": "2020-12-20T09:13:07Z",
   "time_period_end": "2020-12-25T11:14:07Z",
   "value": 90.0,
   "project": "EGI-ACE",
   "provider": "grnet",
   "installation": "GRNET-KNS"
}
```

### [GET] - Collecting Metrics from specific Project

You can collect all Metrics under a specific Project by executing the following GET HTTP request :

```
GET /accounting-system/projects/{project_id}/metrics

Authorization: Bearer {token}
```

By default, the first page of 10 Metrics under that Project will be returned. For instance:

Success Response `200 OK`

```

{
   "size_of_page": 10,
   "number_of_page": 1,
   "total_elements": 12,
   "total_pages": 2,
   "content": [
       {
           "id": "6297459e0f41a20c683e9019",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 90.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974da70f41a20c683e9021",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 325.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974dcd0f41a20c683e9022",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 444.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974d9b0f41a20c683e901f",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 450.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974dd00f41a20c683e9023",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 567.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974d8b0f41a20c683e901a",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 904.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974d8f0f41a20c683e901b",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 905.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974d910f41a20c683e901c",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 906.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974d950f41a20c683e901d",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 909.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       },
       {
           "id": "62974da10f41a20c683e9020",
           "metric_definition_id": "62973fea0f41a20c683e9014",
           "time_period_start": "2020-12-20T09:13:07Z",
           "time_period_end": "2020-12-25T11:14:07Z",
           "value": 6000.0,
           "project": "EGI-ACE",
           "provider": "grnet",
           "installation": "GRNET-KNS"
       }
   ],
   "links": [
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/projects/101017567/metrics?page=1&size=10",
           "rel": "first"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/projects/101017567/metrics?page=2&size=10",
           "rel": "last"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/projects/101017567/metrics?page=1&size=10",
           "rel": "self"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/projects/101017567/metrics?page=2&size=10",
           "rel": "next"
       }
   ]
}
```

You can tune the default values by using the query parameters page and size as shown in the example below.

```
GET /accounting-system/providers?page=2&size=15
```

The above request returns the second page which contains 15 Metrics.

### [GET] - Collecting Metrics from specific Provider

Respectively, you can collect all Metrics that exist in a specific Provider. To execute the following request, you need to pass the Project ID to which that Provider belongs and of course the Provider ID.

```
GET /accounting-system/projects/{project_id}/providers/{provider_id}/metrics

Authorization: Bearer {token}
```

By default, the first page of 10 Metrics under that Provider will be returned. Of course, You can tune the default values by using the query parameters page and size.


### [GET] - Collecting Metrics from specific Installation

Likewise, you can collect all Metrics that exist in a specific Installation. To execute the following request, you need to pass the Installation ID.

```
GET /accounting-system/installations/{installation_id}/metrics

Authorization: Bearer {token}

```

By default, the first page of 10 Metrics under that Installation will be returned. Of course, You can tune the default values by using the query parameters page and size.

### Errors

Please refer to section [Errors](./api_errors) to see all possible Errors.