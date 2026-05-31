# Expense Tracker — Android

A full-featured personal finance app built with **Kotlin**, **Jetpack Compose**, and **Clean MVVM Architecture**. Designed as a portfolio-grade project demonstrating modern Android engineering practices.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean MVVM |
| DI | Hilt (KSP) |
| Database | Room 2.7 |
| Async | Coroutines + Flow + StateFlow |
| Navigation | Navigation Compose |
| State | StateFlow + collectAsStateWithLifecycle |
| Storage | DataStore Preferences |
| Testing | JUnit4, MockK, Turbine, Room Testing |

---

## Project Structure

```
app/src/main/java/com/push/dev/expensetracker/
├── data/
│   ├── local/
│   │   ├── dao/          ExpenseDao.kt
│   │   ├── entity/       ExpenseEntity.kt
│   │   └──               ExpenseDatabase.kt
│   ├── model/            Expense.kt, Category.kt, MonthlySummary.kt
│   └── repository/       ExpenseRepository.kt
├── di/                   AppModule.kt
├── ui/
│   ├── components/       CommonComponents.kt
│   │   └── charts/       PieChart.kt, BarChart.kt, LineChart.kt
│   ├── navigation/       Screen.kt, NavGraph.kt
│   ├── screens/
│   │   ├── dashboard/    DashboardScreen.kt
│   │   ├── addexpense/   AddEditExpenseScreen.kt
│   │   ├── history/      ExpenseHistoryScreen.kt
│   │   └── analytics/    AnalyticsScreen.kt
│   └── theme/            Color.kt, Theme.kt, Type.kt
├── util/                 CsvExporter.kt, DateUtils.kt, NotificationHelper.kt
├── viewmodel/            ExpenseViewModel.kt, ExpenseUiState.kt
├── ExpenseTrackerApp.kt
└── MainActivity.kt
```

---

## Features

### Core
- **Add / Edit / Delete** expenses with full form validation
- **6 categories**: Food, Travel, Shopping, Bills, Entertainment, Other
- **Date picker** (Material 3 DatePickerDialog)
- **Notes** per expense

### Dashboard
- Monthly total spending with previous-month navigation
- Category-wise pie chart with legend
- Recent expenses list
- Monthly budget tracker with progress bar and alert banner
- Average daily spending card

### History
- Full expense list with swipe-friendly cards
- Live **search** (title + notes)
- **Filter** by category (bottom sheet)
- **Sort** by date (asc/desc) or amount (asc/desc)
- **CSV export** via Android share sheet

### Analytics
- Monthly summary cards (total, count, daily avg, top category)
- **Pie chart** — category distribution
- **Bar chart** — 6-month spending trend (custom Canvas)
- **Line chart** — last 7-day daily trend (custom Canvas with gradient)

### Extras
- Dark mode support (Material 3 theming)
- Smooth enter/exit animations (slide + fade)
- Notification channel for budget alerts
- Splash screen (SplashScreen API)

---

## Architecture

```
UI (Compose) ←→ ViewModel ←→ Repository ←→ Room DAO ←→ SQLite
                    ↕
               DataStore (budget preference)
```

- **Repository** is the single source of truth; exposes `Flow` APIs
- **ViewModel** combines flows with `combine()` into screen-specific `StateFlow<UiState>`
- **Hilt** wires everything together; `@Singleton` on Repository and Database

---

## Running the Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

Test coverage includes:
- `ExpenseViewModelTest` — 10 unit tests (MockK + Turbine)
- `ExpenseRepositoryTest` — 8 unit tests (MockK)
- `ExpenseDaoTest` — 10 instrumented tests (in-memory Room)

---

## Setup

1. Clone the repo
2. Open in Android Studio Meerkat or later
3. Let Gradle sync (requires internet for dependency resolution)
4. Run on an emulator/device with API 29+

---

## Sample Data

The app starts empty. Use the **+** FAB on Dashboard or History to add expenses. To see analytics with rich charts, add 5–10 expenses across different categories and dates.

---
