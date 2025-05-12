# RetroArcade (Multiplayer Gaming System) – Server

This repository contains the **server-side** implementation of a fully custom-built multiplayer gaming platform written in Java. The server handles client connections, matchmaking, game session management, messaging, and integrates with a PostgreSQL database for persistent storage.

---

## 🧠 Responsibilities

- Accepts and manages client socket connections
- Routes messages between threads using BlockingQueues
- Matches players based on rating and game type
- Spawns and manages individual game sessions
- Interfaces with the database to store and retrieve user data

---

## 🛠️ Tech Stack

- **Language:** Java 23  
- **Concurrency:** Virtual threads & BlockingQueues  
- **Communication:** TCP Sockets with JSON-formatted thread messages  
- **Database:** PostgreSQL (via JDBC)

---

## 📁 Project Structure

~~~
server/
├── common/            # Shared classes like ThreadMessage and utilities
├── database/          # DatabaseConnector and persistence logic
├── management/        # ConnectionManager, ServerController, ThreadRegistry
├── player/            # PlayerHandler and client-specific thread logic
├── session/           # GameCreator, GameSessionManager, matchmaking system
~~~

---

## 🧪 Testing

- Manual testing of login, matchmaking, and gameplay flows
- Simulated multiple clients to validate thread communication
- Future improvement: add unit tests for session management and matchmaking

---

## 📄 Documentation

- Message types and schemas: see `docs/ThreadMessageTypes.md`
- Module design and planning: see `planning/` folder
- Each class includes Javadoc comments for clarity

---

## 👥 Contributors

- **Evan Poulson** – In-Game Player Management & Matchmaking  
- **Colby** – Out-of-Game Player Management
- **Cristian** – Profile, Leaderboard, & Database Management

---

## 📌 Notes

This server is part of a larger client-server gaming system and was developed **entirely from scratch** without the use of frameworks. It demonstrates an advanced understanding of multi-threaded systems, socket communication, and modular architecture.
