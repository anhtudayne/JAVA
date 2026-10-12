# 📘 BÀI 12: TRANSACTION MANAGEMENT — ĐẢM BẢO TOÀN VẸN DỮ LIỆU TRONG HỆ THỐNG

> **Mục tiêu bài học:**
> 1. Hiểu **ACID Properties** — 4 tính chất nền tảng đảm bảo tính toàn vẹn dữ liệu trong mọi hệ thống.
> 2. Nắm vững cơ chế hoạt động của **`@Transactional`** — Spring AOP Proxy, PlatformTransactionManager.
> 3. Hiểu sâu **Rollback Rules**: khi nào rollback tự động, khi nào KHÔNG, và cách tùy chỉnh.
> 4. Thành thạo **Propagation Levels** (7 mức) — chiến lược quản lý transaction lồng nhau (nested).
> 5. Phân biệt **Isolation Levels** (4 mức) — chống xung đột khi nhiều transaction truy cập đồng thời.
> 6. Tránh được **5 bẫy chết người** thường gặp với `@Transactional` (Self-Invocation, Checked Exception, private method...).
> 7. Hiểu **giới hạn** của `@Transactional` với hệ thống phân tán (Distributed Transaction) và các giải pháp nâng cao (Saga, Outbox Pattern).
> 8. Triển khai **OrderService** hoàn chỉnh với transaction đảm bảo tính toàn vẹn: tạo đơn hàng + giảm tồn kho trong cùng 1 transaction.

---

## 📑 MỤC LỤC

| Phần | Nội dung | Mục tiêu |
|:---:|:---|:---|
| 1 | 💥 Vấn đề — Tại sao cần Transaction? | Nhận diện Pain Point |
| 2 | 🧱 ACID Properties — 4 tính chất nền tảng | Kiến thức lý thuyết cốt lõi |
| 3 | ⚙️ Spring `@Transactional` — Cơ chế Proxy bên dưới | Hiểu bản chất hoạt động |
| 4 | 🔄 Rollback Rules — Khi nào rollback, khi nào không? | Rollback behavior |
| 5 | 📖 `readOnly = true` — Tối ưu hiệu năng cho READ | Performance optimization |
| 6 | 🌊 Propagation Levels — 7 chiến lược transaction lồng nhau | Nested transaction |
| 7 | 🔒 Isolation Levels — 4 mức cô lập dữ liệu | Concurrent access |
| 8 | 🪤 Self-Invocation Trap — Bẫy chết người của Spring Proxy | Tránh bug hóc búa |
| 9 | ⚠️ 5 lỗi phổ biến nhất với `@Transactional` | Anti-patterns |
| 10 | 🌐 Distributed Transaction — Giới hạn của `@Transactional` | Hệ thống phân tán |
| 11 | 💻 Thực hành — Triển khai OrderService với Transaction | Hands-on coding |
| 12 | 🎯 Best Practices & Anti-Patterns | Takeaway |
| 13 | 💼 Câu hỏi phỏng vấn (Interview Questions) | Chuẩn bị phỏng vấn |
| 14 | 📚 Kiến thức bổ sung: Java Core & Design Patterns | Nâng cao |
| 15 | 🔗 Tài liệu liên kết | References |

---

## PHẦN 1: 💥 VẤN ĐỀ — TẠI SAO CẦN TRANSACTION?

### 1.1. Kịch bản thực tế: Chuyển tiền ngân hàng

Giả sử bạn viết hệ thống chuyển tiền:

```java
// ❌ CODE NGUY HIỂM — KHÔNG CÓ TRANSACTION
public void transferMoney(Long fromAccountId, Long toAccountId, Double amount) {
    Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow();
    Account toAccount = accountRepository.findById(toAccountId).orElseThrow();

    // Bước 1: Trừ tiền người gửi
    fromAccount.setBalance(fromAccount.getBalance() - amount);
    accountRepository.save(fromAccount);  // ✅ SQL UPDATE thành công

    // 💀 ĐÚNG LÚC NÀY — server bị crash, mất điện, network timeout...

    // Bước 2: Cộng tiền người nhận — KHÔNG BAO GIỜ ĐƯỢC THỰC THI!
    toAccount.setBalance(toAccount.getBalance() + amount);
    accountRepository.save(toAccount);     // ❌ Không bao giờ chạy đến đây!
}
```

```
┌──────────────────────────────────────────────────────────────────────┐
│  💀 KẾT QUẢ KHI KHÔNG CÓ TRANSACTION:                              │
│                                                                      │
│  • Người gửi A: bị trừ 1,000,000đ ✅ (đã save vào DB)              │
│  • Người nhận B: KHÔNG được cộng tiền ❌ (server crash trước khi save)│
│  • 1,000,000đ "BỐC HƠI" khỏi hệ thống! 💸                         │
│                                                                      │
│  → Vi phạm ACID: Atomicity bị phá vỡ                                │
│  → Dữ liệu ở trạng thái INCONSISTENT (không nhất quán)              │
└──────────────────────────────────────────────────────────────────────┘
```

### 1.2. Giải pháp: Gói trong Transaction

```java
// ✅ CODE AN TOÀN — CÓ TRANSACTION
@Transactional  // ← Tất cả hoặc không gì cả (All or Nothing)
public void transferMoney(Long fromAccountId, Long toAccountId, Double amount) {
    Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow();
    Account toAccount = accountRepository.findById(toAccountId).orElseThrow();

    // Bước 1: Trừ tiền người gửi
    fromAccount.setBalance(fromAccount.getBalance() - amount);

    // Bước 2: Cộng tiền người nhận
    toAccount.setBalance(toAccount.getBalance() + amount);

    // Hibernate Dirty Checking: cả 2 thay đổi được flush cùng lúc khi commit
    // Nếu BẤT KỲ lỗi nào xảy ra → ROLLBACK toàn bộ → không ai mất tiền
}
```

```mermaid
sequenceDiagram
    participant Client
    participant Service as @Transactional<br/>Service Method
    participant Hibernate
    participant DB as Database

    Client->>Service: transferMoney(A→B, 1M)
    Service->>Hibernate: BEGIN TRANSACTION
    Hibernate->>DB: BEGIN

    Service->>Hibernate: fromAccount.setBalance(-1M)
    Note over Hibernate: Dirty Checking ghi nhận<br/>thay đổi (chưa flush SQL)

    Service->>Hibernate: toAccount.setBalance(+1M)
    Note over Hibernate: Dirty Checking ghi nhận<br/>thay đổi (chưa flush SQL)

    alt ✅ Không có lỗi
        Service->>Hibernate: Method return (kết thúc)
        Hibernate->>DB: UPDATE accounts SET balance=... WHERE id=A
        Hibernate->>DB: UPDATE accounts SET balance=... WHERE id=B
        Hibernate->>DB: COMMIT ✅
        DB-->>Client: Thành công: cả 2 cập nhật đồng thời
    else ❌ Có RuntimeException
        Service->>Hibernate: throw RuntimeException
        Hibernate->>DB: ROLLBACK ❌
        DB-->>Client: Thất bại: KHÔNG thay đổi nào được lưu
    end
```

---

## PHẦN 2: 🧱 ACID PROPERTIES — 4 TÍNH CHẤT NỀN TẢNG

### 2.1. Bảng tổng hợp ACID

| Tính chất | Ý nghĩa | Ví dụ cụ thể trong dự án | Ai đảm bảo? |
|:---:|:---|:---|:---|
| **🔷 Atomicity** (Nguyên tử) | Tất cả hoặc Không gì cả. Không có trạng thái "nửa vời" | Tạo Order + Giảm Stock = 1 đơn vị công việc. Nếu giảm stock fail → Order cũng bị hủy | **Transaction Manager** |
| **🔷 Consistency** (Nhất quán) | Dữ liệu luôn ở trạng thái hợp lệ trước & sau transaction | Tổng stock trước = tổng stock sau + quantity đã bán. Không bao giờ âm | **Business Logic** + **DB Constraints** |
| **🔷 Isolation** (Cô lập) | Các transaction đồng thời không ảnh hưởng lẫn nhau | 2 khách mua cùng sản phẩm cuối cùng → chỉ 1 người thành công | **Isolation Level** |
| **🔷 Durability** (Bền vững) | Dữ liệu đã COMMIT sẽ tồn tại vĩnh viễn, kể cả server crash | Sau khi đơn hàng xác nhận → dù restart server, data vẫn còn | **Database Engine** (WAL, fsync) |

### 2.2. Sơ đồ trực quan ACID

```mermaid
graph TB
    subgraph ACID["🧱 ACID Properties"]
        A["🔷 Atomicity<br/>All or Nothing"]
        C["🔷 Consistency<br/>Valid State → Valid State"]
        I["🔷 Isolation<br/>Concurrent Safety"]
        D["🔷 Durability<br/>Crash-Proof"]
    end

    A --> TM["Transaction Manager<br/>(Spring PlatformTransactionManager)"]
    C --> BL["Business Logic<br/>+ DB Constraints<br/>(CHECK, UNIQUE, FK)"]
    I --> IL["Isolation Levels<br/>(READ_COMMITTED,<br/>REPEATABLE_READ...)"]
    D --> DE["Database Engine<br/>(WAL, Redo Log,<br/>fsync to disk)"]

    style A fill:#4285f4,color:#fff
    style C fill:#0f9d58,color:#fff
    style I fill:#f4b400,color:#000
    style D fill:#db4437,color:#fff
```

