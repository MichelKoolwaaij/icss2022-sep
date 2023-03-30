package nl.han.ica.datastructures;

import nl.han.ica.icss.ast.types.ExpressionType;
import org.junit.jupiter.api.Test;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HANLinkedListTest {

    @Test
    void addFirst() {
        //arrange
        IHANLinkedList<HashMap<String, ExpressionType>> test;
        test = new HANLinkedList<>();
        var first = new HashMap<String, ExpressionType>();
        first.put("AdjustColor", ExpressionType.BOOL);
        var second = new HashMap<String, ExpressionType>();
        second.put("LinkColor", ExpressionType.COLOR);
        //act
        test.addFirst(first);
        test.addFirst(second);
        //assert
        assertEquals(test.getFirst().get("LinkColor"), ExpressionType.COLOR);
    }

    @Test
    void insert() {
        //arrange
        IHANLinkedList<HashMap<String, ExpressionType>> test;
        test = new HANLinkedList<>();
        var first = new HashMap<String, ExpressionType>();
        var second = new HashMap<String, ExpressionType>();
        second.put("LinkColor", ExpressionType.COLOR);
        //act
        test.insert(0, first);
        test.insert(1, second);
        //assert
        assertEquals(test.getSize(), 2);
        assertEquals(test.getFirst(), first);
        assertEquals(test.get(1), second);
    }

    @Test
    void get() {
        //arrange
        IHANLinkedList<HashMap<String, ExpressionType>> test = new HANLinkedList<>();
        var first = new HashMap<String, ExpressionType>();
        var second = new HashMap<String, ExpressionType>();
        second.put("LinkColor", ExpressionType.COLOR);
        test.addFirst(first);
        test.addFirst(second);
        //act
        //assert
        assertEquals(test.get(0),second);
        assertEquals(test.get(1),first);
        assertNull(test.get(2));
    }

    @Test
    void iterator() {
        //arrange
        IHANLinkedList<HashMap<String, ExpressionType>> test = new HANLinkedList<>();
        var first = new HashMap<String, ExpressionType>();
        first.put("UseLinkColor", ExpressionType.BOOL);
        test.addFirst(first);
        //assert
        test.iterator().next();
        assertTrue(test.iterator().hasNext());
        test.removeFirst();
        assertFalse(test.iterator().hasNext());
    }
}
