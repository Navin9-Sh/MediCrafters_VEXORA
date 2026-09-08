<div align="center">

# MediWise

**Clinical Decision & Real-Time Consultation Platform**

An end-to-end telehealth system — native Android client, Spring Boot backend, and an operations dashboard — covering doctor discovery, appointment booking with race-condition-safe slot locking, real-time chat & WebRTC video signaling, AI-assisted symptom triage, and payments.

[![CI/CD](https://github.com/abhisheksharma-swe/MediWise-Clinical-Decision-and-Real-Time-Consultation-Platform/actions/workflows/ci.yml/badge.svg)](https://github.com/abhisheksharma-swe/MediWise-Clinical-Decision-and-Real-Time-Consultation-Platform/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![React](https://img.shields.io/badge/React-18.3-61DAFB?logo=react&logoColor=black)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

</div>

---

## Overview

MediWise connects patients with verified doctors for online and in-person consultations. A patient can search doctors by specialty, book an available slot, pay via Razorpay, chat or video-call their doctor in real time, and walk away with a permanent digital record of the consultation — diagnosis, prescription and notes — that any doctor they see next can pull up.

The system is split into three independently deployable pieces sharing one backend contract:

| Component | What it's for |
|---|---|
| **`android-app/`** | Patient-facing native app (Kotlin, Jetpack Compose) |
| **`backend/`** | REST + WebSocket API, business logic, persistence (Spring Boot) |
| **`mediwise-admin/`** | Internal ops console — doctor verification, audit trail, assignment (React) |

---

## Architecture

```
   ┌───────────────────┐              ┌───────────────────┐
   │     Android App     │              │   Admin Dashboard    │
   │  Kotlin · Compose      │              │   React · Vite          │
   └──────────┬────────┘              └──────────┬────────┘
              │                                     │
              │      REST (JWT)  +  STOMP/WebSocket    │
              └──────────────────┬──────────────────┘
                                  ▼
                   ┌───────────────────────────┐
                   │      Spring Boot 3 API       │
                   │    Java 17 · modular by domain │
                   │  auth · appointment · chat · ai  │
                   │ payment · doctor · schedule · admin │
                   └─────┬───────────┬───────────┬─────┘
                         │           │           │
             ┌───────────┘     ┌─────┘     └───────────┐
             ▼                 ▼                        ▼
      ┌─────────────┐   ┌─────────────┐         ┌─────────────┐
      │  PostgreSQL    │   │   MongoDB      │         │     Redis       │
      │ users, doctors,  │   │ chat history,    │         │  Redisson:        │
      │ appointments,     │   │ AI symptom logs,  │         │ slot locks,          │
      │ payments, slots     │   │ audit trail          │         │ search/chat cache      │
      └─────────────┘   └─────────────┘         └─────────────┘

External services: Firebase (Auth · FCM) · Razorpay (payments) · AWS S3 (medical files) · Google Gemini (symptom triage)
```

**Why three data stores?** Each does the job it's actually good at instead of forcing everything into one model:
- **PostgreSQL** — the transactional core (bookings, payments, doctor profiles) where relational integrity and Flyway-versioned migrations matter.
- **MongoDB** — high-volume, loosely-structured, append-heavy data: chat messages, AI symptom logs, and the audit trail written by every service call.
- **Redis (Redisson)** — short-lived state: slot locks during the booking flow, and TTL-based caches for doctor search and slot availability.

---

## Feature Highlights

**Patient experience**
- Search & filter doctors by specialty, rating and availability
- Book a time slot with a lock-then-confirm flow (see *Engineering Highlights*)
- Razorpay checkout with server-side payment verification
- Real-time in-consultation chat and WebRTC video/audio signaling
- AI-assisted symptom checker that recommends a specialty and urgency level before booking
- Full appointment history — chief complaint, doctor's notes, diagnosis and prescription — carried forward to every future consultation

**Doctor workflow**
- Own schedule and appointment queue, filterable by status
- Start / complete consultation lifecycle (`CONFIRMED → IN_PROGRESS → COMPLETED`) with clinical notes captured on completion
- **Cross-consultation patient history** — before seeing a new patient, a doctor can pull every past *completed* consultation for that patient (across all doctors, not just their own), so care isn't repeated blind. Access is scoped: a doctor can only see history for a patient they've actually had an appointment with.

**Platform & operations**
- Role-based access for `PATIENT`, `DOCTOR`, `ADMIN`
- Admin console for doctor verification, manual doctor assignment, and a full audit log viewer
- Push notifications via Firebase Cloud Messaging
- OpenAPI/Swagger documentation generated from the live codebase

---

## Engineering Highlights

A few design decisions worth calling out beyond the feature list:

- **Race-condition-safe booking.** Slots move through `AVAILABLE → LOCKED → BOOKED`. A slot is only lockable by one user at a time, and booking fails fast with a `SlotConflictException` if the lock has expired or belongs to someone else — no double-booked appointments under concurrent load.
- **JWT auth that also covers WebSocket.** Standard Spring Security handles REST, but WebSocket upgrades bypass the HTTP filter chain entirely — so STOMP `CONNECT` frames are intercepted separately, the bearer token is validated, and a `Principal` is attached to the session before any chat or call-signaling message is accepted.
- **AI triage with a hard safety floor.** Symptom analysis calls Google Gemini, but the model is never trusted blindly: any emergency-flagged keyword forces `urgencyScore = 100` and an Emergency Medicine referral *regardless of what the model returns*, every response gets a server-appended medical disclaimer, and a Gemini outage falls back to a safe generic response instead of failing the request.
- **Zero-effort audit logging.** An AspectJ `@Around` advice wraps every `@Service` method and writes actor, action and outcome to MongoDB automatically — individual services don't call an audit API, they just get audited.
- **Full CI/CD, not just a build script.** GitHub Actions runs backend tests against real Postgres/Mongo/Redis service containers, builds the admin dashboard, packages the Spring Boot JAR, builds and pushes a Docker image to GHCR, and deploys to AWS ECS on merge to `main`.

---

## Tech Stack

| Layer | Stack |
|---|---|
| **Android** | Kotlin, Jetpack Compose, MVVM, Coroutines + Flow, Hilt, Retrofit + OkHttp, Room (offline cache), DataStore, Firebase Auth/FCM/Analytics, Coil |
| **Backend** | Java 17, Spring Boot 3.2, Spring Security (JWT), Spring Data JPA + Flyway, Spring Data MongoDB, Spring WebSocket (STOMP/SockJS), Redisson, MapStruct, springdoc-openapi |
| **Admin Dashboard** | React 18, Vite, React Router |
| **Data** | PostgreSQL 16, MongoDB 7, Redis 7 |
| **External Services** | Firebase, Razorpay, AWS S3, Google Gemini |
| **Infra / CI-CD** | Docker, Docker Compose, GitHub Actions, GHCR, AWS ECS |

---

## Project Structure

```
MediWise/
├── android-app/          # Native patient app (Kotlin + Compose)
│   └── app/src/main/java/com/mediwise/
│       ├── core/          # DI, networking, datastore
│       ├── data/          # Repositories, Room DAOs, Retrofit DTOs
│       ├── domain/        # Use cases, domain models
│       └── presentation/  # Screens + ViewModels (MVVM)
├── backend/               # Spring Boot API
│   └── src/main/java/com/mediwise/
│       ├── auth/ · admin/ · appointment/ · chat/
│       ├── doctor/ · payment/ · profile/ · schedule/
│       ├── ai/ · analytics/ · notification/ · common/
├── mediwise-admin/        # React ops dashboard
│   └── src/{pages,services,components,context}/
└── .github/workflows/     # CI: test → build → dockerize → deploy
```

---

## Getting Started

### Prerequisites
- JDK 17, Maven (or use the bundled `mvnw`)
- Docker & Docker Compose
- Node.js 20+ (for the admin dashboard)
- Android Studio (Koala or newer) for the Android client
- API keys: Firebase service account, Razorpay test keys, AWS S3 credentials, Gemini API key

### 1. Clone
```bash
git clone https://github.com/abhisheksharma-swe/MediWise-Clinical-Decision-and-Real-Time-Consultation-Platform.git
cd MediWise-Clinical-Decision-and-Real-Time-Consultation-Platform
```

### 2. Backend
```bash
cd backend
cp .env.example .env   # fill in Firebase / Razorpay / AWS / Gemini credentials
docker compose up --build
```
This starts the Spring Boot app alongside PostgreSQL, MongoDB and Redis, each with health checks so the app waits for its dependencies. The API is available at:
```
http://localhost:8080
```

### 3. Admin Dashboard
```bash
cd mediwise-admin
npm install
npm run dev
```

### 4. Android App
Open `android-app/` in Android Studio, point `RetrofitClient`/`NetworkModule` at your backend URL, drop in your Firebase `google-services.json`, and run on an emulator or device.

---

## API Documentation

With the backend running, interactive Swagger UI is available at:
```
http://localhost:8080/swagger-ui.html
```
A Postman collection (`backend/mediwise_postman_collection.json`) is also included for quick manual testing.

---

## Testing & CI/CD

- Backend unit and integration tests run under `mvn test` against real Postgres/Mongo/Redis service containers (not mocks-only), covering auth, admin, and payment verification flows.
- Every push/PR to `main`/`develop` triggers the pipeline in `.github/workflows/ci.yml`: backend tests → admin build → Docker image → push to GHCR → deploy to AWS ECS (production deploy gated to `main`).

---

## Roadmap

- [ ] Doctor-facing UI for cross-consultation patient history (backend endpoint is live: `GET /api/v1/appointments/patient/{patientId}/history`)
- [ ] WebRTC client integration in the Android app (signaling layer is implemented server-side; peer connection wiring on-device is in progress)
- [ ] Structured prescriptions (medication + dosage + frequency) instead of free text

---

## License

MIT — see [LICENSE](LICENSE).

## Author

Built by [Abhishek Sharma](https://github.com/abhisheksharma-swe) as an industry-oriented project focused on production Android architecture, a modular Spring Boot backend, real-time communication, and safe AI-assisted triage.