### 2.3. Ví dụ ACID trong dự án springboot-learning

```java
/**
 * Kịch bản: Khách hàng đặt mua 3 sản phẩm MacBook Pro M3 (stock = 25)
 *
 * ATOMICITY:
 *   - Tạo Order (INSERT orders)
 *   - Giảm stock Product (UPDATE products SET stock = stock - 3)
 *   → Cả 2 phải cùng thành công hoặc cùng thất bại
 *
 * CONSISTENCY:
 *   - Trước: Product.stock = 25
 *   - Sau: Product.stock = 22  (không bao giờ = -1)
 *   - Order.totalAmount = 3 × 2499.99 = 7499.97 (phải đúng)
 *
 * ISOLATION:
 *   - 2 khách cùng đặt mua 24 chiếc MacBook (stock = 25)
 *   - Chỉ 1 người thành công, người kia nhận lỗi "Hết hàng"
 *
 * DURABILITY:
 *   - Sau khi đơn hàng COMMIT → dù restart server, data vẫn còn
 */
```

---

## PHẦN 3: ⚙️ SPRING `@TRANSACTIONAL` — CƠ CHẾ PROXY BÊN DƯỚI

### 3.1. Bản chất: Spring AOP Proxy

Khi bạn gắn `@Transactional` lên method, Spring **KHÔNG** thay đổi code gốc. Thay vào đó, Spring tạo một **Proxy object** bọc quanh bean thật:

```
┌───────────────────────────────────────────────────────────────────┐
│  🧩 LUỒNG GỌI METHOD @Transactional                              │
│                                                                   │
│  Controller gọi:  orderService.createOrder(request)              │
│                           │                                       │
│                           ▼                                       │
│  ┌─────────────────────────────────────────────────────┐         │
│  │  🔮 Spring AOP PROXY (OrderService$$SpringCGLIB)    │         │
│  │                                                     │         │
│  │  1. Kiểm tra @Transactional annotation              │         │
│  │  2. Lấy PlatformTransactionManager                  │         │
│  │  3. txManager.getTransaction(definition)             │         │
│  │     → Hibernate: Session.beginTransaction()          │         │
│  │     → JDBC: connection.setAutoCommit(false)          │         │
│  │                                                     │         │
│  │  4. ┌─────────────────────────────────┐              │         │
│  │     │  📦 GỌI METHOD THẬT:            │              │         │
│  │     │  orderService.createOrder(req)  │              │         │
│  │     └─────────────────────────────────┘              │         │
│  │                                                     │         │
│  │  5a. ✅ Không có exception:                          │         │
│  │      txManager.commit(status)                       │         │
│  │      → Hibernate flush + JDBC COMMIT                │         │
│  │                                                     │         │
│  │  5b. ❌ RuntimeException:                            │         │
│  │      txManager.rollback(status)                     │         │
│  │      → JDBC ROLLBACK                                │         │
│  └─────────────────────────────────────────────────────┘         │
└───────────────────────────────────────────────────────────────────┘
```

### 3.2. Sơ đồ Proxy Pattern (Mermaid)

```mermaid
sequenceDiagram
    participant Controller
    participant Proxy as 🔮 CGLIB Proxy<br/>(OrderService$$SpringCGLIB)
    participant TxManager as PlatformTransactionManager
    participant RealBean as 📦 OrderService<br/>(Bean thật)
    participant DB as Database

    Controller->>Proxy: createOrder(request)
    Proxy->>TxManager: getTransaction(txDef)
    TxManager->>DB: SET autocommit = false
    Note over DB: BEGIN TRANSACTION

    Proxy->>RealBean: createOrder(request)
    RealBean->>DB: INSERT INTO orders ...
    RealBean->>DB: UPDATE products SET stock = ...
    RealBean-->>Proxy: return order ✅

    alt ✅ Thành công
        Proxy->>TxManager: commit(txStatus)
        TxManager->>DB: COMMIT
    else ❌ RuntimeException
        Proxy->>TxManager: rollback(txStatus)
        TxManager->>DB: ROLLBACK
    end

    Proxy-->>Controller: return result
```

### 3.3. PlatformTransactionManager — Trái tim của Transaction

Spring sử dụng **Strategy Pattern** để trừu tượng hóa quản lý transaction:

```mermaid
classDiagram
    class PlatformTransactionManager {
        <<interface>>
        +getTransaction(TransactionDefinition) TransactionStatus
        +commit(TransactionStatus) void
        +rollback(TransactionStatus) void
    }

    class JpaTransactionManager {
        +getTransaction() TransactionStatus
        +commit() void
        +rollback() void
    }

    class DataSourceTransactionManager {
        +getTransaction() TransactionStatus
        +commit() void
        +rollback() void
    }

    class JtaTransactionManager {
        +getTransaction() TransactionStatus
        +commit() void
        +rollback() void
    }

    PlatformTransactionManager <|-- JpaTransactionManager : implements
    PlatformTransactionManager <|-- DataSourceTransactionManager : implements
    PlatformTransactionManager <|-- JtaTransactionManager : implements

    note for JpaTransactionManager "🏆 Dùng trong dự án của chúng ta\n(Spring Boot auto-config khi có JPA)"
    note for DataSourceTransactionManager "Dùng khi chỉ có JDBC\n(không có JPA/Hibernate)"
    note for JtaTransactionManager "Dùng cho Distributed Transaction\n(nhiều datasource)"
```

> [!NOTE]
> **Spring Boot tự động cấu hình `JpaTransactionManager`** khi phát hiện JPA dependency trong classpath. Bạn không cần khai báo bean thủ công — đây chính là sức mạnh của Auto-Configuration (đã học ở Bài 1).

### 3.4. Pseudo-code mà Spring Proxy thực sự làm

```java
// 🎯 Đây chính là code mà Spring AOP Proxy sinh ra "đằng sau" @Transactional
public class OrderService$$SpringCGLIB extends OrderService {

    private final PlatformTransactionManager txManager;

    @Override
    public Order createOrder(OrderRequest request) {
        // 1. Tạo TransactionDefinition từ @Transactional attributes
        TransactionDefinition def = new DefaultTransactionDefinition();
        // def.setPropagation(REQUIRED);
        // def.setIsolationLevel(DEFAULT);
        // def.setReadOnly(false);

        // 2. Bắt đầu transaction
        TransactionStatus status = txManager.getTransaction(def);

        try {
            // 3. Gọi method thật (bean gốc, KHÔNG phải proxy)
            Order result = super.createOrder(request);

            // 4. Commit nếu thành công
            txManager.commit(status);
            return result;

        } catch (RuntimeException ex) {
            // 5. Rollback nếu unchecked exception
            txManager.rollback(status);
            throw ex;
        }
        // ⚠️ Chú ý: Checked Exception KHÔNG rollback theo mặc định!
    }
}
```

---

## PHẦN 4: 🔄 ROLLBACK RULES — KHI NÀO ROLLBACK, KHI NÀO KHÔNG?

### 4.1. Quy tắc mặc định

```
┌──────────────────────────────────────────────────────────────────────────┐
│  📜 QUY TẮC ROLLBACK MẶC ĐỊNH CỦA @Transactional:                      │
│                                                                          │
│  ✅ ROLLBACK khi:                                                        │
│     • RuntimeException (unchecked) — và tất cả subclass                  │
│     • Error (OutOfMemoryError, StackOverflowError...)                    │
│                                                                          │
│  ❌ KHÔNG ROLLBACK khi:                                                  │
│     • Checked Exception (IOException, SQLException, ParseException...)   │
│     • Method return bình thường (dù data logic sai)                      │
│                                                                          │
│  → Đây là quy tắc "mặc định" — có thể tùy chỉnh bằng attribute         │
└──────────────────────────────────────────────────────────────────────────┘
```

### 4.2. Phân loại Exception trong Java (Java Core Knowledge)

```mermaid
graph TD
    Throwable["java.lang.Throwable"]
    Error["java.lang.Error<br/>❌ ROLLBACK mặc định<br/>(OutOfMemoryError,<br/>StackOverflowError)"]
    Exception["java.lang.Exception"]
    Checked["Checked Exceptions<br/>⚠️ KHÔNG ROLLBACK mặc định<br/>(IOException, SQLException,<br/>ParseException)"]
    Unchecked["RuntimeException<br/>✅ ROLLBACK mặc định<br/>(NullPointerException,<br/>IllegalArgumentException,<br/>ResourceNotFoundException)"]

    Throwable --> Error
    Throwable --> Exception
    Exception --> Checked
    Exception --> Unchecked

    style Error fill:#db4437,color:#fff
    style Unchecked fill:#0f9d58,color:#fff
    style Checked fill:#f4b400,color:#000
```

### 4.3. Tùy chỉnh Rollback Rules

