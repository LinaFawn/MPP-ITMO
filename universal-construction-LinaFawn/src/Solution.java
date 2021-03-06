/**
 * @author :TODO: LastName FirstName
 */
public class Solution implements AtomicCounter {
    // объявите здесь нужные вам поля

    private final Node head = new Node(0);
    private final ThreadLocal<Node> tail =
            ThreadLocal.withInitial(() -> head);

    public int getAndAdd(int x) {
        while (true) {
            final int oldValue = tail.get().value;
            final int newValue = oldValue + x;
            final Node newNode = new Node(newValue);
            tail.set(tail.get().next.decide(newNode));
            if (tail.get() == newNode) {
                return oldValue;
            }
        }
    }

    // вам наверняка потребуется дополнительный класс
    private static class Node {
        final int value;
        final Consensus<Node> next = new Consensus<>();

        Node(int value) {
            this.value = value;
        }
    }
}