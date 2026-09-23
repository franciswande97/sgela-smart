# Sgela-Smart

**Smart Student Task and Goal Management Platform** — an execution layer for academic work, not another standalone planner.

Students already have digital planners and AI chat tools. What's missing is something that connects the two: automated calendar ingestion instead of manual setup, and AI assistance that actually knows the student's real tasks, goals, and free time — rather than starting from zero context every session. That's what this project builds.

Developed for the IT Project module (NQF7, 3rd year) — the institution retains ownership of this project and its contents.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Android native, Kotlin |
| Backend | Java, Spring Boot |
| Database | H2 (development) → MySQL (production-style testing) |
| AI — primary | Groq (`llama-3.3-70b-versatile`) |
| AI — fallback | Google Gemini (AI Studio free tier) |
| On-device OCR | Google ML Kit Text Recognition |

## MVP Features

1. **Task/Goal Engine** — create and track tasks tied to longer-term goals, with automatic timeline breakdown when a deadline is set.
2. **AI Co-Pilot & External Router** — breaks a goal down via Groq/Gemini, maps it to free calendar time, and routes complex work to the best external AI tool with a ready-to-paste prompt.
3. **Calendar** — manual event entry, plus photo-to-timetable import for one supported template layout.
4. **Deep Work Mode** — a focus timer that logs session duration and completed intervals against the active goal.

See `/docs` for the full MVP definition, including what's deliberately out of scope for this phase.

## Repo Structure

```
/backend      → Spring Boot project — entities, repositories, services, controllers
/android      → Android Studio project (Kotlin)
/docs         → Planning, Analysis, Design & Implementation phase documentation, diagrams
README.md     → this file
```

## Getting Started

**Backend:** see `/backend/README.md` for setup — H2 console verification, required `pom.xml` dependencies, and the full list of working API endpoints.

**Android:** open `/android` in Android Studio. Base API URL and endpoint contract are documented in `/backend/README.md` until a dedicated Android setup doc is added.

**Docs:** current phase drafts live in `/docs` — read `Planning_Phase_Draft.md` and `Analysis_Phase_Draft.md` before touching a section that's already written, to avoid conflicting edits.

## Team & Roles

Each area has one Lead (accountable owner) and one Support.

| Area | Lead | Support |
|---|---|---|
| Android / Frontend | _TBD_ | Backend Lead |
| Backend / Core API | _TBD_ | AI Integration Lead |
| AI Integration | _TBD_ | Calendar Lead |
| Calendar & Timetable Parser | _TBD_ | Frontend Lead |
| Documentation | _TBD_ | Testing/QA Lead |
| Testing/QA + Deep Work | _TBD_ | Documentation Lead |

## Workflow

- `main` stays always-working — no direct pushes.
- One feature branch per task (`feature/core-api`, `feature/android-ui`, `feature/ai-copilot`, etc.), merged into `main` via pull request.
- A quick self-review before merging catches more than it seems like it would — use it even solo.

## Status

Private repository — coursework submission, not for public distribution.
