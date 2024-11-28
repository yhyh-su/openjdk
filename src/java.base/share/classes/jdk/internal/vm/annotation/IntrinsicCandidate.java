/*
 * Copyright (c) 2015, 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.vm.annotation;

import java.lang.annotation.*;

/**
 IntrinsicCandidate 注解是专门为 HotSpot 虚拟机设计的。它表示被注解的方法可能会（但不保证）被 HotSpot VM 内部实现为内联方法（intrinsified）。如果一个方法被内联，表示 HotSpot VM 会用手写的汇编代码和/或手写的编译器中间表示（IR）替换该方法——即编译器内联（compiler intrinsic），以提升性能。{@code @IntrinsicCandidate} 注解是 Java 库的内部实现，因此不应对应用程序代码产生影响。
 Java 库的维护者在修改带有 {@code @IntrinsicCandidate} 注解的方法时，必须考虑以下事项：
 <ul>
 <li>修改带有 {@code @IntrinsicCandidate} 注解的方法时，必须更新 HotSpot VM 实现中的相应内联代码，以匹配被注解方法的语义。</li>
 <li>对于某些带注解的方法，对应的内联实现可能会省略一些低级别的检查，这些检查通常在使用 Java 字节码实现内联时会进行。这是因为每个 Java 字节码操作都会隐式检查像 {@code NullPointerException} 和 {@code ArrayStoreException} 这样的异常。如果这样的一个方法被汇编语言编写的内联代码替代，那么所有在字节码操作中正常执行的检查，必须在进入汇编代码之前完成。这些检查必须适当执行，针对所有内联的参数以及通过这些参数获得的其他值进行检查。这些检查可以通过检查方法中的非内联 Java 代码来推导出，并确定该代码可能抛出的异常，包括未声明的隐式 {@code RuntimeException}。因此，根据内联访问的数据，这些检查可能包括：
 <ul>
 <li>对引用类型的 null 检查</li>
 <li>对用作数组索引的原始类型值的范围检查</li>
 <li>对原始类型值的其他有效性检查（例如，除零条件检查）</li>
 <li>对存储在数组中的引用类型值进行存储检查</li>
 <li>对在内联操作中使用的数组的长度检查</li>
 <li>对引用类型的强制转换检查（当形式参数为 {@code Object} 或其他弱类型时）</li>
 </ul>
 </li>
 <li>请注意，接收者值（{@code this}）会作为额外的参数传递给所有非静态方法。如果一个非静态方法是内联的，接收者值不需要进行 null 检查，但（如上所述）从对象字段加载的任何值也必须进行检查。为了清晰起见，最好将内联方法定义为静态方法，以明确依赖于 {@code this}。另外，最好在进入内联代码之前显式加载所有所需的对象字段值，并将这些值作为显式参数传递。首先，这可能是为了 null 检查（或其他检查）。其次，如果内联方法从字段重新加载值并在没有检查的情况下操作这些值，竞态条件可能会将未经检查的无效值引入内联代码。如果内联方法需要将一个值存回对象字段，该值应该显式地从内联方法返回；如果有多个返回值，开发人员应该考虑将它们缓存在一个数组中。将字段访问从内联方法中移除不仅可以明确 JVM 和 JDK 之间的接口，还可以帮助解耦 HotSpot 和 JDK 的实现，因为如果 JDK 代码在内联方法前后处理所有字段访问，那么内联方法可以被编写为与对象布局无关。</li>
 HotSpot VM 的维护者在修改内联方法时必须考虑以下事项：
 <ul>
 <li>当添加新的内联方法时，确保 Java 库中相应的方法被 {@code @IntrinsicCandidate} 注解，并且所有导致调用该内联方法的调用序列都包含内联省略的检查（如果有）。</li>
 <li>当修改现有的内联方法时，必须更新 Java 库，以匹配内联方法的语义，并执行所有内联省略的检查（如果有）。</li>
 </ul>
 与 Java 库或 HotSpot VM 的维护无关的人可以安全地忽略方法是否被 {@code @IntrinsicCandidate} 注解。
 HotSpot VM 内部定义了一个内联方法列表。并不是所有的内联方法都在 HotSpot VM 支持的所有平台上可用。此外，内联方法在某个平台上的可用性取决于 HotSpot VM 的配置（例如启用的 VM 标志集）。因此，给方法加上 {@code @IntrinsicCandidate} 注解并不保证该方法一定会被 HotSpot VM 内联。
 如果启用了 {@code CheckIntrinsics} VM 标志，HotSpot VM 会在加载类时检查：（1）该类中的所有方法是否被 {@code @IntrinsicCandidate} 注解，并且（2）该类中所有带 {@code @IntrinsicCandidate} 注解的方法是否在内联方法列表中。
 @since 16 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntrinsicCandidate {
}
