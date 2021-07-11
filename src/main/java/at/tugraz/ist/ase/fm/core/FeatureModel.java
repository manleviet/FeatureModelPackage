/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

import java.util.ArrayList;

import static at.tugraz.ist.ase.fm.core.Utilities.isExistInArrayList;

/**
 * Represents a feature model
 * ver 1.0 - support basic feature models
 *
 * @author Viet-Man Le (vietman.le@ist.tugraz.at)
 */
public class FeatureModel {
    public final String version = "1.0"; // support basic feature models

    private ArrayList<Feature> bfFeatures; // breadth-first order
    private ArrayList<Relationship> relationships;
    private ArrayList<Relationship> constraints;

    private boolean consistency;

    /**
     * A constructor
     */
    public FeatureModel() {
        bfFeatures = new ArrayList<Feature>();
        relationships = new ArrayList<Relationship>();
        constraints = new ArrayList<Relationship>();
        consistency = false;
    }

    /**
     * Sets the consistency
     * @param consistency of this feature model
     */
    public void setConsistency(boolean consistency) {
        this.consistency = consistency;
    }

    /**
     * Gets the consistency of this feature model
     * @return true if the feature model is consistent, false otherwise
     */
    public boolean isConsistency() {
        return consistency;
    }

    /**
     * Gets the name of the feature model. This name is also the root feature's name.
     * @return name of the feature model.
     */
    public String getName() {
        return bfFeatures.get(0).getName();
    }

