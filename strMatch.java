import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


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
    protected static String fillUpScopeBuffer(int chunkCount, DataInputStream source)
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
    	assert(scope.length() == chunkCount);
    	return scope;
    }
    
    protected static boolean bruteForceMatch(String pattern, DataInputStream source)
    {
    	System.out.println(">>> Brute Force Pattern Match <<<");
    	int     chunkCount = pattern.length();
    	boolean patternFound = false;
    	String  scope = fillUpScopeBuffer(chunkCount, source); // fill up the scope buffer
        byte 	prevByte = 0x00;

// 		REFACTOR WITH RING:    	
//        byte[] scope = new byte[chunkCount];
//        int    scopeHead = 0, scopeTail = 0;
//        scopeHead += (scopeHead +1) % chunkCount;
//        
//        
//        scopeTail += (scopeTail +1) % chunkCount;

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
    
    protected static boolean rabinKarpMatch(String pattern, DataInputStream source)
    {
    	long srcHash = 0;
    	long patHash = 0;
    	byte prevByte = 0x00;
    	
    	// Generate the hash value for the pattern
    	for (int i = 0; i < pattern.length(); i++) {
    		byte b = (byte)pattern.charAt(i);
    		
    		// Our string is base 256, so each digit is 256^i where
    		// 0 <= i <= n, where is the length the string.
    	    int exponent = (i * 8) % 55;
    	    patHash += ((long)Math.pow(2.0, exponent))*(long)b;
    	}

    	int chunkCount = pattern.length();
    	boolean patternFound = false;
    	String scope = fillUpScopeBuffer(chunkCount, source); // fill up the scope buffer

    	System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");
// 		REFACTOR WITH RING:    	
//        byte[] scope = new byte[chunkCount];
//        int    scopeHead = 0, scopeTail = 0;
//        scopeHead += (scopeHead +1) % chunkCount;
//        
//        
//        scopeTail += (scopeTail +1) % chunkCount;

    	// Reinitialize the srcHash SUM
		srcHash = 0;
		
    	// Generate the hash value for the pattern
    	for (int i = 0; i < scope.length(); i++) {
    		byte b = (byte)scope.charAt(i);

    		// Our string is base 256, so each digit is 256^i where
    		// 0 <= i <= n, where is the length the string.
    	    int exponent = (i * 8) % 55;
    	    srcHash += ((long)Math.pow(2.0, exponent))*(long)b;
    		assert((long)b >= 0);
    		//prevByte = b;
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
    			
    			// Divide by 256 and append the next byte for the updated
    			// rolling hash function
    			//srcHash >>>= 8;
    			srcHash /= 256;//(long)prevByte;

    		long asrcHash = 0;
        	// Generate the hash value for the pattern
        	for (int i = 0; i < scope.length()-1; i++) {
        		byte ba = (byte)scope.charAt(i);

        		// Our string is base 256, so each digit is 256^i where
        		// 0 <= i <= n, where is the length the string.
        	    int exponent = (i * 8) % 55;
        	    asrcHash += ((long)Math.pow(2.0, exponent))*(long)ba;

//        		asrcHash += fastExp(256, i, 997L)*(long)ba;
        		assert((long)ba >= 0);
        		//prevByte = b;
        	}
        	System.out.println("scope=" + scope);
        	System.out.println("Expected srcHash=" + Long.toHexString(asrcHash) + " srcHash=" + Long.toHexString(srcHash));
        	assert(asrcHash == srcHash);
        	
    		    //srcHash += fastExp(256, pattern.length()-1, 997L)*(long)b;
        	    
        	    int exponent = ((pattern.length() - 1) * 8) % 55;
        	    srcHash += ((long)Math.pow(2.0, exponent))*(long)b;
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
    
    protected static boolean kmpMatch(String pattern, DataInputStream source)
    {
    	System.out.println(">>> Knuth-Morris-Pratt Pattern Match <<<");
    	return false;
    }

    protected static boolean bmooreMatch(String pattern, DataInputStream source)
    {
    	System.out.println(">>> Boyer-Moore Pattern Match <<<");
    	return false;
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
			    
			    System.out.println("***************************************");
			    System.out.println("* Search for '" + strPattern + "'");
			    System.out.println("***************************************");
			    
			    if (strPattern.length() > 0) { // do nothing if the string is empty
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
