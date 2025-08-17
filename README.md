# VidsoNet - Microservices Architecture

## 📌 Introduction
VidsoNet is a video-sharing and social networking platform built with **microservices architecture**.  
The project simulates key features such as video management, posts, comments, user chat, memberships, and payments.  

---

## 🏗️ Main Services
- **Auth Service**: Authentication, login, registration, JWT/OAuth2.  
- **Profile Service**: Manage user profile and channel information.  
- **Video Service**: Manage videos, categories, playlists, and watch progress.  
- **Post Service**: Manage community posts and reactions.  
- **Comment Service**: Manage comments for both videos and posts.  
- **Membership Service**: Manage subscription plans.  
- **Payment Service**: Handle payments and transactions.  
- **Notification Service**: Deliver and store user notifications.  
- **Chat Service**: Real-time messaging between users.  
- **Search Service**: Full-text search for videos, users, playlists, and posts.  

---

## 🗄️ Databases Used
- **PostgreSQL/MySQL**: Relational data (users, video metadata, posts, memberships, payments).  
- **MongoDB**: Flexible data (comments, chat, notifications).  
- **Redis**: Session storage, caching, pub/sub for chat and notifications.  
- **Elasticsearch**: Full-text search for videos, users, posts.  
- **Object Storage (S3/MinIO)**: Store video files and images.  

---

## 🔗 System Architecture
- **API Gateway**: Routes requests to services, handles JWT authentication.  
- **Service Discovery**: Service registration and discovery (Eureka/Consul).  
- **Message Broker**: Kafka/RabbitMQ for event-driven communication.  
- **CI/CD**: Docker & Docker Compose for development and deployment.  

---

## 🚀 How to Run (Development)
1. Clone the repository
   ```bash
   git clone https://github.com/hoaithi/video-social-networking-microservice
