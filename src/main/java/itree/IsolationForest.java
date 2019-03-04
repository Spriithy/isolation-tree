package itree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.util.FastMath;

import lombok.Getter;
import lombok.NonNull;

/**
 * 
 * @author theophile
 *
 */
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
	@Getter
	private int numberOfTrees;

	/**
	 * The size of the samples used to generate trees.
	 */
	@Getter
	private int samplingSize;

	/**
	 * The height threshold at which the evaluation algorithm will stop exploring an
	 * iTree.
	 */
	@Getter
	private int heightLimit;

	/**
	 * The dataset to process.
	 */
	@Getter
	private List<T> values;

	/**
	 * The attributes the algorithm will use to score values.
	 */
	@Getter
	private List<Function<T, Double>> attributes;

	/**
	 * The iTrees composing the iForest.
	 */
	private Set<IsolationTreeNode<T>> forest;

	/**
	 * The average path length of unsuccessful searches in an iTree.
	 */
	private double cFactor;

	public static class Builder<T> {

		private IsolationForest<T> iForest;

		public Builder(Class<T> tClass) {
			iForest = new IsolationForest<>(tClass);
			numberOfTrees(DEFAULT_NUMBER_OF_TREES);
			iForest.samplingSize = DEFAULT_SAMPLING_SIZE;
		}

		public IsolationForest.Builder<T> values(@NonNull List<T> values) {
			iForest.values = values;
			samplingSize(iForest.samplingSize);
			return this;
		}

		public IsolationForest.Builder<T> numberOfTrees(int numberOfTrees) {
			iForest.numberOfTrees = numberOfTrees;
			return this;
		}

		public IsolationForest.Builder<T> samplingSize(int samplingSize) {
			iForest.setSamplingSize(samplingSize);
			return this;
		}

		public IsolationForest.Builder<T> heightLimit(int heightLimit) {
			iForest.heightLimit = heightLimit >= iForest.samplingSize ? iForest.samplingSize - 1
					: FastMath.min(iForest.heightLimit, heightLimit);
			return this;
		}

		public IsolationForest.Builder<T> attributes(@NonNull List<Function<T, Double>> attributes) {
			iForest.attributes = attributes;
			return this;
		}

		public IsolationForest<T> build() {
			iForest.forest = new HashSet<>();
			for (int i = 0; i < iForest.numberOfTrees; i++) {
				List<T> sample = randomSample(iForest.values, iForest.samplingSize);
				iForest.forest.add(new IsolationTreeNode<T>(sample, iForest.attributes));
			}
			return iForest;
		}
	}

	IsolationForest(Class<T> tClass) {
	}

	/**
	 * 
	 * @param values
	 * @param attributes
	 * @param numberOfTrees
	 * @param samplingSize
	 * @param heightLimit
	 */
	IsolationForest(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes, int numberOfTrees,
			int samplingSize, int heightLimit) {
		this.numberOfTrees = numberOfTrees;
		this.values = values;
		this.attributes = attributes;
		setSamplingSize(samplingSize);
		this.heightLimit = Math.min(heightLimit, this.heightLimit);
	}

	IsolationForest(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes) {
		this(values, attributes, DEFAULT_NUMBER_OF_TREES, DEFAULT_SAMPLING_SIZE, DEFAULT_SAMPLING_SIZE - 1);
	}

	private void setSamplingSize(int samplingSize) {
		this.samplingSize = samplingSize >= values.size() ? closestInferiorPowerOfTwo(values.size()) : samplingSize;
		this.heightLimit = this.samplingSize - 1;
		cFactor = computeCFactor(this.samplingSize);
	}

	private double pathLength(T value, IsolationTreeNode<T> iTree, int heightLimit) {
		return pathLength(value, iTree, heightLimit, 0);
	}

	private double pathLength(T value, IsolationTreeNode<T> iTree, int heightLimit, int pathLength) {
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
		double averagePathLength = forest.stream() //
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
			return 2 * H(psi - 1) - 2 * (psi - 1) / values.size();
		}
		return psi == 2 ? 1 : 0;
	}

	private static double H(double i) {
		return Math.log(i) + Constants.EULERS_CONSTANT;
	}

	public static int closestInferiorPowerOfTwo(int x) {
		int y = x - 1;
		y |= y >> 1;
		y |= y >> 2;
		y |= y >> 4;
		y |= y >> 8;
		y |= y >> 16;
		return x >= y + 1 ? y + 1 : (y + 1) / 2;
	}

}
