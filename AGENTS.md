# Project Instructions & Agent Guidelines

- **Liên kết file:** Luôn sử dụng link markdown có thể nhấp được (`[filename](file:///path/to/file)`) khi đề cập tới các file trong dự án.
- **Tạo bài giảng:** Khi được yêu cầu tạo bài giảng, thực hiện theo quy trình 2 bước sau:
  1. **Tạo Artifact:** Trình bày bài giảng dưới dạng artifact (khác tên với các bài giảng đã có và khác tên với implementation plan) lưu vào thư mục artifacts nội bộ của phiên làm việc (`<appDataDir>/brain/<conversation-id>/...`).
  2. **Lưu trữ vào dự án:** Đồng thời lưu một bản sao file bài giảng vào thư mục con tương ứng bên trong thư mục [Note](file:///Users/vovantu/HTML_CSS/JAVA%20/Note) (ví dụ: `Note/Lesson01/`, `Note/Lesson02/`,...). Lưu ý: Không truyền `ArtifactMetadata` khi ghi file trực tiếp vào thư mục workspace bên ngoài để tránh lỗi đường dẫn của Antigravity.
  - **Yêu cầu nội dung & hình thức bài giảng:** Trình bày rõ ràng, chi tiết và khoa học (kết hợp chặt chẽ giữa lý thuyết chuyên sâu và code minh hoạ thực tế), không chỉ thuần văn bản (text). Tích cực sử dụng icon trực quan, bảng so sánh/tổng hợp (tables), sơ đồ Mermaid và biểu đồ trực quan hóa luồng dữ liệu/kiến trúc khi cần thiết để bài học sinh động và dễ hiểu. Đồng thời bổ sung kiến thức về Java core, pattern hay và các câu hỏi phỏng vấn (Interview Questions) ở cuối bài giảng. Không cần viết test vào thư mục springboot-learning/src/test.