```java
// ✅ 1. Rollback cho TẤT CẢ exception (cả Checked)
@Transactional(rollbackFor = Exception.class)
public void processPayment(PaymentRequest req) throws PaymentException {
    // PaymentException (checked) sẽ gây rollback
}

// ✅ 2. Rollback cho exception cụ thể
@Transactional(rollbackFor = {PaymentException.class, InsufficientStockException.class})
public Order createOrderWithPayment(OrderRequest req) throws PaymentException {
    // ...
}

// ✅ 3. KHÔNG rollback cho exception cụ thể (dù là RuntimeException)
@Transactional(noRollbackFor = EmailSendingException.class)
public void createOrderAndNotify(OrderRequest req) {
    orderRepository.save(order);           // rollback nếu fail
    emailService.sendConfirmation(order);  // EmailSendingException → KHÔNG rollback
    // Order vẫn được lưu dù gửi email thất bại
}

// ✅ 4. Kết hợp cả hai
@Transactional(
    rollbackFor = Exception.class,
    noRollbackFor = NotificationException.class
)
public void complexBusinessLogic() {
    // Rollback cho mọi exception, TRỪ NotificationException
}
```

### 4.4. Bảng so sánh rollback behavior

| Exception loại | Mặc định | Với `rollbackFor = Exception.class` |
|:---|:---:|:---:|
| `RuntimeException` (unchecked) | ✅ Rollback | ✅ Rollback |
| `Error` | ✅ Rollback | ✅ Rollback |
| `IOException` (checked) | ❌ Commit | ✅ Rollback |
| `SQLException` (checked) | ❌ Commit | ✅ Rollback |
| Custom `BusinessException extends RuntimeException` | ✅ Rollback | ✅ Rollback |

> [!IMPORTANT]
> **Best practice trong dự án của chúng ta:** Tất cả custom exception đã kế thừa `RuntimeException` (thông qua `BusinessException`):
> - `ResourceNotFoundException extends BusinessException extends RuntimeException`
> - `DuplicateResourceException extends BusinessException extends RuntimeException`
>
> → **Luôn tự động rollback theo mặc định!** Đây là thiết kế đúng đắn từ Bài 7.

---

## PHẦN 5: 📖 `readOnly = true` — TỐI ƯU HIỆU NĂNG CHO READ

### 5.1. readOnly = true làm gì bên dưới?

```java
@Transactional(readOnly = true)
public List<ProductResponse> getAllProducts() {
    return productRepository.findAll().stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
}
```

Khi `readOnly = true`, Hibernate thực hiện **3 tối ưu hóa** quan trọng:

```
┌───────────────────────────────────────────────────────────────────────┐
│  📖 readOnly = true — Hibernate Optimizations:                       │
│                                                                       │
│  1. ❌ SKIP Dirty Checking:                                           │
│     Hibernate KHÔNG tạo snapshot copy của entity                     │
│     → Tiết kiệm RAM (đặc biệt khi load nhiều entity)                │
│     → Tiết kiệm CPU (không so sánh field khi flush)                  │
│                                                                       │
│  2. ❌ SKIP Flush tự động:                                            │
│     FlushMode = MANUAL → không tự flush trước query                  │
│     → Giảm roundtrip tới database                                    │
│                                                                       │
│  3. 📡 Database hint:                                                  │
│     JDBC: connection.setReadOnly(true)                               │
│     → Database có thể route query tới READ REPLICA                   │
│     → PostgreSQL: tránh WAL overhead                                  │
│     → MySQL: có thể bypass locking overhead                          │
└───────────────────────────────────────────────────────────────────────┘
```

### 5.2. So sánh readOnly = false vs true

| Tiêu chí | `readOnly = false` (mặc định) | `readOnly = true` |
|:---|:---:|:---:|
| Dirty Checking | ✅ Bật (tạo snapshot) | ❌ Tắt (không tạo snapshot) |
| Flush Mode | AUTO (flush trước mỗi query) | MANUAL (không auto flush) |
| RAM usage | Cao (2x entity size cho snapshot) | Thấp (1x entity size) |
| Có thể UPDATE/INSERT? | ✅ Có | ⚠️ Vẫn chạy nhưng **bị bỏ qua khi flush** |
| JDBC hint | `connection.setReadOnly(false)` | `connection.setReadOnly(true)` |
| Use case | CREATE, UPDATE, DELETE | SELECT, COUNT, AGGREGATE |

### 5.3. Pattern trong dự án springboot-learning

```java
@Service
public class ProductService {

    // ✅ READ method → readOnly = true
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() { ... }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) { ... }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProductsPaged(Pageable pageable) { ... }

    // ✅ WRITE method → readOnly = false (mặc định)
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) { ... }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) { ... }

    @Transactional
    public void deleteProduct(Long id) { ... }
}
```

> [!TIP]
> **Pattern hay:** Đặt `@Transactional(readOnly = true)` ở **class level**, rồi override bằng `@Transactional` cho từng write method. Spring sẽ ưu tiên annotation trên method:
>
> ```java
> @Service
> @Transactional(readOnly = true) // Mặc định: tất cả method là read-only
> public class ProductService {
>
>     public List<ProductResponse> getAllProducts() { ... }  // Kế thừa: readOnly = true
>
>     @Transactional // Override: readOnly = false (cho write)
>     public ProductResponse createProduct(...) { ... }
> }
> ```

---

## PHẦN 6: 🌊 PROPAGATION LEVELS — 7 CHIẾN LƯỢC TRANSACTION LỒNG NHAU

### 6.1. Vấn đề: Method A (có transaction) gọi Method B (cũng có transaction)

```java
@Transactional
public void methodA() {
    // Đang trong transaction A
    methodB();  // ← B cũng có @Transactional → tạo transaction mới hay dùng chung?
}

@Transactional
public void methodB() {
    // Transaction nào sẽ được sử dụng ở đây?
}
```

→ Câu trả lời phụ thuộc vào **Propagation Level**.

### 6.2. Bảng tổng hợp 7 Propagation Levels

| Propagation | Hành vi | Khi nào dùng? | Tần suất sử dụng |
|:---|:---|:---|:---:|
| **`REQUIRED`** (mặc định) | Có TX → dùng chung. Chưa có → tạo mới | **90% use case**: mọi business method thông thường | ⭐⭐⭐⭐⭐ |
| **`REQUIRES_NEW`** | **Luôn** tạo TX mới, tạm dừng TX cũ | Audit log, notification — phải persist dù TX chính fail | ⭐⭐⭐ |
| **`NESTED`** | Tạo **savepoint** trong TX hiện tại. Rollback savepoint không rollback TX cha | Thử các thao tác phụ, nếu fail thì chỉ rollback phần phụ | ⭐⭐ |
| **`SUPPORTS`** | Có TX → dùng. Không có → chạy không TX | Method hoạt động OK cả có lẫn không có TX (hiếm dùng) | ⭐ |
| **`NOT_SUPPORTED`** | **Tạm dừng** TX hiện tại, chạy không TX | Gọi hệ thống ngoài (external API) không cần TX | ⭐ |
| **`MANDATORY`** | **Bắt buộc** phải có TX sẵn, nếu không → ném exception | Enforce: method này PHẢI được gọi trong TX | ⭐ |
| **`NEVER`** | **Bắt buộc** KHÔNG có TX, nếu có → ném exception | Enforce: method này KHÔNG ĐƯỢC nằm trong TX | ⭐ |

### 6.3. Sơ đồ trực quan REQUIRED vs REQUIRES_NEW

```mermaid
graph TB
    subgraph REQUIRED["📘 REQUIRED (mặc định)"]
        direction TB
        RA["methodA() bắt đầu"] --> RT["TX-1 được tạo"]
        RT --> RB["methodB() THAM GIA TX-1"]
        RB --> RC["Cả 2 method<br/>dùng CHUNG TX-1"]
        RC --> RD["COMMIT hoặc ROLLBACK<br/>ĐỒng thời"]
    end

    subgraph REQUIRES_NEW["📕 REQUIRES_NEW"]
        direction TB
        NA["methodA() bắt đầu"] --> NT["TX-1 được tạo"]
        NT --> NP["TX-1 TẠM DỪNG ⏸️"]
        NP --> NB["methodB() tạo TX-2 MỚI"]
        NB --> NC["TX-2 COMMIT/ROLLBACK<br/>ĐỘC LẬP"]
        NC --> ND["TX-1 TIẾP TỤC ▶️"]
    end

    style REQUIRED fill:#e8f5e9
    style REQUIRES_NEW fill:#fce4ec
```

### 6.4. Ví dụ thực tế: REQUIRES_NEW cho Audit Log

```java
@Service
public class OrderService {

    private final AuditLogService auditLogService;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Validate & tạo order
        Order order = orderRepository.save(buildOrder(request));

        // 2. Giảm stock
        productService.reduceStock(request.getProductId(), request.getQuantity());

        // 3. Ghi audit log — PHẢI persist dù order fail
        auditLogService.logAction("CREATE_ORDER", order.getId());
        // ↑ Nếu sau dòng này có exception → order rollback
        //   nhưng audit log PHẢI VẪN CÒN (để trace lỗi)

        return order;
    }
}

@Service
public class AuditLogService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // ↑ Tạo transaction MỚI, ĐỘC LẬP với transaction của OrderService
    public void logAction(String action, Long entityId) {
        AuditLog log = new AuditLog(action, entityId, LocalDateTime.now());
        auditLogRepository.save(log);
        // Transaction này COMMIT ngay lập tức, không bị ảnh hưởng
        // nếu transaction của OrderService rollback sau đó
    }
}
```

### 6.5. Ví dụ thực tế: NESTED với Savepoint

