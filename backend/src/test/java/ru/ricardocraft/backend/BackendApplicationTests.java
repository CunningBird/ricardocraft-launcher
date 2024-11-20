package ru.ricardocraft.backend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.auth.AuthResponse;
import ru.ricardocraft.backend.dto.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.dto.auth.ProfilesResponse;
import ru.ricardocraft.backend.dto.secure.GetSecureLevelInfoResponse;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void jacksonTest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String getAvailabilityAuth = "{\"requestUUID\":\"d299512a-8968-4d80-8d7e-56fd45396c13\",\"type\":\"getAvailabilityAuth\"}";
        String auth = "{" +
                "\"login\":\"cunningbird\"," +
                "\"password\":{" +
                    "\"password\":\"13488stef\"," +
                    "\"type\":\"plain\"" +
                "}," +
                "\"auth_id\":\"std\"," +
                "\"getSession\":false," +
                "\"authType\":\"API\"," +
                "\"requestUUID\":\"02603771-5dfd-4062-8454-e6ffa4d0bec3\"," +
                "\"type\":\"auth\"" +
                "}";
        String profiles = "{\"requestUUID\":\"34bc1924-d2e7-46f3-b7c0-8984c79bd83a\",\"type\":\"profiles\"}";
        String getSecureLevelInfo = "{\"requestUUID\":\"c571a648-df39-41e4-b161-2378ac7cc3ee\",\"type\":\"getSecureLevelInfo\"}";

        SimpleResponse getAvailabilityAuthDeserialized = objectMapper.readValue(getAvailabilityAuth, SimpleResponse.class);
        SimpleResponse authDeserialized = objectMapper.readValue(auth, SimpleResponse.class);
        SimpleResponse profilesDeserialized = objectMapper.readValue(profiles, SimpleResponse.class);
        SimpleResponse getSecureLevelInfoDeserialized = objectMapper.readValue(getSecureLevelInfo, SimpleResponse.class);

        Assertions.assertTrue(getAvailabilityAuthDeserialized.getClass().isAssignableFrom(GetAvailabilityAuthResponse.class));
        Assertions.assertTrue(authDeserialized.getClass().isAssignableFrom(AuthResponse.class));
        Assertions.assertTrue(profilesDeserialized.getClass().isAssignableFrom(ProfilesResponse.class));
        Assertions.assertTrue(getSecureLevelInfoDeserialized.getClass().isAssignableFrom(GetSecureLevelInfoResponse.class));
    }
}