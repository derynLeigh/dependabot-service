package com.dependabot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DependabotApplicationTest {

    @Test
    void contextLoads() {
        // This test will fail until we create the main application class
        assertThat(true).isTrue();
    }
}