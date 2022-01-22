/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.parser;

/**
 * An enum of types of feature model formats.
 */
public enum FMFormat {
    NONE,
    SXFM, // SPLOT format
    FEATUREIDE, // FeatureIDE format
    XMI, // v.control format
    GLENCOE, // Glencoe format
    DESCRIPTIVE // my format
}
