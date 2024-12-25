/*
 * Copyright (c) 1997, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;

/**
 * 该接口为实现它的每个类的对象施加了一个完全的排序规则。
 * 这种排序规则被称为类的<i>自然排序</i>，类的{@code compareTo}方法
 * 被称为该类的<i>自然比较方法</i>。<p>
 *
 * 实现此接口的对象的列表（和数组）可以通过 {@link Collections#sort(List) Collections.sort}
 * （和 {@link Arrays#sort(Object[]) Arrays.sort}）自动排序。
 * 实现此接口的对象可以作为 {@linkplain SortedMap 排序映射} 的键，
 * 或作为 {@linkplain SortedSet 排序集合} 的元素，无需指定 {@linkplain Comparator 比较器}。<p>
 *
 * 对于类 {@code C}，自然排序被称为与equals方法一致，前提是
 * {@code e1.compareTo(e2) == 0} 和 {@code e1.equals(e2)} 对于每个
 * {@code e1} 和 {@code e2} 都有相同的布尔值。请注意，{@code null}
 * 不是任何类的实例，{@code e.compareTo(null)} 应该抛出
 * {@code NullPointerException}，即使 {@code e.equals(null)}
 * 返回 {@code false}。<p>
 *
 * 强烈建议（尽管不是强制的）自然排序与equals一致。
 * 这是因为没有显式比较器的排序集合（和排序映射）在与
 * 自然排序与equals不一致的元素（或键）一起使用时会表现得“奇怪”。
 * 特别是，当向没有显式比较器的排序集合（或排序映射）中添加
 * 两个键 {@code a} 和 {@code b}，其中 {@code (!a.equals(b) && a.compareTo(b) == 0)} 时，
 * 第二次 {@code add} 操作将返回false（排序集合的大小不会增加），
 * 因为从排序集合的角度看 {@code a} 和 {@code b} 是等价的。<p>
 *
 * 几乎所有实现 {@code Comparable} 的 Java 核心类都有与 equals 一致的自然排序。
 * 其中一个例外是 {@link java.math.BigDecimal}，它的 {@linkplain
 * java.math.BigDecimal#compareTo 自然排序} 认为具有相同数值但不同表示的
 * {@code BigDecimal} 对象相等（如 4.0 和 4.00）。对于 {@link
 * java.math.BigDecimal#equals BigDecimal.equals()} 来说，只有当
 * 两个 {@code BigDecimal} 对象的表示和数值都相同时，它们才会相等。<p>
 *
 * 对于数学爱好者来说，定义自然排序的<i>关系</i>是：<pre>{@code
 *       {(x, y) 使得 x.compareTo(y) <= 0}.
 * }</pre> 此<i>商集</i>是：<pre>{@code
 *       {(x, y) 使得 x.compareTo(y) == 0}.
 * }</pre>
 *
 * 根据 {@code compareTo} 方法的契约，自然排序的商集是一个<i>等价关系</i>，
 * 自然排序是 {@code C} 上的一个<i>完全顺序</i>。当我们说一个类的自然排序
 * 与equals一致时，意味着自然排序的商集与该类的 {@link Object#equals(Object) equals} 方法定义的
 * 等价关系是相同的：<pre>
 *     {(x, y) 使得 x.equals(y)}. </pre><p>
 *
 * 换句话说，当一个类的自然排序与equals一致时，等价关系类由 {@code equals} 方法定义，
 * 与 {@code compareTo} 方法的商集定义的等价关系类是相同的。
 *
 * <p>此接口是<a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 * Java 集合框架</a>的一部分。
 *
 * @param <T> 该对象可能与之比较的对象的类型
 *
 * @author Josh Bloch
 * @see java.util.Comparator
 * @since 1.2
 */
public interface Comparable<T> {

    /**
     * 比较此对象与指定对象的顺序。根据该对象是否小于、等于或大于 指定对象，返回一个负整数、零或正整数。
     *
     * <p>实现者必须确保 {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} 对于所有的
     * {@code x} 和 {@code y} 都成立。 （这意味着 {@code
     * x.compareTo(y)} 必须抛出异常当且仅当 {@code y.compareTo(x)} 抛出异常时。）
     *
     * <p>实现者还必须确保关系是传递的：
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} 则
     * {@code x.compareTo(z) > 0}。
     *
     * <p>最后，实施者必须确保 {@code
     * x.compareTo(y)==0} 表示 {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))} 对于所有的 {@code z}。
     *
     * @apiNote
     * 强烈建议，但不是严格要求 {@code (x.compareTo(y)==0) == (x.equals(y))}。
     * 通常，任何实现了 {@code Comparable} 接口且违反此条件的类应明确指出此事实。
     * 推荐的语言是：“注意：此类具有与 equals 不一致的自然排序。”
     *
     * @param   o 要比较的对象。
     * @return 一个负整数、零或正整数，表示该对象小于、等于或大于指定对象。
     *
     * @throws NullPointerException 如果指定的对象为 null
     * @throws ClassCastException 如果指定对象的类型无法与此对象进行比较。
     */
    public int compareTo(T o);
}

