package nl.han.ica.datastructures;

public class ListNode<T> {
    T element;
    ListNode<T> next;

    public ListNode(T element, ListNode<T> next) {
        this.element = element;
        this.next = next;
    }

    public ListNode() {
    }
}
