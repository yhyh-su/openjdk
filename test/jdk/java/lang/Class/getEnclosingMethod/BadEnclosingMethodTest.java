/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @summary Test behaviors with bad EnclosingMethod attribute
 * @library /test/lib
 * @enablePreview
 * @run junit BadEnclosingMethodTest
 */

import jdk.test.lib.ByteCodeLoader;
import org.junit.jupiter.api.Test;

import java.lang.classfile.ClassFile;
import java.lang.classfile.attribute.EnclosingMethodAttribute;
import java.lang.constant.ClassDesc;
import java.lang.reflect.GenericSignatureFormatError;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BadEnclosingMethodTest {

    private Class<?> loadTestClass(String name, String type) throws Exception {
        var bytes = ClassFile.of().build(ClassDesc.of("Test"), clb -> {
            var cp = clb.constantPool();
            // fake a container
            var enclosingClass = cp.classEntry(CD_Object); // fake a container class
            var enclosingMethodName = cp.utf8Entry(name);
            var enclosingMethodType = cp.utf8Entry(type); // a malformed method type
            clb.with(EnclosingMethodAttribute.of(enclosingClass, Optional.of(cp.nameAndTypeEntry(
                    enclosingMethodName, enclosingMethodType
            ))));
        });

        return ByteCodeLoader.load("Test", bytes);
    }

    @Test
    void testBadTypes() throws Exception {
        var malformedMethodType = loadTestClass("methodName", "(L[;)V");
        assertThrows(GenericSignatureFormatError.class,
                malformedMethodType::getEnclosingMethod);

        var malformedConstructorType = loadTestClass(INIT_NAME, "(L[;)V");
        assertThrows(GenericSignatureFormatError.class,
                malformedConstructorType::getEnclosingConstructor);

        var absentMethodType = loadTestClass("methodName", "(Ldoes/not/Exist;)V");
        assertThrows(TypeNotPresentException.class,
                absentMethodType::getEnclosingMethod);

        var absentConstructorType = loadTestClass(INIT_NAME, "(Ldoes/not/Exist;)V");
        assertThrows(TypeNotPresentException.class,
                absentConstructorType::getEnclosingConstructor);
    }
}
