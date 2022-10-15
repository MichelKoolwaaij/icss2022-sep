package nl.han.ica.datastructures;

import java.util.Iterator;

public class HANLinkedList<T> implements IHANLinkedList<T>{
    private ListNode<T> header;
    private int length = 0;

    public HANLinkedList(){
        this.header = new ListNode<>();
    }

    /**
     * Adds value to the front of the list
     *
     * @param value generic value to be added
     */
    @Override
    public void addFirst(T value) {
        ListNode<T> tmp = new ListNode<>();
        tmp.element = value;
        if (length != 0) {
            var next = header.next;
            header.next = tmp;
            header.next.next = next;
        }
        else header.next = tmp;
        length++;
    }

    /**
     * Clears list. Size equals 0 afterwards
     */
    @Override
    public void clear() {
        header.next = null;
        length = 0;
    }

    /**
     * Adds value to index position
     *
     * @param index the position
     * @param value the value to add at index
     */
    @Override
    public void insert(int index, T value) {
        if(length == 0){
            addFirst(value);
        }
        else if(index<=length){
            ListNode<T> currentNode = header;
            for (int i = 0; i < index; i++) {
                currentNode = currentNode.next;
            }
            ListNode<T> tmp = new ListNode<>();
            tmp.element = value;
            tmp.next = currentNode.next;
            currentNode.next = tmp;
            length++;
        }
        else throw new IndexOutOfBoundsException();
    }

    /**
     * Deletes value at position
     *
     * @param pos position where value is deleted
     */
    @Override
    public void delete(int pos) {
        if(pos <= length){
            ListNode<T> current = header;
            for (int i = 0; i < pos; i++) {
                current = current.next;
            }
            current.next = current.next.next;
            length--;
        }
        else throw new IndexOutOfBoundsException();
    }

    /**
     * Returns generic value T at postion
     *
     * @param pos position to look up value
     * @return value at position pos
     */
    @Override
    public T get(int pos) {
        if(pos<length){
            ListNode<T> current = header;
            for (int i = 0; i < pos; i++) {
                current = current.next;
            }
            return current.next.element;
        }
        return null;
    }

    /**
     * Removes first element
     */
    @Override
    public void removeFirst() {
        header.next = header.next.next;
        length--;
    }

    /**
     * Returns first element in O(n) time
     *
     * @return first element
     */
    @Override
    public T getFirst() {
        return header.next.element;
    }

    /**
     * Determines size of the list, equals the number of stored items but not the header node
     *
     * @return number of items in list
     */
    @Override
    public int getSize() {
        return length;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator iterator() {
        return new Iterator() {
            @Override
            public boolean hasNext() {
                return header.next != null;
            }

            @Override
            public Object next() {
                return header.next.element;
            }
        };
    }
}
