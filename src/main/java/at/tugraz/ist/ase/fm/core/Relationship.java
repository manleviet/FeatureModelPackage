/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

import java.util.ArrayList;
import java.util.List;

import static at.tugraz.ist.ase.fm.core.Utilities.createStringFromArrayWithSeparator;

/**
 * Represents a relationship/constraint of a feature model.
 *
 * @author Viet-Man Le (vietman.le@ist.tugraz.at)
 */
public class Relationship {
    /**
     * The types of relationships/constraints
     */
    public enum RelationshipType {
        MANDATORY,
        OPTIONAL,
        ALTERNATIVE,
        OR,
        REQUIRES,
        EXCLUDES,
        SPECIAL // 3CNF
    }

    private RelationshipType type;
    private String leftSide;
    private ArrayList<String> rightSide;

    private List<Clause> clauses;

    private String confRule;

    private ArrayList<String> constraints;

    /**
     * A constructor
     * @param type type of relationship/constraint
     * @param leftSide left part of relationship/constraint
     * @param rightSide right part of relationship/constraint
     */
    public Relationship(RelationshipType type, String leftSide, ArrayList<String> rightSide) {
        this.type = type;
        this.leftSide = leftSide;
        this.rightSide = rightSide;

        convertToConfRule();

        constraints = new ArrayList<>();
    }

    /**
     * A constructor for 3CNF constraints.
     * @param type type of constraint
     * @param constraint3CNF a 3CNF constraint.
     */
    public Relationship(RelationshipType type, String constraint3CNF) {
        this.type = type;
        clauses = new ArrayList<>();
        splitTestCase(constraint3CNF);

        convertToConfRule();

        constraints = new ArrayList<>();
    }

    private void splitTestCase(String constraint3CNF) {
        String[] clauses = constraint3CNF.split(" & ");

        for (String c: clauses) {
            Clause clause = new Clause(c);
            this.clauses.add(clause);
        }
    }

    /**
     * Gets the type of the relationship/constraint.
     * @return
     */
    public RelationshipType getType() {
        return type;
    }

    /**
     * Gets clauses of the 3CNF constraint.
     * @return list of {@link Clause}s.
     */
    public List<Clause> getClauses() {
        return clauses;
    }

    /**
     * Checks whether the relationship is optional.
     * @return true if the relationship is OPTIONAL or OR, false otherwise.
     */
    public boolean isOptional() {
        if (type == RelationshipType.OPTIONAL || type == RelationshipType.OR) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the type of relationship/constraint is the given {@link RelationshipType} type.
     * @param type
     * @return the type of relationship/constraint.
     */
    public boolean isType(RelationshipType type) {
        return this.type == type;
    }

    /**
     * Gets the left part of the relationship/constraint.
     * @return
     */
    public String getLeftSide() {
        return leftSide;
    }

    /**
     * Checks whether the given {@link Feature} belongs to the left part of the relationship/constraint.
     * @param feature
     * @return true if yes, false otherwise.
     */
    public boolean belongsToLeftSide(Feature feature) {
        if (leftSide.equals(feature.toString()))
            return true;
        return false;
    }

    /**
     * Checks whether the given {@link Feature} belongs to the right part of the relationship/constraint.
     * @param feature
     * @return true if yes, false otherwise.
     */
    public boolean belongsToRightSide(Feature feature) {
        for (String right: rightSide) {
            if (right.equals(feature.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the right part of the relationship/constraint.
     * @return an array of the right part.
     */
    public ArrayList<String> getRightSide() {
        return rightSide;
    }

    public String getConfRule() {
        return confRule;
    }

    /**
     * Adds a textual Choco constraint.
     * @param constraint a textual Choco constraint.
     */
    public void setConstraint(String constraint) {
        constraints.add(constraint);
    }

    /**
     * Gets all Choco constraints.
     * @return an array of textual Choco constraints.
     */
    public ArrayList<String> getConstraints() {
        return constraints;
    }

    private void convertToConfRule() {
        switch (type) {
            case MANDATORY:
                confRule = String.format("mandatory(%s, %s)", leftSide, rightSide.get(0));
                break;
            case OPTIONAL:
                confRule = String.format("optional(%s, %s)", leftSide, rightSide.get(0));
                break;
            case REQUIRES:
                confRule = String.format("requires(%s, %s)", leftSide, rightSide.get(0));
                break;
            case ALTERNATIVE:
                confRule = String.format("alternative(%s, %s)", leftSide, createStringFromArrayWithSeparator(rightSide,","));
                break;
            case OR:
                confRule = String.format("or(%s, %s)", leftSide, createStringFromArrayWithSeparator(rightSide,","));
                break;
            case EXCLUDES:
                confRule = String.format("excludes(%s, %s)", leftSide, rightSide.get(0));
                break;
            case SPECIAL:
                confRule = String.format("3cnf(%s)", createStringFromArrayWithSeparator(getNameOfClauses(clauses),","));
                break;
        }
    }

    private ArrayList<String> getNameOfClauses(List<Clause> clauses) {
        ArrayList<String> names = new ArrayList<>();
        for (Clause c : clauses) {
            if (c.getValue())
                names.add(c.getLiteral());
            else
                names.add("~" + c.getLiteral());
        }
        return names;
    }
}

