package application;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

	private static final int width = 800, height = 600;

	public static void main(final String[ ] args) {

		launch(args);
	}

	private ImageView		viewer;

	private PixelReader		reader;

	private Color[ ][ ]		colors;

	private PixelWriter		writer;

	private WritableImage	editedImage;

	private double			circleRadius;

	private byte			minNumColors	= 2;

	private final Pane		truePane		= new Pane( );

	private Color			circleColor		= Color.BLACK;

	private String			currentView		= "";

	private boolean			drawPerimeters	= false;

	private WritableImage	original;

	private boolean			written			= false;

	private void computeCircles( ) {

		if (written) {
			for (int x = 0; x < original.getWidth( ); x++) {
				for (int y = 0; y < original.getHeight( ); y++) {
					writer.setColor(x, y, original.getPixelReader( ).getColor(x, y));
				}
			}
		}

		double offset = circleRadius / Math.sqrt(2);
		double distance = circleRadius;

		for (double i = 0; i < circleRadius; i++) {
			writer.setColor((int) i, 0, circleColor);
		}

		for (double x = offset; x < (editedImage.getWidth( ) - offset); x += distance) {
			for (double y = offset; y < (editedImage.getHeight( ) - offset); y += distance) {
				Circle c = new Circle(x, y, circleRadius);

				if (c.hasNumColors(minNumColors)) {
					if (drawPerimeters) {
						for (double theta = 0; theta < (Math.PI * 2); theta += .05) {
							int xLoc = (int) (x + (Math.cos(theta) * circleRadius));
							int yLoc = (int) (y + (Math.sin(theta) * circleRadius));

							if ((xLoc >= 0) && (yLoc >= 0)) {
								writer.setColor(xLoc, yLoc, circleColor);
							}
						}
					}

					writer.setColor((int) x, (int) y, circleColor);
					writer.setColor((int) x + 1, (int) y, circleColor);
					writer.setColor((int) x - 1, (int) y, circleColor);
					writer.setColor((int) x, (int) y + 1, circleColor);
					writer.setColor((int) x, (int) y - 1, circleColor);

				}
			}
		}

		written = true;

	}

	private void setScreenElements(final GridPane grid, final Stage stage) throws FileNotFoundException {

		int pos = 0;

		FileChooser chooser = new FileChooser( );
		chooser.setInitialDirectory(new File("."));
		Button fileOpen = new Button("Open Biome Map");
		fileOpen.setOnAction(e -> {
			File imageFile = chooser.showOpenDialog(stage);
			try {
				setViewer(imageFile, grid);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace( );
			}
		});
		grid.add(fileOpen, pos++, 0);

		Label radius = new Label("Radius of Search Circles:");
		grid.add(radius, pos++, 0);

		TextField size = new TextField( );
		size.setOnAction(e -> {
			circleRadius = Double.valueOf(size.getText( ));
			System.out.println("Circle Radius is now " + circleRadius);
		});
		grid.add(size, pos++, 0);

		ToggleButton drawCircumfrence = new ToggleButton("Draw Circle Perimeters");
		drawCircumfrence.setOnAction(e -> {
			drawPerimeters = drawCircumfrence.isSelected( );
		});
		grid.add(drawCircumfrence, pos++, 0);

		Label numColors = new Label("Minimum Number of Colors:");
		grid.add(numColors, pos++, 0);

		TextField min = new TextField( );
		min.setOnAction(e -> {
			minNumColors = Byte.parseByte(min.getText( ));
			System.out.println("Minimum Colors is now " + minNumColors);
		});
		grid.add(min, pos++, 0);

		Button compute = new Button("Compute");
		compute.setOnAction(e -> {
			if (!min.getText( ).isEmpty( ) && !size.getText( ).isEmpty( )) {
				minNumColors = Byte.parseByte(min.getText( ));
				System.out.println("Minimum Colors is now " + minNumColors);

				circleRadius = Double.valueOf(size.getText( ));
				System.out.println("Circle Radius is now " + circleRadius);

				computeCircles( );

			}
		});
		grid.add(compute, pos++, 0);

		Button saveMap = new Button("Save Map");
		saveMap.setOnAction(e -> {
			File f = new File("res/" + currentView + "_r-" + circleRadius + "_mc-" + minNumColors + "_dc-" + drawPerimeters + ".png");
			RenderedImage image = SwingFXUtils.fromFXImage(editedImage, null);
			try {
				ImageIO.write(image, "png", f);
			} catch (IOException e1) {
				e1.printStackTrace( );
			}
		});
		grid.add(saveMap, pos++, 0);
	}

	private void setViewer(final File img, final Pane pane) throws FileNotFoundException {

		if (img == null) return;

		Image image = new Image(new FileInputStream(img));
		currentView = img.getName( );
		viewer = new ImageView( );
		viewer.setImage(image);
		viewer.setFitWidth(1750);
		viewer.setPreserveRatio(true);

		if (!image.isError( )) {
			System.out.println("Successfully read " + img.getPath( ));
			reader = image.getPixelReader( );
			original = new WritableImage((int) image.getWidth( ), (int) image.getHeight( ));
			editedImage = new WritableImage((int) image.getWidth( ), (int) image.getHeight( ));
			writer = editedImage.getPixelWriter( );
			colors = new Color[(int) image.getWidth( )][(int) image.getHeight( )];

			PixelWriter origWrite = original.getPixelWriter( );

			for (int x = 0; x < image.getWidth( ); x++) {
				for (int y = 0; y < image.getHeight( ); y++) {
					colors[x][y] = reader.getColor(x, y);
					writer.setColor(x, y, colors[x][y]);
					origWrite.setColor(x, y, colors[x][y]);

					if (colors[x][y] == circleColor) {
						System.out.println("Found center color on map. May be difficult to read.");
					}
				}
			}

			Circle.setMap(colors);
			viewer.setImage(editedImage);
			viewer.relocate(0, 50);
			truePane.getChildren( ).add(viewer);
		}

	}

	@Override
	public void start(final Stage primaryStage) {

		try {

			truePane.setMinWidth(width);
			truePane.setPrefWidth(width);

			GridPane controls = new GridPane( );
			controls.setMinSize((width * 3) / 4, (height * 3) / 4);
			controls.setVgap(5);
			controls.setHgap(5);

			truePane.getChildren( ).add(controls);

			Scene scene = new Scene(truePane);

			setScreenElements(controls, primaryStage);

			primaryStage.setScene(scene);
			primaryStage.setTitle("KSP Biome Finder");
			primaryStage.setMinWidth(truePane.getMinWidth( ));
			primaryStage.setMinHeight(height);
			primaryStage.show( );

		} catch (Exception e) {
			e.printStackTrace( );
		}

		System.out.println("Success on starting");
	}

	@Override
	public void stop( ) {

	}
}
