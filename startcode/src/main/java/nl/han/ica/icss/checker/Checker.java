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
import java.util.List;
import java.util.Objects;


public class Checker {

    private final HashMap<String, List<ExpressionType>> propertyNames = new HashMap<>();
    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        setupLinkedLists();
        check(ast.root.body);
    }

    private void setupLinkedLists() {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
        propertyNames.put("width", List.of(ExpressionType.PIXEL, ExpressionType.PERCENTAGE));
        propertyNames.put("height", List.of(ExpressionType.PIXEL, ExpressionType.PERCENTAGE));
        propertyNames.put("color", List.of(ExpressionType.COLOR));
        propertyNames.put("background-color", List.of(ExpressionType.COLOR));
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
            node.setError("Variable cannot be scalar");
        } else if (node.expression instanceof Operation) {
            variableTypes.getFirst().put(node.name.name, checkOperation((Operation) node.expression));
        } else variableTypes.getFirst().put(node.name.name, ExpressionType.UNDEFINED);
    }

    private void checkDeclaration(Declaration node) {

        if (node.expression instanceof VariableReference) {
            ExpressionType variableType = getInitialisedVariableType((VariableReference) node.expression);
            if (variableType == null) {
                node.setError("variable " + ((VariableReference) node.expression).name + " is not initialised");
            } else if (!variableType.equals(ExpressionType.UNDEFINED)) {
                checkProperty(node, propertyNames, variableType);
            }
        }
        //CH01
        if (node.expression instanceof Operation) {
            ExpressionType expressionType = checkOperation((Operation) node.expression);
            if (!expressionType.equals(ExpressionType.UNDEFINED)) {
                checkProperty(node, propertyNames, expressionType);
            }
        }
        if (node.expression instanceof Literal) {
            if (node.expression instanceof ScalarLiteral) {
                node.setError("Property cannot be scalar");
            } else checkProperty(node, propertyNames, getExpressionType((Literal) node.expression));
        }
    }

    private void checkProperty(Declaration node, HashMap<String, List<ExpressionType>> types, ExpressionType type) {
        if (!types.get(node.property.name).contains(type)) {
            node.setError("Property " + node.property.name + " must be of type " + types.get(node.property.name));
        }
    }

    private ExpressionType checkOperation(Operation node) {
        //CH03 for non variables
        if (node.lhs instanceof ColorLiteral || node.rhs instanceof ColorLiteral) {
            node.setError("Operations don't work with colors");
        }
        //CH02
        else if (node instanceof MultiplyOperation) {
            return checkMultiply((MultiplyOperation) node, new HANLinkedList<>());
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

    private ExpressionType checkMultiply(MultiplyOperation node, IHANLinkedList<ExpressionType> expressionTypes) {
        if (node.lhs instanceof MultiplyOperation || node.rhs instanceof MultiplyOperation) {
            if (node.lhs instanceof MultiplyOperation) {
                if (node.rhs instanceof Literal) {
                    expressionTypes.addFirst(getExpressionType((Literal) node.rhs));
                } else {
                    expressionTypes.addFirst(getInitialisedVariableType((VariableReference) node.rhs));
                }
                return checkMultiply((MultiplyOperation) node.lhs, expressionTypes);
            } else {
                if (node.lhs instanceof Literal) {
                    expressionTypes.addFirst(getExpressionType((Literal) node.lhs));
                } else expressionTypes.addFirst(getInitialisedVariableType((VariableReference) node.lhs));
                return checkMultiply((MultiplyOperation) node.rhs, expressionTypes);
            }
        }
        //variables
        if (node.lhs instanceof VariableReference || node.rhs instanceof VariableReference) {
            //both variables
            if (node.lhs instanceof VariableReference && node.rhs instanceof VariableReference) {
                ExpressionType lhsType = getInitialisedVariableType((VariableReference) node.lhs);
                ExpressionType rhsType = getInitialisedVariableType((VariableReference) node.rhs);
                if (lhsType == null) {
                    node.lhs.setError("variable " + ((VariableReference) node.lhs).name + " is not initialized");
                } else if (rhsType == null) {
                    node.rhs.setError("variable " + ((VariableReference) node.rhs).name + " is not initialized");
                } else if (expressionTypes.getSize() > 0) {
                    expressionTypes.addFirst(rhsType);
                    expressionTypes.addFirst(lhsType);
                    return checkMultiplicationWithThreeOrMoreNumbers(node, expressionTypes);
                } else if (lhsType.equals(rhsType)) {
                    return lhsType;
                } else return ExpressionType.UNDEFINED;
            }
            //lhs is variable and we can assume rhs is literal
            else if (node.lhs instanceof VariableReference) {
                ExpressionType lhsType = getInitialisedVariableType((VariableReference) node.lhs);
                if (lhsType == null) {
                    node.lhs.setError("variable " + ((VariableReference) node.lhs).name + " is not initialized");
                    //return ExpressionType.UNDEFINED;
                } else if (expressionTypes.getSize() > 0) {
                    expressionTypes.addFirst(lhsType);
                    return checkMultiplicationWithThreeOrMoreNumbers(node, expressionTypes);
                } else if (node.rhs instanceof PixelLiteral || node.rhs instanceof PercentageLiteral) {
                    node.setError("should be at least one scalar value in product");
                    return ExpressionType.UNDEFINED;
                } else if (ExpressionType.SCALAR.equals(getExpressionType((Literal) node.rhs))) {
                    return lhsType;
                } else return ExpressionType.UNDEFINED;
            }
            //rhs is variable and we can safely assume lhs is literal
            else {
                ExpressionType rhsType = getInitialisedVariableType((VariableReference) node.rhs);
                if (rhsType == null) {
                    node.rhs.setError("variable " + ((VariableReference) node.rhs).name + " is not initialized");
                    //return ExpressionType.UNDEFINED;
                } else if (expressionTypes.getSize() > 0) {
                    expressionTypes.addFirst(rhsType);
                    return checkMultiplicationWithThreeOrMoreNumbers(node, expressionTypes);
                } else if (node.lhs instanceof PixelLiteral || node.lhs instanceof PercentageLiteral) {
                    node.setError("should be at least one scalar value in product");
                    return ExpressionType.UNDEFINED;
                } else if (ExpressionType.SCALAR.equals(getExpressionType((Literal) node.lhs))) {
                    return rhsType;
                } else return ExpressionType.UNDEFINED;
                //return checkMultiplicationTypes(node,getExpressionType((Literal) node.lhs),rhsType);
            }
        }
        //both literals
        if (node.lhs instanceof Literal && node.rhs instanceof Literal) {
            if (expressionTypes.getSize() > 0) {
                expressionTypes.addFirst(getExpressionType((Literal) node.rhs));
                expressionTypes.addFirst(getExpressionType((Literal) node.lhs));
                return checkMultiplicationWithThreeOrMoreNumbers(node, expressionTypes);
            }
            if (node.lhs instanceof ScalarLiteral && node.rhs instanceof ScalarLiteral) {
                node.setError("cannot have two scalar values in product");
                return ExpressionType.UNDEFINED;
            } else if (!(node.lhs instanceof ScalarLiteral) && !(node.rhs instanceof ScalarLiteral)) {
                node.setError("should be at least one scalar value in product");
                return ExpressionType.UNDEFINED;
            } else if (node.lhs instanceof BoolLiteral || node.rhs instanceof BoolLiteral) {
                node.setError("cannot have two booleans");
                return ExpressionType.UNDEFINED;
            } else if (node.lhs instanceof ScalarLiteral) {
                return getExpressionType((Literal) node.rhs);
            } else {
                return getExpressionType((Literal) node.lhs);
            }
        }
        System.out.println("oh no");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkMultiplicationWithThreeOrMoreNumbers(MultiplyOperation node, IHANLinkedList<ExpressionType> expressionTypes) {
        boolean hasScalar = false;
        List<ExpressionType> list = new ArrayList<>();
        for (ExpressionType type :
                expressionTypes) {
            if (type.equals(ExpressionType.SCALAR)) {
                hasScalar = true;
            }
            if (type.equals(ExpressionType.PIXEL) || type.equals(ExpressionType.PERCENTAGE)) {
                list.add(type);
            }
        }
        if (!hasScalar) {
            node.setError("Multiplication must contain at least one scalar value");
            return ExpressionType.UNDEFINED;
        }
        if (list.size() == 0) {
            node.setError("Multiplication must contain one pixel or percentage literal");
        }
        if (list.size() > 1) {
            node.setError("Multiplication cannot contain more than one pixel or percentage literal");
        }
        if (list.size() == 1) {
            return list.get(0);
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
