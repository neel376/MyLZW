/******************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   http://algs4.cs.princeton.edu/55compression/abraLZW.txt
 *                http://algs4.cs.princeton.edu/55compression/ababLZW.txt
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 ******************************************************************************/

/**
 *  The {@code LZW} class provides static methods for compressing
 *  and expanding a binary input using LZW compression over the 8-bit extended
 *  ASCII alphabet with 12-bit codewords.
 *  <p>
 *  For additional documentation,
 *  see <a href="http://algs4.cs.princeton.edu/55compress">Section 5.5</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick  
 *  @author Kevin Wayne
 */
public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
    private static final int MIN_LENGTH = 9;
    private static final int MAX_LENGTH = 16;
    private static double oldRatio = 0;
    private static int uncompressedData = 0;
    private static int compressedData = 0;
    private static double currentratio = 0;
     private static boolean isMonitoring = false;
    private static int mode;


    // Do not instantiate.
    private MyLZW() { }

    /**
     * Reads a sequence of 8-bit bytes from standard input; compresses
     * them using LZW compression with 12-bit codewords; and writes the results
     * to standard output.
     */
    public static void compress() { 
        
        BinaryStdOut.write(mode, 8);
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);  
        int code = R+1;  // R is codeword for EOF
        
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            uncompressedData += (s.length() * 8);

            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
             compressedData += W;
            int t = s.length();
            if (t < input.length()){ 
                // System.err.println("Code is " + code);
                
                //if no more room, resize
                if(code >= L){

                    //If still not a full codebook with 16 bits, increase codeword size
                    if(W < MAX_LENGTH){
                        W++;
                        L = (int)Math.pow(2, (W));
                        
                        st.put(input.substring(0, t + 1), code++);

                   
                    //If code book fills up, start looking for other options(reset, nothing, monitor)
                    }else if(W == MAX_LENGTH){

                        if(mode == 0){
                        //do nothing

                        }else if(mode == 1){
                            //reset
                            st = resetCompressCodeBook();
                            W = MIN_LENGTH;
                            L = (int)Math.pow(2, (W));
                            code = R + 1;
                            st.put(input.substring(0, t + 1), code++);
                           

                        }else if(mode == 2){
                                currentratio = (double)(((double)uncompressedData)/(double)(compressedData));
                                if(!isMonitoring){
                                    
                                    oldRatio = currentratio;
                                    isMonitoring = true;
                                    
                                }else if((oldRatio/currentratio) > 1.1){
                            
                                    st = resetCompressCodeBook();
                                    W = MIN_LENGTH;
                                    L = (int)Math.pow(2, (W));
                                    code = R + 1;
                                    st.put(input.substring(0, t + 1), code++);
                                    isMonitoring = false;
                            }     
                        }
                    }
                    
                //if not all codewords are used
                }else if(code < L){
                    st.put(input.substring(0, t + 1), code++);
                }
                
            }   
            input = input.substring(t);            // Scan past s in input.
           
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 

    /**
     * Reads a sequence of bit encoded using LZW compression with
     * 12-bit codewords from standard input; expands them; and writes
     * the results to standard output.
     */
    public static void expand() {

        mode = BinaryStdIn.readInt(8); // Read mode
        String[] st = new String[65536];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = ""; // (unused) lookahead for EOF
        int codeword = BinaryStdIn.readInt(W);

        if (codeword == R) return;           // expanded message is empty string
        
        String val = st[codeword];
         

        while (true) {
         uncompressedData += (val.length() * 8);
         compressedData += W;

            if(i >= L){

                // if all codewords are used but still havent reached 16 bits
                if(W < MAX_LENGTH){
                    W++;
                    L = (int)Math.pow(2, (W));
                   

                // if all codewords are reached and we already are using 16 bits         
                }else if(W == MAX_LENGTH){
 
                    if(mode == 0){
                        //do nothing

                    }else if(mode == 1){
                        //reset    
                        st = resetExpandCodeBook();   
                        W = MIN_LENGTH;
                        L = (int)Math.pow(2, (W));
                        i = R + 1;    

                    }else if(mode == 2){
                        //monitor
                        // currentratio = (double)(uncompressedData/compressedData);
                        currentratio = (double)(((double)uncompressedData)/(double)(compressedData));
                        if(!isMonitoring){
                                    
                            oldRatio = currentratio;
                            isMonitoring = true;
                                    
                        }else if((oldRatio/currentratio) > 1.1){
                            
                            st = resetExpandCodeBook();   
                            W = MIN_LENGTH;
                            L = (int)Math.pow(2, (W));
                            i = R + 1; 
                            isMonitoring = false;
                                

                        }
                    }       
                }          
            
            }

            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            
        
            
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
        }
        BinaryStdOut.close();
    }

    /**
     * Sample client that calls {@code compress()} if the command-line
     * argument is "-" an {@code expand()} if it is "+".
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        
        if(args[0].equals("-")){
            
            if(args[1].equals("n")){
                 mode = 0;
            
            }else if(args[1].equals("r")){
                 mode = 1;
            
            }else if (args[1].equals("m")){
                 mode = 2;
            
            }else{
                System.err.println("No mode entered, exiting");
                System.exit(1);
            }

         compress();
        }
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

  public static TST<Integer> resetCompressCodeBook(){
     TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);  

        return st;
  } 

  public static String[] resetExpandCodeBook(){
     String[] st = new String[65536];
     int i;
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";    

        return st;
  } 

}
