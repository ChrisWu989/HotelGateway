# HotelGateway — AI-Powered Hotel Management Chatbot

A **Spring Boot** backend that combines hotel management APIs with an intelligent AI chatbot for hotel homepages. Uses **OpenAI** for natural language processing and **pgvector** for semantic search over hotel data and policy documents.

---

## Overview

HotelGateway provides an AI-driven customer service layer for hotels. Guests can ask natural-language questions about room availability, pricing, amenities, check-in/out policies, and more — and receive contextually accurate, session-aware responses. Hotel staff get a full CRUD API for managing hotels, rooms, reviews, and embeddings.

---

## Features

- **Conversational AI Chatbot** — Session-based chat with conversation history, context tracking (remembers which hotel was last discussed), and semantic search over hotel data and PDF policy documents
- **Semantic Search** — Vector embeddings (pgvector + OpenAI `text-embedding-3-small`) enable similarity-based hotel recommendations
- **PDF Knowledge Base** — Upload hotel policy/FAQ PDFs; they are chunked and embedded for retrieval during chat
- **Hotel Management API** — Full CRUD for hotels, rooms, amenities, and reviews
- **Advanced Search & Filtering** — Filter hotels by star rating, price range, amenities, city, state, or free-text
- **Room Availability Tracking** — Query available rooms by type, price range, or minimum count; update availability after bookings
- **Review System** — Create and retrieve guest reviews with per-hotel average rating calculation

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.0 |
| AI / NLP | Spring AI 1.0.0-M4, OpenAI GPT-4o-mini |
| Embeddings | OpenAI `text-embedding-3-small` (1536 dimensions) |
| Vector Store | pgvector 0.1.4 (HNSW index, cosine distance) |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| PDF Processing | Apache PDFBox 3.0.0 |
| Build | Maven |

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   REST API Layer                     │
│  /api/chatbot  /api/hotels  /api/hotel-rooms         │
│  /api/reviews  /api/embeddings  /api/pdf             │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│                  Service Layer                       │
│  ChatbotService  EmbeddingService  PDFEmbeddingService│
│  HotelService    HotelRoomService  ReviewService     │
└────────────────────────┬────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────┐
│            PostgreSQL + pgvector                     │
│   hotel_db   <->   vector store (1536-dim HNSW)      │
└─────────────────────────────────────────────────────┘
```

---

## Project Structure

```
src/main/java/com/synex/
├── HotelGatewayApplication.java
├── config/
│   ├── AIConfig.java               # ChatClient bean
│   └── DataSourceConfig.java       # Shared DataSource for JPA + pgvector
├── controller/
│   ├── ChatBotController.java
│   ├── HotelController.java
│   ├── HotelRoomController.java
│   ├── ReviewController.java
│   ├── EmbeddingController.java
│   └── PDFController.java
├── entity/
│   ├── Hotel.java
│   ├── HotelRoom.java
│   ├── Review.java
│   ├── Amenities.java
│   └── RoomType.java
├── repository/
│   ├── HotelRepository.java        # Complex JPQL search/filter queries
│   ├── HotelRoomRepository.java
│   ├── ReviewRepository.java
│   ├── AmenitiesRepository.java
│   └── RoomTypeRepository.java
└── service/
    ├── ChatbotService.java         # Session context + history pruning
    ├── EmbeddingService.java       # Hotel -> vector document
    ├── PDFEmbeddingService.java    # PDF chunking + embedding
    ├── HotelService.java
    ├── HotelRoomService.java
    └── ReviewService.java
```

---

## API Reference

### Chatbot — `/api/chatbot`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/chat` | Send a message; returns AI response |
| POST | `/clear/{sessionId}` | Clear conversation history |

**Chat request/response:**
```json
// Request
{ "sessionId": "abc123", "message": "What rooms are available at the Grand Hotel?" }

// Response
{ "sessionId": "abc123", "userMessage": "...", "botResponse": "..." }
```

