package streaming;

import java.util.Vector;

/**
 * Created by vijay on 1/20/15.
 */
public class NBUtils {
    // TODO: Stop word and punctuation removal

    /**
     * @param cur_doc
     * @return
     *  Vector of tokens from cur_doc
     */
    static Vector<String> tokenizeDoc(String cur_doc) {
        String[] words = cur_doc.split("\\s+");
        Vector<String> tokens = new Vector<String>();
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\W", "");
            if (words[i].length() > 0) {
                tokens.add(words[i]);
            }
        }
        return tokens;
    }

}
