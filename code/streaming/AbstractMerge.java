package streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * //TODO: Fix documentation
 * Streams (event, delta) pairs from stdin and merges deltas for like events.
 *
 * Assumes input is sorted by event.
 *
 * Created by vijay on 1/22/15.
 */
public abstract class AbstractMerge {

    public void merge() {
        String previousKey = null;
        String valueForPreviousKey = initValue();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            while ((line = br.readLine()) != null && line.length() != 0) {
                String[] pair = line.split("\t");
                String event = pair[0];
                String delta = pair[1];

                // Citation: Taken from scalable-nb-notes (1/20).
                if (event.equals(previousKey)) {
                    //System.err.format("%s, %s\n", event, previousKey);
                    valueForPreviousKey = joinValues(valueForPreviousKey, delta);
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

    protected abstract String initValue();

    protected abstract String joinValues(String oldValue, String delta);

    // Citation: Taken from scalable-nb-notes (1/20).
    private static void outputKey(String key, String value) {
        if (key != null) {
            System.out.format("%s,%d\n", key, value);
        }
    }
}
