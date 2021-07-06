package component;

import java.util.*;
import java.util.Map.*;

import businessObject.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;

public class TextPreprocessor {

	private final static String SYM_STOPWORD = "%STOPWORD%";

	private Set<String> stopwords = new HashSet<>();

	public Set<String> getStopwords() {
		return stopwords;
	}

	private NerTreeNode nerTreeRoot = new NerTreeNode();

	public TextPreprocessor() {

		String[] stopwordArr = { "a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and",
				"any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below",
				"between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does",
				"doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further",
				"had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her",
				"here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn",
				"isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more",
				"most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of",
				"off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s",
				"same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some",
				"such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there",
				"these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was",
				"wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who",
				"whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll",
				"you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's",
				"here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's",
				"there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's",
				"when's", "where's", "who's", "why's", "would", "able", "abst", "accordance", "according",
				"accordingly", "across", "act", "actually", "added", "adj", "affected", "affecting", "affects",
				"afterwards", "ah", "almost", "alone", "along", "already", "also", "although", "always", "among",
				"amongst", "announce", "another", "anybody", "anyhow", "anymore", "anyone", "anything", "anyway",
				"anyways", "anywhere", "apparently", "approximately", "arent", "arise", "around", "aside", "ask",
				"asking", "auth", "available", "away", "awfully", "b", "back", "became", "become", "becomes",
				"becoming", "beforehand", "begin", "beginning", "beginnings", "begins", "behind", "believe", "beside",
				"besides", "beyond", "biol", "brief", "briefly", "c", "ca", "came", "cannot", "can't", "cause",
				"causes", "certain", "certainly", "co", "com", "come", "comes", "contain", "containing", "contains",
				"couldnt", "date", "different", "done", "downwards", "due", "e", "ed", "edu", "effect", "eg", "eight",
				"eighty", "either", "else", "elsewhere", "end", "ending", "enough", "especially", "et", "etc", "even",
				"ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "except", "f", "far", "ff",
				"fifth", "first", "five", "fix", "followed", "following", "follows", "former", "formerly", "forth",
				"found", "four", "furthermore", "g", "gave", "get", "gets", "getting", "give", "given", "gives",
				"giving", "go", "goes", "gone", "got", "gotten", "h", "happens", "hardly", "hed", "hence", "hereafter",
				"hereby", "herein", "heres", "hereupon", "hes", "hi", "hid", "hither", "home", "howbeit", "however",
				"hundred", "id", "ie", "im", "immediate", "immediately", "importance", "important", "inc", "indeed",
				"index", "information", "instead", "invention", "inward", "itd", "it'll", "j", "k", "keep", "keeps",
				"kept", "kg", "km", "know", "known", "knows", "l", "largely", "last", "lately", "later", "latter",
				"latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "line", "little", "'ll",
				"look", "looking", "looks", "ltd", "made", "mainly", "make", "makes", "many", "may", "maybe", "mean",
				"means", "meantime", "meanwhile", "merely", "mg", "might", "million", "miss", "ml", "moreover",
				"mostly", "mr", "mrs", "much", "mug", "must", "n", "na", "name", "namely", "nay", "nd", "near",
				"nearly", "necessarily", "necessary", "need", "needs", "neither", "never", "nevertheless", "new",
				"next", "nine", "ninety", "nobody", "non", "none", "nonetheless", "noone", "normally", "nos", "noted",
				"nothing", "nowhere", "obtain", "obtained", "obviously", "often", "oh", "ok", "okay", "old", "omitted",
				"one", "ones", "onto", "ord", "others", "otherwise", "outside", "overall", "owing", "p", "page",
				"pages", "part", "particular", "particularly", "past", "per", "perhaps", "placed", "please", "plus",
				"poorly", "possible", "possibly", "potentially", "pp", "predominantly", "present", "previously",
				"primarily", "probably", "promptly", "proud", "provides", "put", "q", "que", "quickly", "quite", "qv",
				"r", "ran", "rather", "rd", "readily", "really", "recent", "recently", "ref", "refs", "regarding",
				"regardless", "regards", "related", "relatively", "research", "respectively", "resulted", "resulting",
				"results", "right", "run", "said", "saw", "say", "saying", "says", "sec", "section", "see", "seeing",
				"seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sent", "seven", "several", "shall",
				"shed", "shes", "show", "showed", "shown", "showns", "shows", "significant", "significantly", "similar",
				"similarly", "since", "six", "slightly", "somebody", "somehow", "someone", "somethan", "something",
				"sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specifically", "specified",
				"specify", "specifying", "still", "stop", "strongly", "sub", "substantially", "successfully",
				"sufficiently", "suggest", "sup", "sure", "take", "taken", "taking", "tell", "tends", "th", "thank",
				"thanks", "thanx", "thats", "that've", "thence", "thereafter", "thereby", "thered", "therefore",
				"therein", "there'll", "thereof", "therere", "theres", "thereto", "thereupon", "there've", "theyd",
				"theyre", "think", "thou", "though", "thoughh", "thousand", "throug", "throughout", "thru", "thus",
				"til", "tip", "together", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "ts",
				"twice", "two", "u", "un", "unfortunately", "unless", "unlike", "unlikely", "unto", "upon", "ups", "us",
				"use", "used", "useful", "usefully", "usefulness", "uses", "using", "usually", "v", "value", "various",
				"'ve", "via", "viz", "vol", "vols", "vs", "w", "want", "wants", "wasnt", "way", "wed", "welcome",
				"went", "werent", "whatever", "what'll", "whats", "whence", "whenever", "whereafter", "whereas",
				"whereby", "wherein", "wheres", "whereupon", "wherever", "whether", "whim", "whither", "whod",
				"whoever", "whole", "who'll", "whomever", "whos", "whose", "widely", "willing", "wish", "within",
				"without", "wont", "words", "world", "wouldnt", "www", "x", "yes", "yet", "youd", "youre", "z", "zero",
				"a's", "ain't", "allow", "allows", "apart", "appear", "appreciate", "appropriate", "associated", "best",
				"better", "c'mon", "c's", "cant", "changes", "clearly", "concerning", "consequently", "consider",
				"considering", "corresponding", "course", "currently", "definitely", "described", "despite", "entirely",
				"exactly", "example", "going", "greetings", "hello", "help", "hopefully", "ignored", "inasmuch",
				"indicate", "indicated", "indicates", "inner", "insofar", "it'd", "keep", "keeps", "novel",
				"presumably", "reasonably", "second", "secondly", "sensible", "serious", "seriously", "sure", "t's",
				"third", "thorough", "thoroughly", "three", "well", "wonder", ",", ".", "]", "[", "'s", "-", "--", ")",
				"(", "\"", ":", "...", "'", "%", "!", "$", "/", ";", "?", "good" };
		stopwords = new HashSet<>(Arrays.asList(stopwordArr));

	}

