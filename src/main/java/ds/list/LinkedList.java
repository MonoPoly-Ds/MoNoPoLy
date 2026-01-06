package ds.list;

public class LinkedList {
    private Node head;
    private Node tail;
    private int size;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }


    public void add(Object data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = newNode;
            tail = newNode;
            newNode.next = head; // اشاره به خودش (حلقه تک‌عضوی)
        } else {
            tail.next = newNode; // اتصال آخر قبلی به جدید
            tail = newNode;      // آپدیت کردن tail
            tail.next = head;    // بستن حلقه (اتصال tail جدید به head)
        }
        size++;
    }

    // متد حرکت روی خانه‌ها (مهم برای بازی)
    public Node move(Node startNode, int steps) {
        if (startNode == null) return head; // اگر نال بود از شروع برو

        Node current = startNode;
        for (int i = 0; i < steps; i++) {
            if (current.next != null) {
                current = current.next;
            } else {
                // اگر لینک لیست پاره شده باشد (نباید اتفاق بیفتد)
                current = head;
            }
        }
        return current;
    }

    public Node getHead() {
        return head;
    }

    public int size() {
        return size;
    }
}