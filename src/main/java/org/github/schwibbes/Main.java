package org.github.schwibbes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final String repoURL = "https://github.com/schwibbes/testing.git";
	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final Path repoDir = Paths.get("C:/temp/git");
	private static final Path dotgit = repoDir.resolve(".git");

	private static final String masterBranch = "master";
	private static final String firstCommitRevision = "abc";
	private static final String lastCommitRevision = "master";

	public static void main(String[] args) throws Exception {

		final Git git = openRepo();

		final ObjectId stopAt = git.getRepository().resolve(firstCommitRevision);

		try (ObjectReader reader = git.getRepository().newObjectReader();
				RevWalk revWalk = new RevWalk(git.getRepository());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DiffFormatter formatter = new DiffFormatter(out)) {

			formatter.setRepository(git.getRepository());
			final RevCommit stopAtCommit = revWalk.parseCommit(stopAt);

			for (final RevCommit commit : git.log().call()) {
				logger.info("{}", commit.getAuthorIdent().getEmailAddress());

				if (commit.getParentCount() == 0 || Objects.equals(stopAtCommit, commit)) {
					logger.info("stopping @ commit: {}", commit);
					break;
				}

				for (final DiffEntry entry : readDiffs(commit, reader, git)) {
					logger.info("Entry: " + entry);
					formatter.format(entry);
				}
			}

			final String[] lines = out.toString().split("\\r?\\n");

			for (final String l : lines) {
				if (l.startsWith("+") && l.contains("stream(")) {
					System.out.println(l);
				}
			}
		}

	}

	private static List<DiffEntry> readDiffs(RevCommit commit, ObjectReader reader, Git git) throws Exception {
		final CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		oldTreeIter.reset(reader, commit.getParent(0).getTree().getId());

		final CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		newTreeIter.reset(reader, commit.getTree().getId());

		return git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
	}

	private static Git openRepo() throws GitAPIException, InvalidRemoteException, TransportException, IOException {
		Git git;
		if (!Files.isDirectory(repoDir, LinkOption.NOFOLLOW_LINKS)
				|| !Files.isDirectory(dotgit, LinkOption.NOFOLLOW_LINKS)) {
			logger.info("cloning repo!");

			git = Git.cloneRepository()
					.setDirectory(repoDir.toFile())
					.setURI(repoURL)
					.setBranchesToClone(Collections.singleton(lastCommitRevision))
					.setBranch(lastCommitRevision)
					.call();
		} else {
			logger.info("reuse existing repo!");
			final FileRepositoryBuilder builder = new FileRepositoryBuilder();
			builder.setGitDir(dotgit.toFile());
			git = new Git(builder.build());
			git.checkout().setName(masterBranch).call();
			git.pull();
		}
		return git;
	}

}
