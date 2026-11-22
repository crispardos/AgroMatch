table "ratings" {
  auth = false

  schema {
    int id
    // In Room this reference cascades when the interest is deleted.
    int interestId {
      table = "interests"
    }
    int score
    text? comment?
    int createdEpochDay
    text origin
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "interestId"}]}
  ]
  guid = "accemdsJj1evV4j_QzgygItr1OU"
}
