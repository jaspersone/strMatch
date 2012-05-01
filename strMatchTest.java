import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
/**
 * @author jasper
 *
 */
public class strMatchTest {	

    /**
     * Test getMyPhrase for each subclass of Match
     */
    @Test
    public void testGetMyPhrase() {
        strMatch.BruteForceMatch    bf  = new strMatch.BruteForceMatch();
        strMatch.RabinKarpMatch     rk  = new strMatch.RabinKarpMatch();
        strMatch.KMPMatch           kmp = new strMatch.KMPMatch();
        strMatch.BMooreMatch        bm  = new strMatch.BMooreMatch();
        strMatch.Match []       matches = {bf, rk, kmp, bm};
        String [] expectedResults       = {"BF ", "RK ", "KMP ", "BM "};
        for (int i = 0; i < matches.length; i++) {
            String tempTrue             = matches[i].getMyPhrase(true);
            String tempFalse            = matches[i].getMyPhrase(false);
            String expectedResultTrue   = expectedResults[i] + "MATCHED: ";
            String expectedResultFalse   = expectedResults[i] + "FAILED: ";
            assertEquals(expectedResultTrue, tempTrue);
            assertEquals(expectedResultFalse, tempFalse);
        }
        
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
		int[] testResult = strMatch.buildCoreTable3(pattern);
		int[] expectedResult = {0, 0, 0, 0, 0, 0, 0, 1, 2, 3};
		assertEquals(testResult.length, expectedResult.length);
		for (int i = 0; i < expectedResult.length; i++) {
			assertEquals(expectedResult[i], testResult[i]);
		}
	}

	/**
	 * Test method for getNextChunkCountBytes(int chunkCount, byte[] source, int leftPoint)
	 * Try to grab first word
	 */
	@Test
	public void testGetNextChunkCountBytes_frontOfArray() {
		String test = "There was a little black horse, which loved to run through the forest.";
		byte[] testArray = test.getBytes();
		String testResult = strMatch.getNextChunkCountBytes(5, testArray, 0);
		String expectedResult = "There";
		assertEquals(expectedResult, testResult);
	}
	
	/**
	 * Test method for getNextChunkCountBytes(int chunkCount, byte[] source, int leftPoint)
	 * Try to grab first word
	 */
	@Test
	public void testGetNextChunkCountBytes_backOfArray() {
		String test = "There was a little black horse, which loved to run through the forest.";
		byte[] testArray = test.getBytes();
		String testResult = strMatch.getNextChunkCountBytes(7, testArray, 63);
		String expectedResult = "forest.";
		assertEquals(expectedResult, testResult);
	}
	
	/**
	 * Test method for getNextChunkCountBytes(int chunkCount, byte[] source, int leftPoint)
	 * Try to grab first word
	 */
	@Test
	public void testGetNextChunkCountBytes_chunkCountOverLengthOfArray() {
		String test = "There was a little black horse, which loved to run through the forest.";
		byte[] testArray = test.getBytes();
		String testResult = strMatch.getNextChunkCountBytes(23, testArray, 63);
		String expectedResult = "forest.";
		assertEquals(expectedResult, testResult);
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

		for (int i = 0; i < testCount; i++) {
//			System.out.println("brute force test: " + i);
			found = strMatch.bruteForceMatch(pattern, sourceArr);
			assertEquals(false, found);
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
		for (int i = 0; i < testCount; i++) {
//			System.out.println("brute force test: " + i);
			found = strMatch.rabinKarpMatchSUM(pattern, sourceArr);
			assertEquals(false, found);
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
		for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
			found = strMatch.kmpMatch(pattern, sourceArr);
			assertEquals(false, found);
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

		for (int i = 0; i < testCount; i++) {
//			System.out.println("brute force test: " + i);
			found = strMatch.bmooreMatch(pattern, sourceArr);
			assertEquals(false, found);
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
		for (int i = 0; i < testCount; i++) {
//				System.out.println("brute force test: " + i);
			found = strMatch.bmooreMatch(pattern, sourceArr);
			assertEquals(false, found);
		}
		s.stop();
		System.out.println("Average time for Boyer-Moore Match 1: " + (s.time() / testCount));
	}
}
