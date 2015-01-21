package streaming;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by vijay on 1/12/15.
 */

/**
 * TODO: Run category tests again
 * TODO: Look at HW2 guidelines
 */
public class NBTest {
    private final NBFeatureDictionary features;

    public NBTest() {
        // TODO: Make these configurable
        String[] categories = {"ECAT", "CCAT", "GCAT", "MCAT"};
        features = new NBFeatureDictionary(categories);
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

    public static void main(String[] args) {
        NBTest nb = new NBTest();
        if (args.length != 1) {
            System.out.println("Usage: cat train.txt | java src.NBTrain | java src.NBTest test.txt");
            return;
        }

        nb.features.load();
        nb.test(args[0]);
    }
}
