package src;
import java.io.*;

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

    public void merge() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        String line;
        int vocabSize = 0;

        while ((line = br.readLine()) != null && line.length() != 0) {
            String[] pair = line.split(",");
            String event = pair[0];
            int delta = Integer.valueOf(pair[1]);

            // Citation: Taken from scalable-nb-notes (1/20).
            if (event.equals(previousKey)) {
                //System.err.format("%s, %s\n", event, previousKey);
                sumForPreviousKey = sumForPreviousKey + delta;
            } else {
                vocabSize++;
                outputKey(bw);
                previousKey = event;
                sumForPreviousKey = delta;
            }
        }
        outputKey(bw);
        System.out.println("Vocab\t" + vocabSize);

        bw.close();
    }

    // Citation: Taken from scalable-nb-notes (1/20).
    private void outputKey(BufferedWriter bw) throws IOException {
        if (previousKey != null) {
            bw.write(String.format("%s,%d\n", previousKey, sumForPreviousKey));
        }
    }

    public static void main(String[] args) throws IOException {
        MergeCounts m = new MergeCounts();
        m.merge();
    }
}
