package itree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

/**
 * 
 * @author theophile
 *
 */
public class Main {

	private static final Random RANDOM = new Random();
	private static final int OBSERVATION_COUNT = 100;
	private static final int OUTLIER_COUNT = 10;

	public static void main(String[] args) {
		List<Point> values = generatePoints();
		System.out.println("Using " + values.size() + " points...");
		List<Function<Point, Double>> attributes = new ArrayList<>();
		attributes.add(Point::getX);
		attributes.add(Point::getY);
		IsolationForest<Point> iForest = new IsolationForest<>(values, attributes, 100, 64, 63);
		System.out.println("Building iForest [numberOfTrees=" + iForest.getNumberOfTrees() + ", samplingSize="
				+ iForest.getSamplingSize() + "] ...");
		iForest.buildForest();
		System.out.println("10 most 'abnormal' points (or outliers):");
		System.out.println("----------------------------------------");
		double averageAnomalyScore = values.stream().mapToDouble(iForest::anomalyScore).average().getAsDouble();
		values.stream() //
				.map(p -> Pair.of(p, iForest.anomalyScore(p))) //
				.sorted(Main::descending) //
				.limit(10) //
				.forEach(p -> {
					System.out.println((values.indexOf(p.getKey()) + 1) + " - " + p.getKey().toString()
							+ "\tanomalyScore=" + p.getValue() + "\tdeviation=" + (p.getValue() - averageAnomalyScore));
				});
	}

	public static int descending(Pair<Point, Double> x, Pair<Point, Double> y) {
		return Double.compare(y.getValue(), x.getValue());
	}

	public static List<Point> generatePoints() {
		double xMean = 0;
		double xStdDev = 0.2d;
		double yMean = 0;
		double yStdDev = 0.2d;
		System.out.println("Generating points...");
		List<Point> points = new ArrayList<>(OBSERVATION_COUNT);
		// Generate 99% legit values
		for (int i = 0; i < OBSERVATION_COUNT - OUTLIER_COUNT; i++) {
			points.add(generatePoint(xMean, xStdDev, yMean, yStdDev));
		}
		// Generate 1% outliers
		for (int i = 0; i < OUTLIER_COUNT; i++) {
			points.add(generatePoint(-5.5d, 0.1d, 3.5d, 0.1d));
		}
		Collections.shuffle(points);
		return points;
	}

	public static List<Point> simple() {
		List<Point> points = new ArrayList<>(20);
		points.add(new Point(0, 0, 0));
		points.add(new Point(0, -0.1, 0));
		points.add(new Point(0, 0.1, 0));
		points.add(new Point(0, 0.2, 0));
		points.add(new Point(0, -0.2, 0));
		points.add(new Point(0.1, 0, 0));
		points.add(new Point(0.1, 0.1, 0));
		points.add(new Point(0.1, -0.1, 0));
		points.add(new Point(0.1, 0.2, 0));
		points.add(new Point(0.1, -0.2, 0));
		points.add(new Point(0.2, 0, 0));
		points.add(new Point(0.2, 0.1, 0));
		points.add(new Point(0.2, -0.1, 0));
		points.add(new Point(0.2, 0.2, 0));
		points.add(new Point(0.2, -0.2, 0));

		points.add(new Point(-3, 3, 0));
		points.add(new Point(-100, 3, 0));
		points.add(new Point(5, 2, 0));
		points.add(new Point(-10, -5, 0));
		points.add(new Point(8, 8, 0));
		return points;
	}

