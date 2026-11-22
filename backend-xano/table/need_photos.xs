table "need_photos" {
  auth = false

  schema {
    int id
    // In Room this reference cascades when the need is deleted.
    int needId {
      table = "needs"
    }
    text filePath {
      description = "Stable storage key for the shared reference photo, not a device path or an expiring URL."
    }
    // Room uses zero-based positions for photos attached to the need.
    int sortOrder
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree", field: [{name: "needId"}]}
  ]
  guid = "AxaSzkWFUTqsl-O63BhVkrilBm0"
}
