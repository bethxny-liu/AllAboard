# UML Class Diagrams (Main Classes)

These diagrams focus on the most important classes in the layered architecture:

- **Entities/Models** (Domain + Data layer interfaces)
- **ViewModels** (Presentation layer, with supported screens)
- **Relationships between classes**

## 1) Domain + Data Layer

```mermaid
classDiagram
    direction LR

    namespace Domain_Layer_Entities_Models {
        class User {
          +String id
          +String displayName
          +String email
          +BudgetLevel budget
          +TravelVibe travelVibe
          +Set~String~ interests
          +String imageUrl
        }
        class Trip {
          +String id
          +String title
          +String destination
          +String region
          +String startDate
          +String endDate
          +String imageUrl
          +TripStatus status
          +List~User~ members
        }
        class Activity {
          +String id
          +String title
          +String location
          +String description
          +Float rating
          +String priceLevel
          +String mapPinLabel
          +String imageUrl
          +String link
          +ActivityType type
          +String preference
          +Double latitude
          +Double longitude
          +String addedBy
        }
        class Vote {
          +String id
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
          +Trip trip
          +List~Activity~ activities
          +List~ActivityVoteResult~ votingResults
          +Itinerary itinerary
        }
        class AllAboardModel {
          +getTrip(tripId): Trip
          +getTripDashboard(tripId): TripDashboard
          +getActivity(activityId): Activity
          +createTrip(...)
          +createActivityForTrip(...)
          +voteOnActivity(...)
          +getVotingResults(tripId): List~ActivityVoteResult~
          +getItinerary(tripId): Itinerary
          +getCurrentUser(): User
          +updateUserPreferences(...)
          +signInWithGoogle()
          +logout()
        }
    }

    namespace Data_Layer_Repository_Interfaces {
        class TripRepository {
          <<interface>>
          +getTrip(tripId)
          +getTripsForUser()
          +createTrip(trip)
          +updateTrip(trip)
          +deleteTrip(tripId)
          +joinTrip(tripId)
          +removeMemberFromTrip(tripId, userId)
          +getTripDashboard(tripId)
        }
        class ActivityRepository {
          <<interface>>
          +getActivity(activityId)
          +getActivitiesForTrip(tripId)
          +addActivity(tripId, activity)
          +updateActivity(activity)
          +deleteActivity(activityId)
        }
        class VoteRepository {
          <<interface>>
          +submitVote(vote)
          +getVotingResultsForTrip(tripId)
          +getVotedActivityIds(tripId, userId)
        }
        class UserRepository {
          <<interface>>
          +getCurrentUser()
          +setCurrentUserId(userId)
          +updateUserPreferences(userId, budget, vibe, interests)
          +clearCache()
        }
        class ItineraryRepository {
          <<interface>>
          +getItinerary(tripId)
          +regenerateItinerary(tripId)
          +updateScheduledActivity(tripId, date, scheduledActivity)
          +exportToGoogleCalendar(tripId, token, timeZone, calendarId)
        }
        class DatabaseRepository {
          <<interface>>
          +signInWithGoogle()
          +logout()
        }
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

    class PresentationLayer
    class DomainLayer

    class LoginScreenContext
    class HomeScreenContext
    class ProfileScreenContext
    class OnboardingScreenContext
    class CreateTripScreenContext
    class TripHomeScreenContext
    class SwipingScreenContext
    class SwipingResultsScreenContext
    class ItineraryScreenContext
    class CreateCustomActivityScreenContext
    class ActivityDetailsScreenContext

    class LoginViewModel {
      +signIn()
    }
    class HomeViewModel {
      +onSearchQueryChange(query)
    }
    class ProfileViewModel {
      +logout()
    }
    class OnboardingViewModel {
      +savePreferences(...)
    }
    class CreateTripViewModel {
      +initialize(mode, tripId)
      +onCreateOrUpdateTrip(...)
    }
    class TripHomeViewModel {
      +refresh()
      +deleteTrip()
    }
    class SwipingViewModel {
      +refresh()
      +vote(voteType)
    }
    class SwipingResultsViewModel {
      +refresh()
      +onCategorySelected(index)
    }
    class ItineraryViewModel {
      +refresh()
      +exportAllDaysToGoogleCalendar()
    }
    class CreateCustomActivityViewModel {
      +onCreateOrUpdateActivity()
    }
    class ActivityDetailsViewModel {
      +refresh()
      +deleteActivity()
    }

    class AllAboardModel

    PresentationLayer ..> LoginViewModel : contains
    PresentationLayer ..> HomeViewModel : contains
    PresentationLayer ..> ProfileViewModel : contains
    PresentationLayer ..> OnboardingViewModel : contains
    PresentationLayer ..> CreateTripViewModel : contains
    PresentationLayer ..> TripHomeViewModel : contains
    PresentationLayer ..> SwipingViewModel : contains
    PresentationLayer ..> SwipingResultsViewModel : contains
    PresentationLayer ..> ItineraryViewModel : contains
    PresentationLayer ..> CreateCustomActivityViewModel : contains
    PresentationLayer ..> ActivityDetailsViewModel : contains
    DomainLayer ..> AllAboardModel : contains

    LoginScreenContext --> LoginViewModel : supports
    HomeScreenContext --> HomeViewModel : supports
    ProfileScreenContext --> ProfileViewModel : supports
    OnboardingScreenContext --> OnboardingViewModel : supports
    CreateTripScreenContext --> CreateTripViewModel : supports
    TripHomeScreenContext --> TripHomeViewModel : supports
    SwipingScreenContext --> SwipingViewModel : supports
    SwipingResultsScreenContext --> SwipingResultsViewModel : supports
    ItineraryScreenContext --> ItineraryViewModel : supports
    CreateCustomActivityScreenContext --> CreateCustomActivityViewModel : supports
    ActivityDetailsScreenContext --> ActivityDetailsViewModel : supports

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

Note: `*ScreenContext` nodes indicate mapping context only (not UI compose implementation classes).

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
