package com.piggymetrics.account;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.config.ResourceServerConfig;
import com.piggymetrics.account.controller.AccountController;
import com.piggymetrics.account.controller.ErrorHandler;
import com.piggymetrics.account.repository.AccountRepository;
import com.piggymetrics.account.service.AccountService;
import com.piggymetrics.account.service.AccountServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountApplicationTest {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private AccountController accountController;

	@Autowired
	private AccountService accountService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ErrorHandler errorHandler;

	@Autowired
	private ResourceServerConfig resourceServerConfig;

	@Autowired
	private AuthServiceClient authServiceClient;

	@Autowired
	private StatisticsServiceClient statisticsServiceClient;

	@Test
	public void shouldLoadApplicationContext() {
		assertNotNull(context);
	}

	@Test
	public void shouldRegisterCoreBeans() {
		assertNotNull(accountController);
		assertNotNull(accountService);
		assertNotNull(accountRepository);
		assertNotNull(errorHandler);
		assertNotNull(resourceServerConfig);
	}

	@Test
	public void shouldWireFeignClients() {
		assertNotNull(authServiceClient);
		assertNotNull(statisticsServiceClient);
	}

	@Test
	public void shouldExposeAccountServiceImplementation() {
		assertTrue(accountService instanceof AccountServiceImpl);
	}

	@Test
	public void mainClassShouldDeclareExpectedAnnotations() {
		assertNotNull(AccountApplication.class.getAnnotation(SpringBootApplication.class));
		assertNotNull(AccountApplication.class.getAnnotation(EnableDiscoveryClient.class));
		assertNotNull(AccountApplication.class.getAnnotation(EnableOAuth2Client.class));
		assertNotNull(AccountApplication.class.getAnnotation(EnableFeignClients.class));
		assertNotNull(AccountApplication.class.getAnnotation(EnableCircuitBreaker.class));
		assertNotNull(AccountApplication.class.getAnnotation(EnableGlobalMethodSecurity.class));
	}

	@Test
	public void mainMethodShouldBeAvailable() throws Exception {
		assertNotNull(AccountApplication.class.getDeclaredMethod("main", String[].class));
	}
}
