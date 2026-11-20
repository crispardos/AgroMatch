table "pilot_profiles" {
  auth = false

  schema {
    int id
    int userId {
      table = "users"
    }
    text serviceCode?="FUMIGATION" filters=trim
    int? experienceYears? filters=min:0
    decimal? hectaresPerHour? filters=min:0
    decimal? workingHoursPerDay? filters=min:0
    date? availableFrom?
    date? availableTo?
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "userId"}]}
    {type: "btree", field: [{name: "serviceCode"}]}
  ]
  guid = "Rt-nzTy34pO13yY5ukvbLh5KU0o"
}
