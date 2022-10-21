package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
        check(ast.root.body);
    }

    private void check(ArrayList<ASTNode> nodes) {
        for(ASTNode node: nodes) {
            if(node instanceof Stylerule){
                variableTypes.addFirst(new HashMap<>());
                check(((Stylerule) node).body);
                variableTypes.removeFirst();
            }
            if(node instanceof VariableAssignment){
                if(((VariableAssignment) node).expression instanceof BoolLiteral){
                    variableTypes.getFirst().put(((VariableAssignment) node).name.name, ExpressionType.BOOL);
                }
                else if(((VariableAssignment) node).expression instanceof ColorLiteral){
                    variableTypes.getFirst().put(((VariableAssignment) node).name.name, ExpressionType.COLOR);
                }
                else if(((VariableAssignment) node).expression instanceof PercentageLiteral){
                    variableTypes.getFirst().put(((VariableAssignment) node).name.name, ExpressionType.PERCENTAGE);
                }
                else if(((VariableAssignment) node).expression instanceof PixelLiteral){
                    variableTypes.getFirst().put(((VariableAssignment) node).name.name, ExpressionType.PIXEL);
                }
                else if(((VariableAssignment) node).expression instanceof ScalarLiteral){
                    variableTypes.getFirst().put(((VariableAssignment) node).name.name, ExpressionType.SCALAR);
                }
                else variableTypes.getFirst().put(((VariableAssignment) node).name.name, ExpressionType.UNDEFINED);
            }
            if(node instanceof Declaration){
                checkDeclaration((Declaration) node);
            }
            if(node instanceof IfClause){
                if(((IfClause) node).conditionalExpression instanceof VariableReference){
                    if((checkInitialisedVariable((VariableReference) ((IfClause) node).conditionalExpression))){
                        if(!Objects.equals(findInitialisedVariable((VariableReference) ((IfClause) node).conditionalExpression), ExpressionType.BOOL)){
                            node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not a boolean");
                        }
                    }
                    else node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not initialised");
                }
                else if(!(((IfClause) node).conditionalExpression instanceof BoolLiteral)){
                    node.setError((node).getNodeLabel() + ((IfClause) node).conditionalExpression.toString() + " is not a boolean expression");
                }
                variableTypes.addFirst(new HashMap<>());
                check(((IfClause) node).body);
                variableTypes.removeFirst();
            }
        }
    }

    private void checkDeclaration(Declaration node) {
        if(node.expression instanceof VariableReference){
            if(!checkInitialisedVariable(((VariableReference) node.expression))){
                node.setError("variable " + ((VariableReference) node.expression).name + " is not initialised");
            }
        }
        //CH01
        if (node.expression instanceof Operation){
            //operation within lhs
            /*if(((Operation) node.expression).lhs instanceof Operation){
                //checkDeclaration(((Operation) node.expression).lhs, variableTypes);
            }*/
            //CH03
            if(((Operation) node.expression).lhs instanceof ColorLiteral || ((Operation) node.expression).rhs instanceof ColorLiteral){
                node.setError("Operations don't work with colors");
            }
            //CH02
            else if(node.expression instanceof MultiplyOperation){
                checkMultiply(node.expression);
            }
            else if(node.expression instanceof AddOperation){
                checkAdd(node.expression);
            }
            else if(node.expression instanceof SubtractOperation){
                checkSubtract(node.expression);
            }
        }
    }

    private void checkSubtract(ASTNode node) {

    }

    private boolean checkInitialisedVariable(VariableReference node) {
        for (HashMap<String, ExpressionType> hashMap:
             variableTypes) {
            if(hashMap!=null && hashMap.containsKey(node.name)){
                return true;
            }
        }
        return false;
    }

    private ExpressionType findInitialisedVariable(VariableReference node){
        for (HashMap<String, ExpressionType> hashMap:
                variableTypes) {
            if(hashMap!=null && hashMap.containsKey(node.name)){
                return hashMap.get(node.name);
            }
        }
        return null;
    }

    private void checkMultiply(ASTNode node) {
        if(node instanceof MultiplyOperation){
            //Both variables
            if(((MultiplyOperation) node).lhs instanceof VariableReference && ((MultiplyOperation) node).rhs instanceof VariableReference){
                if(checkInitialisedVariable((VariableReference) ((MultiplyOperation) node).lhs) && checkInitialisedVariable((VariableReference) ((MultiplyOperation) node).rhs)){
                    ExpressionType lhsType = findInitialisedVariable((VariableReference) ((MultiplyOperation) node).lhs);
                    ExpressionType rhsType = findInitialisedVariable((VariableReference) ((MultiplyOperation) node).rhs);
                    if(Objects.equals(lhsType, ExpressionType.SCALAR) || Objects.equals(rhsType, ExpressionType.SCALAR)){
                        if(Objects.equals(lhsType, ExpressionType.BOOL) || Objects.equals(rhsType, ExpressionType.BOOL)){
                            node.setError("booleans are not allowed in products");
                        }
                    }
                    else node.setError("Products need at least one scalar value");
                }
                else node.setError("One of the variables " + node + " is not initialised");
            }
            //lhs is variable
            else if(((MultiplyOperation) node).lhs instanceof VariableReference){
                if(checkInitialisedVariable((VariableReference) ((MultiplyOperation) node).lhs)){
                    ExpressionType type = findInitialisedVariable((VariableReference) ((MultiplyOperation) node).lhs);
                    if(!Objects.equals(type, ExpressionType.SCALAR) && !(((MultiplyOperation) node).rhs instanceof ScalarLiteral)){
                        node.setError("Products need at least one scalar value");
                    }
                }
                else node.setError("variable " + ((VariableReference) ((MultiplyOperation) node).lhs).name + " is not initialised");
            }
            //rhs is variable
            else if(((MultiplyOperation) node).rhs instanceof VariableReference){
                if(checkInitialisedVariable((VariableReference) ((MultiplyOperation) node).rhs)){
                    ExpressionType type = findInitialisedVariable((VariableReference) ((MultiplyOperation) node).rhs);
                    if(!Objects.equals(type, ExpressionType.SCALAR) && !(((MultiplyOperation) node).lhs instanceof ScalarLiteral)){
                        node.setError("Products need at least one scalar value");
                    }
                }
                else node.setError("variable " + ((VariableReference) ((MultiplyOperation) node).rhs).name + " is not initialised");
            }
            //if node is not a variable
            else if(!((((MultiplyOperation) node).lhs instanceof ScalarLiteral) || ((MultiplyOperation) node).rhs instanceof ScalarLiteral)){
                node.setError("Products need at least one scalar value");
            }
        }
    }

    private void checkAdd(ASTNode node) {
        if(node instanceof AddOperation){
            //lhs and rhs are variables
            if(((AddOperation) node).lhs instanceof VariableReference && ((AddOperation) node).rhs instanceof VariableReference) {
                if(checkInitialisedVariable((VariableReference) ((AddOperation) node).lhs) && checkInitialisedVariable((VariableReference) ((AddOperation) node).rhs)){
                    if(Objects.equals(findInitialisedVariable((VariableReference) ((AddOperation) node).lhs), ExpressionType.COLOR) || Objects.equals(findInitialisedVariable((VariableReference) ((AddOperation) node).rhs), ExpressionType.COLOR)){
                        node.setError("Operations don't work with colors");
                    }
                    else if(!Objects.equals(findInitialisedVariable((VariableReference) ((AddOperation) node).lhs), findInitialisedVariable((VariableReference) ((AddOperation) node).rhs))){
                        node.setError("Variables are not from the same type");
                    }
                }
                else node.setError("One of the variables " + node + " is not initialised");
            }
            //lhs is variable
            else if(((AddOperation) node).lhs instanceof VariableReference){
                if(checkInitialisedVariable((VariableReference) ((AddOperation) node).lhs)){
                    ExpressionType type = findInitialisedVariable((VariableReference) ((AddOperation) node).lhs);
                    if(Objects.equals(type, ExpressionType.COLOR) || ((AddOperation) node).rhs.getClass().equals(ColorLiteral.class)){
                        node.setError("Operations don't work with colors");
                    }
                    else if(matchingVariabletypeAndLiteral(Objects.requireNonNull(type), (Literal) ((AddOperation) node).rhs)){
                        node.setError("Cannot add different types");
                    }
                }
                else node.setError("variable " + ((VariableReference) ((AddOperation) node).lhs).name + " is not initialised");
            }
            //rhs is variable
            else if(((AddOperation) node).rhs instanceof VariableReference){
                if(checkInitialisedVariable((VariableReference) ((AddOperation) node).rhs)){
                    ExpressionType type = findInitialisedVariable((VariableReference) ((AddOperation) node).rhs);
                    if(Objects.equals(type, ExpressionType.COLOR) || ((AddOperation) node).lhs.getClass().equals(ColorLiteral.class)){
                        node.setError("Operations don't work with colors");
                    }
                    else if(matchingVariabletypeAndLiteral(Objects.requireNonNull(type), (Literal) ((AddOperation) node).lhs)){
                        node.setError("Cannot add different types");
                    }
                }
                else node.setError("variable " + ((VariableReference) ((AddOperation) node).rhs).name + " is not initialised");
            }
            //if rhs and lhs are not variables
            else if(!((AddOperation) node).lhs.getClass().equals(((AddOperation) node).rhs.getClass())){
                    node.setError("Cannot add different types");
            }
        }
    }
    private boolean matchingVariabletypeAndLiteral(ExpressionType type, Literal node){
        if(type.equals(ExpressionType.BOOL) && node instanceof BoolLiteral){
            return false;
        }
        if(type.equals(ExpressionType.COLOR) && node instanceof ColorLiteral){
            return false;
        }
        if(type.equals(ExpressionType.PERCENTAGE) && node instanceof PercentageLiteral){
            return false;
        }
        if(type.equals(ExpressionType.PIXEL) && node instanceof PixelLiteral){
            return false;
        }
        return !type.equals(ExpressionType.SCALAR) || !(node instanceof ScalarLiteral);
    }
}
