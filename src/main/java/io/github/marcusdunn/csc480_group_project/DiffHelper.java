package io.github.marcusdunn.csc480_group_project;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class DiffHelper {
    private final Git git;
    private final CanonicalTreeParser parentTreeParser = new CanonicalTreeParser();

    public DiffHelper(Git git) {
        this.git = git;
    }

    public Optional<String> getDiffFromParent(RevCommit commit) {
        final var currentTreeParser = new CanonicalTreeParser();
        try {
            currentTreeParser.reset(git.getRepository().newObjectReader(), commit.getTree());

            final var parent = getParent(commit);
            if (parent.isEmpty()) {
                return Optional.empty();
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                final var diffFormatter = new DiffFormatter(new BufferedOutputStream(byteArrayOutputStream));
                diffFormatter.setRepository(git.getRepository());
                parentTreeParser.reset(git.getRepository().newObjectReader(), parent.get());
                diffFormatter.format(currentTreeParser, parentTreeParser);
                return Optional.of(byteArrayOutputStream.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<RevTree> getParent(RevCommit commit) {
        try {
            return Optional.of(commit.getParent(0).getTree());
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}
