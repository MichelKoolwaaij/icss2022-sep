package nl.han.ica.icss.parser;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.Stylesheet;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.checker.Checker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CheckerTest {
    @Test
    public void checkErrorsAddingDifferentTypes(){
        //arrange
        Stylesheet stylesheet = new Stylesheet();
        stylesheet.addChild((new Stylerule())
                .addChild(new IdSelector("#menu"))
                .addChild((new Declaration("width"))
                        .addChild((new AddOperation())
                                        .addChild(new PercentageLiteral("2%"))
                                        .addChild(new PixelLiteral("10px"))

                                )));
        //act
        AST ast = new AST(stylesheet);
        Checker checker = new Checker();
        checker.check(ast);

        //assert
        assertNotNull(ast.getErrors());
        assertEquals("Literals must be of the same type", ast.getErrors().get(0).description);
    }

    @Test
    public void checkErrorsAddingSameType(){
        //arrange
        Stylesheet stylesheet = new Stylesheet();
        stylesheet.addChild((new Stylerule())
                .addChild(new IdSelector("#menu"))
                .addChild((new Declaration("width"))
                        .addChild((new AddOperation())
                                .addChild(new PixelLiteral("2px") )
                                .addChild(new PixelLiteral("10px"))

                        )));
        //act
        AST ast = new AST(stylesheet);
        Checker checker = new Checker();
        checker.check(ast);

        //assert
        assertEquals(new ArrayList<>(), ast.getErrors());
    }
}
