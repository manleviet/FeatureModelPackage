/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.parser;

/**
 * An exception for errors which occur in parsing feature model files
 */
public class FeatureModelParserException extends Exception {

    public FeatureModelParserException(String message) {
        super(message);
    }

    public FeatureModelParserException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

