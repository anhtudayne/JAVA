package com.example.springbootlearning.controller;

import com.example.springbootlearning.model.User;
import com.example.springbootlearning.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 📘 BÀI 14 — Slice Test cho UserController sử dụng @WebMvcTest & MockMvc
 *
 * Đặc điểm:
 *   - Chỉ nạp tầng Web MVC (UserController), không nạp tầng Service/DB
 *   - Dùng @MockitoBean để mock UserService trong ApplicationContext
 *   - Kiểm tra mã HTTP status, header và cấu trúc JSON trả về qua jsonPath()
 */
@WebMvcTest(UserController.class)
@DisplayName("Kiểm thử UserController với WebMvcTest & MockMvc")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users: Trả về HTTP 200 và mảng JSON chứa danh sách người dùng")
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Given
        List<User> userList = List.of(
                new User(1L, "Nguyễn Văn A", "a@gmail.com"),
                new User(2L, "Trần Thị B", "b@gmail.com")
        );
        when(userService.getAllUsers()).thenReturn(userList);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Nguyễn Văn A")))
                .andExpect(jsonPath("$[0].email", is("a@gmail.com")))
                .andExpect(jsonPath("$[1].name", is("Trần Thị B")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/1: Trả về HTTP 200 khi tìm thấy người dùng theo ID")
    void getUserById_WhenExists_ShouldReturnUser() throws Exception {
        // Given
        User user = new User(1L, "Nguyễn Văn A", "a@gmail.com");
        when(userService.getUserById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Nguyễn Văn A")))
                .andExpect(jsonPath("$.email", is("a@gmail.com")));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("POST /api/users: Trả về HTTP 200 và thông tin User vừa được tạo")
    void createUser_WithValidPayload_ShouldReturnCreatedUser() throws Exception {
        // Given
        User request = new User(null, "Lê Văn C", "c@gmail.com");
        User response = new User(3L, "Lê Văn C", "c@gmail.com");

        when(userService.createUser(any(User.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Lê Văn C")))
                .andExpect(jsonPath("$.email", is("c@gmail.com")));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/users/1: Trả về HTTP 200 và chuỗi thông báo xóa thành công")
    void deleteUser_ShouldReturnSuccessMessage() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Đã xóa user với id: 1"));

        verify(userService, times(1)).deleteUser(1L);
    }
}
