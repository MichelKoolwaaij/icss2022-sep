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
            if ((findInitialisedVariable((VariableReference) ((IfClause) node).conditionalExpression)==null)) {
                //CH05 variable reference
                if (!Objects.equals(findInitialisedVariable((VariableReference) ((IfClause) node).conditionalExpression), ExpressionType.BOOL)) {
                    node.setError("variable " + ((VariableReference) ((IfClause) node).conditionalExpression).name + " is not a boolean");
                }
            } else
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
            if (findInitialisedVariable((VariableReference) node.expression)==null) {
                node.setError("variable " + ((VariableReference) node.expression).name + " is not initialised");
            }
        }
        //CH01
        if (node.expression instanceof Operation) {
            checkOperation(node);
        }
        if(node.expression instanceof ScalarLiteral){
            node.setError("Property cannot be scalar");
        }
    }

    private void checkOperation(Declaration node) {
        //CH03 for non variables
        if (((Operation) node.expression).lhs instanceof ColorLiteral || ((Operation) node.expression).rhs instanceof ColorLiteral) {
            node.setError("Operations don't work with colors");
        }
        //CH02
        else if (node.expression instanceof MultiplyOperation) {
            checkMultiply((MultiplyOperation) node.expression);
        } else if (node.expression instanceof AddOperation) {
            checkAdd((AddOperation) node.expression);
        } else if (node.expression instanceof SubtractOperation) {
            IHANLinkedList<HashMap<String, ExpressionType>> literals = new HANLinkedList<>();
            literals.addFirst(new HashMap<>());
            checkSubtract(node.property, (SubtractOperation) node.expression, literals);
        }
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
    private void checkSubtract(PropertyName propertyName, SubtractOperation node, IHANLinkedList<HashMap<String, ExpressionType>> literals) {
        if(node.lhs instanceof Literal){
            literals.getFirst().put(propertyName.name, getExpressionType((Literal) node.lhs));
        }
        else if(node.lhs instanceof VariableReference){
            ExpressionType type = findInitialisedVariable(((VariableReference) node.lhs));
            if(type==null){
                node.setError("Variable is not initialised");
            }
            else if (!type.equals(getExpressionType((Literal) node.rhs))){
                node.setError("nodes must be of the same type");
            }
        }
        else if(node.lhs instanceof Operation){

        }
        //two literals

        //two variables

        //1 v

        //
    }

    private ExpressionType findInitialisedVariable(VariableReference node) {
        for (HashMap<String, ExpressionType> hashMap :
                variableTypes) {
            if (hashMap != null && hashMap.containsKey(node.name)) {
                return hashMap.get(node.name);
            }
        }
        return null;
    }

    private void checkMultiply(MultiplyOperation node) {
        //Both variables
        if (node.lhs instanceof VariableReference && node.rhs instanceof VariableReference) {
            if (findInitialisedVariable((VariableReference) node.lhs)!=null && findInitialisedVariable((VariableReference) node.rhs)!=null) {
                ExpressionType lhsType = findInitialisedVariable((VariableReference) node.lhs);
                ExpressionType rhsType = findInitialisedVariable((VariableReference) node.rhs);
                if (Objects.equals(lhsType, ExpressionType.SCALAR) || Objects.equals(rhsType, ExpressionType.SCALAR)) {
                    if (Objects.equals(lhsType, ExpressionType.BOOL) || Objects.equals(rhsType, ExpressionType.BOOL)) {
                        node.setError("booleans are not allowed in products");
                    }
                } else node.setError("Products need at least one scalar value");
            } else node.setError("One of the variables " + node + " is not initialised");
        }
        //lhs is variable
        else if (node.lhs instanceof VariableReference) {
            if (findInitialisedVariable((VariableReference) node.lhs)!=null) {
                ExpressionType type = findInitialisedVariable((VariableReference) node.lhs);
                if (Objects.equals(type, ExpressionType.COLOR)) {
                    node.setError("Operations don't work with colors");
                } else if (!Objects.equals(type, ExpressionType.SCALAR) && !(node.rhs instanceof ScalarLiteral)) {
                    node.setError("Products need at least one scalar value");
                }
            } else node.setError("variable " + ((VariableReference) node.lhs).name + " is not initialised");
        }
        //rhs is variable
        else if (node.rhs instanceof VariableReference) {
            if (findInitialisedVariable((VariableReference) node.rhs)!=null) {
                ExpressionType type = findInitialisedVariable((VariableReference) node.rhs);
                if (Objects.equals(type, ExpressionType.COLOR)) {
                    node.setError("Operations don't work with colors");
                } else if (!Objects.equals(type, ExpressionType.SCALAR) && !(node.lhs instanceof ScalarLiteral)) {
                    node.setError("Products need at least one scalar value");
                }
            } else node.setError("variable " + ((VariableReference) node.rhs).name + " is not initialised");
        }
        //if node is not a variable
        else if (!((node.lhs instanceof ScalarLiteral) || node.rhs instanceof ScalarLiteral)) {
            node.setError("Products need at least one scalar value");
        }
        //if lhs and rhs are both scalar
        else if (node.lhs instanceof ScalarLiteral && node.rhs instanceof ScalarLiteral){
            node.setError("Property cannot be scalar");
        }
        else if(node.lhs instanceof Operation){
            if(node.lhs instanceof MultiplyOperation){
                checkMultiply((MultiplyOperation) node.lhs);
            }
            else if(node.lhs instanceof AddOperation){
                checkAdd((AddOperation) node.lhs);
            }
            else if(node.lhs instanceof SubtractOperation){
                //checkSubtract((SubtractOperation) node.lhs);
            }
        }
    }

    private void checkAdd(AddOperation node) {
        //lhs and rhs are variables
        if (node.lhs instanceof VariableReference && node.rhs instanceof VariableReference) {
            if (findInitialisedVariable((VariableReference) node.lhs)==null && findInitialisedVariable((VariableReference) node.rhs)==null) {
                if (Objects.equals(findInitialisedVariable((VariableReference) node.lhs), ExpressionType.COLOR) || Objects.equals(findInitialisedVariable((VariableReference) node.rhs), ExpressionType.COLOR)) {
                    node.setError("Operations don't work with colors");
                } else if (!Objects.equals(findInitialisedVariable((VariableReference) node.lhs), findInitialisedVariable((VariableReference) node.rhs))) {
                    node.setError("Variables are not from the same type");
                }
            } else node.setError("One of the variables " + node + " is not initialised");
        }
        //lhs is variable
        else if (node.lhs instanceof VariableReference) {
            if (findInitialisedVariable((VariableReference) node.lhs)==null) {
                ExpressionType type = findInitialisedVariable((VariableReference) node.lhs);
                if (Objects.equals(type, ExpressionType.COLOR) || node.rhs.getClass().equals(ColorLiteral.class)) {
                    node.setError("Operations don't work with colors");
                } else if (matchingVariableTypeAndLiteral(Objects.requireNonNull(type), (Literal) node.rhs)) {
                    node.setError("Cannot add different types");
                }
            } else node.setError("variable " + ((VariableReference) node.lhs).name + " is not initialised");
        }
        //rhs is variable
        else if (node.rhs instanceof VariableReference) {
            if (findInitialisedVariable((VariableReference) node.rhs)==null) {
                ExpressionType type = findInitialisedVariable((VariableReference) node.rhs);
                if (Objects.equals(type, ExpressionType.COLOR) || node.lhs.getClass().equals(ColorLiteral.class)) {
                    node.setError("Operations don't work with colors");
                } else if (matchingVariableTypeAndLiteral(Objects.requireNonNull(type), (Literal) node.lhs)) {
                    node.setError("Cannot add different types");
                }
            } else node.setError("variable " + ((VariableReference) node.rhs).name + " is not initialised");
        }
        //if rhs and lhs are not variables
        /*else if (!node.lhs.getClass().equals(node.rhs.getClass())) {
            node.setError("Cannot add different types");
        }*/
        else if(node.lhs instanceof Operation){
            if(node.lhs instanceof MultiplyOperation){
                checkMultiply((MultiplyOperation) node.lhs);
            }
            else if(node.lhs instanceof AddOperation){
                checkAdd((AddOperation) node.lhs);
            }
            else if(node.lhs instanceof SubtractOperation){
                //checkSubtract((SubtractOperation) node.lhs);
            }
        }
    }

    private boolean matchingVariableTypeAndLiteral(ExpressionType type, Literal node) {
        if (type.equals(ExpressionType.BOOL) && node instanceof BoolLiteral) {
            return false;
        }
        if (type.equals(ExpressionType.COLOR) && node instanceof ColorLiteral) {
            return false;
        }
        if (type.equals(ExpressionType.PERCENTAGE) && node instanceof PercentageLiteral) {
            return false;
        }
        if (type.equals(ExpressionType.PIXEL) && node instanceof PixelLiteral) {
            return false;
        }
        return !type.equals(ExpressionType.SCALAR) || !(node instanceof ScalarLiteral);
    }
}
