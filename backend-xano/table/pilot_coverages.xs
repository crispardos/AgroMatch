table "pilot_coverages" {
  auth = false

  schema {
    int id
    int pilotId {
      table = "pilot_profiles"
      description = "Internal profile ID (pilot_profiles.id), not the pilot's user ID."
    }
    text zoneCode
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "pilotId"}, {name: "zoneCode"}]}
  ]
  guid = "XxIzWpSctSaMS0pDPmRk8cAZd10"
}
