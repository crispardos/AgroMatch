table "interests" {
  auth = false

  schema {
    int id
    int needId {
      table = "needs"
    }
    int pilotId {
      table = "users"
    }
    enum status?="ACTIVE" {
      values = ["ACTIVE", "ACCEPTED", "REPLACED", "COMPLETED"]
    }
    int createdEpochDay filters=min:0
    int? acceptedEpochDay? filters=min:0
    int? completedEpochDay? filters=min:0
    // The server sets this to needId for ACTIVE and ACCEPTED, and to null otherwise.
    int? activeNeedId? {
      table = "needs"
    }
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "activeNeedId"}]}
    {type: "btree", field: [{name: "needId"}, {name: "createdEpochDay", op: "desc"}]}
    {type: "btree", field: [{name: "pilotId"}, {name: "status"}]}
  ]
  guid = "vQZyeYheinmzLMIUi7uBxcQnfFg"
}
