package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        //var bodyVariables = getBodyVariables(ast.root);

        check(ast.root.body, new ArrayList<>());
    }

    private void check(ArrayList<ASTNode> nodes, ArrayList<VariableAssignment> variables) {
        for(ASTNode node: nodes) {
            if(node instanceof Stylerule){
                int length = variables.size();
                check(((Stylerule) node).body, variables);
                removeVariablesOutOfScope(variables, length);
            }
            if(node instanceof VariableAssignment){
                variables.add((VariableAssignment) node);
            }
            if(node instanceof Declaration){
                checkProperty(((Declaration) node).expression, variables);
            }
            if(node instanceof IfClause){
                int length = variables.size();
                if(variables.stream().noneMatch(variableAssignment -> variableAssignment.name.equals(((IfClause) node).conditionalExpression))){
                    node.setError(((IfClause) node).conditionalExpression + " in if clause");
                }
                check(((IfClause) node).body, variables);
                removeVariablesOutOfScope(variables, length);
            }
        }
    }

    private void removeVariablesOutOfScope(ArrayList<VariableAssignment> variables, int length) {
        for (int i = length; i<variables.size(); i++){
            variables.remove(i);
        }
    }

    private void checkProperty(ASTNode node, ArrayList<VariableAssignment> variables) {
        if (node instanceof VariableReference){
            if(variables.stream().noneMatch(variableAssignment -> variableAssignment.name.equals(node))){
                node.setError("Variable " + ((VariableReference) node).name + " is not initialised");
            }
        }
    }


}
