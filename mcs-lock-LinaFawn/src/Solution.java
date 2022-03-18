import java.util.concurrent.atomic.AtomicReference;

public class Solution implements Lock<Solution.Node> {
    private final Environment env;
    private final AtomicReference<Node> last;

    // todo: необходимые поля (final, используем AtomicReference)

    public Solution(Environment env) {
        this.env = env;
        last = new AtomicReference<>(null);
    }

    @Override
    public Node lock() {
        // todo: алгоритм
        Node my = new Node(); // сделали узел
        Node prev = last.getAndSet(my);

        if (prev != null) {
            prev.next.set(my);
            while (my.locked.get()) {
                env.park();
            }
        }

        return my; // вернули узел
    }

    @Override
    public void unlock(Node node) {
        // todo: алгоритм

        if (node.next.get() == null) {
            if (last.compareAndSet(node, null)) {
                return;
            }

            while (node.next.get() == null) {

            }
        }

        node.next.get().locked.set(false);
        env.unpark(node.next.get().thread);
    }

    static class Node {
        // todo: необходимые поля (final, используем AtomicReference)
        final Thread thread = Thread.currentThread();
        final AtomicReference<Boolean> locked = new AtomicReference<>(true);
        final AtomicReference<Node> next = new AtomicReference<>(null);
    }
}