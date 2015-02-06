
package src;
import java.io.*;
import java.util.*;

/**
 * Created by vijay on 1/20/15.
 */
public class NBUtils {
    private static final String[] STOP_WORDS = {"a","about","above","across","after","again","against","all","almost","alone","along","already","also","although","always","among","an","and","another","any","anybody","anyone","anything","anywhere","are","area","areas","around","as","ask","asked","asking","asks","at","away","b","back","backed","backing","backs","be","became","because","become","becomes","been","before","began","behind","being","beings","best","better","between","big","both","but","by","c","came","can","cannot","case","cases","certain","certainly","clear","clearly","come","could","d","did","differ","different","differently","do","does","done","down","down","downed","downing","downs","during","e","each","early","either","end","ended","ending","ends","enough","even","evenly","ever","every","everybody","everyone","everything","everywhere","f","face","faces","fact","facts","far","felt","few","find","finds","first","for","four","from","full","fully","further","furthered","furthering","furthers","g","gave","general","generally","get","gets","give","given","gives","go","going","good","goods","got","great","greater","greatest","group","grouped","grouping","groups","h","had","has","have","having","he","her","here","herself","high","high","high","higher","highest","him","himself","his","how","however","i","if","important","in","interest","interested","interesting","interests","into","is","it","its","itself","j","just","k","keep","keeps","kind","knew","know","known","knows","l","large","largely","last","later","latest","least","less","let","lets","like","likely","long","longer","longest","m","made","make","making","man","many","may","me","member","members","men","might","more","most","mostly","mr","mrs","much","must","my","myself","n","necessary","need","needed","needing","needs","never","new","new","newer","newest","next","no","nobody","non","noone","not","nothing","now","nowhere","number","numbers","o","of","off","often","old","older","oldest","on","once","one","only","open","opened","opening","opens","or","order","ordered","ordering","orders","other","others","our","out","over","p","part","parted","parting","parts","per","perhaps","place","places","point","pointed","pointing","points","possible","present","presented","presenting","presents","problem","problems","put","puts","q","quite","r","rather","really","right","right","room","rooms","s","said","same","saw","say","says","second","seconds","see","seem","seemed","seeming","seems","sees","several","shall","she","should","show","showed","showing","shows","side","sides","since","small","smaller","smallest","so","some","somebody","someone","something","somewhere","state","states","still","still","such","sure","t","take","taken","than","that","the","their","them","then","there","therefore","these","they","thing","things","think","thinks","this","those","though","thought","thoughts","three","through","thus","to","today","together","too","took","toward","turn","turned","turning","turns","two","u","under","until","up","upon","us","use","used","uses","v","very","w","want","wanted","wanting","wants","was","way","ways","we","well","wells","went","were","what","when","where","whether","which","while","who","whole","whose","why","will","with","within","without","work","worked","working","works","would","x","y","year","years","yet","you","young","younger","youngest","your","yours","z"};
    private static final Set<String> STOP_WORD_SET = new HashSet<String>(Arrays.asList(STOP_WORDS));
    //private static final Set<String> STOP_WORD_SET = new HashSet<String>();
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
            if (words[i].matches("[a-zA-z]{3,}") && !STOP_WORD_SET.contains(words[i])) {
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

    static String join(Iterator<String> iter, String conjunction)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        while (iter.hasNext())
        {
            if (first)
                first = false;
            else
                sb.append(conjunction);
            sb.append(iter.next());
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        String[] split = ",1,2,3".split(",");
        for (String s : split) {
            System.out.println(s + " " + s.length());
        }
    }
}
