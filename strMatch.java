import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class strMatch {
	static boolean TESTING = false;
	
	// static variable so our chunk function can notice the last byte of
	// the previous chunk, for the 0x0D,0x0A Windows newline pattern.
	static byte prevByte = 0x00;	

	/**
	 * Grabs the first chunk of chars from the source file to kick off several types
	 * of string matching algorithms
	 * 
	 * Arguments:
	 * @param chunkCount - the size of the chunk that needs to be grabbed 
	 * @param source - the source file from which to grab chunks
	 * @return a string, of the first scope of length chunkCount
	 */
	protected static String getNextChunkCountChars(int chunkCount, DataInputStream source)
	{
		assert(chunkCount > 0);
		String	scope		= "";

		try {
			for (int i = 0; i < chunkCount; i++) {
				byte currentByte = source.readByte();
				byte byteToAdd = currentByte; // assume you want to add current byte
				// Windows uses two characters to represent a text 
				// newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
				// itself, so convert 0x0D to 0x0A, and absorb
				// trailing 0x0A if this is a windows newline encoding
				//
				if (currentByte == 0x0D) {
					scope += (char)0x0A;
				} else if ((prevByte == 0x0D) && (currentByte == 0x0A)) {
					// Absorb this iteration...
					i--;
				} else {
					scope += (char)currentByte;
				}

				prevByte = currentByte;
			}
			// TODO Auto-generated catch block
		} catch (EOFException fu) {
			if (TESTING)
				System.out.println("Got to end of file without finding pattern");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return scope;
	}

	/**
	 * 
	 * @param pattern
	 * @param source
	 * @return
	 */
	protected static boolean bruteForceMatch(String pattern, DataInputStream source)
	{
		if (TESTING) {
			System.out.println(">>> Brute Force Pattern Match <<<");
		}
		int     chunkCount = pattern.length();
		boolean patternFound = false;
		String  scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer

		// Reset our prevByte for our stream parser.
		prevByte = 0x00;

		// go through the source and search for the pattern
		while(!patternFound) {
			// compare scope so far
			int i = 0;
			while (i < chunkCount) {
				if (pattern.charAt(i) != scope.charAt(i))
					break;
				i++;
			}
			if (i == chunkCount) {
				if (TESTING) {
					System.out.println("PATTERN FOUND!!! YES!!!");
				}
				patternFound = true;
			}
			// try to shift the scope
			try {
				byte readByte = source.readByte();
				byte b = readByte;

				// Windows uses two characters to represent a text 
				// newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
				// itself, so convert 0x0D to 0x0A, and absorb
				// trailing 0x0A if this is a windows newline encoding
				//
				if (b == 0x0D) { 
					scope = scope.substring(1) + (char)0x0A;
				} else if ((prevByte == 0x0D) && (b == 0x0A)) {
					// We skip this byte and compare against a non-modified scope
				} else {
					scope = scope.substring(1) + (char)b;
				}
				prevByte = readByte;
				assert(scope.length() == pattern.length());
			} catch (EOFException fu) {
				if (TESTING) {
					System.out.println("Last chance to find the pattern");
				}
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} 
		}
		return patternFound;
	}

	/**
	 * Simple Rabin-Karp pattern matching using summation algorithm.  Since
	 * we are using longs, the maximum sum we can have is 256+256+...+256 for
	 * 2^55 times, and we can assume that this data set is intractable for 
	 * computational time.  
	 * 
	 * @param pattern
	 * @param source
	 * @return
	 */
	protected static boolean rabinKarpMatch(String pattern, DataInputStream source)
	{
		long srcHash = 0;
		long patHash = 0;

		// Reset prevByte for our stream parser.
		prevByte = 0x00;

		// Generate the hash value for the pattern
		for (int i = 0; i < pattern.length(); i++) {
			byte b = (byte)pattern.charAt(i);
			patHash += (long)b & 0xff;
		}

		int chunkCount = pattern.length();
		boolean patternFound = false;
		String scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer

		if (TESTING) {
			System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");
		}

		// Generate the hash value for the scope
		for (int i = 0; i < scope.length(); i++) {
			byte b = (byte)scope.charAt(i);

			srcHash += (long)b & 0xff;
			assert((long)b >= 0);
		}

		if (TESTING) {
			System.out.println("scope=" + scope);
			System.out.println("Expected srcHash=" + srcHash);
		}

		//prevByte = ;

		// go through the source and search for the pattern
		while(!patternFound) {
			// compare scope so far
			if (srcHash == patHash) {
				int i = 0;

				// Do a comparison of the actual strings
				while (i < chunkCount) {
					if (pattern.charAt(i) != scope.charAt(i))
						break;
					i++;
				}

				if (i == chunkCount) {
					if (TESTING) {
						System.out.println("PATTERN FOUND!!! YES!!!");
					}
					patternFound = true;
				}
			}

			// try to shift the scope
			try {
				byte readByte = source.readByte();
				byte b = readByte;

				// Windows uses two characters to represent a text 
				// newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
				// itself, so convert 0x0A to 0x0D, and absorb
				// trailing 0x0A if this is a windows newline encoding
				//
				if (b == 0x0D) {
					srcHash -= (long)scope.charAt(0) & 0xff;
					scope = scope.substring(1) + (char)0x0A;
					prevByte = readByte;
					assert(scope.length() == pattern.length());
					srcHash += (long)0x0A;
				} else if ((prevByte == 0x0D) && (b == 0x0A)) {
					// We skip this byte and compare against a non-modified scope
				} else {
					srcHash -= (long)scope.charAt(0) & 0xff;
					scope = scope.substring(1) + (char)b;
					prevByte = readByte;
					assert(scope.length() == pattern.length());
					srcHash += (long)b & 0xff;
				}
			} catch (EOFException fu) {
				if (TESTING) {
					System.out.println("Last chance to find the pattern");
				}
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} 
		}
		return patternFound;
	}

	/**
	 * 
	 * @param pattern
	 * @return
	 */
	protected static String[] getKMPSubStrings(String pattern) {
		String[] substrings = new String[pattern.length() + 1];
		// build array of strings that contain substrings
		for (int i = 0; i <= pattern.length(); i++) {
			substrings[i] = pattern.substring(0, i);
		}
		return substrings;
	}

	/**
	 * Given a string s, returns the longest core it can find (This is just for testing)
	 * @param s - string to search for a core within
	 * @return a substring of the search string s, of which is the longest pre/suffix
	 */
	protected static String getCoreTest(String s, String[] substrings) {
		String core = "";
		for (int i = 1; i < s.length(); i++) {
			String tempCore = s.substring(0, i);
			String tempS = s.substring(s.length() - i);
			if (tempCore.equals(tempS) && tempS.length() != s.length()) {
				core = tempCore;
			}
		}
		return core;
	}

	/**
	 * Builds a table which contains a list of numbers which correspond to
	 * the length of the max core size of each substring of pattern
	 * @param pattern - the string to build the core table from
	 * @return an array of integers which indicate the length of each substring's core
	 */
	protected static int[] buildCoreTable(String pattern) {
		String[] substrings = getKMPSubStrings(pattern);
		int[] table = new int[substrings.length];
		for (int i = 0; i < substrings.length; i++) {
			table[i] = getCoreTest(substrings[i], substrings).length();
		}
		return table;
	}

	/**
	 * Iterative core building functionality that is O(3m) time,
	 * where m is the length of the pattern.
	 *
	 * Portions modeled after Boyer-Moore algorithm described on 
	 * p209 of "Theory in Programmig Practice", Misra
	 * 
	 * @param p Byte array containing our pattern.
	 * @param rt Integer array of an offset from the left of the pattern.
	 *           Size of the array is assumed to be 256 entries, and is 
	 *           initialized by the caller to all entries equal -1.
	 * @return An array representing b(s), the precomputed jump table
	 *         for the good suffix heurisitic.
	 */
	protected static int[] buildCoreTable2(byte[] p, int[] rt) {
		// The pattern length is the size of the table, since each core
		// calculation involves the next symbol in p[i+direction]
		assert(rt.length == 256);
		int[] table = new int[p.length+1];
		int[] b = new int[p.length+1];
		int   i = p.length-1;
		int   m = 0;
		int   vLength = 1;
		int   coreLength = 0;

		// Start with the first value
		while (m < p.length) {
			// If length of v is enough to generate a proper
			// prefix and suffix, determine if we have a core.
			if (vLength > 1) {
				//
				// Match ..xa 
				// if x==a then core is coreLength+1.
				// else coreLength = 0, since we cannot match
				//                      a proper prefix and suffix
				//
				// This is explicitly for suffix, since index is 
				// p.length-1-coreLength.  For prefix, you would need
				// vLength - coreLength..
				if (p[i] == p[p.length - 1 - coreLength]) {
					table[vLength] = ++coreLength;
				} else {
					coreLength = 0;
				}
			}

			if (TESTING) {
				String myDumbString = new String("");
				for (int k = 0; k < vLength; k++)
					myDumbString += (char)p[i + k];
				System.out.println("Substring=" + myDumbString + " coreLength=" + coreLength);
			}
			// 
			// Bad Symbol Heuristic
			//
			// Map the offset of this byte to the rightmost offset
			// in the pattern.
			//
			rt[p[i]] = Math.max(rt[p[i]], i);

			// Length of the substring of p
			vLength++;
			i--;
			m++;
		}

		// 
		// Good Suffix Heuristic
		//
		// Default elements of b to m-|c(p)|
		//
		for (int j = 0; j < m+1; j++) {
			b[j] = m - table[m];
		}

		if (TESTING) {
			System.out.println("Core Table=" + Arrays.toString(table));
			System.out.println("b=" + Arrays.toString(b));
		}
		//
		// Third pass modeled after algorithm described on pg 209
		// of "Theory in Programming Practice", Misra
		// 
		// i := |c(v)|
		// if b[j-i] > j-i then b[j-i] := j-i.
		//
		// A single character comparison will always have a core
		// value of epsilon, therefore b[0]=|v|-|epsilon|= 1
		for (int j = 1; j < m+1; j++) {
			i = table[j];
			b[i] = Math.min(b[i], j-i);
		}
		if (TESTING) {
			System.out.println("bFinal=" + Arrays.toString(b));
		}
		return b;
	}

	/**
	 * Iterative core building functionality that is O(3m) time,
	 * where m is the length of the pattern.
	 *
	 * Portions modeled after Boyer-Moore algorithm described on 
	 * p209 of "Theory in Programmig Practice", Misra
	 * 
	 * @param p Byte array containing our pattern.
	 * @return An array representing core table lengths
	 */
	protected static int[] buildCoreTable3(byte[] p) {
		// The pattern length is the size of the table, since each core
		// calculation involves the next symbol in p[i+direction]

		int[] table = new int[p.length+1];
		int   i = 0;
		int   coreLength = 0;

		table[0] = 0; // epsilon length value = 0
		// Start with the first value
		while (i < p.length) {
			// If length of v is enough to generate a proper
			// prefix and suffix, determine if we have a core.
			if (i > 1) {
				//
				// Match ..xa 
				// if x==a then core is coreLength+1.
				// else coreLength = 0, since we cannot match
				//                      a proper prefix and suffix
				//
				// This is explicitly for suffix, since index is 
				// p.length-1-coreLength.  For prefix, you would need
				// vLength - coreLength..
				if (p[coreLength] == p[i]) {
					table[i+1] = ++coreLength;
				} else {
					coreLength = 0;
				}
			} else {
				table[i] = 0; // default length of core for sizes <= 1
			}        
			if (TESTING) {
				String myDumbString = new String("");
				for (int k = 0; k <= i; k++)
					myDumbString += (char)p[k];
				System.out.println("Substring=" + myDumbString + " coreLength=" + coreLength);
			}
			i++;
		}

		return table;
	}

	/**
	 * Implements the Knuth-Morris-Pratt algorithm as described
	 * in CS337, Eberlein and "Theory in Programming Practice", Misra.
	 *
	 * @param pattern Input pattern to search for.
	 * @param source Source text that we will traverse for a pattern match.
	 * @return TRUE if pattern found, FALSE if pattern not found.
	 */ 
	protected static boolean kmpMatch(String pattern, DataInputStream source)
	{
		if (TESTING) {
			System.out.println(">>> Knuth-Morris-Pratt Pattern Match <<<");
		}
		int     chunkCount = pattern.length();
		int		bytesToGrab = 0;
		boolean patternFound = false;
		String  scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer
		int[]	c = buildCoreTable3(pattern.getBytes());

		// Reset prevByte for our stream parser.
		prevByte = 0x00;

		// note that the left most will always be 0 in our version
		// t = scope
		// r = right index
		// l = left index (always 0 in our implementation)
		// p = pattern

		while (!patternFound) {
			int r;
			for (r = 0; r < chunkCount; r++) {
				// Case 1: t[r] = p[r-l]
				// nothing to do, because this loop will iterate r

				if (pattern.charAt(r) != scope.charAt(r)) {
					// Case 2: t[r] != p[r-l] and r = l
					if (r == 0) {
						bytesToGrab = 1;
					} else { // Case 3: t[r] != p[r-l] and r > l
						bytesToGrab = r - c[r];
					}
					break; // early break out of the loop because no match
				}
			}
			// if we made it through the loop and everything matches
			if (r == chunkCount) {
				patternFound = true;
				if (TESTING) {
					System.out.println("PATTERN FOUND!!! YES!!!");
				}
			} else { // something didn't match, so get next scope
				scope = scope.substring(bytesToGrab) + getNextChunkCountChars(bytesToGrab, source);
				if (scope.length() != chunkCount) {
					return false;
				}
			}
		}
		return patternFound;
	}

	/**
	 * Implementation of Boyer-Moore pattern matching algorithm
	 * as described in CS337, Eberlein and 
	 * "Theory in Programming Practice", Misra.
	 *
	 * @param pattern Input pattern to search for.
	 * @param source Source text that we will traverse for a pattern match.
	 * @return TRUE if pattern found, FALSE if pattern not found.
	 */ 
	protected static boolean bmooreMatch(String pattern, DataInputStream source)
	{
		if (TESTING) {
			System.out.println(">>> Boyer-Moore Pattern Match <<<");
		}
		int     chunkCount = pattern.length();
		if (chunkCount <= 0) return true; 
		int[] rt = new int[256];
		for(int i = 0; i < rt.length; i++)
			rt[i] = -1; // initialize rt with all -1 values (flags)
		int[] b = buildCoreTable2(pattern.getBytes(), rt);
		int		bytesToGrab = 0;
		boolean patternFound = false;
		String scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer

		// Reset our prevByte for our stream parser.
		prevByte = 0x00;

		// note that the left most will always be 0 in our version
		// t = scope
		// r = right index
		// l = left index (always 0 in our implementation)
		// p = pattern
		// Q1: Every occurrence of p in t that begins before index l 
		//     has been previously found.
		// Q2: 0²j²chunkCount,p[j..chunkCount]=t[l+j..l+chunkCount]

		while (!patternFound) {
			int j;
			for (j = chunkCount; j > 0; j--) {
				// Case 1: j > 0 ^ p[j-1] = t[l+j-1]
				// nothing to do, because this loop will iterate r

				if (pattern.charAt(j-1) != scope.charAt(j-1)) {
					// Case 2: Q1 ^ Q2 ^ (j == 0 v p[j-1] != t[l+j-1])
					int badSymbolHeuristic = 0;
					int goodSuffixHeuristic = 0;

					// Since j is the rightmost index into the pattern,
					// it reflects the b(s) for the suffix starting at 
					// j.
					goodSuffixHeuristic = b[chunkCount-j];
					badSymbolHeuristic = j - 1 - rt[(int) scope.charAt(j-1) & 0xff];

					// get good suffix heuristic value	    				

					bytesToGrab = Math.max(badSymbolHeuristic, goodSuffixHeuristic);

					if (TESTING) {
						System.out.println("Scope: " + scope);
						System.out.println("badSH= " + badSymbolHeuristic + " goodSH= " + goodSuffixHeuristic);

						System.out.println("About to break");
					}
					break; // early break out of the loop because no match
				}
			}
			// if we made it through the loop and everything matches
			if (j <= 0) {
				patternFound = true;
				if (TESTING) {
					System.out.println("PATTERN FOUND!!! YES!!!");
				}
			} else { // something didn't match, so get next scope
				scope = scope.substring(bytesToGrab) + getNextChunkCountChars(bytesToGrab, source);
				if (scope.length() != chunkCount) {
					return false;
				}
			}
		}
		return patternFound;
	}

	protected static void runExperiments(String patternFileName, String sourceFileName, String outputFileName) {
		boolean patternEofFound = false;
		boolean TESTING = false;

		// Get file set up
		try {
			FileInputStream pinput = new FileInputStream(patternFileName);
			DataInputStream p = new DataInputStream(pinput);
			FileInputStream sinput = new FileInputStream(sourceFileName);
			DataInputStream s = new DataInputStream(sinput);
			FileOutputStream outFile = new FileOutputStream(outputFileName);

			// Outer loop over all patterns in the pattern file
			while (!patternEofFound) {
				boolean sourceEofFound = false;
				int     patternAmpCount = 0;
				String  strPattern = new String("");
				StringBuilder strTmp = new StringBuilder("");

				// Reset our prevByte for our stream parser
				prevByte = 0x00;

				// Read in the pattern from the pattern file.  Per the 
				// assignment documentation, the pattern is surrounded by
				// '&'
				while ((patternAmpCount < 2) && (patternEofFound == false)) {
					try {
						byte b = p.readByte();

						if (b == '&') {
							patternAmpCount++;
						} else if (patternAmpCount > 0) {
							// Windows uses two characters to represent a text 
							// newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
							// itself, so convert 0x0D to 0x0A, and absorb
							// trailing 0x0A if this is a windows newline encoding
							//
							if (b == 0x0D) { 
								strTmp.append((char)0x0A);
							} else if ((prevByte == 0x0D) && (b == 0x0A)) {
								// We skip this byte and compare against a non-modified scope
							} else { 
								strTmp.append((char)b);
							}
						}

						prevByte = b;

					} catch (EOFException fu) {
						patternEofFound = true;
					}
				}

				// Now we have the pattern, so store it in the strPattern
				strPattern = strTmp.toString();

				if (strPattern.length() > 0) { // do nothing if the string is empty
					if (TESTING) {
						System.out.println("***************************************");
						System.out.println("* Search for '" + strPattern + "'");
						System.out.println("***************************************");
					}

					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 

					// Setup output
					String output = "";

					// Brute Force String Matching algorithm
					if (bruteForceMatch(strPattern, s))
						output = "BF MATCHED: " + strPattern;
					else
						output = "BF FAILED: " + strPattern;
					if (TESTING) System.out.println(output);
					outFile.write((output + "\n").getBytes());

					s.close();
					sinput.close();

					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 

					// Rabin-Karp algorithm
					if (rabinKarpMatch(strPattern, s))
						output = "RK MATCHED: " + strPattern;
					else
						output = "RK FAILED: " + strPattern;
					if (TESTING) System.out.println(output);
					outFile.write((output + "\n").getBytes());

					s.close();
					sinput.close();

					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 

					// Knuth-Morris-Pratt algorithm
					if (kmpMatch(strPattern, s))
						output = "KMP MATCHED: " + strPattern;
					else
						output = "KMP FAILED: " + strPattern;
					if (TESTING) System.out.println(output);
					outFile.write((output + "\n").getBytes());

					s.close();
					sinput.close();

					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 

					// Boyer-Moore algorithm
					if (bmooreMatch(strPattern, s))
						output = "BM MATCHED: " + strPattern;
					else
						output = "BM FAILED: " + strPattern;
					if (TESTING) System.out.println(output);
					outFile.write((output + "\n").getBytes());

					s.close();
					sinput.close();
				}
			} // LOOP to next pattern

			// close output file
			outFile.flush();
			outFile.close();

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
	 * @param args
	 */
	public static void main(String[] args) {
		String patternFileName = args[0];
		String sourceFileName = args[1];
		String outputFileName = args[2];
		runExperiments(patternFileName, sourceFileName, outputFileName);
	}

}
