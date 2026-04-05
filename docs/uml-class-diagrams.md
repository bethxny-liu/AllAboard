# UML Class Diagrams (Main Classes)

These diagrams focus on the most important classes in the layered architecture:

- **Entities/Models** (Domain + Data layer interfaces)
- **ViewModels** (Presentation layer, with supported screens)
- **Relationships between classes**

## 1) Domain + Data Layer

```mermaid
classDiagram
    direction LR

    class User
    class Trip
    class Activity
    class Vote
    class ActivityVoteResult
    class Itinerary
    class ItineraryDay
    class ScheduledActivity
    class TripDashboard

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

    class TripRepository
    class ActivityRepository
    class VoteRepository
    class UserRepository
    class ItineraryRepository
    class DatabaseRepository

    <<interface>> TripRepository
    <<interface>> ActivityRepository
    <<interface>> VoteRepository
    <<interface>> UserRepository
    <<interface>> ItineraryRepository
    <<interface>> DatabaseRepository

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

    class LoginScreen
    class HomeScreen
    class ProfileScreen
    class OnboardingScreen
    class CreateTripScreen
    class TripHomeScreen
    class SwipingScreen
    class SwipingResultsScreen
    class ItineraryScreen
    class CreateCustomActivityScreen
    class ActivityDetailsScreen

    class LoginViewModel
    class HomeViewModel
    class ProfileViewModel
    class OnboardingViewModel
    class CreateTripViewModel
    class TripHomeViewModel
    class SwipingViewModel
    class SwipingResultsViewModel
    class ItineraryViewModel
    class CreateCustomActivityViewModel
    class ActivityDetailsViewModel

    class AllAboardModel

    LoginScreen --> LoginViewModel : supports
    HomeScreen --> HomeViewModel : supports
    ProfileScreen --> ProfileViewModel : supports
    OnboardingScreen --> OnboardingViewModel : supports
    CreateTripScreen --> CreateTripViewModel : supports
    TripHomeScreen --> TripHomeViewModel : supports
    SwipingScreen --> SwipingViewModel : supports
    SwipingResultsScreen --> SwipingResultsViewModel : supports
    ItineraryScreen --> ItineraryViewModel : supports
    CreateCustomActivityScreen --> CreateCustomActivityViewModel : supports
    ActivityDetailsScreen --> ActivityDetailsViewModel : supports

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
