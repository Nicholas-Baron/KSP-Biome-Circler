package application;

import java.util.ArrayList;

import javafx.scene.paint.Color;

public class Circle {

	private static Color[ ][ ] map;

	private static Color getColorAt(final int x, final int y) {

		if ((x < 0) || (y < 0) || (x >= map.length) || (y >= map[0].length)) return null;

		return map[x][y];
	}

	public static Color[ ][ ] getMap( ) {

		return map;
	}

	public static void setMap(final Color[ ][ ] map) {

		Circle.map = map;
	}

	private final double x, y, r;

	public Circle(final double x, final double y, final double circleRadius) {

		this.x = x;
		this.y = y;
		r = circleRadius;
	}

	public boolean hasNumColors(final byte numColors) {

		ArrayList<Color> seen = new ArrayList<>( );
		seen.add(map[(int) x][(int) y]);

		for (double theta = 0; theta < (Math.PI * 2); theta += .1) {
			for (double rad = 0; rad < r; rad += .5) {
				int xLoc = (int) (x + (Math.cos(theta) * r));
				int yLoc = (int) (y + (Math.sin(theta) * r));

				Color c = getColorAt(xLoc, yLoc);

				if ((c != null) && !seen.contains(c)) {
					seen.add(c);
				}
			}
		}
		seen.trimToSize( );
		return seen.size( ) >= numColors;
	}

}
