import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_singleCharString() {
		String substring = "a";
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic1() {
		String substring = "ab";
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic2() {
		String substring = "aba";
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "a";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic3() {
		String substring = "abab";
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "ab";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic4() {
		String substring = "ababa";
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "aba";
		assertEquals(testResult, expectedResult);
	}
	
	/**
	 * Test method for getCore(String)
	 */
	@Test
	public void testGetCore_basic5() {
		String substring = "ababac";
		String[] substrings = strMatch.getKMPSubStrings("ababac");
		String testResult = strMatch.getCoreTest(substring, substrings);
		String expectedResult = "";
		assertEquals(testResult, expectedResult);
	}

	/**
	 * Test method for buildCoreTable2(String)
	 */
	@Test
	public void testBuildCoreTable2_basicPattern2() {
		String pattern = new String("xabcabxab");
		int[] rt = new int[256];
		for (int i = 0; i < 256; i++)
			rt[i] = -1;
		
		int[] testResult = strMatch.buildCoreTable2(pattern.getBytes(), rt);
		int[] expectedResult = {1, 3, 3, 6, 6, 6, 6, 6, 6, 6};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(expectedResult[i], testResult[i]);
		}
	}

	/**
	 * Test method for buildCoreTable2(String)
	 */
	@Test
	public void testBuildCoreTable3_basicPattern1() {
		String pattern = new String("xabcabxab");
		int[] testResult = strMatch.buildCoreTable3(pattern.getBytes());
		int[] expectedResult = {0, 0, 0, 0, 0, 0, 0, 1, 2, 3};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(expectedResult[i], testResult[i]);
		}
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
	 * Test method for main(java.lang.String[])
	 */
	@Test
	public void testRunExperiments_simple_newline() {
		String pattern = "pattern.txt";
		String source = "02_exodus.txt";
		String outputFileName = "test_output.txt";
		String expectedFileName = "expected_output.txt";
		strMatch.runExperiments(pattern, source, outputFileName);
		
		try {
			FileInputStream outputFile = new FileInputStream(outputFileName);
			FileInputStream expectedFile = new FileInputStream(expectedFileName);
			
			int testResult = outputFile.read();
			int expectedResult = expectedFile.read();
			while (testResult != -1 && expectedResult != -1) {
				assertEquals(expectedResult, testResult);
				testResult = outputFile.read();
				expectedResult = expectedFile.read();
			}
		} catch (FileNotFoundException ex) {
			// TODO Auto-generated catch block
			System.out.println("There was an error opening the file " + ex);
			ex.printStackTrace();
		} catch (IOException r) {
			// TODO Auto-generated catch block
			System.out.println("IO Exception occurred while accessing f.");
			r.printStackTrace();
		}
		
		assertTrue(true);
	}

	/**
	 * Test method for main(java.lang.String[])
	 */
	@Test
	public void testRunExperiments_funky_newlines() {
		String pattern = "pattern.txt";
		String source = "01_genesis.txt";
		String outputFileName = "test_output_genesis.txt";
		String expectedFileName = "expected_output_genesis.txt";
		strMatch.runExperiments(pattern, source, outputFileName);
		
		try {
			FileInputStream outputFile = new FileInputStream(outputFileName);
			FileInputStream expectedFile = new FileInputStream(expectedFileName);
			
			int testResult = outputFile.read();
			int expectedResult = expectedFile.read();
			while (testResult != -1 && expectedResult != -1) {
				assertEquals(expectedResult, testResult);
				testResult = outputFile.read();
				expectedResult = expectedFile.read();
			}
		} catch (FileNotFoundException ex) {
			// TODO Auto-generated catch block
			System.out.println("There was an error opening the file " + ex);
			ex.printStackTrace();
		} catch (IOException r) {
			// TODO Auto-generated catch block
			System.out.println("IO Exception occurred while accessing f.");
			r.printStackTrace();
		}
		
		assertTrue(true);
	}
	
	/**
	 * Testing Timing: Brute Force bruteForceMatch(String pattern, DataInputStream source)
	 */
	@Test
	public void testBruteForceMatch_largePatternNoMatch() {
		Stopwatch s = new Stopwatch();
		String pattern = "12345678";
		StringBuilder source = new StringBuilder("");
		boolean found = false;
		int testCount = 100;
		int sourceSize = 1048576;
		for (int i = 0; i < sourceSize; i++)
			source.append("a");
		
		byte[] sourceArr = source.toString().getBytes();
		
		s.start();
		try {
			for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
				ByteArrayInputStream bstream = new ByteArrayInputStream(sourceArr);		
				DataInputStream sourceStream = new DataInputStream(bstream);
				found = strMatch.bruteForceMatch(pattern, sourceStream);
				sourceStream.close();
				assertEquals(false, found);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Average time for Brute Force 1: " + (s.time() / testCount));
	}
	
	/**
	 * Testing Timing: Rabin Karp rabinKarpMatch(String pattern, DataInputStream source)
	 */
	@Test
	public void testRabinKarpMatch_largePatternNoMatch() {
		Stopwatch s = new Stopwatch();
		String pattern = "12345678";
		StringBuilder source = new StringBuilder("");
		boolean found = false;
		int testCount = 100;
		int sourceSize = 1048576;
		for (int i = 0; i < sourceSize; i++)
			source.append("a");
		
		byte[] sourceArr = source.toString().getBytes();
		
		s.start();
		try {
			for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
				ByteArrayInputStream bstream = new ByteArrayInputStream(sourceArr);		
				DataInputStream sourceStream = new DataInputStream(bstream);
				found = strMatch.rabinKarpMatch(pattern, sourceStream);
				sourceStream.close();
				assertEquals(false, found);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Average time for Rabin-Karp 1: " + (s.time() / testCount));
	}
	
	/**
	 * Testing Timing: KMP Match kmpMatch(String pattern, DataInputStream source)
	 */
	@Test
	public void testKmpMatch_largePatternNoMatch() {
		Stopwatch s = new Stopwatch();
		String pattern = "12345678";
		StringBuilder source = new StringBuilder("");
		boolean found = false;
		int testCount = 100;
		int sourceSize = 1048576;
		for (int i = 0; i < sourceSize; i++)
			source.append("a");
		
		byte[] sourceArr = source.toString().getBytes();
		
		s.start();
		try {
			for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
				ByteArrayInputStream bstream = new ByteArrayInputStream(sourceArr);		
				DataInputStream sourceStream = new DataInputStream(bstream);
				found = strMatch.kmpMatch(pattern, sourceStream);
				sourceStream.close();
				assertEquals(false, found);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Average time for KMP Match 1: " + (s.time() / testCount));
	}
	
	/**
	 * Testing Timing: Boyer-Moore bmooreMatch(String pattern, DataInputStream source)
	 */
	@Test
	public void testBoyerMoore_largePatternNoMatch() {
		Stopwatch s = new Stopwatch();
		String pattern = "12345678";
		StringBuilder source = new StringBuilder("");
		boolean found = false;
		int testCount = 100;
		int sourceSize = 1048576;
		for (int i = 0; i < sourceSize; i++)
			source.append("a");
		
		byte[] sourceArr = source.toString().getBytes();
		
		s.start();
		try {
			for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
				ByteArrayInputStream bstream = new ByteArrayInputStream(sourceArr);		
				DataInputStream sourceStream = new DataInputStream(bstream);
				found = strMatch.bmooreMatch(pattern, sourceStream);
				sourceStream.close();
				assertEquals(false, found);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Average time for Boyer-Moore Match 1: " + (s.time() / testCount));
	}
	
	public void testMatchGeneral(String pattern, int testCount, int sourceSize, String sourceChars) {
		Stopwatch s = new Stopwatch();
		StringBuilder source = new StringBuilder("");
		boolean found = false;
		for (int i = 0; i < sourceSize; i++)
			source.append(sourceChars);
		
		byte[] sourceArr = source.toString().getBytes();
		
		s.start();
		try {
			for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
				ByteArrayInputStream bstream = new ByteArrayInputStream(sourceArr);		
				DataInputStream sourceStream = new DataInputStream(bstream);
				found = strMatch.bmooreMatch(pattern, sourceStream);
				sourceStream.close();
				assertEquals(false, found);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s.stop();
		System.out.println("Average time for Boyer-Moore Match 1: " + (s.time() / testCount));
	}
}
