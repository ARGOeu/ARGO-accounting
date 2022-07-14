---
id: accounting_system_api
title: Accounting System API
slug: /
---

The Accounting System is a platform that is responsible for collecting, aggregating, and exchanging the metrics between different infrastructures, providers, and projects.

Essentially, the main functions of the platform are expressed by a REST API. Therefore, the primary duties of the API are the following:

- Accepting input from several different resources.
- Storing the input into a database.
- Aggregating the incoming input.
- Offering the aggregated input to several different clients.
- Request accounting data for a specific time period.

In addition, API resources must only be obtainable by authenticated clients. For this reason, every client who wants access to API resources should be authenticated. 
