/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.gradle.api.internal.tasks.compile.incremental.jar

import spock.lang.Specification

class JarSnapshotTest extends Specification {

    def "knows when snapshots are the same"() {
        JarSnapshot s1 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['x.X', 'y.Y']), "Bar": new ClassSnapshot("b".bytes, [])])
        JarSnapshot s2 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['x.X', 'y.Y']), "Bar": new ClassSnapshot("b".bytes, [])])

        expect:
        s1.getDependentsDelta(s2).dependentClasses.isEmpty()
        s2.getDependentsDelta(s1).dependentClasses.isEmpty()
    }

    def "knows when other snapshots have extra/missing classes"() {
        JarSnapshot s1 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['X']),
                                              "Bar": new ClassSnapshot("b".bytes, ['X', 'Y']),
                                              "Car": new ClassSnapshot("c".bytes, ['Z'])])
        JarSnapshot s2 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['X'])])

        expect:
        s1.getDependentsDelta(s2).dependentClasses == ["X", "Y", "Z"] as Set
        s2.getDependentsDelta(s1).dependentClasses == [] as Set //ignore class additions
    }

    def "knows when other snapshots have class with different hash"() {
        JarSnapshot s1 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['X']),
                "Bar": new ClassSnapshot("b".bytes, ['X', 'Y']),
                "Car": new ClassSnapshot("yyy".bytes, ['Z'])])
        JarSnapshot s2 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['X']),
                "Car": new ClassSnapshot("xxx".bytes, ['Z'])])

        expect:
        s1.getDependentsDelta(s2).dependentClasses == ["X", "Y", "Z"] as Set
        s2.getDependentsDelta(s1).dependentClasses == ["Z"] as Set
    }

    def "informs that all classes are dependent"() {
        JarSnapshot s1 = new JarSnapshot(["com.Foo": new ClassSnapshot("f".bytes, ['X']),
                "Bar": new ClassSnapshot("b".bytes)])
        JarSnapshot s2 = new JarSnapshot([:])

        expect:
        s1.getDependentsDelta(s2).dependencyToAll
        s2.getDependentsDelta(s1).dependentClasses == [] as Set
    }

    def "knows if any of the classes may incur full rebuild"() {
        JarSnapshot s1 = new JarSnapshot(["A": new ClassSnapshot("A".bytes, ['B']),
                                          "B": new ClassSnapshot("B".bytes)])
        JarSnapshot s2 = new JarSnapshot(["C": new ClassSnapshot("C".bytes, ['D'])])

        expect:
        s1.containsFullRebuildClasses()
        !s2.containsFullRebuildClasses()
    }
}
