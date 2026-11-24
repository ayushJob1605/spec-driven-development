package com.supportticket.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTicketReturns201WithOpenStatus() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cannot login",
                                  "description": "User reports 401",
                                  "priority": "HIGH",
                                  "assignee": "alex"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    @Test
    void createRejectsBlankTitleWith422() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Valid description",
                                  "priority": "LOW"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    void createRejectsUnknownPropertyWith400() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Title",
                                  "description": "Description",
                                  "priority": "LOW",
                                  "status": "OPEN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.MALFORMED_REQUEST));
    }

    @Test
    void getMissingTicketReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCodes.NOT_FOUND))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void listSupportsSearchFilterAndPagination() throws Exception {
        createTicket("Alpha unique-keyword", "Body one", "LOW");
        createTicket("Beta other", "unique-keyword in description", "MEDIUM");

        mockMvc.perform(get("/api/v1/tickets")
                        .param("q", "unique-keyword")
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void sizeAboveMaxReturns422() throws Exception {
        mockMvc.perform(get("/api/v1/tickets").param("size", "101"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
    }

    @Test
    void patchUpdatesFieldsAndUnassigns() throws Exception {
        String id = createTicket("Patch me", "Description", "LOW");

        mockMvc.perform(patch("/api/v1/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Patched",
                                  "priority": "CRITICAL",
                                  "assignee": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Patched"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.assignee").value(nullValue()));
    }

    @Test
    void patchWithStatusPropertyReturns400() throws Exception {
        String id = createTicket("No status patch", "Description", "LOW");

        mockMvc.perform(patch("/api/v1/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Still open",
                                  "status": "IN_PROGRESS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCodes.MALFORMED_REQUEST));
    }

    @Test
    void validAndInvalidStatusTransitions() throws Exception {
        String id = createTicket("Transition", "Description", "HIGH");

        mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OPEN\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCodes.INVALID_TRANSITION));

        mockMvc.perform(patch("/api/v1/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Nope\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
    }

    @Test
    void cancelFromOpenAndRejectCommentOnTerminal() throws Exception {
        String id = createTicket("Cancel me", "Description", "LOW");

        mockMvc.perform(post("/api/v1/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Looking into it\",\"author\":\"alex\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Looking into it"));

        mockMvc.perform(post("/api/v1/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/v1/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Should fail\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));

        mockMvc.perform(get("/api/v1/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)));
    }

    @Test
    void invalidPriorityEnumReturns422() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Title",
                                  "description": "Description",
                                  "priority": "URGENT"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(ErrorCodes.VALIDATION_ERROR));
    }

    private String createTicket(String title, String description, String priority) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "%s",
                                  "priority": "%s"
                                }
                                """.formatted(title, description, priority)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asText();
    }
}
