# E-Learning Platform (Backend)

Backend REST API cho nền tảng học trực tuyến, xây dựng với Spring Boot.  
Dự án tập trung vào các nghiệp vụ: xác thực người dùng, quản lý khóa học/bài học, giỏ hàng, đơn hàng, thanh toán VNPay và trợ lý chat AI (RAG).

## Tech Stack
- Java 17
- Spring Boot 3.3.5
- Spring Web, Spring Security (JWT Resource Server)
- Spring Data JPA + Hibernate
- MySQL (dữ liệu nghiệp vụ chính)
- PostgreSQL + pgvector (lưu vector/AI chat history)
- Redis (cache/session hỗ trợ)
- Spring AI (OpenAI-compatible API)
- Swagger/OpenAPI (`springdoc-openapi`)
- Maven

## Tính năng chính
- Authentication: login, refresh token, logout, quên mật khẩu/đặt lại mật khẩu, Google OAuth callback
- User & Role management
- Category, Course, Lesson CRUD
- Cart, Order, Enrollment workflow
- Payment integration với VNPay (sandbox)
- AI Chat endpoint + reindex dữ liệu cho RAG

## Cấu trúc nhanh
- `controller/`: REST API endpoints
- `service/`: business logic
- `repository/`: JPA repositories (MySQL)
- `repository/pg/`: repositories cho PostgreSQL
- `configuration/`: security, datasource, OpenAPI, Redis, pgvector

## Yêu cầu môi trường
- JDK 17+
- Maven 3.9+
- Docker (khuyến nghị để chạy Redis + PostgreSQL)
- MySQL đang chạy local (mặc định `localhost:3306/elearning_db`)

## Cấu hình cần chuẩn bị
Biến môi trường thường dùng:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PGVECTOR_DATASOURCE_URL`
- `PGVECTOR_DATASOURCE_USERNAME`
- `PGVECTOR_DATASOURCE_PASSWORD`
- `OPENAI_API_KEY` (embedding)
- `GEMINI_KEY` (chat model theo OpenAI-compatible endpoint)
- `CLIENT_ID`, `CLIENT_SECRET` (Google OAuth)
- `YOUR_TMN_CODE`, `YOUR_HASH_SECRET` (VNPay)

## Cách chạy dự án
1. Khởi động services phụ trợ:
```bash
docker compose up -d
```

2. Đảm bảo MySQL đã tạo DB `elearning_db` và cập nhật biến môi trường.

3. Chạy ứng dụng:
```bash
./mvnw spring-boot:run
```
Trên Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

4. API base URL:
- `http://localhost:8080/api/v1`

## API docs
- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v1/v3/api-docs`

## Nhóm endpoint tiêu biểu
- `/auth/**`
- `/users/**`
- `/categories/**`
- `/courses/**`
- `/lessons/**`
- `/cart/**`
- `/orders/**`
- `/enrollments/**`
- `/payments/**`
- `/chat/**`