	public Map<String, Double> preprocess(String text) {

		// TODO
		int minFreqCountForNgrams = 10;

		List<String> finalTokens = new ArrayList<>();

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
		props.setProperty("tokenize.language", "en");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props, false);

		// run all Annotators on this text
		CoreDocument document = pipeline.processToCoreDocument(text);
		pipeline.annotate(document);
//		System.out.println("Finished annotation for (tokenize,ssplit,pos,lemma,ner).");

		// get confidences for entities
		// System.out.println("Collecting mentioned Entities...");
		for (CoreEntityMention em : document.entityMentions()) {
			Map<String, Double> entityTypeConfidences = em.entityTypeConfidences();
			for (Entry<String, Double> entityTypeConfidence : entityTypeConfidences.entrySet()) {

				Double confidence = entityTypeConfidence.getValue();
				// TODO: Here should be parameterized
				if (confidence.compareTo(0.5) < 0) {
					continue;
				}

				NerTreeNode subNerTree = nerTreeRoot;
				String entity = em.text();
				String[] entityTokens = entity.split(" ");
				for (String entityToken : entityTokens) {
					subNerTree = subNerTree.appendChildNode(entityToken, false);
				}
				subNerTree.setIsNerEnd(true);

			}
			// System.out.println(em.text() + "\t" + em.entityTypeConfidences());
		}
		// System.out.println("Full tree: " + nerTreeRoot);

		List<CoreLabel> docTokens = document.tokens();
//		Integer totalDocTermCounts = docTokens.size();
//		System.out.println(String.format("totalDocTermCounts = %d", totalDocTermCounts));

