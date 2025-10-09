package org.apache.maven.plugin.surefire.report;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}