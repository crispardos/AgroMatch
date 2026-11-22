table "pilot_specialties" {
  auth = false

  schema {
    int id
    int pilotId {
      table = "pilot_profiles"
      description = "Internal profile ID (pilot_profiles.id), not the pilot's user ID."
    }
    text specialtyCode
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "pilotId"}, {name: "specialtyCode"}]}
  ]
  guid = "MXeHro8uMHc8o9XWfM6HQooHfTM"
}
