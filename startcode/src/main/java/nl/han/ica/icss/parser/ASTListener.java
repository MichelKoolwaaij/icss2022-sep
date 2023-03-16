package nl.han.ica.icss.parser;

import java.util.ArrayList;
import java.util.Collections;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private IHANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>();
    }

    public AST getAST() {
        return ast;
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        var children = new ArrayList<ASTNode>();

        for (int i = 0; i < ctx.variableAssignment().size() + ctx.stylerule().size(); i++) {
            children.add(currentContainer.pop());
        }

        for (var li = children.listIterator(children.size()); li.hasPrevious(); ) {
            ast.root.addChild(li.previous());
        }
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        var value = currentContainer.pop();
        var name = currentContainer.pop();
        currentContainer.push(new VariableAssignment().addChild(name).addChild(value));
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        var stylerule = new Stylerule();

        for (int i = 0; i < ctx.selector().size() + ctx.styleruleBody().size(); i++) {
            stylerule.addChild(currentContainer.pop());
        }
        Collections.reverse(stylerule.body);
        currentContainer.push(stylerule);
    }

    @Override
    public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
        currentContainer.push(new TagSelector(ctx.LOWER_IDENT().getText()));
    }

    @Override
    public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
        currentContainer.push(new ClassSelector(ctx.CLASS_IDENT().getText()));
    }

    @Override
    public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
        currentContainer.push(new IdSelector(ctx.ID_IDENT().getText()));
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        currentContainer.push(new Declaration(ctx.LOWER_IDENT().getText()).addChild(currentContainer.pop()));
    }

    @Override
    public void exitIfClause(ICSSParser.IfClauseContext ctx) {
        var ifClause = new IfClause();

        for (var i = 0; i <= ctx.styleruleBody().size(); i++) {
            ifClause.addChild(currentContainer.pop());
        }
        Collections.reverse(ifClause.body);
        currentContainer.push(ifClause);
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        var elseClause = new ElseClause();

        for (var i = 0; i < ctx.styleruleBody().size(); i++) {
            elseClause.addChild(currentContainer.pop());
        }
        Collections.reverse(elseClause.body);
        currentContainer.peek().addChild(elseClause);
    }

    @Override
    public void exitMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
        var rhs = currentContainer.pop();
        var lhs = currentContainer.pop();
        currentContainer.push(new MultiplyOperation().addChild(lhs).addChild(rhs));
    }

    @Override
    public void exitAddOperation(ICSSParser.AddOperationContext ctx) {
        var rhs = currentContainer.pop();
        var lhs = currentContainer.pop();
        currentContainer.push(new AddOperation().addChild(lhs).addChild(rhs));
    }

    @Override
    public void exitSubtractOperation(ICSSParser.SubtractOperationContext ctx) {
        var rhs = currentContainer.pop();
        var lhs = currentContainer.pop();
        currentContainer.push(new SubtractOperation().addChild(lhs).addChild(rhs));
    }

    @Override
    public void exitColorLiteral(ICSSParser.ColorLiteralContext ctx) {
        currentContainer.push(new ColorLiteral(ctx.getText()));
    }

    @Override
    public void exitPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
        currentContainer.push(new PixelLiteral(ctx.getText()));
    }

    @Override
    public void exitPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
        currentContainer.push(new PercentageLiteral(ctx.getText()));
    }

    @Override
    public void exitScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
        currentContainer.push(new ScalarLiteral(ctx.getText()));
    }

    @Override
    public void exitBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
        currentContainer.push(new BoolLiteral(ctx.getText()));
    }

    @Override
    public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
        currentContainer.push(new VariableReference(ctx.getText()));
    }

}
