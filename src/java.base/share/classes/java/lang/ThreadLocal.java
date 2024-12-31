/*
 * Copyright (c) 1997, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jdk.internal.misc.CarrierThreadLocal;
import jdk.internal.misc.TerminatingThreadLocal;
import sun.security.action.GetPropertyAction;

/**
 * This class provides thread-local variables.  These variables differ from
 * their normal counterparts in that each thread that accesses one (via its
 * {@code get} or {@code set} method) has its own, independently initialized
 * copy of the variable.  {@code ThreadLocal} instances are typically private
 * static fields in classes that wish to associate state with a thread (e.g.,
 * a user ID or Transaction ID).
 *
 * <p>For example, the class below generates unique identifiers local to each
 * thread.
 * A thread's id is assigned the first time it invokes {@code ThreadId.get()}
 * and remains unchanged on subsequent calls.
 * <pre>
 * import java.util.concurrent.atomic.AtomicInteger;
 *
 * public class ThreadId {
 *     // Atomic integer containing the next thread ID to be assigned
 *     private static final AtomicInteger nextId = new AtomicInteger(0);
 *
 *     // Thread local variable containing each thread's ID
 *     private static final ThreadLocal&lt;Integer&gt; threadId =
 *         new ThreadLocal&lt;Integer&gt;() {
 *             &#64;Override protected Integer initialValue() {
 *                 return nextId.getAndIncrement();
 *         }
 *     };
 *
 *     // Returns the current thread's unique ID, assigning it if necessary
 *     public static int get() {
 *         return threadId.get();
 *     }
 * }
 * </pre>
 * <p>Each thread holds an implicit reference to its copy of a thread-local
 * variable as long as the thread is alive and the {@code ThreadLocal}
 * instance is accessible; after a thread goes away, all of its copies of
 * thread-local instances are subject to garbage collection (unless other
 * references to these copies exist).
 * @param <T> the type of the thread local's value
 *
 * @author Josh Bloch and Doug Lea
 * @since 1.2
 */
public class ThreadLocal<T> {
    private static final boolean TRACE_VTHREAD_LOCALS = traceVirtualThreadLocals();

    /**
     * ThreadLocals 依赖于附加到每个线程的每线程线性探测哈希映射（Thread.threadLocals 和 inheritableThreadLocals）。
     * ThreadLocal 对象充当键，通过 threadLocalHashCode 进行搜索。
     * 这是一个自定义的哈希码（仅在 ThreadLocalMaps 中有用），它消除了在常见情况下连续构造的 ThreadLocals 被同一线程使用时的哈希冲突，
     * 同时在不常见的情况下仍然表现良好。
     */
    private final int threadLocalHashCode = nextHashCode();

    /**
     * The next hash code to be given out. Updated atomically. Starts at
     * zero.
     */
    private static final AtomicInteger nextHashCode = new AtomicInteger();

    /**
     * 连续生成的哈希码之间的差异——将隐式的顺序线程局部 ID 转换为接近最优分布的乘法哈希值，
     * 适用于大小为二的幂的哈希表。
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Returns the next hash code.
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * Returns the current thread's "initial value" for this
     * thread-local variable.  This method will be invoked the first
     * time a thread accesses the variable with the {@link #get}
     * method, unless the thread previously invoked the {@link #set}
     * method, in which case the {@code initialValue} method will not
     * be invoked for the thread.  Normally, this method is invoked at
     * most once per thread, but it may be invoked again in case of
     * subsequent invocations of {@link #remove} followed by {@link #get}.
     *
     * @implSpec
     * This implementation simply returns {@code null}; if the
     * programmer desires thread-local variables to have an initial
     * value other than {@code null}, then either {@code ThreadLocal}
     * can be subclassed and this method overridden or the method
     * {@link ThreadLocal#withInitial(Supplier)} can be used to
     * construct a {@code ThreadLocal}.
     *
     * @return the initial value for this thread-local
     * @see #withInitial(java.util.function.Supplier)
     */
    protected T initialValue() {
        return null;
    }

    /**
     * Creates a thread local variable. The initial value of the variable is
     * determined by invoking the {@code get} method on the {@code Supplier}.
     *
     * @param <S> the type of the thread local's value
     * @param supplier the supplier to be used to determine the initial value
     * @return a new thread local variable
     * @throws NullPointerException if the specified supplier is null
     * @since 1.8
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * 创建一个线程局部变量。
     * @see #withInitial(java.util.function.Supplier)
     */
    public ThreadLocal() {
    }

    /**
     * Returns the value in the current thread's copy of this
     * thread-local variable.  If the variable has no value for the
     * current thread, it is first initialized to the value returned
     * by an invocation of the {@link #initialValue} method.
     *
     * @return the current thread's value of this thread-local
     */
    public T get() {
        return get(Thread.currentThread());
    }

