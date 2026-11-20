table "need_photos" {
  auth = false

  schema {
    int id
    int needId {
      table = "needs"
    }
    // Null while the slot is reserved. The saved image contains metadata and a Storage path.
    image? file?
    int sortOrder filters=min:1|max:5
    int createdEpochDay filters=min:0
    timestamp reservedAt?=now
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "needId"}, {name: "sortOrder"}]}
  ]
  guid = "AxaSzkWFUTqsl-O63BhVkrilBm0"
}
