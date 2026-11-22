table "users" {
  auth = true

  schema {
    int id
    text alias
    text aliasKey
    text role
    // Xano native authentication replaces the local passwordHash and passwordSalt.
    password password {
      sensitive = true
    }
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "aliasKey"}]}
  ]
  guid = "YmfkCJLzNeTbHkDQ-ebzzsEmDho"
}
