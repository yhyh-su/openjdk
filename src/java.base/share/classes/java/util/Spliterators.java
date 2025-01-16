/*
 * Copyright (c) 2013, 2024, Oracle and/or its affiliates. All rights reserved.
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
package java.util;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * Static classes and methods for operating on or creating instances of
 * {@link Spliterator} and its primitive specializations
 * {@link Spliterator.OfInt}, {@link Spliterator.OfLong}, and
 * {@link Spliterator.OfDouble}.
 *
 * @see Spliterator
 * @since 1.8
 */
public final class Spliterators {

    // Suppresses default constructor, ensuring non-instantiability.
    private Spliterators() {
    }

    // Empty spliterators

    /**
     * Creates an empty {@code Spliterator}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @param <T> Type of elements
     * @return An empty spliterator
     */
    @SuppressWarnings("unchecked")
    public static <T> Spliterator<T> emptySpliterator() {
        return (Spliterator<T>) EmptySpliterator.OfRef.EMPTY_SPLITERATOR;
    }

    /**
     * Creates an empty {@code Spliterator.OfInt}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return An empty spliterator
     */
    public static Spliterator.OfInt emptyIntSpliterator() {
        return EmptySpliterator.OfInt.EMPTY_INT_SPLITERATOR;
    }

    /**
     * Creates an empty {@code Spliterator.OfLong}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return An empty spliterator
     */
    public static Spliterator.OfLong emptyLongSpliterator() {
        return EmptySpliterator.OfLong.EMPTY_LONG_SPLITERATOR;
    }

    /**
     * Creates an empty {@code Spliterator.OfDouble}
     *
     * <p>The empty spliterator reports {@link Spliterator#SIZED} and
     * {@link Spliterator#SUBSIZED}.  Calls to
     * {@link java.util.Spliterator#trySplit()} always return {@code null}.
     *
     * @return An empty spliterator
     */
    public static Spliterator.OfDouble emptyDoubleSpliterator() {
        return EmptySpliterator.OfDouble.EMPTY_DOUBLE_SPLITERATOR;
    }

    // Array-based spliterators

