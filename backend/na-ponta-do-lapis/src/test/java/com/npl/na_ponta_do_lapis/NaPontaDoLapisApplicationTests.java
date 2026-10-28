package com.npl.na_ponta_do_lapis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"app.cors.allowed-origins=http://localhost",
		"jwt.secret=test-jwt-secret-must-be-at-least-32-bytes"
})
@ActiveProfiles("h2")
class NaPontaDoLapisApplicationTests {

	@Test
	void contextLoads() {
	}

}
