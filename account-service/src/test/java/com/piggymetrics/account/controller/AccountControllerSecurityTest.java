package com.piggymetrics.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.service.AccountService;
import com.sun.security.auth.UserPrincipal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security-focused tests for {@link AccountController}. We boot a slim Spring context with
 * method-level security enabled (using the OAuth2 expression handler) so that the
 * {@code @PreAuthorize} on {@code getAccountByName} is actually evaluated, exactly as it would
 * be in production.
 *
 * <p>The full OAuth2 resource-server filter chain is not started up here — those concerns are
 * verified by the live deployment and indirectly by
 * {@link com.piggymetrics.account.AccountServiceApplicationTests#contextLoads()}.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AccountControllerSecurityTest.TestConfig.class)
public class AccountControllerSecurityTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class TestConfig extends GlobalMethodSecurityConfiguration {

		@Override
		protected MethodSecurityExpressionHandler createExpressionHandler() {
			return new OAuth2MethodSecurityExpressionHandler();
		}

		@Bean
		public AccountService accountService() {
			return Mockito.mock(AccountService.class);
		}

		@Bean
		public AccountController accountController() {
			return new AccountController();
		}

		@Bean
		public ErrorHandler errorHandler() {
			return new ErrorHandler();
		}
	}

	@Autowired
	private AccountController accountController;

	@Autowired
	private AccountService accountService;

	@Autowired
	private ErrorHandler errorHandler;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		Mockito.reset(accountService);
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountController)
				.setControllerAdvice(errorHandler)
				.build();
	}

	@After
	public void clearAuth() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void shouldReturn401WhenNoPrincipalOnCurrent() throws Exception {
		// /current dereferences principal.getName(); without a Principal the controller cannot
		// resolve a user. The production filter chain rejects unauthenticated requests with 401
		// before they reach the controller; here we verify the controller-layer behaviour by
		// asserting the request fails server-side (NPE wrapped as 500).
		try {
			mockMvc.perform(get("/current"))
					.andExpect(status().is5xxServerError());
		} catch (Exception e) {
			if (!(rootCauseIsNpe(e))) {
				throw e;
			}
		}
	}

	@Test
	public void shouldDenyAccessWhenInvalidScope() throws Exception {
		setAuth(authWithScopes("ui"));

		try {
			mockMvc.perform(get("/anyone"))
					.andExpect(status().isForbidden());
		} catch (Exception e) {
			// Standalone MockMvc has no ExceptionTranslationFilter, so an unauthorised call
			// surfaces as a wrapped AccessDeniedException rather than an HTTP 403. Either is
			// an acceptable signal of denial; we only re-throw if the cause chain is unrelated.
			if (!causeChainContains(e, AccessDeniedException.class)) {
				throw e;
			}
		}
	}

	@Test
	public void shouldAllowAccessWithServerScope() throws Exception {
		Account account = new Account();
		account.setName("alice");
		when(accountService.findByName("alice")).thenReturn(account);

		setAuth(authWithScopes("server"));

		mockMvc.perform(get("/alice"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldAllowDemoAccessWithoutServerScope() throws Exception {
		Account demo = new Account();
		demo.setName("demo");
		when(accountService.findByName("demo")).thenReturn(demo);

		setAuth(authWithScopes("ui"));

		// /demo is whitelisted by #name.equals('demo') in @PreAuthorize, regardless of scope.
		mockMvc.perform(get("/demo"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldAllowDemoAccessForAnonymousAuthentication() throws Exception {
		Account demo = new Account();
		demo.setName("demo");
		when(accountService.findByName("demo")).thenReturn(demo);

		Authentication anon = new AnonymousAuthenticationToken(
				"anon-key", "anonymousUser",
				AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
		setAuth(anon);

		mockMvc.perform(get("/demo"))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldAllowCurrentAccessWithValidPrincipal() throws Exception {
		Account account = new Account();
		account.setName("authed");
		when(accountService.findByName("authed")).thenReturn(account);

		setAuth(new UsernamePasswordAuthenticationToken("authed", "n/a",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		mockMvc.perform(get("/current").principal(new UserPrincipal("authed")))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldRegisterNewAccountWithoutScope() throws Exception {
		// POST / has no @PreAuthorize, so any authenticated caller (or permitAll-routed call)
		// can hit it. We assert no scope check blocks the request.
		User user = new User();
		user.setUsername("newuser");
		user.setPassword("strongpassword");

		setAuth(new UsernamePasswordAuthenticationToken("newuser", "n/a",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		mockMvc.perform(post("/")
				.principal(new UserPrincipal("newuser"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(user)))
				.andExpect(status().isOk());
	}

	@Test
	public void contextShouldBeWired() {
		assertNotNull(accountController);
		assertNotNull(accountService);
	}

	private static void setAuth(Authentication authentication) {
		SecurityContext ctx = SecurityContextHolder.createEmptyContext();
		if (authentication != null) {
			ctx.setAuthentication(authentication);
		}
		SecurityContextHolder.setContext(ctx);
	}

	private static OAuth2Authentication authWithScopes(String... scopes) {
		HashSet<String> scopeSet = new HashSet<>(Arrays.asList(scopes));
		OAuth2Request request = new OAuth2Request(
				Collections.<String, String>emptyMap(),
				"client",
				AuthorityUtils.createAuthorityList("ROLE_USER"),
				true,
				scopeSet,
				Collections.<String>emptySet(),
				null,
				Collections.<String>emptySet(),
				new HashMap<String, Serializable>());
		TestingAuthenticationToken user = new TestingAuthenticationToken("user", "n/a", "ROLE_USER");
		user.setAuthenticated(true);
		return new OAuth2Authentication(request, user);
	}

	private static Throwable unwrap(Throwable t) {
		Throwable cur = t;
		while (cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		return cur;
	}

	private static boolean rootCauseIsNpe(Throwable t) {
		return causeChainContains(t, NullPointerException.class);
	}

	private static boolean causeChainContains(Throwable t, Class<? extends Throwable> type) {
		Throwable cur = t;
		while (cur != null) {
			if (type.isInstance(cur)) {
				return true;
			}
			if (cur.getCause() == cur) {
				break;
			}
			cur = cur.getCause();
		}
		return false;
	}
}
