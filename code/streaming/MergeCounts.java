package streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Streams (event, delta) pairs from stdin and merges deltas for like events.
 *
 * Assumes input is sorted by event.
 *
 * Created by vijay on 1/22/15.
 */
public class MergeCounts {

    public static void main(String[] args) {
        String previousKey = null;
        int sumForPreviousKey = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            while ((line = br.readLine()) != null && line.length() != 0) {
                String[] pair = line.split("\t");
                String event = pair[0];
                int delta = Integer.valueOf(pair[1]);

                // Citation: Taken from scalable-nb-notes (1/20).
                if (event.equals(previousKey)) {
                    //System.err.format("%s, %s\n", event, previousKey);
                    sumForPreviousKey += delta;
                } else {
                    outputKey(previousKey, sumForPreviousKey);
                    previousKey = event;
                    sumForPreviousKey = delta;
                }
            }
            outputKey(previousKey, sumForPreviousKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Citation: Taken from scalable-nb-notes (1/20).
    private static void outputKey(String key, int sum) {
        if (key != null) {
            //System.err.format("Printing: %s, %d\n", key, sum);
            System.out.format("%s,%d\n", key, sum);
        }
    }
}
