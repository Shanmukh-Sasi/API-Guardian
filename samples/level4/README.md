# Level 4 comparison sample

These two files describe the same Payments API at different versions:

- `payments-v1.json` is the baseline version `1.0.0`.
- `payments-v2.yaml` is the target version `2.0.0`.

Upload both files to the same existing Service with the Level 3 upload endpoint:

```text
POST /api/services/{serviceId}/specifications
```

Use Postman **Body → form-data**, with a File field named `file`. After both uploads succeed, compare them:

```http
POST /api/services/{serviceId}/comparisons
Content-Type: application/json
```

```json
{
  "baselineVersion": "1.0.0",
  "targetVersion": "2.0.0"
}
```

The response contains the saved comparison ID and a `changes` array. The corresponding example change list is in `expected-changes.json`. Retrieve the saved result with:

```text
GET /api/comparisons/{comparisonId}
```

The sample covers a removed endpoint, an added HTTP method, added/removed/changed parameters, added/removed request and response fields, and changed parameter and schema field types.

The `breaking` flag is a first-pass rule: removed endpoints/methods/fields and changed types are marked breaking; adding an optional request parameter or response field is marked non-breaking; adding a required request field/parameter is marked breaking. This is a useful starting classification, not a complete compatibility standard.
