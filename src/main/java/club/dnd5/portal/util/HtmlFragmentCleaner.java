package club.dnd5.portal.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class HtmlFragmentCleaner {
	public String clean(String html) {
		if (html == null || html.trim().isEmpty()) {
			return html;
		}

		String fragment = stripEmbeddedDocument(html);
		Document document = Jsoup.parseBodyFragment(fragment);
		document.outputSettings().prettyPrint(false);
		unwrapInvalidParagraphBlocks(document.body());

		return document.body().html();
	}

	private String stripEmbeddedDocument(String html) {
		String lower = html.toLowerCase();
		int doctypeIndex = lower.indexOf("<!doctype");
		int htmlIndex = lower.indexOf("<html");
		if (htmlIndex >= 0 && lower.indexOf("<head", htmlIndex) < 0 && lower.indexOf("<body", htmlIndex) < 0) {
			htmlIndex = -1;
		}
		int documentIndex = -1;

		if (doctypeIndex >= 0 && htmlIndex >= 0) {
			documentIndex = Math.min(doctypeIndex, htmlIndex);
		} else if (doctypeIndex >= 0) {
			documentIndex = doctypeIndex;
		} else if (htmlIndex >= 0) {
			documentIndex = htmlIndex;
		}

		if (documentIndex < 0) {
			return html;
		}

		return html.substring(0, documentIndex);
	}

	private void unwrapInvalidParagraphBlocks(Element root) {
		for (Element paragraph : root.select("p")) {
			if (paragraph.selectFirst("ul, ol, table, details, div, p") != null) {
				paragraph.tagName("div");
			}
		}

		for (Element span : root.select("span")) {
			if (span.selectFirst("ul, ol, table, details, div, p") != null) {
				span.tagName("div");
			}
		}
	}
}
