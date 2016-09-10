package org.github.schwibbes.testcounter;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffReader {

	private static final Logger logger = LoggerFactory.getLogger(DiffReader.class);

	private final Git git;

	public DiffReader(Git git) {
		this.git = git;
	}

	public String[] getDiff(final RevCommit commit) throws Exception {

		try (ObjectReader reader = git.getRepository().newObjectReader();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DiffFormatter formatter = new DiffFormatter(out)) {

			formatter.setRepository(git.getRepository());

			for (final DiffEntry entry : readDiffs(commit, reader)) {
				logger.debug("Commit: {} -> File: {}", commit.getId().abbreviate(5).name(), entry.getNewPath());
				formatter.format(entry);
			}

			return out.toString().split("\\r?\\n");

		}
	}

	private List<DiffEntry> readDiffs(RevCommit commit, ObjectReader reader) throws Exception {
		final CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
		oldTreeIter.reset(reader, commit.getParent(0).getTree().getId());

		final CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
		newTreeIter.reset(reader, commit.getTree().getId());

		return git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
	}
}
