# PHASE 2
## Basic App Structure
### Step 1: Main Application Class
Start with a failing test:
Create `src/test/java/com/dependabot/DependabotApplicationTest.java`
```
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
```
Create the main application class - `src/main/java/com/dependabot/DependabotApplication.java` :
```aiignore
package com.dependabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DependabotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DependabotApplication.class, args);
    }
}
```
Run the test again - it should pass now that it has a class to test.