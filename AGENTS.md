# Project Instructions & Agent Guidelines

- **Liên kết file:** Luôn sử dụng link markdown có thể nhấp được (`[filename](file:///path/to/file)`) khi đề cập tới các file trong dự án.
- **Tạo bài giảng & Chuyên đề:** Khi được yêu cầu tạo bài giảng hoặc chuyên đề, thực hiện theo quy trình 2 bước sau:
  1. **Tạo Artifact:** Trình bày bài giảng dưới dạng artifact (khác tên với các bài giảng đã có và khác tên với implementation plan) lưu vào thư mục artifacts nội bộ của phiên làm việc (`<appDataDir>/brain/<conversation-id>/...`).
  2. **Lưu trữ vào dự án:** Đồng thời lưu một bản sao file bài giảng vào thư mục con tương ứng bên trong thư mục [Note](file:///Users/vovantu/HTML_CSS/JAVA%20/Note) (ví dụ: `Note/Lesson01/`, `Note/Lesson02/`, `Note/ChuyenDe/`,...). Lưu ý: Không truyền `ArtifactMetadata` khi ghi file trực tiếp vào thư mục workspace bên ngoài để tránh lỗi đường dẫn của Antigravity.

- **Tiêu chuẩn chất lượng nội dung & hình thức bài giảng:**
  - **Định nghĩa chuẩn xác & Tóm tắt cốt lõi (Bắt buộc):** Mỗi bài giảng/chuyên đề phải có phần định nghĩa rõ ràng, tóm tắt bản chất của các đối tượng, khái niệm hoặc mục tiêu kỹ thuật chính ngay từ đầu để người đọc nhanh chóng nắm bắt bức tranh toàn cảnh trước khi đi sâu vào chi tiết.
  - **Văn phong & Diễn đạt khoa học:** Sử dụng cấu trúc câu mạch lạc, từ ngữ khoa học, chuẩn xác nhưng tự nhiên, dễ hiểu; tránh câu dài dòng phức tạp. Kết hợp ẩn dụ thực tế để làm sáng tỏ các khái niệm trừu tượng.
  - **Code ví dụ cần chi tiết, đầy đủ:** 
  - **Trực quan hóa đa phương tiện:** Tích cực sử dụng icon trực quan, bảng so sánh/tổng hợp (tables), sơ đồ Mermaid (luồng dữ liệu, vòng đời, kiến trúc) đảm bảo đúng cú pháp không gây lỗi render.
  - **Mở rộng chuyên sâu:** Bổ sung kiến thức Java Core, Design Pattern liên quan và Bộ câu hỏi phỏng vấn (Interview Questions) có đáp án chuẩn ở cuối bài giảng.
  - Không cần viết test vào thư mục `springboot-learning/src/test`. 
