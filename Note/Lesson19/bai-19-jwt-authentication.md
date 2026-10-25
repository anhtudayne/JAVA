# 📘 Bài 19: JWT Authentication — Tự Viết Để Hiểu Cơ Chế

> **Giai đoạn:** 🔴 GĐ 6 — Security & Authentication (Bài 19/28)
> **Thời lượng:** ~4-5 giờ lý thuyết + thực hành
> **Yêu cầu tiên quyết:** Bài 18 (Spring Security Fundamentals — SecurityFilterChain, UserDetailsService, BCrypt, Method Security)

---

## 🔑 Định Nghĩa & Tóm Tắt Cốt Lõi

| Khái niệm | Định nghĩa ngắn gọn |
|:---|:---|
| **JWT (JSON Web Token)** | Chuỗi ký tự mã hóa Base64 gồm 3 phần (Header.Payload.Signature), mang thông tin xác thực một cách **tự chứa** (self-contained) — server không cần lưu session. |
| **Access Token** | JWT ngắn hạn (15-60 phút) gửi kèm mỗi request qua header `Authorization: Bearer <token>`. Server xác thực bằng cách **verify chữ ký** mà không cần tra cứu database. |
| **Refresh Token** | Token dài hạn (7-30 ngày) dùng để **xin cấp Access Token mới** khi token cũ hết hạn, tránh việc user phải đăng nhập lại. |
| **Stateless Authentication** | Cơ chế xác thực mà server **không lưu trạng thái phiên** (session) — mọi thông tin cần thiết đều nằm trong JWT. |
| **Bearer Token** | Mẫu gửi token trong HTTP header: `Authorization: Bearer eyJhbGci...`. Bất kỳ ai "mang" (bear) token hợp lệ đều được xác thực. |
| **HMAC-SHA256** | Thuật toán ký số đối xứng (symmetric): cùng 1 secret key để **ký** và **xác minh** JWT. Phù hợp cho hệ thống monolithic. |

> [!IMPORTANT]
> **Ẩn dụ thực tế:** JWT hoạt động như **vé máy bay điện tử (e-ticket)**.
> - **Đăng nhập** = Check-in tại quầy → nhận vé (JWT) in mã QR chứa tên, số ghế, hạng vé
> - **Gọi API** = Mỗi cửa gate quét mã QR trên vé → xác nhận **hợp lệ** mà không cần gọi lại quầy check-in
> - **Access Token hết hạn** = Vé boarding quá giờ → cần đổi vé mới (dùng Refresh Token)
> - **Secret Key** = Con dấu của hãng hàng không in trên vé — chống giả mạo
> - **Stateless** = Nhân viên cổng gate KHÔNG tra cứu danh sách hành khách — chỉ quét mã trên vé

---

## 📑 Mục Lục

