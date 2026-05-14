package com.piggymetrics.account.config;

import com.piggymetrics.account.service.security.CustomUserInfoTokenServices;
import feign.RequestInterceptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ResourceServerConfigTest {

	@Autowired
	private ClientCredentialsResourceDetails clientCredentialsResourceDetails;

	@Autowired
	private RequestInterceptor requestInterceptor;

	@Autowired
	private OAuth2RestTemplate oAuth2RestTemplate;

	@Autowired
	private ResourceServerTokenServices tokenServices;

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void shouldCreateClientCredentialsResourceDetailsBean() {
		assertNotNull(clientCredentialsResourceDetails);
	}

	@Test
	public void shouldCreateRequestInterceptorBean() {
		assertNotNull(requestInterceptor);
	}

	@Test
	public void shouldCreateOAuth2RestTemplateBean() {
		assertNotNull(oAuth2RestTemplate);
	}

	@Test
	public void shouldCreateTokenServicesBean() {
		assertNotNull(tokenServices);
		assertTrue(tokenServices instanceof CustomUserInfoTokenServices);
	}

	@Test
	public void shouldAllowUnauthenticatedAccessToRootPath() throws Exception {
		// Root path is configured as permitAll; POST / maps to createNewAccount.
		// A GET to / will not be unauthorized — it either returns 405 (no GET handler) or 200.
		// The key assertion is that the request is NOT rejected as 401/403.
		int statusCode = mockMvc.perform(get("/")).andReturn().getResponse().getStatus();
		assertNotEquals(401, statusCode);
		assertNotEquals(403, statusCode);
	}

	@Test
	public void shouldAllowUnauthenticatedAccessToDemoPath() throws Exception {
		int statusCode = mockMvc.perform(get("/demo")).andReturn().getResponse().getStatus();
		assertNotEquals(401, statusCode);
		assertNotEquals(403, statusCode);
	}

	@Test
	public void shouldDenyUnauthenticatedAccessToProtectedPaths() throws Exception {
		mockMvc.perform(get("/current"))
				.andExpect(status().isUnauthorized());
	}
}
