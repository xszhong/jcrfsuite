package com.github.jcrfsuite;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.jcrfsuite.util.Pair;

public class CrfTrainerTest {
	private static final Path TRAINING_FOLDER = Paths.get(System.getProperty("user.dir"))
			.resolve("src")
			.resolve("test")
			.resolve("resources")
			.resolve("com")
			.resolve("github")
			.resolve("jcrfsuite")
			.resolve("trainer");

	private static final Path TRAINING_DATA_PATH = TRAINING_FOLDER
			.resolve("test_Jcrfsuite_training_data.tsv");

	private static final Path MODEL_PATH = TRAINING_FOLDER.resolve("test_Jcrfsuite_trained_model.crfsuite");

	@Before
	public void setUpTest() throws IOException {
		// Delete the model to make sure we make a new one.
		Files.deleteIfExists(MODEL_PATH);
	}

	@After
	public void teardownTest() throws IOException {
		// Delete the model to make sure we make a new one next time and to make extra sure it's not commited.
		Files.deleteIfExists(MODEL_PATH);
	}

	@Test
	public void testTrainStringString() throws IOException {
		String trainingDataPathString = TRAINING_DATA_PATH.toString();
		String modelPathString = MODEL_PATH.toString();
		CrfTrainer.train(trainingDataPathString, modelPathString);

		// Try it out.
		CrfTagger tagger = new CrfTagger(modelPathString);

		// Make sure the labels are correct.
		assertThat(tagger.getlabels(), contains("TEST", "O"));

		List<List<Pair<String, Double>>> tagging = tagger.tag(trainingDataPathString);
		// Make sure both got tagged.
		assertThat(tagging, hasSize(3));

		// Using ugly explicit tests to use closeTo.
		List<Pair<String, Double>> firstTest = tagging.get(0);
		assertThat(firstTest, hasSize(2));
		assertThat(firstTest.get(0).getFirst(), equalTo("TEST"));
		assertThat(firstTest.get(0).getSecond(), closeTo(0.682, 0.001));
		assertThat(firstTest.get(1).getFirst(), equalTo("O"));
		assertThat(firstTest.get(1).getSecond(), closeTo(0.597, 0.001));

		List<Pair<String, Double>> secondTest = tagging.get(1);
		assertThat(secondTest, hasSize(1));
		assertThat(secondTest.get(0).getFirst(), equalTo("TEST"));
		// Testing to make sure "test:3" got split properly by trainer and tester.
		double scoreForTripleFeature = 0.855;
		assertThat(secondTest.get(0).getSecond(), closeTo(scoreForTripleFeature, 0.001));

		List<Pair<String, Double>> thirdTest = tagging.get(2);
		assertThat(thirdTest, hasSize(1));
		assertThat(thirdTest.get(0).getFirst(), equalTo("TEST"));
		assertThat(thirdTest.get(0).getSecond(), closeTo(scoreForTripleFeature, 0.001));
	}
}