1. [Tại sao cần JWT? Vấn đề của Session-Based Auth](#1-tại-sao-cần-jwt)
2. [Session-Based vs Token-Based Authentication](#2-session-based-vs-token-based-authentication)
3. [Cấu trúc JWT — Mổ xẻ từng byte](#3-cấu-trúc-jwt--mổ-xẻ-từng-byte)
4. [Luồng JWT Authentication end-to-end](#4-luồng-jwt-authentication-end-to-end)
5. [Thư viện jjwt — Tạo, Parse, Validate Token](#5-thư-viện-jjwt--tạo-parse-validate-token)
6. [JwtService — Trái tim của hệ thống JWT](#6-jwtservice--trái-tim-của-hệ-thống-jwt)
7. [JwtAuthenticationFilter — Bộ lọc tự viết](#7-jwtauthenticationfilter--bộ-lọc-tự-viết)
8. [Cập nhật SecurityFilterChain cho JWT](#8-cập-nhật-securityfilterchain-cho-jwt)
9. [Cập nhật AuthController — Login/Register trả JWT](#9-cập-nhật-authcontroller--loginregister-trả-jwt)
10. [Refresh Token — Cơ chế xoay token](#10-refresh-token--cơ-chế-xoay-token)
11. [Bảo mật JWT — Secret Key, Revocation, Best Practices](#11-bảo-mật-jwt--secret-key-revocation-best-practices)
12. [HMAC vs RSA/EC — Symmetric vs Asymmetric Signing](#12-hmac-vs-rsaec--symmetric-vs-asymmetric-signing)
13. [Liên hệ Bài 18 — Từ HTTP Basic đến JWT Bearer](#13-liên-hệ-bài-18--từ-http-basic-đến-jwt-bearer)
14. [Triển khai thực hành — Danh sách file](#14-triển-khai-thực-hành--danh-sách-file)
15. [Bảng tổng hợp Components & Annotations](#15-bảng-tổng-hợp-components--annotations)
16. [Design Patterns trong JWT Authentication](#16-design-patterns-trong-jwt-authentication)
17. [Câu hỏi phỏng vấn (Interview Questions)](#17-câu-hỏi-phỏng-vấn)

---

## 🎯 Mục Tiêu Bài Học

- [ ] Hiểu JWT là gì, cấu trúc 3 phần (Header.Payload.Signature), và tại sao nó "self-contained"
- [ ] Phân biệt rõ ràng Session-Based vs Token-Based Authentication
- [ ] Triển khai `JwtService` sử dụng thư viện `jjwt`: tạo, parse, validate JWT
- [ ] Viết `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) — tự tay "chặn" request, trích JWT, nạp SecurityContext
- [ ] Cập nhật `SecurityFilterChain`: chuyển từ HTTP Basic sang stateless JWT Bearer
- [ ] Cập nhật `AuthController`: Login/Register trả về JWT token pair (accessToken + refreshToken)
- [ ] Implement Refresh Token API: xoay token khi Access Token hết hạn
- [ ] Hiểu các chiến lược bảo mật JWT: Secret Key management, Token Revocation, Token Blacklist
- [ ] So sánh HMAC (symmetric) vs RSA (asymmetric) signing — khi nào dùng cái nào

---

## 1. Tại Sao Cần JWT?

### 1.1. Vấn đề của HTTP Basic Authentication (Bài 18)

```
⚠️ VẤN ĐỀ HIỆN TẠI (Bài 18):

Client → GET /api/users
         Header: Authorization: Basic YWRtaW5AZ21haWwuY29tOmFkbWluMTIz
         (= base64("admin@gmail.com:admin123"))

         → Mỗi request gửi username:password đã mã hóa Base64
         → Base64 KHÔNG phải mã hóa — dễ dàng decode ngược
         → Nếu bị đánh cắp header → toàn bộ credentials bị lộ
         → Server phải tra cứu database MỖI request để xác thực
```

> [!CAUTION]
> **HTTP Basic Authentication** gửi credentials trên **mọi request** — cực kỳ nguy hiểm trên mạng không mã hóa. Ngay cả với HTTPS, việc gửi password liên tục tăng bề mặt tấn công (attack surface). Ở Bài 18 ta chỉ dùng để **học cơ bản**, không dùng cho production.

### 1.2. JWT giải quyết vấn đề gì?

```
✅ GIẢI PHÁP JWT:

Bước 1: Client đăng nhập 1 LẦN DUY NHẤT
         POST /api/auth/login {email, password}
         → Server xác thực, trả về JWT token

Bước 2: Mọi request sau đó chỉ gửi TOKEN (không gửi password nữa!)
         GET /api/users
         Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ...
         → Server chỉ verify chữ ký token — KHÔNG cần query database
         → Nhanh, stateless, scalable
```

| Tiêu chí | HTTP Basic (Bài 18) | JWT (Bài 19) |
|:---|:---|:---|
| 🔑 **Gửi credentials** | Mỗi request (username:password) | Chỉ 1 lần khi login |
| 🗄️ **Server lưu session?** | Không (nhưng query DB mỗi lần) | Không (stateless hoàn toàn) |
| 📊 **Database query/request** | Mỗi request (tra cứu user) | Không cần (verify chữ ký token) |
| 📈 **Scalability** | Kém (mỗi server đều phải query DB) | Tốt (bất kỳ server nào cũng verify được) |
| 📱 **Phù hợp Mobile/SPA** | Không (phải lưu password trên client) | Có (chỉ lưu token) |
| ⏰ **Tự hết hạn** | Không | Có (exp claim trong token) |

---

## 2. Session-Based vs Token-Based Authentication

### 2.1. Session-Based (Stateful)

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant SessionStore as "Session Store<br/>(Memory/Redis)"
    participant DB

    Client->>Server: POST /login {email, password}
    Server->>DB: Kiểm tra credentials
    DB-->>Server: User hợp lệ ✅
    Server->>SessionStore: Lưu session: {sessionId → userId, roles}
    Server-->>Client: Set-Cookie: JSESSIONID=abc123

    Note over Client,DB: Các request tiếp theo
    Client->>Server: GET /api/users<br/>Cookie: JSESSIONID=abc123
    Server->>SessionStore: Tra cứu session "abc123"
    SessionStore-->>Server: {userId: 1, roles: [ADMIN]}
    Server-->>Client: Response data
```

### 2.2. Token-Based (Stateless) — JWT

```mermaid
sequenceDiagram
    participant Client
    participant JWTFilter as "JWT Filter"
    participant Server
    participant DB

    Client->>Server: POST /login {email, password}
    Server->>DB: Kiểm tra credentials
    DB-->>Server: User hợp lệ ✅
    Server-->>Client: {accessToken: "eyJ...", refreshToken: "eyJ..."}

    Note over Client,DB: Các request tiếp theo
    Client->>JWTFilter: GET /api/users<br/>Authorization: Bearer eyJ...
    JWTFilter->>JWTFilter: Verify chữ ký + kiểm tra expiration
    Note right of JWTFilter: ❌ KHÔNG cần query DB<br/>❌ KHÔNG cần Session Store
    JWTFilter->>Server: Request + Authentication object
    Server-->>Client: Response data
```

### 2.3. Bảng so sánh toàn diện

| Tiêu chí | Session-Based (Stateful) | JWT (Stateless) |
|:---|:---|:---|
| 📍 **Nơi lưu trạng thái** | Server (memory/Redis) | Client (localStorage/cookie) |
| 🔄 **Scale ngang (Load Balancer)** | ⚠️ Cần sticky session hoặc shared session store | ✅ Bất kỳ server nào cũng verify được |
| 🗑️ **Thu hồi quyền truy cập** | ✅ Dễ — xóa session | ⚠️ Khó — token vẫn hợp lệ đến khi hết hạn |
| 📊 **Hiệu năng** | ⚠️ Mỗi request tra cứu session store | ✅ Chỉ verify chữ ký (CPU operation) |
| 💾 **Bộ nhớ server** | ⚠️ Tỷ lệ thuận với số session active | ✅ Không tốn bộ nhớ |
| 📱 **Mobile/SPA friendly** | ⚠️ Cookie-based, CORS phức tạp | ✅ Gửi qua header, đơn giản |
| 🔒 **CSRF risk** | ⚠️ Có (cookie tự gửi) | ✅ Không (header phải chủ động gửi) |
| 🎯 **Microservices** | ⚠️ Cần centralized session | ✅ Tự verify, không phụ thuộc service khác |

> [!TIP]
> **Khi nào dùng Session? Khi nào dùng JWT?**
> - **Session:** Server-side rendered (Thymeleaf, JSP), ít server, cần thu hồi session tức thì
> - **JWT:** REST API, SPA (React/Vue/Angular), Mobile app, Microservices, cần scale ngang

---

## 3. Cấu Trúc JWT — Mổ Xẻ Từng Byte

### 3.1. Tổng quan 3 phần

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJpYXQiOjE3Mjk4NDk2MDAsImV4cCI6MTcyOTg1MzIwMCwicm9sZXMiOlsiUk9MRV9BRE1JTiIsIlJPTEVfVVNFUiJdfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk
|___________HEADER____________|._________________________PAYLOAD_________________________|.__________SIGNATURE___________|
```

### 3.2. Khái niệm & Phân tích chi tiết 3 thành phần

Một chuỗi JWT chuẩn (theo RFC 7519) được cấu thành từ 3 phần tách biệt, kết nối với nhau bởi dấu chấm (`.`):

$$\text{JWT} = \text{Base64Url}(\text{Header}) \;\mathbf{.}\; \text{Base64Url}(\text{Payload}) \;\mathbf{.}\; \text{Signature}$$

```mermaid
graph TD
    subgraph Header["🏷️ 1. HEADER (Metadata kỹ thuật)"]
        H1["Base64Url Decode →"]
        H2["{<br/>  'alg': 'HS256',<br/>  'typ': 'JWT'<br/>}"]
        H1 --> H2
    end

    subgraph Payload["📦 2. PAYLOAD (Dữ liệu / Claims)"]
        P1["Base64Url Decode →"]
        P2["{<br/>  'sub': 'admin@gmail.com',<br/>  'iat': 1729849600,<br/>  'exp': 1729853200,<br/>  'roles': ['ROLE_ADMIN', 'ROLE_USER'],<br/>  'userId': 4<br/>}"]
        P1 --> P2
    end

    subgraph Signature["🔏 3. SIGNATURE (Chữ ký chống giả mạo)"]
        S1["HMAC-SHA256(<br/>  base64(header) + '.' + base64(payload),<br/>  SECRET_KEY<br/>)"]
    end

    Header ---|"."| Payload ---|"."| Signature
```

#### 🏷️ 1. Header (Tiêu đề Metadata)
* **Khái niệm:** Là phần chứa **thông tin mô tả (metadata)** về cấu trúc của chính token. Nó thông báo cho phía nhận (Server/Client) biết định dạng của token và thuật toán mật mã nào đã được sử dụng để tạo ra chữ ký.
* **Cơ chế lưu trữ:** Là một đối tượng JSON thuần túy, sau đó được encode sang định dạng chuỗi bằng chuẩn **Base64Url** (để truyền an toàn qua URL và HTTP Header mà không bị xung đột ký tự).
* **Các trường tiêu chuẩn cốt lõi:**
  * `typ` *(Type)*: Loại token, giá trị tiêu chuẩn luôn là `"JWT"`.
  * `alg` *(Algorithm)*: Thuật toán ký số/băm được áp dụng (ví dụ: thuật toán đối xứng `HS256`, `HS384`, `HS512` dùng chung một Secret Key; hoặc thuật toán bất đối xứng `RS256`, `ES256` dùng cặp Private/Public Key).

---

#### 📦 2. Payload (Dữ liệu / Danh sách Claims)
* **Khái niệm:** Là **phần chứa nội dung nghiệp vụ chính** của token. Payload lưu trữ các **Claims** — tức là những thông tin khẳng định về danh tính người dùng (Subject), quyền hạn (Roles/Authorities) và các dữ liệu phụ trợ liên quan.
* **Cơ chế lưu trữ:** Là một đối tượng JSON gồm các cặp key-value, được encode sang chuỗi bằng **Base64Url**.
* **Đặc tính kỹ thuật then chốt:**
  * **Chỉ encode Base64Url chứ KHÔNG mã hóa bí mật (Not Encrypted):** Bất kỳ ai có được chuỗi JWT đều có thể giải mã (decode) Base64Url để đọc rõ toàn bộ nội dung JSON bên trong Payload trong vòng 0 giây (ví dụ qua trang `jwt.io`).
  * **Tự chứa thông tin (Self-contained):** Nhờ việc gói sẵn thông tin người dùng và quyền hạn bên trong Payload, Server khi nhận request chỉ cần đọc Payload là có thể phân quyền ngay lập tức mà không cần tốn thời gian truy vấn vào Database.
  * ⚠️ **Nguyên tắc an toàn tối thượng:** Tuyệt đối **không** lưu trữ mật khẩu, mã PIN, số thẻ tín dụng hoặc các dữ liệu cá nhân nhạy cảm trong Payload.

---

#### 🔏 3. Signature (Chữ ký số bảo đảm toàn vẹn)
* **Khái niệm:** Là chuỗi mã băm mật mã dùng để **xác thực nguồn gốc** và **bảo vệ tính toàn vẹn (Integrity)** tuyệt đối của token, bảo đảm rằng nội dung ở phần Header và Payload chưa từng bị bất kỳ ai chỉnh sửa hay giả mạo trên đường truyền.
* **Cơ chế tính toán:** Được sinh ra bằng cách ghép chuỗi `Base64Url(Header)` và `Base64Url(Payload)` lại với nhau (ngăn cách bằng dấu `.`), sau đó kết hợp với một khóa bí mật (**Secret Key**) chỉ duy nhất Server biết, rồi đưa qua thuật toán băm đã chỉ định ở Header:
  $$\text{Signature} = \text{HMACSHA256}\Big(\text{Base64Url(Header)} + \text{"."} + \text{Base64Url(Payload)},\; \text{SECRET\_KEY}\Big)$$
* **Nguyên lý chống giả mạo:**
  * Chỉ Server sở hữu `SECRET_KEY` mới có khả năng tạo ra một `Signature` hợp lệ.
  * Nếu Client hoặc Hacker can thiệp sửa đổi bất kỳ byte nào trong Payload (ví dụ: tự ý đổi `"roles": ["ROLE_USER"]` thành `["ROLE_ADMIN"]`), thì khi request gửi tới Server, Server dùng `SECRET_KEY` tính lại Signature sẽ ra một kết quả hoàn toàn khác với chữ ký đính kèm. Ngay lập tức Token bị coi là bất hợp pháp và request bị từ chối (HTTP 401 Unauthorized).

---

#### 📋 Bảng tổng hợp bản chất 3 thành phần

| Thành phần | Định dạng gốc | Cách biểu diễn trong JWT | Có bị lộ nội dung không? | Ai có thể tạo / chỉnh sửa? |
| :--- | :--- | :--- | :--- | :--- |
| **Header** | JSON Metadata | Chuỗi mã hóa Base64Url | **CÓ** (Ai cũng decode đọc được) | Server tạo lúc đăng nhập |
| **Payload** | JSON Claims | Chuỗi mã hóa Base64Url | **CÓ** (Ai cũng decode đọc được) | Server tạo lúc đăng nhập |
| **Signature** | Chuỗi băm mật mã | Chuỗi Base64Url nhị phân | **KHÔNG** (Không thể dịch ngược ra Secret Key) | **Chỉ Server** (vì chỉ Server giữ Secret Key) |

---

### 3.3. Các loại Claims trong Payload

| Loại | Tên Claim | Ý nghĩa | Ví dụ |
|:---|:---|:---|:---|
| **Registered** | `sub` (Subject) | Định danh chủ sở hữu token | `"admin@gmail.com"` |
| **Registered** | `iat` (Issued At) | Thời điểm phát hành | `1729849600` (Unix timestamp) |
| **Registered** | `exp` (Expiration) | Thời điểm hết hạn | `1729853200` |
| **Registered** | `iss` (Issuer) | Bên phát hành | `"springboot-learning"` |
| **Custom** | `roles` | Danh sách vai trò | `["ROLE_ADMIN", "ROLE_USER"]` |
| **Custom** | `userId` | ID người dùng | `4` |
| **Custom** | `fullName` | Tên đầy đủ | `"System Administrator"` |

> [!WARNING]
> **KHÔNG được đặt thông tin nhạy cảm vào Payload!** Payload chỉ được **Base64 encode**, KHÔNG được mã hóa — bất kỳ ai cũng có thể decode và đọc nội dung. Không bao giờ đặt password, credit card, hoặc dữ liệu cá nhân nhạy cảm vào JWT payload.

### 3.4. Signature — Tại sao JWT không thể giả mạo?

```java
// Quá trình tạo Signature:
String data = base64Url(header) + "." + base64Url(payload);
String signature = HMACSHA256(data, SECRET_KEY);
// → Chỉ server biết SECRET_KEY mới tạo được signature hợp lệ

// Quá trình verify:
// 1. Server tách header.payload từ token nhận được
// 2. Tính lại signature bằng SECRET_KEY
// 3. So sánh signature tính được với signature trong token
// 4. Nếu khớp → token chưa bị sửa đổi → hợp lệ ✅
// 5. Nếu không khớp → token đã bị giả mạo → reject ❌
```

> **Nếu attacker sửa payload** (ví dụ đổi `roles` từ USER thành ADMIN), **signature sẽ không khớp** vì attacker không có SECRET_KEY để tính lại signature.

---

## 4. Luồng JWT Authentication End-to-End

### 4.1. Luồng tổng thể

```mermaid
sequenceDiagram
    participant Client as "🌐 Client<br/>(React/Mobile)"
    participant Filter as "🛡️ JwtAuthenticationFilter"
    participant Controller as "🎯 AuthController"
    participant JwtSvc as "🔧 JwtService"
    participant AuthMgr as "🔐 AuthenticationManager"
    participant UserSvc as "👤 UserDetailsService"
    participant DB as "🗄️ Database"

    rect rgb(220, 240, 255)
        Note over Client,DB: 📝 BƯỚC 1: ĐĂNG KÝ (Register)
        Client->>Controller: POST /api/auth/register<br/>{name, email, password}
        Controller->>DB: Kiểm tra email trùng lặp
        Controller->>DB: Lưu user (password đã hash BCrypt)
        Controller->>JwtSvc: generateAccessToken(user) + generateRefreshToken(user)
        JwtSvc-->>Controller: {accessToken, refreshToken}
        Controller-->>Client: 201 Created<br/>{accessToken, refreshToken, user info}
    end

    rect rgb(255, 240, 220)
        Note over Client,DB: 🔐 BƯỚC 2: ĐĂNG NHẬP (Login)
        Client->>Controller: POST /api/auth/login<br/>{email, password}
        Controller->>AuthMgr: authenticate(email, password)
        AuthMgr->>UserSvc: loadUserByUsername(email)
        UserSvc->>DB: findByEmail(email)
        DB-->>UserSvc: User entity + Roles
        UserSvc-->>AuthMgr: CustomUserDetails
        AuthMgr-->>Controller: Authentication (verified ✅)
        Controller->>JwtSvc: generateAccessToken + generateRefreshToken
        JwtSvc-->>Controller: {accessToken, refreshToken}
        Controller-->>Client: 200 OK<br/>{accessToken, refreshToken, user info}
    end

    rect rgb(220, 255, 220)
        Note over Client,DB: 📡 BƯỚC 3: GỌI API VỚI JWT
        Client->>Filter: GET /api/users<br/>Authorization: Bearer eyJ...accessToken
        Filter->>Filter: ① Trích token từ header
        Filter->>JwtSvc: ② validateToken(token)
        JwtSvc-->>Filter: ③ Hợp lệ ✅ + email từ token
        Filter->>UserSvc: ④ loadUserByUsername(email)
        UserSvc->>DB: findByEmail(email)
        DB-->>UserSvc: User + Roles
        UserSvc-->>Filter: CustomUserDetails
        Filter->>Filter: ⑤ Nạp SecurityContext
        Filter->>Controller: ⑥ Request đã authenticated
        Controller-->>Client: 200 OK — Response data
    end

    rect rgb(255, 230, 230)
        Note over Client,DB: 🔄 BƯỚC 4: REFRESH TOKEN (khi access token hết hạn)
        Client->>Controller: POST /api/auth/refresh<br/>{refreshToken: "eyJ..."}
        Controller->>JwtSvc: validateToken(refreshToken)
        JwtSvc-->>Controller: Hợp lệ ✅ + email
        Controller->>JwtSvc: generateAccessToken(email) — Token mới
        JwtSvc-->>Controller: newAccessToken
        Controller-->>Client: 200 OK<br/>{accessToken: "eyJ...new"}
    end
```

---

## 5. Thư Viện jjwt — Tạo, Parse, Validate Token

### 5.1. Dependency Maven

```xml
<!-- 📘 BÀI 19: JWT Authentication với jjwt -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

> [!NOTE]
> **Tại sao 3 dependency?** `jjwt` tách thành 3 module:
> - `jjwt-api`: Interface & API công khai — compile-time
> - `jjwt-impl`: Implementation — runtime only (tránh coupling trực tiếp)
> - `jjwt-jackson`: Jackson serializer cho JSON — runtime only

### 5.2. Các API cốt lõi

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

// ===== 1. Tạo SecretKey từ chuỗi =====
byte[] keyBytes = Decoders.BASE64.decode("dGhpcyBpcyBhIHNlY3JldCBrZXkgZm9yIGp3dCBhdXRoZW50aWNhdGlvbg==");
SecretKey key = Keys.hmacShaKeyFor(keyBytes);

// ===== 2. Tạo JWT Token =====
String token = Jwts.builder()
        .subject("admin@gmail.com")                          // sub claim
        .issuedAt(new Date())                                 // iat claim
        .expiration(new Date(System.currentTimeMillis() + 3600000)) // exp (1 giờ)
        .claim("roles", List.of("ROLE_ADMIN", "ROLE_USER"))  // custom claim
        .claim("userId", 4L)                                  // custom claim
        .signWith(key)                                         // Ký bằng HMAC-SHA256
        .compact();                                            // Tạo chuỗi JWT

// ===== 3. Parse (giải mã) JWT Token =====
Claims claims = Jwts.parser()
        .verifyWith(key)                                       // Key để verify signature
        .build()
        .parseSignedClaims(token)                              // Parse + verify
        .getPayload();                                         // Lấy payload (Claims)

String email = claims.getSubject();                            // "admin@gmail.com"
Date expiration = claims.getExpiration();                       // Token hết hạn khi nào
List<String> roles = claims.get("roles", List.class);          // ["ROLE_ADMIN", "ROLE_USER"]
Long userId = claims.get("userId", Long.class);                // 4

// ===== 4. Khi token hết hạn hoặc bị sửa đổi =====
// → Ném ExpiredJwtException hoặc JwtException
```

---

## 6. JwtService — Trái Tim Của Hệ Thống JWT

```java
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;  // mili giây

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // mili giây

    // ===== 1. Tạo Access Token =====
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Nhúng roles vào token để tránh query DB mỗi request
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        if (userDetails instanceof CustomUserDetails customUser) {
            extraClaims.put("userId", customUser.getId());
            extraClaims.put("fullName", customUser.getFullName());
        }

        return buildToken(extraClaims, userDetails.getUsername(), accessTokenExpiration);
    }

    // ===== 2. Tạo Refresh Token =====
    public String generateRefreshToken(UserDetails userDetails) {
        // Refresh token chỉ chứa subject, KHÔNG chứa roles
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshTokenExpiration);
    }

    // ===== 3. Validate Token =====
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ===== 4. Trích xuất email từ token =====
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ===== 5. Trích xuất claim tùy ý =====
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ===== PRIVATE METHODS =====

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

> [!TIP]
> **Best practice:** Nhúng `roles` vào Access Token để **tránh query database mỗi request**. Tuy nhiên, Refresh Token **KHÔNG nên** chứa roles — khi refresh, server sẽ tải lại roles mới nhất từ DB, đảm bảo thay đổi quyền được cập nhật kịp thời.

---

## 7. JwtAuthenticationFilter — Bộ Lọc Tự Viết

### 7.1. Vị trí trong Security Filter Chain

```mermaid
graph LR
    Request["HTTP Request"] --> CorsFilter["CorsFilter"]
    CorsFilter --> CsrfFilter["CsrfFilter<br/>(disabled)"]
    CsrfFilter --> JwtFilter["🆕 JwtAuthenticationFilter<br/>(tự viết!)"]
    JwtFilter --> AuthFilter["UsernamePasswordAuth<br/>Filter"]
    AuthFilter --> AuthzFilter["AuthorizationFilter"]
    AuthzFilter --> DS["DispatcherServlet<br/>→ Controller"]

    style JwtFilter fill:#FF7043,color:#fff,stroke-width:3px
```

> **JwtAuthenticationFilter** được gắn **trước** `UsernamePasswordAuthenticationFilter` trong chuỗi filter. Nó chặn request, trích JWT từ header, verify, và nạp `Authentication` vào `SecurityContext` — để các filter phía sau (đặc biệt `AuthorizationFilter`) biết user đã được xác thực.

### 7.2. Mã nguồn JwtAuthenticationFilter

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 📘 BÀI 19 — JWT Authentication Filter
 *
 * Mỗi request HTTP đi qua filter này ĐÚNG 1 LẦN (OncePerRequestFilter).
 * Luồng xử lý:
 *   ① Trích chuỗi JWT từ header "Authorization: Bearer xxx"
 *   ② Giải mã token → lấy email (subject)
 *   ③ Nạp UserDetails từ database qua UserDetailsService
 *   ④ Kiểm tra token hợp lệ (chữ ký + hạn sử dụng)
 *   ⑤ Tạo Authentication object → đẩy vào SecurityContext
 *   ⑥ Chuyển tiếp request cho filter tiếp theo
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                    UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ① Trích JWT từ header
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Không có token → bỏ qua, chuyển cho filter tiếp theo
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7); // Cắt "Bearer "

        try {
            // ② Giải mã token → lấy email
            final String userEmail = jwtService.extractUsername(jwt);

            // ③ Chỉ xử lý khi chưa có Authentication trong SecurityContext
            if (userEmail != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // ④ Nạp UserDetails từ database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // ⑤ Kiểm tra token hợp lệ
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // ⑥ Tạo Authentication → nạp vào SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,  // credentials = null (đã xác thực bằng JWT)
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token hết hạn → không set Authentication, request sẽ bị 401
            logger.debug("JWT token hết hạn: " + e.getMessage());
        } catch (io.jsonwebtoken.JwtException e) {
            // Token không hợp lệ (sai signature, format sai...)
            logger.debug("JWT token không hợp lệ: " + e.getMessage());
        }

        // ⑦ Chuyển tiếp cho filter tiếp theo
        filterChain.doFilter(request, response);
    }
}
```

### 7.3. Tại sao extends OncePerRequestFilter?

| Filter Base Class | Đặc điểm | Dùng khi |
|:---|:---|:---|
| `GenericFilterBean` | Có thể chạy **nhiều lần** cho 1 request (forward, include) | Ít dùng cho security |
| `OncePerRequestFilter` | Đảm bảo chạy **đúng 1 lần** mỗi request | ✅ **Luôn dùng cho JWT filter** |

> Trong servlet pipeline, một request có thể bị **forward** (ví dụ: error page) hoặc **include** (ví dụ: JSP include). `OncePerRequestFilter` dùng attribute flag để ngăn chạy lại lần 2.

---

## 8. Cập Nhật SecurityFilterChain Cho JWT

```java
@Bean
public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtAuthFilter
) throws Exception {
    return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/products/**").permitAll()
                    .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MANAGER")
                    .anyRequest().authenticated()
            )

            // 🆕 BÀI 19: Chuyển sang Stateless Session
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 🆕 BÀI 19: Gắn JwtFilter TRƯỚC UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // ❌ BỎ httpBasic — không cần nữa khi có JWT
            // .httpBasic(Customizer.withDefaults())

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )
            .build();
}
```

### 8.1. Hai thay đổi quan trọng

| Thay đổi | Trước (Bài 18) | Sau (Bài 19) | Tại sao |
|:---|:---|:---|:---|
| **Session Policy** | Mặc định (STATEFUL) | `STATELESS` | JWT tự chứa, không cần session server-side |
| **Authentication Filter** | `httpBasic()` | `addFilterBefore(jwtFilter, ...)` | Chuyển từ Basic Auth sang Bearer Token |

> [!IMPORTANT]
> **`SessionCreationPolicy.STATELESS`** — Cực kỳ quan trọng! Nếu quên cấu hình này, Spring Security sẽ vẫn tạo `JSESSIONID` cookie và lưu session → phá vỡ toàn bộ cơ chế stateless của JWT.

---

## 9. Cập Nhật AuthController — Login/Register Trả JWT

### 9.1. Response DTO mới

```java
public class JwtAuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;        // Thời gian sống access token (giây)
    private Long id;
    private String name;
    private String email;
    private Set<String> roles;
    private String message;
    // Constructors, Getters, Setters...
}
```

### 9.2. Login API trả JWT

```java
@PostMapping("/login")
public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest request) {
    // 1. Xác thực credentials qua AuthenticationManager
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    // 2. Lấy UserDetails đã xác thực
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    // 3. Tạo JWT tokens
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // 4. Trả về response kèm tokens
    Set<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

    JwtAuthResponse response = new JwtAuthResponse(
            accessToken, refreshToken, "Bearer",
            accessTokenExpirationMs / 1000,  // Convert ms → giây
            userDetails.getId(), userDetails.getFullName(),
            userDetails.getEmail(), roles, "Đăng nhập thành công!"
    );

    return ResponseEntity.ok(response);
}
```

---

## 10. Refresh Token — Cơ Chế Xoay Token

### 10.1. Tại sao cần Refresh Token?

```mermaid
graph TD
    subgraph Problem["❌ VẤN ĐỀ: Không có Refresh Token"]
        A1["Access Token hết hạn sau 30 phút"]
        A2["User phải đăng nhập lại!"]
        A3["Trải nghiệm tệ 😤"]
        A1 --> A2 --> A3
    end

    subgraph Solution["✅ GIẢI PHÁP: Có Refresh Token"]
        B1["Access Token hết hạn sau 30 phút"]
        B2["Client tự động gửi Refresh Token"]
        B3["Server trả Access Token mới"]
        B4["User không bị gián đoạn 😊"]
        B1 --> B2 --> B3 --> B4
    end
```

### 10.2. Luồng Refresh Token

```mermaid
sequenceDiagram
    participant Client
    participant Server
    participant JwtSvc as "JwtService"

    Client->>Server: GET /api/users<br/>Authorization: Bearer <accessToken_cũ>
    Server-->>Client: 401 Unauthorized (token hết hạn)

    Client->>Server: POST /api/auth/refresh<br/>{refreshToken: "eyJ..."}
    Server->>JwtSvc: validateToken(refreshToken)
    JwtSvc-->>Server: Hợp lệ ✅, email = "admin@gmail.com"
    Server->>JwtSvc: generateAccessToken(email)
    JwtSvc-->>Server: newAccessToken
    Server-->>Client: 200 OK {accessToken: "eyJ...new"}

    Client->>Server: GET /api/users<br/>Authorization: Bearer <accessToken_mới>
    Server-->>Client: 200 OK — Data
```

### 10.3. Refresh Token API

```java
@PostMapping("/refresh")
public ResponseEntity<JwtAuthResponse> refreshToken(
        @RequestBody Map<String, String> request) {

    String refreshToken = request.get("refreshToken");

    if (refreshToken == null || refreshToken.isBlank()) {
        return ResponseEntity.badRequest().build();
    }

    // 1. Validate refresh token
    String email = jwtService.extractUsername(refreshToken);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    if (!jwtService.isTokenValid(refreshToken, userDetails)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // 2. Tạo Access Token mới (Refresh Token giữ nguyên)
    String newAccessToken = jwtService.generateAccessToken(userDetails);

    // 3. Trả về token mới
    JwtAuthResponse response = new JwtAuthResponse();
    response.setAccessToken(newAccessToken);
    response.setRefreshToken(refreshToken);  // Giữ nguyên refresh token
    response.setMessage("Token đã được làm mới thành công!");

    return ResponseEntity.ok(response);
}
```

### 10.4. Access Token vs Refresh Token — So sánh

| | Access Token | Refresh Token |
|:---|:---|:---|
| **Thời hạn** | Ngắn (15 phút — 1 giờ) | Dài (7 — 30 ngày) |
| **Mục đích** | Xác thực mỗi API request | Xin cấp Access Token mới |
| **Chứa roles?** | ✅ Có (tránh query DB) | ❌ Không (tải lại roles mới nhất khi refresh) |
| **Gửi đi đâu?** | Header `Authorization: Bearer ...` | Body POST `/api/auth/refresh` |
| **Lưu ở client** | Memory (JavaScript variable) | httpOnly cookie hoặc secure storage |
| **Bị đánh cắp** | Thiệt hại nhỏ (hết hạn nhanh) | Thiệt hại lớn (sống lâu) |

---

## 11. Bảo Mật JWT — Secret Key, Revocation, Best Practices

### 11.1. Secret Key Management

```properties
# ❌ SAI: Hard-code trong source code
jwt.secret=mySecretKey123

# ✅ ĐÚNG: Đọc từ biến môi trường
jwt.secret=${JWT_SECRET_KEY}

# ✅ ĐÚNG: Key phải đủ dài (>= 256 bits cho HS256)
# Sinh key an toàn:
# openssl rand -base64 64
jwt.secret=dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciBqd3QgYXV0aGVudGljYXRpb24gdGhhdCBpcyBhdCBsZWFzdCAyNTYgYml0cw==
```

### 11.2. Chiến lược Token Revocation (Thu hồi token)

| Chiến lược | Cơ chế | Ưu điểm | Nhược điểm |
|:---|:---|:---|:---|
| **Short-lived tokens** | Access token hết hạn sau 15 phút | Đơn giản, không cần DB | Không thu hồi tức thì |
| **Token Blacklist** | Lưu token bị thu hồi vào DB/Redis | Thu hồi tức thì | Mất tính stateless, cần tra cứu mỗi request |
| **Token Version** | Mỗi user có `tokenVersion` trong DB; nhúng vào JWT | Thu hồi theo user (đổi password → tăng version) | Cần query DB mỗi request |
| **Refresh Token Rotation** | Mỗi lần refresh → sinh cả cặp token mới, hủy refresh cũ | Giảm rủi ro refresh token bị đánh cắp | Phức tạp hơn |

### 11.3. Best Practices tổng hợp

> [!TIP]
> **Checklist bảo mật JWT cho Production:**
> 1. ✅ Luôn dùng **HTTPS** — JWT gửi qua HTTP plaintext sẽ bị sniff
> 2. ✅ **Secret key ≥ 256 bits** — dùng `openssl rand -base64 64` để sinh
> 3. ✅ Lưu secret key trong **environment variable** hoặc vault, KHÔNG hard-code
> 4. ✅ Access token **hết hạn ngắn** (15-60 phút)
> 5. ✅ **KHÔNG đặt thông tin nhạy cảm** trong JWT payload
> 6. ✅ Validate token trên **mỗi request** — không cache kết quả
> 7. ✅ Sử dụng **Refresh Token Rotation** cho production
> 8. ✅ Log các event bảo mật: login thất bại, token hết hạn, token bị từ chối

---

## 12. HMAC vs RSA/EC — Symmetric vs Asymmetric Signing

```mermaid
graph TD
    subgraph HMAC["🔑 HMAC-SHA256 (Symmetric)"]
        HK["Cùng 1 Secret Key"]
        HS["Server A: Ký token<br/>bằng Secret Key"]
        HV["Server B: Verify token<br/>bằng cùng Secret Key"]
        HK --> HS
        HK --> HV
    end

    subgraph RSA["🔐 RSA/EC (Asymmetric)"]
        PriK["Private Key<br/>(giữ bí mật)"]
        PubK["Public Key<br/>(chia sẻ công khai)"]
        RS["Auth Server: Ký token<br/>bằng Private Key"]
        RV["Resource Server: Verify token<br/>bằng Public Key"]
        PriK --> RS
        PubK --> RV
    end
```

| | HMAC (Symmetric) | RSA/EC (Asymmetric) |
|:---|:---|:---|
| **Key** | 1 secret key dùng chung | 1 private key + 1 public key |
| **Ký token** | Bằng secret key | Bằng private key |
| **Verify token** | Bằng cùng secret key | Bằng public key |
| **Bảo mật** | Mọi service biết key đều có thể ký giả | Chỉ Auth Server mới ký được |
| **Phù hợp** | Monolithic, 1 server | Microservices, OAuth2 Provider |
| **Bài này dùng** | ✅ HMAC-SHA256 | Bài 20 (OAuth2 Resource Server) |

---

## 13. Liên Hệ Bài 18 — Từ HTTP Basic Đến JWT Bearer

```mermaid
graph LR
    subgraph Bai18["📘 Bài 18: HTTP Basic"]
        B18_1["Mỗi request gửi<br/>username:password"]
        B18_2["Server query DB<br/>mỗi request"]
        B18_3["Stateful ngầm<br/>(phụ thuộc DB)"]
    end

    subgraph Bai19["📘 Bài 19: JWT Bearer"]
        B19_1["Login 1 lần<br/>→ nhận JWT token"]
        B19_2["Server chỉ verify chữ ký<br/>(không query DB*)"]
        B19_3["Stateless hoàn toàn"]
    end

    Bai18 -->|"Nâng cấp"| Bai19
```

| Thành phần | Bài 18 | Bài 19 |
|:---|:---|:---|
| **SecurityConfig** | `httpBasic()` | `addFilterBefore(jwtFilter)` + `STATELESS` |
| **AuthController /login** | Trả `AuthResponse` (không token) | Trả `JwtAuthResponse` (accessToken + refreshToken) |
| **Client gửi credentials** | Mỗi request: `Authorization: Basic ...` | Chỉ khi login; sau đó: `Authorization: Bearer ...` |
| **Session Management** | Mặc định (STATEFUL) | `SessionCreationPolicy.STATELESS` |
| **Thêm mới** | — | `JwtService`, `JwtAuthenticationFilter`, `JwtAuthResponse` |

> _(*) Trong bài thực hành này, filter vẫn query DB (loadUserByUsername) để lấy thông tin mới nhất. Ở production, có thể cache hoặc lấy roles trực tiếp từ JWT claims._

---

## 14. Triển Khai Thực Hành — Danh Sách File

| # | File | Hành động | Mô tả |
|:---:|:---|:---:|:---|
| 1 | `pom.xml` | Sửa | Thêm 3 dependency jjwt |
| 2 | `application.properties` | Sửa | Thêm `jwt.secret`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration` |
| 3 | `JwtService.java` | Tạo mới | Tạo/parse/validate JWT token |
| 4 | `JwtAuthenticationFilter.java` | Tạo mới | Filter trích JWT, nạp SecurityContext |
| 5 | `JwtAuthResponse.java` | Tạo mới | DTO response chứa accessToken + refreshToken |
| 6 | `SecurityConfig.java` | Sửa | Stateless session + gắn JwtFilter |
| 7 | `AuthController.java` | Sửa | Login/Register trả JWT, thêm /refresh API |

---

## 15. Bảng Tổng Hợp Components & Annotations

| Component | Thuộc về | Mục đích |
|:---|:---|:---|
| `JwtService` | `@Service` | Tạo, parse, validate JWT token |
| `JwtAuthenticationFilter` | `@Component` extends `OncePerRequestFilter` | Chặn request, trích JWT, nạp SecurityContext |
| `JwtAuthResponse` | DTO | Response chứa accessToken, refreshToken, user info |
| `@Value("${jwt.secret}")` | Spring Boot | Inject secret key từ properties |
| `SessionCreationPolicy.STATELESS` | Spring Security | Không tạo HTTP session |
| `addFilterBefore()` | `HttpSecurity` | Gắn custom filter vào vị trí cụ thể trong chain |
| `Jwts.builder()` | jjwt library | Builder pattern tạo JWT |
| `Jwts.parser()` | jjwt library | Parser pattern giải mã JWT |
| `SecretKey` | javax.crypto | Key mã hóa đối xứng |

---

## 16. Design Patterns Trong JWT Authentication

| Pattern | Áp dụng ở đâu | Mô tả |
|:---|:---|:---|
| **Builder** | `Jwts.builder()...compact()` | Xây dựng JWT token từng bước với fluent API |
| **Strategy** | `SignatureAlgorithm` (HS256, RS256) | Thay đổi thuật toán ký mà không đổi code tạo token |
| **Chain of Responsibility** | Security Filter Chain | JwtFilter xử lý xong → chuyển cho filter tiếp theo |
| **Template Method** | `OncePerRequestFilter.doFilterInternal()` | Lớp cha xử lý "chỉ chạy 1 lần", subclass chỉ override logic |
| **Factory Method** | `Keys.hmacShaKeyFor()` | Tạo SecretKey từ byte array mà không cần biết implementation |
| **Decorator** | `UsernamePasswordAuthenticationToken` bọc `UserDetails` | Bọc thêm thông tin authentication lên UserDetails |

---

## 17. Câu Hỏi Phỏng Vấn

### 🟢 Mức Cơ Bản

**Q1: JWT gồm mấy phần? Mỗi phần chứa gì?**

> JWT gồm **3 phần** phân cách bởi dấu chấm `.`:
> 1. **Header:** Chứa thuật toán ký (`alg`: HS256) và loại token (`typ`: JWT)
> 2. **Payload:** Chứa các claims — thông tin user (sub, iat, exp, roles, userId...)
> 3. **Signature:** Chữ ký số = HMAC-SHA256(base64(header) + "." + base64(payload), secretKey)
>
> Header và Payload chỉ được Base64 encode (không mã hóa). Signature đảm bảo token không bị sửa đổi.

**Q2: JWT là mã hóa (encryption) hay ký số (signing)?**

> JWT **tiêu chuẩn (JWS)** là **ký số**, KHÔNG phải mã hóa. Bất kỳ ai cũng có thể decode Base64 để đọc payload. Signature chỉ đảm bảo **tính toàn vẹn** (integrity) — token chưa bị sửa đổi, KHÔNG đảm bảo **tính bảo mật** (confidentiality). Nếu cần mã hóa payload, phải dùng **JWE (JSON Web Encryption)** — đó là tiêu chuẩn khác.

**Q3: Tại sao Access Token nên có thời hạn ngắn?**

> Vì JWT không thể bị thu hồi (revoke) sau khi phát hành — chữ ký vẫn hợp lệ cho đến khi hết hạn. Nếu token bị đánh cắp mà thời hạn 30 ngày, attacker có 30 ngày truy cập hệ thống. Thời hạn ngắn (15-60 phút) giới hạn cửa sổ tấn công (attack window). Kết hợp Refresh Token để user không phải login lại.

**Q4: Tại sao không lưu thông tin nhạy cảm trong JWT payload?**

> Vì payload chỉ được **Base64 encode** — bất kỳ ai có token đều decode được bằng lệnh `echo "eyJz..." | base64 -d`. Không bao giờ đặt password, credit card, SSN hoặc dữ liệu cá nhân nhạy cảm vào payload.

**Q5: Bearer Token nghĩa là gì?**

> "Bearer" nghĩa là "người mang". `Authorization: Bearer <token>` có nghĩa: "bất kỳ ai **mang** (bear) token hợp lệ này đều được coi là đã xác thực". Đây là lý do token phải được bảo vệ cẩn thận — giống như chìa khóa nhà, ai cầm chìa đều mở được cửa.

### 🟡 Mức Trung Bình

**Q6: JwtAuthenticationFilter kế thừa OncePerRequestFilter thay vì GenericFilterBean — tại sao?**

> `OncePerRequestFilter` đảm bảo filter chỉ chạy **đúng 1 lần** mỗi HTTP request, kể cả khi request bị forward hoặc include trong servlet pipeline. `GenericFilterBean` không có cơ chế này — có thể chạy nhiều lần cho 1 request, gây ra việc xác thực JWT trùng lặp hoặc lỗi logic.

**Q7: Refresh Token khác Access Token như thế nào?**

> | | Access Token | Refresh Token |
> |:---|:---|:---|
> | **Thời hạn** | Ngắn (15-60 phút) | Dài (7-30 ngày) |
> | **Mục đích** | Xác thực API request | Xin cấp Access Token mới |
> | **Chứa roles?** | Có | Không (tải lại roles mới nhất khi refresh) |
> | **Gửi trong** | Header `Authorization: Bearer ...` | Body POST `/api/auth/refresh` |
>
> Refresh Token KHÔNG chứa roles để khi user bị thay đổi quyền, Access Token mới sẽ phản ánh roles mới nhất từ database.

**Q8: Tại sao cần `SessionCreationPolicy.STATELESS` khi dùng JWT?**

> Nếu không set STATELESS, Spring Security mặc định tạo HTTP session (`JSESSIONID` cookie). Khi đó:
> 1. Client đăng nhập → Spring lưu session trên server → gửi cookie về client
> 2. Request tiếp theo: Spring xác thực bằng session cookie thay vì JWT
> 3. → JWT filter bị bypass, phá vỡ toàn bộ cơ chế stateless
>
> `STATELESS` buộc Spring Security **không tạo session** — mỗi request phải tự mang JWT token.

**Q9: `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` nghĩa là gì?**

> Gắn `JwtAuthenticationFilter` vào vị trí **trước** `UsernamePasswordAuthenticationFilter` trong Security Filter Chain. Mục đích: JWT filter phải chạy trước để trích token, xác thực, và nạp `SecurityContext`. Nếu gắn sau, `UsernamePasswordAuthenticationFilter` sẽ chạy trước và thất bại vì không có form login.

**Q10: Nếu attacker sửa payload JWT (đổi role từ USER thành ADMIN), có thành công không?**

> **Không.** Khi sửa payload, signature không còn khớp. Server tính lại `HMAC-SHA256(header.newPayload, secretKey)` → khác với signature trong token → reject token → 401 Unauthorized. Attacker phải biết `secretKey` để tạo signature mới — đó là lý do secret key phải được bảo vệ tuyệt đối.

### 🔴 Mức Nâng Cao

**Q11: So sánh HMAC (symmetric) và RSA (asymmetric) cho JWT signing. Khi nào dùng cái nào?**

> | | HMAC-SHA256 | RSA-256 / EC |
> |:---|:---|:---|
> | **Key** | 1 shared secret | Private key (ký) + Public key (verify) |
> | **Ai ký được?** | Bất kỳ service nào biết secret | Chỉ Auth Server (giữ private key) |
> | **Ai verify được?** | Bất kỳ service nào biết secret | Mọi service (public key) |
> | **Monolithic** | ✅ Đơn giản, phù hợp | Overkill |
> | **Microservices** | ⚠️ Mọi service biết secret → rủi ro | ✅ Resource Server chỉ cần public key |
> | **OAuth2 Provider** | Không dùng | ✅ Chuẩn (JWKS endpoint cung cấp public key) |

**Q12: Làm sao thu hồi (revoke) JWT trước khi hết hạn?**

> JWT tự bản chất là **stateless** — không có cơ chế thu hồi built-in. Các chiến lược:
> 1. **Token Blacklist (Redis/DB):** Lưu danh sách JWT bị thu hồi, kiểm tra mỗi request → mất tính stateless
> 2. **Short-lived + Refresh:** Access token 15 phút, thu hồi bằng cách refuse refresh
> 3. **Token Version trong DB:** Mỗi user có `tokenVersion`; nhúng vào JWT, kiểm tra mỗi request
> 4. **Refresh Token Rotation:** Mỗi refresh → sinh cả cặp mới, hủy refresh cũ
>
> Trade-off: Muốn thu hồi tức thì → phải mất tính stateless (query DB/cache mỗi request).

**Q13: Tại sao trong bài thực hành, JwtAuthenticationFilter vẫn query database (loadUserByUsername) mỗi request? Có cách nào tránh?**

> Trong bài thực hành ta query DB để lấy thông tin user mới nhất (roles, active status). Để tránh:
> 1. **Lấy roles trực tiếp từ JWT claims** — không cần query DB, nhưng roles có thể cũ
> 2. **Cache UserDetails trong Redis/Caffeine** — giảm DB load, TTL ngắn (5 phút)
> 3. **Tạo Authentication trực tiếp từ JWT claims** — hoàn toàn stateless, trade-off: không phản ánh thay đổi quyền real-time
>
> Production thường dùng phương án 2 (cache) hoặc 3 (claims-based) tùy yêu cầu consistency.

**Q14: JWT có thể bị tấn công theo những cách nào? Cách phòng tránh?**

> | Tấn công | Mô tả | Phòng tránh |
> |:---|:---|:---|
> | **Token theft** | Đánh cắp token qua XSS, network sniffing | HTTPS, httpOnly cookie, Content Security Policy |
> | **Brute-force secret** | Đoán secret key | Key ≥ 256 bits, rotation định kỳ |
> | **Algorithm confusion** | Đổi alg từ RS256 → HS256, dùng public key làm secret | Luôn chỉ định algorithm khi verify, không dùng `none` |
> | **Token replay** | Sử dụng lại token cũ | Short-lived tokens, nonce/jti claim |
> | **JWT injection** | Inject claims độc hại | Validate và sanitize claims |

**Q15: Self-invocation problem có ảnh hưởng đến `@PreAuthorize` khi dùng JWT không? Giải thích.**

> **Có, hoàn toàn giống Bài 17 (AOP) và Bài 18.** `@PreAuthorize` hoạt động qua **AOP Proxy**. Khi method A trong cùng class gọi `this.methodB()` (có `@PreAuthorize`), lời gọi đi thẳng đến object thật, bypass proxy → kiểm tra quyền bị bỏ qua. JWT chỉ thay đổi cách **xác thực** (từ Basic Auth sang token), không thay đổi cơ chế **phân quyền** (vẫn dùng AOP Proxy). Giải pháp: tách method sang class khác hoặc inject chính mình qua `@Lazy`.

---

> [!NOTE]
> **Bài tiếp theo (Bài 20):** Sẽ học cách dùng `spring-boot-starter-oauth2-resource-server` — thay thế JWT filter tự viết bằng cơ chế chuẩn của Spring Security. So sánh trade-off giữa "tự viết để hiểu" và "dùng framework chuẩn cho production".