		// Variables for examining NER
		Stack<String> tempTokenStack = new Stack<>();
		List<String> tempRegEntityTokens = new ArrayList<>();
		Boolean isPotentialRegEntity = false;
		NerTreeNode nerTreeCurrentNode = null;
		for (CoreLabel token : docTokens) {

			String word = token.word();
			String lemma = token.lemma();
			String lowerCaseStemmedWord = lemma.toLowerCase();

			boolean isNumber = true;
			try {
				Double.valueOf(lowerCaseStemmedWord);
			} catch (NumberFormatException e) {
				isNumber = false;
			}

			if (this.stopwords.contains(lowerCaseStemmedWord) || isNumber) {

				if (!tempTokenStack.isEmpty()) {
					String previousToken = tempTokenStack.peek();
					if (!SYM_STOPWORD.equals(previousToken)) {
						tempTokenStack.add(SYM_STOPWORD);
					}
				}

				isPotentialRegEntity = false;
				nerTreeCurrentNode = null;
				continue;
			}

			if (!isPotentialRegEntity) {

				NerTreeNode childNode = nerTreeRoot.getChildNode(word);
				if (childNode == null) {
					tempTokenStack.add(lowerCaseStemmedWord);
				} else {
					isPotentialRegEntity = true;
					nerTreeCurrentNode = childNode;
					tempRegEntityTokens.add(lowerCaseStemmedWord);
				}

			} else {

				// isPotentialRegEntity = True
				NerTreeNode childNode = nerTreeCurrentNode.getChildNode(word);
				if (childNode == null) {

					// process previous tokens
					if (nerTreeCurrentNode.getIsNerEnd()) {
						String recognizedEntity = nerTreeCurrentNode.getFullToken();
						tempTokenStack.add(recognizedEntity.toLowerCase());
						tempRegEntityTokens.clear();
					} else {
						// previous non-NER tokens
						tempTokenStack.addAll(tempRegEntityTokens);
						tempRegEntityTokens.clear();
					}

					// current token
					nerTreeCurrentNode = nerTreeRoot.getChildNode(word);
					if (nerTreeCurrentNode == null) {
						// current token is not a recognized entity
						isPotentialRegEntity = false;
						tempTokenStack.add(lowerCaseStemmedWord);
					} else {
						// current token could be a recognized entity
						// isPotentialRegEntity is still true;
						tempTokenStack.add(lowerCaseStemmedWord);
					}

				} else {
					// childNode != null
					// isPotentialRegEntity is still true;
					nerTreeCurrentNode = childNode;
					tempRegEntityTokens.add(lowerCaseStemmedWord);
				}

			}

		}

		// Flush the buffer
		if (nerTreeCurrentNode != null && nerTreeCurrentNode.getIsNerEnd()) {
			String recognizedEntity = nerTreeCurrentNode.getFullToken();
			tempTokenStack.add(recognizedEntity.toLowerCase());
			tempRegEntityTokens.clear();
		}

		if (tempRegEntityTokens.size() > 0) {
			tempTokenStack.addAll(tempRegEntityTokens);
			tempRegEntityTokens.clear();
		}

//		System.out.println("After combining recognized entities:");
//		System.out.println(tempTokenStack);

		List<String> tempTokenList = new ArrayList<>(tempTokenStack);
		List<String> tempResultTokenList = new ArrayList<>();
		LinkedList<String> potentialNGrams = new LinkedList<>();
		Set<LinkedList<String>> actualNGramsSet = new HashSet<>();

