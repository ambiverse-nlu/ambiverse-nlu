package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RawWikiCorpusExtractor {

	private static final Logger logger = LoggerFactory.getLogger(RawWikiCorpusExtractor.class);

	static String extractFirstParagraphs(Document document) {
		Elements paragraphs = document.select("p");
		StringBuilder sb = new StringBuilder();
		for (Element p : paragraphs) {
			if (!p.parent().hasClass("mw-parser-output") || !p.children().isEmpty() && p.child(0).attr("style").equals("display:none")) {
				continue;
			}
			sb.append(p.text()).append("\n");

			if ( p.nextElementSibling() != null && (p.nextElementSibling().className().startsWith("toc") ||
					!p.nextElementSibling().nodeName().equals("p") && !p.nextElementSibling().attr("style").equals("display:none"))) {
				break;
			}
//
//            if (p.className().startsWith("toc")) {
//                break;
//            }
		}
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		Path directory = Paths.get(args[0]);
		if (!Files.exists(directory)) {
			Files.createDirectory(directory);
		}
		List<String> wikipediaLinks = new ArrayList<>();
		wikipediaLinks.add("Philadelphia_Phillies");
		wikipediaLinks.add("Chicago");
		wikipediaLinks.add("Leonardo_da_Vinci");
		wikipediaLinks.add("Albert_Einstein");
		wikipediaLinks.add("William_Shakespeare");
		wikipediaLinks.add("Delhi");
		wikipediaLinks.add("Russland");
		wikipediaLinks.add("Platon");
		wikipediaLinks.add("Amazonas");
		wikipediaLinks.add("Napoleon_Bonaparte");
		wikipediaLinks.add("ABBA");
		wikipediaLinks.add("Sagrada_Família");
		wikipediaLinks.add("Mao_Zedong");
		for (String link : wikipediaLinks) {
			try {
//                String decodedLink = URLDecoder.decode(link, "UTF-8");
				Document document = Jsoup.connect("https://de.wikipedia.org/wiki/" + link).get();
				String docTitle0 = document.select("h1#firstHeading").first().text();
				String docText = extractFirstParagraphs(document);
				Files.write(Paths.get(directory.toString(), docTitle0 + ".txt"), docText.getBytes());
			} catch (IOException e) {
				logger.warn("Error during processing link " + link, e);
			}
		}
	}
}