```java
@Service
public class OrderService {

    @Transactional
    public Order createOrderWithBonusAttempt(OrderRequest request) {
        // 1. Tạo order (TX chính)
        Order order = orderRepository.save(buildOrder(request));

        // 2. THỬ tặng bonus — nếu fail thì chỉ bỏ bonus, order vẫn OK
        try {
            bonusService.tryAwardBonus(order.getUser(), order.getTotalAmount());
        } catch (Exception e) {
            log.warn("Bonus thất bại, nhưng order vẫn tiếp tục: {}", e.getMessage());
            // Savepoint đã rollback → bonus không được tạo
            // Nhưng TX chính (order) vẫn chạy tiếp
        }

        return order; // ← Order vẫn COMMIT thành công
    }
}

@Service
public class BonusService {

    @Transactional(propagation = Propagation.NESTED)
    // ↑ Tạo SAVEPOINT trong TX chính
    public void tryAwardBonus(User user, Double orderAmount) {
        if (orderAmount < 100) {
            throw new RuntimeException("Đơn hàng quá nhỏ để tặng bonus");
            // → Rollback đến SAVEPOINT, KHÔNG rollback TX chính
        }
        bonusRepository.save(new Bonus(user, calculateBonus(orderAmount)));
    }
}
```

> [!WARNING]
> **`NESTED` chỉ hoạt động với `JpaTransactionManager`** (JDBC savepoint). Với `JtaTransactionManager`, `NESTED` sẽ ném `NestedTransactionNotSupportedException`.

---

## PHẦN 7: 🔒 ISOLATION LEVELS — 4 MỨC CÔ LẬP DỮ LIỆU

### 7.1. Vấn đề: Concurrent Access (Truy cập đồng thời)

Khi nhiều transaction chạy song song, có thể xảy ra **3 hiện tượng bất thường (anomalies)**:

| Anomaly | Mô tả | Ví dụ |
|:---|:---|:---|
| 🔸 **Dirty Read** | Đọc dữ liệu mà transaction khác **chưa commit** | TX-A update stock = 0, chưa commit. TX-B đọc stock = 0 → từ chối bán. TX-A rollback → stock = 25 trở lại. TX-B đã từ chối sai! |
| 🔸 **Non-Repeatable Read** | Đọc cùng 1 dòng 2 lần trong cùng TX nhưng ra **kết quả khác** | TX-A đọc stock = 25. TX-B update stock = 20 rồi commit. TX-A đọc lại → stock = 20 (khác lần 1!) |
| 🔸 **Phantom Read** | Đọc cùng 1 query 2 lần nhưng **số dòng kết quả khác** | TX-A: SELECT COUNT(*) FROM products WHERE category='Laptop' = 5. TX-B INSERT thêm 1 laptop. TX-A chạy lại query → = 6 (xuất hiện "ma"!) |

### 7.2. Bảng 4 Isolation Levels

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read | Performance | Use case |
|:---|:---:|:---:|:---:|:---:|:---|
| `READ_UNCOMMITTED` | ⚠️ Có thể | ⚠️ Có thể | ⚠️ Có thể | 🟢 Nhanh nhất | Hầu như không dùng |
| `READ_COMMITTED` ⭐ | ✅ Chặn | ⚠️ Có thể | ⚠️ Có thể | 🟡 Tốt | **Default** (PostgreSQL, Oracle, H2) |
| `REPEATABLE_READ` | ✅ Chặn | ✅ Chặn | ⚠️ Có thể | 🟠 Khá | **Default** MySQL InnoDB |
| `SERIALIZABLE` | ✅ Chặn | ✅ Chặn | ✅ Chặn | 🔴 Chậm nhất | Tài chính, banking (cần tuyệt đối chính xác) |

### 7.3. Sơ đồ trực quan Isolation Levels

```mermaid
graph LR
    RU["READ_UNCOMMITTED<br/>🔓 Không khóa gì"]
    RC["READ_COMMITTED<br/>🔒 Khóa khi write"]
    RR["REPEATABLE_READ<br/>🔒🔒 Khóa khi read+write"]
    S["SERIALIZABLE<br/>🔒🔒🔒 Khóa toàn bộ"]

    RU -->|"Chặn Dirty Read"| RC
    RC -->|"Chặn Non-Repeatable Read"| RR
    RR -->|"Chặn Phantom Read"| S

    subgraph tradeoff["⚖️ Trade-off"]
        P["Performance 🚀"] -.-> C["Consistency 🛡️"]
    end

    style RU fill:#ffcdd2
    style RC fill:#c8e6c9
    style RR fill:#fff9c4
    style S fill:#bbdefb
```

### 7.4. Cách khai báo Isolation Level trong Spring

```java
// Sử dụng isolation level cụ thể
@Transactional(isolation = Isolation.READ_COMMITTED)
public ProductResponse getProductById(Long id) { ... }

// Cho các thao tác tài chính nghiêm ngặt
@Transactional(isolation = Isolation.SERIALIZABLE)
public void transferMoney(Long fromId, Long toId, Double amount) { ... }

// DEFAULT = sử dụng isolation level mặc định của database
@Transactional(isolation = Isolation.DEFAULT)
public Order createOrder(OrderRequest request) { ... }
```

> [!TIP]
> **Trong thực tế**, hầu hết ứng dụng dùng `READ_COMMITTED` (mặc định) và xử lý concurrency bằng cách khác:
> - **Optimistic Locking** (`@Version`): Kiểm tra conflict khi commit (Bài sau)
> - **Pessimistic Locking** (`@Lock(PESSIMISTIC_WRITE)`): Khóa row khi đọc
> - **Atomic SQL**: `UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty` (đã có trong `ProductRepository.reduceStockAtomic()`)

---

## PHẦN 8: 🪤 SELF-INVOCATION TRAP — BẪY CHẾT NGƯỜI CỦA SPRING PROXY

### 8.1. Vấn đề: Gọi @Transactional method từ CÙNG class

```java
@Service
public class OrderService {

    // ❌ BUG: @Transactional KHÔNG HOẠT ĐỘNG ở đây!
    public void processOrder(OrderRequest request) {
        // Đây là "self-invocation" — gọi method trong cùng class
        this.createOrder(request);
        // ↑ this.createOrder() → gọi TRỰC TIẾP method, BYPASS proxy!
        // → @Transactional bị bỏ qua hoàn toàn!
    }

    @Transactional  // ← Annotation này BỊ IGNORE khi gọi từ processOrder()
    public Order createOrder(OrderRequest request) {
        orderRepository.save(order);
        productService.reduceStock(...);
        // Nếu exception ở đây → KHÔNG rollback vì KHÔNG có transaction!
        return order;
    }
}
```

### 8.2. Giải thích bằng sơ đồ

```mermaid
graph TB
    subgraph correct["✅ GỌI TỪ BÊN NGOÀI (qua Proxy)"]
        Controller1["Controller"] -->|"gọi"| Proxy1["🔮 Proxy"]
        Proxy1 -->|"BEGIN TX"| Real1["📦 OrderService<br/>.createOrder()"]
        Real1 -->|"return"| Proxy1
        Proxy1 -->|"COMMIT"| Controller1
    end

    subgraph wrong["❌ SELF-INVOCATION (bypass Proxy)"]
        Controller2["Controller"] -->|"gọi"| Proxy2["🔮 Proxy"]
        Proxy2 -->|"delegate"| Real2["📦 OrderService<br/>.processOrder()"]
        Real2 -->|"this.createOrder()<br/>GỌI TRỰC TIẾP ⚠️"| Real3["📦 OrderService<br/>.createOrder()"]
        Note2["❌ Proxy KHÔNG CAN THIỆP<br/>→ KHÔNG có transaction!"]
    end

    style correct fill:#e8f5e9
    style wrong fill:#ffebee
```

```
┌──────────────────────────────────────────────────────────────────────┐
│  🪤 TẠI SAO SELF-INVOCATION BYPASS PROXY?                           │
│                                                                      │
│  Khi bạn viết: this.createOrder(request)                            │
│                                                                      │
│  • "this" ở đây = chính bean THẬT (OrderService instance)           │
│  • KHÔNG PHẢI proxy (OrderService$$SpringCGLIB)                     │
│  • → Proxy không can thiệp → không có BEGIN TX / COMMIT / ROLLBACK  │
│                                                                      │
│  Chỉ khi gọi từ BÊN NGOÀI (Controller → Proxy → Bean thật)        │
│  thì Proxy mới can thiệp được                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### 8.3. Giải pháp cho Self-Invocation

```java
// ✅ GIẢI PHÁP 1: Tách logic sang class khác (KHUYẾN NGHỊ)
@Service
public class OrderFacadeService {

    private final OrderService orderService;

    public void processOrder(OrderRequest request) {
        // Gọi qua proxy của OrderService → @Transactional hoạt động!
        orderService.createOrder(request);
    }
}

@Service
public class OrderService {

    @Transactional
    public Order createOrder(OrderRequest request) {
        // Transaction hoạt động đúng vì được gọi qua proxy
        return orderRepository.save(buildOrder(request));
    }
}
```

```java
// ✅ GIẢI PHÁP 2: Inject chính mình (Self-injection) — ít dùng hơn
@Service
public class OrderService {

    @Lazy
    @Autowired
    private OrderService self;  // Inject PROXY của chính mình

