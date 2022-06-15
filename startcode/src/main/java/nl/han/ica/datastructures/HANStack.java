package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T>{
    private ListNode<T> topOfStack;
    /**
     * pushes value T to the top of the stack
     *
     * @param value value to push
     */
    @Override
    public void push(T value) {
        topOfStack = new ListNode<>(value, topOfStack);
    }

    /**
     * Pops (and removes) value at top of stack
     *
     * @return popped value
     */
    @Override
    public T pop() {
        T element = topOfStack.element;
        topOfStack = topOfStack.next;
        return element;
    }

    /**
     * Peeks at the top of the stack. Does not remove anything
     *
     * @return value at the top of the stack
     */
    @Override
    public T peek() {
        return topOfStack.element;
    }
}
