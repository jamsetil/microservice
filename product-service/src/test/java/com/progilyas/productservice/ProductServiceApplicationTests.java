package com.progilyas.productservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progilyas.productservice.dto.ProductRequest;
import com.progilyas.productservice.dto.ProductResponse;
import com.progilyas.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

//....::::::Tests are not right !!
    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"));

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){
        dynamicPropertyRegistry.add("spring.data.mongodb.uri",mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest productRequest = getProductRequest();
        String json = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated());

        Assertions.assertEquals(1,productRepository.findAll().size());
    }

    private ProductRequest getProductRequest() {
    return ProductRequest.builder()
            .description("128gb")
            .name("Iphone 15")
            .price(BigDecimal.valueOf(12000))
            .build();
    }

    private ProductResponse getProductResponse(){
        return ProductResponse.builder()
                .id("1")
                .description("128gb")
                .name("Iphone 15")
                .price(BigDecimal.valueOf(12000))
                .build();
    }

    @Test
    void shouldReturnProducts() throws Exception {
        ProductResponse productResponse = getProductResponse();
        String json = objectMapper.writeValueAsString(productResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isAccepted());

        Assertions.assertEquals(0,productRepository.findAll().size());
    }

}
