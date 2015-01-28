package streaming;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vijay on 1/12/15.
 */

/**
 * TODO: Run category tests again
 * TODO: Look at HW2 guidelines
 */
public class NBTest {
    private final NBFeatureDictionary features;
    private static final String COUNTS_TEMP_FILE = "counts_new.tmp";
    private static final String CLASS_FREQUENCIES_FILE = "class_frequencies.tmp";
    private static final String REQUESTS_TEMP_FILE = "requests.tmp";
    private static final String MERGED_REQUESTS_FILE = "merged_requests.tmp";
    private static final String REQUESTS_BY_DOC_FILE = "requests_by_doc.tmp";

    public NBTest() {
        // TODO: Make these configurable
        String[] categories = {"ECAT", "CCAT", "GCAT", "MCAT"};
        features = new NBFeatureDictionary(categories);
    }

    /* Converts the count structure C into C'. Reads C from stdin, writes C' out to COUNTS_TEMP_FILE */
    private int reorganizeCounts() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter p = new PrintWriter(new File(COUNTS_TEMP_FILE));
        PrintWriter classFrequencies = new PrintWriter(new File(CLASS_FREQUENCIES_FILE));
        Pattern wordCountLine = Pattern.compile("W=(\\S+) Y=(\\S+?),(\\d+)");
        List<String> events = null;
        String prevToken = null;
        String line, token;
        Matcher m;
        int vocabSize = 0;

        // Collects (event,count) pairs by token
        while ((line = br.readLine()) != null) {
            m = wordCountLine.matcher(line);
            if (m.matches()) {
                token = m.group(1);

                if (token.equals("*")) {
                    classFrequencies.write(line + "\n");
                    continue;
                }

                // Same token, aggregate events
                if (token.equals(prevToken)) {
                    events.add(line);
                } else {
                    // Different token, dump previous (if not initial case)
                    if (prevToken != null) {
                        String eventString = NBUtils.join(events, "\t");
                        p.format("%s\t%s\n", prevToken, eventString);
                    }
                    // Start aggregating for new token
                    prevToken = token;
                    events = new LinkedList<String>();
                    events.add(line);
                    vocabSize++;
                }
            }
        }
        p.close();

        return vocabSize;
    }

    /* Writes "requests" of the form (token, docId) to REQUESTS_TEMP_FILE */
    private void generateRequests(String testFile) throws IOException {
        PrintWriter p = new PrintWriter(new File(REQUESTS_TEMP_FILE));
        int docId = 1;

        BufferedReader br = new BufferedReader(new FileReader(testFile));
        String line;
        while ((line = br.readLine()) != null) {
            Vector<String> tokens = NBUtils.tokenizeDoc(line.split("\t")[1]);
            for (String token : tokens) {
                p.format("%s %d\n", token, docId);
            }
            docId += 1;
        }
        p.close();
    }

    private void mergeRequestsAndCounts() throws IOException {
        // Concatenate the files and sort the result
        String command = String.format("cat %s %s", COUNTS_TEMP_FILE, REQUESTS_TEMP_FILE);
        Process p = Runtime.getRuntime().exec(command);

        // Redirect the output to MERGED_REQUESTS_FILE
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        PrintWriter pw = new PrintWriter(new File(MERGED_REQUESTS_FILE));
        String line;

        while ((line = br.readLine()) != null) {
            pw.write(line + "\n");
        }

        pw.close();

        NBUtils.sortFile(MERGED_REQUESTS_FILE, "");
    }

    private void generateReplies() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(MERGED_REQUESTS_FILE));
        PrintWriter pw = new PrintWriter(new File(REQUESTS_BY_DOC_FILE));

        Pattern wordCountPattern = Pattern.compile("(\\S+)\t(.*)");
        Pattern requestPattern = Pattern.compile("(\\S+) (\\d+)");

        String line, requestedToken, requestingDocId, wordCount = null, token = null;
        Matcher wordCountMatcher, requestMatcher;

        while ((line = br.readLine()) != null) {
            wordCountMatcher = wordCountPattern.matcher(line);
            requestMatcher = requestPattern.matcher(line);

            if (wordCountMatcher.matches()) {
                token = wordCountMatcher.group(1);
                wordCount = wordCountMatcher.group(2);
            }

            if (requestMatcher.matches()) {
                requestedToken = requestMatcher.group(1);
                requestingDocId = requestMatcher.group(2);

                if (token != null) {
                    if (token.equals(requestedToken)) {
                        pw.write(String.format("%s\t%s\t%s\n", requestingDocId, requestedToken, wordCount));
                    } else {
                        // There is no training data for this token, omit wordCount
                        pw.write(String.format("%s\t%s\t\n", requestingDocId, requestedToken));
                    }
                }
                if (token != null && token.equals(requestedToken)) {
                    pw.write(String.format("%s\t%s\t%s\n", requestingDocId, requestedToken, wordCount));
                }
            }
        }

        pw.close();

        // Sort by doc ID number
        NBUtils.sortFile(REQUESTS_BY_DOC_FILE, "-n -k1,1");
    }:q

    /* Streams through REQUESTS_BY_DOC_FILE and classifies using the available data */
    private void classify(int vocabSize) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(REQUESTS_BY_DOC_FILE));
        String line;

        while ((line = br.readLine()) != null) {
            // Line is of the form <docId>  <token> <event1,count1>...

        }

    }

    public void test(String testFilename) {
        // Measures of accuracy of model against test
        int correct = 0;
        int total = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(testFilename));
            String line;
            while ((line = br.readLine()) != null) {
                // True categories; used to compute accuracy of learning for this test set
                Set<String> categories = new HashSet<String>(Arrays.asList(line.split("\t")[0].split(",")));
                Vector<String> tokens = NBUtils.tokenizeDoc(line.split("\t")[1]);

                String bestLabel = "";
                double bestProb = -Double.MAX_VALUE;

                for (String curLabel : features.LABELS) {
                    // P(Y = label), the prior
                    double curProb = Math.log(((double) features.numDocsForLabel(curLabel)) / features.numDocs());

                    // Sum over Wi of p(W = wi | Y = label)
                    for (String token : tokens) {
                        int tokenCount = features.tokenFreqForLabel(token, curLabel);
                        curProb += Math.log(((double) tokenCount + 1) /
                                (features.numTokensForLabel(curLabel) + 1 + features.dictionarySize()));
                    }

                    if (curProb > bestProb) {
                        bestLabel = curLabel;
                        bestProb = curProb;
                    }
                }
                System.out.printf("%s\t%.4f\n", bestLabel, bestProb);
                if (categories.contains(bestLabel)) {
                    correct++;
                }
                total++;
            }

        }catch(IOException e){
            System.out.println("Error: " + e.getMessage());
        }

        System.out.printf("Test complete. Classification rate is %d/%d = %.2f%%\n",
                correct, total, (double) (100*correct) / total);
    }

    public static void main(String[] args) throws IOException {
        NBTest nb = new NBTest();
        if (args.length != 1) {
            System.out.println("Usage: cat train.txt | java src.NBTrain | java src.NBTest test.txt");
            return;
        }
        int vocabSize = nb.reorganizeCounts();
        nb.generateRequests(args[0]);
        nb.mergeRequestsAndCounts();
        nb.generateReplies();
        nb.classify(vocabSize);

        //nb.test(args[0]);
    }
}
