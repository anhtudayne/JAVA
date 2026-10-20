package com.example.springbootlearning.service;

import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 📘 BÀI 14 — Unit Test cho UserService với JUnit 5 và Mockito
 *
 * Áp dụng:
 *   - @ExtendWith(MockitoExtension.class) để khởi chạy môi trường Mockito thuần túy
 *   - @Mock cho UserRepository (cô lập hoàn toàn khỏi Database)
 *   - @InjectMocks cho UserService (đối tượng cần kiểm thử - SUT)
 *   - Chuẩn cấu trúc Given - When - Then (Arrange - Act - Assert)
 *   - ArgumentCaptor để kiểm tra tham số lưu trữ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử UserService với JUnit 5 & Mockito")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Nguyễn Văn A", "a@gmail.com");
    }

    @Nested
    @DisplayName("Kịch bản getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("Thành công: Trả về User khi ID tồn tại trong hệ thống")
        void getUserById_WhenUserExists_ReturnsUser() {
            // Given (Arrange)
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            // When (Act)
            User actual = userService.getUserById(1L);

            // Then (Assert)
            assertNotNull(actual, "Kết quả trả về không được phép null");
            assertAll("Kiểm tra toàn bộ thuộc tính của User",
                    () -> assertEquals(1L, actual.getId()),
                    () -> assertEquals("Nguyễn Văn A", actual.getName()),
                    () -> assertEquals("a@gmail.com", actual.getEmail())
            );
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Thất bại: Ném RuntimeException khi ID không tồn tại")
        void getUserById_WhenUserNotFound_ThrowsRuntimeException() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
            assertTrue(ex.getMessage().contains("User không tồn tại với id: 999"));
            verify(userRepository, times(1)).findById(999L);
        }
    }

    @Nested
    @DisplayName("Kịch bản getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("Thành công: Trả về danh sách tất cả người dùng")
        void getAllUsers_ReturnsUserList() {
            // Given
            List<User> mockList = List.of(
                    sampleUser,
                    new User(2L, "Trần Thị B", "b@gmail.com")
            );
            when(userRepository.findAll()).thenReturn(mockList);

            // When
            List<User> actualList = userService.getAllUsers();

            // Then
            assertEquals(2, actualList.size());
            assertEquals("Nguyễn Văn A", actualList.get(0).getName());
            assertEquals("Trần Thị B", actualList.get(1).getName());
            verify(userRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("Kịch bản createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Thành công: Lưu người dùng mới khi dữ liệu hợp lệ và email chưa tồn tại")
        void createUser_WithValidData_ReturnsSavedUser() {
            // Given
            User newUser = new User(null, "Lê Văn C", "c@gmail.com");
            when(userRepository.existsByEmail("c@gmail.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(new User(3L, "Lê Văn C", "c@gmail.com"));

            // When
            User saved = userService.createUser(newUser);

            // Then
            assertNotNull(saved);
            assertEquals(3L, saved.getId());
            verify(userRepository, times(1)).existsByEmail("c@gmail.com");
            verify(userRepository, times(1)).save(newUser);
        }

        @Test
        @DisplayName("Bắt tham số: Sử dụng ArgumentCaptor để kiểm tra object truyền vào save")
        void createUser_CapturesSavedArgument() {
            // Given
            User newUser = new User(null, "Phạm D", "d@gmail.com");
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            userService.createUser(newUser);

            // Then
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User captured = captor.getValue();
            assertEquals("Phạm D", captured.getName());
            assertEquals("d@gmail.com", captured.getEmail());
        }

        @Test
        @DisplayName("Kiểm tra thứ tự: Dùng InOrder để kiểm chứng existsByEmail được gọi TRƯỚC save")
        void createUser_VerifiesExecutionOrder() {
            // Given
            User newUser = new User(null, "Trần E", "e@gmail.com");
            when(userRepository.existsByEmail("e@gmail.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(new User(5L, "Trần E", "e@gmail.com"));

            // When
            userService.createUser(newUser);

            // Then
            org.mockito.InOrder inOrder = inOrder(userRepository);
            inOrder.verify(userRepository).existsByEmail("e@gmail.com");
            inOrder.verify(userRepository).save(newUser);
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.NullAndEmptySource
        @org.junit.jupiter.params.provider.ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("ParameterizedTest: Ném IllegalArgumentException với nhiều trường hợp tên không hợp lệ")
        void createUser_WhenNameIsInvalid_ThrowsException(String invalidName) {
            // Given
            User invalidUser = new User(null, invalidName, "valid@gmail.com");

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(invalidUser));
            assertEquals("Tên không được để trống", ex.getMessage());
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Thất bại: Ném IllegalArgumentException khi email rỗng")
        void createUser_WhenEmailIsBlank_ThrowsException() {
            // Given
            User invalidUser = new User(null, "Tên Hợp Lệ", "");

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(invalidUser));
            assertEquals("Email không được để trống", ex.getMessage());
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Thất bại: Ném IllegalArgumentException khi email đã tồn tại trong DB")
        void createUser_WhenEmailAlreadyExists_ThrowsException() {
            // Given
            User duplicateUser = new User(null, "Trùng Email", "a@gmail.com");
            when(userRepository.existsByEmail("a@gmail.com")).thenReturn(true);

            // When & Then
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.createUser(duplicateUser));
            assertEquals("Email đã tồn tại: a@gmail.com", ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Kịch bản deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("Thành công: Xóa user khi ID tồn tại")
        void deleteUser_WhenUserExists_DeletesSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            doNothing().when(userRepository).deleteById(1L);

            // When
            userService.deleteUser(1L);

            // Then
            verify(userRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Thất bại: Ném ngoại lệ khi xóa user có ID không tồn tại")
        void deleteUser_WhenUserNotFound_ThrowsException() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
            verify(userRepository, times(1)).findById(999L);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Kịch bản getUsersPaged")
    class GetUsersPagedTests {

        @Test
        @DisplayName("Thành công: Trả về đối tượng Page phân trang chuẩn")
        void getUsersPaged_ReturnsPagedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> mockPage = new PageImpl<>(List.of(sampleUser), pageable, 1);
            when(userRepository.findAll(pageable)).thenReturn(mockPage);

            // When
            Page<User> result = userService.getUsersPaged(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            verify(userRepository, times(1)).findAll(pageable);
        }
    }
}
