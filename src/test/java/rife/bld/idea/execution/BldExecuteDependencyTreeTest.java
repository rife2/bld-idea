/*
 * Copyright 2024 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.idea.execution;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class BldExecuteDependencyTreeTest {
    // the dependency-tree output of bld 3.0.1, as the process handler hands it over
    private static final List<String> OUTPUT = """
        extensions:
        ├─ com.uwyn.rife2:bld-antlr4:1.4.5
        │  ├─ com.uwyn.rife2:bld:3.0.1
        │  └─ org.antlr:antlr4:4.11.1
        │     ├─ org.antlr:antlr4-runtime:4.11.1
        │     └─ com.ibm.icu:icu4j:71.1
        ├─ com.uwyn.rife2:bld-tests-badge:1.6.5
        └─ com.uwyn.rife2:bld-junit-reporter:1.2.0
           └─ com.uwyn.rife2:bld-extensions-tools:1.3.0-SNAPSHOT

        compile:
        └─ com.uwyn.rife2:rife2:1.10.1@modular-jar

        provided:
        no dependencies

        runtime:
        no dependencies

        test:
        ├─ org.junit.jupiter:junit-jupiter:6.1.3
        │  ├─ org.junit.jupiter:junit-jupiter-api:6.1.3
        │  │  └─ org.jspecify:jspecify:1.0.0
        │  └─ org.junit.jupiter:junit-jupiter-engine:6.1.3
        │     └─ org.junit.platform:junit-platform-engine:6.1.3
        └─ org.junit.platform:junit-platform-console-standalone:6.1.3
        """.lines().map(line -> line + "\n").toList();

    @Test
    public void parsesEveryScopeIntoItsNesting() {
        var tree = BldExecuteDependencyTree.parse(OUTPUT);

        assertEquals("""
            Extensions
              com.uwyn.rife2:bld-antlr4:1.4.5
                com.uwyn.rife2:bld:3.0.1
                org.antlr:antlr4:4.11.1
                  org.antlr:antlr4-runtime:4.11.1
                  com.ibm.icu:icu4j:71.1
              com.uwyn.rife2:bld-tests-badge:1.6.5
              com.uwyn.rife2:bld-junit-reporter:1.2.0
                com.uwyn.rife2:bld-extensions-tools:1.3.0-SNAPSHOT
            Compile
              com.uwyn.rife2:rife2:1.10.1@modular-jar
            Provided
            Runtime
            Test
              org.junit.jupiter:junit-jupiter:6.1.3
                org.junit.jupiter:junit-jupiter-api:6.1.3
                  org.jspecify:jspecify:1.0.0
                org.junit.jupiter:junit-jupiter-engine:6.1.3
                  org.junit.platform:junit-platform-engine:6.1.3
              org.junit.platform:junit-platform-console-standalone:6.1.3
            """, render(tree));
    }

    @Test
    public void linksEachDependencyToItsParent() {
        var tree = BldExecuteDependencyTree.parse(OUTPUT);

        var antlr4 = tree.extensions.children().get(0).children().get(1);
        var icu4j = antlr4.children().get(1);
        assertSame(antlr4, icu4j.parent());
        assertSame(tree.extensions, tree.extensions.children().get(1).parent());
    }

    private static String render(List<BldDependencyNode> nodes) {
        var result = new StringBuilder();
        for (var node : nodes) {
            render(node, 0, result);
        }
        return result.toString();
    }

    private static void render(BldDependencyNode node, int depth, StringBuilder result) {
        result.append("  ".repeat(depth)).append(node.name()).append('\n');
        for (var child : node.children()) {
            render(child, depth + 1, result);
        }
    }
}
