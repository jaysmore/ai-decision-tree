import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DecisionStump {
	/*
	 * TODO: Note that this program is based on the "genres" value of the following
	 * particular format: val1|val2|val3 e.g. Action|Adventure|Fantasy|Sci-Fi
	 */
	private static final String COMMA_DELIMITER = ","; // CSV delimiter
	private static final String CAT_DELIMITER = "\\|"; // categorical feature delimiter
	private static final String PATH_TO_TRAIN = "./train.csv"; // path to dataset (CSV file)
	private static final String NAME_OF_CLASS_LABEL = "gross";
	private static final String NAME_OF_CAT_FEATURE = "genres"; // the one and only categorical feature

	private static String[] rel_cols = { NAME_OF_CLASS_LABEL, NAME_OF_CAT_FEATURE, "cont_feature0", "cont_feature1",
			"cont_feature2", "cont_feature3" }; // relevant columns
	private static double B = 600000000; // revenue threshold

	/*
	 * Parse original CSV into a list of relevant features (according to
	 * 'rel_cols'), discard any instances with missing values
	 */
	public static List<List<String>> parseRecords(String file_path) throws FileNotFoundException, IOException {
		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file_path))) {
			String line;
			List<Integer> rel_indices = new ArrayList<>();
			if ((line = br.readLine()) != null) { // read column titles
				String[] string_values = line.split(COMMA_DELIMITER);
				for (int i = 0; i < rel_cols.length; i++)
					if (Arrays.asList(string_values).contains(rel_cols[i]))
						rel_indices.add(Arrays.asList(string_values).indexOf(rel_cols[i]));
			}
			outerloop: while ((line = br.readLine()) != null) {
				String[] string_values = line.split(COMMA_DELIMITER);
				String[] record = new String[rel_indices.size()];
				for (int i = 0; i < rel_indices.size(); i++) {
					if (string_values[rel_indices.get(i)].isEmpty())
						continue outerloop; // discard line with missing feature(s)
					record[i] = string_values[rel_indices.get(i)]; // relevant features
				}
				records.add(Arrays.asList(record));
			}
		}
		return records;
	}

	/*
	 * Perform one-hot encode then pick one of several possible genres then perform
	 * info. gain TODO: You should implement a different approach if this is not
	 * what expected from the instructor
	 */
	private static void oneHotEncodingThenPickOneThenInfoGain(List<List<String>> records, int cat_index,
			int num_of_smaller_cats) {
		List<String> cat_vals = new ArrayList<>();
		Random rng = new Random();
		for (int i = 0; i < records.size(); i++) { // loop to collect all possible values, then encode
			String temp = records.get(i).get(cat_index);
			String[] temp_array = temp.split(CAT_DELIMITER);
			for (int j = 0; j < temp_array.length; j++)
				if (!cat_vals.contains(temp_array[j]))
					cat_vals.add(temp_array[j]);
			records.get(i).set(cat_index,
					Integer.toString(cat_vals.indexOf(temp_array[rng.nextInt(temp_array.length)]))); // randomly choose
																										// the genre
		}
		// TODO: before calling 'infoGain', you need to combine some of the categories
		// so that the total number of categories is less than or equal to 10
		infoGain(records, cat_index, num_of_smaller_cats);
	}

	/*
	 * Split a continuous feature K categories then compute info. gain; note that K
	 * is # of categories TODO: More efficient implementation: convert all cont.
	 * features inside 'parseRecords' method
	 */
	private static void contToCatThenInfoGain(List<List<String>> records, int cont_index, int K) {
		if (records.get(0) == null)
			return;
		Double a = Double.parseDouble(records.get(0).get(cont_index)); // maximum
		Double b = Double.parseDouble(records.get(0).get(cont_index)); // minimum
		double epsilon = 0.01; // hard-code to 0.01
		for (int i = 1; i < records.size(); i++) {
			double curr = Double.parseDouble(records.get(i).get(cont_index));
			if (curr > a)
				a = curr;
			if (curr < b)
				b = curr;
		}
		for (int i = 0; i < records.size(); i++) {
			double var = Double.parseDouble(records.get(i).get(cont_index));
			double numer = var - a;
			double denom = (b + epsilon - a) / K;
			int x_ij = (int) Math.floor(numer / denom);
			records.get(i).set(cont_index, Integer.toString(x_ij));
		}
		infoGain(records, cont_index, K);
	}

	/*
	 * Compute info. gain TODO: More efficient implementation: count #(Y=y) only
	 * once
	 */
	private static void infoGain(List<List<String>> records, int cat_index, int K) {
		// TODO: implement to calculate the information gain using the formula given in
		// class
		List<Object> result = new ArrayList<>();
		double hy = 0;
		double hyxj = 0;
		for (int i = 0; i < K; i++) {
			// TODO: implement the counting for #(Y=y, X_j=x), #(X_j=x), etc.
			// then update hy and hyxj
			int num_of_pos = 0;
			int num_of_neg = 0;
			result.add(num_of_pos);
			result.add(num_of_neg);
		}
		result.add(0, K); // # of categories; add to the beginning of the list
		result.add(1, hy - hyxj); // info. gain as the second element of the list
		System.out.println(result.toString().replace("[", "").replace("]", "")); // print the list
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		List<List<String>> records = parseRecords(PATH_TO_TRAIN); // Parse CSV files
		// TODO: make sure output is printed in the proper order
		// by calling 'oneHotEncodingThenPickOneThenInfoGain' and
		// 'contToCatThenInfoGain' in a specified order
		oneHotEncodingThenPickOneThenInfoGain(records, Arrays.asList(rel_cols).indexOf(NAME_OF_CAT_FEATURE), 5); // TODO:
																													// hard-code
																													// the
																													// cat
																													// feature
																													// to
																													// 3
																													// categories;
																													// you
																													// might
																													// want
																													// to
																													// try
																													// different
																													// #
																													// of
																													// cats
		for (int i = 0; i < rel_cols.length; i++) {
			if (i == Arrays.asList(rel_cols).indexOf(NAME_OF_CAT_FEATURE)
					|| i == Arrays.asList(rel_cols).indexOf(NAME_OF_CLASS_LABEL))
				continue;
			contToCatThenInfoGain(records, i, 3); // TODO: hard-code all cont. features to 3 categories; you might want
													// to generalize this
		}
	}
}
