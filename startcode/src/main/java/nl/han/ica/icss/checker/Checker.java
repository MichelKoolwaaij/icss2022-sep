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
        for (ASTNode node : nodes) {
            if (node instanceof Stylerule) {
                variableTypes.addFirst(new HashMap<>());
                check(((Stylerule) node).body);
                variableTypes.removeFirst();
            }
            if (node instanceof VariableAssignment) {
                setVariableTypes((VariableAssignment) node);
            }
            if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            }
            if (node instanceof IfClause) {
                checkIfClause(node);
            }
        }
    }

    private void checkIfClause(ASTNode node) {
        if (((IfClause) node).conditionalExpression instanceof VariableReference) {
            if ((getInitialisedVariableType((VariableReference) ((IfClause) node).conditionalExpression)!=null)) {
                //CH05 variable reference
                if (!Objects.equals(getInitialisedVariableType((VariableReference) ((IfClause) node).conditionalExpression), ExpressionType.BOOL)) {
                    node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not a boolean");
                }
            }
            else
                node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not initialised");
        }
        //CH05 boolean literal
        else if (!(((IfClause) node).conditionalExpression instanceof BoolLiteral)) {
            node.setError(node.getNodeLabel() + ((IfClause) node).conditionalExpression.toString() + " is not a boolean expression");
        }
        variableTypes.addFirst(new HashMap<>());
        check(((IfClause) node).body);
        variableTypes.removeFirst();
        if (((IfClause) node).elseClause != null) {
            variableTypes.addFirst(new HashMap<>());
            check(((IfClause) node).elseClause.body);
            variableTypes.removeFirst();
        }
    }

    private void setVariableTypes(VariableAssignment node) {
        if (node.expression instanceof BoolLiteral) {
            variableTypes.getFirst().put(node.name.name, ExpressionType.BOOL);
        } else if (node.expression instanceof ColorLiteral) {
            variableTypes.getFirst().put(node.name.name, ExpressionType.COLOR);
        } else if (node.expression instanceof PercentageLiteral) {
            variableTypes.getFirst().put(node.name.name, ExpressionType.PERCENTAGE);
        } else if (node.expression instanceof PixelLiteral) {
            variableTypes.getFirst().put(node.name.name, ExpressionType.PIXEL);
        } else if (node.expression instanceof ScalarLiteral) {
            variableTypes.getFirst().put(node.name.name, ExpressionType.SCALAR);
        } else variableTypes.getFirst().put(node.name.name, ExpressionType.UNDEFINED);
    }

    private void checkDeclaration(Declaration node) {
        if (node.expression instanceof VariableReference) {
            if (getInitialisedVariableType((VariableReference) node.expression)==null) {
                node.setError("variable " + ((VariableReference) node.expression).name + " is not initialised");
            }
        }
        //CH01
        if (node.expression instanceof Operation) {
            checkOperation((Operation) node.expression, new HANLinkedList<ExpressionType>());
        }
        if(node.expression instanceof ScalarLiteral){
            node.setError("Property cannot be scalar");
        }
    }

    private ExpressionType checkOperation(Operation node, HANLinkedList<ExpressionType> expressionTypes) {
        //CH03 for non variables
        if (node.lhs instanceof ColorLiteral || node.rhs instanceof ColorLiteral) {
            node.setError("Operations don't work with colors");
        }
        //CH02
        else if (node instanceof MultiplyOperation) {
            return checkMultiply((MultiplyOperation) node,expressionTypes);
        } else if (node instanceof AddOperation) {
            return checkAdd((AddOperation) node, expressionTypes);
        } else if (node instanceof SubtractOperation) {
            IHANLinkedList<HashMap<String, ExpressionType>> literals = new HANLinkedList<>();
            literals.addFirst(new HashMap<>());
            //checkSubtract(node.property, (SubtractOperation) node.expression, literals);
        }
        return ExpressionType.UNDEFINED;
    }

    public ExpressionType getExpressionType(Literal literal){
        if(literal instanceof BoolLiteral){
            return ExpressionType.BOOL;
        }
        if (literal instanceof ScalarLiteral){
            return ExpressionType.SCALAR;
        }
        if (literal instanceof ColorLiteral){
            return ExpressionType.COLOR;
        }
        if (literal instanceof PercentageLiteral){
            return ExpressionType.PERCENTAGE;
        }
        if (literal instanceof PixelLiteral){
            return ExpressionType.PIXEL;
        }
        else return ExpressionType.UNDEFINED;
    }

    private void checkSubtract(SubtractOperation node) {
        if(node.lhs instanceof Literal || node.rhs instanceof Literal){

        }
        else if(node.lhs instanceof VariableReference || node.rhs instanceof VariableReference){
            if(node.lhs instanceof VariableReference && node.rhs instanceof VariableReference){

            }
            else if(node.lhs instanceof VariableReference && getInitialisedVariableType((VariableReference) node.lhs)!=null){
                //checkVariableReferenceInitialization((VariableReference) node.lhs);
            }
            else {
                //checkVariableReferenceInitialization((VariableReference) node.rhs);
            }
        }
        else if(node.lhs instanceof Operation){

        }
        //one or two literals

        //one or two variables

        //one or two operation: 2px*4-7%*2
    }

    private ExpressionType getInitialisedVariableType(VariableReference node) {
        for (HashMap<String, ExpressionType> hashMap :
                variableTypes) {
            if (hashMap != null && hashMap.containsKey(node.name)) {
                return hashMap.get(node.name);
            }
        }
        return null;
    }

    private ExpressionType checkMultiply(MultiplyOperation node, HANLinkedList<ExpressionType> expressionTypes) {
        //operations
        if(node.lhs instanceof Operation || node.rhs instanceof Operation){
            if (node.lhs instanceof Operation){
                expressionTypes.addFirst(checkOperation((Operation) node.lhs, expressionTypes));
            }
            else{
                expressionTypes.addFirst(checkOperation((Operation) node.rhs, expressionTypes));
            }
            return expressionTypes.getFirst();
        }
        //variables
        if(node.lhs instanceof VariableReference || node.rhs instanceof VariableReference){
            //both variables
            if (node.lhs instanceof VariableReference && node.rhs instanceof VariableReference) {
                ExpressionType lhsType = getInitialisedVariableType((VariableReference) node.lhs);
                if (lhsType ==null){
                    node.lhs.setError("variable " + ((VariableReference) node.lhs).name + " is not initialized");
                    return ExpressionType.UNDEFINED;
                }
                ExpressionType rhsType = getInitialisedVariableType((VariableReference) node.rhs);
                if (rhsType ==null){
                    node.rhs.setError("variable " + ((VariableReference) node.rhs).name + " is not initialized");
                    return ExpressionType.UNDEFINED;
                }
                return checkMultiplicationTypes(node, lhsType, rhsType);
            }
            //lhs is variable
            else if(node.lhs instanceof VariableReference){
                ExpressionType lhsType = getInitialisedVariableType((VariableReference) node.lhs);
                if (lhsType ==null){
                    node.lhs.setError("variable " + ((VariableReference) node.lhs).name + " is not initialized");
                    return ExpressionType.UNDEFINED;
                }
                return checkMultiplicationTypes(node,lhsType,getExpressionType((Literal) node.rhs));
            }
            //rhs is variable
            else {
                ExpressionType rhsType = getInitialisedVariableType((VariableReference) node.rhs);
                if (rhsType ==null){
                    node.rhs.setError("variable " + ((VariableReference) node.rhs).name + " is not initialized");
                    return ExpressionType.UNDEFINED;
                }
                return checkMultiplicationTypes(node,getExpressionType((Literal) node.lhs),rhsType);
            }
        }
        //literals
        assert node.lhs instanceof Literal;
        return checkMultiplicationTypes(node, getExpressionType((Literal) node.lhs), getExpressionType((Literal) node.rhs));
//        //lhs is variable
//        else if (node.lhs instanceof VariableReference) {
//            if (getInitialisedVariableType((VariableReference) node.lhs)!=null) {
//                ExpressionType type = getInitialisedVariableType((VariableReference) node.lhs);
//                if (Objects.equals(type, ExpressionType.COLOR)) {
//                    node.setError("Operations don't work with colors");
//                } else if (!Objects.equals(type, ExpressionType.SCALAR) && !(node.rhs instanceof ScalarLiteral)) {
//                    node.setError("Products need at least one scalar value");
//                }
//            } else node.setError("variable " + ((VariableReference) node.lhs).name + " is not initialised");
//        }
//        //rhs is variable
//        else if (node.rhs instanceof VariableReference) {
//            if (getInitialisedVariableType((VariableReference) node.rhs)!=null) {
//                ExpressionType type = getInitialisedVariableType((VariableReference) node.rhs);
//                if (Objects.equals(type, ExpressionType.COLOR)) {
//                    node.setError("Operations don't work with colors");
//                } else if (!Objects.equals(type, ExpressionType.SCALAR) && !(node.lhs instanceof ScalarLiteral)) {
//                    node.setError("Products need at least one scalar value");
//                }
//            } else node.setError("variable " + ((VariableReference) node.rhs).name + " is not initialised");
//        }
//        //if node is not a variable
//        else if (!((node.lhs instanceof ScalarLiteral) || node.rhs instanceof ScalarLiteral)) {
//            node.setError("Products need at least one scalar value");
//        }
//        //if lhs and rhs are both scalar
//        else if (node.lhs instanceof ScalarLiteral && node.rhs instanceof ScalarLiteral){
//            node.setError("Property cannot be scalar");
//        }
//        else if(node.lhs instanceof Operation){
//            checkOperation((Operation) node.lhs);
//        }
//        else if(node.rhs instanceof Operation){
//            checkOperation((Operation) node.rhs);
//        }
    }

    private static ExpressionType checkMultiplicationTypes(MultiplyOperation node, ExpressionType lhsType, ExpressionType rhsType) {
        if(lhsType.equals(ExpressionType.COLOR) || rhsType.equals(ExpressionType.COLOR)){
            node.setError("no colors in products");
            return ExpressionType.UNDEFINED;
        }
        if(lhsType.equals(ExpressionType.BOOL) || rhsType.equals(ExpressionType.BOOL)){
            node.setError("no booleans in products");
            return ExpressionType.UNDEFINED;
        }
        if(lhsType.equals(ExpressionType.SCALAR) || rhsType.equals(ExpressionType.SCALAR)){
            if(lhsType.equals(ExpressionType.SCALAR) && rhsType.equals(ExpressionType.SCALAR)){
                node.setError("Products must have one unit");
                return ExpressionType.UNDEFINED;
            }
            if(lhsType.equals(ExpressionType.SCALAR)){
                return rhsType;
            }
            else return lhsType;
        }
        else {
            node.setError("Products need at least one scalar value");
            return ExpressionType.UNDEFINED;
        }
    }

    private ExpressionType checkAdd(AddOperation node, HANLinkedList<ExpressionType> expressionTypes) {
        //operation consists of one or two operations
        if (node.lhs instanceof Operation || node.rhs instanceof Operation) {
            getInnerOperationType(node,expressionTypes);
        }
        //lhs and rhs are variables
        if (node.lhs instanceof VariableReference && node.rhs instanceof VariableReference) {
            if (getInitialisedVariableType((VariableReference) node.lhs)!=null && getInitialisedVariableType((VariableReference) node.rhs)!=null) {
                if (Objects.equals(getInitialisedVariableType((VariableReference) node.lhs), ExpressionType.COLOR) || Objects.equals(getInitialisedVariableType((VariableReference) node.rhs), ExpressionType.COLOR)) {
                    node.setError("Operations don't work with colors");
                } else if (!Objects.equals(getInitialisedVariableType((VariableReference) node.lhs), getInitialisedVariableType((VariableReference) node.rhs))) {
                    node.setError("Variables are not from the same type");
                }
            } else node.setError("One of the variables " + node + " is not initialised");
        }
        //lhs is variable
        else if (node.lhs instanceof VariableReference) {
                ExpressionType type = getInitialisedVariableType((VariableReference) node.lhs);
                if(type==null){
                    node.setError("variable " + ((VariableReference) node.lhs).name + " is not initialised");
                }
                else if (Objects.equals(type, ExpressionType.COLOR) || node.rhs.getClass().equals(ColorLiteral.class)) {
                    node.setError("Operations don't work with colors");
                }
                else if(node.rhs instanceof Operation && !type.equals(expressionTypes.getFirst())){
                    node.setError("Other part of operation is not from the same type");
                }
                else if(node.rhs instanceof Literal && !type.equals(getExpressionType((Literal) node.rhs))){
                    node.setError("Other literal is not from the same type");
                }
        }
        //rhs is variable
        else if (node.rhs instanceof VariableReference) {
            if (getInitialisedVariableType((VariableReference) node.rhs)!=null) {
                ExpressionType type = getInitialisedVariableType((VariableReference) node.rhs);
                if (Objects.equals(type, ExpressionType.COLOR) || node.lhs.getClass().equals(ColorLiteral.class)) {
                    node.setError("Operations don't work with colors");
                }
            } else node.setError("variable " + ((VariableReference) node.rhs).name + " is not initialised");
        }
        //lhs and/or rhs are literals
        else if(node.lhs instanceof Literal || node.rhs instanceof Literal){
            if(node.lhs instanceof Literal && node.rhs instanceof Literal){
                if(!getExpressionType((Literal) node.lhs).equals(getExpressionType((Literal) node.rhs))){
                    node.setError("Literals must be of the same type");
                }
            }
            else if(node.lhs instanceof Literal){
                if(!getExpressionType((Literal) node.lhs).equals(expressionTypes.getFirst())){
                    node.setError("Literals must be of the same type");
                }
            }
            else {
                if(!getExpressionType((Literal) node.rhs).equals(expressionTypes.getFirst())){
                    node.setError("Literals must be of the same type");
                }
            }
        }
        return null;
    }

    private void getInnerOperationType(AddOperation node, HANLinkedList<ExpressionType> expressionTypes) {
        if(node.lhs instanceof Operation || node.rhs instanceof Operation){
            if(node.lhs instanceof Operation && node.rhs instanceof Operation){
                expressionTypes.addFirst(checkOperation((Operation) node.lhs, expressionTypes));
                expressionTypes.addFirst(checkOperation((Operation) node.rhs, expressionTypes));
            }
            if (node.lhs instanceof Operation){
                expressionTypes.addFirst(checkOperation((Operation) node.lhs, expressionTypes));
            }
            else{
                expressionTypes.addFirst(checkOperation((Operation) node.rhs, expressionTypes));
            }
        }
    }
}
