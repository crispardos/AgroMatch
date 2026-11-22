table "interests" {
  auth = false

  schema {
    int id
    // In Room both references cascade when their parent is deleted.
    int needId {
      table = "needs"
    }
    int pilotId {
      table = "users"
      description = "The assigned pilot's user ID (users.id), not the profile ID."
    }
    text status
    int createdEpochDay
    int? completedEpochDay?
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree", field: [{name: "needId"}]}
    {type: "btree", field: [{name: "pilotId"}]}
  ]
  guid = "vQZyeYheinmzLMIUi7uBxcQnfFg"
}
