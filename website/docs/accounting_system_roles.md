---
id: accounting_system_roles
title: Accounting System Roles
---

When clients register to the system, they can perform the following actions :

- Create metric definitions
- Read all the metric definitions
- Update/Delete their  created metric definitions

| Collection       	| Create 	| Update 	| Delete 	| Read   	|
|------------------	|--------	|--------	|--------	|--------	|
| MetricDefinition 	| Always 	| Entity 	| Entity 	| Always 	|


- Create providers
- Read all the providers
- Update/Delete their  created providers

| Collection       	| Create 	| Update 	| Delete 	| Read   	|
|------------------	|--------	|--------	|--------	|--------	|
| Provider      	| Always 	| Entity 	| Entity 	| Always 	|

- Read all the clients

| Collection       	| Read   	| 
|------------------	|--------	|
| Client        	| Always 	|


By default, in the accounting system the following roles exist and can be assigned to the registered client.

### Project Admin

Project admin role, has  full access  on the projects it is assigned and can perform all actions on the projects as well as the providers, installations and metrics that are assigned to these projects.

**On an assigned project and the providers/installation/metrics of the project, the project admin role can : **

- Register the assigned Project
- Read the project
- Associate and dissociate providers on the project
- Grant access to a client  with a specific role, on a project

| Collection       	| Register 	| Associate | Dissociate | Read   	| Acl    |
|------------------	|--------	|--------	|--------	 |--------	| ------ |
| Project        	| Always 	| Always 	| Always 	 | Always 	| Always |

- Create/Update/Delete/Read  Installations, on a project
- Grant access to a client with a specific role , on an installation

| Collection       	| Create 	| Update    | Delete     | Read   	| Acl    |
|------------------	|--------	|--------	|--------	 |--------	| ------ |
| Installation      | Always 	| Always 	| Always 	 | Always 	| Always |

- Create/Update/Delete/Read Metrics on  a project

| Collection       	| Create 	| Update    | Delete     | Read   	|
|------------------	|--------	|--------	|--------	 |--------	|
| Metric            | Always 	| Always 	| Always 	 | Always 	|

- Grant access to a client  with a specific role , on a provider

| Collection       	| Acl   	| 
|------------------	|--------	|
| Provider        	| Always    |

### Provider Admin

A provider admin role,  has full access on the providers it is assigned and can perform all actions on the installations as well as the  metrics that are assigned to the specific providers

**On an assigned provider and the installations/metrics of the provider, the provider  admin role  can : **

- Create/Update/Delete/Read  Installations, on a specific Provider
- Grant access to a client with a specific role , on an installation

| Collection       	| Create 	| Update    | Delete     | Read   	| Acl    |
|------------------	|--------	|--------	|--------	 |--------	| ------ |
| Installation      | Always 	| Always 	| Always 	 | Always 	| Always |

- Create/Update/Delete/Read Metrics on an Installation

| Collection       	| Create 	| Update    | Delete     | Read   	|
|------------------	|--------	|--------	|--------	 |--------	|
| Metric            | Always 	| Always 	| Always 	 | Always 	|

- Grant access to a client  with a specific role , on a provider

| Collection       	| Acl   	| 
|------------------	|--------	|
| Provider        	| Always    |


### Installation Admin

An installation admin role, has full access on the installations it is assigned and can perform all actions on the metrics that are assigned to the specific installations

**On an assigned installation and the metrics of the installation, the installation  admin role can : **

- Read/Update/Delete installation
- Grant access to a client  with a specific role , on an installation

| Collection       	| Update    | Delete     | Read   	| Acl    |
|------------------	|--------	|--------	 |--------	| ------ |
| Installation      | Always 	| Always 	 | Always 	| Always |

- Create/Read/Update/Delete metrics , on an installation

| Collection       	| Create 	| Update    | Delete     | Read   	|
|------------------	|--------	|--------	|--------	 |--------	|
| Metric            | Always 	| Always 	| Always 	 | Always 	|