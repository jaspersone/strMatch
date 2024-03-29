/*
 *  Student information for assignment:
 *  Matthew McClure: Slip days used for this project: 0  Slip days used (total): 0
 *  Jasper Sone: Slip days used for this project: 0  Slip days used (total): 1
 */
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

public class strMatch {
    static boolean TESTING = false;
    static boolean STATS_ON = true;
    
    // Pick a PRIME that is within the scope of our data type
    static int     PRIME = 28657;//2147483647; // 2^31-1
    
    // static variable so our chunk function can notice the last byte of
    // the previous chunk, for the 0x0D,0x0A Windows newline pattern.
    static byte prevByte = 0x00;

    // begin McClure driving
    // Helper class for a rotating ring buffer.
    public static class RingByteBuffer {
        // Local data store for the Ring Buffer
        byte[]  array = null;
        
        // The index of the most recent element added
        int     myHead;
        
        // The index of the least recent element added
        int     myTail;
        
        // Size of the array, for computing modulo indexing and implied
        // removal on an add to a full array.
        int     myLimit;
        
        // Number of active elements in the array
        int     myNumElements;
        
        /**
         * Constructor for the RingByteBuffer
         */
        public RingByteBuffer(int limit) {
            myHead = 0;
            myTail = 0;
            myLimit = limit;
            array = new byte[limit];
        }
        
        /**
         * Add a byte to the array, removing the oldest entry from visibility
         * once the size of the buffer reaches the limit.
         */
        public final void add(byte e) {
            // Move the tail only on a replace
            if (myNumElements == myLimit)
                myTail = (myTail + 1) % myLimit;
            
            array[myHead] = e;

            // Increment head to the next position
            myHead = (myHead + 1) % myLimit;
            myNumElements = Math.min(myNumElements+1, myLimit);
        }
        
        public void addRange(byte[] s, int start, int count) {
            for (int i = start; i < start + count; i++) {
                add(s[i]);
            }
        }
        
        /**
         * Get the ith element in the array, adjusted for ring addressing.
         */
        public final byte get(int index) {
            return array[(myTail + index) % myLimit];
        }
        
        /**
         * Get the head of the array, which was the last element inserted
         */
        public final byte getHead() {
            if ((myHead-1) < 0) 
                return array[myLimit - 1];
            else 
                return array[myHead - 1];
        }
        
        /**
         * Returns the number of elements
         */
        public int size() {
            return myNumElements;
        }
        