    /**
     * Checks whether a given name is different from other feature names.
     * @param name
     * @return true if the given name is unique, false otherwise
     */
    private boolean isUniqueFeatureName(String name) {
        for (Feature f: bfFeatures) { // TODO - improve the performance
            if (f.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a new feature
     * @param fname name of the feature
     * @throws FeatureModelException
     */
    public void addFeature(String fname) throws FeatureModelException {
        // Checks blank fname
        if (fname.isEmpty()) {
            throw new FeatureModelException("The feature name can't be blank.");
        }

        // Checks the existence of fname in the feature model
        if (!isUniqueFeatureName(fname)) {
            StringBuilder st = new StringBuilder("The feature name " + fname.toUpperCase() + " is used many times in the feature model.");
            st.append("\n\n").append("The feature name must be unique.");

            throw new FeatureModelException(st.toString());
        }

        Feature f = new Feature(fname);
        this.bfFeatures.add(f);
    }

    /**
     * Adds multiple features
     * @param fnames an array of feature's names
     * @throws FeatureModelException
     */
    public void addFeatures(String[] fnames) throws FeatureModelException {
        for (String fname: fnames) {
            addFeature(fname);
        }
    }

    /**
     * Gets all {@link Feature}s of the feature model.
     * @return an array of {@link Feature}s.
     */
    public ArrayList<Feature> getFeatures(){
        return bfFeatures;
    }

    /**
     * Gets the {@link Feature} at a given index.
     * @param index
     * @return a {@link Feature}
     * @throws FeatureModelException
     */
    public Feature getFeature(int index) throws FeatureModelException {
        if (index < 0 || index >= bfFeatures.size()) throw new FeatureModelException("The index is out of bounds!");

        return bfFeatures.get(index);
    }

    /**
     * Gets the {@link Feature} which has a given name.
     * @param name
     * @return a {@link Feature}
     * @throws FeatureModelException
     */
    public Feature getFeature(String name) throws FeatureModelException {
        for (Feature f: bfFeatures) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        throw new FeatureModelException("Feature " + name + "doesn't exist!");
    }

    /**
     * Gets the number of features
     * @return the number of features
     */
    public int getNumOfFeatures() {
        return bfFeatures.size();
    }

    /**
     * Checks whether the given {@link Feature} is mandatory.
     * @param feature a {@link Feature}
     * @return true if the given {@link Feature} is mandatory, false otherwise.
     */
    public boolean isMandatoryFeature(Feature feature) {
        for (Relationship r : relationships) {
            switch (r.getType()) {
                case MANDATORY:
                    if (r.belongsToRightSide(feature)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    /**
     * Checks whether the given {@link Feature} is optional.
     * @param feature a {@link Feature}
     * @return true if the given {@link Feature} is optional, false otherwise.
     */
    public boolean isOptionalFeature(Feature feature) {
        for (Relationship r : relationships) {
            switch (r.getType()) {
                case OPTIONAL:
                    if (r.belongsToLeftSide(feature)) {
                        return true;
                    }
                    break;
                case OR:
                case ALTERNATIVE:
                    if (r.belongsToRightSide(feature)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    /**
     * Gets all {@link Feature}s participating in the right side of the constraint
     * in which the left side is the given {@link Feature}.
     * @param leftSide
     * @return an array of {@link Feature}s
     * @throws FeatureModelException
     */
    public ArrayList<Feature> getRightSideOfRelationships(Feature leftSide) throws FeatureModelException {
        ArrayList<Feature> children = new ArrayList<>();
        for (Relationship r : relationships) {
            if (r.getType() == Relationship.RelationshipType.OPTIONAL) {
                if (r.belongsToRightSide(leftSide)) {
                    String left = r.getLeftSide();
                    Feature parent = getFeature(left);
                    if (parent != null) {
                        children.add(parent);
                    }
                }
            } else {
                if (r.belongsToLeftSide(leftSide)) {
                    ArrayList<String> rightSide = r.getRightSide();
                    for (String right : rightSide) {
                        Feature child = getFeature(right);
                        if (child != null) {
                            children.add(child);
                        }
                    }
                }
            }
        }
        return children;
    }

    /**
     * Gets all {@link Relationship}s in which the given {@link Feature} participates.
     * @param feature
     * @return an array of {@link Relationship}s.
     */
    private ArrayList<Relationship> getRelationshipsWith(Feature feature) {
        ArrayList<Relationship> rs = new ArrayList<>();
        for (Relationship r : relationships) {
            if (r.belongsToRightSide(feature) || r.belongsToLeftSide(feature)) {
                rs.add(r);
            }
        }
        for (Relationship r : constraints) {
            if (!r.isType(Relationship.RelationshipType.SPECIAL)) {
                if (r.belongsToRightSide(feature) || r.belongsToLeftSide(feature)) {
                    rs.add(r);
                }
            }
            // TODO - 3CNF
        }
        return rs;
    }

    /**
     * Gets all parent {@link Feature}s of the given {@link Feature}.
     * @param rightSide a {@link Feature}.
     * @return an array of {@link Feature}s.
     * @throws FeatureModelException
     */
    public ArrayList<Feature> getMandatoryParents(Feature rightSide) throws FeatureModelException {
        ArrayList<Feature> parents = new ArrayList<>();

        ArrayList<Relationship> relationships = getRelationshipsWith(rightSide);
        for (Relationship r : relationships) {
            ArrayList<String> parentsqueue = new ArrayList<>();
            if (r.getType() == Relationship.RelationshipType.REQUIRES) {
                if (r.belongsToRightSide(rightSide)) {
                    parentsqueue.add(rightSide.toString());
                    getMandatoryParent(r, rightSide, parents, parentsqueue);
                }
            } else if (r.getType() == Relationship.RelationshipType.ALTERNATIVE
                    || r.getType() == Relationship.RelationshipType.OR) {
                if (r.belongsToRightSide(rightSide) || r.belongsToLeftSide(rightSide)) {
                    parentsqueue.add(rightSide.toString());
                    getMandatoryParent(r, rightSide, parents, parentsqueue);
                }
            } // TODO - 3CNF
        }

        return parents;
    }

    private void getMandatoryParent(Relationship r, Feature feature, ArrayList<Feature> parents, ArrayList<String> parentsqueue) throws FeatureModelException {
        if (feature.toString().equals(this.getName())) return;

        if (r.getType() == Relationship.RelationshipType.REQUIRES) {
            String left = r.getLeftSide();
            Feature parent = getFeature(left);

            if (parent.getName().equals(this.getName())) return;
            if (isExistInArrayList(parentsqueue, left)) return;

            if (this.isMandatoryFeature(parent)) {
                if (!parents.contains(parent)) {
                    parents.add(parent);
                }
            } else {

                ArrayList<Relationship> relationships = getRelationshipsWith(parent);
                for (Relationship r1 : relationships) {
                    if (r1.getType() == Relationship.RelationshipType.REQUIRES) {
                        if (r1.belongsToRightSide(parent)) {
                            parentsqueue.add(left);
                            getMandatoryParent(r1, parent, parents, parentsqueue);
                            parentsqueue.remove(parentsqueue.size() - 1);
                        }
                    } else if (r1.getType() == Relationship.RelationshipType.ALTERNATIVE
                            || r1.getType() == Relationship.RelationshipType.OR) {
                        if (r1.belongsToRightSide(parent) || r1.belongsToLeftSide(parent)) {
                            parentsqueue.add(left);
                            getMandatoryParent(r1, parent, parents, parentsqueue);
                            parentsqueue.remove(parentsqueue.size() - 1);
                        }
                    }
                }
            }
        } else if (r.getType() == Relationship.RelationshipType.ALTERNATIVE
                || r.getType() == Relationship.RelationshipType.OR) {
            if (r.belongsToRightSide(feature)) {
                String left = r.getLeftSide();
                Feature parent = getFeature(left);

                if (parent.getName().equals(this.getName())) return;
                if (isExistInArrayList(parentsqueue,left)) return;

                if (this.isMandatoryFeature(parent)) {
                    if (!parents.contains(parent)) {
                        parents.add(parent);
                    }
                } else {
                    ArrayList<Relationship> relationships = getRelationshipsWith(parent);
                    for (Relationship r1 : relationships) {
                        if (r1.getType() == Relationship.RelationshipType.REQUIRES) {
                            if (r1.belongsToRightSide(parent)) {
                                parentsqueue.add(left);
                                getMandatoryParent(r1, parent, parents, parentsqueue);
                                parentsqueue.remove(parentsqueue.size() - 1);
                            }
                        } else if (r1.getType() == Relationship.RelationshipType.ALTERNATIVE
                                || r1.getType() == Relationship.RelationshipType.OR) {
                            if (r1.belongsToRightSide(parent) || r1.belongsToLeftSide(parent)) {
                                parentsqueue.add(left);
                                getMandatoryParent(r1, parent, parents, parentsqueue);
                                parentsqueue.remove(parentsqueue.size() - 1);
                            }
                        }
                    }
                }
            } else if (r.belongsToLeftSide(feature)) {
                ArrayList<String> lefts = r.getRightSide();
                for (String left: lefts) {

                    if (isExistInArrayList(parentsqueue,left)) return;

                    Feature parent = getFeature(left);
                    if (parent.getName().equals(this.getName())) return;

                    if (this.isMandatoryFeature(parent)) {
                        if (!parents.contains(parent)) {
                            parents.add(parent);
                        }
                    } else {
                        ArrayList<Relationship> relationships = getRelationshipsWith(parent);
                        for (Relationship r1 : relationships) {
                            if (r1.getType() == Relationship.RelationshipType.REQUIRES) {
                                if (r1.belongsToRightSide(parent)) {
                                    parentsqueue.add(left);
                                    getMandatoryParent(r1, parent, parents, parentsqueue);
                                    parentsqueue.remove(parentsqueue.size() - 1);
                                }
                            } else if (r1.getType() == Relationship.RelationshipType.ALTERNATIVE
                                    || r1.getType() == Relationship.RelationshipType.OR) {
                                if (r1.belongsToRightSide(parent) || r1.belongsToLeftSide(parent)) {
                                    parentsqueue.add(left);
                                    getMandatoryParent(r1, parent, parents, parentsqueue);
                                    parentsqueue.remove(parentsqueue.size() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a new relationship to the feature model.
     * @param type type of the relationship
     * @param leftSide the left part of relationship
     * @param rightSide the right part of relationship
     */
    public void addRelationship(Relationship.RelationshipType type, String leftSide, String[] rightSide) {
        ArrayList<String> rightSideAL = convertArray2ArrayList(rightSide);

        Relationship r = new Relationship(type, leftSide, rightSideAL);
        this.relationships.add(r);
    }

    /**
     * Gets all {@link Relationship}s
     * @return an array of {@link Relationship}s
     */
    public ArrayList<Relationship> getRelationships() {
        return relationships;
    }

    /**
     * Gets the number of relationships.
     * @return number of relationships
     */
    public int getNumOfRelationships() {
        return relationships.size();
    }

    /**
     * Gets the number of relationships with the specific type.
     * @param type type of relationships
     * @return number of relationships with the specific type.
     */
    public int getNumOfRelationships(Relationship.RelationshipType type) {
        int count = 0;
        if (type == Relationship.RelationshipType.REQUIRES || type == Relationship.RelationshipType.EXCLUDES) {
            for (Relationship relationship : constraints) {
                if (relationship.isType(type)) {
                    count++;
                }
            }
        } else {
            for (Relationship relationship : relationships) {
                if (relationship.isType(type)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Adds a new constraint
     * @param type type of relationship
     * @param leftSide left part of the constraint
     * @param rightSide right part of the constraint
     */
    public void addConstraint(Relationship.RelationshipType type, String leftSide, String[] rightSide) {
        ArrayList<String> rightSideAL = convertArray2ArrayList(rightSide);

        Relationship r = new Relationship(type, leftSide, rightSideAL);
        this.constraints.add(r);
    }

    /**
     * Adds a new 3CNF constraint
     * @param type type of relationship
     * @param constraint3CNF 3CNF constraint
     */
    public void addConstraint(Relationship.RelationshipType type, String constraint3CNF) {
        Relationship r = new Relationship(type, constraint3CNF);
        this.constraints.add(r);
    }

    /**
     * Gets all constraints.
     * @return an array of {@link Relationship}s.
     */
    public ArrayList<Relationship> getConstraints() {
        return constraints;
    }

    /**
     * Gets the number of constraints.
     * @return number of constraints.
     */
    public int getNumOfConstraints() {
        return constraints.size();
    }

    @Override
    public String toString() {
        if (bfFeatures.isEmpty()) return "";

        StringBuilder st = new StringBuilder();

        st.append("FEATURES:\n");
        for (Feature feature : bfFeatures) {
            st.append(String.format("\t%s\n", feature));
        }

        st.append("RELATIONSHIPS:\n");
        for (Relationship relationship: relationships) {
            st.append(String.format("\t%s\n", relationship.getConfRule()));
        }

        st.append("CONSTRAINTS:\n");
        for (Relationship constraint: constraints) {
            st.append(String.format("\t%s\n", constraint.getConfRule()));
        }

        return st.toString();
    }

    private ArrayList<String> convertArray2ArrayList(String[] arrStr) {
        ArrayList<String> arrList = new ArrayList<String>();
        for(String name: arrStr) {
            arrList.add(name);
        }
        return arrList;
    }
}

