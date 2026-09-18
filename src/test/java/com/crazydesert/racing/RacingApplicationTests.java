package com.crazydesert.racing;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "crazy.super-admin.enabled=false")
class RacingApplicationTests {

	@Test
	void contextLoads() {
	}

}
