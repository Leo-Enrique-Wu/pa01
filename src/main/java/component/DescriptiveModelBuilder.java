package component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;

import businessObject.*;

public class DescriptiveModelBuilder {

	private TextPreprocessor textProcessor = new TextPreprocessor();

	@SuppressWarnings({ "resource" })
	public Map<String, Map<String, Double>> genDocTermMatrix() {

		Map<String, Map<String, Double>> docTermMatrix = new HashMap<>();

		// Read documents from files
		Path currentRelativePath = Paths.get("");
		String currentPathStr = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + currentPathStr);

		String srcDocRootPath = currentPathStr + "/analyze/src";
		File srcDocRoot = new File(srcDocRootPath);
		File[] categoryDirs = srcDocRoot.listFiles();
		Map<String, Map<String, Double>> articleTermFreqMap = new HashMap<>();
		List<List<String>> docTermList = new ArrayList<>();
		for (File categoryDir : categoryDirs) {
			if (categoryDir.isDirectory()) {

				String dirName = categoryDir.getName();

				File[] articles = categoryDir.listFiles();
				for (File article : articles) {

					String articleFileName = article.getName();
					if (article.isFile() && articleFileName != null && articleFileName.endsWith(".txt")) {

						StringBuilder sb = new StringBuilder();
						sb.append(dirName);
						sb.append("_");
						String articleName = articleFileName.replaceAll(".txt", "");
						sb.append(articleName);
						String label = sb.toString();

						System.out.println(String.format("Processing %s", label));

						FileReader fr;
						try {

							fr = new FileReader(article);
							BufferedReader br = new BufferedReader(fr);

							StringBuilder articleContentSb = new StringBuilder();
							String line = null;
							try {
								while ((line = br.readLine()) != null) {
									if (line.length() > 0) {
										articleContentSb.append(line);
									} else {
										articleContentSb.append(" ");
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
							String articleContent = articleContentSb.toString();
							// System.out.println(articleContent);
							Map<String, Double> termFreq = textProcessor.preprocess(articleContent);
							articleTermFreqMap.put(label, termFreq);

							List<String> terms = new ArrayList<>(termFreq.keySet());
							docTermList.add(terms);

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

					}

				}

			}
		}

		Map<String, Double> inverseDocFreqMatrix = textProcessor.calculateInverseDocFreq(docTermList);

		List<String> allTerms = new ArrayList<>(inverseDocFreqMatrix.keySet());
		for (Entry<String, Map<String, Double>> articleTermFreq : articleTermFreqMap.entrySet()) {

			String label = articleTermFreq.getKey();
			Map<String, Double> termFreq = articleTermFreq.getValue();

			Map<String, Double> tf_idf = new HashMap<>();
			for (String term : allTerms) {

				Double freq = termFreq.get(term);
				if (freq == null) {
					tf_idf.put(term, Double.valueOf(0));
				} else {
					tf_idf.put(term, freq);
				}

			}
			docTermMatrix.put(label, tf_idf);

		}

		String outputFilePathStr = currentPathStr + "/analyze/output";
		String docTermMatrixOutputFilePathStr = outputFilePathStr + "/docTermMatrix.csv";
		File docTermMatrixOutputFile = new File(docTermMatrixOutputFilePathStr);
		try {

			if (docTermMatrixOutputFile.exists()) {
				docTermMatrixOutputFile.delete();
				docTermMatrixOutputFile.createNewFile();
			}

			FileWriter fw = new FileWriter(docTermMatrixOutputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			try {
				StringBuilder outputSb = new StringBuilder();
				for (String term : allTerms) {
					outputSb.append(",");
					outputSb.append(term);
				}
				bw.write(outputSb.toString());
				bw.newLine();
				outputSb.delete(0, outputSb.length());

				for (Entry<String, Map<String, Double>> docTerm : docTermMatrix.entrySet()) {

					String docLable = docTerm.getKey();
					outputSb.append(docLable);

					Map<String, Double> termValueMap = docTerm.getValue();
					for (String term : allTerms) {
						Double value = termValueMap.get(term);
						value = (value != null) ? value : Double.valueOf(0);
						outputSb.append(",");
						outputSb.append(value);
					}
					bw.write(outputSb.toString());
					bw.newLine();
					outputSb.delete(0, outputSb.length());

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				bw.close();
				fw.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Generate topics for each folder
		String topicOutputFilePathStr = outputFilePathStr + "/topic.csv";
		File topicOutputFile = new File(topicOutputFilePathStr);
		try {

			if (topicOutputFile.exists()) {
				topicOutputFile.delete();
				topicOutputFile.createNewFile();
			}

			FileWriter fw = new FileWriter(topicOutputFile);
			BufferedWriter bw = new BufferedWriter(fw);
			try {
				for (File categoryDir : categoryDirs) {
					if (categoryDir.isDirectory()) {

						String dirName = categoryDir.getName();
						Set<TermFreq> sortedTermFreqs = new TreeSet<>(new Comparator<TermFreq>() {

							@Override
							public int compare(TermFreq o1, TermFreq o2) {
								String o1Term = o1.getTerm();
								Double o1Freq = o1.getFreq();
								String o2Term = o2.getTerm();
								Double o2Freq = o2.getFreq();
								return (o1Freq.compareTo(o2Freq) != 0) ? (o1Freq.compareTo(o2Freq) * -1)
										: o1Term.compareTo(o2Term);
							}

						});

						for (String term : allTerms) {

							Double totalFreq = Double.valueOf(0);
							File[] articles = categoryDir.listFiles();
							for (File article : articles) {

								String articleFileName = article.getName();
								if (article.isFile() && articleFileName != null && articleFileName.endsWith(".txt")) {

									StringBuilder sb = new StringBuilder();
									sb.append(dirName);
									sb.append("_");
									String articleName = articleFileName.replaceAll(".txt", "");
									sb.append(articleName);
									String label = sb.toString();
									Map<String, Double> termFreq = docTermMatrix.get(label);
									Double freq = termFreq.get(term);
									totalFreq += freq;

								}

							}
							TermFreq termFreq = new TermFreq(term, totalFreq);
							sortedTermFreqs.add(termFreq);

						}

						List<TermFreq> sortedTermFreqList = new ArrayList<>(sortedTermFreqs);
						StringBuilder topicBuilder = new StringBuilder();
						topicBuilder.append(dirName);
						for (int i = 0; i < 5; i++) {
							String topic = sortedTermFreqList.get(i).getTerm();
							topicBuilder.append(",");
							topicBuilder.append(topic);
						}

						bw.write(topicBuilder.toString());
						bw.newLine();
						topicBuilder.delete(0, topicBuilder.length());

					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				bw.close();
				fw.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return docTermMatrix;

	}

	public static void main(String[] args) {

		DescriptiveModelBuilder service = new DescriptiveModelBuilder();
		service.genDocTermMatrix();

	}

}
