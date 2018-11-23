package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERResourceChecker;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker.KnowNERResourceResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KnowNERLanguageConfiguratorTest {

	@Test
	public void shouldHaveNoErrorMessagesWhenAllResourcesThere() throws KnowNERLanguageConfiguratorException {
		KnowNERResourceChecker mockedChecker = mock(KnowNERResourceChecker.class);
		when(mockedChecker.check()).thenReturn(new KnowNERResourceResult());
		KnowNERLanguageConfigurator configurator = new KnowNERLanguageConfiguratorBuilder()
				.setLanguage("de")
				.setMainDir("")
				.addResourceChecker(mockedChecker)
				.create();

		List<String> errorMessages = configurator.run();

		assertEquals(0, errorMessages.size());

	}

	@Test
	public void shoulHavedOneErrorMessageWhenResourceIsMissing() throws KnowNERLanguageConfiguratorException {
		KnowNERResourceChecker mockedChecker = mock(KnowNERResourceChecker.class);
		when(mockedChecker.check()).thenReturn(new KnowNERResourceResult("Resource is missing"));
		KnowNERLanguageConfigurator configurator = new KnowNERLanguageConfiguratorBuilder()
				.setLanguage("de")
				.setMainDir("")
				.addResourceChecker(mockedChecker)
				.create();

		List<String> errorMessages = configurator.run();

		assertEquals(1, errorMessages.size());

	}

}