package com.piggymetrics.account.config;

import com.piggymetrics.account.service.security.CustomUserInfoTokenServices;
import feign.RequestInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ResourceServerConfigTest {

	private ResourceServerProperties sso;
	private ResourceServerConfig config;

	@Before
	public void setup() {
		sso = new ResourceServerProperties("test-client", "test-secret");
		sso.setUserInfoUri("http://auth-service/uaa/users/current");
		config = new ResourceServerConfig(sso);
	}

	@Test
	public void shouldExposeClientCredentialsResourceDetailsBean() {
		ClientCredentialsResourceDetails details = config.clientCredentialsResourceDetails();

		assertNotNull(details);
	}

	@Test
	public void shouldExposeOAuth2FeignRequestInterceptor() {
		RequestInterceptor interceptor = config.oauth2FeignRequestInterceptor();

		assertNotNull(interceptor);
		assertTrue(interceptor instanceof OAuth2FeignRequestInterceptor);
	}

	@Test
	public void shouldExposeClientCredentialsRestTemplate() {
		OAuth2RestTemplate restTemplate = config.clientCredentialsRestTemplate();

		assertNotNull(restTemplate);
		assertSame(config.clientCredentialsResourceDetails().getClass(),
				restTemplate.getResource().getClass());
	}

	@Test
	public void shouldExposeCustomTokenServices() {
		ResourceServerTokenServices tokenServices = config.tokenServices();

		assertNotNull(tokenServices);
		assertTrue(tokenServices instanceof CustomUserInfoTokenServices);
	}

	@Test
	public void shouldProduceFreshBeanInstancesOnEachCall() {
		// Outside of the Spring container the @Bean methods return new instances per call.
		// This is the unmanaged behaviour we exercise in unit tests.
		assertEquals(ClientCredentialsResourceDetails.class,
				config.clientCredentialsResourceDetails().getClass());
		assertEquals(OAuth2RestTemplate.class,
				config.clientCredentialsRestTemplate().getClass());
		assertTrue(config.oauth2FeignRequestInterceptor() instanceof RequestInterceptor);
	}
}
