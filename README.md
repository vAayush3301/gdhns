# GDHNS – The “Safe & Secure” API You Probably Can’t Use

This is a Spring Boot REST API, exclusively configured for **my website** [`ggps-club.onrender.com`](https://ggps-club.onrender.com). If you’re hoping to clone it and magically have it work, **good luck**. You’ll need to rewrite half of it because it’s tailored, personal, and **intentionally undocumented**.  

---

## 🚀 Features

- CRUD operations for the website. Safely. Securely. Exclusively.  
- Built with **Spring Boot** and **Java 17** because old Java is for amateurs.  
- Uses **Firebase Admin SDK** for all your authentication and backend magic.  
- Docker-ready with a **Dockerfile**, because why not containerize perfection?  
- Hosted on **Render**, dedicated server, nobody else allowed.  

---

## 🛠️ Tech Stack

- Java 17+  
- Spring Boot  
- Gradle  
- Firebase Admin SDK  
- Docker  
- Dedicated Render server  

---

## 📦 Build & Run Instructions

### Clone the repo

```bash
git clone https://github.com/vAayush3301/gdhns.git
cd gdhns
```
Build the project with Gradle
```bash
./gradlew build
```

Run the server locally
```bash
./gradlew bootRun
```

Build and run using Docker (optional)
```bash
docker build -t gdhns .
docker run -p 8080:8080 gdhns
```
> **Pro Tip:** No comments. No JavaDoc. Read the damn code and figure it out yourself. You'll thank me later (or maybe not).

---

## 🌐 Deployment

Dedicated Render server, deployed specifically for ggps-club.onrender.com. Not for public use. Anyone else touching it will have to redefine the entire logic, so… don’t.

## 📄 License

#### MIT
Use it, abuse it, IDC.
