package eu.ibagroup.vfjobstorageservice;

import eu.ibagroup.vfjobstorageservice.controllers.JobStorageController;
import eu.ibagroup.vfjobstorageservice.services.JobStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class VfJobStorageServiceApplicationTests {

	@Autowired
	private JobStorageController jobController;
	@Autowired
	private JobStorageService jobService;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private RestTemplate authRestTemplate;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void contextLoads() {
		assertNotNull(jobController);
		assertNotNull(jobService);
		assertNotNull(restTemplate);
		assertNotNull(authRestTemplate);
		assertNotNull(redisTemplate);
	}

}
