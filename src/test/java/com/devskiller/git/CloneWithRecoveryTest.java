package com.devskiller.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.devskiller.GitService;

import static org.awaitility.Awaitility.await;

@Slf4j
public class CloneWithRecoveryTest {

	public static final String TEST_FILE = "testfile";
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File source;
	private File destination;
	private GitService gitService  = new GitService();

	@Before
	public void setUp() throws Exception {
		source = temporaryFolder.newFolder("source");
		try (Git git = Git.init().setDirectory(source).setBare(false).call()) {
			addContent(git, TEST_FILE);
		}

		log.info("Source {}", source);
		Files.list(source.toPath())
				.forEach(path -> log.info(">: {}", path.toString()));

		destination = temporaryFolder.newFolder("destination");
		log.info("Destination {}", destination);
	}

	@Test
	public void shouldCloneRepository() throws IOException {
		log.info("Start cloning test...");

		new Thread(() -> gitService.cloneRepository(source, destination)).start();
		new Thread(() -> gitService.cloneRepository(source, destination)).start();
		new Thread(() -> gitService.cloneRepository(source, destination)).start();
		new Thread(() -> gitService.cloneRepository(source, destination)).start();

		File clonedFile = destination.toPath().resolve(TEST_FILE).toFile();
		log.info("Verify that cloned file exists: {}", clonedFile);
		await().ignoreExceptions().atMost(3, TimeUnit.MINUTES).until(clonedFile::exists);

		Files.list(destination.toPath())
				.forEach(path -> log.info("File: {}", path.toString()));
		log.info("Finish cloning test!");
	}

	private void addContent(Git git, final String someFile) throws IOException, GitAPIException {
		File testFile = new File(source, TEST_FILE);
		testFile.createNewFile();
		git.add()
				.addFilepattern(someFile)
				.call();
		git.commit().setMessage("Test file added").call();
	}


}
