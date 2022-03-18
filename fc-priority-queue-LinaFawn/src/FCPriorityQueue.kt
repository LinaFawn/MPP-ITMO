import java.util.*
import kotlinx.atomicfu.*

class FCPriorityQueue<E : Comparable<E>> {
    private val q = PriorityQueue<E>()
    private val lock = atomic(false)
    private val arrSize = 15
    private val operationsArray = atomicArrayOfNulls<Node<E>>(arrSize)

    class Node<E>(var operation: Operations, var element: E?)

    enum class Operations {
        ADD,
        POLL,
        PEEK,
        DONE
    }

    private fun tryLock(): Boolean {
        return lock.compareAndSet(false, true)
    }

    private fun unlock() {
        lock.getAndSet(false)
    }

    private fun fcCombaining(node: Node<E>): E? {
        while (true) {
            val index = Random().nextInt(arrSize)

            if (operationsArray[index].compareAndSet(null, node)) {
                if (tryLock()) {
                    for (i in 0 until arrSize) {
                        val value = operationsArray[i].value
                        if (value != null) {
                            if (value.operation != Operations.DONE) {
                                if (value.operation == Operations.ADD) {
                                    q.add(value.element)
                                }
                                if (value.operation == Operations.POLL) {
                                    value.element = q.poll()
                                }
                                if (value.operation == Operations.PEEK){
                                    value.element = q.peek()
                                }
                                value.operation = Operations.DONE
                            }
                        }
                    }
                    unlock()
                }

                if (node.operation == Operations.DONE) {
                    val result = node.element
                    operationsArray[index].value = null
                    return result
                }
            }
        }
    }

    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        return fcCombaining(Node(Operations.POLL, null))
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        return fcCombaining(Node(Operations.PEEK, null))
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        fcCombaining(Node(Operations.ADD, element))
    }
}