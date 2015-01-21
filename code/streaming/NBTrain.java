package streaming;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by vijay on 1/12/15.
 */
public class NBTrain {
    private final Set<String> LABELS;
    private HashMap<String, Integer> eventCache;
    // TODO: Make this configurable
    private final int CACHE_LIMIT = 1000;

    NBTrain() {
        String[] categories = {"ECAT", "CCAT", "GCAT", "MCAT"};
        LABELS = new HashSet<String>(Arrays.asList(categories));

        eventCache = new HashMap<String, Integer>();
    }

    /**
     * Store the event in the cache of counts of recently seen events. Flush cache if too large
     * @param event
     */
    private void cache(String event) {
        if (eventCache.containsKey(event)) {
            eventCache.put(event, eventCache.get(event) + 1);
        } else {
            eventCache.put(event, 1);
        }

        if (eventCache.size() >= CACHE_LIMIT) {
            flushCache();
        }
    }

    /**
     * Helper method to flush the cache out to disk in the form "event<tab>count"
     */
    private void flushCache() {
        for (String key : eventCache.keySet()) {
            System.out.println(key + "\t" + eventCache.get(key));
        }
        eventCache = new HashMap<String, Integer>();
    }

    public void train() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                // Each line is of the form Cat1,Cat2,...,CatK  w1 w2 w3 ... wN
                String[] pair = line.split("\t");
                String[] labels = pair[0].split(",");
                Vector<String> tokens = NBUtils.tokenizeDoc(pair[1]);

                // Stream events corresponding to document to stdout
                for (String label : labels) {
                    // Ignore labels that we don't want to classify
                    if (LABELS.contains(label)) {
                        // #(Y = y) += 1
                        cache("Y=" + label);
                        // #(Y = *) += 1
                        cache("Y=ANY");
                        for (String token : tokens) {
                            // #(Y = y, W = w) += 1
                            cache("Y=" + label + ",W=" + token);
                            // #(Y = y, W = *) += 1
                            cache("Y=" + label + ",W=*");
                        }
                    }
                }
            }
            flushCache();
        } catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        NBTrain nb = new NBTrain();
        nb.train();
        //nb.features.dump();
    }

}
