package com.github.chriskn.jsonconsolelogger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class JsonConsoleLoggerTest {

	@Test
	public void testDoMainValid() {
		JsonConsoleLogger l = new SilentJsonConsoleLogger();
		assertThat(l.doMain(null), is(JsonConsoleLogger.RC_OK));
		assertThat(l.doMain(new String[] {}), is(JsonConsoleLogger.RC_OK));
		assertThat(l.doMain(new String[] { "4444" }), is(JsonConsoleLogger.RC_OK));
		assertThat(l.doMain(new String[] { "4443" }), is(JsonConsoleLogger.RC_OK));
	}

	@Test
	public void testDoMainUsage() {
		JsonConsoleLogger l = new SilentJsonConsoleLogger();
		assertThat(l.doMain(new String[] { "" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "-h" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "-H" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "--help" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "--HELP" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "-h", "4444" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "4ABC" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "A123" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "-1" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "-2" }), is(JsonConsoleLogger.RC_USAGE));
		assertThat(l.doMain(new String[] { "65536" }), is(JsonConsoleLogger.RC_USAGE));
	}

	// subclass to avoid creating a "real" server for tests
	public static class SilentJsonConsoleLogger extends JsonConsoleLogger {
		protected Thread createServer(int port) throws Exception {
			return new Thread();
		}
	}
}
