 # VidsoNet - Microservices Architecture



 ## 📌 Giới thiệu

VidsoNet là một hệ thống nền tảng chia sẻ video và mạng xã hội được triển khai theo kiến trúc   **microservices  **.  

Dự án mô phỏng các tính năng chính như: quản lý video, bài viết, bình luận, chat giữa user, membership và thanh toán.  



---



 ## 🏗️ Các Service chính

 -  **Auth Service **: Xác thực, đăng nhập, đăng ký, JWT/OAuth2.  

 -  **Profile Service **: Quản lý thông tin cá nhân và kênh của người dùng.  

 -  **Video Service **: Quản lý video, danh mục, playlist, tiến trình xem.  

 -  **Post Service **: Quản lý bài viết cộng đồng và reaction.  

 -  **Comment Service **: Quản lý comment cho video và bài viết.  

 -   **Membership Service  **: Quản lý gói hội viên.  

 -   **Payment Service  **: Xử lý thanh toán và giao dịch.  

 -   **Notification Service  **: Gửi và lưu thông báo.  

 -   **Chat Service  **: Hỗ trợ nhắn tin real-time giữa người dùng.  

 -   **Search Service  **: Tìm kiếm video, user, playlist, post.  



---



 ## 🗄️ Database được sử dụng

 -   **PostgreSQL/MySQL  **: Lưu dữ liệu quan hệ (user, video metadata, posts, membership, payment).  

 -   **MongoDB  **: Lưu dữ liệu linh hoạt (comment, chat, notification).  

 -   **Redis  **: Lưu session, cache, message pub/sub cho chat và notification.  

 -   **Elasticsearch  **: Tìm kiếm full-text cho video, user, post.  

 -   **Object Storage (S3/MinIO)  **: Lưu file video và ảnh.  



---



 ## 🔗 Kiến trúc hệ thống

 -   **API Gateway  **: Routing request đến các service, xác thực JWT.  

 -   **Service Discovery  **: Đăng ký và phát hiện service (Eureka/Consul).  

 -   **Message Broker  **: Kafka/RabbitMQ để truyền sự kiện giữa các service.  

 -   **CI/CD  **: Docker  & Docker Compose để phát triển và triển khai.  



---



 ## 🚀 Cách chạy dự án (phát triển)

1 . Clone repo

```bash

git clone https://github.com/hoaithi/video-social-networking-microservices

