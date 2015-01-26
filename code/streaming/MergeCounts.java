package streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * //TODO: Fix documentation
 * Streams (event, delta) pairs from stdin and merges deltas for identical events.
 *
 * Assumes input is sorted by event.
 *
 * Created by vijay on 1/22/15.
 */
public class MergeCounts {
    private String previousKey = null;
    private int sumForPreviousKey = 0;

    public void merge() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            while ((line = br.readLine()) != null && line.length() != 0) {
                String[] pair = line.split(",");
                String event = pair[0];
                int delta = Integer.valueOf(pair[1]);

                // Citation: Taken from scalable-nb-notes (1/20).
                if (event.equals(previousKey)) {
                    //System.err.format("%s, %s\n", event, previousKey);
                    sumForPreviousKey = sumForPreviousKey + delta;
                } else {
                    outputKey();
                    previousKey = event;
                    sumForPreviousKey = delta;
                }
            }
            outputKey();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Citation: Taken from scalable-nb-notes (1/20).
    private void outputKey() {
        if (previousKey != null) {
            System.out.format("%s,%d\n", previousKey, sumForPreviousKey);
        }
    }

    public static void main(String[] args) {
        MergeCounts m = new MergeCounts();
        m.merge();
    }
}
