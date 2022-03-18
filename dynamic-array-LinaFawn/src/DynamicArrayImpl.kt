import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))
    private val arraySize = atomic(0)

    override fun get(index: Int): E {
        if (index >= size) {
            throw IllegalArgumentException()
        }

        while (true) {
            val curArray = core.value

            if (index > curArray.capacity - 1)
                continue

            val value = curArray.array[index].value
            if (value != null) {
                return value.element.value
            }
        }
    }

    override fun put(index: Int, element: E) {
        if (index >= size) {
            throw IllegalArgumentException()
        }

        while (true) {
            val curArray = core.value

            if (index > curArray.capacity - 1)
                continue

            val value = curArray.array[index].value

            if (value != null){
                if (value.element.compareAndSet(value.element.value, element) && curArray.array[index].value == value) {
                    return
                }
            }
        }
    }

    override fun pushBack(element: E) {
        val curSize = arraySize.getAndIncrement()

        while (true) {
            val curArray = core.value

            if (curSize > curArray.capacity - 1) {
                val nextArray = Core<E>(2 * curArray.capacity)

                if (curArray.next.compareAndSet(null, nextArray)) {
                    for (i in 0 until curArray.capacity) {
                        while (true) {
                            val value = curArray.array[i].getAndSet(null)
                            if (value != null) {
                                nextArray.array[i].compareAndSet(null, value)
                                break
                            }
                        }
                    }
                    core.compareAndSet(curArray, nextArray)
                }
            } else if (curArray.array[curSize].compareAndSet(null, CoreWrapper(atomic(element)))) {
                break
            }
        }
    }

    override val size: Int
        get() {
            return arraySize.value
        }
}

private class CoreWrapper<T>(val element: AtomicRef<T>)

private class Core<E>(val capacity: Int) {
    val array = atomicArrayOfNulls<CoreWrapper<E>>(capacity)
    val next: AtomicRef<Core<E>?> = atomic(null)
}

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME