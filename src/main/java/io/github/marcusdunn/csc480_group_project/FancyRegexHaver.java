package io.github.marcusdunn.csc480_group_project;

/**
 * Java let me store strings outside of a class.
 */
public class FancyRegexHaver {
    public static final String identifier = "(?:\\b[_a-zA-Z]|\\B$)[_$a-zA-Z0-9]*";
    public static final String variableDeclarationsWithVar = ".*(final\\s+)?var " + identifier + "\\s*=\\s*";
    public static String variableDeclarationsWithType = ".*(final\\s+)?(?!var)[_a-zA-Z][_$a-zA-Z0-9<>]*\\s+" + identifier + "\\s*=\\s*.*";
}
