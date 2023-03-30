package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {

    public String generate(AST ast) {
        StringBuilder output = new StringBuilder();
        ast.root.body.forEach(astNode -> {
            if (astNode instanceof Stylerule) {
                ((Stylerule) astNode).selectors.forEach(selector -> output.append(selector.toString()));
                output.append(" {\n");
                ((Stylerule) astNode).body.forEach(declaration -> {
                    output.append("  ");
                    addDeclaration(output, (Declaration) declaration);
                    output.append(";\n");
                });
                output.append("}\n");
            }
        });
        return output.toString();
    }

    private void addDeclaration(StringBuilder output, Declaration declaration) {
        output.append(declaration.property.name);
        output.append(": ");
        if (declaration.expression instanceof Literal) {
            if (declaration.expression instanceof ColorLiteral) {
                output.append(((ColorLiteral) declaration.expression).value);
            }
            if (declaration.expression instanceof PixelLiteral) {
                output.append(((PixelLiteral) declaration.expression).value).append("px");
            }
            if (declaration.expression instanceof PercentageLiteral) {
                output.append(((PercentageLiteral) declaration.expression).value).append("%");
            }
        }
    }
}
