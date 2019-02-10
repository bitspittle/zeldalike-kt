package bitspittle.game2d.extras.memory

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.fail


class PoolTest {
    data class Person(var name: String = "", var age: Int = 0)
    data class MutableString(var value: String = "")
    data class MutableInt(var value: Int = 0)

    @Test
    fun `assert defaults`() {
        val pool = Pool({ Person() })
        assertThat(pool.size).isEqualTo(0)
        assertThat(pool.capacity).isEqualTo(Pool.DEFAULT_CAPACITY)
    }

    @Test
    fun `getItem works if its handle is valid`() {
        val pool = Pool({ Person() })

        val handlePerson = pool.newItem()

        var itemRetrieved = false
        pool.getItem(handlePerson) {
            itemRetrieved = true
        }
        assertThat(handlePerson.isValid()).isTrue()
        assertThat(itemRetrieved).isTrue()

        pool.freeItem(handlePerson)

        itemRetrieved = false
        pool.getItem(handlePerson) {
            itemRetrieved = true
        }
        assertThat(handlePerson.isValid()).isFalse()
        assertThat(itemRetrieved).isFalse()
    }

    @Test
    fun `can fill and empty the pool`() {
        val pool = Pool({ Person() })

        repeat(2) {
            // make sure we can tear down and build up again repeatedly
            for (i in 0 until Pool.DEFAULT_CAPACITY) {
                pool.newItem()
                assertThat(pool.size).isEqualTo(i + 1)
            }
            assertThat(pool.size).isEqualTo(Pool.DEFAULT_CAPACITY)

            var expectedSize = pool.size
            val handles = pool.handles
            // Remove in weird, non-sequential order, to expose possible issues with
            // reusing free slots
            for (odd in 1 until handles.size step 2) {
                pool.freeItem(handles[odd])
                expectedSize--
                assertThat(pool.size).isEqualTo(expectedSize)
            }
            for (even in 0 until handles.size step 2) {
                pool.freeItem(handles[even])
                expectedSize--
                assertThat(pool.size).isEqualTo(expectedSize)
            }
            assertThat(pool.isEmpty).isTrue()
        }
    }

    @Test
    fun `can add, remove, and query pool items`() {
        val pool = Pool({ Person() })

        val handleJoe = pool.newItem {
            name = "Joe"
            age = 23
        }
        assertThat(pool.size).isEqualTo(1)

        val handleJane = pool.newItem {
            name = "Jane"
            age = 27
        }
        assertThat(pool.size).isEqualTo(2)

        val handlePat = pool.newItem {
            name = "Pat"
            age = 45
        }
        assertThat(pool.size).isEqualTo(3)

        assertThat(handleJoe.isValid()).isTrue()
        pool.getItem(handleJoe) {
            assertThat(name).isEqualTo("Joe")
            assertThat(age).isEqualTo(23)
        }
        assertThat(handleJane.isValid()).isTrue()
        pool.getItem(handleJane) {
            assertThat(name).isEqualTo("Jane")
            assertThat(age).isEqualTo(27)
        }
        assertThat(handlePat.isValid()).isTrue()
        pool.getItem(handlePat) {
            assertThat(name).isEqualTo("Pat")
            assertThat(age).isEqualTo(45)
        }

        pool.freeItem(handleJane)
        assertThat(handleJane.isValid()).isFalse()
        assertThat(pool.size).isEqualTo(2)

        pool.getItem(handleJane) {
            fail("getItem callback called on removed person unexpectedly")
        }
        pool.freeItem(handleJane) // No-op but allowed

        // Internally, allocating after an item was removed should re-use it
        val handleJack = pool.newItem {
            name = "Jack"
            age = 35
        }
        assertThat(pool.size).isEqualTo(3)
        assertThat(handleJack.isValid()).isTrue()
        assertThat(handleJane.isValid()).isFalse()

        // Remove everyone else and make sure we can still add an object to an empty pool
        pool.freeItem(handlePat)
        pool.freeItem(handleJoe)
        pool.freeItem(handleJack)
        assertThat(pool.size).isEqualTo(0)

        val handleJill = pool.newItem {
            name = "Jill"
            age = 35
        }

        assertThat(pool.size).isEqualTo(1)
        pool.freeItem(handleJill)
        assertThat(pool.size).isEqualTo(0)
    }

    @Test
    fun `removing multiple times is harmless`() {
        val pool = Pool({ MutableString() })
        val handleLorem = pool.newItem { value = "lorem" }
        pool.newItem { value = "ipsum" }

        pool.freeItem(handleLorem)
        pool.freeItem(handleLorem)
        pool.freeItem(handleLorem)

        assertThat(pool.size).isEqualTo(1)

        pool.newItem { value = "dolor" }
        pool.newItem { value = "sit" }
        pool.newItem { value = "amet" }

        assertThat(pool.size).isEqualTo(4)
    }

    @Test
    fun `capacity must be greater than 0`() {
        assertThrows<IllegalArgumentException> {
            Pool({ "dummy" }, 0)
        }
    }

    @Test
    fun `can iterate items`() {
        val pool = Pool({ MutableInt() })

        for (i in 1..10) {
            pool.newItem { value = i }
        }

        for (item in pool) {
            item.value *= 2
        }

        assertThat(pool.map { it.value }).containsExactlyElementsIn((2..20 step 2)).inOrder()
    }

    @Test
    fun `can iterate handles`() {
        val pool = Pool({ MutableInt() })
        for (i in 1 .. 10) {
            pool.newItem { value = i }
        }

        for (handle in pool.handles) {
            pool.getItem(handle) { value *= 2 }
        }

        assertThat(pool.map { it.value }).containsExactlyElementsIn((2 .. 20 step 2)).inOrder()
    }
}
