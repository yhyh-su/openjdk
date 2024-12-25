/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
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

/**
 * 虚拟线程实现的基类。
 */
abstract sealed class BaseVirtualThread extends Thread
        permits VirtualThread, ThreadBuilders.BoundVirtualThread {

    /**
     * 初始化一个虚拟线程。
     * @param name 线程名称，可以为 null
     * @param characteristics 线程的特性
     * @param bound 当线程绑定到操作系统线程时为 true
     */
    BaseVirtualThread(String name, int characteristics, boolean bound) {
        // 调用父类 Thread 的构造方法进行初始化
        super(name, characteristics, bound);
    }

    /**
     * 将当前虚拟线程挂起，直到停车许可证可用或线程被中断。
     *
     * 如果当前线程不是此线程，行为未定义。
     */
    abstract void park();

    /**
     * 将当前虚拟线程挂起，直到停车许可证可用、线程被中断，或达到指定的等待时间（纳秒）。
     *
     * 如果当前线程不是此线程，行为未定义。
     */
    abstract void parkNanos(long nanos);

    /**
     * 向指定的虚拟线程释放停车许可证。
     */
    abstract void unpark();
}