    /**
     * Creates a {@code Spliterator} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(Object[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param <T> Type of elements
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(Object[])
     */
    public static <T> Spliterator<T> spliterator(Object[] array,
                                                 int additionalCharacteristics) {
        return new ArraySpliterator<>(Objects.requireNonNull(array),
                additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator} covering a range of elements of a given
     * array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(Object[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param <T> Type of elements
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(Object[], int, int)
     */
    public static <T> Spliterator<T> spliterator(Object[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new ArraySpliterator<>(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(int[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(int[])
     */
    public static Spliterator.OfInt spliterator(int[] array,
                                                int additionalCharacteristics) {
        return new IntArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} covering a range of elements of a
     * given array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(int[], int, int)}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(int[], int, int)
     */
    public static Spliterator.OfInt spliterator(int[] array, int fromIndex, int toIndex,
                                                int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new IntArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(long[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(long[])
     */
    public static Spliterator.OfLong spliterator(long[] array,
                                                 int additionalCharacteristics) {
        return new LongArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} covering a range of elements of a
     * given array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(long[], int, int)}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report.  (For example, if it is
     * known the array will not be further modified, specify {@code IMMUTABLE};
     * if the array data is considered to have an encounter order, specify
     * {@code ORDERED}).  The method {@link Arrays#spliterator(long[], int, int)} can
     * often be used instead, which returns a spliterator that reports
     * {@code SIZED}, {@code SUBSIZED}, {@code IMMUTABLE}, and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(long[], int, int)
     */
    public static Spliterator.OfLong spliterator(long[] array, int fromIndex, int toIndex,
                                                 int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new LongArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} covering the elements of a given array,
     * using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(double[])}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report; it is common to
     * additionally specify {@code IMMUTABLE} and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @see Arrays#spliterator(double[])
     */
    public static Spliterator.OfDouble spliterator(double[] array,
                                                   int additionalCharacteristics) {
        return new DoubleArraySpliterator(Objects.requireNonNull(array), additionalCharacteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} covering a range of elements of a
     * given array, using a customized set of spliterator characteristics.
     *
     * <p>This method is provided as an implementation convenience for
     * Spliterators which store portions of their elements in arrays, and need
     * fine control over Spliterator characteristics.  Most other situations in
     * which a Spliterator for an array is needed should use
     * {@link Arrays#spliterator(double[], int, int)}.
     *
     * <p>The returned spliterator always reports the characteristics
     * {@code SIZED} and {@code SUBSIZED}.  The caller may provide additional
     * characteristics for the spliterator to report.  (For example, if it is
     * known the array will not be further modified, specify {@code IMMUTABLE};
     * if the array data is considered to have an encounter order, specify
     * {@code ORDERED}).  The method {@link Arrays#spliterator(long[], int, int)} can
     * often be used instead, which returns a spliterator that reports
     * {@code SIZED}, {@code SUBSIZED}, {@code IMMUTABLE}, and {@code ORDERED}.
     *
     * @param array The array, assumed to be unmodified during use
     * @param fromIndex The least index (inclusive) to cover
     * @param toIndex One past the greatest index to cover
     * @param additionalCharacteristics Additional spliterator characteristics
     *        of this spliterator's source or elements beyond {@code SIZED} and
     *        {@code SUBSIZED} which are always reported
     * @return A spliterator for an array
     * @throws NullPointerException if the given array is {@code null}
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex} is negative,
     *         {@code toIndex} is less than {@code fromIndex}, or
     *         {@code toIndex} is greater than the array size
     * @see Arrays#spliterator(double[], int, int)
     */
    public static Spliterator.OfDouble spliterator(double[] array, int fromIndex, int toIndex,
                                                   int additionalCharacteristics) {
        checkFromToBounds(Objects.requireNonNull(array).length, fromIndex, toIndex);
        return new DoubleArraySpliterator(array, fromIndex, toIndex, additionalCharacteristics);
    }

    /**
     * Validate inclusive start index and exclusive end index against the length
     * of an array.
     * @param arrayLength The length of the array
     * @param origin The inclusive start index
     * @param fence The exclusive end index
     * @throws ArrayIndexOutOfBoundsException if the start index is greater than
     * the end index, if the start index is negative, or the end index is
     * greater than the array length
     */
    private static void checkFromToBounds(int arrayLength, int origin, int fence) {
        if (origin > fence) {
            throw new ArrayIndexOutOfBoundsException(
                    "origin(" + origin + ") > fence(" + fence + ")");
        }
        if (origin < 0) {
            throw new ArrayIndexOutOfBoundsException(origin);
        }
        if (fence > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(fence);
        }
    }

    /**
     * 将一个 {@link Collection} 转换为 {@link Spliterator}，用于支持流操作。
     *
     * @param <T> 元素类型，表示集合中的元素类型
     * @param c 需要转换为 {@link Spliterator} 的集合
     * @param characteristics {@link Spliterator} 的特性标志，表示集合的特性（例如：是否有序、是否唯一等）
     * @return 返回一个 {@link Spliterator}，用于遍历指定集合
     * @throws NullPointerException 如果提供的集合为 null，则抛出空指针异常
     */
    public static <T> Spliterator<T> spliterator(Collection<? extends T> c, int characteristics) {
        // 确保集合不为 null，如果是 null 则抛出 NullPointerException
        return new IteratorSpliterator<>(Objects.requireNonNull(c), characteristics);
    }


    /**
     * Creates a {@code Spliterator} using a given {@code Iterator}
     * as the source of elements, and with a given initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param <T> Type of elements
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static <T> Spliterator<T> spliterator(Iterator<? extends T> iterator,
                                                 long size,
                                                 int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(iterator), size,
                characteristics);
    }

    /**
     * Creates a {@code Spliterator} using a given {@code Iterator}
     * as the source of elements, with no initial size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param <T> Type of elements
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static <T> Spliterator<T> spliteratorUnknownSize(Iterator<? extends T> iterator,
                                                            int characteristics) {
        return new IteratorSpliterator<>(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} using a given
     * {@code IntStream.IntIterator} as the source of elements, and with a given
     * initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}.
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfInt spliterator(PrimitiveIterator.OfInt iterator,
                                                long size,
                                                int characteristics) {
        return new IntIteratorSpliterator(Objects.requireNonNull(iterator),
                size, characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfInt} using a given
     * {@code IntStream.IntIterator} as the source of elements, with no initial
     * size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfInt spliteratorUnknownSize(PrimitiveIterator.OfInt iterator,
                                                           int characteristics) {
        return new IntIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} using a given
     * {@code LongStream.LongIterator} as the source of elements, and with a
     * given initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}.
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfLong spliterator(PrimitiveIterator.OfLong iterator,
                                                 long size,
                                                 int characteristics) {
        return new LongIteratorSpliterator(Objects.requireNonNull(iterator),
                size, characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfLong} using a given
     * {@code LongStream.LongIterator} as the source of elements, with no
     * initial size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfLong spliteratorUnknownSize(PrimitiveIterator.OfLong iterator,
                                                            int characteristics) {
        return new LongIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} using a given
     * {@code DoubleStream.DoubleIterator} as the source of elements, and with a
     * given initially reported size.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned, or the initially reported
     * size is not equal to the actual number of elements in the source.
     *
     * @param iterator The iterator for the source
     * @param size The number of elements in the source, to be reported as
     *        initial {@code estimateSize}
     * @param characteristics Characteristics of this spliterator's source or
     *        elements.  The characteristics {@code SIZED} and {@code SUBSIZED}
     *        are additionally reported unless {@code CONCURRENT} is supplied.
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfDouble spliterator(PrimitiveIterator.OfDouble iterator,
                                                   long size,
                                                   int characteristics) {
        return new DoubleIteratorSpliterator(Objects.requireNonNull(iterator),
                size, characteristics);
    }

    /**
     * Creates a {@code Spliterator.OfDouble} using a given
     * {@code DoubleStream.DoubleIterator} as the source of elements, with no
     * initial size estimate.
     *
     * <p>The spliterator is not
     * <em><a href="Spliterator.html#binding">late-binding</a></em>, inherits
     * the <em>fail-fast</em> properties of the iterator, and implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>Traversal of elements should be accomplished through the spliterator.
     * The behaviour of splitting and traversal is undefined if the iterator is
     * operated on after the spliterator is returned.
     *
     * @param iterator The iterator for the source
     * @param characteristics Characteristics of this spliterator's source
     *        or elements ({@code SIZED} and {@code SUBSIZED}, if supplied, are
     *        ignored and are not reported.)
     * @return A spliterator from an iterator
     * @throws NullPointerException if the given iterator is {@code null}
     */
    public static Spliterator.OfDouble spliteratorUnknownSize(PrimitiveIterator.OfDouble iterator,
                                                              int characteristics) {
        return new DoubleIteratorSpliterator(Objects.requireNonNull(iterator), characteristics);
    }

    // Iterators from Spliterators

    /**
     * Creates an {@code Iterator} from a {@code Spliterator}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param <T> Type of elements
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static <T> Iterator<T> iterator(Spliterator<? extends T> spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements Iterator<T>, Consumer<T> {
            boolean valueReady = false;
            T nextElement;

            @Override
            public void accept(T t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public T next() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    T t = nextElement;
                    nextElement = null;
                    return t;
                }
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (valueReady) {
                    valueReady = false;
                    T t = nextElement;
                    nextElement = null;
                    action.accept(t);
                }
                spliterator.forEachRemaining(action);
            }
        }

        return new Adapter();
    }

    /**
     * Creates an {@code PrimitiveIterator.OfInt} from a
     * {@code Spliterator.OfInt}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static PrimitiveIterator.OfInt iterator(Spliterator.OfInt spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfInt, IntConsumer {
            boolean valueReady = false;
            int nextElement;

            @Override
            public void accept(int t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public int nextInt() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                Objects.requireNonNull(action);
                if (valueReady) {
                    valueReady = false;
                    action.accept(nextElement);
                }
                spliterator.forEachRemaining(action);
            }
        }

        return new Adapter();
    }

    /**
     * Creates an {@code PrimitiveIterator.OfLong} from a
     * {@code Spliterator.OfLong}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static PrimitiveIterator.OfLong iterator(Spliterator.OfLong spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfLong, LongConsumer {
            boolean valueReady = false;
            long nextElement;

            @Override
            public void accept(long t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public long nextLong() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

            @Override
            public void forEachRemaining(LongConsumer action) {
                Objects.requireNonNull(action);
                if (valueReady) {
                    valueReady = false;
                    action.accept(nextElement);
                }
                spliterator.forEachRemaining(action);
            }
        }

        return new Adapter();
    }

    /**
     * Creates an {@code PrimitiveIterator.OfDouble} from a
     * {@code Spliterator.OfDouble}.
     *
     * <p>Traversal of elements should be accomplished through the iterator.
     * The behaviour of traversal is undefined if the spliterator is operated
     * after the iterator is returned.
     *
     * @param spliterator The spliterator
     * @return An iterator
     * @throws NullPointerException if the given spliterator is {@code null}
     */
    public static PrimitiveIterator.OfDouble iterator(Spliterator.OfDouble spliterator) {
        Objects.requireNonNull(spliterator);
        class Adapter implements PrimitiveIterator.OfDouble, DoubleConsumer {
            boolean valueReady = false;
            double nextElement;

            @Override
            public void accept(double t) {
                valueReady = true;
                nextElement = t;
            }

            @Override
            public boolean hasNext() {
                if (!valueReady)
                    spliterator.tryAdvance(this);
                return valueReady;
            }

            @Override
            public double nextDouble() {
                if (!valueReady && !hasNext())
                    throw new NoSuchElementException();
                else {
                    valueReady = false;
                    return nextElement;
                }
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                Objects.requireNonNull(action);
                if (valueReady) {
                    valueReady = false;
                    action.accept(nextElement);
                }
                spliterator.forEachRemaining(action);
            }
        }

        return new Adapter();
    }

    // Implementations

    private abstract static class EmptySpliterator<T, S extends Spliterator<T>, C> {

        EmptySpliterator() {
        }

        public S trySplit() {
            return null;
        }

        public boolean tryAdvance(C consumer) {
            Objects.requireNonNull(consumer);
            return false;
        }

        public void forEachRemaining(C consumer) {
            Objects.requireNonNull(consumer);
        }

        public long estimateSize() {
            return 0;
        }

        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        private static final class OfRef<T>
                extends EmptySpliterator<T, Spliterator<T>, Consumer<? super T>>
                implements Spliterator<T> {
            static final Spliterator<Object> EMPTY_SPLITERATOR =
                    new EmptySpliterator.OfRef<>();

            private OfRef() {
            }
        }

        @SuppressWarnings("overloads")
        private static final class OfInt
                extends EmptySpliterator<Integer, Spliterator.OfInt, IntConsumer>
                implements Spliterator.OfInt {
            static final Spliterator.OfInt EMPTY_INT_SPLITERATOR =
                    new EmptySpliterator.OfInt();

            private OfInt() {
            }
        }

        @SuppressWarnings("overloads")
        private static final class OfLong
                extends EmptySpliterator<Long, Spliterator.OfLong, LongConsumer>
                implements Spliterator.OfLong {
            static final Spliterator.OfLong EMPTY_LONG_SPLITERATOR =
                    new EmptySpliterator.OfLong();

            private OfLong() {
            }
        }

        @SuppressWarnings("overloads")
        private static final class OfDouble
                extends EmptySpliterator<Double, Spliterator.OfDouble, DoubleConsumer>
                implements Spliterator.OfDouble {
            static final Spliterator.OfDouble EMPTY_DOUBLE_SPLITERATOR =
                    new EmptySpliterator.OfDouble();

            private OfDouble() {
            }
        }
    }

    // Array-based spliterators

    /**
     * A Spliterator designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code Object[]} array.
     */
    static final class ArraySpliterator<T> implements Spliterator<T> {
        /**
         * The array, explicitly typed as Object[]. Unlike in some other
         * classes (see for example CR 6260652), we do not need to
         * screen arguments to ensure they are exactly of type Object[]
         * so long as no methods write into the array or serialize it,
         * which we ensure here by defining this class as final.
         */
        private final Object[] array;
        private final int fence;  // one past last index
        private final int characteristics;
        private int index;        // current index, modified on advance/split
        private long estimatedSize; // if >= 0, the estimated size, to help to split evenly
        // if -1, exact size is known to be fence - index

        /**
         * Creates a spliterator covering all of the given array.
         * Its size is known exactly and it is SIZED and SUBSIZED.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         * of this spliterator's source or elements beyond {@code SIZED} and
         * {@code SUBSIZED} which are always reported
         */
        public ArraySpliterator(Object[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range.
         * Its size is known exactly and it is SIZED and SUBSIZED.
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         * of this spliterator's source or elements beyond {@code SIZED} and
         * {@code SUBSIZED} which are always reported
         */
        public ArraySpliterator(Object[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
            this.estimatedSize = -1;
        }

        /**
         * Creates a spliterator covering the given array and range but that is
         * not SIZED or SUBSIZED. This case occurs as a result of splitting another
         * spliterator that is not sized, so it's inappropriate for one of its
         * sub-spliterators to be sized.
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param characteristics characteristics of this spliterator's source; {@code SIZED} and
         *        {@code SUBSIZED} are removed if present
         * @param estimatedSize the size estimate; should always be nonnegative
         */
        private ArraySpliterator(Object[] array, int origin, int fence, int characteristics, long estimatedSize) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
            this.estimatedSize = estimatedSize;
        }

        @Override
        public Spliterator<T> trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            if (lo >= mid) return null;
            if (estimatedSize == -1) {
                return new ArraySpliterator<>(array, lo, index = mid, characteristics);
            }
            long prefixEstimatedSize = estimatedSize >>> 1;
            estimatedSize -= prefixEstimatedSize;
            return new ArraySpliterator<>(array, lo, index = mid, characteristics, prefixEstimatedSize);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Object[] a;
            int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                    (i = index) >= 0 && i < (index = hi)) {
                do {
                    action.accept((T) a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                @SuppressWarnings("unchecked") T e = (T) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return estimatedSize >= 0 ? estimatedSize : (long) (fence - index);
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfInt designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code int[]} array.
     */
    static final class IntArraySpliterator implements Spliterator.OfInt {
        private final int[] array;
        private final int fence;  // one past last index
        private final int characteristics;
        private int index;        // current index, modified on advance/split
        private long estimatedSize; // estimated size, to help to split evenly

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public IntArraySpliterator(int[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public IntArraySpliterator(int[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
            this.estimatedSize = -1;
        }

        private IntArraySpliterator(int[] array, int origin, int fence, int characteristics, long estimatedSize) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
            this.estimatedSize = estimatedSize;
        }

        @Override
        public OfInt trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            if (lo >= mid) return null;
            if (estimatedSize == -1) {
                return new IntArraySpliterator(array, lo, index = mid, characteristics);
            }
            long prefixEstimatedSize = estimatedSize >>> 1;
            estimatedSize -= prefixEstimatedSize;
            return new IntArraySpliterator(array, lo, index = mid, characteristics, prefixEstimatedSize);
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            int[] a;
            int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                    (i = index) >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return estimatedSize >= 0 ? estimatedSize : (long) (fence - index);
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfLong designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code int[]} array.
     */
    static final class LongArraySpliterator implements Spliterator.OfLong {
        private final long[] array;
        private final int fence;  // one past last index
        private final int characteristics;
        private int index;        // current index, modified on advance/split
        private long estimatedSize; // estimated size, to help to split evenly

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public LongArraySpliterator(long[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public LongArraySpliterator(long[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
            this.estimatedSize = -1;
        }

        private LongArraySpliterator(long[] array, int origin, int fence, int characteristics, long estimatedSize) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
            this.estimatedSize = estimatedSize;
        }

        @Override
        public OfLong trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            if (lo >= mid) return null;
            if (estimatedSize == -1) {
                return new LongArraySpliterator(array, lo, index = mid, characteristics);
            }
            long prefixEstimatedSize = estimatedSize >>> 1;
            estimatedSize -= prefixEstimatedSize;
            return new LongArraySpliterator(array, lo, index = mid, characteristics, prefixEstimatedSize);
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            long[] a;
            int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                    (i = index) >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return estimatedSize >= 0 ? estimatedSize : (long) (fence - index);
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    /**
     * A Spliterator.OfDouble designed for use by sources that traverse and split
     * elements maintained in an unmodifiable {@code int[]} array.
     */
    static final class DoubleArraySpliterator implements Spliterator.OfDouble {
        private final double[] array;
        private final int fence;  // one past last index
        private final int characteristics;
        private int index;        // current index, modified on advance/split
        private long estimatedSize; // estimated size, to help to split evenly

        /**
         * Creates a spliterator covering all of the given array.
         * @param array the array, assumed to be unmodified during use
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public DoubleArraySpliterator(double[] array, int additionalCharacteristics) {
            this(array, 0, array.length, additionalCharacteristics);
        }

        /**
         * Creates a spliterator covering the given array and range
         * @param array the array, assumed to be unmodified during use
         * @param origin the least index (inclusive) to cover
         * @param fence one past the greatest index to cover
         * @param additionalCharacteristics Additional spliterator characteristics
         *        of this spliterator's source or elements beyond {@code SIZED} and
         *        {@code SUBSIZED} which are always reported
         */
        public DoubleArraySpliterator(double[] array, int origin, int fence, int additionalCharacteristics) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
            this.estimatedSize = -1;
        }

        private DoubleArraySpliterator(double[] array, int origin, int fence, int characteristics, long estimatedSize) {
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
            this.estimatedSize = estimatedSize;
        }

        @Override
        public OfDouble trySplit() {
            int lo = index, mid = (lo + fence) >>> 1;
            if (lo >= mid) return null;
            if (estimatedSize == -1) {
                return new DoubleArraySpliterator(array, lo, index = mid, characteristics);
            }
            long prefixEstimatedSize = estimatedSize >>> 1;
            estimatedSize -= prefixEstimatedSize;
            return new DoubleArraySpliterator(array, lo, index = mid, characteristics, prefixEstimatedSize);
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            double[] a;
            int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array).length >= (hi = fence) &&
                    (i = index) >= 0 && i < (index = hi)) {
                do {
                    action.accept(a[i]);
                } while (++i < hi);
            }
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null)
                throw new NullPointerException();
            if (index >= 0 && index < fence) {
                action.accept(array[index++]);
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return estimatedSize >= 0 ? estimatedSize : (long) (fence - index);
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    //

    /**
     * An abstract {@code Spliterator} that implements {@code trySplit} to
     * permit limited parallelism.
     *
     * <p>An extending class need only
     * implement {@link #tryAdvance(java.util.function.Consumer) tryAdvance}.
     * The extending class should override
     * {@link #forEachRemaining(java.util.function.Consumer) forEachRemaining}
     * if it can provide a more performant implementation.
     *
     * @apiNote
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(Iterator, long, int)}.  Depending on the
     * circumstances using an iterator may be easier or more convenient than
     * extending this class, such as when there is already an iterator
     * available to use.
     *
     * @param <T> the type of elements returned by this Spliterator
     *
     * @see #spliterator(Iterator, long, int)
     * @since 1.8
     */
    public abstract static class AbstractSpliterator<T> implements Spliterator<T> {
        static final int BATCH_UNIT = 1 << 10;  // batch array size increment
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * additionalCharacteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator<T> trySplit() {
            /*
             * Split into arrays of arithmetically increasing batch
             * sizes.  This will only improve parallel performance if
             * per-element Consumer actions are more costly than
             * transferring them into an array.  The use of an
             * arithmetic progression in split sizes provides overhead
             * vs parallelism bounds that do not particularly favor or
             * penalize cases of lightweight vs heavyweight element
             * operations, across combinations of #elements vs #cores,
             * whether or not either are known.  We generate
             * O(sqrt(#elements)) splits, allowing O(sqrt(#cores))
             * potential speedup.
             */
            HoldingConsumer<T> holder = new HoldingConsumer<>();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do {
                    a[j] = holder.value;
                } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new ArraySpliterator<>(a, 0, j, characteristics);
                }
                return new ArraySpliterator<>(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }

        static final class HoldingConsumer<T> implements Consumer<T> {
            Object value;

            @Override
            public void accept(T value) {
                this.value = value;
            }
        }
    }

    /**
     * An abstract {@code Spliterator.OfInt} that implements {@code trySplit} to
     * permit limited parallelism.
     *
     * <p>To implement a spliterator an extending class need only
     * implement {@link #tryAdvance(java.util.function.IntConsumer)
     * tryAdvance}.  The extending class should override
     * {@link #forEachRemaining(java.util.function.IntConsumer) forEachRemaining}
     * if it can provide a more performant implementation.
     *
     * @apiNote
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(java.util.PrimitiveIterator.OfInt, long, int)}.
     * Depending on the circumstances using an iterator may be easier or more
     * convenient than extending this class. For example, if there is already an
     * iterator available to use then there is no need to extend this class.
     *
     * @see #spliterator(java.util.PrimitiveIterator.OfInt, long, int)
     * @since 1.8
     */
    public abstract static class AbstractIntSpliterator implements Spliterator.OfInt {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractIntSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator.OfInt trySplit() {
            HoldingIntConsumer holder = new HoldingIntConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                int[] a = new int[n];
                int j = 0;
                do {
                    a[j] = holder.value;
                } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new IntArraySpliterator(a, 0, j, characteristics);
                }
                return new IntArraySpliterator(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }

        static final class HoldingIntConsumer implements IntConsumer {
            int value;

            @Override
            public void accept(int value) {
                this.value = value;
            }
        }
    }

    /**
     * An abstract {@code Spliterator.OfLong} that implements {@code trySplit}
     * to permit limited parallelism.
     *
     * <p>To implement a spliterator an extending class need only
     * implement {@link #tryAdvance(java.util.function.LongConsumer)
     * tryAdvance}.  The extending class should override
     * {@link #forEachRemaining(java.util.function.LongConsumer) forEachRemaining}
     * if it can provide a more performant implementation.
     *
     * @apiNote
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(java.util.PrimitiveIterator.OfLong, long, int)}.
     * Depending on the circumstances using an iterator may be easier or more
     * convenient than extending this class. For example, if there is already an
     * iterator available to use then there is no need to extend this class.
     *
     * @see #spliterator(java.util.PrimitiveIterator.OfLong, long, int)
     * @since 1.8
     */
    public abstract static class AbstractLongSpliterator implements Spliterator.OfLong {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractLongSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator.OfLong trySplit() {
            HoldingLongConsumer holder = new HoldingLongConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                long[] a = new long[n];
                int j = 0;
                do {
                    a[j] = holder.value;
                } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new LongArraySpliterator(a, 0, j, characteristics);
                }
                return new LongArraySpliterator(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }

        static final class HoldingLongConsumer implements LongConsumer {
            long value;

            @Override
            public void accept(long value) {
                this.value = value;
            }
        }
    }

    /**
     * An abstract {@code Spliterator.OfDouble} that implements
     * {@code trySplit} to permit limited parallelism.
     *
     * <p>To implement a spliterator an extending class need only
     * implement {@link #tryAdvance(java.util.function.DoubleConsumer)
     * tryAdvance}.  The extending class should override
     * {@link #forEachRemaining(java.util.function.DoubleConsumer) forEachRemaining}
     * if it can provide a more performant implementation.
     *
     * @apiNote
     * This class is a useful aid for creating a spliterator when it is not
     * possible or difficult to efficiently partition elements in a manner
     * allowing balanced parallel computation.
     *
     * <p>An alternative to using this class, that also permits limited
     * parallelism, is to create a spliterator from an iterator
     * (see {@link #spliterator(java.util.PrimitiveIterator.OfDouble, long, int)}.
     * Depending on the circumstances using an iterator may be easier or more
     * convenient than extending this class. For example, if there is already an
     * iterator available to use then there is no need to extend this class.
     *
     * @see #spliterator(java.util.PrimitiveIterator.OfDouble, long, int)
     * @since 1.8
     */
    public abstract static class AbstractDoubleSpliterator implements Spliterator.OfDouble {
        static final int MAX_BATCH = AbstractSpliterator.MAX_BATCH;
        static final int BATCH_UNIT = AbstractSpliterator.BATCH_UNIT;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator reporting the given estimated size and
         * characteristics.
         *
         * @param est the estimated size of this spliterator if known, otherwise
         *        {@code Long.MAX_VALUE}.
         * @param additionalCharacteristics properties of this spliterator's
         *        source or elements.  If {@code SIZED} is reported then this
         *        spliterator will additionally report {@code SUBSIZED}.
         */
        protected AbstractDoubleSpliterator(long est, int additionalCharacteristics) {
            this.est = est;
            this.characteristics = ((additionalCharacteristics & Spliterator.SIZED) != 0)
                    ? additionalCharacteristics | Spliterator.SUBSIZED
                    : additionalCharacteristics;
        }

        /**
         * {@inheritDoc}
         *
         * This implementation permits limited parallelism.
         */
        @Override
        public Spliterator.OfDouble trySplit() {
            HoldingDoubleConsumer holder = new HoldingDoubleConsumer();
            long s = est;
            if (s > 1 && tryAdvance(holder)) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                double[] a = new double[n];
                int j = 0;
                do {
                    a[j] = holder.value;
                } while (++j < n && tryAdvance(holder));
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new DoubleArraySpliterator(a, 0, j, characteristics);
                }
                return new DoubleArraySpliterator(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the estimated size as reported when
         * created and, if the estimate size is known, decreases in size when
         * split.
         */
        @Override
        public long estimateSize() {
            return est;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * This implementation returns the characteristics as reported when
         * created.
         */
        @Override
        public int characteristics() {
            return characteristics;
        }

        static final class HoldingDoubleConsumer implements DoubleConsumer {
            double value;

            @Override
            public void accept(double value) {
                this.value = value;
            }
        }
    }

    // Iterator-based Spliterators

    /**
     * 使用给定的 Iterator 进行元素操作的 Spliterator。
     * 该 Spliterator 实现了 {@code trySplit}，允许有限的并行性。
     */
    static class IteratorSpliterator<T> implements Spliterator<T> {
        // 批处理单位大小，用于分割
        static final int BATCH_UNIT = 1 << 10;  // 批量数组大小递增
        // 最大批处理大小
        static final int MAX_BATCH = 1 << 25;  // 最大批量数组大小

        private final Collection<? extends T> collection; // 存储源集合（允许为 null）
        private final int characteristics; // 特征值
        private Iterator<? extends T> it; // 当前迭代器
        private long est;  // 大小估计值
        private int batch; // 分割时的批次大小


        /**
         * 构造一个 {@link IteratorSpliterator}，用于遍历集合并支持流操作。
         * @param collection 要遍历的集合
         * @param characteristics {@link Spliterator} 的特性标志（例如：是否有序、是否唯一等）
         */
        public IteratorSpliterator(Collection<? extends T> collection, int characteristics) {
            this.collection = collection;  // 将传入的集合赋值给成员变量
            this.it = null;  // 初始化时，迭代器为 null，稍后会创建迭代器
            // 如果特性标志没有包含 CONCURRENT (例如0)，则自动添加 SIZED 和 SUBSIZED 特性
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }


        /**
         * 使用给定的迭代器创建一个 Spliterator，报告给定的初始大小和特性。
         *
         * @param iterator 迭代器
         * @param size 初始大小（元素数量）
         * @param characteristics 此 Spliterator 的特性
         */
        public IteratorSpliterator(Iterator<? extends T> iterator, long size, int characteristics) {
            this.collection = null; // 集合为 null
            this.it = iterator;
            this.est = size; // 设置估计大小
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * 使用给定的迭代器创建一个 Spliterator，报告给定的特性。
         *
         * @param iterator 迭代器
         * @param characteristics 此 Spliterator 的特性
         */
        public IteratorSpliterator(Iterator<? extends T> iterator, int characteristics) {
            this.collection = null;
            this.it = iterator;
            this.est = Long.MAX_VALUE; // 默认大小为无限
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED); // 不报告大小特性
        }

        /**
         * 尝试将 Spliterator 拆分为更小的部分（支持并行处理）。
         *
         * <p>这个方法将元素分割成递增的批处理大小。如果分割成功，返回一个新的 Spliterator；如果无法分割，返回 null。
         * </p>
         *
         * @return 一个新的 Spliterator 或 null（如果无法分割）
         */
        @Override
        public Spliterator<T> trySplit() {
            Iterator<? extends T> i;
            long s;
            if ((i = it) == null) {
                i = it = collection.iterator();
                s = est = (long) collection.size();
            } else {
                s = est;
            }

            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT; // 每次批量递增
                if (n > s) n = (int) s;
                if (n > MAX_BATCH) n = MAX_BATCH;

                Object[] a = new Object[n];
                int j = 0;
                do {
                    a[j] = i.next(); // 填充当前批次
                } while (++j < n && i.hasNext());

                batch = j; // 更新批次大小
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new ArraySpliterator<>(a, 0, j, characteristics); // 返回新的 Spliterator
                }
                return new ArraySpliterator<>(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null; // 无法分割
        }

        /**
         * 对剩余的所有元素执行给定的操作。
         *
         * @param action 要执行的操作
         */
        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            Iterator<? extends T> i;
            if ((i = it) == null) {
                i = it = collection.iterator();
                est = (long) collection.size();
            }
            i.forEachRemaining(action); // 执行给定的操作
        }

        /**
         * 尝试对下一个元素执行给定的操作。
         *
         * @param action 要执行的操作
         * @return 如果元素存在并成功执行操作，则返回 true，否则返回 false
         */
        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (action == null) throw new NullPointerException();
            if (it == null) {
                it = collection.iterator();
                est = (long) collection.size();
            }
            if (it.hasNext()) {
                action.accept(it.next()); // 执行操作
                return true;
            }
            return false;
        }

        /**
         * 返回此 Spliterator 的估计大小。
         *
         * @return 估计大小
         */
        @Override
        public long estimateSize() {
            if (it == null) {
                it = collection.iterator();
                return est = (long) collection.size();
            }
            return est;
        }

        /**
         * 返回此 Spliterator 的特性值。
         *
         * @return 特性值
         */
        @Override
        public int characteristics() {
            return characteristics;
        }

        /**
         * 返回此 Spliterator 的比较器（如果 Spliterator 被标记为已排序）。
         *
         * @return 比较器
         * @throws IllegalStateException 如果 Spliterator 不是已排序的
         */
        @Override
        public Comparator<? super T> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }


    /**
     * A Spliterator.OfInt using a given IntStream.IntIterator for element
     * operations. The spliterator implements {@code trySplit} to
     * permit limited parallelism.
     */
    static final class IntIteratorSpliterator implements Spliterator.OfInt {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private final PrimitiveIterator.OfInt it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public IntIteratorSpliterator(PrimitiveIterator.OfInt iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfInt trySplit() {
            PrimitiveIterator.OfInt i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                int[] a = new int[n];
                int j = 0;
                do {
                    a[j] = i.nextInt();
                } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new IntArraySpliterator(a, 0, j, characteristics);
                }
                return new IntArraySpliterator(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            if (action == null) throw new NullPointerException();
            it.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextInt());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    static final class LongIteratorSpliterator implements Spliterator.OfLong {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private final PrimitiveIterator.OfLong it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public LongIteratorSpliterator(PrimitiveIterator.OfLong iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfLong trySplit() {
            PrimitiveIterator.OfLong i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                long[] a = new long[n];
                int j = 0;
                do {
                    a[j] = i.nextLong();
                } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new LongArraySpliterator(a, 0, j, characteristics);
                }
                return new LongArraySpliterator(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            if (action == null) throw new NullPointerException();
            it.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextLong());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }

    static final class DoubleIteratorSpliterator implements Spliterator.OfDouble {
        static final int BATCH_UNIT = IteratorSpliterator.BATCH_UNIT;
        static final int MAX_BATCH = IteratorSpliterator.MAX_BATCH;
        private final PrimitiveIterator.OfDouble it;
        private final int characteristics;
        private long est;             // size estimate
        private int batch;            // batch size for splits

        /**
         * Creates a spliterator using the given iterator
         * for traversal, and reporting the given initial size
         * and characteristics.
         *
         * @param iterator the iterator for the source
         * @param size the number of elements in the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, long size, int characteristics) {
            this.it = iterator;
            this.est = size;
            this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                    ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                    : characteristics;
        }

        /**
         * Creates a spliterator using the given iterator for a
         * source of unknown size, reporting the given
         * characteristics.
         *
         * @param iterator the iterator for the source
         * @param characteristics properties of this spliterator's
         * source or elements.
         */
        public DoubleIteratorSpliterator(PrimitiveIterator.OfDouble iterator, int characteristics) {
            this.it = iterator;
            this.est = Long.MAX_VALUE;
            this.characteristics = characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        }

        @Override
        public OfDouble trySplit() {
            PrimitiveIterator.OfDouble i = it;
            long s = est;
            if (s > 1 && i.hasNext()) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = (int) s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                double[] a = new double[n];
                int j = 0;
                do {
                    a[j] = i.nextDouble();
                } while (++j < n && i.hasNext());
                batch = j;
                if (est != Long.MAX_VALUE) {
                    est -= j;
                    return new DoubleArraySpliterator(a, 0, j, characteristics);
                }
                return new DoubleArraySpliterator(a, 0, j, characteristics, Long.MAX_VALUE / 2);
            }
            return null;
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            if (action == null) throw new NullPointerException();
            it.forEachRemaining(action);
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            if (action == null) throw new NullPointerException();
            if (it.hasNext()) {
                action.accept(it.nextDouble());
                return true;
            }
            return false;
        }

        @Override
        public long estimateSize() {
            return est;
        }

        @Override
        public int characteristics() {
            return characteristics;
        }

        @Override
        public Comparator<? super Double> getComparator() {
            if (hasCharacteristics(Spliterator.SORTED))
                return null;
            throw new IllegalStateException();
        }
    }
}
