package org.github.schwibbes.testcounter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repo {

	private static final Logger logger = LoggerFactory.getLogger(Repo.class);

	public static final String DOT_GIT = ".git";

	public Git openRepo(Path repoDir, String repoURL, String branch)
			throws GitAPIException, InvalidRemoteException, TransportException, IOException {
		Git git;
		if (!Files.isDirectory(repoDir, LinkOption.NOFOLLOW_LINKS)
				|| !Files.isDirectory(repoDir.resolve(DOT_GIT), LinkOption.NOFOLLOW_LINKS)) {
			logger.info("cloning repo from: {} to '{}'", repoURL, repoDir);

			git = Git.cloneRepository()
					.setDirectory(repoDir.toFile())
					.setURI(repoURL)
					.setBranchesToClone(Collections.singleton(branch))
					.setBranch(branch)
					.call();
		} else {
			logger.info("reuse existing repo in'{}'", repoDir);
			final FileRepositoryBuilder builder = new FileRepositoryBuilder();
			builder.setGitDir(repoDir.resolve(DOT_GIT).toFile());
			git = new Git(builder.build());
			git.checkout().setName(branch).call();
			git.pull();
		}
		return git;
	}
}
