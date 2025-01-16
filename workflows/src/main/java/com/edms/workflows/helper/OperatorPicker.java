package com.edms.workflows.helper;

import java.util.function.BiPredicate;


public class OperatorPicker {

    public enum Operator {
        EQUALS((a, b) -> a.equals(b)),
        NOT_EQUALS((a, b) -> !a.equals(b)),
        GREATER_THAN((a, b) -> ((Comparable<Object>) a).compareTo(b) > 0),
        LESS_THAN((a, b) -> ((Comparable<Object>) a).compareTo(b) < 0),
        GREATER_THAN_OR_EQUALS((a, b) -> ((Comparable<Object>) a).compareTo(b) >= 0),
        LESS_THAN_OR_EQUALS((a, b) -> ((Comparable<Object>) a).compareTo(b) <= 0),
        CONTAINS((a, b) -> a.toString().contains(b.toString())),
        NOT_CONTAINS((a, b) -> !a.toString().contains(b.toString())),
        STARTS_WITH((a, b) -> a.toString().startsWith(b.toString())),
        ENDS_WITH((a, b) -> a.toString().endsWith(b.toString()));

        private final BiPredicate<Object, Object> predicate;

        Operator(BiPredicate<Object, Object> predicate) {
            this.predicate = predicate;
        }

        public boolean apply(Object a, Object b) {
            return predicate.test(a, b);
        }
    }

    public Operator pickOperator(String operator) {
        switch (operator.toLowerCase()) {
            case "==":
                return Operator.EQUALS;
            case "!=":
                return Operator.NOT_EQUALS;
            case ">":
                return Operator.GREATER_THAN;
            case "<":
                return Operator.LESS_THAN;
            case ">=":
                return Operator.GREATER_THAN_OR_EQUALS;
            case "<=":
                return Operator.LESS_THAN_OR_EQUALS;
            case "contains":
                return Operator.CONTAINS;
            case "not contains":
                return Operator.NOT_CONTAINS;
            case "starts with":
                return Operator.STARTS_WITH;
            case "ends with":
                return Operator.ENDS_WITH;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    
}
