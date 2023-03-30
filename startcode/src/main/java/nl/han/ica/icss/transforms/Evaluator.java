package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        globalVariables(ast);
        for (ASTNode node : ast.root.body){
            if(node instanceof Stylerule){
                node = evaluateIfClause((Stylerule) node, 0);
                evaluateBody(((Stylerule) node).body);
            }
        }
        variableValues.removeFirst();
    }

    private void globalVariables(AST ast) {
        variableValues.addFirst(new HashMap<>());
        ast.root.body.stream().filter(node -> node instanceof VariableAssignment).forEach(node -> saveVariableType((VariableAssignment) node));
        ast.root.body.removeAll(ast.root.body.stream().filter(node -> node instanceof VariableAssignment).collect(Collectors.toList()));
    }

    private Stylerule evaluateIfClause(Stylerule stylerule, int index) {
        if (stylerule.body.size() <= index){
            return stylerule;
        }
        if(stylerule.body.get(index) instanceof Declaration){
            index++;
            return evaluateIfClause(stylerule,index);
        }
        if (stylerule.body.get(index) instanceof VariableAssignment){
            variableValues.addFirst(new HashMap<>());
            saveVariableType((VariableAssignment) stylerule.body.get(index));
            //variableValues.getFirst().put(((VariableAssignment) stylerule.body.get(index)).name,((VariableAssignment) stylerule.body.get(index)).expression);
        }
        if (stylerule.body.get(index) instanceof IfClause){
            IfClause ifClause = (IfClause) stylerule.body.get(index);
            if(ifClause.conditionalExpression instanceof VariableReference){
                BoolLiteral clause = (BoolLiteral) getVariable(((VariableReference) ifClause.conditionalExpression).name);
                assert clause != null;
                rebuildIfClause(stylerule, index, ifClause, clause);
            }
            if(ifClause.conditionalExpression instanceof BoolLiteral){
                rebuildIfClause(stylerule, index, ifClause, ((BoolLiteral) ifClause.conditionalExpression));
            }
            index++;
            return evaluateIfClause(stylerule,index);
        }
        variableValues.removeFirst();
        return stylerule;
    }

    private void rebuildIfClause(Stylerule stylerule, int index, IfClause ifClause, BoolLiteral clause) {
        if(clause.value){
            stylerule.body.remove(ifClause);
            stylerule.body.addAll(index, ifClause.body);
        }
        if(!clause.value && ifClause.elseClause!=null){
            stylerule.body.remove(ifClause);
            stylerule.body.addAll(index, ifClause.elseClause.body);
        }
        else stylerule.body.remove(ifClause);
    }

    private void saveVariableType(VariableAssignment node) {
        if(node.expression instanceof Operation){
            node.expression = calculateOperation(node.expression);
        }
        int index = 0;
        for(int i = 0; i<variableValues.getSize(); i++){
            if(variableValues.get(i).containsKey(node.name.name)){
                index = i;
            }
        }
        variableValues.get(index).put(node.name.name, (Literal) node.expression);
    }

    private void evaluateBody(ArrayList<ASTNode> nodes) {
        variableValues.addFirst(new HashMap<>());
        for (ASTNode node : nodes){
            if(node instanceof Declaration){
                if(((Declaration) node).expression instanceof VariableReference){
                    ((Declaration) node).expression = getVariable(((VariableReference) ((Declaration) node).expression).name);
                }
                else if(((Declaration) node).expression instanceof Operation){
                    ((Declaration) node).expression = calculateOperation(((Declaration) node).expression);
                }
            }
            else if(node instanceof VariableAssignment){
                saveVariableType((VariableAssignment) node);
            }
        }
        variableValues.removeFirst();
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
