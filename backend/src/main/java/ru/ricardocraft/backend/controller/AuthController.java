package ru.ricardocraft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import ru.ricardocraft.backend.socket.WebSocketService;

//@Controller
@Component
public class AuthController {

    private final WebSocketService webSocketService;

    @Autowired
    public AuthController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public void additionalData() {

    }

    public void auth() {

    }

    public void checkServer() {

    }

    public void currentUser() {

    }

    public void exit() {

    }

    public void clientProfileKey() {

    }

    public void getAvailabilityAuth() {

    }

    public void joinServer() {

    }

    public void profiles() {

    }

    public void refreshToken() {

    }

    public void restore() {

    }

    public void setProfile() {

    }
}
