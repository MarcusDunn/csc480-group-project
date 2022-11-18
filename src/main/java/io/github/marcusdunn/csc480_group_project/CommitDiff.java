package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Optional;

public class CommitDiff {
    private final DiffHelper diffHelper;

    public CommitDiff(DiffHelper diffHelper) {
        this.diffHelper = diffHelper;
    }

    public String getName() {
        return "commit diff";
    }

    public Optional<String> data(RevCommit commit) {
        return diffHelper.getDiffFromParent(commit);
    }
}