    public void processOrder(OrderRequest request) {
        self.createOrder(request);  // Gọi qua PROXY → TX hoạt động
    }

    @Transactional
    public Order createOrder(OrderRequest request) {
        return orderRepository.save(buildOrder(request));
    }
}
```

> [!CAUTION]
> **Giải pháp 1 (tách class) là best practice** vì tuân thủ Single Responsibility Principle. Giải pháp 2 tạo circular dependency (dù `@Lazy` giải quyết được) nhưng code khó đọc và maintain.

---

## PHẦN 9: ⚠️ 5 LỖI PHỔ BIẾN NHẤT VỚI `@Transactional`

### Lỗi #1: Self-Invocation (đã trình bày ở Phần 8)

### Lỗi #2: `@Transactional` trên method `private`

```java
@Service
public class OrderService {

    @Transactional  // ❌ BỊ IGNORE! Spring Proxy không override được private method
    private Order internalCreateOrder(OrderRequest req) {
        return orderRepository.save(buildOrder(req));
    }
}
```

> **Lý do**: CGLIB Proxy tạo subclass → không thể override `private` method → annotation bị bỏ qua hoàn toàn.
> **Fix**: Đổi sang `public` hoặc `protected`.

### Lỗi #3: Checked Exception không rollback

```java
@Transactional  // ❌ Checked Exception → COMMIT (không rollback)!
public void sendInvoice(Long orderId) throws MessagingException {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.setStatus("INVOICED");
    orderRepository.save(order);  // ← Đã save

    emailService.send(order);     // ← throw MessagingException (checked)
    // → Status "INVOICED" vẫn COMMIT dù email gửi thất bại!
}

// ✅ FIX: Khai báo rollbackFor
@Transactional(rollbackFor = MessagingException.class)
public void sendInvoice(Long orderId) throws MessagingException {
    // Bây giờ MessagingException sẽ gây rollback
}
```

### Lỗi #4: Catch exception rồi nuốt (swallow)

```java
@Transactional
public void createOrder(OrderRequest req) {
    Order order = orderRepository.save(buildOrder(req));
    try {
        productService.reduceStock(req.getProductId(), req.getQuantity());
    } catch (RuntimeException e) {
        log.error("Lỗi giảm stock: {}", e.getMessage());
        // ❌ NUỐT EXCEPTION! → Transaction COMMIT bình thường
        // → Order được tạo NHƯNG stock KHÔNG bị giảm → DATA INCONSISTENT!
    }
}

// ✅ FIX: Re-throw exception hoặc đừng catch
@Transactional
public void createOrder(OrderRequest req) {
    Order order = orderRepository.save(buildOrder(req));
    try {
        productService.reduceStock(req.getProductId(), req.getQuantity());
    } catch (RuntimeException e) {
        log.error("Lỗi giảm stock: {}", e.getMessage());
        throw e;  // ← RE-THROW để transaction rollback!
    }
}
```

### Lỗi #5: `@Transactional` trên class KHÔNG phải Spring Bean

```java
// ❌ KHÔNG CÓ @Service / @Component → Spring không quản lý
// → @Transactional bị bỏ qua hoàn toàn
public class OrderHelper {

    @Transactional
    public void processOrder(OrderRequest req) {
        // Không có transaction vì class này không phải Spring Bean!
    }
}
```

> **Fix**: Thêm `@Service` hoặc `@Component` để Spring quản lý lifecycle.

### Tổng hợp 5 lỗi

| # | Lỗi | Triệu chứng | Fix |
|:---:|:---|:---|:---|
| 1 | Self-Invocation | `@Transactional` bị ignore khi gọi từ cùng class | Tách sang class khác |
| 2 | Private method | `@Transactional` trên `private` bị ignore | Đổi sang `public` |
| 3 | Checked Exception | Không rollback cho checked exception | `rollbackFor = Exception.class` |
| 4 | Swallow Exception | Catch exception rồi không re-throw | Re-throw hoặc không catch |
| 5 | Non-Spring Bean | Class không có `@Service`/`@Component` | Thêm annotation để Spring quản lý |

---

## PHẦN 10: 🌐 DISTRIBUTED TRANSACTION — GIỚI HẠN CỦA `@TRANSACTIONAL`

### 10.1. Vấn đề: @Transactional chỉ kiểm soát 1 database

```java
@Transactional
public Order createOrderWithExternalPayment(OrderRequest request) {
    // 1. Lưu Order vào DB ← @Transactional kiểm soát được ✅
    Order order = orderRepository.save(buildOrder(request));

    // 2. Gọi Payment Gateway (Stripe, VNPay) ← NGOÀI tầm kiểm soát ❌
    PaymentResult result = paymentGatewayClient.charge(
        request.getAmount(), request.getCardToken()
    );
    // → Tiền ĐÃ bị trừ trên hệ thống Stripe!

    // 3. Cập nhật trạng thái ← @Transactional kiểm soát được ✅
    order.setPaymentId(result.getTransactionId());
    order.setStatus("PAID");

    // 💥 Nếu exception xảy ra ở đây (ví dụ DB constraint violation):
    //    → DB ROLLBACK: Order bị xóa ✅
    //    → Stripe: Tiền ĐÃ BỊ TRỪ và KHÔNG TỰ ROLLBACK ❌
    //    → Khách hàng mất tiền mà không nhận được hàng! 💀
    inventoryService.reduceStock(request.getProductId(), request.getQuantity());

    return order;
}
```

### 10.2. Sơ đồ vấn đề Distributed Transaction

```mermaid
graph TB
    subgraph local["🔒 @Transactional kiểm soát được"]
        DB1["💾 Database<br/>(Order, Product, User)"]
    end

    subgraph external["⚠️ NGOÀI tầm kiểm soát"]
        PAY["💳 Payment Gateway<br/>(Stripe, VNPay)"]
        EMAIL["📧 Email Service<br/>(SendGrid, SES)"]
        SMS["📱 SMS Gateway<br/>(Twilio)"]
        SEARCH["🔍 Elasticsearch<br/>(Search Index)"]
    end

    Service["OrderService<br/>@Transactional"] --> DB1
    Service --> PAY
    Service --> EMAIL
    Service --> SMS

    DB1 -->|"ROLLBACK ✅"| Service
    PAY -->|"KHÔNG rollback ❌"| Service
    EMAIL -->|"KHÔNG rollback ❌"| Service

    style local fill:#e8f5e9
    style external fill:#ffebee
```

### 10.3. Giải pháp: Đặt External Call SAU cùng

```java
@Transactional
public Order createOrderSafer(OrderRequest request) {
    // 1. Validate tất cả business rules TRƯỚC
    validateRequest(request);
    Product product = findAndValidateProduct(request.getProductId());
    validateStock(product, request.getQuantity());

    // 2. Thực hiện TẤT CẢ thao tác DB trước
    Order order = orderRepository.save(buildOrder(request));
    productRepository.reduceStockAtomic(product.getId(), request.getQuantity());

    // 3. Gọi external service SAU CÙNG (giảm thiểu cửa sổ rủi ro)
    // Nếu bước 2 fail → rollback TRƯỚC KHI gọi external
    PaymentResult result = paymentGatewayClient.charge(request.getAmount());

    // 4. Cập nhật kết quả từ external
    order.setPaymentId(result.getTransactionId());
    order.setStatus("PAID");

    return order;
    // ⚠️ Vẫn chưa 100% an toàn: nếu COMMIT thất bại sau khi charge payment
    // → Cần giải pháp nâng cao hơn
}
```

### 10.4. Giải pháp nâng cao (Preview — sẽ học kỹ ở Giai đoạn 8)

```mermaid
graph TD
    subgraph saga["🔄 Saga Pattern"]
        S1["1. Create Order<br/>(local TX)"]
        S2["2. Reserve Stock<br/>(local TX)"]
        S3["3. Charge Payment<br/>(external call)"]
        S4["4. Confirm Order<br/>(local TX)"]

        S1 -->|success| S2
        S2 -->|success| S3
        S3 -->|success| S4

        S3 -->|"failure"| C2["Compensate:<br/>Release Stock"]
        C2 --> C1["Compensate:<br/>Cancel Order"]
    end

    subgraph outbox["📤 Transactional Outbox Pattern"]
        O1["1. Save Order<br/>+ Save Event<br/>(CÙNG 1 TX)"]
        O2["2. Background Worker<br/>đọc Event"]
        O3["3. Gọi Payment<br/>+ Gửi Email"]
        O4["4. Đánh dấu<br/>Event đã xử lý"]

        O1 --> O2
        O2 --> O3
        O3 --> O4
    end

    style saga fill:#e3f2fd
    style outbox fill:#fce4ec
