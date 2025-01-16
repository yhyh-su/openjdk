package java.lang;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 实现此接口使得对象能够作为增强型 {@code for} 循环的目标（也叫做“for-each”循环）。
 *
 * @param <T> 返回的元素类型
 *
 * @since 1.5
 */
public interface Iterable<T> {
    /**
     * 返回一个迭代器，用于遍历类型为 {@code T} 的元素。
     *
     * @return 一个 {@code Iterator} 对象。
     */
    Iterator<T> iterator();


    /**
     * 对 {@code Iterable} 中的每个元素执行给定的操作，直到所有元素都处理完或操作抛出异常。
     * 操作按迭代顺序执行，如果顺序已指定。操作抛出的异常会传递给调用者。
     *
     * @implSpec
     * <p>默认实现方式是：
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     *
     * @param action 对每个元素执行的操作
     * @throws NullPointerException 如果指定的操作为 null
     * @since 1.8
     */
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);  // 确保 action 不为 null
        for (T t : this) {  // 使用 for-each 循环遍历元素
            action.accept(t);  // 执行给定的操作
        }
    }


    /**
     * 创建一个描述该 {@code Iterable} 元素的 {@link Spliterator}。
     *
     * @implSpec
     * 默认实现通过 {@code Iterator} 创建一个早期绑定的 spliterator。
     * 该 spliterator 继承了 Iterable 的迭代器的快速失败属性。
     *
     * @implNote
     * 默认实现通常应当被重写。默认实现的 spliterator 拥有较差的拆分能力，没有大小信息，且不会报告任何 spliterator 特性。
     * 实现类通常能提供更好的实现。
     *
     * @return 返回一个描述该 {@code Iterable} 元素的 {@code Spliterator}。
     * @since 1.8
     */
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }

}
