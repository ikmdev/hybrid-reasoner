package dev.ikm.reasoner.hybrid;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

	private static final Logger log = LoggerFactory.getLogger(LogTest.class);

	@Test
	public void log() throws Exception {
		log.info("Logging...");
		String uuid = UUID.randomUUID().toString();
		log.info(uuid);
		List<String> lines = Files.readAllLines(Paths.get("logs", "log.txt"));
		assertTrue(lines.stream().anyMatch(x -> x.contains(uuid)));
		log.info("Done");
	}

}
