table "needs" {
  auth = false

  schema {
    int id
    // In Room this reference cascades when the farmer is deleted.
    int farmerId {
      table = "users"
    }
    text serviceType
    text cropCode
    text zoneCode
    decimal hectares
    int startEpochDay
    int endEpochDay
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree", field: [{name: "farmerId"}]}
  ]
  guid = "bBQgtUbpONF1qg_AmHmbq2bk0ss"
}
