package itree;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.NonNull;

/**
 * 
 * @author theophile
 *
 */
@Data
public class IsolationTreeNode<T> {

	private Function<T, Double> splitAttribute;
	private double splitValue;
	private IsolationTreeNode<T> left;
	private IsolationTreeNode<T> right;
	private int size;

	public IsolationTreeNode(@NonNull List<T> values, @NonNull List<Function<T, Double>> attributes) {
		this.splitAttribute = attributes.get(new Random().nextInt(attributes.size()));
		double min = values.stream().mapToDouble(splitAttribute::apply).min().getAsDouble();
		double max = values.stream().mapToDouble(splitAttribute::apply).max().getAsDouble();
		splitValue = min + Math.random() * max;
		List<T> leftValues = values.stream().filter(x -> splitAttribute.apply(x) < splitValue)
				.collect(Collectors.toList());
		List<T> rightValues = values.stream().filter(x -> splitAttribute.apply(x) >= splitValue)
				.collect(Collectors.toList());
		if (leftValues.isEmpty() || rightValues.isEmpty()) {
			size = values.size();
			return;
		}
		left = new IsolationTreeNode<>(leftValues, attributes);
		right = new IsolationTreeNode<>(rightValues, attributes);
	}

	public boolean isExternal() {
		return left == null && right == null;
	}

	public boolean isInternal() {
		return left != null & right != null;
	}

	public boolean isIndeterminate() {
		return !isExternal() && !isInternal();
	}
}
