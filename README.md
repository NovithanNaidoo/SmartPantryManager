# Smart Pantry Manager

An Android application that helps reduce household food waste by tracking the
ingredients a user already has at home and suggesting recipes they can cook using
**strictly** those ingredients no shopping trip required.

Built in Java for Mobile App Development 700, Richfield Graduate Institute of
Technology.

**Author:** Novithan Naidoo

---

## The problem it solves

Food goes to waste as it's forgotten in the cupboard or is found to be spoiled. Finding
a recipe online and halfway through realizing, you've missed two ingredients. With smart
Pantry Management, the order is reversed: you first shop and then cook. Users take inventory 
of what they have at home and the app informs them about what is 
realistically cookable at the moment.

## Core feature — strict ingredient matching

A recipe is only suggested when **every single ingredient it requires is present in
the pantry, in at least the required quantity**. If a recipe needs five ingredients
and the pantry contains four of them, that recipe is excluded from the suggestions.

Real people do not type things consistently, so names and units are cleaned up before
being compared:

| Situation | How it is handled |
|---|---|
| "Tomatoes" vs "tomato" | Lowercased, trimmed, simple plurals reduced to singular |
| 1 kg in the pantry, 500 g needed | Both converted to grams, then compared |
| 2 tbsp needed, 100 ml held | Both converted to millilitres |
| 500 g held, 2 tbsp needed | Mass and volume cannot be compared, so the ingredient counts as present without a quantity check |

That last row is a deliberate trade-off. Butter is sold by weight but measured in
tablespoons, and refusing to match those would be more wrong than allowing it.

A separate **Almost There** list shows recipes missing exactly one ingredient, and
names the missing item. It is kept visually apart from the strict suggestions so the
two can never be confused.

## Screens

| Screen | Purpose |
|---|---|
| Pantry List | All current ingredients, with expiring items highlighted |
| Add / Edit Ingredient | One form for both, with input validation |
| Suggested Recipes | Strict matches, plus the separate Almost There list |
| All Recipes | The full cookbook, showing how close each recipe is |
| Recipe Detail | Ingredients with have/missing indicators, and the method |
| Settings | Expiry alert preferences |

Navigation is a bottom bar across Pantry, Suggested, Recipes and Settings.

## Features

- Full CRUD on pantry items — add, view, edit and delete
- Data persists between sessions, stored on the device
- 18 recipes seeded into the database automatically on first launch
- Strict matching rule, with plural and unit handling
- Almost There list for recipes one ingredient short
- Tick and cross indicators showing which ingredients you have
- Expiring-soon highlighting, with a configurable warning window
- Input validation with errors shown against the specific field
- Quantities accept decimals and fractions, e.g. `0.25` or `1/4`
- Meaningful empty states rather than blank screens

## Database — SQLite, and why

The app uses **SQLite** through `SQLiteOpenHelper`, implemented locally on the device.

SQLite was chosen over Firebase and PostgreSQL for three reasons:

**It fits how the app is actually used.** A pantry is personal, single-user data that
never needs to sync between devices or be shared. The cloud features that justify
Firebase would go entirely unused.

**It works with no network connection.** People check what they can cook while
standing in the kitchen, which is exactly where a phone signal tends to be poor. A
cloud-backed database would leave the app unusable at the moment it is most needed.

**It removes a whole category of failure.** No API keys, no backend to host, no
authentication layer, no service outage. SQLite ships inside Android itself, so there
is nothing to install and nothing that can stop working.

The trade-off is that data lives on one device only — uninstalling the app loses the
pantry. For a personal, offline, single-user utility, that is an acceptable cost.

### Data model

```
pantry_items                recipes                recipe_ingredients
-----------                 -------                ------------------
id            PK            id         PK          id            PK
name                        name                   recipe_id     FK -> recipes.id
quantity                    steps                  name
unit                                               quantity
expiry_date   (nullable)                           unit
```

Unlike, ingredients possess properties that are not shared with other ingredients.
A recipe requires numerous ingredients while one column can provide only one value. 
The ingredient row refers back to the recipe, which is a one-to-many relationship.
`ON DELETE CASCADE` means deleting a recipe removes its ingredients automatically.

User settings are stored separately in SharedPreferences, since they are a couple of
small values rather than records.

## Requirements

- Android Studio (Ladybug or newer)
- JDK 11
- An Android device or emulator running **Android 7.0 (API 24)** or higher

## Setup and run

1. Clone the repository:
   ```bash
   git clone https://github.com/NovithanNaidoo/SmartPantryManager.git
   ```
2. Open the project folder in Android Studio via **File → Open**.
3. Wait for Gradle to finish syncing. If it does not start on its own, use
   **File → Sync Project with Gradle Files**.
4. Connect an Android device with USB debugging enabled, or start an emulator.
5. Press **Run** (`Shift+F10`).

The recipe database is seeded automatically the first time the app launches. No
manual setup or import is needed.

### Trying the strict matching rule

1. Add these to the pantry: eggs (12 piece), butter (500 g), milk (2 l),
   flour (1 kg), salt (500 g)
2. Open **Suggested** — Scrambled Eggs appears
3. Pancakes sits under **Almost There**, missing sugar
4. Add sugar (500 g) — Pancakes moves into the main list
5. Delete the sugar — Pancakes drops back to Almost There

## Technical details

| | |
|---|---|
| Language | Java |
| Minimum SDK | API 24 (Android 7.0) |
| Compile / Target SDK | API 36 (Android 16) |
| Database | SQLite via `SQLiteOpenHelper` |
| Settings storage | SharedPreferences |
| Architecture | Activities, with matching logic in its own package |

No mapping SDK, location services or GPS features are used anywhere in this
application. The app requests no permissions at all.

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
├── model/      PantryItem, Recipe, RecipeIngredient — plain data classes
├── data/       DatabaseHelper (schema, CRUD, seeding), AppPreferences
├── adapter/    PantryAdapter, RecipeAdapter — RecyclerView adapters
├── logic/      RecipeMatcher (the strict rule), IngredientMatcher, ExpiryHelper
└── ui/         One Activity per screen, plus BottomNavHelper
```

The matching rule lives in `logic/RecipeMatcher.java`, separate from any screen. Both
the suggestions list and the detail screen ask the same class, so they can never
disagree about whether a recipe is cookable.

## Known limitations

- Mass and volume cannot be converted between each other, so an ingredient held in
  grams against a recipe asking for tablespoons is accepted without a quantity check
- Plural handling covers common English endings, not irregular ones
- Data is not backed up anywhere; uninstalling the app loses the pantry
- The recipe collection is fixed and cannot be added to from within the app

## Author

Novithan Naidoo — BSc Information Technology, Final Year
Richfield Graduate Institute of Technology
