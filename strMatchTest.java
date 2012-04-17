import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */

/**
 * @author jasper
 *
 */
public class strMatchTest {

	/**
	 * Test method for fastExp(long, long, long)
	 */
	@Test
	public void testFastExp1() {
		long base = 4;
		long expo = 35;
		long mod = 11;
		long testResult = strMatch.fastExp(base, expo, mod);
		long expectedResult = 1;
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for fastExp(long, long, long)
	 */
	@Test
	public void testFastExp2() {
		long base = 12348;
		long expo = 7829;
		long mod = 347;
		long testResult = strMatch.fastExp(base, expo, mod);
		long expectedResult = 198;
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for fastExp(long, long, long)
	 */
	@Test
	public void testFastExp3() {
		long base = 56;
		long expo = 7;
		long mod = 33;
		long testResult = strMatch.fastExp(base, expo, mod);
		long expectedResult = 23;
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for fastExp(long, long, long)
	 */
	@Test
	public void testFastExp4() {
		long base = 0;
		long expo = 0;
		long mod = 1;
		long testResult = strMatch.fastExp(base, expo, mod);
		long expectedResult = 0;
		assertEquals(testResult, expectedResult);
	}

	
	/**
	 * Test method for bruteForceMatch(String, DataInputStream)
	 */
	@Test
	public void testBruteForceMatch() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for rabinKarpMatch(String, DataInputStream)
	 */
	@Test
	public void testRabinKarpMatch() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for kmpMatch(String, DataInputStream)
	 */
	@Test
	public void testKmpMatch() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for bmooreMatch(String, DataInputStream)
	 */
	@Test
	public void testBmooreMatch() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for main(java.lang.String[])
	 */
	@Test
	public void testMain() {
		assertTrue(true);
	}

}
