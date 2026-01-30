package ds.tree;

import ds.list.LinkedList;

public class TreeNode {
    public Object data; // داده (مثلاً نام ملک، رنگ، یا نام بازیکن)
    public int key;     // می‌تواند شناسه ملک باشد (اختیاری)
    public TreeNode left;  // برای BST (در این ساختار استفاده نمی‌شود اما بودن آن ضرر ندارد)
    public TreeNode right; // برای BST
    public LinkedList children; // لیست فرزندان (برای درخت عمومی AssetTree)

    public TreeNode(int key, Object data) {
        this.key = key;
        this.data = data;
        this.left = null;
        this.right = null;
        this.children = new LinkedList(); // استفاده از لیست پیوندی دست‌نویس
    }

    @Override
    public String toString() {
        return data.toString();
    }
}