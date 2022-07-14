---
id: provider
title: Provider
---

As Provider, we refer to the Organisation that offers at least one installation to a specific project.

The Provider can either be registered by communicating with EOSC Resource Catalogue, or you can create a new Provider by using the endpoints the Accounting System provides.


### Registering Provider by following the EOSC Onboarding Process at https://providers.eosc-portal.eu/becomeAProvider

The Accounting System communicates with EOSC Providers to retrieve the available Providers. 
A cron job, which runs every day at 12 am, collects the Providers and stores them in the database. From the response we receive we keep specific information which is stored in the collection named Provider. The collection has the following structure:

| Field          	| Description   	      | 
|------------------	|-------------------------|
| id             	| Provider ID             |
| name          	| Provider name           |
| website      	    | Url of Provider website |
| abbreviation      | Provider abbreviation   |
| logo      	    | Url of Provider logo    |

### Registering Provider through Accounting System API

If the Provider is not registered in the EOSC Resource Catalogue, the API offers the functionality to create a new one. Below, we will describe the available operations regarding the Provider that you can interact with.

### [POST] - Create a new Provider

You can submit a new Provider by executing the following POST request:

```
POST /accounting-system/providers

Content-type: application/json
Authorization: Bearer {token}

{
  "id" : "grnet",
  "name" : "National Infrastructures for Research and Technology",
  "website" : "https://www.grnet.gr/",
  "abbreviation" : "GRNET",
"logo": : "https://grnet.gr/wp-content/uploads/sites/13/2016/04/GRNET_Logo_Transparent-e1431694322566-1.png"
}
```

The response is the stored Provider:

Success Response `200 OK`

```
{
"id" : "grnet",
"name" : "National Infrastructures for Research and Technology",
"website" : "https://www.grnet.gr/",
"abbreviation" : "GRNET",
"logo": : "https://grnet.gr/wp-content/uploads/sites/13/2016/04/GRNET_Logo_Transparent-e1431694322566-1.png"
}
```

### [DELETE] - Delete an existing Provider

You can also delete an existing Provider registered through the Accounting System API by executing the following request:

```
DELETE /accounting-system/providers/{provider_id}

Authorization: Bearer {token}
```
If the deletion is successful the following response is returned:

Success Response `200 OK`

```
{
   "code": 200,
   "message": "Provider has been deleted successfully."
}
```

**Finally, it should be noted that deleting Providers which derive from the EOSC Resource Catalogue  is not allowed.**

### [PATCH] - Update an existing Provider

You can update an existing Provider registered through the Accounting System API by executing the following request:

```
PATCH /accounting-system/providers/{provider_id}

Content-type: application/json
Authorization: Bearer {token}

{
  "id" : "id to be updated",
  "name" : "name to be updated",
  "website" : "website to be updated",
  "abbreviation" : "abbreviation to be updated",
  "logo" : "logo to be updated"
}
```
The body of the request must contain an updated representation of Provider. You can update a part or all attributes of the Provider. The empty or null values are ignored.

The response will be the updated entity :

Success Response `200 OK`

```
{
   "updated_entity"
}
```

**Finally, it should be noted that updating Providers which derive from the EOSC Resource Catalogue is not allowed.**

### [GET] - Fetch a registered Provider

You can either fetch a Provider registered through the Accounting System API or EOSC Resource Catalogue by executing the following GET HTTP request:

```
GET /accounting-system/providers/{provider_id}

Authorization: Bearer {token}
```

The response is as follows:

Success Response `200 OK`

```
{
   "id": "osmooc",
   "name": "Open Science MOOC",
   "website": "https://opensciencemooc.eu/",
   "abbreviation": "OSMOOC",
   "logo": "https://opensciencemooc.eu/assets/img/osm/osm-logo.png"
}
```

### [GET] - Fetch all registered Providers

Essentially, the following endpoint returns all Providers available on the EOSC Resource Catalogue as well as all Providers registered through the Accounting System API. By default, the first page of 10 Providers will be returned.

```
GET /accounting-system/providers

Authorization: Bearer {token}
```

You can tune the default values by using the query parameters page and size as shown in the example below.

```
GET /accounting-system/providers?page=2&size=15

Authorization: Bearer {token}
```

The above request returns the second page which contains 15 providers:

Success Response `200 OK`

