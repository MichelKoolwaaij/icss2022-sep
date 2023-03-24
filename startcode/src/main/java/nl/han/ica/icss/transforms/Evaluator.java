package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        for (ASTNode node : ast.root.body){
            if(node instanceof VariableAssignment){
                saveVariableType((VariableAssignment) node);
            }
            if(node instanceof Stylerule){
                evaluateBody(((Stylerule) node).body);
            }
        }
    }

    private void saveVariableType(VariableAssignment node) {
        if(node.expression instanceof Operation){
            node.expression = calculateOperation(node.expression);
        }
        if(variableValues.getSize()==0){
            variableValues.addFirst(new HashMap<>());
        }
        int index = 0;
        for(int i = 0; i<variableValues.getSize(); i++){
            if(variableValues.get(i).containsKey(node.name.name)){
                index = i;
            }
        }
        variableValues.get(index).put(node.name.name, (Literal) node.expression);
    }

    private void evaluateBody(ArrayList<ASTNode> body) {
        for (ASTNode node : body){
            if(node instanceof Declaration){
                if(((Declaration) node).expression instanceof VariableReference){
                    ((Declaration) node).expression = getVariable(((VariableReference) ((Declaration) node).expression).name);
                }
                else if(((Declaration) node).expression instanceof Operation){
                    ((Declaration) node).expression = calculateOperation(((Declaration) node).expression);
                }
            }
        }
    }

    private Expression calculateOperation(Expression expression) {
        if(expression instanceof Operation){
            if(((Operation) expression).lhs instanceof VariableReference){
                ((Operation) expression).lhs = getVariable(((VariableReference) ((Operation) expression).lhs).name);
            }
            if(((Operation) expression).rhs instanceof VariableReference){
                ((Operation) expression).rhs = getVariable(((VariableReference) ((Operation) expression).rhs).name);
            }
            if(((Operation) expression).lhs instanceof Operation){
                ((Operation) expression).lhs = calculateOperation(((Operation) expression).lhs);
            }
            if(((Operation) expression).rhs instanceof Operation){
                ((Operation) expression).rhs = calculateOperation(((Operation) expression).rhs);
            }
            //lhs and rhs are literals
            if(((Operation) expression).lhs instanceof Literal && ((Operation) expression).rhs instanceof Literal){
                if(expression instanceof AddOperation){
                    return addLiterals((Literal) ((AddOperation) expression).lhs, (Literal) ((AddOperation) expression).rhs);
                }
                if(expression instanceof SubtractOperation){
                    return subtractLiterals((Literal) ((SubtractOperation) expression).lhs, (Literal) ((SubtractOperation) expression).rhs);
                }
                if(expression instanceof MultiplyOperation){
                    return multiplyLiterals((Literal) ((MultiplyOperation) expression).lhs, (Literal) ((MultiplyOperation) expression).rhs);
                }
            }
            else return null;
        }
        return expression;
    }

    private Expression multiplyLiterals(Literal lhs, Literal rhs) {
        if(lhs instanceof PixelLiteral || rhs instanceof PixelLiteral){
            if(lhs instanceof PixelLiteral){
                return new PixelLiteral(((PixelLiteral) lhs).value * ((ScalarLiteral) rhs).value);
            }
            else return new PixelLiteral(((PixelLiteral) rhs).value * ((ScalarLiteral) lhs).value);
        }
        else if(lhs instanceof PercentageLiteral || rhs instanceof PercentageLiteral){
            if(lhs instanceof PercentageLiteral){
                return new PercentageLiteral(((PercentageLiteral) lhs).value * ((ScalarLiteral) rhs).value);
            }
            else return new PercentageLiteral(((PercentageLiteral) rhs).value * ((ScalarLiteral) lhs).value);
        }
        return null;
    }

    private Expression subtractLiterals(Literal lhs, Literal rhs) {
        if(lhs instanceof PixelLiteral && rhs instanceof PixelLiteral){
            return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value);
        }
        else if(lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral){
            return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
        }
        return null;
    }

    private Literal addLiterals(Literal lhs, Literal rhs){
        if(lhs instanceof PixelLiteral && rhs instanceof PixelLiteral){
            return new PixelLiteral(((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value);
        }
        else if(lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral){
            return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
        }
        return null;
    }

    private Literal getVariable(String variable) {
        for (HashMap<String, Literal> hashMap:
             variableValues) {
            if(hashMap.containsKey(variable)){
                return hashMap.get(variable);
            }
        }
        return null;
    }
}
