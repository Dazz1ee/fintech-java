package foo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.configurations.SecurityConfig;
import foo.exceptions.CreateUserException;
import foo.models.UserDto;
import foo.services.IdentificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {IdentificationController.class})
@Import({SecurityConfig.class, ObjectMapper.class})
@ActiveProfiles("test")
class IdentificationControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    IdentificationService identificationService;

    @Test
    void signUp() throws Exception {
        UserDto userDto = new UserDto("test", "test", "test");

        mockMvc.perform(post("/registration")
                .with(csrf()).content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void signUpWithoutCsrf() throws Exception {
        UserDto userDto = new UserDto("test", "test", "test");

        mockMvc.perform(post("/registration")
                .content(objectMapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void signUpWhenReturnedCreateUserException() throws Exception {
        UserDto userDto = new UserDto("test", "test", "test");
        doThrow(new CreateUserException()).when(identificationService).signUp(userDto);

        mockMvc.perform(post("/registration")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}