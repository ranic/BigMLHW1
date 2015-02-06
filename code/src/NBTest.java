package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    // The domain of labels for this problem
    private Map<String, Integer> labelCounts = new HashMap<String, Integer>();;
    private Map<String, Integer> tokenCounts = new HashMap<String, Integer>();;
    private int numDocs = 0;
    private int vocabSize = 0;

    private Set<String> loadNeededTokens(String testFilename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(testFilename));
        String line;
        Set<String> needed = new HashSet<String>();

        while ((line = br.readLine()) != null) {
            needed.addAll(NBUtils.tokenizeDoc(line.split("\t")[1]));
        }

        return needed;
    }

    private void loadRelevantEvents(Set<String> needed, String trainFilename) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //BufferedReader br = new BufferedReader(new FileReader(trainFilename));
        String line, event, label;
        int count = 0;

        Pattern labelCountPattern = Pattern.compile("Y=(\\S+),(\\d+)");
        Pattern tokenCountPattern = Pattern.compile("W=(\\S+) Y=(\\S+),(\\d+)");
        Pattern vocabSizePattern = Pattern.compile("Vocab\\t(\\d+)");

        Matcher labelCountMatcher, tokenCountMatcher, vocabSizeMatcher;

        while ((line = br.readLine()) != null) {
            tokenCountMatcher = tokenCountPattern.matcher(line);
            labelCountMatcher = labelCountPattern.matcher(line);
            vocabSizeMatcher = vocabSizePattern.matcher(line);

            if (tokenCountMatcher.matches()) {
                if (tokenCountMatcher.group(1).equals("*") ||
                        needed.contains(tokenCountMatcher.group(1))) {
                    // #(Y = y, W = w)
                    event = String.format("W=%s Y=%s", tokenCountMatcher.group(1), tokenCountMatcher.group(2));
                    tokenCounts.put(event, Integer.valueOf(tokenCountMatcher.group(3)));
                }
            } else if (labelCountMatcher.matches()) {
                label = labelCountMatcher.group(1);
                count = Integer.valueOf(labelCountMatcher.group(2));
                if (label.equals("*"))
                    numDocs = count;
                else
                    labelCounts.put(label, count);
            } else if (vocabSizeMatcher.matches()) {
                vocabSize = Integer.valueOf(vocabSizeMatcher.group(1));
            }
        }
    }

    public void test(String testFilename, String trainFilename) throws IOException {
        Set<String> needed = loadNeededTokens(testFilename);
        loadRelevantEvents(needed, trainFilename);

        BufferedReader br = new BufferedReader(new FileReader(testFilename));
        String line, event, labelEvent;
        Vector<String> tokens;

        //dump(needed);

        while ((line = br.readLine()) != null) {
            String bestLabel = "";
            double bestProb = -Double.MAX_VALUE;
            tokens = NBUtils.tokenizeDoc(line.split("\t")[1]);

            for (String curLabel : labelCounts.keySet()) {
                // P(Y = label), the prior
                double curProb = Math.log(((double) labelCounts.get(curLabel)) / numDocs);
                labelEvent = String.format("W=* Y=%s", curLabel);

                // Sum over Wi of p(W = wi | Y = label)
                for (String token : tokens) {
                    event = String.format("W=%s Y=%s", token, curLabel);
                    int tokenCount = (tokenCounts.containsKey(event)) ? tokenCounts.get(event) : 0;
                    int labelCount = (tokenCounts.containsKey(labelEvent)) ? tokenCounts.get(labelEvent) : 0;
                    curProb += Math.log(((double) tokenCount + 1) /
                            (labelCount + 1 + vocabSize));
                }

                if (curProb > bestProb) {
                    bestLabel = curLabel;
                    bestProb = curProb;
                }
            }
            System.out.printf("%s\t%.4f\n", bestLabel, bestProb);
        }
    }

    private void dump(Set<String> needed) {
        for (String token : needed) {
            System.out.println(token);
        }
        System.out.println("##############");
        for (String c : labelCounts.keySet()) {
            System.out.println(c + "," + labelCounts.get(c));
        }
        System.out.println("##############");
        for (String c : tokenCounts.keySet()) {
            System.out.println(c + "," + tokenCounts.get(c));
        }

        System.out.println(vocabSize);
    }


    public static void main(String[] args) throws IOException {
        NBTest nb = new NBTest();
        if (args.length != 1) {
            System.out.println("Usage: cat train.txt | java src.NBTrain | java src.NBTest test.txt");
            return;
        }
        nb.test(args[0], "");
    }
}
