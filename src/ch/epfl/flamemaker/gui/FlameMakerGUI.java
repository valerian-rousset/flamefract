package ch.epfl.flamemaker.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ch.epfl.flamemaker.color.Color;
import ch.epfl.flamemaker.color.InterpolatedPalette;
import ch.epfl.flamemaker.color.Palette;
import ch.epfl.flamemaker.flame.Flame;
import ch.epfl.flamemaker.flame.Flame.Builder;
import ch.epfl.flamemaker.flame.FlameAccumulator;
import ch.epfl.flamemaker.flame.FlameTransformation;
import ch.epfl.flamemaker.geometry2d.AffineTransformation;
import ch.epfl.flamemaker.geometry2d.Point;
import ch.epfl.flamemaker.geometry2d.Rectangle;

/**
 * The GUI, use much of {@link Flame} in internal
 * 
 * @see Flame
 */
public class FlameMakerGUI {
	/**
	 * The {@link Builder} we are currently working on
	 */
	private Flame.Builder	builder;
	/**
	 * The {@link Color} of the background we use to build the image
	 */
	private Color		background;
	/**
	 * The {@link Palette} we use to build the image
	 */
	private Palette		palette;
	/**
	 * The scope of the fractal
	 */
	private Rectangle	frame;
	/**
	 * The number of iteration
	 */
	private int		density;

