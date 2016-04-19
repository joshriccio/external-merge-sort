package ems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * External Merge Sort: A java implementation of merge sort, a sorting
 * algorithm that takes an input text file, breaks it up into many temp
 * files and sorts in order
 * 
 * @author Joshua Riccio
 * 
 * 
 */
public class ExternalMergeSort {
	private static ArrayList<String> tempFilesA;
	private static ArrayList<String> tempFilesB;
	private static int chunksize = 16;
	private static String outputFile;

	public static void main(String[] args) {
		String inputFile = args[0];
		if (!checkFile(inputFile)) {
			System.out.println("Invalid input file");
			System.exit(1);
		}
		if (args.length < 2) {
			System.out.println("No ouput file specified");
			System.exit(1);
		}
		outputFile = args[1];
		tempFilesA = new ArrayList<String>();
		tempFilesB = new ArrayList<String>();

		if (!breakInput(inputFile)) {
			int pass = 1, chunk = 0;
			String oddFile = null;

			/*
			 * This loop is the two-file merge. The names of the files are
			 * stored in two arrays so that the names don't have to be recalled
			 * using a File directory scanner
			 */
			while (true) {
				boolean even = false;
				chunk = 0;
				if (pass % 2 == 1) {
					if (tempFilesA.size() == 1)
						break;
					if (tempFilesA.size() % 2 == 0)
						even = true;
					int a = 0, b = 1;
					for (int i = 0; i < tempFilesA.size() / 2; i++) {
						mergeTempFiles(tempFilesA.get(a), tempFilesA.get(b), pass, chunk, false);
						chunk++;
						a = a + 2;
						b = b + 2;
					}
					if (!even && oddFile == null) {
						oddFile = tempFilesA.get(tempFilesA.size() - 1);
					} else if (!even && oddFile != null) {
						mergeTempFiles(oddFile, tempFilesA.get(tempFilesA.size() - 1), pass, chunk, false);
						oddFile = null;
					}
					tempFilesA.clear();
				} else {
					if (tempFilesB.size() == 1)
						break;
					if (tempFilesB.size() % 2 == 0)
						even = true;
					int a = 0, b = 1;
					for (int i = 0; i < tempFilesB.size() / 2; i++) {
						mergeTempFiles(tempFilesB.get(a), tempFilesB.get(b), pass, chunk, false);
						chunk++;
						a = a + 2;
						b = b + 2;
					}
					if (!even && oddFile == null) {
						oddFile = tempFilesB.get(tempFilesB.size() - 1);
					} else if (!even && oddFile != null) {
						mergeTempFiles(oddFile, tempFilesB.get(tempFilesB.size() - 1), pass, chunk, false);
						oddFile = null;
					}
					tempFilesB.clear();
				}
				pass++;
			}
			// write output to file specified in args[1]
			if (oddFile != null) {
				if (tempFilesA.size() == 1)
					mergeTempFiles(oddFile, tempFilesA.get(tempFilesA.size() - 1), pass - 1, 0, true);
				if (tempFilesB.size() == 1)
					mergeTempFiles(oddFile, tempFilesB.get(tempFilesB.size() - 1), pass - 1, 0, true);
			} else if (tempFilesA.size() == 1) {
				File f = new File(tempFilesA.get(0)), o = new File(outputFile);
				f.renameTo(o);
			} else if (tempFilesB.size() == 1) {
				File f = new File(tempFilesB.get(0)), o = new File(outputFile);
				f.renameTo(o);
			}
		}
	}

	private static boolean checkFile(String inputFile) {
		File f = new File(inputFile);
		return f.exists();
	}

	private static void mergeTempFiles(String tmp1, String tmp2, int pass, int chunk, boolean out) {
		ArrayList<String> buff = new ArrayList<String>();
		FileReader xms1, xms2;
		String word1 = "", word2 = "";
		try {
			xms1 = new FileReader(tmp1);
			xms2 = new FileReader(tmp2);
			boolean cont1 = true, cont2 = true;
			BufferedReader br1 = new BufferedReader(xms1), br2 = new BufferedReader(xms2);

			try {
				while (true) {
					if (cont1)
						if ((word1 = br1.readLine()) == null)
							break;
					if (cont2)
						if ((word2 = br2.readLine()) == null)
							break;
					if (word1.compareTo(word2) <= 0) {
						buff.add(word1);
						cont2 = false;
						cont1 = true;
					} else {
						buff.add(word2);
						cont1 = false;
						cont2 = true;
					}
				}
				if (word1 != null) {
					buff.add(word1);
					while ((word1 = br1.readLine()) != null) {
						buff.add(word1);
					}
				}
				if (word2 != null) {
					buff.add(word2);
					while ((word2 = br2.readLine()) != null) {
						buff.add(word2);
					}
				}
				writeChunk(buff, pass, chunk, out);
				br1.close();
				br2.close();
			} catch (IOException e) {
				System.out.println("There is an error with the input files.");
				System.exit(1);
			}

		} catch (FileNotFoundException e) {
			System.out.println("The requested file was not found.");
			System.exit(1);
		}
	}

	private static void writeChunk(ArrayList<String> words, int pass, int chunk, boolean out) {
		try {
			if (!out) {
				Path file = Paths
						.get("xms.tmp.pass_" + String.format("%04d", pass) + ".chunk_" + String.format("%04d", chunk));
				if (pass % 2 == 0) {
					tempFilesA.add(
							"xms.tmp.pass_" + String.format("%04d", pass) + ".chunk_" + String.format("%04d", chunk));
				} else {
					tempFilesB.add(
							"xms.tmp.pass_" + String.format("%04d", pass) + ".chunk_" + String.format("%04d", chunk));
				}
				Files.write(file, words, Charset.forName("UTF-8"));
			} else {
				Path file = Paths.get(outputFile);
				Files.write(file, words, Charset.forName("UTF-8"));
			}
		} catch (IOException e) {
			System.out.println("There is an error with the input files.");
			System.exit(1);
		}
	}

	private static ArrayList<String> sortChunk(ArrayList<String> words) {
		// Compares values to lowercase
		for (int i = 0; i < words.size(); i++) {
			for (int j = 0; j <= i; j++) {
				if (words.get(i).compareTo(words.get(j)) <= 0) {
					// swap
					String temp = words.get(j);
					words.set(j, words.get(i));
					words.set(i, temp);
				}
			}
		}
		return words;
	}

	private static boolean breakInput(String inputFile) {
		// create file from string/path
		File file = new File(inputFile);
		// load file into scanner
		try {
			Scanner input = new Scanner(file);
			int chunk = 0, pass = 0, stringcount = 0;
			while (input.hasNext()) {
				ArrayList<String> temp = new ArrayList<String>();
				int i = 0;
				for (; input.hasNext() && i < chunksize; i++) {
					temp.add(input.next());
					stringcount++;
				}
				if (!input.hasNext() && stringcount < chunksize) {
					temp = sortChunk(temp);
					writeChunk(temp, pass, chunk, true);
					input.close();
					return true;
				}
				// add chunk string to chunks list
				temp = sortChunk(temp);
				writeChunk(temp, pass, chunk, false);
				chunk++;
			}
			input.close();
		} catch (FileNotFoundException e) {
			System.out.println("The requested file was not found.");
			System.exit(1);
		}
		return false;
	}
}