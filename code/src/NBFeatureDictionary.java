package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Abstracts the Naive Bayes feature dictionary (counts) with utilities for dumping and loading.
 *
 * Naive Bayes collects the following counts:
 *  - #(Y = y): Number of training instances (docs) for each label
 *  - #(Y = y, W = w): Number of times token w appears in docs classified with label y
 *  - #(Y = y, W = *): Total number of tokens appearing in docs classified with label y
 *  - #(Y = *): Total number of documents
 *
 * Contains an update method, which takes in a token and label and updates counts accordingly.
 * Created by vijay on 1/20/15.
 */
public class NBFeatureDictionary {

    final Set<String> LABELS = new HashSet<String>();

    // #(Y = y) Number of training instances (documents) for each label
    private HashMap<String, Integer> labelToNumDocs = new HashMap<String, Integer>();

    // #(Y = y, W = w) Mapping from a label to a table of word counts in documents with that label
    private HashMap<String, HashMap<String, Integer>> labelToTokenFreqs = new HashMap<String, HashMap<String, Integer>>();

    // #(Y = y, W = *) Mapping from a label to the total number of tokens for documents with that label
    private HashMap<String, Integer> labelToNumTokens = new HashMap<String, Integer>();

    // #(Y = *)
    private int numDocs = 0;

    // Set of all words (its size is used for Laplace smoothing)
    private HashSet<String> dictionary = new HashSet<String>();

    NBFeatureDictionary(String[] labels) {
        for (String label : labels) {
            LABELS.add(label);
            labelToNumDocs.put(label, 0);
            labelToTokenFreqs.put(label, new HashMap<String, Integer>());
            labelToNumTokens.put(label, 0);
        }
    }

    /**
     * Utility that updates the counts associated with label and tokens.
     * @param label
     * @param tokens
     */
    void update(String label, Vector<String> tokens) {
        if (LABELS.contains(label)) {
            // #(Y = y) += 1
            labelToNumDocs.put(label, labelToNumDocs.get(label) + 1);
            // #(Y = y, w = *) += N
            labelToNumTokens.put(label, labelToNumTokens.get(label) + tokens.size());

            HashMap<String, Integer> counts = labelToTokenFreqs.get(label);
            for (String token : tokens) {
                // #(Y = y, W = w) += 1
                dictionary.add(token);
                int oldCount = (counts.containsKey(token)) ? counts.get(token) : 0;
                counts.put(token, oldCount + 1);
            }
        }
    }

    /**
     * Dump the contents of this feature dictionary to stdout.
     *
     * The format is as follows (repeated for each label):
     *  For each label:
     *      <label> <numTokensOfLabel>
     *      Y=<label>,W=<token> <count>
     *              ...
     *      Y=<label>,W=* <count>
     *      Y=<label>   <numDocsWithLabel>
     *
     *  Y=* <numDocs>
     */
    void dump() {
        for (String label : labelToNumDocs.keySet()) {
            System.out.println(label + "\t" + labelToTokenFreqs.get(label).size());
            // Print number of times each token classified by this label appears
            for (String token : labelToTokenFreqs.get(label).keySet()) {
                System.out.println("Y=" + label + ",W=" + token + "\t" + labelToTokenFreqs.get(label).get(token));
            }
            // Print number of documents classified for each label
            System.out.println("Y=" + label + ",W=*" + "\t" + labelToNumTokens.get(label));

            // Print total number of tokens classified by this label
            System.out.println("Y=" + label + "\t" + labelToNumDocs.get(label));
        }
        System.out.println("Y=*\t" + numDocs);
    }

    /**
     * Read a NBFeatureDictionary from stdin in the format specified by dump
     */
    void load() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;

            // For each token classified with this label, read its frequency
            while ((line = br.readLine()) != null && !line.startsWith("Y=*")) {
                String label = line.split("\t")[0];

                int numTokens = Integer.valueOf(line.split("\t")[1]);

                for (int i = 0; i < numTokens; i++) {
                    line = br.readLine();
                    String token = line.split("\t")[0].split(",")[1].split("=")[1];

                    int count = Integer.valueOf(line.split("\t")[1]);
                    labelToTokenFreqs.get(label).put(token, count);
                    dictionary.add(token);
                }

                // Read total number of tokens for this label
                line = br.readLine();
                labelToNumTokens.put(label, Integer.valueOf(line.split("\t")[1]));

                // Read the number of docs classified with this label
                line = br.readLine();
                labelToNumDocs.put(label, Integer.valueOf(line.split("\t")[1]));
            }

            if (line.startsWith("Y=*")) {
                numDocs = Integer.valueOf(line.split("\t")[1]);
            } else {
                throw new Exception("Parsing error");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * @param label
     * @return
     *      Number of training documents associated with 'label'
     */
    int getNumDocsWithLabel(String label) {
        return labelToNumDocs.get(label);
    }

    /**
     * @param token
     * @param label
     * @return
     *  Number of times 'token' appears in document classified as 'label'
     */
    int tokenFreqForLabel(String token, String label) {
        if (labelToTokenFreqs.get(label).containsKey(token)) {
            return labelToTokenFreqs.get(label).get(token);
        } else {
            return 0;
        }
    }

    /**
     * @param label
     * @return
     *  Number of tokens associated with 'label'
     */
    int numTokensForLabel(String label) {
        if (labelToNumTokens.containsKey(label)) {
            return labelToNumTokens.get(label);
        } else {
            return 0;
        }
    }

    /**
     * @param label
     * @return
     *  Number of training documents associated with 'label'
     */
    int numDocsForLabel(String label) {
        if (labelToNumDocs.containsKey(label)) {
            return labelToNumDocs.get(label);
        } else {
            return 0;
        }
    }

    /**
     * @return
     *  Number of training documents
     */
    int numDocs() {
        return numDocs;
    }

    /**
     * Increment number of training documents
     */
    void incrementNumDocs() {
        numDocs++;
    }

    /**
     * @return
     *  Size of dictionary (number of unique tokens)
     */
    int dictionarySize() {
        return this.dictionary.size();
    }
}
