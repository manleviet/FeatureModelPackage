/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

import at.tugraz.ist.ase.fm.parser.ParserException;
import at.tugraz.ist.ase.fm.parser.SXFMParser;

import java.io.File;
import java.util.ArrayList;

import static org.testng.Assert.*;

public class FeatureModelTest {
    private FeatureModel featureModel;

    @org.testng.annotations.BeforeMethod
    public void setUp() throws ParserException {
        File fileFM = new File("src/test/resources/FM_10_0.splx");
        SXFMParser parser = new SXFMParser();
        featureModel = parser.parse(fileFM);
    }

    @org.testng.annotations.AfterMethod
    public void tearDown() {
    }

    @org.testng.annotations.Test
    public void testGetName() {
        assertEquals(featureModel.getName(), "FM_10_0");
    }

    @org.testng.annotations.Test
    public void testGetFeature() throws FeatureModelException {
        Feature f1 = featureModel.getFeatures().get(4);

        Feature f2 = featureModel.getFeature(4);
        Feature f3 = featureModel.getFeature("F3");

        assertEquals(f1, f2);
        assertEquals(f1, f3);
    }

    @org.testng.annotations.Test
    public void testGetNumOfFeatures() {
        assertEquals(featureModel.getNumOfFeatures(), 9);
    }

    @org.testng.annotations.Test
    public void testIsMandatoryFeature() throws FeatureModelException {
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(0)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(1)));
        assertTrue(featureModel.isMandatoryFeature(featureModel.getFeature(2)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(3)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(4)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(5)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(6)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(7)));
        assertFalse(featureModel.isMandatoryFeature(featureModel.getFeature(8)));
    }

    @org.testng.annotations.Test
    public void testIsOptionalFeature() throws FeatureModelException {
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(1)));
        assertFalse(featureModel.isOptionalFeature(featureModel.getFeature(2)));
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(3)));
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(4)));
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(5)));
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(6)));
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(7)));
        assertTrue(featureModel.isOptionalFeature(featureModel.getFeature(8)));
    }

    @org.testng.annotations.Test
    public void testGetLeftSideOfRequiresConstraint() {
    }

    @org.testng.annotations.Test
    public void testGetRightSideOfRelationships() throws FeatureModelException {
        Feature f1 = featureModel.getFeature(0);
        Feature f2 = featureModel.getFeature(2);
        ArrayList<Feature> f1s = featureModel.getRightSideOfRelationships(f1);
        ArrayList<Feature> f2s = featureModel.getRightSideOfRelationships(f2);

        assertEquals(f1s.get(0).toString(), "F1");
        assertEquals(f1s.get(1).toString(), "F2");
        assertEquals(f1s.get(2).toString(), "F3");
        assertEquals(f1s.get(3).toString(), "F4");
        assertEquals(f1s.get(4).toString(), "F5");
        assertEquals(f1s.get(5).toString(), "F6");
        assertEquals(f1s.get(6).toString(), "F7");

        assertEquals(f2s.get(0).toString(), "F8");
    }

    @org.testng.annotations.Test
    public void testGetMandatoryParents() throws FeatureModelException {
        // TODO - 3CNF
//        Feature f1 = featureModel.getFeature("F6");
//
//        ArrayList<Feature> f1s = featureModel.getMandatoryParents(f1);
//
//        assertEquals(f1s.size(), 1);
    }

    @org.testng.annotations.Test
    public void testGetMandatoryParent() {
    }

    @org.testng.annotations.Test
    public void testGetRelationshipByConstraint() {
    }

    @org.testng.annotations.Test
    public void testGetNumOfRelationships() {
        assertEquals(featureModel.getNumOfRelationships(), 5);
    }

    @org.testng.annotations.Test
    public void testGetNumOfRelationshipsWithSpecifyType() {
        assertEquals(featureModel.getNumOfRelationships(Relationship.RelationshipType.MANDATORY), 1);
        assertEquals(featureModel.getNumOfRelationships(Relationship.RelationshipType.OPTIONAL), 2);
        assertEquals(featureModel.getNumOfRelationships(Relationship.RelationshipType.ALTERNATIVE), 1);
        assertEquals(featureModel.getNumOfRelationships(Relationship.RelationshipType.OR), 1);
    }

    @org.testng.annotations.Test
    public void testGetConstraints() {
        ArrayList<Relationship> constraints = featureModel.getConstraints();

        assertEquals(constraints.get(0).getType(),
                Relationship.RelationshipType.REQUIRES);
        assertEquals(constraints.get(1).getType(),
                Relationship.RelationshipType.EXCLUDES);
        assertEquals(constraints.get(2).getType(),
                Relationship.RelationshipType.SPECIAL);

        assertEquals(constraints.get(0).getConfRule(),
                "requires(F8, F6)");
        assertEquals(constraints.get(1).getConfRule(),
                "excludes(F4, F1)");
        assertEquals(constraints.get(2).getConfRule(),
                "3cnf(~F1, F7, F8)");
    }

    @org.testng.annotations.Test
    public void testGetNumOfConstraints() {
        assertEquals(featureModel.getNumOfConstraints(), 3);
    }

    @org.testng.annotations.Test
    public void testTestToString() {
        StringBuilder st = new StringBuilder();

        st.append("FEATURES:\n");
        st.append(String.format("\t%s\n", "FM_10_0"));
        st.append(String.format("\t%s\n", "F1"));
        st.append(String.format("\t%s\n", "F2"));
        st.append(String.format("\t%s\n", "F8"));
        st.append(String.format("\t%s\n", "F3"));
        st.append(String.format("\t%s\n", "F4"));
        st.append(String.format("\t%s\n", "F5"));
        st.append(String.format("\t%s\n", "F6"));
        st.append(String.format("\t%s\n", "F7"));

        st.append("RELATIONSHIPS:\n");
        st.append(String.format("\t%s\n", "optional(F1, FM_10_0)"));
        st.append(String.format("\t%s\n", "mandatory(FM_10_0, F2)"));
        st.append(String.format("\t%s\n", "or(FM_10_0, F3, F4, F5)"));
        st.append(String.format("\t%s\n", "alternative(FM_10_0, F6, F7)"));
        st.append(String.format("\t%s\n", "optional(F8, F2)"));

        st.append("CONSTRAINTS:\n");
        st.append(String.format("\t%s\n", "requires(F8, F6)"));
        st.append(String.format("\t%s\n", "excludes(F4, F1)"));
        st.append(String.format("\t%s\n", "3cnf(~F1, F7, F8)"));

        assertEquals(st.toString(), featureModel.toString());
    }
}