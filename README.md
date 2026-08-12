# Smart Pantry Manager

An Android application that helps reduce household food waste by tracking the
ingredients a user already has at home and suggesting recipes they can cook using
**strictly** those ingredients — no shopping trip required.

Developed by **Novithan Naidoo** for Mobile App Development 700, Richfield Graduate
Institute of Technology.

---

## The problem it solves

Food gets thrown away because people forget what is in the cupboard, or they find a
recipe online and discover halfway through that they are missing two ingredients.
Smart Pantry Manager inverts that: instead of picking a recipe and then shopping, the
user records what they already own and the app tells them what is genuinely cookable
right now.

## Core feature — strict ingredient matching

A recipe is only suggested when **every single ingredient it requires is present in the
pantry, in at least the required quantity**. If a recipe needs five ingredients and the
pantry contains four of them, that recipe is excluded. There are no "almost there"
suggestions in the main list.

To handle everyday untidiness in how people type things, ingredient names are
normalised before comparison — case and surrounding whitespace are ignored, and simple
plurals are reduced to their singular form so that `Tomatoes` and `tomato` are treated
as the same ingredient. Quantities are converted to a common base unit before being
compared, so `1 kg` correctly satisfies a requirement for `500 g`.

A separate **Almost There** list shows recipes missing exactly one ingredient. It is
kept clearly apart from the strict suggestions so the two are never confused.

## Features

- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- Pantry list backed by a `RecyclerView` with a custom adapter, reading live from the
  database
- 18 recipes seeded into the database automatically on first launch
- Suggested Recipes screen driven by the strict-matching rule
- Almost There list for recipes missing a single ingredient
- Recipe detail screen with the full ingredient list and preparation method
- Settings screen with an expiring-soon alert toggle
- Input validation on the ingredient entry form
- Friendly empty state when nothing in the pantry matches a recipe

## Database — SQLite, and why

The app uses **SQLite** through `SQLiteOpenHelper`, implemented locally on the device.

SQLite was chosen over Firebase and PostgreSQL for three reasons:

**It fits how the app is actually used.** A pantry is personal, single-user data that
never needs to sync between devices or be shared with anyone. The cloud features that
justify Firebase would go entirely unused here.

**It works with no network connection.** People check what they can cook while standing
in the kitchen, which is exactly the sort of place a phone signal tends to be poor. A
cloud-backed database would leave the app unusable at the precise moment it is needed.

**It removes an entire category of failure.** No API keys, no backend to host, no
authentication layer, no service outage. The database ships inside Android itself, so
there is nothing to install or configure and nothing that can stop working.

The trade-off is that data lives on one device only — uninstalling the app loses the
pantry. For a personal, offline, single-user utility, that is an acceptable cost.

## Screens

| Screen | Purpose |
|---|---|
| Pantry List | Shows all current ingredients from the database |
| Add / Edit Ingredient | Form for creating and updating pantry items |
| Suggested Recipes | Recipes that pass the strict-matching rule |
| Recipe Detail | Full ingredients and method for one recipe |
| Settings | Expiring-soon alert preference |

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

The recipe database is seeded automatically the first time the app launches — no
manual setup or import is needed.

## Technical details

| | |
|---|---|
| Language | Java |
| Minimum SDK | API 24 (Android 7.0) |
| Compile / Target SDK | API 36 (Android 16) |
| Database | SQLite via `SQLiteOpenHelper` |
| Architecture | Activities with a shared database helper |

No mapping SDK, location services or GPS features are used anywhere in this
application.

## Project structure

```
app/src/main/java/com/example/smartpantrymanager/
├── model/          PantryItem and Recipe data classes
├── data/           DatabaseHelper — schema, CRUD and recipe seeding
├── adapter/        Custom RecyclerView adapters
├── logic/          RecipeMatcher — the strict-matching algorithm
└── ui/             Activities for each screen
```

## Author

Novithan Naidoo — BSc Information Technology, Final Year
Richfield Graduate Institute of Technology
