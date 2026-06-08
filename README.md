# Smart Rental Recommendation Platform

## Project Overview

The Smart Rental Recommendation Platform is a Spring Boot backend that supports the complete apartment rental lifecycle for renters and property owners.

Renters can create profiles and housing preferences, resolve a workplace name into coordinates, receive ranked apartment recommendations, reserve apartments, accept contracts, communicate with owners, submit maintenance requests, write reviews, and find compatible roommates.

Owners can manage buildings and apartments, process reservations and contracts, communicate with renters, handle maintenance requests, review apartment performance, and access AI-generated summaries.

The recommendation engine keeps apartment selection under backend control. It filters and scores apartments using user preferences, rent, family suitability, nearby amenities, and commute information. OpenAI receives the final ranked results only to generate a readable explanation.

## Main Business Flow

1. Owners create buildings and apartments.
2. Users create profiles and rental preferences.
3. Nominatim converts workplace names into coordinates.
4. The backend ranks suitable apartments using preferences, Overpass amenity data, and OSRM commute data.
5. Users submit reservations for available apartments.
6. Owners approve or reject pending reservations.
7. A contract is created for an approved reservation.
8. The user accepts the contract and the apartment becomes rented.
9. Users and owners communicate through apartment conversations.
10. Active tenants can submit AI-classified maintenance requests.
11. When a contract ends, the reservation is completed and the apartment enters maintenance mode.
12. After maintenance, the owner returns the apartment to available status.
13. Users can review completed rentals.

## Main Technologies

- Java and Spring Boot
- Spring Web
- Spring Data JPA
- Jakarta Bean Validation
- MySQL
- Lombok
- RestTemplate
- Jackson ObjectMapper
- OpenAI API for AI explanations and analysis
- Overpass API for nearby services
- OSRM API for commute distance and duration
- Nominatim API for workplace geocoding
- Spring Mail for contract PDF email delivery
- UltraMsg for WhatsApp workflow notifications

## Apartment and Rental Status Flow

```text
Apartment:   AVAILABLE -> RESERVED -> RENTED -> UNDER_MAINTENANCE -> AVAILABLE
Reservation: PENDING -> APPROVED -> COMPLETED
Contract:    PENDING -> ACTIVE -> ENDED
Maintenance: PENDING -> IN_PROGRESS -> COMPLETED
```

Reservations and contracts may also be rejected, cancelled, expired, or terminated according to the current business rules.

## Extra Business Endpoints

```text
GET /api/v1/recommendation/recommend/{userId} // ai
GET /api/v1/apartment/next-available/{apartmentId}
GET /api/v1/apartment/available-on/{apartmentId}/{date}
PUT /api/v1/apartment/toggle-maintenance/{ownerId}/{apartmentId}
GET /api/v1/ai/owners/reputation-summary/{ownerId} // ai
GET /api/v1/ai/apartments/compare/{id1}/{id2} // ai
GET /api/v1/maintenance-request/user/{userId}
GET /api/v1/maintenance-request/apartment/{apartmentId}
GET /api/v1/maintenance-request/building-summary/{buildingId} // ai
POST /api/v1/maintenance-request/add/{userId}/{apartmentId}
PUT /api/v1/maintenance-request/start/{ownerId}/{requestId}
PUT /api/v1/maintenance-request/complete/{ownerId}/{requestId}
GET /api/v1/conversation/user/{userId}
GET /api/v1/conversation/owner/{ownerId}
GET /api/v1/message/conversation/{conversationId}
POST /api/v1/message/add/user/{userId}/{ownerId}/{apartmentId}
POST /api/v1/message/add/owner/{ownerId}/{userId}/{apartmentId}
PUT /api/v1/user-preferences/add-workplace/{userId}
```

## Person 1 Extra Endpoint Count

| Controller | Extra endpoints |
|---|---:|
| ApartmentController | 3 |
| RecommendationController | 1 |
| AiApartmentController | 2 |
| MaintenanceRequestController | 6 |
| ConversationController | 2 |
| MessageController | 3 |
| UserPreferenceController | 1 |
| **Person 1 Total** | **18** |

## External Integration Summary

| Integration | Use in the system |
|---|---|
| OpenAI | Recommendation explanations, apartment comparison, review summaries, owner analysis, contract analysis, maintenance classification, and roommate ranking. |
| Overpass | Counts nearby services such as schools, hospitals, supermarkets, pharmacies, gyms, and restaurants. |
| OSRM | Calculates driving duration and distance for recommendation commute scoring. |
| Nominatim | Converts a workplace name into latitude and longitude. |
| Spring Mail | Sends generated contract PDFs by email. |
| UltraMsg | Sends WhatsApp notifications during supported rental workflows. |

Google Places and Google Routes are not used by the current project.
