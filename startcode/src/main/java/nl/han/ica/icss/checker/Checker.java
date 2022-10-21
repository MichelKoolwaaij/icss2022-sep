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
        //int scope = 0;
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
                //checkInitialisedVariable(((Declaration) node);
                checkDeclaration((Declaration) node);
            }
            if(node instanceof IfClause){
                if(((IfClause) node).conditionalExpression instanceof VariableReference){
                    if((checkInitialisedVariable((VariableReference) ((IfClause) node).conditionalExpression))){

                    }
                    else node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not initialised");
                }
                else if(!(((IfClause) node).conditionalExpression instanceof BoolLiteral)){
                    node.setError("not a boolean expression");
                }
                variableTypes.addFirst(new HashMap<>());
                check(((IfClause) node).body);
                variableTypes.removeFirst();
            }
        }
    }

    private void checkDeclaration(Declaration node) {
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
                checkMultiply(node);
            }
            if(node.expression instanceof AddOperation | node.expression instanceof SubtractOperation){

            }
            checkAdd(node);
            checkSubtract(node);
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

    private void checkMultiply(ASTNode node) {
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

    private void checkAdd(ASTNode node) {
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
