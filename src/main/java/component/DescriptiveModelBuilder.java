package component;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;

import businessObject.*;

public class DescriptiveModelBuilder {

	private TextPreprocessor textProcessor = new TextPreprocessor();

	public Map<String, Map<String, Double>> genDocTermMatrix(String srcDocRootPath, String outputFilePathStr) {

		Map<String, Map<String, Double>> docTermMatrix = new HashMap<>();

//		// Read documents from files
		File srcDocRoot = new File(srcDocRootPath);

		File[] categoryDirs = srcDocRoot.listFiles();
		Map<String, Map<String, Double>> articleTermFreqMap = new HashMap<>();
		List<List<String>> docTermList = new ArrayList<>();
		
		int currentDocRowNum = 0;
		int maxDocRowNum = 5;
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

						currentDocRowNum++;
						if (currentDocRowNum == 1) {
							System.out.print(String.format("Processing %s", label));
						} else {
							System.out.print(String.format(", %s", label));
							if (currentDocRowNum == maxDocRowNum) {
								System.out.println();
								currentDocRowNum = 0;
							}
						}

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
		System.out.println();

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

					// TODO
					if (freq > 0.01) {
						tf_idf.put(term, freq);
					} else {
						tf_idf.put(term, Double.valueOf(0));
					}

				}

			}
			docTermMatrix.put(label, tf_idf);

		}

		List<String> allZeroAttributes = new ArrayList<>();
		Double zero = Double.valueOf(0);
		Map<String, Double> attributeTotalFreqs = new HashMap<>();
		Map<String, Integer> attributeTotalCounts = new HashMap<>();
		for (String docLabel : docTermMatrix.keySet()) {

			Map<String, Double> termMatrix = docTermMatrix.get(docLabel);
			for (Entry<String, Double> termFreq : termMatrix.entrySet()) {

				String term = termFreq.getKey();
				Double freq = termFreq.getValue();
				Double totalFreq = attributeTotalFreqs.get(term);
				if (totalFreq == null) {
					totalFreq = Double.valueOf(0);
				}
				totalFreq += freq;
				attributeTotalFreqs.put(term, totalFreq);

			}

		}

		for (String docLabel : docTermMatrix.keySet()) {

			Map<String, Double> termMatrix = docTermMatrix.get(docLabel);
			for (Entry<String, Double> termFreq : termMatrix.entrySet()) {

				String term = termFreq.getKey();
				Double freq = termFreq.getValue();
				if (Double.valueOf(0).compareTo(freq) == 0) {
					continue;
				}

				Integer totalCount = attributeTotalCounts.get(term);
				if (totalCount == null) {
					attributeTotalCounts.put(term, Integer.valueOf(1));
				} else {
					totalCount++;
					attributeTotalCounts.put(term, totalCount);
				}

			}

		}

		for (Entry<String, Double> attributeTotalFreq : attributeTotalFreqs.entrySet()) {

			String term = attributeTotalFreq.getKey();
			Double totalFreq = attributeTotalFreq.getValue();
			Integer totalCount = attributeTotalCounts.get(term);

			// TODO
			if (zero.compareTo(totalFreq) == 0 || totalCount < 3) {
//			if (zero.compareTo(totalFreq) == 0) {
				String attribute = attributeTotalFreq.getKey();
				allZeroAttributes.add(attribute);
			}
		}
//		System.out.println("allZeroAttributes: " + allZeroAttributes);

		// Filter out allZeroAttributes
		List<String> tmpAllTerms = new ArrayList<>();
		for (String term : allTerms) {
			if (!allZeroAttributes.contains(term)) {
				tmpAllTerms.add(term);
			}
		}
		allTerms = tmpAllTerms;

		for (String docLabel : docTermMatrix.keySet()) {
			Map<String, Double> termMatrix = docTermMatrix.get(docLabel);
			for (String zeroAttribute : allZeroAttributes) {
				termMatrix.remove(zeroAttribute);
			}
			docTermMatrix.put(docLabel, termMatrix);
		}

		outputDocTermMatrix(outputFilePathStr, docTermMatrix, allTerms);
		outputTopics(outputFilePathStr, docTermMatrix, categoryDirs, allTerms);

		return docTermMatrix;

	}

	private void outputTopics(String outputFilePathStr, Map<String, Map<String, Double>> docTermMatrix,
			File[] categoryDirs, List<String> allTerms) {
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

			System.out.println(
					String.format("Output topics for each document folder: %s", topicOutputFile.getAbsolutePath()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void outputDocTermMatrix(String outputFilePathStr, Map<String, Map<String, Double>> docTermMatrix,
			List<String> allTerms) {

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

			System.out.println(
					String.format("Output document-term matrix: %s", docTermMatrixOutputFile.getAbsolutePath()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		DescriptiveModelBuilder service = new DescriptiveModelBuilder();

		Path currentRelativePath = Paths.get("");
		String currentPathStr = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + currentPathStr);

		String srcDocRootPath = currentPathStr + "/analyze/src";
		String outputFilePathStr = currentPathStr + "/analyze/output";

		service.genDocTermMatrix(srcDocRootPath, outputFilePathStr);

	}

}
