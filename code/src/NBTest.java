package src;

import java.io.*;
import java.util.*;

/**
 * Created by vijay on 1/12/15.
 */

/**
 * TODO: Run category tests again
 * TODO: Look at HW2 guidelines
 */
public class NBTest {
    private final Set<String> LABELS = new HashSet<String>();
    // #(Y = y) Number of training instances (documents) for each label
    HashMap<String, Integer> labelToNumDocs = new HashMap<String, Integer>();

    // #(Y = y, W = w) Mapping from a label to a table of word counts in documents with that label
    HashMap<String, HashMap<String, Integer>> labelToTokenFreqs = new HashMap<String, HashMap<String, Integer>>();

    // #(Y = y, W = *) Mapping from a label to the total number of tokens for documents with that label
    HashMap<String, Integer> labelToNumTokens = new HashMap<String, Integer>();

    // #(Y = *)
    int numDocs = 0;

    // Set of all words (used for Laplace smoothing)
    HashSet<String> dictionary = new HashSet<String>();


    public NBTest() {
        // TODO: Make these configurable
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

    public void readFeatureDict() {
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

    public void test(String testFilename) {
        int correct = 0;
        int total = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(testFilename));
            String line;
            while ((line = br.readLine()) != null) {
                Vector<String> tokens = tokenizeDoc(line.split("\t")[1]);
                Vector<String> categories = parseRelevantLabels(line.split("\t")[0]);
                String bestLabel = "";
                double bestProb = -Double.MAX_VALUE;
                for (String curLabel : LABELS) {
                    // P(Y = label)
                    double curProb = Math.log(((double) labelToNumDocs.get(curLabel)) / numDocs);

                    // Sum over Wi of p(W = wi | Y = label). Of all tokens for label, how many are wi?
                    for (String token : tokens) {
                        int tokenCount = labelToTokenFreqs.get(curLabel).containsKey(token) ? labelToTokenFreqs.get(curLabel).get(token) : 0;
                        curProb += Math.log(((double) tokenCount + 1) / (labelToNumTokens.get(curLabel) + dictionary.size() + 1));
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
        NBTest nb = new NBTest();
        if (args.length != 1) {
            System.out.println("Usage: cat train.txt | java src.NBTrain | java src.NBTest test.txt");
            return;
        }

        nb.readFeatureDict();
        nb.test(args[0]);
    }
}