```

| Giải pháp | Mô tả | Độ phức tạp |
|:---|:---|:---:|
| **Đặt external call sau cùng** | Giảm cửa sổ rủi ro (chưa 100% an toàn) | ⭐ |
| **Idempotency Key** | Đảm bảo retry an toàn (gọi lại không tạo side effect mới) | ⭐⭐ |
| **Transactional Outbox** | Ghi event vào DB cùng TX → worker riêng xử lý | ⭐⭐⭐ |
| **Saga Pattern** | Chuỗi local TX + compensating actions | ⭐⭐⭐⭐ |
| **2PC (Two-Phase Commit)** | Coordinator điều phối nhiều database | ⭐⭐⭐⭐⭐ |

> [!NOTE]
> **Trong dự án học tập hiện tại**, chúng ta chỉ sử dụng 1 database (H2) → `@Transactional` đã đủ mạnh. Các giải pháp nâng cao như Saga, Outbox sẽ được tìm hiểu khi học Microservices (Giai đoạn 8).

---

## PHẦN 11: 💻 THỰC HÀNH — TRIỂN KHAI ORDER SERVICE VỚI TRANSACTION

### 11.1. Kịch bản nghiệp vụ

```
┌──────────────────────────────────────────────────────────────────┐
│  📦 Quy trình đặt hàng (Create Order):                          │
│                                                                  │
│  1. Validate: User tồn tại? Product tồn tại? Stock đủ?         │
│  2. Tạo Order entity (status = "PENDING")                        │
│  3. Giảm stock Product (atomic update)                           │
│  4. Cập nhật status Order → "CONFIRMED"                          │
│                                                                  │
│  → Tất cả trong 1 Transaction:                                   │
│    ✅ Thành công → COMMIT tất cả                                 │
│    ❌ Bất kỳ bước nào fail → ROLLBACK tất cả                    │
│    → Đảm bảo: KHÔNG BAO GIỜ có Order mà stock không giảm       │
│    → Đảm bảo: KHÔNG BAO GIỜ có stock giảm mà không có Order    │
└──────────────────────────────────────────────────────────────────┘
```

### 11.2. OrderCreateRequest DTO

```java
package com.example.springbootlearning.dto.request;

import jakarta.validation.constraints.*;

/**
 * 📘 BÀI 12 — Request DTO cho tạo đơn hàng
 */
public record OrderCreateRequest(
        @NotNull(message = "userId không được null")
        Long userId,

        @NotNull(message = "productId không được null")
        Long productId,

        @NotNull(message = "quantity không được null")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        @Max(value = 100, message = "Số lượng tối đa là 100")
        Integer quantity,

        @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
        String description
) {}
```

### 11.3. OrderService — Trái tim của Transaction

```java
package com.example.springbootlearning.service;

import com.example.springbootlearning.dto.request.OrderCreateRequest;
import com.example.springbootlearning.exception.ResourceNotFoundException;
import com.example.springbootlearning.model.Order;
import com.example.springbootlearning.model.Product;
import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.OrderRepository;
import com.example.springbootlearning.repository.ProductRepository;
import com.example.springbootlearning.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 📘 BÀI 12 — OrderService (Transaction Management)
 *
 * Minh họa:
 *   ✅ @Transactional đảm bảo Atomicity: tạo Order + giảm Stock cùng thành công/thất bại
 *   ✅ @Transactional(readOnly = true) cho các thao tác đọc
 *   ✅ Rollback tự động khi RuntimeException (ResourceNotFoundException, IllegalStateException)
 *   ✅ Atomic SQL update tránh race condition khi giảm stock
 */
@Service
@Transactional(readOnly = true) // Mặc định: tất cả method là read-only
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ================================================================
    // WRITE — Tạo đơn hàng (Transaction đảm bảo Atomicity)
    // ================================================================

    @Transactional // Override readOnly → readOnly = false cho WRITE
    public Order createOrder(OrderCreateRequest request) {
        log.info("📥 Bắt đầu tạo đơn hàng: userId={}, productId={}, qty={}",
                request.userId(), request.productId(), request.quantity());

        // ── Bước 1: Validate User tồn tại ──
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("⚠️ User không tồn tại: id={}", request.userId());
                    return new ResourceNotFoundException("User", "id", request.userId());
                    // → RuntimeException → ROLLBACK (dù chưa có gì để rollback)
                });

        // ── Bước 2: Validate Product tồn tại ──
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> {
                    log.warn("⚠️ Product không tồn tại: id={}", request.productId());
                    return new ResourceNotFoundException("Product", "id", request.productId());
                });

        // ── Bước 3: Validate stock đủ ──
        if (product.getStock() < request.quantity()) {
            log.warn("⚠️ Không đủ hàng: stock={}, requested={}",
                    product.getStock(), request.quantity());
            throw new IllegalStateException(
                    String.format("Không đủ hàng trong kho. Tồn kho: %d, Yêu cầu: %d",
                            product.getStock(), request.quantity())
            );
            // → RuntimeException → ROLLBACK
        }

        // ── Bước 4: Tạo Order ──
        double totalAmount = product.getPrice() * request.quantity();
        Order order = new Order(
                request.description() != null ? request.description()
                        : "Đơn hàng " + product.getName() + " x" + request.quantity(),
                totalAmount,
                "CONFIRMED",
                user
        );
        order = orderRepository.save(order);
        log.info("✅ Order đã lưu: id={}, totalAmount={}", order.getId(), totalAmount);

        // ── Bước 5: Giảm stock (Atomic SQL Update) ──
        int updatedRows = productRepository.reduceStockAtomic(
                product.getId(), request.quantity()
        );
        if (updatedRows == 0) {
            // Race condition: ai đó đã mua hết stock trước chúng ta
            log.error("💥 Race condition! Stock đã hết khi cố giảm: productId={}",
                    product.getId());
            throw new IllegalStateException(
                    "Sản phẩm đã hết hàng (race condition detected)"
            );
            // → RuntimeException → ROLLBACK cả Order ở bước 4!
            // → Đây chính là sức mạnh của ATOMICITY
        }
        log.info("✅ Stock đã giảm: productId={}, quantity=-{}",
                product.getId(), request.quantity());

        log.info("🎉 Đơn hàng tạo thành công: orderId={}, userId={}, productId={}",
                order.getId(), user.getId(), product.getId());
        return order;
    }

    // ================================================================
    // READ — Các phương thức đọc (kế thừa readOnly = true từ class)
    // ================================================================

    public java.util.List<Order> getOrdersByUserId(Long userId) {
        log.debug("Lấy danh sách orders của userId={}", userId);
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    public java.util.List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusIgnoreCase(status);
    }

    // ================================================================
    // WRITE — Hủy đơn hàng (Rollback stock)
    // ================================================================

    @Transactional
    public Order cancelOrder(Long orderId) {
        log.info("📥 Hủy đơn hàng: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalStateException("Đơn hàng đã bị hủy trước đó");
        }

        // Hoàn trả stock (logic đơn giản cho learning)
        // Trong production: cần parse quantity từ order items
        order.setStatus("CANCELLED");
        orderRepository.save(order);

        log.info("✅ Đơn hàng đã hủy: orderId={}", orderId);
        return order;
    }
}
```

### 11.4. Luồng chạy Transaction của createOrder()

```mermaid
sequenceDiagram
    participant Client
    participant Proxy as 🔮 CGLIB Proxy
    participant OS as OrderService
    participant UR as UserRepository
    participant PR as ProductRepository
    participant OR as OrderRepository
    participant DB as H2 Database

    Client->>Proxy: POST /api/orders
    Proxy->>DB: BEGIN TRANSACTION

    Proxy->>OS: createOrder(request)

    OS->>UR: findById(userId)
    UR->>DB: SELECT * FROM users WHERE id=?
    DB-->>UR: User ✅

    OS->>PR: findById(productId)
    PR->>DB: SELECT * FROM products WHERE id=?
    DB-->>PR: Product (stock=25) ✅

    Note over OS: Validate: stock(25) >= qty(3) ✅

    OS->>OR: save(order)
    OR->>DB: INSERT INTO orders (...) VALUES (...)
    DB-->>OR: order.id = 5

    OS->>PR: reduceStockAtomic(productId, 3)
    PR->>DB: UPDATE products SET stock=stock-3<br/>WHERE id=? AND stock>=3
    DB-->>PR: updatedRows = 1 ✅

    OS-->>Proxy: return order

    Proxy->>DB: COMMIT ✅
    Note over DB: Order created + Stock reduced<br/>ATOMICALLY!

    Proxy-->>Client: 200 OK + Order response
```

### 11.5. Kịch bản ROLLBACK

```mermaid
sequenceDiagram
    participant Client
    participant Proxy as 🔮 CGLIB Proxy
    participant OS as OrderService
    participant OR as OrderRepository
    participant PR as ProductRepository
    participant DB as H2 Database

    Client->>Proxy: POST /api/orders (qty=30, stock=25)
    Proxy->>DB: BEGIN TRANSACTION

    OS->>OR: save(order)
    OR->>DB: INSERT INTO orders (...) VALUES (...)
    DB-->>OR: order.id = 6

    OS->>PR: reduceStockAtomic(productId, 30)
    PR->>DB: UPDATE products SET stock=stock-30<br/>WHERE id=? AND stock>=30
    DB-->>PR: updatedRows = 0 ❌ (stock=25 < 30)

    Note over OS: throw IllegalStateException<br/>"Sản phẩm đã hết hàng"

    OS-->>Proxy: 💥 RuntimeException!

    Proxy->>DB: ROLLBACK ❌
    Note over DB: Order INSERT bị hủy!<br/>Stock KHÔNG bị giảm!<br/>Dữ liệu NGUYÊN VẸN ✅

    Proxy-->>Client: 500 Error
