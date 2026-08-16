package com.mahindra.api.controller;

import com.mahindra.api.exception.ResourceNotFoundException;
import com.mahindra.api.model.City;
import com.mahindra.api.model.PaginatedResponse;
import com.mahindra.api.service.CityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityService cityService;

    private final City mumbai = new City(1L, "Mumbai", 1L, 20_667_656L, "400001", "Financial capital of India");
    private final City delhi  = new City(2L, "Delhi",  1L, 32_941_000L, "110001", "Capital of India");

    @Test
    void listCitiesByCountry_returnsPaginatedResponse() throws Exception {
        PaginatedResponse<City> page = new PaginatedResponse<>(List.of(mumbai, delhi), 0, 10, 2L, 1);
        when(cityService.getCitiesByCountry(eq(1L), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/countries/1/cities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Mumbai")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalItems", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void listCitiesByCountry_acceptsCustomPageAndSize() throws Exception {
        PaginatedResponse<City> page = new PaginatedResponse<>(List.of(mumbai), 1, 1, 2L, 2);
        when(cityService.getCitiesByCountry(eq(1L), eq(1), eq(1))).thenReturn(page);

        mockMvc.perform(get("/countries/1/cities?page=1&size=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void listCitiesByCountry_returns404_whenCountryNotFound() throws Exception {
        when(cityService.getCitiesByCountry(eq(999L), eq(0), eq(10)))
                .thenThrow(new ResourceNotFoundException("Country not found with id 999"));

        mockMvc.perform(get("/countries/999/cities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    @Test
    void getCityById_returnsCity() throws Exception {
        when(cityService.getCityById(1L)).thenReturn(mumbai);

        mockMvc.perform(get("/cities/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Mumbai")))
                .andExpect(jsonPath("$.countryId", is(1)))
                .andExpect(jsonPath("$.population", is(20667656)))
                .andExpect(jsonPath("$.zipCode", is("400001")))
                .andExpect(jsonPath("$.description", is("Financial capital of India")));
    }

    @Test
    void getCityById_returns404_whenNotFound() throws Exception {
        when(cityService.getCityById(999L))
                .thenThrow(new ResourceNotFoundException("City not found with id 999"));

        mockMvc.perform(get("/cities/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("999")));
    }
}
