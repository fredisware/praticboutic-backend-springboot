package com.ecommerce.praticboutic_backend_java;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"stripe.secret.key=sk_test_51H8fNKHGzhgYgqhxXKxXLCKqGMGaHXXfQ3AedURHAd2BTaNjr07L7wLHVZP41UMNWnxRHt4R7XTdeydg0GWcUXL400QA2swxxl",
		"stripe.public.key=pk_test_51H8fNKHGzhgYgqhxjTrk1jhYn1AyPAZvKUjjTXOAcOZ3AS8wXYAHbTgq6kS2tVw8bg57KNmJTUEJ6jciNwwc5KbX00ovJ3hmZD"
})


class PraticbouticBackendJavaApplicationTests {

	@Test
	void contextLoads() {
	}

}
