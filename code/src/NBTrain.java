
package src;
import java.io.*;
import java.util.*;

/**
 * Created by vijay on 1/12/15.
 */
public class NBTrain {
    // This gets periodically flushed to disk (since it is O(|tokens|))
    private Map<String, Integer> tokenCounts = new HashMap<String, Integer>();
    // This stays in memory until the end of training (since it is O(|features|)
    private Map<String, Integer> labelCounts = new HashMap<String, Integer>();
    private int CACHE_LIMIT = 1000;
    private Set<String> labelFilter = new HashSet<String>();
    private BufferedWriter bw;

    void setCacheLimit(int cache_limit) {
        CACHE_LIMIT = cache_limit;
    }

    void setLabelFilter(String labels) {
        if (labels.length() > 0)
            labelFilter.addAll(Arrays.asList(labels.split(",")));
    }

    Set<String> filterLabels(String[] labels) {
        if (labelFilter.isEmpty()) {
            return new HashSet<String>(Arrays.asList(labels));
        } else {
            Set<String> result = new HashSet<String>();
            for (String s : labels) {
                if (labelFilter.contains(s)) {
                    result.add(s);
                }
            }
            return result;
        }
    }

    private void increment(String event, Map<String, Integer> cache, int count) {
        if (cache.containsKey(event)) {
            cache.put(event, cache.get(event) + count);
        } else {
            cache.put(event, count);
        }
    }

    /**
     * Helper method to flush the cacheTokenCount out to disk in the form "event,count"
     */
    private Map<String, Integer> flushCache(Map<String, Integer> cache) throws IOException {
        for (String key : cache.keySet()) {
            this.bw.write(key + "," + cache.get(key) + "\n");
        }
        return new HashMap<String, Integer>();
    }

    public void train() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            bw = new BufferedWriter(new OutputStreamWriter(System.out));
            //BufferedReader br = new BufferedReader(new FileReader("/Users/vijay/Documents/intellij/BigMLHW1/data/abstract.small.train"));
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                // Each line is of the form Cat1,Cat2,...,CatK  w1 w2 w3 ... wN
                String[] pair = line.split("\t");
                Set<String> labels = filterLabels(pair[0].split(","));
                Vector<String> tokens = NBUtils.tokenizeDoc(pair[1]);

                // #(Y = *) += #(labels)
                increment("Y=*", labelCounts, labels.size());

                // Stream events corresponding to document to stdout
                for (String label : labels) {
                    // #(Y = y) += 1
                    increment("Y=" + label, labelCounts, 1);

                    // #(Y = y, W = *) += |document|
                    increment("W=*" + " Y=" + label, labelCounts, tokens.size());

                    for (String token : tokens) {
                        // #(Y = y, W = w) += 1
                        increment("W=" + token + " Y=" + label, tokenCounts, 1);
                        if (tokenCounts.size() >= CACHE_LIMIT)
                            tokenCounts = flushCache(tokenCounts);
                    }
                }
            }
            flushCache(tokenCounts);
            flushCache(labelCounts);
            bw.close();
        } catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        NBTrain nb = new NBTrain();
        String cacheLimit = args.length > 0 ? args[0] : "1000";
        String labelFilter = args.length > 1 ? args[1] : "";
        nb.setCacheLimit(Integer.valueOf(cacheLimit));
        nb.setLabelFilter(labelFilter);
        nb.train();
    }

}
