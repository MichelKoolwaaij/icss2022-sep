package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        //var variables = new ArrayList<VariableAssignment>();
        evaluate(ast);
    }

    private void evaluate(AST ast) {
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
                for(ASTNode declaration : node.getChildren()){
                    if(declaration instanceof Operation){
                        if(((Operation) declaration).rhs instanceof MultiplyOperation){

                        }
                    }
                    if(declaration instanceof MultiplyOperation){
                    var lhs = ((MultiplyOperation) declaration).lhs.getChildren();
                    var rhs = ((MultiplyOperation) declaration).rhs.getChildren();
                    }
                    if(declaration instanceof AddOperation){
                        System.out.println("add");
                    }
                    if(declaration instanceof SubtractOperation){
                        System.out.println("subtract");
                    }
                }
            }
        }
    }


}
