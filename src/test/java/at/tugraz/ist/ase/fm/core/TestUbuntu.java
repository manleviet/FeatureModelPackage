/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021-2022
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

import at.tugraz.ist.ase.fm.parser.FeatureModelParserException;
import at.tugraz.ist.ase.fm.parser.SXFMParser;

import java.io.File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestUbuntu {
    static FeatureModel featureModel;

    @Test
    void test() throws FeatureModelParserException {
        File fileFM = new File("src/test/resources/ubuntu.splx");
        SXFMParser parser = new SXFMParser();
        featureModel = parser.parse(fileFM);

        assertNotNull(featureModel);
    }
}
