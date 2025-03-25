package armyc2.c5isr.renderer.symbolpicker;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class Node {

    private final String name;
    private final String code;
    private final String symbolSetCode;
    private final String version;
    private final ArrayList<Node> children;

    public Node(String name, String version, String symbolSet, String code) {
        this.name = name;
        this.version = version;
        this.symbolSetCode = symbolSet;
        this.code = code;
        children = new ArrayList<>();
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public void addChildren(List<Node> nodes) {
        children.addAll(nodes);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSymbolSetCode() {
        return symbolSetCode;
    }

    public String getVersion() {
        return version;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public ArrayList<Node> flatten() {
        ArrayList<Node> result = new ArrayList<>();
        for (Node child : children) {
            result.add(child);
            result.addAll(child.flatten());
        }
        return result;
    }

    /** Calculates the size of the tree with this Node as the root.
     * @return number of Nodes in this tree
     */
    public int getSize() {
        int size = 1;
        for (Node n : children) {
            size += n.getSize();
        }
        return size;
    }

    /** Prints the entire subtree of this Node to the debug Log.
     * @param depth Number of tabs to indent this Node in the log. Children will be indented further.
     */
    public void logTree(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("\t");
        }
        Log.d("Node", indent + name + "\t" + code);
        for (Node n : children) {
            n.logTree(depth + 1);
        }
    }
}