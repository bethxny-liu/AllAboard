# UML Class Diagrams (Main Classes)

These diagrams focus on the most important classes in the layered architecture:

- **Entities/Models** (Domain + Data layer interfaces)
- **ViewModels** (Presentation layer, with supported screens)
- **Relationships between classes**

## 1) Domain + Data Layer

```mermaid
classDiagram
    direction LR

    namespace "Domain Layer (Entities / Models)" {
        class User {
          +String id
          +String displayName
          +String email
          +BudgetLevel budget
          +TravelVibe travelVibe
          +Set~String~ interests
          +String? imageUrl
        }
        class Trip {
          +String id
          +String title
          +String destination
          +String region
          +String startDate
          +String endDate
          +String? imageUrl
          +TripStatus status
          +List~User~ members
        }
        class Activity {
          +String id
          +String title
          +String location
          +String? description
          +Float rating
          +String priceLevel
          +String? mapPinLabel
          +String? imageUrl
          +String? link
          +ActivityType type
          +String? preference
          +Double? latitude
          +Double? longitude
          +String? addedBy
        }
        class Vote {
          +String? id
          +String activityId
          +String userId
          +String tripId
          +VoteType voteType
        }
        class ActivityVoteResult {
          +Activity activity
          +Int yesVotes
          +Int noVotes
          +Int totalVotes
          +Float yesPercentage
          +Boolean isComplete
          +Boolean isConfirmed
          +List~String~ voterNames
        }
        class Itinerary {
          +String tripId
          +List~ItineraryDay~ days
        }
        class ItineraryDay {
          +String date
          +Int dayNumber
          +List~ScheduledActivity~ activities
        }
        class ScheduledActivity {
          +Activity activity
          +String startTime
          +String endTime
          +String notes
        }
        class TripDashboard {
          +Trip? trip
          +List~Activity~ activities
          +List~ActivityVoteResult~ votingResults
          +Itinerary? itinerary
        }

        class AllAboardModel {
          +getTrip(tripId)
          +getTripDashboard(tripId)
          +getActivity(activityId)
          +createTrip(...)
          +createActivityForTrip(...)
          +voteOnActivity(...)
          +getVotingResults(tripId)
          +getItinerary(tripId)
          +getCurrentUser()
          +updateUserPreferences(...)
          +signInWithGoogle()
          +logout()
        }
    }

    namespace "Data Layer (Repository Interfaces)" {
        class TripRepository <<interface>>
        class ActivityRepository <<interface>>
        class VoteRepository <<interface>>
        class UserRepository <<interface>>
        class ItineraryRepository <<interface>>
        class DatabaseRepository <<interface>>
    }

    Trip "1" *-- "0..*" User : members
    ActivityVoteResult "1" *-- "1" Activity
    Itinerary "1" *-- "1..*" ItineraryDay
    ItineraryDay "1" *-- "0..*" ScheduledActivity
    ScheduledActivity "1" *-- "1" Activity
    TripDashboard "1" --> "0..1" Trip
    TripDashboard "1" --> "0..*" Activity
    TripDashboard "1" --> "0..*" ActivityVoteResult
    TripDashboard "1" --> "0..1" Itinerary

    AllAboardModel --> TripRepository
    AllAboardModel --> ActivityRepository
    AllAboardModel --> VoteRepository
    AllAboardModel --> UserRepository
    AllAboardModel --> ItineraryRepository
    AllAboardModel --> DatabaseRepository
```

## 2) Presentation Layer (ViewModels by Screen)

```mermaid
classDiagram
    direction LR

    namespace "Presentation Layer (ViewModels)" {
        class LoginViewModel {
          <<LoginScreen>>
        }
        class HomeViewModel {
          <<HomeScreen>>
        }
        class ProfileViewModel {
          <<ProfileScreen>>
        }
        class OnboardingViewModel {
          <<OnboardingScreen>>
        }
        class CreateTripViewModel {
          <<CreateTripScreen>>
        }
        class TripHomeViewModel {
          <<TripHomeScreen>>
        }
        class SwipingViewModel {
          <<SwipingScreen>>
        }
        class SwipingResultsViewModel {
          <<SwipingResultsScreen>>
        }
        class ItineraryViewModel {
          <<ItineraryScreen>>
        }
        class CreateCustomActivityViewModel {
          <<CreateCustomActivityScreen>>
        }
        class ActivityDetailsViewModel {
          <<ActivityDetailsScreen>>
        }
    }

    namespace "Domain Layer" {
        class AllAboardModel
    }

    LoginViewModel --> AllAboardModel
    HomeViewModel --> AllAboardModel
    ProfileViewModel --> AllAboardModel
    OnboardingViewModel --> AllAboardModel
    CreateTripViewModel --> AllAboardModel
    TripHomeViewModel --> AllAboardModel
    SwipingViewModel --> AllAboardModel
    SwipingResultsViewModel --> AllAboardModel
    ItineraryViewModel --> AllAboardModel
    CreateCustomActivityViewModel --> AllAboardModel
    ActivityDetailsViewModel --> AllAboardModel
```

## Screen-to-ViewModel Map

- `LoginScreen` -> `LoginViewModel`
- `HomeScreen` -> `HomeViewModel`
- `ProfileScreen` -> `ProfileViewModel`
- `OnboardingScreen` -> `OnboardingViewModel`
- `CreateTripScreen` -> `CreateTripViewModel`
- `TripHomeScreen` -> `TripHomeViewModel`
- `SwipingScreen` -> `SwipingViewModel`
- `SwipingResultsScreen` -> `SwipingResultsViewModel`
- `ItineraryScreen` -> `ItineraryViewModel`
- `CreateCustomActivityScreen` -> `CreateCustomActivityViewModel`
- `ActivityDetailsScreen` -> `ActivityDetailsViewModel`
