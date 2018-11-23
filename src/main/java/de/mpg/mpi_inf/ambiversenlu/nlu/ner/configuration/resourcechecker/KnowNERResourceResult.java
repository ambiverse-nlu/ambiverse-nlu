package de.mpg.mpi_inf.ambiversenlu.nlu.ner.configuration.resourcechecker;

/**
 * Object which holds the outcome of the KnowNER check
  */
public class KnowNERResourceResult {
	private final boolean success;
	private final String message;

	public KnowNERResourceResult() {
		this.success = true;
		this.message = null;
	}

	public KnowNERResourceResult(String message) {
		this.success = false;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
}