        /**
         * Helper function for toString
         */
        public String toString() {
            String  str = new String("");
            for (int i = 0; i < myNumElements; i++) {
                str += get(i);
            }
            return str;
        }
    }
    
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
    static int fastExp(int a, int b, int n)
    {
        int c = 1, k = 0;

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

    // begin Sone driving
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
        String  scope       = "";
        try {
            for (int i = 0; i < chunkCount; i++) {
                byte currentByte = source.readByte();
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

    protected static String getNextChunkCountBytes(int chunkCount, byte[] source, int leftPoint)
    {
        assert(chunkCount > 0);
        assert(leftPoint >= 0 && leftPoint < source.length);

        StringBuilder scope = new StringBuilder();
        // assigns endPoint to leftPoint + chunkCount, unless it goes beyond the size
        // of the source array
        int endPoint = ((leftPoint + chunkCount) < source.length) ?
                (leftPoint + chunkCount) : source.length;
        
        for (int i = leftPoint; i < endPoint; i++) {
             byte currentByte = source[i];
             // Windows uses two characters to represent a text 
             // newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
             // itself, so convert 0x0D to 0x0A, and absorb
             // trailing 0x0A if this is a windows newline encoding
             //
             if (currentByte == 0x0D) {
                 scope.append((char) 0x0A);
             } else if ((prevByte == 0x0D) && (currentByte == 0x0A)) {
                 // Absorb this iteration...
             } else {
                 scope.append((char) currentByte);
             }
             prevByte = currentByte;
        }
        return scope.toString();
    }

    protected static int 
    getNextChunkCountRingByteBuffer(
        int chunkCount, byte[] source, int leftPoint, RingByteBuffer ring)
    {
        assert(chunkCount > 0);
        assert(leftPoint >= 0 && leftPoint < source.length);
        
        // assigns endPoint to leftPoint + chunkCount, unless it goes beyond the size
        // of the source array
        int endPoint = ((leftPoint + chunkCount) < source.length) ?
            (leftPoint + chunkCount) : source.length;
        
        for (int i = leftPoint; i < endPoint; i++) {
            byte currentByte = source[i];
            // Windows uses two characters to represent a text 
            // newline (Hex 0x0D, 0x0A).  Apple has 0x0D by
            // itself, so convert 0x0D to 0x0A, and absorb
            // trailing 0x0A if this is a windows newline encoding
            //
            if (currentByte == 0x0D) {
                ring.add((byte)0x0A);
            } else if ((prevByte == 0x0D) && (currentByte == 0x0A)) {
                // Absorb this iteration...
            } else {
                ring.add(currentByte);
            }
            prevByte = currentByte;
        }
        
        return Math.max(endPoint - leftPoint, 0);
    }
    
    /**
     * Brute force pattern matching algorithm.  Checks each source byte as 
     * the leftmost comparison with the pattern.
     *
     * @param pattern Incoming pattern we want to find the the source.
     * @param source Source byte array we are trying to match a subset with the 
     *        given pattern.
     * @return TRUE if the pattern was found, FALSE if the pattern was not found.
     */
    protected static boolean bruteForceMatch(String pattern, byte[] source)
    {
        if (TESTING) {
            System.out.println(">>> Brute Force Pattern Match <<<");
        }
        int             chunkCount      = pattern.length();
        int             nextCharIndex   = chunkCount;
        boolean         patternFound    = false;
        RingByteBuffer  byteRing;
        
        
        if (pattern.length() > source.length)
            return false;
        
        if (chunkCount <= 0) 
            return true; 

        // Reset our prevByte for our stream parser.
        prevByte = 0x00;
        
        // Initialize the ring buffer
        byteRing = new RingByteBuffer(chunkCount);
        
        // Copy the first chunkCount bytes of source into the ring
        getNextChunkCountRingByteBuffer(chunkCount, source, 0, byteRing);
        
        // go through the source and search for the pattern
        while(!patternFound && nextCharIndex < source.length) {
            // compare scope so far
            int i = 0;
            while (i < chunkCount) {
                if (pattern.charAt(i) != byteRing.get(i))
                    break;
                i++;
            }
            if (i == chunkCount) {
                if (TESTING) {
                    System.out.println("PATTERN FOUND!!! YES!!!");
                }
                patternFound = true;
            } else { // get the next char
                getNextChunkCountRingByteBuffer(1, source, nextCharIndex++, byteRing);
            }
        }
        return patternFound;
    }

    // Caches pattern hashes of patterns that are reused by experiment wrapper
    // and prevents rehashing of patterns everytime the buffer has to reload
    static Hashtable<String, Integer> sumPatternHashes = new Hashtable<String, Integer>();
    static Hashtable<String, Integer> basePatternHashes = new Hashtable<String, Integer>(); 

    protected static int getPatternHash(String pattern, boolean USE_SUM) {
        if (USE_SUM && sumPatternHashes.containsKey(pattern))
            return sumPatternHashes.get(pattern);
        if (!USE_SUM && basePatternHashes.containsKey(pattern))
            return basePatternHashes.get(pattern);
        
        // Generate the hash value for the pattern if not found in cache
        int patHash = 0;

        if (USE_SUM) {
            for (int i = 0; i < pattern.length(); i++) {
                byte b = (byte)pattern.charAt(i);
                patHash += (int)b & 0xFF;
            }
        } else {
            for (int i = 0; i < pattern.length(); i++) {
                byte b = (byte)pattern.charAt(i);
                patHash = (patHash + 
                    (((byte)b & 0xFF) * 
                        fastExp(256, pattern.length() - i - 1, PRIME) % PRIME)) % PRIME;
            }
        }
    
        // add pattern hash to cache table to prevent rehashing upon repeat calls
        if (USE_SUM) sumPatternHashes.put(pattern, patHash);
        else         basePatternHashes.put(pattern, patHash);
        return patHash;
    }

    // Vars to keep track of collisions verses total iterations
    static int sumCollisions   = 0;
    static int sumTotal        = 0;
    static int baseCollisions  = 0;
    static int baseTotal       = 0;

    /**
     * Simple Rabin-Karp pattern matching using summation algorithm.  Since
     * we are using longs, the maximum sum we can have is 256+256+...+256 for
     * 2^55 times, and we can assume that this data set is intractable for 
     * computational time.  
     * 
     * @param pattern Incoming pattern we want to find the the source.
     * @param source Source byte array we are trying to match the pattern with.
     * @return TRUE if the pattern was found, FALSE if the pattern was not found.
     */
    protected static boolean rabinKarpMatchSUM(String pattern, byte[] source)
    {
        int             chunkCount      = pattern.length();
        int             nextCharIndex   = chunkCount;
        boolean         patternFound    = false;
        int             srcHash         = 0;
        int             patHash         = 0;
        RingByteBuffer  byteRing;
      
        if (pattern.length() > source.length)
            return false;
        
        if (chunkCount <= 0) 
            return true; 
        
        // Reset prevByte for our stream parser.
        prevByte = 0x00;
        
        // Initialize the ring buffer
        byteRing = new RingByteBuffer(chunkCount);
        
        // Copy the first chunkCount bytes of source into the ring
        getNextChunkCountRingByteBuffer(chunkCount, source, 0, byteRing);

        patHash = getPatternHash(pattern, true);

        if (TESTING) {
            System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");
        }

        // Generate the hash value for the scope
        // Using sum hashing algorithm
        for (int i = 0; i < byteRing.size(); i++) {
            byte b = (byte)byteRing.get(i);

            srcHash += (int)b & 0xFF;
            assert((int)b >= 0);
        }

        if (TESTING) {
            System.out.println("scope=" +  byteRing);
            System.out.println("Expected srcHash=" + srcHash);
        }

        // go through the source and search for the pattern
        while(!patternFound && nextCharIndex < source.length) {
            sumTotal++;
            // compare scope so far
            if (srcHash == patHash) {
                sumCollisions++;
                int i = 0;
                if (TESTING) System.out.println("Collision found!");
                // Do a comparison of the actual strings
                while (i < chunkCount) {
                    if (pattern.charAt(i) != byteRing.get(i))
                        break;
                    i++;
                }

                if (i >= chunkCount) {
                    if (TESTING) System.out.println("PATTERN FOUND!!! YES!!!");
                    patternFound = true;
                }
            } 
            
            if (patternFound == false) { // get next scope
                byte lastByte = byteRing.get(0);
                srcHash -= (int)lastByte & 0xFF;

                getNextChunkCountRingByteBuffer(1, source, nextCharIndex++, byteRing);

                byte readByte = byteRing.getHead();
                assert(byteRing.size() == pattern.length());
                srcHash += (int)readByte & 0xFF;
                prevByte = readByte;
            }
        }
        return patternFound;
    }
    // end Sone driving

    // begin McClure driving
    /**
     * Simple Rabin-Karp pattern matching using summation algorithm.  Since
     * we are using longs, the maximum sum we can have is 256+256+...+256 for
     * 2^55 times, and we can assume that this data set is intractable for 
     * computational time.  
     * 
     * @param pattern Incoming pattern we want to find the the source.
     * @param source Source byte array we are trying to match the pattern with.
     * @return TRUE if the pattern was found, FALSE if the pattern was not found.
     */
    protected static boolean rabinKarpMatchBASE(String pattern, byte[] source)
    {
        int             chunkCount      = pattern.length();
        int             nextCharIndex   = chunkCount;
        boolean         patternFound    = false;
        int             srcHash         = 0;
        int             patHash         = 0;
        RingByteBuffer  byteRing;
        
        if (pattern.length() > source.length)
            return false;
        
        if (chunkCount <= 0) 
            return true; 
        
        // Reset prevByte for our stream parser.
        prevByte = 0x00;
        
        // Initialize the ring buffer
        byteRing = new RingByteBuffer(chunkCount);
        
        // Copy the first chunkCount bytes of source into the ring
        getNextChunkCountRingByteBuffer(chunkCount, source, 0, byteRing);
        
        patHash = getPatternHash(pattern, false);
        
        if (TESTING) {
            System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");
        }
        
        // Generate the hash value for the scope
        // Using base hashing algorithm
        int i = 0;
        
        while (i < byteRing.size()) {
            byte b = (byte)byteRing.get(i);
            
            // We are adding a new character, chop off the old one and append a new one
            srcHash = (srcHash + 
                       (((byte)b & 0xFF) * fastExp(256, pattern.length() - i - 1, PRIME) % PRIME)) % PRIME;
            
            if (TESTING) {
                System.out.println("i=" + i + " substring=" + byteRing);
                System.out.println("mhash=" + srcHash);
            }
            i++;
        }
        
        if (TESTING) {
            System.out.println("scope=" +  byteRing);
            System.out.println("Expected srcHash=" + srcHash);
        }
        
        // go through the source and search for the pattern
        while(!patternFound && (nextCharIndex < source.length)) {
            baseTotal++;
            // compare scope so far
            if (srcHash == patHash) {
                baseCollisions++;
                i = 0;
                if (TESTING) System.out.println("Collision found!");

                // Do a comparison of the actual strings
                while (i < chunkCount) {
                    if (pattern.charAt(i) != byteRing.get(i))
                        break;
                    i++;
                }
                
                if (i >= chunkCount) {
                    if (TESTING) System.out.println("PATTERN FOUND!!! YES!!!");
                    patternFound = true;
                }
            } 
            
            if (patternFound == false) { // get next scope
                byte lastByte = byteRing.get(0);

                // Generate the hash value for the scope
                // Base hashing algorithm
                // Subtract out the first character in the scope
                srcHash = srcHash - 
                    (((byte)lastByte & 0xFF)*fastExp(256, pattern.length() - 1, PRIME) % PRIME);

                // Modulo arithmetic
                // 49 is congruent to -1 modulo 50
                if (srcHash < 0) srcHash = PRIME + srcHash;
                
                getNextChunkCountRingByteBuffer(1, source, nextCharIndex++, byteRing);

                byte readByte = byteRing.getHead();

                // Promote all previous characters by multiplying by the base
                srcHash = (srcHash*256) % PRIME;
                srcHash = (srcHash + ((byte)readByte & 0xFF)) % PRIME;
                prevByte = readByte;
            }
        }
        return patternFound;
    }
    // end McClure driving

    // begin Sone driving
    /**
     * Iterative core building functionality that is O(3m) time,
     * where m is the length of the pattern.
     *
     * Portions modeled after Boyer-Moore algorithm described on 
     * p209 of "Theory in Programming Practice", Misra
     * 
     * @param p Byte array containing our pattern.
     * @param rt Integer array of an offset from the left of the pattern.
     *           Size of the array is assumed to be 256 entries, and is 
     *           initialized by the caller to all entries equal -1.
     * @return An array representing b(s), the pre-computed jump table
     *         for the good suffix heuristic.
     */
    protected static int[] buildCoreTable2(byte[] p, int[] rt)
    {
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

    // Prevents the regeneration of the core tables, if they have already
    // been created
    static Hashtable<String, int[]> coreTables = new Hashtable<String, int[]>();
    // end Sone driving
    
    // begin McClure driving
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
    protected static int[] buildCoreTable3(String pattern)
    {
        if (coreTables.containsKey(pattern)) {
            return coreTables.get(pattern);
        }
        // The pattern length is the size of the table, since each core
        // calculation involves the next symbol in p[i+direction]

        byte[]     p             = pattern.getBytes();
        int[]     table         = new int[p.length+1];
        int       i             = 0;
        int       coreLength     = 0;

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
        // Add entry to table
        coreTables.put(pattern, table);
        return table;
    }
    // end McClure driving
    
    // begin Sone driving    
    /**
     * Implements the Knuth-Morris-Pratt algorithm as described
     * in CS337, Eberlein and "Theory in Programming Practice", Misra.
     *
     * @param pattern Incoming pattern we want to find the the source.
     * @param source Source byte array we are trying to match a subset with the 
     *        given pattern.
     * @return TRUE if the pattern was found, FALSE if the pattern was not found.
     */ 
    protected static boolean kmpMatch(String pattern, byte[] source)
    {
        if (TESTING) {
            System.out.println(">>> Knuth-Morris-Pratt Pattern Match <<<");
        }
        int             chunkCount = pattern.length();
        int             bytesToGrab = 0;
        boolean         patternFound = false;
        int             nextCharIndex = chunkCount;
        int[]           c;
        RingByteBuffer  byteRing;

        if (pattern.length() > source.length)
            return false;

        if (chunkCount <= 0) 
            return true; 
        
        // Reset our prevByte for our stream parser.
        prevByte = 0x00;
        
        // Initialize the ring buffer
        byteRing = new RingByteBuffer(chunkCount);
        
        // Copy the first chunkCount bytes of source into the ring
        getNextChunkCountRingByteBuffer(chunkCount, source, 0, byteRing);
        
        // Build the core table for our algorithm.
        c = buildCoreTable3(pattern);
        
        // note that the left most will always be 0 in our version
        // t = scope
        // r = right index
        // l = left index (always 0 in our implementation)
        // p = pattern
        
        while ((!patternFound) && (nextCharIndex <= source.length)) {
            int r;
            for (r = 0; r < chunkCount; r++) {
                // Case 1: t[r] = p[r-l]
                // nothing to do, because this loop will iterate r
                
                if (pattern.charAt(r) != byteRing.get(r)) {
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
                getNextChunkCountRingByteBuffer(bytesToGrab, source, 
                                                nextCharIndex, byteRing);
                nextCharIndex += bytesToGrab;
            }
        }
        return patternFound;
    }
    // end Sone driving

    // begin McClure driving
    /**
     * Implementation of Boyer-Moore pattern matching algorithm
     * as described in CS337, Eberlein and 
     * "Theory in Programming Practice", Misra.
     *
     * @param pattern Incoming pattern we want to find the the source.
     * @param source Source byte array we are trying to match a subset with the 
     *        given pattern.
     * @return TRUE if the pattern was found, FALSE if the pattern was not found.
     */ 
    protected static boolean bmooreMatch(String pattern, byte[] source)
    {
        int             chunkCount = pattern.length();
        int[]           rt = new int[256];
        int[]           b;
        int             bytesToGrab = 0;
        int             nextCharIndex = chunkCount;
        boolean         patternFound = false;
        RingByteBuffer  byteRing;

        if (TESTING) {
            System.out.println(">>> Boyer-Moore Pattern Match <<<");
        }

        if (pattern.length() > source.length)
            return false;
        
        if (chunkCount <= 0) 
            return true; 

        // Reset our prevByte for our stream parser.
        prevByte = 0x00;
        
        // Initialize the ring buffer
        byteRing = new RingByteBuffer(chunkCount);
        
        // Copy the first chunkCount bytes of source into the ring
        getNextChunkCountRingByteBuffer(chunkCount, source, 0, byteRing);
        
        for(int i = 0; i < rt.length; i++)
            rt[i] = -1; // initialize rt with all -1 values (flags)

        // Build the core table for Boyer-Moore
        b = buildCoreTable2(pattern.getBytes(), rt);

        // Reset our prevByte for our stream parser.
        prevByte = 0x00;

        // note that the left most will always be 0 in our version
        // t = scope
        // r = right index
        // l = left index (always 0 in our implementation)
        // p = pattern
        // Q1: Every occurrence of p in t that begins before index l 
        //     has been previously found.
        // Q2: 0^2 j^2 chunkCount,p[j..chunkCount]=t[l+j..l+chunkCount]
        while ((!patternFound) && (nextCharIndex <= source.length)) {
            int j;
            for (j = chunkCount; j > 0; j--) {
                // Case 1: j > 0 ^ p[j-1] = t[l+j-1]
                // nothing to do, because this loop will iterate r
                if (pattern.charAt(j-1) != byteRing.get(j-1)) {
                    // Case 2: Q1 ^ Q2 ^ (j == 0 v p[j-1] != t[l+j-1])
                    int badSymbolHeuristic = 0;
                    int goodSuffixHeuristic = 0;

                    // Since j is the rightmost index into the pattern,
                    // it reflects the b(s) for the suffix starting at 
                    // j.
                    goodSuffixHeuristic = b[chunkCount-j];
                    badSymbolHeuristic = j - 1 - rt[(int) byteRing.get(j-1) & 0xff];
                    // Determine the total increment due to the mismatch, 
                    // the maximum of the badSymboleHeuristic and the goodSuffixHeuristic.
                    bytesToGrab = Math.max(badSymbolHeuristic, goodSuffixHeuristic);

                    if (TESTING) {
                        System.out.println("Scope: " + byteRing);
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
                getNextChunkCountRingByteBuffer(bytesToGrab, source, 
                                                nextCharIndex, byteRing);
                nextCharIndex += bytesToGrab;
            }
        }
        return patternFound;
    }
    // end McClure driving
    
    // begin Sone driving    
    /**
     * Experiment Wrapper takes a particular experiment and buffers the input
     * stream to ensure efficient processing of large files. This hides the
     * the method of gathering the file input from the actual matching algorithms,
     * which still operate on each buffered chunk as a normal file
     * @param algorithm - the class of search algorithm that will be used for this
     *                    experiment
     * @param pattern   - the pattern that the algorithm is searching for
     * @param sourceFileName - the original source file that will be buffered by
     *                    experimentWrapper
     * @return a boolean value that indicates whether or not the file has discovered
     *         the pattern within the source file. Early return of true if the as soon
     *         as the first instance of the pattern is discovered.
     */
    protected static boolean experimentWrapper(strMatch.Match algorithm, String pattern, String sourceFileName) {
        boolean patternFound = false;
        try {
            File f = new File(sourceFileName);
            FileInputStream sinput = new FileInputStream(f);
          
            long offset = 0;
            long size = f.length();
            int  overlapSize = pattern.length() - 1; // testing with full pattern length
            int  chunkSize = Math.max(15000000,(pattern.length() * 10)); // 1000000 is best...

            //TESTING = true;
            // Open a channel
            //FileChannel fc = sinput.getChannel();

            //
            // XXX-Disabled until we can figure out what is wrong with filechannel
            //
            //FileChannel fc = sinput.getChannel();
            //MappedByteBuffer byteBuffer =
            //        fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
            
            byte[] prevBuffer = null;
            
            // feed byte buffers to search algorithm and search
            // for matching pattern
            while ((!patternFound) && (offset < size)) {
                int bytesOffset = 0;
                
                if (TESTING) System.out.println("I AM RUNNING");

                // Initialize a new read
                //byteBuffer.clear();
                byte[] bytes = new byte[(int)chunkSize];

                // Copy over the overlap from the previous read pass
                if (prevBuffer != null) {
                    for (int i = 0; i < overlapSize; i++)
                        bytes[i] = prevBuffer[chunkSize-overlapSize+i];
                    
                    bytesOffset = overlapSize;
                }
                
                // Read from the input stream the amount of non-overlapping bytes
                sinput.read(bytes, bytesOffset, 
                      (int)(Math.min(size - offset, chunkSize - bytesOffset)));

                // byteBuffer.get will get the next bytes in read sequence for
                // the file channel.  Rewinding for overlap is not possible.
              
                //
                // XXX-Disabled until we can figure out what is wrong with filechannel
                //
                //byteBuffer.get(bytes, bytesOffset, 
                //               (int)(Math.min(size - offset, chunkSize - bytesOffset)));
                
                // Search for the given buffer.
                patternFound = algorithm.search(pattern, bytes);

                if (patternFound) break;
                
                // Adjust the offset and remember this read pass
                offset += chunkSize - bytesOffset;
                prevBuffer = bytes;
            }
            // close files
            //fc.close();
            sinput.close();
        } catch (FileNotFoundException ex) {
            // TODO Auto-generated catch block
            System.out.println("There was an error opening the file " + ex);
            ex.printStackTrace();
        } catch (IOException r) {
            // TODO Auto-generated catch block
            System.out.println("IO Exception occurred while accessing f.");
            r.printStackTrace();
        }
      
        return patternFound;
    }

    /**
     * Basic skeleton for various different types of match sub-classes
     * @author jasper, matt
     */
    abstract static class Match
    {
        // the search method runs the default search algorithm for each of the 
        abstract public boolean search(String pattern, byte[] source);
        // By passing search(String pattern, sourceFileName to getMyPhrase,
        // you can obtain the proper search return message
        // to meet project requirements, the user must still concatonate the
        // search pattern to the end of the output given by getMyPhrase
        abstract public String getMyPhrase(boolean found);
        abstract public String getMyName();
    }
    
    static class BruteForceMatch extends Match
    {
        private String myName, myPhrase;
        public BruteForceMatch() {
            myName = "Brute Force Match";
            myPhrase = "BF ";
        }
        public boolean search(String pattern, byte[] source) {
            return strMatch.bruteForceMatch(pattern, source);
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
        public String getMyName() {return myName;}
    }
    
    static class RabinKarpMatch extends Match
    {
        private String myName, myPhrase;
        public RabinKarpMatch() {
            myName = "Rabin-Karp Match";
            myPhrase = "RK ";
        }
        public boolean search(String pattern, byte[] source) {
            return strMatch.rabinKarpMatchSUM(pattern, source);
        }
        public boolean search(String pattern, byte[] source, boolean USE_SUM) {
            if (USE_SUM)
                return strMatch.rabinKarpMatchSUM(pattern, source);
            else 
                return strMatch.rabinKarpMatchBASE(pattern, source);
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
        public String getMyName() {return myName;}
    }
    
    static class KMPMatch extends Match
    {
        private String myName, myPhrase;
        public KMPMatch() {
            myName = "Knuth-Morris-Pratt Match";
            myPhrase = "KMP ";
        }
        public boolean search(String pattern, byte[] source) {
            return strMatch.kmpMatch(pattern, source);
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
        public String getMyName() {return myName;}
    }
    
    static class BMooreMatch extends Match
    {
        private String myName, myPhrase;
        public BMooreMatch() {
            myName = "Boyer-Moore Match";
            myPhrase = "BM ";
        }
        public boolean search(String pattern, byte[] source) {
            return strMatch.bmooreMatch(pattern, source);
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
        public String getMyName() {return myName;}
    }

    protected static void runExperiments(String patternFileName, String sourceFileName, String outputFileName)
    {
        // create instances of various types of match wrappers
        // to prepare polymorphic search
        strMatch.BruteForceMatch bf = new strMatch.BruteForceMatch();
        strMatch.RabinKarpMatch  rk = new strMatch.RabinKarpMatch();
        strMatch.KMPMatch       kmp = new strMatch.KMPMatch();
        strMatch.BMooreMatch     bm = new strMatch.BMooreMatch();
        strMatch.Match[]    matches = {bf, rk, kmp, bm};
        Stopwatch sw = new Stopwatch(); // to collect stats
        boolean patternEofFound = false;

        // Get file set up
        try {
            FileInputStream pinput = new FileInputStream(patternFileName);
            DataInputStream p = new DataInputStream(pinput);
            FileOutputStream outFile = new FileOutputStream(outputFileName);

            // Outer loop over all patterns in the pattern file
            while (!patternEofFound) {
                int               patternAmpCount = 0;
                String            strPattern         = new String("");
                String            rawPattern         = new String("");
                StringBuilder     rawTmp             = new StringBuilder("");
                StringBuilder     strTmp             = new StringBuilder("");

                // Reset our prevByte for our stream parser
                prevByte = 0x00;

                // Read in the pattern from the pattern file.  Per the 
                // assignment documentation, the pattern is surrounded by
                // '&'
                while ((patternAmpCount < 2) && (!patternEofFound)) {
                    try {
                        byte b = p.readByte();

                        if (b == '&') {
                            patternAmpCount++;
                        } else if (patternAmpCount > 0) {
                            rawTmp.append((char)b);
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
                rawPattern = rawTmp.toString();
                if (strPattern.length() > 0) { // only search for strings with chars
                    if (STATS_ON) {
                        System.out.println("********************************************");
                        System.out.println("Search for '" + strPattern + "'");
                        System.out.println("********************************************");
                    }
                    // Loop through each type of match algorithm and run experiment
                    for (strMatch.Match algorithm : matches) {                        
                        // Setup output
                        if (STATS_ON) {
                            // reset collision count and total count for each pattern
                            sumCollisions   = 0;
                            sumTotal        = 0;
                            baseCollisions  = 0;
                            baseTotal       = 0;
                            sw.start();
                        }
                        String output = algorithm.getMyPhrase(
                                            experimentWrapper(algorithm, strPattern, sourceFileName)
                                        ) + rawPattern + "\n";
                        if (STATS_ON) {
                            sw.stop();
                            System.out.println(algorithm.getMyName() + " time: " + sw.time());                            
                            if (algorithm instanceof RabinKarpMatch) {
                                System.out.println("SUM  >>> Collisions / Total Compares :: " + sumCollisions + " / " + sumTotal);
                                System.out.println("BASE >>> Collisions / Total Compares :: " + baseCollisions + " / " + baseTotal);
                            }
                        }
                        outFile.write((output).getBytes());
                    } // LOOP to next algorithm
                    if (STATS_ON) System.out.print("\n");
                } // don't search if pattern is length is less than 1
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
    public static void main(String[] args)
    {
        String patternFileName = args[0];
        String sourceFileName = args[1];
        String outputFileName = args[2];
        runExperiments(patternFileName, sourceFileName, outputFileName);
    }
    // end Sone driving
}
