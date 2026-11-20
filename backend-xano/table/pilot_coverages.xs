table "pilot_coverages" {
  auth = false

  schema {
    int id
    int pilotId {
      table = "pilot_profiles"
    }
    text zoneCode filters=trim
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "pilotId"}, {name: "zoneCode"}]}
    {type: "btree", field: [{name: "zoneCode"}]}
  ]
  guid = "XxIzWpSctSaMS0pDPmRk8cAZd10"
}
