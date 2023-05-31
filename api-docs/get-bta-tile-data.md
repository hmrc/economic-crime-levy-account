# Get BTA Tile Data

Returns the ECL data required in order to display the different possible states on the Business Tax Account ECL tile.
Calls to this API must be made by an authorised user with an ECL enrolment in order for the data to be returned.

**URL**: `/economic-crime-levy-account/bta-tile-data`

**Method**: `GET`

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

## Responses

### Success response

**Code**: `200 OK`

**Response Body**

| Field Name                | Description                                                                                                        | Data Type | Mandatory/Optional |
|---------------------------|--------------------------------------------------------------------------------------------------------------------|-----------|--------------------|
| eclRegistrationReference  | The unique ECL registration reference that is the identifier for the ECL enrolment                                 | String    | Mandatory          |
| dueReturn                 | The highest priority return that is due, if no return is due this object will not be present                       | Object    | Optional           |
| dueReturn.isOverdue       | A boolean indicating whether or not the return is overdue                                                          | Boolean   | Mandatory          |
| dueReturn.dueDate         | The date that the return must be submitted by. This is a date without a time-zone in the ISO-8601 calendar system. | String    | Mandatory          |
| dueReturn.periodStartDate | The start date of the return period. This is a date without a time-zone in the ISO-8601 calendar system.           | String    | Mandatory          |
| dueReturn.periodEndDate   | The end date of the return period. This is a date without a time-zone in the ISO-8601 calendar system.             | String    | Mandatory          |
| dueReturn.fyStartYear     | The starting year of the financial year that the return period is for.                                             | String    | Mandatory          |
| dueReturn.fyEndYear       | The ending year of the financial year that the return period is for.                                               | String    | Mandatory          |

**Response Body Examples**

Where no return is due:

```json
{
  "eclRegistrationReference": "XMECL0000000001"
}
```

Where a return is due but not overdue:

```json
{
  "eclRegistrationReference": "XMECL0000000001",
  "dueReturn": {
    "isOverdue": false,
    "dueDate": "2023-09-30",
    "periodStartDate": "2022-04-01",
    "periodEndDate": "2023-03-31",
    "fyStartYear": "2022",
    "fyEndYear": "2023"
  }
}
```

Where a return is overdue:

```json
{
  "eclRegistrationReference": "XMECL0000000001",
  "dueReturn": {
    "isOverdue": true,
    "dueDate": "2023-09-30",
    "periodStartDate": "2022-04-01",
    "periodEndDate": "2023-03-31",
    "fyStartYear": "2022",
    "fyEndYear": "2023"
  }
}
```

### Unauthorized response

**Code**: `401 UNAUTHORIZED`

This response can occur when a call is made by any consumer without an authorized session that has an ECL enrolment.