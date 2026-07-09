package armyc2.c5isr.renderer.symbolpicker;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import armyc2.c5isr.renderer.R;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.MSInfo;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

public class TreeManager {
    private static final Set<String> SYMBOL_BLACKLIST = new HashSet<>(Arrays.asList(
            // Symbols with no SVG or drawing in standard
            "47", // Meteorological space is an empty Symbol Set currently

            // Symbols with ambiguous draw rules
            "45162004" // Tropical Storm Wind Areas
    ));

    public Node mil2525Tree;

    /** Reads the symbols from msd.txt and builds a tree.
     * @param context Application context in which to use the tree.
     * @param versions mil std 2525 versions to add to tree
     * @throws IOException if there is an error reading msd.txt
     */
    public void buildTree(Context context, int[] versions) throws IOException {
        mil2525Tree = new Node("Root", "XX", "XX", "XX");
        for (int version : versions){
            addToTree(context, version);
        }
    }

    private void addToTree(Context context, int version) throws IOException {
        Stack<Node> parentStack = new Stack<>();
        Node child = mil2525Tree;
        String line;
        String symbolSet = "";

        InputStream is;
        if (version >= SymbolID.Version_2525E) {
            is = context.getResources().openRawResource(R.raw.mse);
        } else {
            is = context.getResources().openRawResource(R.raw.msd);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        try {
            while ((line = br.readLine()) != null) {

                boolean newSymbolSet = false;
                String[] segments = line.split("\\t");
                //lookup doesn't have 13 values.  Add 13 wherever there's a 15 for now.
                if(segments[5].contains("15")) {
                    segments[5] = "13," + segments[5];

                    line = "";

                    for(int lcv = 0; lcv < segments.length; lcv++)
                    {
                        line += segments[lcv];
                        if(lcv < segments.length-1)
                            line += "\t";
                    }
                }

                if (segments[5].contains(String.valueOf(version))) {
                    // count tabs to calculate nodeDepth
                    int nodeDepth = 1;

                    if(SymbolUtilities.isNumber(line.substring(0,2))) {
                        newSymbolSet = true;
                        line = line.substring(2);
                    }
                    while (line.charAt(0) == '\t') {
                        line = line.substring(1);
                        nodeDepth++;
                    }

                    if (nodeDepth > parentStack.size()) {
                        parentStack.push(child);
                    }
                    while (nodeDepth < parentStack.size()) {
                        parentStack.pop();
                    }

                    // special case for parsing the Symbol Set codes since they're only 2 digits
                    if(newSymbolSet){//(nodeDepth == 1 && !segments[0].isEmpty()) {
                        symbolSet = segments[0];

                        if (SYMBOL_BLACKLIST.contains(symbolSet)) {
                            continue;
                        }

                        child = getChild(parentStack.peek(), symbolSet, "000000");
                        if (child == null) {
                            child = new Node(MSInfo.parseSymbolSetName(symbolSet, version), String.valueOf(version), symbolSet, "000000");

                            //new symbol set so go back to the root to add it to the root
                            while(parentStack.size() > 1)
                                parentStack.pop();

                            parentStack.peek().addChild(child);
                        }

                        if (segments[1].equals("Unspecified")) {
                            // Ignore rest of line
                            continue;
                        } else {
                            // There is a subfolder on this line, add parent and continue parsing
                            parentStack.push(child);
                        }
                    }

                    // skip "{Reserved for future use}" codes
                    if (!line.toLowerCase().contains("{reserved for future use}")) {
                        segments = line.split("\\t");
                        String name;

                        name = segments[0];

                        // XXXXXX would indicate an error reading the file where it couldn't find 6 digits
                        String code = "XXXXXX";
                        // extract 6-digit decimal code from remainder of line segments
                        for (int i = 1; i < segments.length; i++) {
                            if (segments[i].matches("\\d{6}")) {
                                code = segments[i];
                                break;
                            }
                        }

                        if (SYMBOL_BLACKLIST.contains(symbolSet) || SYMBOL_BLACKLIST.contains(symbolSet + code)) {
                            continue;
                        }

                        child = getChild(parentStack.peek(), symbolSet, code);
                        if (child == null) {
                            child = new Node(name, String.valueOf(version), symbolSet, code);
                            parentStack.peek().addChild(child);
                        }
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Node getChild(Node parent, String symbolSet, String entityCode) {
        for (Node child : parent.getChildren()){
            if (child.getSymbolSetCode().equals(symbolSet) && child.getCode().equals(entityCode))
                return child;
        }
        return null;
    }
}
