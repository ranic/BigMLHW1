package streaming;

import java.io.*;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String[] words = cur_doc.toLowerCase().split("\\s+");
        Vector<String> tokens = new Vector<String>();
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("\\W", "");
            //words[i] = words[i].replaceAll("[^a-zA-Z ]", "");
            if (words[i].length() > 0) {
                tokens.add(words[i]);
            }
        }
        return tokens;
    }

    static void sortFile(String filename, String arguments) throws IOException {
        String command = String.format("sort %s %s", arguments, filename);
        Process p = Runtime.getRuntime().exec(command);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        PrintWriter pw = new PrintWriter(new File("sorted_" + filename));
        String line;

        while ((line = br.readLine()) != null) {
            pw.write(line + "\n");
        }

        pw.close();

        command = String.format("mv sorted_%s %s", filename, filename);
        p = Runtime.getRuntime().exec(command);


        try {
            p.waitFor();
        } catch (InterruptedException e) {
        }
    }

    static String join(List<String> list, String conjunction)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list)
        {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(item);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Pattern wordCountPattern = Pattern.compile("(\\S+)\t(.*)");
        Matcher m;
        String line;
        while (!(line = br.readLine()).isEmpty()) {
            m = wordCountPattern.matcher(line);
            System.out.println(line + "----->" + m.matches());
        }
    }
}
