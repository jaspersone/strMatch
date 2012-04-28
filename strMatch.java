import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Hashtable;

public class strMatch {
    static boolean TESTING = false;
    // static variable so our chunk function can notice the last byte of
    // the previous chunk, for the 0x0D,0x0A Windows newline pattern.
    static byte prevByte = 0x00;

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
        public void add(byte e) {
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
        public byte get(int index) {
            return array[(myTail + index) % myLimit];
        }
        
        /**
         * Get the head of the array, which was the last element inserted
         */
        public byte getHead() {
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
    protected static boolean 
    rabinKarpMatch(String pattern, DataInputStream source, boolean USE_SUM)
    {
        long srcHash = 0;
        long patHash = 0;

        // Reset prevByte for our stream parser.
        prevByte = 0x00;

        // Generate the hash value for the pattern
        for (int i = 0; i < pattern.length(); i++) {
            byte b = (byte)pattern.charAt(i);

            if (USE_SUM) {
                patHash += (long)b & 0xff;
            } else {
                patHash = (patHash + (((byte)b & 0xFF)* fastExp(256, pattern.length() - i - 1, 28657) % 28657)) % 28657;
            }
        }

        int chunkCount = pattern.length();
        boolean patternFound = false;
        String scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer

        if (TESTING) {
            System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");
        }


        if (USE_SUM) {
            // Generate the hash value for the scope
            // Sum hashing algorithm
            for (int i = 0; i < scope.length(); i++) {
                byte b = (byte)scope.charAt(i);

                srcHash += (long)b & 0xff;
                assert((long)b >= 0);
            }
        } else {
            // Generate the hash value for the scope
            // Base hashing algorithm
            int i = 0;
            while (i < scope.length()) {
                byte b = (byte)scope.charAt(i);

                // We are adding a new character, chop off the old one and append a new one
                srcHash = (srcHash + (((byte)b & 0xFF)* fastExp(256, pattern.length() - i - 1, 28657) % 28657)) % 28657;

                if (TESTING) {
                    System.out.println("i=" + i + " substring=" + scope);
                    System.out.println("mhash=" + srcHash);
                }
                i++;
            }
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
                    if (USE_SUM) {
                        srcHash -= (long)scope.charAt(0) & 0xff;
                        scope = scope.substring(1) + (char)0x0A;
                        prevByte = readByte;
                        assert(scope.length() == pattern.length());
                        srcHash += (long)0x0A;
                    } else {
                        srcHash = srcHash - (((byte)scope.charAt(0) & 0xFF)*fastExp(256, pattern.length() - 1, 28657) % 28657);
                        if (srcHash < 0) srcHash = 28657 + srcHash;
                        srcHash = (srcHash*256) % 28657;
                        srcHash = (srcHash + 0x0A) % 28657;
                        scope = scope.substring(1) + (char)0x0A;
                    }
                } else if ((prevByte == 0x0D) && (b == 0x0A)) {
                    // We skip this byte and compare against a non-modified scope
                } else {
                    if (USE_SUM) {
                        srcHash -= (long)scope.charAt(0) & 0xff;
                        scope = scope.substring(1) + (char)b;
                        prevByte = readByte;
                        assert(scope.length() == pattern.length());
                        srcHash += (long)b & 0xff;
                    } else {
                        // Generate the hash value for the scope
                        // Base hashing algorithm

                        // We are adding a new character, chop off the old one and append a new one
                        srcHash = srcHash - (((byte)scope.charAt(0) & 0xFF)*fastExp(256, pattern.length() - 1, 28657) % 28657);
                        if (srcHash < 0) srcHash = 28657 + srcHash;
                        srcHash = (srcHash*256) % 28657;
                        srcHash = (srcHash + ((byte)readByte & 0xFF)) % 28657;
                        scope = scope.substring(1) + (char)(readByte & 0xFF);

                        assert(srcHash >= 0);
                        assert(scope.length() == pattern.length());

                        if (TESTING) {
                            System.out.println("substring=" + scope);
                            System.out.println("mhash=" + srcHash);
                        }

                        prevByte = readByte;

                    }
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
     * Simple Rabin-Karp pattern matching using summation algorithm.  Since
     * we are using longs, the maximum sum we can have is 256+256+...+256 for
     * 2^55 times, and we can assume that this data set is intractable for 
     * computational time.  
     * 
     * @param pattern Incoming pattern we want to find the the source.
     * @param source Source byte array we are trying to match the pattern with.
     * @return TRUE if the pattern was found, FALSE if the pattern was not found.
     */
    protected static boolean rabinKarpMatch(String pattern, byte[] source, boolean USE_SUM)
    {
        int             chunkCount      = pattern.length();
        int             nextCharIndex   = chunkCount;
        boolean         patternFound    = false;
        long            srcHash         = 0;
        long            patHash         = 0;
        long            prime           = 28657;
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
        
        // Generate the hash value for the pattern
        for (int i = 0; i < pattern.length(); i++) {
            byte b = (byte)pattern.charAt(i);

            if (USE_SUM) {
                patHash += (long)b & 0xFF;
            } else {
                patHash = (patHash + (((byte)b & 0xFF)* fastExp(256, pattern.length() - i - 1, 28657) % 28657)) % 28657;
            }
        }

        if (TESTING) {
            System.out.println(">>> Rabin Karp Pattern Match: " +  "patHash = " + Long.toHexString(patHash) + " <<<");
        }

        // Generate the hash value for the scope
        if (USE_SUM) {
            // Using sum hashing algorithm
            for (int i = 0; i < byteRing.size(); i++) {
                byte b = (byte)byteRing.get(i);

                srcHash += (long)b & 0xFF;
                assert((long)b >= 0);
            }
        } else {
            // Using base hashing algorithm
            int i = 0;

            while (i < byteRing.size()) {
                byte b = (byte)byteRing.get(i);
                
                // We are adding a new character, chop off the old one and append a new one
                srcHash = (srcHash + 
                    (((byte)b & 0xFF) * fastExp(256, pattern.length() - i - 1, prime) % prime)) % prime;

                if (TESTING) {
                    System.out.println("i=" + i + " substring=" + byteRing);
                    System.out.println("mhash=" + srcHash);
                }
                i++;
            }
        }

        if (TESTING) {
            System.out.println("scope=" +  byteRing);
            System.out.println("Expected srcHash=" + srcHash);
        }

        //prevByte = ;

        // go through the source and search for the pattern
        while(!patternFound && nextCharIndex < source.length) {
            // compare scope so far
            if (srcHash == patHash) {
                int i = 0;

                System.out.println("Collision found!");
                
                // Do a comparison of the actual strings
                while (i < chunkCount) {
                    if (pattern.charAt(i) != byteRing.get(i))
                        break;
                    i++;
                }

                if (i >= chunkCount) {
                    if (TESTING) {
                        System.out.println("PATTERN FOUND!!! YES!!!");
                    }
                    patternFound = true;
                }
            } 
            
            if (patternFound == false) { // get next scope
                byte lastByte = byteRing.get(0);
                getNextChunkCountRingByteBuffer(1, source, nextCharIndex++, byteRing);
                byte readByte = byteRing.getHead();
                
                if (USE_SUM) {
                    srcHash -= (long)lastByte & 0xFF;
                    assert(byteRing.size() == pattern.length());
                    srcHash += (long)readByte & 0xFF;
                    prevByte = readByte;
                } else {
                    // Generate the hash value for the scope
                    // Base hashing algorithm
                    // Subtract out the first character in the scope
                    srcHash = srcHash - 
                        (((byte)lastByte & 0xFF)*fastExp(256, pattern.length() - 1, prime) % prime);
                    
                    // Modulo arithmetic
                    // 49 is congruent to -1 modulo 50
                    if (srcHash < 0) srcHash = prime + srcHash;
                    
                    // Promote all previous characters by multiplying by the base
                    srcHash = (srcHash*256) % prime;
                    srcHash = (srcHash + ((byte)readByte & 0xFF)) % prime;

                    if (TESTING) {
                        System.out.println("mhash=" + srcHash);
                    }
                    
                    prevByte = readByte;
                }
            }
        }
        return patternFound;
    }

    /**
     * 
     * @param pattern
     * @return
     */
    protected static String[] getKMPSubStrings(String pattern)
    {
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
    protected static String getCoreTest(String s, String[] substrings)
    {
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
    protected static int[] buildCoreTable(String pattern)
    {
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
    protected static int[] buildCoreTable3(byte[] p)
    {
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
        String          scope; // fill up the scope buffer
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
        c = buildCoreTable3(pattern.getBytes());
        
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
        int     bytesToGrab = 0;
        boolean patternFound = false;
        String  scope = getNextChunkCountChars(chunkCount, source); // fill up the scope buffer
        int[]   c = buildCoreTable3(pattern.getBytes());

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

        String scope = getNextChunkCountBytes(chunkCount, source, 0); // fill up the scope buffer

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
        int     bytesToGrab = 0;
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

    protected static void experimentWrapper(Hashtable<String, Integer> algorithms, String searchAlgorithm, String patternFileName, String sourceFileName) {
        try {
            FileInputStream pinput = new FileInputStream(patternFileName);
            DataInputStream p = new DataInputStream(pinput);
            FileInputStream sinput = new FileInputStream(sourceFileName);
            DataInputStream s = new DataInputStream(sinput);

            boolean patternEofFound = false;

            // Outer loop over all patterns in the pattern file
            while (!patternEofFound) {

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


    abstract static class Match {
        // the search method runs the default search algorithm for each of the 
        abstract public boolean search(String pattern, byte[] sourceFileName);
        // By passing search(String pattern, sourceFileName to getMyPhrase,
        // you can obtain the proper search return message
        // to meet project requirements, the user must still concatonate the
        // search pattern to the end of the output given by getMyPhrase
        // example usage:
        // BruteForceMatch bf = new BruteForceMatch();
        // String message = bf.getMyPhrase(search(pattern, sourceFileName)) + pattern;
        abstract public String getMyPhrase(boolean found);
    }
    
    static class BruteForceMatch extends Match {
        private String myPhrase;
        public BruteForceMatch() {
            myPhrase = "BF ";
        }
        public boolean search(String pattern, byte[] sourceFileName) {
            return false;
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
    }
    
    static class RabinKarpMatch extends Match {
        private String myPhrase;
        public RabinKarpMatch() {
            myPhrase = "RK ";
        }
        public boolean search(String pattern, byte[] sourceFileName) {
            return false;
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
    }
    
    static class KMPMatch extends Match {
        private String myPhrase;
        public KMPMatch() {
            myPhrase = "KMP ";
        }
        public boolean search(String pattern, byte[] sourceFileName) {
            return false;
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
    }
    
    static class BMooreMatch extends Match {
        private String myPhrase;
        public BMooreMatch() {
            myPhrase = "BM ";
        }
        public boolean search(String pattern, byte[] sourceFileName) {
            return false;
        }
        public String getMyPhrase(boolean found) {
            return found ? (myPhrase + "MATCHED: ") : (myPhrase + "FAILED: ");
        }
    }

    protected static void runExperiments(String patternFileName, String sourceFileName, String outputFileName)
    {
        // Adding hash table to store algorithm names and assign them switch values
        // This whole thing should be refactored later
        Hashtable<String, Integer> algorithms = new Hashtable<String, Integer>();
        algorithms.put("bruteForceMatch", 0);
        algorithms.put("bruteForceMatch_byteArray", 1);
        algorithms.put("rabinKarpMatch", 2);
        algorithms.put("rabinKarpMatch_byteArray", 3);
        algorithms.put("kmpMatch", 4);
        algorithms.put("kmpMatch_byteArray", 5);
        algorithms.put("bmooreMatch", 6);
        algorithms.put("bmooreMatch_byteArray", 7);

        boolean patternEofFound = false;

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

                    if (TESTING) {
                        File f = new File(sourceFileName);
                        sinput = new FileInputStream(f);

                        long offset = 0;
                        long size = f.length();
                        long chunkSize = 1000000; // Best...
                        long overlapSize = strPattern.length() - 1;

                        // Open a channel
                        FileChannel fc = sinput.getChannel();

                        MappedByteBuffer byteBuffer =
                                fc.map(FileChannel.MapMode.READ_ONLY, 0, size);

                        boolean patternFound = false;

                        while ((patternFound == false) && (offset < size)) {
                            byteBuffer.clear();
                            byte[] bytes = new byte[(int)chunkSize];
                            byteBuffer.get(bytes, 0, bytes.length);

                            ByteArrayInputStream bstream = new ByteArrayInputStream(bytes);
                            s = new DataInputStream(bstream);

                            // Rabin-Karp algorithm using rolling sum
                            if (rabinKarpMatch(strPattern, s, true)) {
                                patternFound = true;
                                break;
                            }

                            offset += chunkSize - overlapSize;
                        }

                        if (patternFound)
                            output = "RK MATCHED: " + strPattern;
                        else
                            output = "RK FAILED: " + strPattern;

                        if (TESTING) System.out.println(output);
                        outFile.write((output + "\n").getBytes());

                        fc.close();
                        sinput.close();
                    } else {
                        File f = new File(sourceFileName);
                        sinput = new FileInputStream(f);

                        long offset = 0;
                        long size = f.length();
                        long chunkSize = 1000000; // Best...
                        long overlapSize = strPattern.length() - 1;

                        // Open a channel
                        FileChannel fc = sinput.getChannel();

                        MappedByteBuffer byteBuffer =
                                fc.map(FileChannel.MapMode.READ_ONLY, 0, size);

                        boolean patternFound = false;

                        System.out.println("I AM RUNNING");
                        while ((patternFound == false) && (offset < size)) {
                            byteBuffer.clear();
                            byte[] bytes = new byte[(int)chunkSize];
                            byteBuffer.get(bytes, 0, (int)Math.min(size - offset, chunkSize));

                            if (bmooreMatch(strPattern, bytes)) {
                                patternFound = true;
                                break;
                            }

/*                            ByteArrayInputStream bstream = new ByteArrayInputStream(bytes);
                            s = new DataInputStream(bstream);

                            // Rabin-Karp algorithm using rolling sum
                            if (rabinKarpMatch(strPattern, s, false)) {
                                patternFound = true;
                                break;
                            }
*/
                            offset += chunkSize - overlapSize;
                        }

                        if (patternFound)
                            output = "RK MATCHED: " + strPattern;
                        else
                            output = "RK FAILED: " + strPattern;

                        if (TESTING) System.out.println(output);
                        outFile.write((output + "\n").getBytes());

                        fc.close();
                        sinput.close();
                    }

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
    public static void main(String[] args)
    {
        String patternFileName = args[0];
        String sourceFileName = args[1];
        String outputFileName = args[2];
        runExperiments(patternFileName, sourceFileName, outputFileName);
    }
}
