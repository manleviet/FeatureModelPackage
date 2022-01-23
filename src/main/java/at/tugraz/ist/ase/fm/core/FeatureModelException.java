/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021-2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

/**
 * An exception for errors which occur in parsing feature model files
 */
public class FeatureModelException extends Exception {
    public FeatureModelException(String message) {
        super(message);
    }

    public FeatureModelException(String message, Throwable throwable) {
        super(message, throwable);
    }
}


