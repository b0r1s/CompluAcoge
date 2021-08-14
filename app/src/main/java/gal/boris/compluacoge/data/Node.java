package gal.boris.compluacoge.data;


import androidx.core.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class Node<T> {

    // En procedure hay una parte que los copia,
    // asi que hay que tenerlo en cuenta si añado mas atributos

    private final T node;
    private final List<Node<T>> parents;
    private final List<Node<T>> children;

    public Node(T node) {
        this.node = node;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public void addParent(Node<T> parent) {
        this.parents.add(parent);
    }

    public void addParents(Collection<Node<T>> parents) {
        this.parents.addAll(parents);
    }

    public void addChild(Node<T> child) {
        this.children.add(child);
    }

    public void addChildren(Collection<Node<T>> children) {
        this.children.addAll(children);
    }

    public T getNode() {
        return node;
    }

    public List<Node<T>> getParents() {
        return parents;
    }

    public boolean isBeginning() {
        return parents.isEmpty();
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public boolean isEnd() {
        return children.isEmpty();
    }

    public boolean canDelete() { //No inicial y no deja hijos huerfanos
        boolean answer = true;
        for(Node<T> child : children) {
            if(child.getParents().size()==1) {
                answer = false;
            }
        }
        return answer && !isBeginning();
    }

    public List<Node<T>> getAncestors() {
        Set<Node<T>> visited = new HashSet<>();
        Queue<Node<T>> queue = new ArrayDeque<>();
        visited.add(this);
        queue.add(this);
        while(!queue.isEmpty()) {
            Node<T> node = queue.remove();
            for(Node<T> parent : node.getParents()) {
                if(!visited.contains(parent)) {
                    visited.add(parent);
                    queue.add(parent);
                }
            }
        }
        return new ArrayList<>(visited);
    }

    public Pair<Boolean,Set<Node<T>>> allRoadsLeadToRome(Node<T> end) {
        Queue<Node<T>> queue = new ArrayDeque<>();
        Set<Node<T>> visited = new HashSet<>();
        boolean allToEnd = true;
        queue.add(this);
        while(!queue.isEmpty()) {
            Node<T> node = queue.remove();
            if(!visited.contains(node)) {
                visited.add(node);
                if(node.isEnd()) {
                    if(!node.equals(end)) allToEnd = false;
                }
                else queue.addAll(node.getChildren());
            }
        }
        return new Pair<>(allToEnd,visited);
    }

    public int getShortestWay(Node<T> end) {
        Queue<Pair<Node<T>,Integer>> queue = new ArrayDeque<>();
        Set<Node<T>> visited = new HashSet<>();
        queue.add(new Pair<>(this,0));
        if(this.equals(end)) {
            return 0;
        }
        visited.add(this);
        while(!queue.isEmpty()) {
            Pair<Node<T>,Integer> now = queue.remove();
            for(Node<T> child : now.first.getChildren()) {
                if(!visited.contains(child)) {
                    visited.add(child);
                    if(child.equals(end)) {
                        return now.second+1;
                    }
                    queue.add(new Pair<>(child, now.second + 1));
                }
            }
        }
        return -1;
    }

    // --- Equals --- CUIDADO!!!!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node1 = (Node<?>) o;
        return Objects.equals(node, node1.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }
}
