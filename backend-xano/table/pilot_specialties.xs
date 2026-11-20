table "pilot_specialties" {
  auth = false

  schema {
    int id
    int pilotId {
      table = "pilot_profiles"
    }
    text specialtyCode filters=trim
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "pilotId"}, {name: "specialtyCode"}]}
    {type: "btree", field: [{name: "specialtyCode"}]}
  ]
  guid = "MXeHro8uMHc8o9XWfM6HQooHfTM"
}
