package com.piggymetrics.auth;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

public class EmbeddedMongoTestInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static volatile int port = -1;

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
				applicationContext.getEnvironment(),
				"spring.data.mongodb.port=" + ensureStarted());
	}

	private static synchronized int ensureStarted() {
		if (port == -1) {
			TransitionWalker.ReachedState<RunningMongodProcess> running =
					Mongod.instance().start(Version.Main.V6_0);
			port = running.current().getServerAddress().getPort();
			Runtime.getRuntime().addShutdownHook(new Thread(running::close));
		}
		return port;
	}
}
