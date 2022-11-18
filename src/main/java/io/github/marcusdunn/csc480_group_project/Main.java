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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.marcusdunn.csc480_group_project.FancyRegexHaver.variableDeclaration;
import static io.github.marcusdunn.csc480_group_project.FancyRegexHaver.variableDeclarationsWithVar;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        final var remote = List.of(
                "https://github.com/dropwizard/dropwizard",
                "https://github.com/hibernate/hibernate-orm",
                "https://github.com/sofastack/sofa-jraft",
                "https://github.com/SeleniumHQ/selenium",
                "https://github.com/open-telemetry/opentelemetry-java",
                "https://github.com/vsilaev/tascalate-javaflow",
                "https://github.com/shopizer-ecommerce/shopizer",
                "https://github.com/eclipse/eclipse.jdt.ls",
                "https://github.com/elastic/elasticsearch",
                "https://github.com/gradle/gradle",
                "https://github.com/spring-projects/spring-framework",
                "https://github.com/google/error-prone",
                "https://github.com/apache/tomcat",
                "https://github.com/networknt/light-4j",
                "https://github.com/INRIA/spoon"
        );
        // https://www.oracle.com/java/technologies/java-se-support-roadmap.html
        final var java9ReleaseDate = LocalDate.of(2017, 8, 17);
        remote
                .stream()
                .map(s -> new Thread(new StatusLogger(() -> doTheThing(s, java9ReleaseDate), s)))
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
        logger.info(() -> "finished writing to " + fileName);
    }

    private static Stream<Stream<String>> getInterestingThings(
            Git git,
            LocalDate since,
            CommitDiff commitDiff
    ) {
        return getCommitsSince(git, since)
                .flatMap(commit -> commitDiff.data(commit)
                        .stream()
                        .flatMap(diff -> diff.lines()
                                .filter(variableDeclaration.asMatchPredicate())
                                .map(declaration -> Stream.of(
                                        declaration,
                                        variableDeclarationsWithVar.asMatchPredicate().test(declaration) ? "var" : "type",
                                        switch (declaration.charAt(0)) {
                                            case '+' -> "add";
                                            case '-' -> "remove";
                                            default -> "unknown";
                                        },
                                        getLocalDate(commit).toString(),
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
                    .filter(commit -> getLocalDate(commit).isAfter(since)
                    );
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private static LocalDate getLocalDate(RevCommit commit) {
        return commit
                .getAuthorIdent()
                .getWhen()
                .toInstant()
                .atZone(commit.getCommitterIdent().getZoneId())
                .toLocalDate();
    }
}
