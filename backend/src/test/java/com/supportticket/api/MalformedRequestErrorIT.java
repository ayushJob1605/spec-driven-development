package com.supportticket.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MalformedRequestErrorIT.TestEchoController.class)
class MalformedRequestErrorIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownJsonPropertyReturns400MalformedRequest() throws Exception {
        mockMvc.perform(post("/api/v1/_test/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"ok\",\"unexpected\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ErrorCodes.MALFORMED_REQUEST))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/_test/echo"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @RestController
    @RequestMapping("/api/v1/_test")
    static class TestEchoController {

        @PostMapping("/echo")
        EchoResponse echo(@RequestBody EchoRequest request) {
            return new EchoResponse(request.value());
        }
    }

    record EchoRequest(String value) {
    }

    record EchoResponse(String value) {
    }
}
