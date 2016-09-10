package org.github.schwibbes.testcounter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppConfig {

	private String property;
	private Path repoDir;
	private String repoUrl;

	private List<String> keywords;
	private String branch;
	private LocalDate fromDate;

	private LocalDate toDate;

	public AppConfig() {

		try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("app.properties")) {
			final Properties prop = new Properties();
			prop.load(in);

			repoDir = Paths.get(prop.getProperty("repoDir"));
			repoUrl = prop.getProperty("repoUrl");
			keywords = Arrays.asList(prop.getProperty("keywords").split(","));
			branch = prop.getProperty("branch");

			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			fromDate = LocalDate.parse(prop.getProperty("fromDate"), formatter);
			toDate = LocalDate.parse(prop.getProperty("toDate"), formatter);

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getProperty() {
		return property;
	}

	public Path getRepoDir() {
		return repoDir;
	}

	public String getRepoUrl() {
		return repoUrl;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public String getBranch() {
		return branch;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}

}
