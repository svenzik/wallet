package com.playtech.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtech.wallet.AbstractBaseIntegrationTest;
import org.junit.Before;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public abstract class AbstractServiceIntegrationTest extends AbstractBaseIntegrationTest {

    private MockMvc mockMvc;

    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    @Before
    public void setupServiceTest() {
        this.mockMvc = webAppContextSetup(getWac()).build();
    }

    /**
     * Helper method for testing controller. Serializes POJO to JSON
     * @param object
     * @return
     * @throws JsonProcessingException
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }


}