	public static List<Point> hbk() {
		List<Point> points = new ArrayList<>(75);
		points.add(new Point(10.1, 19.6, 28.3));
		points.add(new Point(9.5, 20.5, 28.9));
		points.add(new Point(10.7, 20.2, 31));
		points.add(new Point(9.9, 21.5, 31.7));
		points.add(new Point(10.3, 21.1, 31.1));
		points.add(new Point(10.8, 20.4, 29.2));
		points.add(new Point(10.5, 20.9, 29.1));
		points.add(new Point(9.9, 19.6, 28.8));
		points.add(new Point(9.7, 20.7, 31));
		points.add(new Point(9.3, 19.7, 30.3));
		points.add(new Point(11, 24, 35));
		points.add(new Point(12, 23, 37));
		points.add(new Point(12, 26, 34));
		points.add(new Point(11, 34, 34));
		points.add(new Point(3.4, 2.9, 2.1));
		points.add(new Point(3.1, 2.2, 0.3));
		points.add(new Point(0, 1.6, 0.2));
		points.add(new Point(2.3, 1.6, 2));
		points.add(new Point(0.8, 2.9, 1.6));
		points.add(new Point(3.1, 3.4, 2.2));
		points.add(new Point(2.6, 2.2, 1.9));
		points.add(new Point(0.4, 3.2, 1.9));
		points.add(new Point(2, 2.3, 0.8));
		points.add(new Point(1.3, 2.3, 0.5));
		points.add(new Point(1, 0, 0.4));
		points.add(new Point(0.9, 3.3, 2.5));
		points.add(new Point(3.3, 2.5, 2.9));
		points.add(new Point(1.8, 0.8, 2));
		points.add(new Point(1.2, 0.9, 0.8));
		points.add(new Point(1.2, 0.7, 3.4));
		points.add(new Point(3.1, 1.4, 1));
		points.add(new Point(0.5, 2.4, 0.3));
		points.add(new Point(1.5, 3.1, 1.5));
		points.add(new Point(0.4, 0, 0.7));
		points.add(new Point(3.1, 2.4, 3));
		points.add(new Point(1.1, 2.2, 2.7));
		points.add(new Point(0.1, 3, 2.6));
		points.add(new Point(1.5, 1.2, 0.2));
		points.add(new Point(2.1, 0, 1.2));
		points.add(new Point(0.5, 2, 1.2));
		points.add(new Point(3.4, 1.6, 2.9));
		points.add(new Point(0.3, 1, 2.7));
		points.add(new Point(0.1, 3.3, 0.9));
		points.add(new Point(1.8, 0.5, 3.2));
		points.add(new Point(1.9, 0.1, 0.6));
		points.add(new Point(1.8, 0.5, 3));
		points.add(new Point(3, 0.1, 0.8));
		points.add(new Point(3.1, 1.6, 3));
		points.add(new Point(3.1, 2.5, 1.9));
		points.add(new Point(2.1, 2.8, 2.9));
		points.add(new Point(2.3, 1.5, 0.4));
		points.add(new Point(3.3, 0.6, 1.2));
		points.add(new Point(0.3, 0.4, 3.3));
		points.add(new Point(1.1, 3, 0.3));
		points.add(new Point(0.5, 2.4, 0.9));
		points.add(new Point(1.8, 3.2, 0.9));
		points.add(new Point(1.8, 0.7, 0.7));
		points.add(new Point(2.4, 3.4, 1.5));
		points.add(new Point(1.6, 2.1, 3));
		points.add(new Point(0.3, 1.5, 3.3));
		points.add(new Point(0.4, 3.4, 3));
		points.add(new Point(0.9, 0.1, 0.3));
		points.add(new Point(1.1, 2.7, 0.2));
		points.add(new Point(2.8, 3, 2.9));
		points.add(new Point(2, 0.7, 2.7));
		points.add(new Point(0.2, 1.8, 0.8));
		points.add(new Point(1.6, 2, 1.2));
		points.add(new Point(0.1, 0, 1.1));
		points.add(new Point(2, 0.6, 0.3));
		points.add(new Point(1, 2.2, 2.9));
		points.add(new Point(2.2, 2.5, 2.3));
		points.add(new Point(0.6, 2, 1.5));
		points.add(new Point(0.3, 1.7, 2.2));
		points.add(new Point(0, 2.2, 1.6));
		points.add(new Point(0.3, 0.4, 2.6));
		return points;
	}

	public static Point generatePoint(double xMean, double xStdDev, double yMean, double yStdDev) {
		double x = RANDOM.nextGaussian() * xStdDev + xMean;
		double y = RANDOM.nextGaussian() * yStdDev + yMean;
		return new Point(x, y, 0);
	}

}
