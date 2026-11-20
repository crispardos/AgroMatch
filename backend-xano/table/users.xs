table "users" {
  auth = true

  schema {
    int id
    timestamp createdAt?=now
    text alias filters=trim|lower|min:3|max:40
    enum role {
      values = ["farmer", "pilot"]
    }
    password password filters=min:8|max:128|minUpperAlpha:1|minLowerAlpha:1|minDigit:1 {
      sensitive = true
    }
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "alias"}]}
  ]
  guid = "YmfkCJLzNeTbHkDQ-ebzzsEmDho"
}
