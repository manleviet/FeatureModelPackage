/*
 * at.tugraz.ist.ase.fm - A Maven package for feature models
 *
 * Copyright (c) 2021.
 *
 * @author: Viet-Man Le (vietman.le@ist.tugraz.at)
 */

package at.tugraz.ist.ase.fm.core;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods
 *
 * @author Viet-Man Le (vietman.le@ist.tugraz.at)
 */
public class Utilities {
    /**
     * Replaces special characters (e.g. space, -) by underscore characters.
     *
     * @param st - a string needed to replace
     * @return a new string in which the special characters are replaced
     */
    public static String replaceSpecialCharactersByUnderscore(String st) {
        return st.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Converts an array to a string with a given separator.
     *
     * @param array - an array of strings
     * @param separator - a separator
     * @return a new string includes all strings in the array which is separated by the given separator.
     */
    public static String createStringFromArrayWithSeparator(ArrayList<String> array, String separator) {
        StringBuilder str = new StringBuilder();
        for (String s: array) {
            if (separator.equals("+") || separator.equals("*1+") || separator.equals(" or ")) {
                s = replaceSpecialCharactersByUnderscore(s);
                str.append(String.format("%s%s", s, separator));
            } else if (separator.equals("\n")) {
                str.append(String.format("\t[%s]%s", s, separator));
            } else {
                str.append(String.format("%s%s ", s, separator));
            }
        }
        // delete several residual words in the postfix of the string
        String s = "";
        if (separator.equals("+")) {
            s = str.substring(0, str.length() - 1);
        } else if (separator.equals("*1+")) {
            s = str.substring(0, str.length() - 3);
        } else if (separator.equals(" or ")) {
            s = str.substring(0, str.length() - 4);
        } else if (separator.equals("\n")) {
            s = str.substring(0, str.length() - 1);
        } else {
            s = str.substring(0, str.length() - 2);
        }
        return s;
    }

    // TODO - replace by contains ?
    /**
     * Checks whether if a string exists in an array of strings.
     *
     * @param list - an array of strings
     * @param st - a string will be checked
     * @return true if the string exists in the array, false otherwise
     */
    public static boolean isExistInArrayList(ArrayList<String> list, String st) {
        for (String s: list) {
            if (s.equals(st))
                return true;
        }
        return false;
    }

    /**
     * Prints all constraints of a Choco model to the console
     *
     * @param model - a Choco model
     */
    public static void printConstraints(Model model) {
        List<Constraint> ac = Arrays.asList(model.getCstrs());
        ac.forEach(System.out::println);
    }

    public static void checkAndCreateFolder(String path) {
        File folder = new File(path);

        // check whether the fms folder does not exist
        if (Files.notExists(Paths.get(path))) {
            folder.mkdir(); // if not, create it
        }
    }
}