	/**
	 * Construct a {@link FlameMakerGUI} with the default value
	 */
	public FlameMakerGUI() {
		this.builder = FlameMakerGUI.generateSharkFin();
		this.background = Color.BLACK;
		this.palette = new InterpolatedPalette(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE));
		this.frame = new Rectangle(new Point(-0.25, 0), 5, 4);
		this.density = 50;
	}

	/**
	 * Generate the GUI, used by {@link FlameMaker}
	 */
	public void start() {
		JFrame frame = new JFrame("Flame Maker");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setVisible(true);

		JPanel panelFract = new JPanel();
		panelFract.setLayout(new BorderLayout());
		// FlameBuilderPreviewComponent fractal = new
		// FlameBuilderPreviewComponent(this.builder, this.background,
		// this.palette, this.frame, this.density);
		// panelFract.add(fractal, BorderLayout.CENTER);
		panelFract.setBorder(BorderFactory.createTitledBorder("Fractale"));

		JPanel panelAffine = new JPanel();
		panelAffine.setLayout(new BorderLayout());
		AffineTransformationsComponent transformations = new AffineTransformationsComponent(this.builder,
				this.frame);
		panelAffine.add(transformations, BorderLayout.CENTER);
		panelAffine.setBorder(BorderFactory.createTitledBorder("Transformations affines"));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout());
		panel.add(panelAffine);
		panel.add(panelFract);

		frame.getContentPane().add(panel, BorderLayout.CENTER);

		frame.pack();
	}

	/**
	 * Generate the Shark Fin fractal
	 * 
	 * @return A {@link Flame} containing the fractal
	 */
	private static Flame.Builder generateSharkFin() {
		final Flame.Builder builder = new Flame.Builder(new Flame(new ArrayList<FlameTransformation>()));
		final double[][] array = { { 1, 0.1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0.8, 1 }, { 1, 0, 0, 0, 0, 0 } };

		AffineTransformation affine = new AffineTransformation(-0.4113504, -0.7124804, -0.4, 0.7124795,
				-0.4113508, 0.8);
		builder.addTransformation(new FlameTransformation(affine, array[0]));

		affine = new AffineTransformation(-0.3957339, 0, -1.6, 0, -0.3957337, 0.2);
		builder.addTransformation(new FlameTransformation(affine, array[1]));

		affine = new AffineTransformation(0.4810169, 0, 1, 0, 0.4810169, 0.9);
		builder.addTransformation(new FlameTransformation(affine, array[2]));

		return builder;
	}

	/**
	 * The fractal part of the GUI
	 */
	static class FlameBuilderPreviewComponent extends JComponent {

		/**
		 * The {@link Builder} we are currently working on
		 */
		private Flame.Builder	builder;
		/**
		 * The {@link Color} of the background we use to build the image
		 */
		private Color		background;
		/**
		 * The {@link Palette} we use to build the image
		 */
		private Palette		palette;
		/**
		 * The scope of the fractal
		 */
		private Rectangle	frame;
		/**
		 * The number of iteration
		 */
		private int		density;

		public FlameBuilderPreviewComponent(Flame.Builder builder, Color background, Palette palette,
				Rectangle frame, int density) {
			this.builder = builder;
			this.background = background;
			this.palette = palette;
			this.frame = frame;
			this.density = density;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200, 100);
		}

		@Override
		protected void paintComponent(Graphics g0) {
			BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(),
					BufferedImage.TYPE_INT_RGB);

			Rectangle actualFrame = frame.expandToAspectRatio(this.getWidth() / (double) this.getHeight());
			FlameAccumulator accu = this.builder.build().compute(actualFrame, this.getWidth(),
					this.getHeight(), this.density);

			for (int x = 0; x < accu.width(); x++) {
				for (int y = 0; y < accu.height(); y++) {
					final Color c = accu.color(palette, this.background, x, y);
					final int RGB = c.asPackedRGB();
					image.setRGB(x, accu.height() - 1 - y, RGB);
				}
			}

			g0.drawImage(image, 0, 0, null);
		}
	}

	static class AffineTransformationsComponent extends JComponent {
		private Flame.Builder	builder;
		private Rectangle	frame;
		private int		highlightedTransformationIndex;

		public AffineTransformationsComponent(Builder builder, Rectangle frame) {
			this.builder = builder;
			this.frame = frame;
		}

		public int getHighlightedTransformationIndex() {
			return highlightedTransformationIndex;
		}

		public void setHighlightedTransformationIndex(int highlightedTransformationIndex) {
			if (highlightedTransformationIndex < 0
					|| highlightedTransformationIndex >= this.builder.transformationCount()) {
				throw new NoSuchElementException();
			}
			this.highlightedTransformationIndex = highlightedTransformationIndex;
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200, 100);
		}

		private void paintGrid(Graphics2D g) {
			AffineTransformation transformation = AffineTransformation.newTranslation(this.frame.left()
					+ (this.getWidth() / 2.0), 2 + this.frame.bottom() + (this.getHeight() / 2.0));
//			transformation = transformation.composeWith(AffineTransformation.newScaling(this.getWidth()
//					/ this.frame.width(), this.getHeight() / this.frame.height()));

			System.out.println(this.frame);
			System.out.println(this.getWidth() + "," + this.getHeight());
			System.out.println(transformation.transformPoint(new Point(this.frame.left(), this.frame.bottom())));
			System.out.println(transformation.transformPoint(new Point(this.frame.right(), this.frame.top())));

			// wrong color TODO
			g.setColor(java.awt.Color.GRAY);

			for (int x = (int) this.frame.left(); x < this.frame.right(); x++) {

				System.out.println(x);

				Point up = new Point(x, this.frame.top());
				Point down = new Point(x, this.frame.bottom());

				System.out.println(up + "|" + down);

				up = transformation.transformPoint(up);
				down = transformation.transformPoint(down);

				System.out.println(up + "|" + down);

				Line2D.Double line = new Line2D.Double(down.x(), down.y(), up.x(), up.y());
				g.draw(line);
			}
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D) g0;

			this.paintGrid(g);

			AffineTransformation transformation = AffineTransformation.newTranslation(
					this.getWidth() / 2.0, -this.getHeight() / 2.0);
			Point origin = transformation.transformPoint(Point.ORIGIN);

			Rectangle actualFrame = frame.expandToAspectRatio(this.getWidth() / (double) this.getHeight());

			for (int i = 0; i < this.builder.transformationCount(); i++) {
				if (i == this.highlightedTransformationIndex) {
					g.setColor(java.awt.Color.RED);
				} else {
					g.setColor(java.awt.Color.BLACK);
				}

				Point p = origin;
				p = this.builder.affineTransformation(i).transformPoint(p);

				Line2D.Double asd = new Line2D.Double(origin.x(), origin.y(), (int) p.x(), (int) p.y());
				g.draw(asd);
			}
		}
	}
}
