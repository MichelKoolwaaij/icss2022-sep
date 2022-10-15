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


public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
        int scope = 0;
        check(ast.root.body, variableTypes, scope);
    }

    private void check(ArrayList<ASTNode> nodes, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes, int scope) {
        for(ASTNode node: nodes) {
            if(node instanceof Stylerule){
                scope++;
                variableTypes.addFirst(new HashMap<>());
                check(((Stylerule) node).body, variableTypes, scope);
                scope--;
            }
            if(node instanceof VariableAssignment){
                if(((VariableAssignment) node).expression instanceof BoolLiteral){
                    variableTypes.get(scope).put(((VariableAssignment) node).name.name, ExpressionType.BOOL);
                }
                else if(((VariableAssignment) node).expression instanceof ColorLiteral){
                    variableTypes.get(scope).put(((VariableAssignment) node).name.name, ExpressionType.COLOR);
                }
                else if(((VariableAssignment) node).expression instanceof PercentageLiteral){
                    variableTypes.get(scope).put(((VariableAssignment) node).name.name, ExpressionType.PERCENTAGE);
                }
                else if(((VariableAssignment) node).expression instanceof PixelLiteral){
                    variableTypes.get(scope).put(((VariableAssignment) node).name.name, ExpressionType.PIXEL);
                }
                else if(((VariableAssignment) node).expression instanceof ScalarLiteral){
                    variableTypes.get(scope).put(((VariableAssignment) node).name.name, ExpressionType.SCALAR);
                }
                else variableTypes.get(scope).put(((VariableAssignment) node).name.name, ExpressionType.UNDEFINED);
            }
            if(node instanceof Declaration){
                checkInitialisedVariables((Declaration) node, variableTypes);
                checkDeclaration((Declaration) node, variableTypes);
            }
            if(node instanceof IfClause){
                if(!variableTypes.iterator().next().containsKey(((VariableReference) ((IfClause) node).conditionalExpression).name)){
                    node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not initialised");
                }
                /*if(variables.stream().noneMatch(variableAssignment -> variableAssignment.name.equals(((IfClause) node).conditionalExpression))){
                    node.setError(((IfClause) node).conditionalExpression + " in if clause");
                }*/
                scope++;
                check(((IfClause) node).body, variableTypes, scope);
                scope--;
            }
        }
    }

    private void checkDeclaration(Declaration node, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        //CH01
        if (node.expression instanceof Operation){
            //operation within lhs
            /*if(((Operation) node.expression).lhs instanceof Operation){

                //checkDeclaration(((Operation) node.expression).lhs, variableTypes);
            }*/
            //CH03
            if(((Operation) node.expression).lhs instanceof ColorLiteral | ((Operation) node.expression).rhs instanceof ColorLiteral){
                node.setError("Operations don't work with colors");
            }
            //CH02
            if(node.expression instanceof MultiplyOperation){
                checkMultiply(node, variableTypes);
            }
            if(node.expression instanceof AddOperation | node.expression instanceof SubtractOperation){

            }
            checkAdd(node, variableTypes);
            checkSubtract(node, variableTypes);
        }
    }

    private void checkSubtract(ASTNode node, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
    }

    private void checkInitialisedVariables(Declaration node, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        if (node.expression instanceof VariableReference){
            if(!variableTypes.iterator().next().containsKey(((VariableReference) node.expression).name)){
                node.setError("variable " + ((VariableReference) node.expression).name + " is not initialised");
            }
        }
    }

    private void checkMultiply(ASTNode node, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        if(node instanceof MultiplyOperation){
            //check variable type
            if(((MultiplyOperation) node).lhs instanceof VariableReference){
                //check if variable type is not scalar

                /*if(variables.stream().anyMatch(variableAssignment ->
                    ((VariableReference) ((MultiplyOperation) node).lhs).name.equals(variableAssignment.name.name)
                    && !(variableAssignment.expression instanceof ScalarLiteral))){
                        if(!(((MultiplyOperation) node).rhs instanceof ScalarLiteral)){
                            node.setError("Product need at least one scalar value");
                        }
                }*/
            }
            //if node is not a variable
            else if(!((((MultiplyOperation) node).lhs instanceof ScalarLiteral) | ((MultiplyOperation) node).rhs instanceof ScalarLiteral)){
                node.setError("Products need at least one scalar value");
            }
        }
    }

    private void checkAdd(ASTNode node, IHANLinkedList<HashMap<String, ExpressionType>> variableTypes) {
        if(node instanceof AddOperation){

            if(((AddOperation) node).lhs instanceof VariableReference) {
                //var lhsType = checkVariableReference(((VariableReference) ((AddOperation) node).lhs), variables);
            }

                if(((AddOperation) node).lhs instanceof VariableReference && ((AddOperation) node).rhs instanceof VariableReference){
                    //var rhsType = checkVariableReference(((VariableReference) ((AddOperation) node).lhs), variables);
                    /*if(!lhsType.expression.getClass().equals(rhsType.expression.getClass())){
                        node.setError("");
                    }*/
                }
                //check if variable type is equal to rhs type
                if(((AddOperation) node).lhs instanceof VariableReference && !(((AddOperation) node).rhs instanceof VariableReference)){

                }
                //if rhs and lhs are not variables
                else if(!((AddOperation) node).rhs.getClass().equals(((AddOperation) node).lhs.getClass())){
                    node.setError("Operation is not permitted");
                }
            }

    }
}
