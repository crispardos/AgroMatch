table "needs" {
  auth = false

  schema {
    int id
    int farmerId {
      table = "users"
    }
    text serviceType?="FUMIGATION" filters=trim
    text cropCode filters=trim
    text zoneCode filters=trim
    decimal hectares filters=min:0
    int startEpochDay filters=min:0
    int endEpochDay filters=min:0
    int createdEpochDay filters=min:0
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree", field: [{name: "farmerId"}, {name: "startEpochDay", op: "desc"}]}
  ]
  guid = "bBQgtUbpONF1qg_AmHmbq2bk0ss"
}
