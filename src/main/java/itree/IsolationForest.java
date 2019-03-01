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

	public static final int DEFAULT_HEIGHT_LIMIT = 255;
	public static final int DEFAULT_NUMBER_OF_TREES = 100;
	public static final int DEFAULT_SUBSAMPLING_SIZE = 256;

	private int numberOfTrees;
	private int samplingSize;
	private int heightLimit;
	private List<T> values;
	private List<Function<T, Double>> attributes;
	private Set<IsolationTreeNode<T>> isolationForest;

	private final double c;

	public IsolationForest(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes, int numberOfTrees,
			int subsamplingSize, int heightLimit) {
		if (subsamplingSize > values.size()) {
			throw new IllegalArgumentException("subsamplingSize > values.size()");
		}
		this.heightLimit = heightLimit;
		this.numberOfTrees = numberOfTrees;
		this.samplingSize = subsamplingSize;
		this.values = values;
		this.attributes = attributes;
		c = computeC(subsamplingSize);
	}

	public IsolationForest(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes) {
		this(values, attributes, DEFAULT_NUMBER_OF_TREES, DEFAULT_SUBSAMPLING_SIZE, DEFAULT_HEIGHT_LIMIT);
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
			return pathLength + computeC(iTree.getSize());
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
		return Math.pow(2, -2 * (averagePathLength / c));
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

	private double computeC(double psi) {
		if (psi > 2) {
			return 2 * H(psi - 1) - 2 * (psi - 1) / values.size();
		}
		return psi == 2 ? 1 : 0;
	}

	private static double H(double i) {
		return Math.log(i) + Constants.EULERS_CONSTANT;
	}

}
