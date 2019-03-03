package itree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import lombok.Data;
import lombok.NonNull;

/**
 * 
 * @author theophile
 *
 */
@Data
public class IsolationForest<T> {

	/**
	 * For large amounts of data, 100 iTrees are sufficient.
	 */
	public static final int DEFAULT_NUMBER_OF_TREES = 100;

	/**
	 * When processing large datasets, it is a loss of processing time and memory to
	 * use more than 256 as a sample size. Indeed, the ratio precision gained over
	 * efficiency loss is not interesting enough.
	 */
	public static final int DEFAULT_SAMPLING_SIZE = 256;

	/**
	 * The number of trees to generate.
	 */
	private int numberOfTrees;

	/**
	 * The size of the samples used to generate trees.
	 */
	private int samplingSize;

	/**
	 * The height threshold at which the evaluation algorithm will stop exploring an
	 * iTree.
	 */
	private int heightLimit;

	/**
	 * The dataset to process.
	 */
	private List<T> values;

	/**
	 * The attributes the algorithm will use to score values.
	 */
	private List<Function<T, Double>> attributes;

	/**
	 * The iTrees composing the iForest.
	 */
	private Set<IsolationTreeNode<T>> isolationForest;

	/**
	 * The average path length of unsuccessful searches in an iTree.
	 */
	private double cFactor;

	/**
	 * 
	 * @param values
	 * @param attributes
	 * @param numberOfTrees
	 * @param samplingSize
	 * @param heightLimit
	 */
	public IsolationForest(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes, int numberOfTrees,
			int samplingSize, int heightLimit) {
		this.numberOfTrees = numberOfTrees;
		this.values = values;
		this.attributes = attributes;
		setSamplingSize(samplingSize);
		this.heightLimit = Math.min(heightLimit, this.heightLimit);
	}

	public IsolationForest(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes) {
		this(values, attributes, DEFAULT_NUMBER_OF_TREES, DEFAULT_SAMPLING_SIZE, DEFAULT_SAMPLING_SIZE - 1);
	}

	public void setSamplingSize(int samplingSize) {
		this.samplingSize = samplingSize;
		this.heightLimit = samplingSize - 1;
		cFactor = computeCFactor(samplingSize);
	}

	public void buildForest() {
		isolationForest = new HashSet<>();
		for (int i = 0; i < numberOfTrees; i++) {
			List<T> sample = randomSample(values, samplingSize);
			isolationForest.add(new IsolationTreeNode<>(sample, attributes));
		}
	}

	public double pathLength(T value, IsolationTreeNode<T> iTree, int heightLimit) {
		return pathLength(value, iTree, heightLimit, 0);
	}

	public double pathLength(T value, IsolationTreeNode<T> iTree, int heightLimit, int pathLength) {
		if (iTree.isExternal() || pathLength >= heightLimit) {
			return pathLength + computeCFactor(iTree.getSize());
		}
		Function<T, Double> splitAttribute = iTree.getSplitAttribute();
		if (splitAttribute.apply(value) < iTree.getSplitValue()) {
			return pathLength(value, iTree.getLeft(), heightLimit, pathLength + 1);
		}
		return pathLength(value, iTree.getRight(), heightLimit, pathLength + 1);
	}

	public double anomalyScore(T value) {
		double averagePathLength = isolationForest.stream() //
				.mapToDouble(iTree -> pathLength(value, iTree, heightLimit)) //
				.average() //
				.getAsDouble();
		return Math.pow(2, -2 * (averagePathLength / cFactor));
	}

	private static <T> List<T> randomSample(List<T> items, int m) {
		Random random = new Random();
		List<T> sample = new ArrayList<>();
		int n = items.size();
		if (m > n / 2) {
			List<T> negativeSet = randomSample(items, n - m);
			for (T item : items) {
				if (!negativeSet.contains(item)) {
					sample.add(item);
				}
			}
		} else {
			while (sample.size() < m) {
				int randPos = random.nextInt(n);
				sample.add(items.get(randPos));
			}
		}
		return sample;
	}

	private double computeCFactor(double psi) {
		if (psi > 2) {
			return 2 * H(psi - 1) - 2 * (psi - 1) / psi;
		}
		return psi == 2 ? 1 : 0;
	}

	private static double H(double i) {
		return Math.log(i) + Constants.EULERS_CONSTANT;
	}

}