		int repeatTime = 2;
		int repeatCount = 1;
		for (int ngram = 2; ngram < 2 + repeatTime; ngram++, repeatCount++) {

			// System.out.println(String.format("Start to search for %d-grams.", ngram));
			for (int i = 0; i < tempTokenList.size(); i++) {

				String token = tempTokenList.get(i);
				if (SYM_STOPWORD.equals(token)) {

					tempResultTokenList.addAll(potentialNGrams);
					potentialNGrams.clear();

					if (repeatCount < repeatTime) {
						tempResultTokenList.add(SYM_STOPWORD);
					}
					continue;

				}

				potentialNGrams.addLast(token);
				int twoGramsSize = potentialNGrams.size();
				if (twoGramsSize < 2) {
					continue;
				}

				// twoGramsSize = 2
				String firstWord = potentialNGrams.get(0);
				String secondWord = potentialNGrams.get(1);
				if (actualNGramsSet.contains(potentialNGrams)) {
					String newToken = String.format("%s %s", firstWord, secondWord);
					tempResultTokenList.add(newToken);
					potentialNGrams.clear();
					continue;
				}

				int nextIdx = i + 1;
				int count = 1;
				while (nextIdx < tempTokenList.size() - ngram + 1) {

					List<String> subTempTokenList = tempTokenList.subList(nextIdx, tempTokenList.size());
					int matchIdx = subTempTokenList.indexOf(firstWord);
					if (matchIdx == -1) {
						break;
					}

					int matchNextIdx = matchIdx + 1;
					if (matchNextIdx < subTempTokenList.size()) {
						String nextWord = subTempTokenList.get(matchIdx + 1);
						if (secondWord.equals(nextWord)) {
							count++;
							nextIdx += (matchIdx + 2);
						} else {
							nextIdx += matchNextIdx;
						}
					} else {
						break;
					}

				}

				if (count >= minFreqCountForNgrams) {

					String newToken = String.format("%s %s", firstWord, secondWord);
					tempResultTokenList.add(newToken);
					actualNGramsSet.add(new LinkedList<>(potentialNGrams));
					potentialNGrams.clear();

				} else {
					String newToken = potentialNGrams.pollFirst();
					tempResultTokenList.add(newToken);
				}

			}

			tempTokenList = tempResultTokenList;
			tempResultTokenList = new ArrayList<>();
			potentialNGrams.clear();
			actualNGramsSet.clear();

		}

		finalTokens.addAll(tempTokenList);
//		System.out.println("After combining 2-grams and 3-grams:");
//		System.out.println(finalTokens);

		// Calculate the term count
		Map<String, Integer> termCounts = new HashMap<>();
		for (String token : finalTokens) {
			Integer termCount = termCounts.get(token);
			if (termCount == null) {
				termCounts.put(token, Integer.valueOf(1));
			} else {
				termCount++;
				termCounts.put(token, termCount);
			}
		}
		// System.out.println("TermCount: " + termCounts);

		// Calculate term frequency
		Map<String, Double> termFreqs = new HashMap<>();
		Integer tokenNum = finalTokens.size();
		for (Entry<String, Integer> termCount : termCounts.entrySet()) {
			String token = termCount.getKey();
			Integer count = termCount.getValue();
			Double termFreq = (count * 1.0) / tokenNum;
			termFreqs.put(token, termFreq);
		}

		return termFreqs;

	}

	public Map<String, Double> calculateInverseDocFreq(List<List<String>> docTermList) {

		Map<String, Double> inverseDocFreqs = new HashMap<>();

		Integer docNum = docTermList.size();
		// System.out.println(String.format("docNum: %d", docNum));

		Map<String, Integer> termDocCounts = new HashMap<>();
		for (List<String> docTerms : docTermList) {
			for (String term : docTerms) {
				Integer count = termDocCounts.get(term);
				if (count == null) {
					termDocCounts.put(term, Integer.valueOf(1));
				} else {
					count++;
					termDocCounts.put(term, count);
				}
			}
		}

		for (Entry<String, Integer> termDocCount : termDocCounts.entrySet()) {
			String term = termDocCount.getKey();
			Integer docCount = termDocCount.getValue();
			Double inverseDocFreq = Math.log((docNum * 1.0) / docCount);
			inverseDocFreqs.put(term, inverseDocFreq);
		}

		return inverseDocFreqs;

	}

	public static void main(String[] args) {

		TextPreprocessor preprocessor = new TextPreprocessor();
		String text = "John Doe studied Computer Science in the Microsoft Corporation."
				+ " Computer Science is a hot major in the United States."
				+ " However, Computer and Science are two different things."
				+ " New York has a beautiful maple tree garden." + " John had worked in the maple tree garden.";

		System.out.println("Original: " + text);
		Map<String, Double> termFreqs = preprocessor.preprocess(text);
		System.out.println(termFreqs);

	}

}
