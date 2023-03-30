package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvaluatorTest {

    @Test
    void transformTrueIfClause() {
        //arrange
        Evaluator sut = new Evaluator();
        Stylesheet stylesheet = new Stylesheet();
        ASTNode exptectedNode = new Declaration("color").addChild(new ColorLiteral("#124532"));
        stylesheet.addChild((new VariableAssignment())
                .addChild(new VariableReference("AdjustColor"))
                .addChild(new BoolLiteral(true))
        );
        stylesheet.addChild((new Stylerule())
                .addChild(new TagSelector("p"))
                .addChild((new IfClause())
                        .addChild(new VariableReference("AdjustColor"))
                        .addChild(exptectedNode)

                )
        );
        Stylesheet output = new Stylesheet();
        output.addChild(new Stylerule().addChild(new TagSelector("p"))
                .addChild(exptectedNode)
        );
        //act
        AST ast = new AST(stylesheet);
        sut.apply(ast);
        //assert
        Assertions.assertEquals(output, ast.root);
    }

    @Test
    void transformFalseIfClause() {
        //arrange
        Evaluator sut = new Evaluator();
        Stylesheet input = new Stylesheet();
        ASTNode exptectedNode = new Declaration("color").addChild(new ColorLiteral("#124532"));
        input.addChild((new VariableAssignment())
                .addChild(new VariableReference("AdjustColor"))
                .addChild(new BoolLiteral(false))
        );
        input.addChild((new Stylerule())
                .addChild(new TagSelector("p"))
                .addChild((new IfClause())
                        .addChild(new VariableReference("AdjustColor"))
                        .addChild(exptectedNode)

                )
        );
        Stylesheet output = new Stylesheet();
        output.addChild(new Stylerule().addChild(new TagSelector("p")));
        //act
        AST ast = new AST(input);
        sut.apply(ast);
        //assert
        Assertions.assertEquals(output, ast.root);
    }

    @Test
    void checkAddingVariable() {

    }
}