    /**
     * Returns the value in the current carrier thread's copy of this
     * thread-local variable.
     */
    T getCarrierThreadLocal() {
        assert this instanceof CarrierThreadLocal<T>;
        return get(Thread.currentCarrierThread());
    }

    private T get(Thread t) {
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T) e.value;
                return result;
            }
        }
        return setInitialValue(t);
    }

    /**
     * Returns {@code true} if there is a value in the current carrier thread's copy of
     * this thread-local variable, even if that values is {@code null}.
     *
     * @return {@code true} if current carrier thread has associated value in this
     *         thread-local variable; {@code false} if not
     */
    boolean isCarrierThreadLocalPresent() {
        assert this instanceof CarrierThreadLocal<T>;
        return isPresent(Thread.currentCarrierThread());
    }

    private boolean isPresent(Thread t) {
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            return map.getEntry(this) != null;
        } else {
            return false;
        }
    }

    /**
     * Variant of set() to establish initialValue. Used instead
     * of set() in case user has overridden the set() method.
     *
     * @return the initial value
     */
    private T setInitialValue(Thread t) {
        T value = initialValue();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            map.set(this, value);
        } else {
            createMap(t, value);
        }
        if (this instanceof TerminatingThreadLocal<?> ttl) {
            TerminatingThreadLocal.register(ttl);
        }
        if (TRACE_VTHREAD_LOCALS && t == Thread.currentThread() && t.isVirtual()) {
            printStackTrace();
        }
        return value;
    }

    /**
     * 将当前线程的线程局部变量的副本设置为指定的值。
     * 大多数子类无需覆盖此方法，可以仅依赖 {@link #initialValue} 方法来设置线程局部变量的值。
     *
     * @param value 要存储在当前线程副本中的值
     */
    public void set(T value) {
        // 调用内部的 set 方法，实际设置当前线程的线程局部变量
        set(Thread.currentThread(), value);
        // 如果启用了虚拟线程的追踪，并且当前线程是虚拟线程，打印栈追踪
        if (TRACE_VTHREAD_LOCALS && Thread.currentThread().isVirtual()) {
            printStackTrace();
        }
    }


    void setCarrierThreadLocal(T value) {
        assert this instanceof CarrierThreadLocal<T>;
        set(Thread.currentCarrierThread(), value);
    }

    private void set(Thread t, T value) {
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            map.set(this, value);
        } else {
            createMap(t, value);
        }
    }

    /**
     * Removes the current thread's value for this thread-local
     * variable.  If this thread-local variable is subsequently
     * {@linkplain #get read} by the current thread, its value will be
     * reinitialized by invoking its {@link #initialValue} method,
     * unless its value is {@linkplain #set set} by the current thread
     * in the interim.  This may result in multiple invocations of the
     * {@code initialValue} method in the current thread.
     *
     * @since 1.5
     */
    public void remove() {
        remove(Thread.currentThread());
    }

    void removeCarrierThreadLocal() {
        assert this instanceof CarrierThreadLocal<T>;
        remove(Thread.currentCarrierThread());
    }

    private void remove(Thread t) {
        ThreadLocalMap m = getMap(t);
        if (m != null) {
            m.remove(this);
        }
    }

    /**
     * 获取 线程t 的字段 threadLocals 与 ThreadLocal 关联的映射。 在 InheritableThreadLocal 中被重写。
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * Create the map associated with a ThreadLocal. Overridden in
     * InheritableThreadLocal.
     *
     * @param t the current thread
     * @param firstValue value for the initial entry of the map
     */
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * 创建继承的线程局部变量映射的工厂方法。
     * 仅设计用于从 Thread 构造函数中调用。
     * @param  parentMap 与父线程关联的映射
     * @return 包含父线程可继承绑定的映射
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * Method childValue is visibly defined in subclass
     * InheritableThreadLocal, but is internally defined here for the
     * sake of providing createInheritedMap factory method without
     * needing to subclass the map class in InheritableThreadLocal.
     * This technique is preferable to the alternative of embedding
     * instanceof tests in methods.
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * An extension of ThreadLocal that obtains its initial value from
     * the specified {@code Supplier}.
     */
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    /**
     * ThreadLocalMap is a customized hash map suitable only for
     * maintaining thread local values. No operations are exported
     * outside of the ThreadLocal class. The class is package private to
     * allow declaration of fields in class Thread.  To help deal with
     * very large and long-lived usages, the hash table entries use
     * WeakReferences for keys. However, since reference queues are not
     * used, stale entries are guaranteed to be removed only when
     * the table starts running out of space.
     */
    static class ThreadLocalMap {

        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * The initial capacity -- MUST be a power of two.
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary.
         * table.length MUST always be a power of two.
         */
        private Entry[] table;

        /**
         * The number of entries in the table.
         */
        private int size = 0;

        /**
         * The next size value at which to resize.
         */
        private int threshold; // Default to 0

        /**
         * Set the resize threshold to maintain at worst a 2/3 load factor.
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * Increment i modulo len.
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * Decrement i modulo len.
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * Construct a new map without a table.
         */
        private ThreadLocalMap() {
        }

        /**
         * Construct a new map initially containing (firstKey, firstValue).
         * ThreadLocalMaps are constructed lazily, so we only create
         * one when we have at least one entry to put in it.
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * 构造一个新的映射，将给定父线程映射中的所有 InheritableThreadLocal 包含进来。
         * 仅通过 createInheritedMap 方法调用。
         * @param parentMap 与父线程关联的映射。
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            // 获取父线程映射中的所有条目（Entry），并计算其长度
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;

            // 设置阈值，决定表的大小，具体阈值根据实现的需要设置
            setThreshold(len);

            // 初始化当前线程的映射表，大小与父线程的映射表相同
            table = new Entry[len];

            // 遍历父线程映射中的所有条目，处理每个条目
            for (Entry e : parentTable) {
                if (e != null) {
                    // 从父线程条目中获取 ThreadLocal 键，进行类型转换
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    // 如果键不为空，表示该 ThreadLocal 依然有效
                    if (key != null) {
                        // 获取与父线程条目相关的值，并通过子类方法调整值
                        Object value = key.childValue(e.value);
                        // 创建一个新的条目，将键和值存储在其中
                        Entry c = new Entry(key, value);
                        // 计算该条目的哈希值并定位到映射表中的位置
                        int h = key.threadLocalHashCode & (len - 1);
                        // 如果目标位置已经有条目，依次查找下一个位置，直到找到空位
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        // 将新条目插入到找到的位置
                        table[h] = c;
                        size++; // 增加映射表的大小
                    }
                }
            }
        }


        /**
         * Returns the number of elements in the map.
         */
        int size() {
            return size;
        }

        /**
         * Get the entry associated with key.  This method
         * itself handles only the fast path: a direct hit of existing
         * key. It otherwise relays to getEntryAfterMiss.  This is
         * designed to maximize performance for direct hits, in part
         * by making this method readily inlinable.
         *
         * @param  key the thread local object
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.refersTo(key))
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * Version of getEntry method for use when key is not found in
         * its direct hash slot.
         *
         * @param  key the thread local object
         * @param  i the table index for key's hash code
         * @param  e the entry at table[i]
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                if (e.refersTo(key))
                    return e;
                if (e.refersTo(null))
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * 设置与指定 key 关联的值。
         *
         * @param key 线程局部变量对象
         * @param value 要设置的值
         */
        private void set(ThreadLocal<?> key, Object value) {
            // 不像 get() 方法那样使用快速路径，因为创建新条目和替换现有条目同样常见。
            // 如果使用快速路径，往往会失败，因为替换操作频繁发生。

            // 获取当前线程的本地存储映射（Entry 数组）
            Entry[] tab = table;
            int len = tab.length;
            // 计算 key 对应的索引位置，使用 key 的 hash code 和数组长度
            int i = key.threadLocalHashCode & (len - 1);

            // 遍历该索引位置的链表，查找是否已有该 key 对应的值
            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                // 判断引用e指向的对象是key (key 不为null)
                if (e.refersTo(key)) {
                    e.value = value;  // 更新该 Entry 的值
                    return;
                }
                // 判断引用e指向的对象被垃圾回收了
                if (e.refersTo(null)) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            // 如果没有找到相应的 key，直接将新的 Entry 插入该位置
            tab[i] = new Entry(key, value);

            // 更新当前大小，并清理多余的槽位，如果超过阈值则扩容
            int sz = ++size;
            // 如果槽位未清理，且当前大小超过了阈值，进行扩容操作
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }


        /**
         * Remove the entry for key.
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len - 1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.refersTo(key)) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * Replace a stale entry encountered during a set operation
         * with an entry for the specified key.  The value passed in
         * the value parameter is stored in the entry, whether or not
         * an entry already exists for the specified key.
         *
         * As a side effect, this method expunges all stale entries in the
         * "run" containing the stale entry.  (A run is a sequence of entries
         * between two null slots.)
         *
         * @param  key the key
         * @param  value the value to be associated with key
         * @param  staleSlot index of the first stale entry encountered while
         *         searching for key.
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value, int staleSlot) {
            // 获取当前哈希表的引用
            Entry[] tab = table;
            int len = tab.length;  // 哈希表的长度
            Entry e;

            // 回溯检查当前槽位之前是否存在过时条目，清理整个“run”
            // “run” 是一段连续的条目区间，直到遇到 null 插槽
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len); (e = tab[i]) != null; i = prevIndex(i, len)) {
                if (e.refersTo(null)) {
                    // 如果当前条目已经过时（即其值已经被垃圾回收），更新清理标记
                    slotToExpunge = i;
                }
            }

            // 从 staleSlot 开始向前遍历，查找目标 key 或遇到 null 插槽
            for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) {
                // 如果找到与 key 匹配的条目
                if (e.refersTo(key)) {
                    // 更新条目的值
                    e.value = value;

                    // 交换当前找到的条目和 staleSlot 位置上的条目，以保持哈希表顺序
                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // 如果有过时条目，则从当前位置开始清理
                    if (slotToExpunge == staleSlot) {
                        slotToExpunge = i;
                    }
                    // 清理过时条目
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // 如果没有找到过时条目，继续向前遍历，寻找第一个过时条目
                if (e.refersTo(null) && slotToExpunge == staleSlot) {
                    slotToExpunge = i;
                }
            }

            // 如果在表中没有找到与 key 匹配的条目，将新条目放入 staleSlot 中
            tab[staleSlot].value = null;  // 清空原来的值
            tab[staleSlot] = new Entry(key, value);  // 插入新条目

            // 如果还有其他过时条目，清理它们
            if (slotToExpunge != staleSlot) {
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
            }
        }


        /**
         * Expunge a stale entry by rehashing any possibly colliding entries
         * lying between staleSlot and the next null slot.  This also expunges
         * any other stale entries encountered before the trailing null.  See
         * Knuth, Section 6.4
         *
         * @param staleSlot index of slot known to have null key
         * @return the index of the next null slot after staleSlot
         * (all between staleSlot and this slot will have been checked
         * for expunging).
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * Heuristically scan some cells looking for stale entries.
         * This is invoked when either a new element is added, or
         * another stale one has been expunged. It performs a
         * logarithmic number of scans, as a balance between no
         * scanning (fast but retains garbage) and a number of scans
         * proportional to number of elements, that would find all
         * garbage but would cause some insertions to take O(n) time.
         *
         * @param i a position known NOT to hold a stale entry. The
         * scan starts at the element after i.
         *
         * @param n scan control: {@code log2(n)} cells are scanned,
         * unless a stale entry is found, in which case
         * {@code log2(table.length)-1} additional cells are scanned.
         * When called from insertions, this parameter is the number
         * of elements, but when from replaceStaleEntry, it is the
         * table length. (Note: all this could be changed to be either
         * more or less aggressive by weighting n instead of just
         * using straight log n. But this version is simple, fast, and
         * seems to work well.)
         *
         * @return true if any stale entries have been removed.
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                if (e != null && e.refersTo(null)) {
                    n = len;
                    removed = true;
                    i = expungeStaleEntry(i);
                }
            } while ((n >>>= 1) != 0);
            return removed;
        }

        /**
         * Re-pack and/or re-size the table. First scan the entire
         * table removing stale entries. If this doesn't sufficiently
         * shrink the size of the table, double the table size.
         */
        private void rehash() {
            expungeStaleEntries();

            // Use lower threshold for doubling to avoid hysteresis
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * Double the capacity of the table.
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (Entry e : oldTab) {
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * Expunge all stale entries in the table.
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.refersTo(null))
                    expungeStaleEntry(j);
            }
        }
    }

    /**
     * Reads the value of the jdk.traceVirtualThreadLocals property to determine if
     * a stack trace should be printed when a virtual thread sets a thread local.
     */
    private static boolean traceVirtualThreadLocals() {
        String propValue = GetPropertyAction.privilegedGetProperty("jdk.traceVirtualThreadLocals");
        return (propValue != null)
                && (propValue.isEmpty() || Boolean.parseBoolean(propValue));
    }

    /**
     * Print the stack trace of the current thread, skipping the printStackTrace frame.
     * A thread local is used to detect reentrancy as the printing may itself use
     * thread locals.
     */
    private void printStackTrace() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map.getEntry(DUMPING_STACK) == null) {
            map.set(DUMPING_STACK, true);
            try {
                var stack = StackWalker.getInstance().walk(s ->
                        s.skip(1)  // skip caller
                                .collect(Collectors.toList()));
                System.out.println(t);
                for (StackWalker.StackFrame frame : stack) {
                    System.out.format("    %s%n", frame.toStackTraceElement());
                }
            } finally {
                map.remove(DUMPING_STACK);
            }
        }
    }

    private static final ThreadLocal<Boolean> DUMPING_STACK = new ThreadLocal<>();
}
