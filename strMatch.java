import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;


public class strMatch {
	static boolean TESTING = false;

    // begin McClure driving
	/**
     * Helper function for fast exponentiation
     * 
     * a^b (mod n)
     * 
     * Arguments:
     * a - Base of the number, MUST BE NONNEGATIVE
     * b - Exponent of the number, MUST BE NONNEGATIVE
     * n - Modulus for the exponentiation
     *
     * Returns:
     * Result of the calculation a^b (mod n)
     */
    static long fastExp(long a, long b, long n)
    {
        long c = 1, k = 0;

        // Return a bad value if we have bad inputs
        if ((a < 0) || (b < 0))
            return -1;

        // Find the topmost bit location
        while ((1 << k+1) <= b)
            k++;

        // Fast exponentiation, without branching
        while (k >= 0)
        {
            // This is equivalent to:
            //
            // if ((b & (1 << k)) > 0)
            //    c = (((c*c) % n) * a) % n;
            // else
            //    c = ((c*c) % n);
            c = (((c*c) % n) + (((c*c) % n) * ((a-1) * (b & (1 << k)) >> k)) % n) % n;
            k--;
        }

        return c;
    }
    // end McClure driving
    
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
    	byte 	prevByte 	= 0x00;
    	try {
			for (int i = 0; i < chunkCount; i++) {
				byte currentByte = source.readByte();
				byte byteToAdd = currentByte; // assume you want to add current byte
				
	    		// Windows uses two characters to represent a text 
	    		// newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
	    		// itself, so convert 0x0A to 0x0D, and absorb
	    		// trailing 0x0A if this is a windows newline encoding
	    		//
	    		if ((currentByte == 0x0A) && (prevByte == 0x0D)) {
	    			// Ignore
	    			byteToAdd = source.readByte();
	    		} else if ((currentByte == 0x0A) && (prevByte != 0x0D)) {
	    			// Convert all independent 0x0A to 0x0D
	    			byteToAdd = 0x0D;
	    		}
				scope += (char)byteToAdd;
				prevByte = currentByte;
			}
			// TODO Auto-generated catch block
    	} catch (EOFException fu) {
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
    	System.out.println(">>> Brute Force Pattern Match <<<");
    	int     chunkCount = pattern.length();
    	boolean patternFound = false;
    	String  scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer
        byte 	prevByte = 0x00;

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
	    		System.out.println("PATTERN FOUND!!! YES!!!");
				patternFound = true;
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
	    		if ((b == 0x0A) && (prevByte == 0x0D)) {
	    			// Ignore
	    			b = source.readByte();
	    		} else if ((b == 0x0A) && (prevByte != 0x0D)) {
	    			// Convert all independent 0x0A to 0x0D
	    			b = 0x0D;
	    		}
	    		
	    		scope = scope.substring(1) + (char)b;
	    		prevByte = readByte;

	    		assert(scope.length() == pattern.length());
    			
    		} catch (EOFException fu) {
            	System.out.println("Last chance to find the pattern");
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
    	byte prevByte = 0x00;
    	
    	// Generate the hash value for the pattern
    	for (int i = 0; i < pattern.length(); i++) {
    		byte b = (byte)pattern.charAt(i);
    	    patHash += (long)b;
    	}

    	int chunkCount = pattern.length();
    	boolean patternFound = false;
    	String scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer

    	System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");

    	// Generate the hash value for the scope
    	for (int i = 0; i < scope.length(); i++) {
    		byte b = (byte)scope.charAt(i);

    	    srcHash += (long)b;
    		assert((long)b >= 0);
    	}
    	
    	System.out.println("scope=" + scope);
    	System.out.println("Expected srcHash=" + srcHash);

    	//prevByte = ;
    	
    	// go through the source and search for the pattern
    	while(!patternFound) {
    		//System.out.println(scope);
//    		srcHash = 0;
//        	// Generate the hash value for the pattern
//        	for (int i = 0; i < scope.length(); i++) {
//        		byte b = (byte)scope.charAt(i);
//
//        		// Our string is base 256, so each digit is 256^i where
//        		// 0 <= i <= n, where is the length the string.
//        		srcHash <<= 8;
//        		srcHash |= /*fastExp(256, i, 997L)**/(long)b;
//        		assert((long)b >= 0);
//        		//prevByte = b;
//        	}
        	
    		if (pattern.equals(scope) && srcHash != patHash) {
    			System.out.println("HASH srcHash=" + Long.toHexString(srcHash) + " patHash=" + Long.toHexString(patHash));
    		}
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
    	    		System.out.println("PATTERN FOUND!!! YES!!!");
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
	    		if ((b == 0x0A) && (prevByte == 0x0D)) {
	    			// Ignore
	    			b = source.readByte();
	    		} else if ((b == 0x0A) && (prevByte != 0x0D)) {
	    			// Convert all independent 0x0A to 0x0D
	    			b = 0x0D;
	    		}

	    		srcHash -= (long)scope.charAt(0);
	    		
	    		scope = scope.substring(1) + (char)b;
    			prevByte = readByte;
                
    			assert(scope.length() == pattern.length());
    			
        	    srcHash += (long)b;
        	    
    			assert((long)b >= 0);
    		} catch (EOFException fu) {
            	System.out.println("Last chance to find the pattern");
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
     * Given a string s, returns the longest core it can find
     * @param s - string to search for a core within
     * @return a substring of the search string s, of which is the longest pre/suffix
     */
    protected static String getCore(String s) {
    	String core = "";
    	if (s.length() < 2) return core;

    	// only find core for strings of two characters or greater
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
    		table[i] = getCore(substrings[i]).length();
    	}
    	return table;
    }
    
    protected static boolean kmpMatch(String pattern, DataInputStream source)
    {
    	System.out.println(">>> Knuth-Morris-Pratt Pattern Match <<<");
    	int     chunkCount = pattern.length();
    	int		bytesToGrab = 0;
    	boolean patternFound = false;
    	String  scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer
    	int[]	c = buildCoreTable(pattern);
    	
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
				System.out.println("PATTERN FOUND!!! YES!!!");
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
     * Takes a pattern and maps all the chars to the right most index
     * the char is found inside the pattern (indexing starts at 0)
     * @param pattern - the string to find occurance mappings of
     * @return a hashmap of all the available chars in the pattern and a mapping
     * 			to their right most positions in the pattern
     */
    protected static HashMap occurancePatternMap(String pattern) {
    	HashMap<Byte, Integer> result = new HashMap<Byte, Integer>();
    	for (int i = 0; i < pattern.length(); i++) {
    		result.put((byte) pattern.charAt(i), i);
    	}
    	return result;
    }
    
    protected static boolean bmooreMatch(String pattern, DataInputStream source)
    {
    	System.out.println(">>> Boyer-Moore Pattern Match <<<");
    	int     chunkCount = pattern.length();
    	if (chunkCount <= 0) return true; 
    	HashMap<Byte, Integer> occuranceMap = occurancePatternMap(pattern);
    	int		bytesToGrab = 0;
    	boolean patternFound = false;
    	String  scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer
    	
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
	    	for (j = chunkCount - 1; j >= 0; j--) {
	    		// Case 1: j > 0 ^ p[j-1] = t[l+j-1]
	    		// nothing to do, because this loop will iterate r
	    		
	    		if (pattern.charAt(j) != scope.charAt(j)) {
	    			// Case 2: Q1 ^ Q2 ^ (j == 0 v p[j-1] != t[l+j-1])
	    			if (j == 0) {
	    				bytesToGrab = 1;
	    			} else { // Q1^Q2^j>0^p[j-1]!=t[l+j-1]
	    				int badSymbolHeuristic = 0;
	    				int goodSuffixHeuristic = 0;
	    				
	    				// get bad symbol heuristic value
	    				if (occuranceMap.containsKey((byte) scope.charAt(j))) {
	    		    		badSymbolHeuristic = chunkCount - occuranceMap.get((byte) scope.charAt(j));
	    		    	} else {
	    		    		badSymbolHeuristic = j;
	    		    	}
	    				
	    				// get good suffix heuristic value
	    				
	    				
	    				bytesToGrab = Math.max(badSymbolHeuristic, goodSuffixHeuristic);
	    			}
	    			break; // early break out of the loop because no match
	    		}
	    	}
	    	// if we made it through the loop and everything matches
			if (j < 0) {
				patternFound = true;
				System.out.println("PATTERN FOUND!!! YES!!!");
			} else { // something didn't match, so get next scope
				scope = scope.substring(bytesToGrab) + getNextChunkCountChars(bytesToGrab, source);
				if (scope.length() != chunkCount) {
					return false;
				}
			}
    	}
    	return patternFound;
    }

    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

    	String patternFileName = args[0];
		String sourceFileName = args[1];
		String outputFileName = args[2];
		boolean patternEofFound = false;
		boolean TESTING = false;
    	int LIMIT = 2;
		
		// Get file set up
		try {
			FileInputStream pinput = new FileInputStream(patternFileName);
			DataInputStream p = new DataInputStream(pinput);
			FileInputStream sinput = new FileInputStream(sourceFileName);
			DataInputStream s = new DataInputStream(sinput); 

			// Outer loop over all patterns in the pattern file
			while (!patternEofFound) {
				boolean sourceEofFound = false;
				int     patternAmpCount = 0;
				String  strPattern = new String("");
				StringBuilder strTmp = new StringBuilder("");

				// Read in the pattern from the pattern file.  Per the 
				// assignment documentation, the pattern is surrounded by
				// '&'
			    while ((patternAmpCount < 2) && (patternEofFound == false)) {
			        try {
			    		byte b = p.readByte();
			    		
			    		// Windows uses two characters to represent a text 
			    		// newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
			    		// itself, so convert 0x0D to 0x0A, and absorb
			    		// trailing 0x0A if this is a windows newline encoding
			    		//
			    		while (b == 0x0D) {
			    			// If we are recording the pattern, replace
			    			// 0x0D with 0x0A
			    			if (patternAmpCount > 0)
				    			strTmp.append((char)0x0A);

			    			b = p.readByte();

			    			// If this is a windows newline encoding, our 
			    			// next byte will be 0x0A.  Ignore the trailing
			    			// 0x0A and read to the next byte in the stream 
			    			if (b == 0x0A)
			    				b = p.readByte();
			    		}
			    		
			    		if (b == '&') {
			    			patternAmpCount++;
			    		} else if (patternAmpCount > 0) {
			    			strTmp.append((char)b);
			    		}
			        } catch (EOFException fu) {
			        	patternEofFound = true;
			        }
			    }
			    
			    // Now we have the pattern, so store it in the strPattern
			    strPattern = strTmp.toString();
			    			    
			    if (strPattern.length() > 0) { // do nothing if the string is empty
				    System.out.println("***************************************");
				    System.out.println("* Search for '" + strPattern + "'");
				    System.out.println("***************************************");
			    	
			    	// Brute Force String Matching algorithm
				    if (bruteForceMatch(strPattern, s))
				    	System.out.println("BF MATCHED: " + strPattern);
				    else
				    	System.out.println("BF FAILED: " + strPattern);
				    
				    s.close();
				    sinput.close();
				    
					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 
		
				    // Rabin-Karp algorithm
				    if (rabinKarpMatch(strPattern, s))
				    	System.out.println("RK MATCHED: " + strPattern);
				    else
				    	System.out.println("RK FAILED: " + strPattern);
				    
				    s.close();
				    sinput.close();
				    
					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 
				    
				    // Knuth-Morris-Pratt algorithm
				    if (kmpMatch(strPattern, s))
				    	System.out.println("KMP MATCHED: " + strPattern);
				    else
				    	System.out.println("KMP FAILED: " + strPattern);
				    
				    s.close();
				    sinput.close();
				    
					sinput = new FileInputStream(sourceFileName);
					s = new DataInputStream(sinput); 
				    
				    // Boyer-Moore algorithm
				    if (bmooreMatch(strPattern, s))
				    	System.out.println("BM MATCHED: " + strPattern);
				    else
				    	System.out.println("BM FAILED: " + strPattern);
			    }
			} // LOOP to next pattern
	
//			FileOutputStream outf = new FileOutputStream(outputFileName);
//			// Pull sets of LIMIT from f and encrypt them
//			int[] tuple = new int[LIMIT];
//			byte[] translatedTuple; // the tuple to write 
//			int message = 0;
//			int t = 0;
//			while(!eofFound) {
//				
//				
//				
//				for (int i = 0; i < LIMIT; i++) {
//					try {
//						t = f.readUnsignedByte();
//					} catch (EOFException fu) {
//						eofFound = true;
//					}
//					if (!eofFound) {
//						tuple[i] =  t; // loads LIMIT # of elements into tuple
//					} else { // if EOF is found
//						while(i < LIMIT) { // fill rest of tuple with zeros
//							tuple[i] = 0;
//							i++;
//						}
//						f.close();
//						finput.close();
//						break;
//					}
//				}
//			}
//			if (eofFound) {
//				if (TESTING) {
//					System.out.println("Found EOF.");
//				}
//				outf.close();
//			} else {
//				if (TESTING) {
//					System.out.println("You have problems if you made it into this section without closing the file.");
//				}
//				assert(false);
//			}
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

}
