package ru.ricardocraft.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.ricardocraft.backend.dto.request.update.LauncherRequest;
import ru.ricardocraft.backend.manangers.JacksonManager;

import java.util.Arrays;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private JacksonManager jacksonManager;

    @Test
    void contextLoads() {

    }

    @Test
    void jacksonTest() throws JsonProcessingException {
        String request = "{\"digest\":\"z4PhNX7vuL3xVChQ1m2AB9Yg5AULVxXcg_SpIdNs6c5H0NE8XYXysP-DGNKHfuwvY7kxvUdBeoGlODJ6-SfaPg==\",\"launcher_type\":1,\"requestUUID\":\"91d0b2b0-3f50-417a-acc5-143fd6fa6ef9\",\"type\":\"launcher\"}";
        LauncherRequest deserialized = jacksonManager.getMapper().readValue(request, LauncherRequest.class);
        System.out.println(Arrays.toString(deserialized.digest));
    }
}