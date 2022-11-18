package io.github.marcusdunn.csc480_group_project;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        final var remote = getFromEnv("REMOTE").orElseThrow().split(";");
        final var since = LocalDate.now().minusYears(2);
        Arrays
                .stream(remote)
                .map(s -> new Thread(() -> doTheThing(s, since)))
                .peek(Thread::start)
                .toList()
                .forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void doTheThing(String remote, LocalDate since) {
        final Path temp;
        try {
            temp = Files.createTempDirectory("csc480_group_project");
            logger.fine(() -> "created " + temp);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Failed to create temp directory");
            throw new RuntimeException(e);
        }
        logger.info("cloning " + remote);
        try (final var git = Git.cloneRepository().setURI(remote).setDirectory(temp.toFile()).call()) {
            logger.info("cloned " + remote);
            printRepoToCsv(git, since, temp);
        } catch (GitAPIException e) {
            logger.log(Level.SEVERE, e, () -> "Failed to open repo at " + remote);
        }
    }

    private static void printRepoToCsv(Git git, LocalDate since, Path temp) {
        String[] split = git.getRepository().getConfig().getString("remote", "origin", "url").split("/");
        final var fileName = split[split.length - 1].replace(".git", "") + ".csv";
        try (final var out = new FileWriter(fileName)) {
            logger.info(() -> "created " + fileName);
            final var csvFormat = CSVFormat.DEFAULT
                    .builder()
                    .setHeader("line", "declaration_type", "diff_type", "date_added", "indentation")
                    .build();
            try (final var csvPrinter = new CSVPrinter(out, csvFormat)) {
                final var interestingThings = getInterestingThings(git, since, new CommitDiff(new DiffHelper(git)));
                interestingThings.forEach(stream -> {
                    final var values = stream.toArray(Object[]::new);
                    logger.info(() -> Arrays.toString(values));
                    try {
                        logger.fine(() -> "adding record " + Arrays.toString(values));
                        csvPrinter.printRecord(values);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e, () -> "Failed to print a record: " + Arrays.toString(values));
                    }
                });
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, () -> "Failed to open a csvPrinter at: " + out);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Failed to open a fileWriter at:  output.csv");
        }
        try {
            Files.walkFileTree(temp, DeletingFileVisitor.INSTANCE);
            logger.fine(() -> "deleted " + temp);
        } catch (IOException e) {
            logger.warning("failed to delete temp directory " + temp);
        }
    }

    private static Stream<Stream<String>> getInterestingThings(
            Git git,
            LocalDate since,
            CommitDiff commitDiff
    ) {
        Pattern withVar = Pattern.compile(FancyRegexHaver.variableDeclarationsWithVar);
        Pattern withType = Pattern.compile(FancyRegexHaver.variableDeclarationsWithType);
        return getCommitsSince(git, since)
                .flatMap(commit -> commitDiff.data(commit)
                        .stream()
                        .flatMap(diff -> diff.lines()
                                .filter(withVar.asMatchPredicate().or(withType.asMatchPredicate()))
                                .map(declaration -> Stream.of(
                                        declaration,
                                        withVar.asMatchPredicate().test(declaration) ? "var" : "type",
                                        switch (declaration.charAt(0)) {
                                            case '+' -> "add";
                                            case '-' -> "remove";
                                            default -> "unknown";
                                        },
                                        commit.getAuthorIdent().getWhen().toInstant().atZone(ZonedDateTime.now().getZone()).toLocalDate().toString(),
                                        String.valueOf(declaration
                                                .replaceFirst("[+\\-]", "")
                                                .replace("\t", "    ")
                                                .chars()
                                                .takeWhile(c -> c == ' ')
                                                .count()
                                        )
                                ))
                        )
                );
    }

    private static Stream<RevCommit> getCommitsSince(Git git, LocalDate since) {
        final int CHUNK_SIZE = 100;
        try {
            return Stream
                    .iterate(Pair.of(
                                    CHUNK_SIZE,
                                    git
                                            .log()
                                            .setSkip(0)
                                            .setMaxCount(CHUNK_SIZE)
                                            .call()
                            ),
                            pair -> pair.second().iterator().hasNext(),
                            (pair) -> {
                                try {
                                    return Pair.of(
                                            pair.first() + CHUNK_SIZE,
                                            git.log().setMaxCount(CHUNK_SIZE).setSkip(pair.first()).call()
                                    );
                                } catch (GitAPIException e) {
                                    logger.log(Level.SEVERE, e, () -> "Failed to get commits since " + since);
                                    throw new RuntimeException(e);
                                }
                            }
                    )
                    .flatMap(pair -> StreamSupport
                            .stream(
                                    pair.second().spliterator(),
                                    false
                            )
                    )
                    .filter(commit -> commit
                            .getAuthorIdent()
                            .getWhen()
                            .toInstant()
                            .atZone(commit
                                    .getAuthorIdent()
                                    .getTimeZone()
                                    .toZoneId()
                            )
                            .toLocalDate()
                            .isAfter(since)
                    );
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<String> getFromEnv(String key) {
        final var value = System.getenv(key);
        if (value == null) {
            logger.warning(() -> "Environment variable " + key + " is not set");
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }
}
