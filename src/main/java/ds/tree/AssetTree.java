package ds.tree;

import ds.list.LinkedList;
import ds.list.Node;

public class AssetTree {
    private TreeNode root;

    // ریشه درخت، نام خودِ بازیکن است
    public AssetTree(String playerName) {
        this.root = new TreeNode(0, playerName);
    }

    public TreeNode getRoot() {
        return root;
    }

    /**
     * افزودن ملک به درخت دارایی‌ها
     * ساختار: بازیکن -> گروه رنگی -> ملک
     */
    public void addProperty(String colorGroup, String propertyName, int propertyId) {
        // ۱. ابتدا دنبال نود "گروه رنگی" در فرزندان ریشه می‌گردیم
        TreeNode colorNode = findChild(root, colorGroup);

        // اگر این رنگ هنوز وجود نداشت (اولین ملک از این رنگ)، آن را می‌سازیم
        if (colorNode == null) {
            colorNode = new TreeNode(0, colorGroup);
            root.children.add(colorNode);
        }

        // ۲. حالا ملک را به زیرمجموعه آن رنگ اضافه می‌کنیم
        // چک می‌کنیم ملک تکراری نباشد
        if (findChild(colorNode, propertyName) == null) {
            TreeNode propertyNode = new TreeNode(propertyId, propertyName);
            colorNode.children.add(propertyNode);
        }
    }

    /**
     * افزودن ساختمان (خانه/هتل) به یک ملک خاص
     * ساختار: ... -> ملک -> ساختمان
     */
    public void addBuilding(String propertyName, String buildingType) {
        // ملک می‌تواند در هر گروه رنگی باشد، پس باید پیدایش کنیم
        TreeNode propertyNode = findPropertyNode(propertyName);

        if (propertyNode != null) {
            TreeNode buildingNode = new TreeNode(0, buildingType);
            propertyNode.children.add(buildingNode);
        }
    }

    // --- متدهای کمکی برای جستجو در لیست پیوندی و درخت ---

    // جستجوی فرزند مستقیم (مثلاً پیدا کردن رنگ در زیرمجموعه بازیکن)
    private TreeNode findChild(TreeNode parent, String name) {
        Node current = parent.children.getHead();
        if (current == null) return null;

        Node head = current;
        do {
            TreeNode child = (TreeNode) current.data;
            if (child.data.toString().equals(name)) {
                return child;
            }
            current = current.next;
        } while (current != null && current != head); // هندل کردن لیست حلقوی
        return null;
    }

    // جستجوی عمیق برای پیدا کردن ملک (چون ملک در لایه دوم است)
    private TreeNode findPropertyNode(String propertyName) {
        Node colorNodeWrapper = root.children.getHead();
        if (colorNodeWrapper == null) return null;
        Node head = colorNodeWrapper;

        // پیمایش روی رنگ‌ها
        do {
            TreeNode colorNode = (TreeNode) colorNodeWrapper.data;
            // پیمایش روی املاکِ آن رنگ
            TreeNode found = findChild(colorNode, propertyName);
            if (found != null) return found;

            colorNodeWrapper = colorNodeWrapper.next;
        } while (colorNodeWrapper != null && colorNodeWrapper != head);

        return null;
    }

    // متد پیمایش (Traversal) برای نمایش گزارش در کنسول
    public String printTree() {
        StringBuilder sb = new StringBuilder();
        sb.append("Asset Structure for: ").append(root.data).append("\n");
        printRecursive(root, 0, sb);
        return sb.toString();
    }

    private void printRecursive(TreeNode node, int level, StringBuilder sb) {
        // ایجاد فاصله برای نمایش سلسله‌مراتب
        for (int i = 0; i < level; i++) sb.append("    ");
        if (level > 0) sb.append("|-- ");
        if (level > 0) sb.append(node.data).append("\n");

        Node current = node.children.getHead();
        if (current == null) return;
        Node head = current;

        do {
            printRecursive((TreeNode) current.data, level + 1, sb);
            current = current.next;
        } while (current != null && current != head);
    }
}