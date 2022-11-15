package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.revwalk.RevCommit;

public class FullCommitMessage implements ThingWeAreInterestedIn<String> {
    @Override
    public String getName() {
        return "full commit message";
    }

    @Override
    public String getThing(RevCommit commit) {
        return commit.getFullMessage();
    }
}