---

### Hotels — `/api/hotels`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | All hotels |
| GET | `/{id}` | Hotel by ID |
| GET | `/search?searchLocation=&searchType=` | Search by name/city/state/address |
| GET | `/filter?searchLocation=&minStarRating=&maxStarRating=&minPrice=&maxPrice=&amenities=` | Advanced filter |
| POST | `/` | Create hotel |
| PUT | `/{id}` | Update hotel |
| DELETE | `/{id}` | Delete hotel |
| PUT | `/{id}/increment-bookings` | Increment booking counter |

---

### Hotel Rooms — `/api/hotel-rooms`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | All rooms |
| GET | `/{id}` | Room by ID |
| GET | `/hotel/{hotelId}` | Rooms for a hotel |
| GET | `/available?minRooms=` | Rooms with minimum availability |
| GET | `/price-range?minPrice=&maxPrice=` | Rooms by price range |
| POST | `/` | Create room |
| PUT | `/{id}` | Update room |
| DELETE | `/{id}` | Delete room |
| PUT | `/{id}/update-availability` | Update room count after booking |

---

### Reviews — `/api/reviews`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/hotel/{hotelId}` | Reviews for a hotel |
| GET | `/user/{userId}` | Reviews by user |
| GET | `/hotel/{hotelId}/average-rating` | Average rating |
| POST | `/` | Create review |
| PUT | `/{id}` | Update review |
| DELETE | `/{id}` | Delete review |

---

### Embeddings — `/api/embeddings`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/generate-all` | Embed all hotels |
| POST | `/generate/{hotelId}` | Embed a single hotel |
| GET | `/search?query=&limit=` | Semantic similarity search |
| DELETE | `/{hotelId}` | Delete hotel embedding |

---

### PDF — `/api/pdf`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/embed` | Process all PDFs from `resources/pdfs/` and embed |

---

## Setup & Configuration

### Prerequisites

- Java 17+
- PostgreSQL with the [pgvector extension](https://github.com/pgvector/pgvector) enabled
- An OpenAI API key
- Maven 3.8+

### Database Setup

```sql
CREATE DATABASE hotel_db;
\c hotel_db
CREATE EXTENSION vector;
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# OpenAI
spring.ai.openai.api-key=YOUR_OPENAI_API_KEY
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.embedding.options.model=text-embedding-3-small

# Server
server.port=8282
```

### Run

```bash
# Clone the repo
git clone https://github.com/your-username/HotelGateway.git
cd HotelGateway

# Build and run
mvn spring-boot:run
```

The API will be available at `http://localhost:8282`.

> **Note:** The application is configured with `-Xmx4g -Xms2g` JVM args for PDF embedding workloads. Adjust in your run configuration as needed.

### Adding PDF Documents

Place PDF files (hotel policies, FAQs, etc.) in `src/main/resources/pdfs/`, then call:

```bash
POST http://localhost:8282/api/pdf/embed
```

---

## How the Chatbot Works

1. **User sends a message** with a `sessionId` to `/api/chatbot/chat`
2. **ChatbotService** retrieves or creates a `ConversationContext` for that session (stores the last 20 messages / 10 exchanges)
3. The message is enriched with context — the last hotel discussed is injected so follow-up questions like *"Does it have a pool?"* resolve correctly
4. **Semantic search** runs against both hotel embeddings and PDF chunk embeddings in pgvector
5. Retrieved context + conversation history are sent to **OpenAI GPT-4o-mini** with a system prompt that prevents hallucinations
6. The response is cleaned (markdown stripped) and returned

---

## Expected Outcomes

- Automated 24/7 guest query handling
- Faster query resolution without human agents
- Improved booking experience with intelligent room recommendations
- Increased revenue through personalized upselling
- Scalable foundation for multilingual support and human-agent escalation

---

## License

This project was developed as part of a SynergisticIT training program.


## License

MIT License — see [LICENSE](LICENSE) for details.
