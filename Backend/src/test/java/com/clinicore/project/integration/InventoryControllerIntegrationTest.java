package com.clinicore.project.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final Long ADMIN_ID = 1L;
    private static Long createdMedicationId;
    private static Long createdConsumableId;

    @Test
    @Order(1)
    @DisplayName("TEST 1: GET medication inventory returns list")
    void testGetMedicationInventory() throws Exception {
        System.out.println("\n=== TEST 1: Get Medication Inventory ===");

        mockMvc.perform(get("/api/inventory/medication")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: GET consumables inventory returns list")
    void testGetConsumablesInventory() throws Exception {
        System.out.println("\n=== TEST 2: Get Consumables Inventory ===");

        mockMvc.perform(get("/api/inventory/consumables")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: POST creates a new medication inventory item")
    void testCreateMedicationItem() throws Exception {
        System.out.println("\n=== TEST 3: Create Medication Item ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Test Ibuprofen");
        body.put("quantity", 50);
        body.put("dosagePerServing", "200mg");
        body.put("notes", "created by integration test");

        String response = mockMvc.perform(post("/api/inventory/medication")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Ibuprofen"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andReturn().getResponse().getContentAsString();

        // Extract id for downstream tests
        Map<?, ?> parsed = objectMapper.readValue(response, Map.class);
        createdMedicationId = ((Number) parsed.get("id")).longValue();

        System.out.println("PASSED TEST 3 — id=" + createdMedicationId);
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: PUT updates medication quantity")
    void testUpdateMedication() throws Exception {
        System.out.println("\n=== TEST 4: Update Medication ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Test Ibuprofen");
        body.put("quantity", 5);   // drop below threshold
        body.put("dosagePerServing", "200mg");

        mockMvc.perform(put("/api/inventory/medication/" + createdMedicationId)
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: GET low-stock medication returns items below threshold")
    void testLowStockMedication() throws Exception {
        System.out.println("\n=== TEST 5: Low Stock Medication ===");

        mockMvc.perform(get("/api/inventory/medication/lowstock")
                        .param("currentUserId", ADMIN_ID.toString())
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                // Should include our updated item with quantity=5
                .andExpect(jsonPath("$[?(@.id==" + createdMedicationId + ")]").exists());

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: POST creates a new consumable")
    void testCreateConsumable() throws Exception {
        System.out.println("\n=== TEST 6: Create Consumable ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Test Gauze");
        body.put("quantity", 100);

        String response = mockMvc.perform(post("/api/inventory/consumables")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Gauze"))
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> parsed = objectMapper.readValue(response, Map.class);
        createdConsumableId = ((Number) parsed.get("id")).longValue();

        System.out.println("PASSED TEST 6 — id=" + createdConsumableId);
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: GET low-stock consumables works")
    void testLowStockConsumables() throws Exception {
        System.out.println("\n=== TEST 7: Low Stock Consumables ===");

        mockMvc.perform(get("/api/inventory/consumables/lowstock")
                        .param("currentUserId", ADMIN_ID.toString())
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: DELETE removes the medication item")
    void testDeleteMedication() throws Exception {
        System.out.println("\n=== TEST 8: Delete Medication ===");

        mockMvc.perform(delete("/api/inventory/medication/" + createdMedicationId)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk());

        // Verify it's gone
        mockMvc.perform(get("/api/inventory/medication/" + createdMedicationId)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().is4xxClientError());

        System.out.println("PASSED TEST 8");
    }

    @Test
    @Order(9)
    @DisplayName("TEST 9: DELETE removes the consumable")
    void testDeleteConsumable() throws Exception {
        System.out.println("\n=== TEST 9: Delete Consumable ===");

        mockMvc.perform(delete("/api/inventory/consumables/" + createdConsumableId)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 9");
    }
}
