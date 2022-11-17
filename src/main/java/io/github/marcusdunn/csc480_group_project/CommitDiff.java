package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Optional;

public class CommitDiff implements ThingWeAreInterestedIn<Optional<String>> {
    private final DiffHelper diffHelper;

    public CommitDiff(DiffHelper diffHelper) {
        this.diffHelper = diffHelper;
    }

    @Override
    public String getName() {
        return "commit diff";
    }

    @Override
    public Optional<String> getThing(RevCommit commit) {
        return diffHelper.getDiffFromParent(commit);
    }

}
