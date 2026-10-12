package com.npl.na_ponta_do_lapis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {
		"app.cors.allowed-origins=http://localhost",
		"jwt.secret=test-jwt-secret-must-be-at-least-32-bytes"
})
@ActiveProfiles("h2")
@TestPropertySource(properties = "spring.sql.init.mode=never")
class NaPontaDoLapisApplicationTests {

	@Test
	void contextLoads() {
	}

}
