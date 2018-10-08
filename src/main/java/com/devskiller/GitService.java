package com.devskiller;

import java.io.File;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

@Slf4j
public class GitService {

	public void cloneRepository(final File source, File destination) {
		Mono.fromRunnable(() -> doClone("file://" + source.toString(), destination))
				.retryWhen(Retry.anyOf(IllegalStateException.class).fixedBackoff(Duration.ofMillis(20)).retryMax(10))
				.subscribe();
		log.info("Repository cloned");
	}


	private void doClone(String source, final File destination) {
		try {
			Git.cloneRepository().setURI(source).setDirectory(destination).call();
			log.info("*** SUCCESS! ***");
		} catch (GitAPIException e) {
			throw new IllegalStateException(e);
		}
	}

}