```
{
   "size_of_page": 15,
   "number_of_page": 2,
   "total_elements": 237,
   "total_pages": 16,
   "content": [
       {
           "id": "ubora",
           "name": "Open Biomedical Engineering e-platform for Innovation through Education",
           "website": "http://ubora-biomedical.org/",
           "abbreviation": "UBORA",
           "logo": "http://ubora-biomedical.org/wp-content/uploads/2017/01/UBORA-Logo-Final-JPEGb.jpg"
       },
       {
           "id": "bioexcel",
           "name": "BioExcel Centre of Excellence",
           "website": "https://bioexcel.eu/",
           "abbreviation": "BIOEXCEL",
           "logo": "https://bioexcel.eu/wp-content/uploads/2017/02/Bioexcell_logo_payoff_1080px_transp.png"
       },
       {
           "id": "surf-nl",
           "name": "SURF",
           "website": "https://www.surf.nl/",
           "abbreviation": "SURF",
           "logo": "https://www.surf.nl/themes/surf/logo.svg"
       },
       {
           "id": "emso_eric",
           "name": "European Multidisciplinary Seafloor and water column Observatory",
           "website": "http://emso.eu",
           "abbreviation": "EMSO ERIC",
           "logo": "http://emso.eu/wp-content/uploads/2018/03/logo-w-300.png"
       },
       {
           "id": "elixir-belgium",
           "name": "ELIXIR Belgium",
           "website": "https://www.elixir-belgium.org/",
           "abbreviation": "ELIXIR Belgium",
           "logo": "https://www.elixir-belgium.org/sites/default/files/logo.jpg"
       },
       {
           "id": "cyi",
           "name": "The Cyprus Institute",
           "website": "https://www.cyi.ac.cy/",
           "abbreviation": "CyI",
           "logo": "https://numismatics-medieval.dioptra.cyi.ac.cy/sites/default/files/CyI.png"
       },
       {
           "id": "uni-freiburg",
           "name": "University of Freiburg",
           "website": "https://www.uni-freiburg.de",
           "abbreviation": "UNI FREIBURG",
           "logo": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSH9gnjhQ5503SSs1_6E8Rr7UITWeacwmm3u3rK1eSdLQiKdywpqQ&s"
       },
       {
           "id": "dcc-uk",
           "name": "Digital Curation Centre",
           "website": "https://www.dcc.ac.uk/",
           "abbreviation": "DMPonline",
           "logo": "https://arts.unimelb.edu.au/__data/assets/image/0009/2913867/DDC2019.jpg"
       },
       {
           "id": "cloudferro",
           "name": "CloudFerro",
           "website": "https://cloudferro.com/",
           "abbreviation": "CloudFerro",
           "logo": "https://cf2.cloudferro.com:8080/94d2acacf10346a18c289981f4d0bd33:cloudferro_cms/static/img/cloudferro-logo.svg"
       },
       {
           "id": "psnc",
           "name": "Poznan Supercomputing and Networking Center",
           "website": "https://www.psnc.pl/",
           "abbreviation": "PSNC",
           "logo": "https://www.psnc.pl/files/PSNC_logo_.svg"
       },
       {
           "id": "forschungsdaten",
           "name": "Forschungsdaten.info",
           "website": "https://www.forschungsdaten.info/praxis-kompakt/english-pages/",
           "abbreviation": "forschungsdaten",
           "logo": "https://www.forschungsdaten.info/typo3temp/secure_downloads/99679/0/ba8ae03722cb4bfb8baa63cfb1ab045bb7a03c78/csm_logo_long_seeblau100_af47d6577b.png"
       },
       {
           "id": "umg-br",
           "name": "University of Minas Gerais",
           "website": "https://ufmg.br/",
           "abbreviation": "UMG",
           "logo": "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2d/Symbolfumg.jpg/375px-Symbolfumg.jpg"
       },
       {
           "id": "coard",
           "name": "Collaborative Open Access Research and Development",
           "website": "https://coard.community/",
           "abbreviation": "COARD",
           "logo": "https://images.squarespace-cdn.com/content/v1/5f44f92bd523407260108ba6/1598444273981-CRFZ1SC35B9VTRNT0QUS/Coard+logo%402x.png"
       },
       {
           "id": "erasmusmc",
           "name": "Erasmus Medical Center",
           "website": "https://www.erasmusmc.nl/",
           "abbreviation": "Erasmus MC",
           "logo": "https://seeklogo.com/images/E/Erasmus_MC-logo-B857CA0725-seeklogo.com.png"
       },
       {
           "id": "osmooc",
           "name": "Open Science MOOC",
           "website": "https://opensciencemooc.eu/",
           "abbreviation": "OSMOOC",
           "logo": "https://opensciencemooc.eu/assets/img/osm/osm-logo.png"
       }
   ],
   "links": [
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/providers?page=1&size=15",
           "rel": "first"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/providers?page=16&size=15",
           "rel": "last"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/providers?page=2&size=15",
           "rel": "self"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/providers?page=1&size=15",
           "rel": "prev"
       },
       {
           "href": "https://acc.devel.argo.grnet.gr/accounting-system/providers?page=3&size=15",
           "rel": "next"
       }
   ]
}
```

### [POST] - Access Control Entry for a particular Provider of a specific Project

Any client can have different responsibilities at different Providers. The actions the client can perform at each Provider are determined by the role and the permissions it has.

To grant a role to a client on a specific Provider of a Project, you have to execute the following request:

```
POST /accounting-system/projects/{project_id}/providers/{provider_id}/acl/{who}

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

**Keep in mind that** to execute the above operation, you must have been assigned a role containing the Provider Acl permission.

### Errors

Please refer to section [Errors](./api_errors) to see all possible Errors.