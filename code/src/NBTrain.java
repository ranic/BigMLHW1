package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vijay on 1/12/15.
 */
public class NBTrain {
    private final NBFeatureDictionary features;

    public NBTrain() {
        String[] categories = {"ECAT", "CCAT", "GCAT", "MCAT"};
        features = new NBFeatureDictionary(categories);
    }

    public void train() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                // Each line is of the form Cat1,Cat2,...,CatK  w1 w2 w3 ... wN
                // TODO: Sanitize input or throw for malformed input
                String[] pair = line.split("\t");
                String[] labels = pair[0].split(",");
                Vector<String> tokens = NBUtils.tokenizeDoc(pair[1]);

                for (String label : labels) {
                    features.update(label, tokens);
                }

                // #(Y = *) += 1
                features.incrementNumDocs();
            }
        } catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        NBTrain nb = new NBTrain();
        nb.train();
        nb.features.dump();
    }

}
