table "ratings" {
  auth = false

  schema {
    int id
    int interestId {
      table = "interests"
    }
    int score filters=min:1|max:5
    text? comment? filters=trim|max:300
    enum origin?="farmer_to_pilot" {
      values = ["farmer_to_pilot"]
    }
    int createdEpochDay filters=min:0
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "interestId"}]}
  ]
  guid = "accemdsJj1evV4j_QzgygItr1OU"
}