```

### 11.6. OrderController

```java
package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.request.OrderCreateRequest;
import com.example.springbootlearning.dto.response.ApiResponse;
import com.example.springbootlearning.model.Order;
import com.example.springbootlearning.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 📘 BÀI 12 — Order REST Controller (Transaction Demo)
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Order API", description = "Quản lý đơn hàng (Transaction Management - Bài 12)")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Tạo đơn hàng mới (Transaction: Order + Stock giảm)")
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {
        log.info("📥 POST /api/v1/orders — userId={}, productId={}, qty={}",
                request.userId(), request.productId(), request.quantity());
        Order order = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đơn hàng đã tạo thành công", order));
    }

    @Operation(summary = "Lấy danh sách đơn hàng theo User ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByUser(
            @PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy đơn hàng thành công", orders));
    }

    @Operation(summary = "Lấy đơn hàng theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy đơn hàng thành công", order));
    }

    @Operation(summary = "Hủy đơn hàng")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(@PathVariable Long id) {
        log.info("📥 PATCH /api/v1/orders/{}/cancel", id);
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("Đơn hàng đã hủy thành công", order));
    }
}
```

---

## PHẦN 12: 🎯 BEST PRACTICES & ANTI-PATTERNS

### ✅ Best Practices

| # | Practice | Giải thích |
|:---:|:---|:---|
| 1 | **Đặt `@Transactional` ở Service layer** (KHÔNG ở Controller/Repository) | Service = nơi chứa business logic = nơi xác định ranh giới transaction |
| 2 | **`readOnly = true` cho mọi method đọc** | Hibernate skip dirty checking → tiết kiệm RAM & CPU |
| 3 | **Dùng `rollbackFor = Exception.class`** cho nghiệp vụ quan trọng | Tránh bỏ sót checked exception |
| 4 | **Giữ transaction ngắn nhất có thể** | Transaction dài = giữ lock lâu = bottleneck |
| 5 | **Không gọi external API trong transaction** | External call chậm + không rollback được |
| 6 | **Dùng Atomic SQL thay vì read-modify-write** | `UPDATE ... SET stock = stock - :qty WHERE stock >= :qty` thay vì load → setStock → save |
| 7 | **`@Transactional(readOnly = true)` ở class level** + override cho write | Pattern sạch, mặc định an toàn |
| 8 | **Log trước khi throw exception** | Dễ debug khi rollback xảy ra |

### ❌ Anti-Patterns

| # | Anti-Pattern | Vấn đề |
|:---:|:---|:---|
| 1 | `@Transactional` trên Controller | Controller không phải nơi quản lý business transaction |
| 2 | Transaction quá lớn (God Transaction) | Giữ lock lâu, ảnh hưởng throughput |
| 3 | Gọi REST API/gửi email TRONG transaction | External call KHÔNG rollback được |
| 4 | Catch exception rồi nuốt (swallow) | Transaction KHÔNG rollback → data inconsistent |
| 5 | Self-invocation (`this.method()`) | Bypass proxy → `@Transactional` bị ignore |
| 6 | `@Transactional` trên `private` method | CGLIB proxy không override được |

---

## PHẦN 13: 💼 CÂU HỎI PHỎNG VẤN (INTERVIEW QUESTIONS)

### Câu 1: ACID là gì? Giải thích từng tính chất với ví dụ cụ thể.
**Trả lời:**
ACID là 4 tính chất đảm bảo tính toàn vẹn của database transaction:
- **Atomicity**: Tất cả thao tác trong transaction cùng thành công hoặc cùng thất bại. VD: Chuyển tiền A→B: trừ A + cộng B phải xảy ra đồng thời.
- **Consistency**: Dữ liệu chuyển từ trạng thái hợp lệ này sang trạng thái hợp lệ khác. VD: Tổng tiền trước và sau chuyển khoản phải bằng nhau.
- **Isolation**: Transaction đang chạy không nhìn thấy thay đổi chưa commit của transaction khác. VD: 2 người cùng mua sản phẩm cuối → chỉ 1 người thành công.
- **Durability**: Dữ liệu đã commit sẽ tồn tại vĩnh viễn, kể cả server crash. Database đảm bảo bằng WAL (Write-Ahead Logging).

### Câu 2: `@Transactional` hoạt động như thế nào bên dưới? Tại sao self-invocation không hoạt động?
**Trả lời:**
Spring sử dụng **AOP Proxy** (CGLIB hoặc JDK Dynamic Proxy) để bọc quanh bean. Khi method được gọi từ bên ngoài, request đi qua Proxy → Proxy bắt đầu transaction → gọi method thật → commit/rollback. Khi self-invocation (`this.method()`), gọi trực tiếp trên bean thật mà không qua Proxy → `@Transactional` bị bỏ qua. Giải pháp: tách method sang class khác, hoặc inject self (`@Lazy @Autowired private MyService self`).

### Câu 3: `@Transactional(readOnly = true)` làm gì? Có thực sự chỉ đọc không?
**Trả lời:**
`readOnly = true` gợi ý (hint) cho Hibernate và JDBC driver:
1. **Hibernate**: Tắt dirty checking (không tạo snapshot) → tiết kiệm RAM. Đặt flush mode = MANUAL → giảm roundtrip DB.
2. **JDBC**: `connection.setReadOnly(true)` → database có thể route query tới read replica.
Tuy nhiên, nó KHÔNG ngăn cản ghi. Nếu gọi `save()` trong readOnly method, Hibernate vẫn cho phép nhưng thay đổi sẽ bị bỏ qua khi flush (vì flush mode = MANUAL). Một số database (PostgreSQL) sẽ ném exception nếu có INSERT/UPDATE trong read-only transaction.

### Câu 4: Phân biệt Propagation REQUIRED vs REQUIRES_NEW. Khi nào dùng REQUIRES_NEW?
**Trả lời:**
- **REQUIRED** (mặc định): Nếu đã có transaction → tham gia. Chưa có → tạo mới. Cả method A và B dùng chung 1 transaction → rollback 1 là rollback cả 2.
- **REQUIRES_NEW**: Luôn tạo transaction MỚI, tạm dừng (suspend) transaction cũ. 2 transaction hoàn toàn độc lập.
Dùng `REQUIRES_NEW` khi muốn method B persist dữ liệu ĐỘC LẬP, không bị ảnh hưởng nếu method A rollback. VD: Ghi audit log (phải lưu được dù business logic fail), ghi lỗi vào bảng error_log.

### Câu 5: Tại sao checked exception không rollback theo mặc định? Thiết kế này hợp lý hay bất hợp lý?
**Trả lời:**
Spring tuân theo quy ước của **EJB (Enterprise JavaBeans)**: checked exception thường đại diện cho **business error** có thể phục hồi (recoverable) → commit là hợp lý. Unchecked exception (RuntimeException) đại diện cho **programming error** không thể phục hồi → rollback.
Tuy nhiên, trong thực tế hiện đại, ranh giới này không rõ ràng. Best practice: dùng `rollbackFor = Exception.class` cho các method quan trọng, hoặc chỉ dùng unchecked exception trong toàn bộ ứng dụng (như dự án của chúng ta với `BusinessException extends RuntimeException`).

### Câu 6: Giải thích Dirty Read, Non-Repeatable Read, Phantom Read.
**Trả lời:**
- **Dirty Read**: TX-A đọc dữ liệu mà TX-B chưa commit. Nếu TX-B rollback → TX-A đã đọc "dữ liệu bẩn" không tồn tại.
- **Non-Repeatable Read**: TX-A đọc cùng 1 row 2 lần, nhưng giữa 2 lần đọc, TX-B đã UPDATE row đó và commit → TX-A đọc ra giá trị khác lần 1.
- **Phantom Read**: TX-A chạy cùng 1 query 2 lần, nhưng giữa 2 lần, TX-B đã INSERT/DELETE rows mới → TX-A nhận được số lượng rows khác lần 1 (rows "ma" xuất hiện/biến mất).

### Câu 7: READ_COMMITTED vs REPEATABLE_READ — khi nào dùng cái nào?
**Trả lời:**
- **READ_COMMITTED** (PostgreSQL default): Chỉ đọc dữ liệu đã commit. Mỗi câu SELECT thấy snapshot mới nhất tại thời điểm đó. Đủ tốt cho 90% ứng dụng.
- **REPEATABLE_READ** (MySQL InnoDB default): Đọc snapshot cố định từ lúc bắt đầu transaction. Mọi SELECT trong cùng TX luôn thấy cùng dữ liệu. Dùng khi cần đọc nhất quán (report generation, tính toán phụ thuộc vào nhiều query).
Trade-off: REPEATABLE_READ an toàn hơn nhưng giữ lock lâu hơn, giảm throughput.

### Câu 8: `@Transactional` có hoạt động trên private method không? Tại sao?
**Trả lời:**
**KHÔNG.** Spring sử dụng CGLIB Proxy để tạo subclass. CGLIB proxy override method bằng cách tạo subclass (extends). Theo quy tắc Java, `private` method không thể bị override → proxy không thể thêm logic transaction vào. `@Transactional` trên `private` method sẽ bị **im lặng bỏ qua** (không có warning hay error). Fix: đổi sang `public` hoặc `protected`.

### Câu 9: Có thể dùng `@Transactional` để rollback external API call (Stripe, SMS) không?
**Trả lời:**
**KHÔNG.** `@Transactional` chỉ kiểm soát database transaction (qua JDBC connection). External API call (HTTP request, message queue, email) hoàn toàn nằm ngoài tầm kiểm soát. Nếu database rollback, external service đã thực thi rồi (tiền đã trừ, SMS đã gửi).
Giải pháp: (1) Đặt external call sau cùng trong transaction để giảm cửa sổ rủi ro. (2) Idempotency key: đảm bảo retry an toàn. (3) Saga Pattern: chuỗi local transaction + compensating action. (4) Transactional Outbox: ghi event vào DB cùng TX, worker riêng xử lý bất đồng bộ.

### Câu 10: Khi nào KHÔNG nên dùng `@Transactional`?
**Trả lời:**
1. **Method chỉ đọc 1 câu query đơn giản**: Spring Data JPA method `findById()` đã chạy trong auto-commit transaction. Thêm `@Transactional(readOnly = true)` vẫn OK cho optimization nhưng không bắt buộc.
2. **Khi gọi external API/service dài**: Transaction giữ database connection → connection pool exhaustion nếu external call chậm.
3. **Batch processing lớn**: Transaction quá lớn giữ lock và RAM → chia nhỏ thành nhiều transaction.

### Câu 11: `@Transactional` ở class level vs method level — cái nào ưu tiên?
**Trả lời:**
**Method level ưu tiên hơn class level.** Đây là quy tắc "most specific wins" của Spring. Pattern phổ biến: đặt `@Transactional(readOnly = true)` ở class level (mặc định read), rồi override bằng `@Transactional` ở method level cho các write method.

### Câu 12: Timeout trong `@Transactional(timeout = 30)` có ý nghĩa gì?
**Trả lời:**
`timeout` (giây) đặt giới hạn thời gian cho transaction. Nếu transaction chưa hoàn thành sau timeout → tự động rollback + ném `TransactionTimedOutException`. Mặc định = -1 (không timeout, phụ thuộc database). Dùng để ngăn transaction bị treo do query chậm, deadlock, hoặc external call timeout.

### Câu 13: Explain Optimistic Locking vs Pessimistic Locking.
**Trả lời:**
- **Optimistic Locking** (`@Version`): Không khóa row khi đọc. Khi commit, kiểm tra version number. Nếu version đã thay đổi → `OptimisticLockException` → retry. Phù hợp: ít conflict (read-heavy workload).
- **Pessimistic Locking** (`@Lock(PESSIMISTIC_WRITE)`): Khóa row ngay khi đọc (`SELECT ... FOR UPDATE`). Transaction khác phải chờ. Phù hợp: nhiều conflict (write-heavy workload), critical sections.

### Câu 14: Tại sao nên dùng Atomic SQL (`UPDATE ... SET stock = stock - :qty WHERE stock >= :qty`) thay vì read-modify-write?
**Trả lời:**
Pattern read-modify-write (load → setStock → save) có **race condition**: 2 transaction đọc stock = 25 cùng lúc → cả 2 setStock(22) → chỉ giảm 3 thay vì 6. Atomic SQL thực hiện đọc + ghi trong **1 câu SQL duy nhất** → database đảm bảo serialization ở row level. Thêm `WHERE stock >= :qty` ngăn stock âm. Kiểm tra `updatedRows == 0` để phát hiện conflict.

### Câu 15: Giải thích Transactional Outbox Pattern.
**Trả lời:**
Outbox Pattern giải quyết vấn đề "dual write" (ghi database + gửi event/message phải đồng bộ):
1. Trong CÙNG 1 database transaction: save business data + save event vào bảng `outbox_events`.
2. Background worker (CDC hoặc polling) đọc bảng `outbox_events`.
3. Worker gửi event tới message broker (Kafka, RabbitMQ) hoặc gọi external API.
4. Sau khi gửi thành công → đánh dấu event đã xử lý.
Ưu điểm: đảm bảo at-least-once delivery, không bao giờ mất event (vì event nằm trong cùng TX với business data). Nhược điểm: tăng độ trễ (latency), cần worker bổ sung.

---

## PHẦN 14: 📚 KIẾN THỨC BỔ SUNG: JAVA CORE & DESIGN PATTERNS

### 14.1. Proxy Pattern (Java Core & Design Pattern)

```mermaid
classDiagram
    class Subject {
        <<interface>>
        +doWork() void
    }
    class RealSubject {
        +doWork() void
    }
    class ProxySubject {
        -realSubject: RealSubject
        +doWork() void
    }

    Subject <|.. RealSubject
    Subject <|.. ProxySubject
    ProxySubject --> RealSubject : delegates to

    note for ProxySubject "Spring @Transactional sử dụng<br/>CGLIB Proxy (subclass-based)<br/>để thêm logic BEGIN/COMMIT/ROLLBACK<br/>trước và sau method thật"
