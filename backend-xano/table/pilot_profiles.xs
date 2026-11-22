table "pilot_profiles" {
  auth = false

  schema {
    // Xano's profile ID is independent from the user's ID.
    int id
    int userId {
      table = "users"
      description = "The pilot's user ID. Unique so each user has at most one profile."
    }
    text serviceCode
    int? experienceYears?
    decimal hectaresPerHour
    decimal workingHoursPerDay
    text? availableFrom?
    text? availableTo?
  }

  index = [
    {type: "primary", field: [{name: "id"}]}
    {type: "btree|unique", field: [{name: "userId"}]}
  ]
  guid = "Rt-nzTy34pO13yY5ukvbLh5KU0o"
}
