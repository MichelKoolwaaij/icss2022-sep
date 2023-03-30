package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CheckerTest {
    @Test
    //CH02
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
        assertEquals("Cannot add or subtract [Percentage literal (2)|] and [Pixel literal (10)|] because they have different types",ast.getErrors().get(0).description);
    }

    @Test
    //CH02
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
        assert(ast.getErrors().size()==0);
    }

    @Test
    //CH02
    public void checkErrorsAddingVariableSameType(){
        //arrange
        Stylesheet stylesheet = new Stylesheet();
        stylesheet.addChild((new VariableAssignment())
                .addChild(new VariableReference("ParWidth"))
                .addChild(new PixelLiteral("500px"))
        );
        stylesheet.addChild((new Stylerule())
                .addChild(new IdSelector("#menu"))
                .addChild((new Declaration("width"))
                        .addChild((new AddOperation())
                                .addChild(new VariableReference("ParWidth"))
                                .addChild(new PixelLiteral("10px"))

                        )));
        //act
        AST ast = new AST(stylesheet);
        Checker checker = new Checker();
        checker.check(ast);

        //assert
        assert(ast.getErrors().size()==0);
    }

    @Test
    //CH02
    public void checkErrorsAddingVariableDifferentType(){
        //arrange
        Stylesheet stylesheet = new Stylesheet();
        stylesheet.addChild((new VariableAssignment())
                .addChild(new VariableReference("ParWidth"))
                .addChild(new PercentageLiteral("500%"))
        );
        stylesheet.addChild((new Stylerule())
                .addChild(new IdSelector("#menu"))
                .addChild((new Declaration("width"))
                        .addChild((new AddOperation())
                                .addChild(new VariableReference("ParWidth"))
                                .addChild(new PixelLiteral("10px"))

                        )));
        //act
        AST ast = new AST(stylesheet);
        Checker checker = new Checker();
        checker.check(ast);

        //assert
        assert(ast.getErrors().size()>0);
        assertEquals("Cannot add or subtract [VariableReference (ParWidth)|] and [Pixel literal (10)|] because they have different types", ast.getErrors().get(0).description);
    }

    @Test
    //CH04
    public void checkErrorsInvalidPropertyType(){
        //arrange
        Stylesheet stylesheet = new Stylesheet();
        stylesheet.addChild((new Stylerule())
                .addChild(new ClassSelector(".menu"))
                .addChild((new Declaration("width"))
                        .addChild(new ColorLiteral("#000000")))
        );
        //act
        AST ast = new AST(stylesheet);
        Checker checker = new Checker();
        checker.check(ast);

        //assert
        assert(ast.getErrors().size()>0);
        assertEquals("Property width must be of type [PIXEL, PERCENTAGE]", ast.getErrors().get(0).description);
    }
}
