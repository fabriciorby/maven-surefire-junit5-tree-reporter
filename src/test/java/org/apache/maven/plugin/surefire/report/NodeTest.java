package org.apache.maven.plugin.surefire.report;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NodeTest {

    @BeforeEach
    void setup() {
       Node.clearTree();
    }

    @Test
    void addNode() {
        //Given I have a empty ROOT
        Node root = Node.getRoot();
        assertThat(root.getName()).isEqualTo("ROOT");
        assertThat(root.branches).isEmpty();

        //When I add 5 children in sequence
        Node child = root.addChildren("how", "are", "you", "?");
        assertThat(root.branches).hasSize(1);
        assertThat(root.branches.get(0).getName()).isEqualTo("how");
        assertThat(child.getName()).isEqualTo("?");
        assertThat(child.getParent().getName()).isEqualTo("you");

        //And then add more 3 items in the middle of the tree
        Node areNode = root.getBranchNode(Lists.newArrayList("how", "are")).get();
        assertThat(areNode.branches).hasSize(1);

        //I should have a node with two branches in a tree that looks like this
        /*
                ROOT
                 |
                how
                 |
                are
               /   \
             you   your
              |     |
              ?   parents
        */
        Node newChild = areNode.addChildren("your", "parents");
        assertThat(newChild.getName()).isEqualTo("parents");
        assertThat(newChild.getParent().getName()).isEqualTo("your");
        assertThat(areNode.branches).hasSize(2);
    }

    @Nested
    class BranchLookupTests {

        @Test
        void getBranchNode_returnsNodeWhenExists() {
            Node root = Node.getRoot();
            root.addChildren("TestClass");

            Optional<Node> result = root.getBranchNode("TestClass");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("TestClass");
        }

        @Test
        void getBranchNode_returnsEmptyWhenNotExists() {
            Node root = Node.getRoot();
            root.addChildren("TestClass");

            Optional<Node> result = root.getBranchNode("NonExistentClass");

            assertThat(result).isEmpty();
        }

        @Test
        void containsBranch_returnsTrueWhenExists() {
            Node root = Node.getRoot();
            root.addChildren("TestClass");

            assertThat(root.containsBranch("TestClass")).isTrue();
        }

        @Test
        void containsBranch_returnsFalseWhenNotExists() {
            Node root = Node.getRoot();
            root.addChildren("TestClass");

            assertThat(root.containsBranch("NonExistentClass")).isFalse();
        }

        @Test
        void getBranchNode_withPath_returnsDeepNestedNode() {
            Node root = Node.getRoot();
            root.addChildren("OuterClass", "InnerClass", "DeepInnerClass");

            Optional<Node> result = root.getBranchNode(
                    Lists.newArrayList("OuterClass", "InnerClass", "DeepInnerClass"));

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("DeepInnerClass");
        }

        @Test
        void getBranchNode_withPath_returnsEmptyForPartialPath() {
            Node root = Node.getRoot();
            root.addChildren("OuterClass", "InnerClass");

            Optional<Node> result = root.getBranchNode(
                    Lists.newArrayList("OuterClass", "InnerClass", "NonExistent"));

            assertThat(result).isEmpty();
        }

        @Test
        void lookupPerformance_withManyBranches() {
            Node root = Node.getRoot();

            // Add many branches to simulate a large test suite
            for (int i = 0; i < 1000; i++) {
                root.addChildren("TestClass" + i);
            }

            // Lookup should be O(1) with HashMap
            long startTime = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                assertThat(root.containsBranch("TestClass" + i)).isTrue();
                assertThat(root.getBranchNode("TestClass" + i)).isPresent();
            }
            long duration = System.nanoTime() - startTime;

            // Should complete very quickly with O(1) lookups
            assertThat(duration).isLessThan(100_000_000L); // 100ms
        }
    }

    @Nested
    class RemoveBranchTests {

        @Test
        void removeBranch_removesBranchFromListAndMap() {
            Node root = Node.getRoot();
            root.addChildren("TestClass1");
            root.addChildren("TestClass2");
            root.addChildren("TestClass3");

            Node nodeToRemove = root.getBranchNode("TestClass2").get();
            root.removeBranch(nodeToRemove);

            assertThat(root.branches).hasSize(2);
            assertThat(root.containsBranch("TestClass1")).isTrue();
            assertThat(root.containsBranch("TestClass2")).isFalse();
            assertThat(root.containsBranch("TestClass3")).isTrue();
            assertThat(root.getBranchNode("TestClass2")).isEmpty();
        }

        @Test
        void removeBranch_maintainsOrderInList() {
            Node root = Node.getRoot();
            root.addChildren("First");
            root.addChildren("Second");
            root.addChildren("Third");

            Node nodeToRemove = root.getBranchNode("Second").get();
            root.removeBranch(nodeToRemove);

            assertThat(root.branches).hasSize(2);
            assertThat(root.branches.get(0).getName()).isEqualTo("First");
            assertThat(root.branches.get(1).getName()).isEqualTo("Third");
        }

        @Test
        void removeBranch_worksForNestedNodes() {
            Node root = Node.getRoot();
            root.addChildren("Parent", "Child1");
            root.addChildren("Parent", "Child2");

            Node parent = root.getBranchNode("Parent").get();
            Node childToRemove = parent.getBranchNode("Child1").get();
            parent.removeBranch(childToRemove);

            assertThat(parent.branches).hasSize(1);
            assertThat(parent.containsBranch("Child1")).isFalse();
            assertThat(parent.containsBranch("Child2")).isTrue();
        }
    }

    @Nested
    class ClearTreeTests {

        @Test
        void clearTree_clearsAllBranchesAndMaps() {
            Node root = Node.getRoot();
            root.addChildren("Class1");
            root.addChildren("Class2");
            root.addChildren("Class3", "Nested");

            Node.clearTree();

            assertThat(root.branches).isEmpty();
            assertThat(root.containsBranch("Class1")).isFalse();
            assertThat(root.containsBranch("Class2")).isFalse();
            assertThat(root.containsBranch("Class3")).isFalse();
            assertThat(root.getBranchNode("Class1")).isEmpty();
        }
    }
}