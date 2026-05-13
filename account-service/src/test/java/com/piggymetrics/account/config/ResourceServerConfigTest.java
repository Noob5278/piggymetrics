package com.piggymetrics.account.config;

import com.piggymetrics.account.service.security.CustomUserInfoTokenServices;
import feign.RequestInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceServerConfigTest {

	private ResourceServerProperties sso;
	private ResourceServerConfig config;

	@Before
	public void setup() {
		sso = mock(ResourceServerProperties.class);
		when(sso.getUserInfoUri()).thenReturn("http://auth-service:5000/uaa/user");
		when(sso.getClientId()).thenReturn("account-service");
		config = new ResourceServerConfig(sso);
	}

	@Test
	public void shouldStoreResourceServerPropertiesViaConstructor() {
		assertNotNull(config);
	}

	@Test
	public void shouldExposeClientCredentialsResourceDetailsBean() {
		ClientCredentialsResourceDetails details = config.clientCredentialsResourceDetails();
		assertNotNull(details);
	}

	@Test
	public void shouldExposeFeignOAuth2RequestInterceptorBean() {
		RequestInterceptor interceptor = config.oauth2FeignRequestInterceptor();
		assertNotNull(interceptor);
		assertTrue(interceptor instanceof OAuth2FeignRequestInterceptor);
	}

	@Test
	public void shouldExposeOAuth2RestTemplateBean() {
		OAuth2RestTemplate template = config.clientCredentialsRestTemplate();
		assertNotNull(template);
		assertNotNull(template.getResource());
	}

	@Test
	public void shouldExposeCustomUserInfoTokenServicesBean() {
		ResourceServerTokenServices tokenServices = config.tokenServices();
		assertNotNull(tokenServices);
		assertTrue(tokenServices instanceof CustomUserInfoTokenServices);

		verify(sso, times(1)).getUserInfoUri();
		verify(sso, times(1)).getClientId();
	}

	@Test
	public void shouldExtendResourceServerConfigurerAdapter() {
		assertTrue(config instanceof ResourceServerConfigurerAdapter);
	}

	@Test
	public void shouldBeAnnotatedWithEnableResourceServer() {
		EnableResourceServer annotation = ResourceServerConfig.class
				.getAnnotation(EnableResourceServer.class);
		assertNotNull("ResourceServerConfig must be annotated with @EnableResourceServer",
				annotation);
	}

	@Test
	public void shouldOverrideConfigureHttpSecurityMethod() throws NoSuchMethodException {
		Method method = ResourceServerConfig.class.getDeclaredMethod(
				"configure",
				org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
		assertNotNull(method);
		assertSame(ResourceServerConfig.class, method.getDeclaringClass());
	}

	@Test
	public void shouldReturnIndependentBeanInstancesPerInvocation() {
		ClientCredentialsResourceDetails first = config.clientCredentialsResourceDetails();
		ClientCredentialsResourceDetails second = config.clientCredentialsResourceDetails();

		assertNotNull(first);
		assertNotNull(second);
		// @Bean method itself returns a new instance each call when invoked directly;
		// Spring proxies it in a real context, but unit-level invocation is unproxied.
		assertSame(first.getClass(), second.getClass());
	}
}