```

**Proxy Pattern** là 1 trong 23 GoF Design Patterns:
- **Intent**: Cung cấp đại diện (surrogate) cho object khác để kiểm soát truy cập.
- **Trong Spring**: CGLIB Proxy extends class gốc, override public method, thêm AOP logic (transaction, caching, security).
- **Java JDK Dynamic Proxy**: Chỉ áp dụng cho interface (`InvocationHandler`). Spring Data JPA repository sử dụng cơ chế này.

### 14.2. Strategy Pattern (PlatformTransactionManager)

Spring `PlatformTransactionManager` là ví dụ điển hình của **Strategy Pattern**:
- **Context**: `TransactionInterceptor` (AOP advice)
- **Strategy interface**: `PlatformTransactionManager`
- **Concrete strategies**: `JpaTransactionManager`, `DataSourceTransactionManager`, `JtaTransactionManager`

→ Bạn có thể swap transaction manager mà không thay đổi business logic.

### 14.3. Template Method Pattern (TransactionTemplate)

Ngoài `@Transactional` (declarative), Spring còn hỗ trợ **programmatic transaction** qua `TransactionTemplate`:

```java
@Service
public class OrderService {

    private final TransactionTemplate transactionTemplate;

    public OrderService(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    public Order createOrderProgrammatic(OrderCreateRequest request) {
        return transactionTemplate.execute(status -> {
            // Tất cả code ở đây nằm trong transaction
            Order order = orderRepository.save(buildOrder(request));
            productRepository.reduceStockAtomic(request.productId(), request.quantity());
            return order;
            // Nếu exception → tự rollback
            // Có thể gọi status.setRollbackOnly() để force rollback
        });
    }
}
```

| Tiêu chí | Declarative (`@Transactional`) | Programmatic (`TransactionTemplate`) |
|:---|:---:|:---:|
| Cách dùng | Annotation trên method | Code Java trực tiếp |
| Linh hoạt | Thấp (cấu hình qua annotation) | Cao (có thể if/else trong TX) |
| Dễ đọc | ✅ Rất dễ | ⚠️ Verbose hơn |
| Self-invocation | ❌ Bị bypass | ✅ Hoạt động bình thường |
| Use case | 95% trường hợp | Cần kiểm soát chi tiết transaction boundary |

### 14.4. Exception Hierarchy trong Java

```
java.lang.Throwable
├── java.lang.Error                    ← JVM error, không bắt được
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── ...
└── java.lang.Exception                ← Application error
    ├── IOException                    ← Checked (phải try-catch hoặc throws)
    ├── SQLException                   ← Checked
    ├── ParseException                 ← Checked
    └── java.lang.RuntimeException     ← Unchecked (không bắt buộc try-catch)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IllegalStateException
        └── com.example...BusinessException  ← Custom exception trong dự án
            ├── ResourceNotFoundException
            └── DuplicateResourceException
```

> **Quy ước trong dự án**: Tất cả custom exception kế thừa `RuntimeException` → tự động rollback `@Transactional`.

---

## PHẦN 15: 🔗 TÀI LIỆU LIÊN KẾT

### File liên quan trong dự án

- [Order.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/model/Order.java) — Order Entity
- [Product.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/model/Product.java) — Product Entity (BaseAuditableEntity)
- [User.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/model/User.java) — User Entity
- [OrderRepository.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/repository/OrderRepository.java) — Order Repository
- [ProductRepository.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/repository/ProductRepository.java) — Product Repository (reduceStockAtomic)
- [ProductService.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/service/ProductService.java) — ProductService (pattern @Transactional)
- [ResourceNotFoundException.java](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/java/com/example/springbootlearning/exception/ResourceNotFoundException.java) — Custom exception (extends RuntimeException)
- [application.properties](file:///Users/vovantu/HTML_CSS/JAVA%20/springboot-learning/src/main/resources/application.properties) — JPA + H2 configuration

### Bài giảng liên quan

- [Bài 9: Spring Data JPA & Hibernate ORM](file:///Users/vovantu/HTML_CSS/JAVA%20/Note/Lesson09/bai-9-spring-data-jpa-hibernate-orm.md) — ORM, Entity, Persistence Context
- [Bài 10: Entity Relationships & Query Methods](file:///Users/vovantu/HTML_CSS/JAVA%20/Note/Lesson10) — @ManyToOne, @OneToMany, N+1 Query
- [Bài 11: Pagination, Sorting & Specifications](file:///Users/vovantu/HTML_CSS/JAVA%20/Note/Lesson11/bai-11-pagination-sorting-specifications.md) — Pageable, Specification Pattern
- [Implementation Plan](file:///Users/vovantu/.gemini/antigravity-ide/brain/efda256c-57a1-492b-a7c0-e53fec100685/implementation_plan.md) — Lộ trình tổng thể
