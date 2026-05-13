package com.piggymetrics.account.config;

import com.piggymetrics.account.service.security.CustomUserInfoTokenServices;
import feign.RequestInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.lang.reflect.Method;

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
		sso.setUserInfoUri("http://localhost/uaa/users/current");
		config = new ResourceServerConfig(sso);
	}

	@Test
	public void shouldExposeClientCredentialsResourceDetailsBean() {
		ClientCredentialsResourceDetails details = config.clientCredentialsResourceDetails();
		assertNotNull(details);
	}

	@Test
	public void shouldReturnNewInstanceOnEachCallToClientCredentialsResourceDetails() {
		ClientCredentialsResourceDetails a = config.clientCredentialsResourceDetails();
		ClientCredentialsResourceDetails b = config.clientCredentialsResourceDetails();
		assertNotSame(a, b);
	}

	@Test
	public void shouldExposeOauth2FeignRequestInterceptorBean() {
		RequestInterceptor interceptor = config.oauth2FeignRequestInterceptor();
		assertNotNull(interceptor);
	}

	@Test
	public void shouldExposeClientCredentialsRestTemplateBean() {
		OAuth2RestTemplate restTemplate = config.clientCredentialsRestTemplate();
		assertNotNull(restTemplate);
	}

	@Test
	public void shouldExposeTokenServicesAsCustomUserInfoTokenServices() {
		ResourceServerTokenServices tokenServices = config.tokenServices();
		assertNotNull(tokenServices);
		assertTrue(tokenServices instanceof CustomUserInfoTokenServices);
	}

	@Test
	public void shouldStoreInjectedResourceServerProperties() {
		ResourceServerProperties other = new ResourceServerProperties("another-client", "another-secret");
		other.setUserInfoUri("http://other/userinfo");
		ResourceServerConfig localConfig = new ResourceServerConfig(other);
		assertNotNull(localConfig.tokenServices());
	}

	@Test
	public void shouldDeclareConfigurationAndResourceServerAnnotations() {
		assertNotNull(ResourceServerConfig.class.getAnnotation(Configuration.class));
		assertNotNull(ResourceServerConfig.class.getAnnotation(EnableResourceServer.class));
	}

	@Test
	public void shouldExtendResourceServerConfigurerAdapter() {
		assertTrue(ResourceServerConfigurerAdapter.class.isAssignableFrom(ResourceServerConfig.class));
	}

	@Test
	public void shouldOverrideHttpSecurityConfigureMethod() throws NoSuchMethodException {
		Method configure = ResourceServerConfig.class.getDeclaredMethod("configure", HttpSecurity.class);
		assertNotNull(configure);
		assertEquals(ResourceServerConfig.class, configure.getDeclaringClass());
	}

	private static void assertNotSame(Object a, Object b) {
		org.junit.Assert.assertNotSame("Bean factory method should return new instances", a, b);
		assertSame(a.getClass(), b.getClass());
	}
}
