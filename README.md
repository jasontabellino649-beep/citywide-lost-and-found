# Citywide Lost & Found Management System

A Java Swing desktop application that helps people across an entire city report lost items, register found items, search for missing belongings, contact finders, and claim recovered items. It has separate **citizen** and **administrator** interfaces and is built to demonstrate the four pillars of object-oriented programming.

Reports are not tied to a single school or building. Every report records a **city district** (Downtown, Airport, Harbor & Port, Transit Hub, Shopping Malls, Parks, and more) plus the specific place, so items can be searched city-wide.

## Features

| # | Feature | Description |
|---|---------|-------------|
| 1 | Report lost items | Name, category, description, district, place, date lost, optional reward |
| 2 | Register found items | Same details, plus where the item is currently kept |
| 3 | Search and filters | Keyword, type, category, district, status, and date range |
| 4 | Contact the reporter | In-app messaging with an inbox, unread badge, and replies |
| 5 | Claim found items | Submit proof of ownership; an admin approves or rejects |
| 6 | Status tracking | Open, Claim Pending, Claimed / Returned, Closed |
| 7 | Smart match suggestions | Scores lost and found reports by category, district, date, and shared keywords |
| 8 | Admin tools | Review claims, update or delete reports, statistics dashboard, user list |
| 9 | Auto-save | Data is saved to `lostfound.dat` automatically |

## User and admin interfaces

**Citizen tabs:** Browse & Search, Report Lost Item, Register Found Item, My Activity, Inbox

**Administrator tabs:** all of the above, plus Claims Review, Statistics, and Users

## Demo accounts

| Role | Username | Password |
|------|----------|----------|
| Administrator | `admin` | `admin123` |
| Citizen | `user` | `user123` |
| Citizen | `maria` | `maria123` |

New citizens can also create their own account from the login screen. Sample reports are preloaded the first time the app runs.

## Requirements

- JDK 11 or newer
- Any OS that supports Java Swing (Windows, macOS, Linux)

## How to run

### From the command line

```bash
javac *.java
java LostFoundSystem
```

### From IntelliJ IDEA

1. Open the project folder (**File > Open**).
2. If the files show errors, right-click the folder and choose **Mark Directory as > Sources Root**, and make sure a JDK is set under **File > Project Structure**.
3. Open `LostFoundSystem.java` and click the green run arrow next to `main`.

The `main` method is in `LostFoundSystem.java`.

## OOP concepts demonstrated

| Pillar | Where it appears |
|--------|------------------|
| **Encapsulation** | Private fields with getters, hashed passwords (SHA-256), and all business rules kept inside `LostFoundService` |
| **Inheritance** | `User` is extended by `RegularUser` and `Admin`; `Item` is extended by `LostItem` and `FoundItem` |
| **Polymorphism** | `getType()`, `getExtraInfo()`, `isClaimable()`, and `canModerate()` behave differently for each subclass |
| **Abstraction** | Abstract classes `User` and `Item`; interfaces `Searchable` and `Refreshable` |

## Input validation and error handling

- `Validator` checks text lengths, date format (`yyyy-MM-dd`), no future dates, and email or phone format.
- Business rules are enforced in the service layer: no claiming your own item, no duplicate pending claims, no messaging yourself, and admin-only actions.
- Custom exceptions `ValidationException` and `AuthenticationException` are caught in the GUI and shown as clear error dialogs.
- Loading and saving the data file are wrapped in error handling, and the app falls back to sample data if the saved file cannot be read.

## Project structure

```
LostFoundSystem/
|-- LostFoundSystem.java        Entry point (main)
|
|-- User.java                   Abstract base class for people
|-- RegularUser.java            Citizen account
|-- Admin.java                  Administrator account
|
|-- Item.java                   Abstract base class for reports
|-- LostItem.java               Lost item report
|-- FoundItem.java              Found item report
|
|-- Claim.java                  Ownership claim
|-- Message.java                In-app message
|-- Location.java               District + specific place
|-- MatchResult.java            Possible match with a score
|-- SearchFilter.java           Search criteria
|
|-- Category.java               Enum: item categories
|-- District.java               Enum: city districts
|-- ItemStatus.java             Enum: report status
|-- ClaimStatus.java            Enum: claim status
|
|-- Searchable.java             Interface
|-- Refreshable.java            Interface
|
|-- Validator.java              Input validation
|-- ValidationException.java    Custom exception
|-- AuthenticationException.java Custom exception
|
|-- LostFoundService.java       Business logic
|-- DataStore.java              Save and load (serialization)
|
|-- Ui.java                     GUI helper methods
|-- LoginFrame.java             Login and registration window
|-- MainFrame.java              Main window with tabs
|-- BrowsePanel.java            Search and browse tab
|-- ReportFormPanel.java        Lost and found report forms
|-- MyActivityPanel.java        My reports and claims
|-- InboxPanel.java             Messages
|-- ClaimsReviewPanel.java      Admin: review claims
|-- StatsPanel.java             Admin: statistics
|-- UsersPanel.java             Admin: user list
|
|-- LostFoundSystem_UML.mermaid UML class diagram
```

## UML class diagram

The full class diagram is in [`LostFoundSystem_UML.mermaid`](LostFoundSystem_UML.mermaid). GitHub can display it directly, or you can paste its contents into [mermaid.live](https://mermaid.live).

## Data storage

Data is stored in `lostfound.dat` in the folder where you run the program. Delete this file to reset the system back to the sample data.

## Author

Your Name

## License

For educational use.
