package ch.bbw.m450.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Smoke tests to confirm that JUnit 5 and AssertJ are wired up correctly in the build.
 * Not testing any TicTacToe logic on purpose.
 */
class DummyTest {

	@Test
	void dummyJUnitTest() {
		var sum = 1 + 1;
		assertTrue(sum == 2);
	}

	@Test
	void dummyAssertJTest() {
		var sum = 1 + 1;
		assertThat(sum).isEqualTo(2);
	}
}
