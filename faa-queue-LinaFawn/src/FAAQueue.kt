import kotlinx.atomicfu.*

class FAAQueue<T> {
    private val head: AtomicRef<Segment> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicRef<Segment> // Tail pointer, similarly to the Michael-Scott queue

    init {
        val firstNode = Segment()
        head = atomic(firstNode)
        tail = atomic(firstNode)
    }

    /**
     * Adds the specified element [x] to the queue.
     */
    fun enqueue(x: T) {
        while (true){
            val curTail = tail.value
            val enqIdx = curTail.enqIdx.getAndIncrement()

            if (enqIdx >= SEGMENT_SIZE) {
                val newTail = Segment(x)
                if (curTail.next.compareAndSet(null, newTail)) {
                    tail.compareAndSet(curTail, newTail)
                    return
                }

                tail.compareAndSet(curTail, curTail.next.value!!)

            } else if (curTail.segmentElements[enqIdx].compareAndSet(null, x)) {
                return
            }
        }
    }

    /**
     * Retrieves the first element from the queue
     * and returns it; returns `null` if the queue
     * is empty.
     */
    fun dequeue(): T? {
        while (true) {
            val curHead = head.value
            val deqIdx = curHead.deqIdx.getAndIncrement()

            if (deqIdx >= SEGMENT_SIZE) {
                val headNext = curHead.next.value
                if (headNext == null)
                    return null

                head.compareAndSet(curHead, headNext)
                continue
            }

            val result = curHead.segmentElements[deqIdx].getAndSet(DONE)
            if(result == null)
                continue

            return result as T?
        }
    }

    /**
     * Returns `true` if this queue is empty;
     * `false` otherwise.
     */
    val isEmpty: Boolean get() {
        val head = head.value
        val deqInd = head.deqIdx.value
        if (deqInd >= SEGMENT_SIZE){
            val headNext = head.next.value
            if (headNext == null){
                return true
            }
        }
        return false
    }
}

private class Segment {

    val next: AtomicRef<Segment?> = atomic(null)
    val enqIdx = atomic(0) // index for the next enqueue operation
    val deqIdx = atomic(0) // index for the next dequeue operation
    val segmentElements = atomicArrayOfNulls<Any>(SEGMENT_SIZE)

    constructor() // for the first segment creation

    constructor(x: Any?) { // each next new segment should be constructed with an element
        enqIdx.value = 1
        segmentElements[0].value = x
    }
}

private val DONE = Any() // Marker for the "DONE" slot state; to avoid memory leaks
const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS

