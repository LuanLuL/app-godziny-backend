package com.cefet.godziny;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class GodzinyApplicationTests {

    @Test
    @DisplayName("Should start the application through the main method")
    void shouldCallSpringApplicationRun() {
        String[] args = {};

        try (MockedStatic<SpringApplication> springApplication =
                 mockStatic(SpringApplication.class)) {

            GodzinyApplication.main(args);

            springApplication.verify(
                () -> SpringApplication.run(GodzinyApplication.class, args)
            );
        }
    }
}