/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates. All rights reserved.
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
package jdk.internal.vm;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.invoke.MhUtil;

/**
 * 一个“共享”的线程容器。共享线程容器没有所有者，
 * 旨在用于非结构化的用途，例如线程池。.
 */
public class SharedThreadContainer extends ThreadContainer implements AutoCloseable {
    private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();
    private static final VarHandle CLOSED;
    private static final VarHandle VIRTUAL_THREADS;

    static {
        MethodHandles.Lookup l = MethodHandles.lookup();
        CLOSED = MhUtil.findVarHandle(l, "closed", boolean.class);
        VIRTUAL_THREADS = MhUtil.findVarHandle(l, "virtualThreads", Set.class);
    }

    // name of container, used by toString
    private final String name;

    // the virtual threads in the container, created lazily
    private volatile Set<Thread> virtualThreads;

    // the key for this container in the registry
    private volatile Object key;

    // 容器关闭时设置为 true
    private volatile boolean closed;

    /**
     * Initialize a new SharedThreadContainer.
     * @param name the container name, can be null
     */
    private SharedThreadContainer(String name) {
        super(/*shared*/ true);
        this.name = name;
    }

    /**
     * 用给定的父容器和名字创建一个共享线程容器
     * @throws IllegalArgumentException 如果父容器已经有拥有者
     */
    public static SharedThreadContainer create(ThreadContainer parent, String name) {
        if (parent.owner() != null) throw new IllegalArgumentException("parent has owner");
        var container = new SharedThreadContainer(name);
        // register the container to allow discovery by serviceability tools
        container.key = ThreadContainers.registerContainer(container);
        return container;
    }

    /**
     * Creates a shared thread container with the given name. Its parent will be
     * the root thread container.
     */
    public static SharedThreadContainer create(String name) {
        return create(ThreadContainers.root(), name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Thread owner() {
        return null;
    }

    @Override
    public void onStart(Thread thread) {
        // 如果是虚拟线程（virtual thread），则需要进行跟踪
        if (thread.isVirtual()) {
            // 获取当前线程容器的虚拟线程集合
            Set<Thread> vthreads = this.virtualThreads;
            // 如果虚拟线程集合为空，则需要初始化它
            if (vthreads == null) {
                // 使用 ConcurrentHashMap.newKeySet() 创建一个线程安全的 Set 用于存储虚拟线程
                vthreads = ConcurrentHashMap.newKeySet();
                // 尝试使用 CAS（Compare-And-Set）更新虚拟线程集合
                if (!VIRTUAL_THREADS.compareAndSet(this, null, vthreads)) {
                    // 如果 CAS 失败，表示在这段时间内其他线程已经初始化了虚拟线程集合
                    // 直接获取当前虚拟线程集合
                    vthreads = this.virtualThreads;
                }
            }
            // 将当前虚拟线程添加到集合中
            vthreads.add(thread);
        }
    }


    @Override
    public void onExit(Thread thread) {
        if (thread.isVirtual())
            virtualThreads.remove(thread);
    }

    @Override
    public Stream<Thread> threads() {
        // live platform threads in this container
        Stream<Thread> platformThreads = Stream.of(JLA.getAllThreads())
                .filter(t -> JLA.threadContainer(t) == this);
        Set<Thread> vthreads = this.virtualThreads;
        if (vthreads == null) {
            // live platform threads only, no virtual threads
            return platformThreads;
        } else {
            // all live threads in this container
            return Stream.concat(platformThreads,
                    vthreads.stream().filter(Thread::isAlive));
        }
    }

    /**
     * 启动一个此容器中的线程
     * @throws IllegalStateException 如果容器已关闭
     */
    public void start(Thread thread) {
        if (closed) throw new IllegalStateException();
        JLA.start(thread, this);
    }

    /**
     * Closes this container. Further attempts to start a thread in this container
     * throw IllegalStateException. This method has no impact on threads that are
     * still running or starting around the time that this method is invoked.
     */
    @Override
    public void close() {
        if (!closed && CLOSED.compareAndSet(this, false, true)) {
            ThreadContainers.deregisterContainer(key);
        }
    }
}
