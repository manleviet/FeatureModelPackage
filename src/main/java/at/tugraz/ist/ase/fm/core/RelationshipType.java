/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021-2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

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
    ThreeCNF // 3CNF
}
