package streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by vijay on 1/22/15.
 */
public class MergeEvents {
    public void merge() {
        String previousKey = null;
        String valueForPreviousKey = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            while ((line = br.readLine()) != null && line.length() != 0) {
                String[] pair = line.split("\t");
                String event = pair[0];
                String delta = pair[1];

                if (delta.matches("\\d+")) {
                    // This is a document id
                    // TODO: Handle empty value here
                    System.out.println(delta + "\t" + event + "\t" +
                            valueForPreviousKey);
                    continue;
                }

                // Citation: Taken from scalable-nb-notes (1/20).
                if (event.equals(previousKey)) {
                    //System.err.format("%s, %s\n", event, previousKey);
                    valueForPreviousKey = valueForPreviousKey + "\t" +  delta;
                } else {
                    outputKey(previousKey, valueForPreviousKey);
                    previousKey = event;
                    valueForPreviousKey = delta;
                }
            }
            outputKey(previousKey, valueForPreviousKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Citation: Taken from scalable-nb-notes (1/20).
    private static void outputKey(String key, String value) {
        if (key != null) {
            System.out.format("%s,%d\n", key, value);
        }
    }

    public static void main(String[] args) {
        MergeCounts m = new MergeCounts();
        m.merge();
    }
}
