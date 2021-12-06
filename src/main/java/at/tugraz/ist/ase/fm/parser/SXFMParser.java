/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.parser;

import at.tugraz.ist.ase.common.LoggerUtils;
import at.tugraz.ist.ase.fm.core.Feature;
import at.tugraz.ist.ase.fm.core.FeatureModel;
import at.tugraz.ist.ase.fm.core.RelationshipType;
import constraints.BooleanVariable;
import constraints.PropositionalFormula;
import fm.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A parser for the SPLOT format
 *
 * Using "fmapi" library of Generative Software Development Lab (http://gsd.uwaterloo.ca/)
 * University of Waterloo
 * Waterloo, Ontario, Canada
 *
 * For further details of this library, we refer to http://52.32.1.180:8080/SPLOT/sxfm.html
 */
@Slf4j
public class SXFMParser implements FeatureModelParser {

    /**
     * Checks whether the format of the given file is SPLOT format
     *
     * @param filePath - a {@link File}
     * @return true - if the format of the given file is SPLOT format
     *          false - otherwise
     */
    @Override
    public boolean checkFormat(@NonNull File filePath) {
        // first, check the extension of file
        checkArgument(filePath.getName().endsWith(".sxfm") || filePath.getName().endsWith(".splx"), "The file is not in SPLOT format!");
        // second, check the structure of file
        try {
            // read the file
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(filePath.toString());
            Element rootEle = doc.getDocumentElement();

            // if it has three tag "feature_model", "feature_tree" and "constraints"
            if (rootEle.getTagName().equals("feature_model") &&
                    rootEle.getElementsByTagName("feature_tree").getLength() > 0 &&
                    rootEle.getElementsByTagName("constraints").getLength() > 0) {
                return true;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return false; // if it raises an exception, it's not SPLOT format
        }
        return false;
    }

    /**
     * This function parses the given {@link File} into a {@link FeatureModel}.
     *
     * @param filePath - a {@link File}
     * @return a {@link FeatureModel}
     * @throws FeatureModelParserException when error occurs in parsing
     */
    @Override
    public FeatureModel parse(@NonNull File filePath) throws FeatureModelParserException {
        checkArgument(checkFormat(filePath), "The format of file is not SPLOT format or there exists errors in the file!");

        log.info("Parsing the feature model file {}", filePath.getName());
        LoggerUtils.indent();

        FeatureModel featureModel;
        try {
            fm.FeatureModel sxfm = new XMLFeatureModel(filePath.toString(), XMLFeatureModel.USE_VARIABLE_NAME_AS_ID);

            // Load the XML file and create
            sxfm.loadModel();

            // create the feature model
            featureModel = new FeatureModel();

            featureModel.setName(sxfm.getName());
            // convert features
            convertFeatures(sxfm, featureModel);

            if (featureModel.getNumOfFeatures() == 0) {
                throw new FeatureModelParserException("Couldn't parse any features in the feature model file!");
            }

            // convert relationships
            convertRelationships(sxfm, featureModel);

            // convert constraints
            convertConstraints(sxfm, featureModel);
        } catch (FeatureModelException | at.tugraz.ist.ase.fm.core.FeatureModelException ex) {
            throw new FeatureModelParserException(ex.getMessage());
        }

        LoggerUtils.outdent();
        log.info("Parsing the feature model file {} is done!", filePath.getName());
        return featureModel;
    }

    /**
     * Iterate nodes to take features.
     *
     * @param sxfm - a {@link fm.FeatureModel}
     */
    private void convertFeatures(fm.FeatureModel sxfm, FeatureModel featureModel) throws FeatureModelParserException {
        log.debug("{}Generating features...", LoggerUtils.tab);
        LoggerUtils.indent();

        Queue<FeatureTreeNode> queue = new LinkedList<>();
        queue.add(sxfm.getRoot());

        FeatureTreeNode node;
        while (!queue.isEmpty()) {
            node = queue.remove();

            if ((node instanceof RootNode)
                    || (node instanceof SolitaireFeature)
                    || (node instanceof GroupedFeature)) {
                String name = node.getName();
                String id = node.getID();

                if (name.isEmpty()) {
                    throw new FeatureModelParserException(node + " - The feature name could not be blank!");
                }
                if (id.isEmpty()) {
                    throw new FeatureModelParserException(node + " - The feature id could not be blank!");
                }
                featureModel.addFeature(name, id);
                log.debug("{}Feature '{}' with id '{}' is being parsed...", LoggerUtils.tab, name, id);
            }

            exploreChildren(queue, node);
        }

        LoggerUtils.outdent();
    }

    private void exploreChildren(Queue<FeatureTreeNode> queue, FeatureTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            FeatureTreeNode child = (FeatureTreeNode) node.getChildAt(i);
            queue.add(child);
        }
    }

    /**
     * Iterates nodes to take the relationships between features.
     *
     * @param sxfm - a {@link fm.FeatureModel}
     * @param featureModel - a {@link FeatureModel}
     * @throws FeatureModelParserException a ParserException
     */
    private void convertRelationships(fm.FeatureModel sxfm, FeatureModel featureModel) throws FeatureModelParserException, at.tugraz.ist.ase.fm.core.FeatureModelException {
        log.debug("{}Generating relationships...", LoggerUtils.tab);
        LoggerUtils.indent();

        Queue<FeatureTreeNode> queue = new LinkedList<>();
        queue.add(sxfm.getRoot());

        FeatureTreeNode node;
        while (!queue.isEmpty()) {
            node = queue.remove();

            Feature leftSide = null;
            List<Feature> rightSide = new ArrayList<>();
            RelationshipType type = null;

            if (node instanceof SolitaireFeature) {
                if (((SolitaireFeature) node).isOptional()) { // OPTIONAL
                    type = RelationshipType.OPTIONAL;
                    leftSide = featureModel.getFeature(node.getID());
                    rightSide.add(featureModel.getFeature(((FeatureTreeNode) node.getParent()).getID()));
                } else { // MANDATORY
                    type = RelationshipType.MANDATORY;
                    leftSide = featureModel.getFeature(((FeatureTreeNode) node.getParent()).getID());
                    rightSide.add(featureModel.getFeature(node.getID()));
                }
                featureModel.addRelationship(type, leftSide, rightSide);

                log.debug("{}Relationship '{}' parsed", LoggerUtils.tab, featureModel.getRelationships().get(featureModel.getNumOfRelationships() - 1).getConfRule());
            } else if (node instanceof FeatureGroup) {
                leftSide = featureModel.getFeature(((FeatureTreeNode) node.getParent()).getID());
                rightSide = getChildren(node);
                if (((FeatureGroup) node).getMax() == 1) { // ALTERNATIVE
                    type = RelationshipType.ALTERNATIVE;
                } else { // OR
                    type = RelationshipType.OR;
                }
                featureModel.addRelationship(type, leftSide, rightSide);

                log.debug("{}Relationship '{}' parsed", LoggerUtils.tab, featureModel.getRelationships().get(featureModel.getNumOfRelationships() - 1).getConfRule());
            }

            exploreChildren(queue, node);
        }

        LoggerUtils.outdent();
    }

    /**
     * Converts constraints on the file into constraints in {@link FeatureModel}
     *
     * @param sxfm - a {@link fm.FeatureModel}
     * @param featureModel - a {@link FeatureModel}
     * @throws FeatureModelParserException a ParserException
     */
    private void convertConstraints(fm.FeatureModel sxfm, FeatureModel featureModel) throws FeatureModelParserException, at.tugraz.ist.ase.fm.core.FeatureModelException {
        log.debug("{}Generating constraints...", LoggerUtils.tab);
        LoggerUtils.indent();

        for (PropositionalFormula formula : sxfm.getConstraints()) {
            BooleanVariable[] variables = formula.getVariables().toArray(new BooleanVariable[0]);

            if (variables.length == 2) {

                BooleanVariable leftSide = variables[0];
                BooleanVariable rightSide = variables[1];

                // take type
                RelationshipType type;
                if ((leftSide.isPositive() && !rightSide.isPositive())
                        || ((!leftSide.isPositive() && rightSide.isPositive()))) { // REQUIRES
                    type = RelationshipType.REQUIRES;
                } else if (!leftSide.isPositive() && !rightSide.isPositive()) { // EXCLUDES
                    type = RelationshipType.EXCLUDES;
                } else {
                    throw new FeatureModelParserException(formula + " is not supported constraints!");
                }

                Feature left;
                List<Feature> rightSideList = new LinkedList<>();
                if (!rightSide.isPositive()) {
                    // take rightSide
                    left = featureModel.getFeature(rightSide.getID());
                    rightSideList.add(featureModel.getFeature(leftSide.getID()));
                } else {
                    // take leftSide
                    left = featureModel.getFeature(leftSide.getID());
                    // take rightSide
                    rightSideList.add(featureModel.getFeature(rightSide.getID()));
                }

                featureModel.addConstraint(type, left, rightSideList);
            } else {
                // take type
                RelationshipType type = RelationshipType.ThreeCNF;

                StringBuilder constraint3CNF = new StringBuilder();
                for (BooleanVariable var : variables) {
                    if (!var.isPositive()) {
                        constraint3CNF.append("~");
                    }
                    constraint3CNF.append(sxfm.getNodeByID(var.getID()).getName()).append(" & ");
                }
                constraint3CNF = new StringBuilder(constraint3CNF.substring(0, constraint3CNF.length() - 3));

                featureModel.addConstraint(type, constraint3CNF.toString());
            }

            log.debug("{}Constraint '{}' parsed", LoggerUtils.tab, featureModel.getConstraints().get(featureModel.getNumOfConstraints() - 1).getConfRule());
        }

        LoggerUtils.outdent();
    }

    /**
     * Gets an array of names of child features.
     *
     * @param node - a node {@link FeatureTreeNode}
     * @return an array of names of child features.
     */
    private List<Feature> getChildren(FeatureTreeNode node) throws FeatureModelParserException {
        List<Feature> features = new LinkedList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            FeatureTreeNode child = (FeatureTreeNode)node.getChildAt(i);

            String name = child.getName();
            String id = child.getID();
            if (name.isEmpty()) {
                throw new FeatureModelParserException("The feature name could not be blank!");
            }
            Feature feature = new Feature(name, id);
            features.add(feature);
        }
        return features;
    }
}

