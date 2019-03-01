package itree;

import lombok.Data;

/**
 * 
 * @author theophile
 *
 */
@Data
public class Point {

	private double x;
	private double y;
	private double z;

	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double norm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

}
