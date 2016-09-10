package org.github.schwibbes.testcounter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final AppConfig config = new AppConfig();

	public static void main(String[] args) throws Exception {

		final Git git = new Repo().openRepo(config.getRepoDir(), config.getRepoUrl(), config.getBranch());
		final DiffReader diffReader = new DiffReader(git);

		final List<RevCommit> commits = new ArrayList<>();

		git.log().all().call().forEach(commit -> {

			final LocalDate date = Instant.ofEpochMilli( //
					commit.getAuthorIdent().getWhen().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

			if (commit.getParentCount() == 1 && date.isBefore(config.getToDate())
					&& date.isAfter(config.getFromDate())) {
				commits.add(commit);
			}

		});

		logger.info("found {} commits.", commits.size());

		final Map<String, List<CommitData>> result = commits.stream() //
				.map(x -> new CommitData(x, countTests(x, diffReader))) //
				.collect(Collectors.groupingBy( //
						x -> x.commit.getAuthorIdent().getEmailAddress()));

		result.keySet().forEach(person -> {

			final Optional<Integer> totalCount = result.get(person)
					.stream() //
					.peek(count -> logger.debug("{}", count.testsWritten)) //
					.map(count -> count.testsWritten) //
					.reduce((a, b) -> a + b);

			if (!totalCount.isPresent()) {
				logger.error("no commits found for user {}", person);
			} else {
				logger.info("Dev: '{}' -> Score: {}", person, totalCount.get());
			}
		});

	}

	private static int countTests(RevCommit commit, DiffReader diffReader) {

		String[] lines = new String[0];
		try {
			lines = diffReader.getDiff(commit);
		} catch (final Exception e) {
			logger.error("error while reading diff", e);
		}

		int result = 0;
		for (final String l : lines) {
			if (l.startsWith("+") && lineMatchesKeyword(l)) {
				logger.debug(l);
				result++;
			}
		}

		return result;
	}

	private static boolean lineMatchesKeyword(final String l) {
		return config.getKeywords().stream().anyMatch(l::contains);
	}

	private static class CommitData {
		RevCommit commit;
		int testsWritten = 0;

		public CommitData(RevCommit commit, int testsWritten) {
			this.commit = commit;
			this.testsWritten = testsWritten;
		}
	}

}
