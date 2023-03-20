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
            if ((getInitialisedVariableType((VariableReference) ((IfClause) node).conditionalExpression) != null)) {
                //CH05 variable reference
                if (!Objects.equals(getInitialisedVariableType((VariableReference) ((IfClause) node).conditionalExpression), ExpressionType.BOOL)) {
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
            if (getInitialisedVariableType((VariableReference) node.expression) == null) {
                node.setError("variable " + ((VariableReference) node.expression).name + " is not initialised");
            }
        }
        //CH01
        if (node.expression instanceof Operation) {
            checkOperation((Operation) node.expression);
        }
        if (node.expression instanceof ScalarLiteral) {
            node.setError("Property cannot be scalar");
        }
    }

    private ExpressionType checkOperation(Operation node) {
        //CH03 for non variables
        if (node.lhs instanceof ColorLiteral || node.rhs instanceof ColorLiteral) {
            node.setError("Operations don't work with colors");
        }
        //CH02
        else if (node instanceof MultiplyOperation) {
            return checkMultiply((MultiplyOperation) node);
        } else if (node instanceof AddOperation) {
            return checkAdd((AddOperation) node);
        } else if (node instanceof SubtractOperation) {
            return checkSubtract((SubtractOperation) node);
        }
        return ExpressionType.UNDEFINED;
    }

    public ExpressionType getExpressionType(Literal literal) {
        if (literal instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }
        if (literal instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        if (literal instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }
        if (literal instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        if (literal instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkSubtract(SubtractOperation node) {
        return checkAddingOrSubtracting(node);
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

    private ExpressionType checkMultiply(MultiplyOperation node) {
        if (node.lhs instanceof Literal || node.rhs instanceof Literal) {
            if (node.lhs instanceof Literal && node.rhs instanceof Literal) {
                if (node.lhs instanceof ScalarLiteral && node.rhs instanceof ScalarLiteral) {
                    node.setError("cannot have two scalar values in product");
                } else if (node.lhs instanceof BoolLiteral || node.rhs instanceof BoolLiteral) {
                    node.setError("cannot have two booleans");
                }
            }
        }
        /*if (node.lhs instanceof MultiplyOperation) {

        }*/
        //operations
        /*if (node.lhs instanceof Operation || node.rhs instanceof Operation) {
            if (node.lhs instanceof Operation) {
                //expressionTypes.addFirst(checkOperation((Operation) node.lhs, expressionTypes));
            } else {
                //expressionTypes.addFirst(checkOperation((Operation) node.rhs, expressionTypes));
            }
            //expressionTypes.getFirst();
        }*/
        //variables
        if (node.lhs instanceof VariableReference || node.rhs instanceof VariableReference) {
            //both variables
            if (node.lhs instanceof VariableReference && node.rhs instanceof VariableReference) {
                ExpressionType lhsType = getInitialisedVariableType((VariableReference) node.lhs);
                if (lhsType == null) {
                    node.lhs.setError("variable " + ((VariableReference) node.lhs).name + " is not initialized");
                }
                ExpressionType rhsType = getInitialisedVariableType((VariableReference) node.rhs);
                if (rhsType == null) {
                    node.rhs.setError("variable " + ((VariableReference) node.rhs).name + " is not initialized");
                }
                //return checkMultiplicationTypes(node, lhsType, rhsType);
            }
            //lhs is variable
            else if (node.lhs instanceof VariableReference) {
                ExpressionType lhsType = getInitialisedVariableType((VariableReference) node.lhs);
                if (lhsType == null) {
                    node.lhs.setError("variable " + ((VariableReference) node.lhs).name + " is not initialized");
                    //return ExpressionType.UNDEFINED;
                }
                //return checkMultiplicationTypes(node,lhsType,getExpressionType((Literal) node.rhs));
            }
            //rhs is variable
            else {
                ExpressionType rhsType = getInitialisedVariableType((VariableReference) node.rhs);
                if (rhsType == null) {
                    node.rhs.setError("variable " + ((VariableReference) node.rhs).name + " is not initialized");
                    //return ExpressionType.UNDEFINED;
                }
                //return checkMultiplicationTypes(node,getExpressionType((Literal) node.lhs),rhsType);
            }
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkAdd(AddOperation node) {
        return checkAddingOrSubtracting(node);
    }

    private ExpressionType checkAddingOrSubtracting(Operation node) {
        if (node.lhs instanceof Literal && node.rhs instanceof Literal) {
            if (checkAddingNotAllowedLiterals((Literal) node.lhs) || checkAddingNotAllowedLiterals((Literal) node.rhs)) {
                if (checkAddingNotAllowedLiterals((Literal) node.lhs)) {
                    node.setError("Cannot add or subtract values from the type " + node.lhs.getClass());
                } else node.setError("Cannot add or subtract values from the type " + node.rhs.getClass());
            } else if (!getExpressionType((Literal) node.lhs).equals(getExpressionType((Literal) node.rhs))) {
                node.setError("Cannot add or subtract " + node.lhs + " and " + node.rhs + " because they have different types");
            } else return getExpressionType((Literal) node.lhs);
        } else if (node.lhs instanceof Literal) {
            if (checkAddingNotAllowedLiterals((Literal) node.lhs)) {
                node.setError("Cannot add or subtract values from the type " + node.lhs.getClass());
            } else if (!getExpressionType((Literal) node.lhs).equals(getOtherType(node.rhs))) {
                node.setError("Cannot add or subtract " + node.lhs + " and " + node.rhs + " because they have different types");
            } else return getExpressionType((Literal) node.lhs);
        } else if (node.rhs instanceof Literal) {
            if (checkAddingNotAllowedLiterals((Literal) node.rhs)) {
                node.setError("Cannot add or subtract values from the type " + node.rhs.getClass());
            } else if (!getExpressionType((Literal) node.rhs).equals(getOtherType(node.lhs))) {
                node.setError("Cannot add or subtract " + node.lhs + " and " + node.rhs + " because they have different types");
            } else return getExpressionType((Literal) node.rhs);
        } else {
            ExpressionType lhs = getOtherType(node.lhs);
            ExpressionType rhs = getOtherType(node.rhs);
            if (!lhs.equals(rhs)) {
                node.setError("Cannot add or subtract " + node.lhs + " and " + node.rhs + " because they have different types");
            } else return lhs;
        }
        return ExpressionType.UNDEFINED;
    }

    private boolean checkAddingNotAllowedLiterals(Literal literal) {
        return literal instanceof BoolLiteral || literal instanceof ColorLiteral || literal instanceof ScalarLiteral;
    }

    //This function gets called when one side of an expression is a VariableReference or an Operation
    private ExpressionType getOtherType(Expression node) {
        if (node instanceof VariableReference) {
            ExpressionType expressionType = getInitialisedVariableType((VariableReference) node);
            if (expressionType == null) {
                node.setError("variable " + ((VariableReference) node).name + " is not initialised");
                return ExpressionType.UNDEFINED;
            } else return expressionType;
        } else return checkOperation((Operation) node);
    }
}
