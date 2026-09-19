package com.crazydesert.racing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"crazy.super-admin.enabled=false",
		"crazy.jwt.secret=test-only-jwt-secret-with-at-least-32-bytes"
})
class RacingApplicationTests {

	@Test
	void contextLoads() {
	}

}
