package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vijay on 1/12/15.
 */
public class NBTrain {
    private final Set<String> LABELS = new HashSet<String>();
    // #(Y = y) Number of training instances (documents) for each label
    HashMap<String, Integer> labelToNumDocs = new HashMap<String, Integer>();

    // #(Y = y, W = w) Mapping from a label to a table of word counts in documents with that label
    HashMap<String, HashMap<String, Integer>> labelToTokenFreqs = new HashMap<String, HashMap<String, Integer>>();

    // #(Y = y, W = *) Mapping from a label to the total number of tokens for documents with that label
    HashMap<String, Integer> labelToNumTokens = new HashMap<String, Integer>();

    // #(Y = *)
    int numDocs = 0;

    public NBTrain() {
        String[] categories = {"ECAT", "CCAT", "GCAT", "MCAT"};
        LABELS.addAll(Arrays.asList(categories));
        for (String label : LABELS) {
            labelToNumDocs.put(label, 0);
            labelToTokenFreqs.put(label, new HashMap<String, Integer>());
            labelToNumTokens.put(label, 0);
        }
    }


    Vector<String> parseRelevantLabels(String categories) {
        Vector<String> result = new Vector<String>();
        for (String s : categories.split(",")) {
            if (LABELS.contains(s)) {
                result.add(s);
            }
        }
        return result;
    }

    Vector<String> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        Vector<String> tokens = new Vector<String>();
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\W", "");
            if (words[i].length() > 0) {
                tokens.add(words[i]);
            }
        }
        return tokens;
    }

    public void train() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                // Each line is of the form Cat1,Cat2,...,CatK  w1 w2 w3 ... wN
                // TODO: Sanitize input or throw for malformed input
                String[] pair = line.split("\t");
                Vector<String> labels = parseRelevantLabels(pair[0]);
                Vector<String> tokens = tokenizeDoc(pair[1]);

                for (String label : labels) {
                    // #(Y = y) += 1
                    labelToNumDocs.put(label, labelToNumDocs.get(label) + 1);
                    // #(Y = y, w = *) += N
                    labelToNumTokens.put(label, labelToNumTokens.get(label) + tokens.size());

                    HashMap<String, Integer> counts = labelToTokenFreqs.get(label);
                    for (String token : tokens) {
                        // #(Y = y, W = w) += 1
                        int oldCount = (counts.containsKey(token)) ? counts.get(token) : 0;
                        counts.put(token, oldCount + 1);
                    }
                }

                // #(Y = *) += 1
                numDocs++;
            }
        } catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }

    public void writeFeatureDict() {
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

    public static void main(String[] args) {
        NBTrain nb = new NBTrain();
        nb.train();
        nb.writeFeatureDict();
    }

}
