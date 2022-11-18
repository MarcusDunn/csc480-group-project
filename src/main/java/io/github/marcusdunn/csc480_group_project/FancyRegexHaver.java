package io.github.marcusdunn.csc480_group_project;

import java.util.regex.Pattern;

/**
 * Java let me store strings outside of a class.
 */
public class FancyRegexHaver {

    public static final Pattern variableDeclarationsWithVar = Pattern.compile(".*var\\s+[_a-zA-Z][_$a-zA-Z0-9]*\\s*=.*");

    public static final Pattern variableDeclaration = Pattern.compile("[^<#{]*[_a-zA-Z][_$a-zA-Z0-9<>]*\\s+[_a-zA-Z][_$a-zA-Z0-9]*\\s*=\\s*.*");
}
