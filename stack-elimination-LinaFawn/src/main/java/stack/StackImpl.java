package stack;

import kotlinx.atomicfu.AtomicRef;

import java.util.Random;

public class StackImpl implements Stack {

    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);
    private EliminationArray eliminationArray = new EliminationArray(8);

    private static class Cell {
        States state;
        volatile int value;

        Cell() {
            this.state = States.FREE;
            this.value = 0;
        }

        Cell(States state, int value) {
            this.state = state;
            this.value = value;
        }
    }

    private static class EliminationArray {
        AtomicRef<Cell>[] array;
        private Random random;

        EliminationArray(int length) {
            array = new AtomicRef[length];
            random = new Random();

            for (int i = 0; i < length; ++i) {
                array[i] = new AtomicRef<>(new Cell());
            }
        }

        int getLeftNeighbour(int current, int range) {
            return Math.max(0, current - range);
        }

        int getRightNeighbour(int current, int range) {
            return Math.min(array.length, current + range);
        }
    }

    private boolean tryPush(int x) {
        int current = eliminationArray.random.nextInt(eliminationArray.array.length);
        int leftNeighbour = eliminationArray.getLeftNeighbour(current, 2);
        int rightNeighbour = eliminationArray.getRightNeighbour(current, 2);

        Cell busyCell = new Cell(States.BUSY, x);

        for (int i = leftNeighbour; i < rightNeighbour; ++i) {
            Cell cellValue = eliminationArray.array[i].getValue();

            if (cellValue.state == States.FREE) {
                if (eliminationArray.array[current].compareAndSet(cellValue, busyCell)) {
                    Cell freeCell = new Cell();

                    for (int j = 0; j < 8; ++j) {
                        Cell cell2 = eliminationArray.array[current].getValue();
                        if (cell2.state == States.DONE) {
                            if (eliminationArray.array[current].compareAndSet(
                                    cell2, freeCell)) {
                                return true;
                            }
                        }
                    }

                    Cell freeCellAccepted = eliminationArray.array[current].getAndSet(freeCell);
                    return freeCellAccepted.state == States.DONE;
                }
            }
        }
        return false;
    }

    @Override
    public void push(int x) {

        if (!tryPush(x)) {
            while (true) {
                Node H = head.getValue();
                Node newHead = new Node(x, H);
                if (head.compareAndSet(H, newHead)) {
                    return;
                }
            }
        }
    }

    @Override
    public int pop() {
        for (int i = 0; i < 4; ++i) {
            int current = eliminationArray.random.nextInt(eliminationArray.array.length);
            AtomicRef<Cell> cell = eliminationArray.array[current];
            Cell cellValue = cell.getValue();

            if (cellValue.state == States.BUSY) {
                if (cell.compareAndSet(cellValue, new Cell(States.DONE, cellValue.value))) {
                    return cellValue.value;
                }
            }
        }

        while (true) {
            Node H = head.getValue();
            if (H == null) {
                return Integer.MIN_VALUE;
            }
            if (head.compareAndSet(H, H.next.getValue())) {
                return H.x;
            }
        }
    }
}