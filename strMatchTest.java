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
	 * Test method for getKMPSubStrings(String)
	 */
	@Test
	public void testGetKMPSubStrings_generalString() {
		String pattern = "help";
		String[] testResult = strMatch.getKMPSubStrings(pattern);
		String[] expectedResult = {"", "h", "he", "hel","help"};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(testResult[i], expectedResult[i]);
		}
	}

	/**
	 * Test method for getKMPSubStrings(String)
	 */
	@Test
	public void testGetKMPSubStrings_emptyString() {
		String pattern = "";
		String[] testResult = strMatch.getKMPSubStrings(pattern);
		String[] expectedResult = {""};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(testResult[i], expectedResult[i]);
		}
	}

	/**
	 * Test method for getKMPSubStrings(String)
	 */
	@Test
	public void testGetKMPSubStrings_crazyString() {
		String pattern = "asuer9q2ur12nushv zkjfhasre9";
		String[] testResult = strMatch.getKMPSubStrings(pattern);
		String[] expectedResult = {"", "a", "as", "asu", "asue", "asuer", "asuer9",
								   "asuer9q", "asuer9q2", "asuer9q2u", "asuer9q2ur",
								   "asuer9q2ur1", "asuer9q2ur12", "asuer9q2ur12n",
								   "asuer9q2ur12nu", "asuer9q2ur12nus", "asuer9q2ur12nush",
								   "asuer9q2ur12nushv", "asuer9q2ur12nushv ",
								   "asuer9q2ur12nushv z", "asuer9q2ur12nushv zk",
								   "asuer9q2ur12nushv zkj", "asuer9q2ur12nushv zkjf",
								   "asuer9q2ur12nushv zkjfh", "asuer9q2ur12nushv zkjfha",
								   "asuer9q2ur12nushv zkjfhas", "asuer9q2ur12nushv zkjfhasr",
								   "asuer9q2ur12nushv zkjfhasre", "asuer9q2ur12nushv zkjfhasre9"};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(testResult[i], expectedResult[i]);
		}
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_emptyString() {
		String substring = "";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_singleCharString() {
		String substring = "a";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic1() {
		String substring = "ab";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic2() {
		String substring = "aba";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "a";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic3() {
		String substring = "abab";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "ab";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic4() {
		String substring = "ababa";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "aba";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic5() {
		String substring = "ababac";
		String testResult = strMatch.getCore(substring);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for buildCoreTable(String)
	 */
	@Test
	public void testBuildCoreTable_emptyPattern() {
		String pattern = "";
		int[] testResult = strMatch.buildCoreTable(pattern);
		int[] expectedResult = {0};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(testResult[i], expectedResult[i]);
		}
	}

	/**
	 * Test method for buildCoreTable(String)
	 */
	@Test
	public void testBuildCoreTable_singleCharPattern() {
		String pattern = "a";
		int[] testResult = strMatch.buildCoreTable(pattern);
		int[] expectedResult = {0, 0};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(testResult[i], expectedResult[i]);
		}
	}

	/**
	 * Test method for buildCoreTable(String)
	 */
	@Test
	public void testBuildCoreTable_basicPattern1() {
		String pattern = "ababac";
		int[] testResult = strMatch.buildCoreTable(pattern);
		int[] expectedResult = {0, 0, 0, 1, 2, 3, 0};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(testResult[i], expectedResult[i]);
		}
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
