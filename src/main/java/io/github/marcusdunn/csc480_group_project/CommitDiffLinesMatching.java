package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CommitDiffLinesMatching implements ThingWeAreInterestedIn<Optional<List<String>>> {
    private final String name;
    private final DiffHelper diffHelper;
    private final Pattern pattern;

    public CommitDiffLinesMatching(String name, DiffHelper diffHelper, Pattern pattern) {
        this.name = name;
        this.diffHelper = diffHelper;
        this.pattern = pattern;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<List<String>> getThing(RevCommit commit) {
        final var diffFromParent = diffHelper.getDiffFromParent(commit);
        return diffFromParent.map(s -> s.lines().filter(pattern.asPredicate()).toList());
    }
}
