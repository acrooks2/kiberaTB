/*
Parth Chopra, Summer 2013, GMU under mentorship of Dr. Andrew Crooks.
Contact: parthchopra28@gmail.com
 */

package kiberaTB;

//------imports----------------
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialScale;



import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

//-------------------------------

public class KiberaTBWithUI extends GUIState {

	Display2D display; //displaying the model 
	JFrame displayFrame; //frame containing all the displays
	GeomVectorFieldPortrayal roadLinkPortrayal = new GeomVectorFieldPortrayal();
	KiberaTB kibera; //kibera model

	sim.util.media.chart.TimeSeriesChartGenerator chartSeriesTB;
	sim.util.media.chart.TimeSeriesChartGenerator healthChart;

	public KiberaTBWithUI(String[] args) {
		super(new KiberaTB(System.currentTimeMillis(), args));

	}

	public KiberaTBWithUI(SimState state) {
		super(state);

	}

	//-------------Main method----------------------------------
	public static void main(String[] args) {
		KiberaTBWithUI kbUI = new KiberaTBWithUI(args);
		Console c = new Console(kbUI);
		c.setVisible(true);
	}

	//----------MASON simulation functions-------------------------
	public void start() {
		super.start();
		setupPortrayals();
	}

	@Override
	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	//adds visual portrayals to the display
	public void setupPortrayals() {
		kibera = (KiberaTB) state;
		display.detatchAll();

		//-----------------------Adding a portrayal that will display the kibera land---------------------------

		//		FieldPortrayal2D landPortrayal = new ObjectGridPortrayal2D();
		//		landPortrayal.setField(kibera.landGrid);
		//		landPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(66, 245, 245)));
		//		display.attach(landPortrayal, "Land");

		//---------------------Adding the road portrayal------------------------------

		roadLinkPortrayal.setField(kibera.roadLinks);
		roadLinkPortrayal.setPortrayalForAll(new GeomPortrayal(Color.BLACK, 2.0, true));
		display.attach(roadLinkPortrayal, "Roads");

		//--------------------Adding the health facilities portrayal---------------------
		FieldPortrayal2D healthFacilityPortrayal = new SparseGridPortrayal2D();
		healthFacilityPortrayal.setField(kibera.healthFacilityGrid);
		healthFacilityPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(255, 0, 0), 1.0, false));
		display.attach(healthFacilityPortrayal, "HealthFacilities");

		//---------------------Adding the school portrayal--------------------------------
		FieldPortrayal2D schoolFacilityPortrayal = new SparseGridPortrayal2D();
		schoolFacilityPortrayal.setField(kibera.schoolGrid);
		schoolFacilityPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(0, 255, 0), 1.0, false));
		display.attach(schoolFacilityPortrayal, "Schools");

		//--------------------Adding the religious facilities portrayal--------------------
		FieldPortrayal2D religiousFacilityPortrayal = new SparseGridPortrayal2D();
		religiousFacilityPortrayal.setField(kibera.religiousFacilityGrid);
		religiousFacilityPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(0, 0, 102), 1.0, false));
		display.attach(religiousFacilityPortrayal, "ReligiousFacilities");

		//-------------------Adding the resteruants portrayal--------------------------------	
		FieldPortrayal2D restaurantPortrayal = new SparseGridPortrayal2D();
		restaurantPortrayal.setField(kibera.restaurantGrid);
		restaurantPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(102, 0, 102), 1.0, false));
		display.attach(restaurantPortrayal, "restaurants");

		//------------------Adding the water sources portrayal-------------------------------
		FieldPortrayal2D waterPortrayal = new SparseGridPortrayal2D();
		waterPortrayal.setField(kibera.waterSourcesGrid);
		waterPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(0, 102, 102), 1.0, false));
		display.attach(waterPortrayal, "Public Water Sources");

		//------------------Adding the different businesses (temp) portrayal----------------------------
		FieldPortrayal2D businessPortrayal = new SparseGridPortrayal2D();
		businessPortrayal.setField(kibera.businessGrid);
		businessPortrayal.setPortrayalForAll(new RectanglePortrayal2D(new Color(0, 128, 255), 1.0, false));
		display.attach(businessPortrayal, "Businesses");

		//------------------Adding the different households (temp) portrayal----------------------------
		FieldPortrayal2D householdPortrayal = new SparseGridPortrayal2D();
		householdPortrayal.setField(kibera.householdGrid);
		householdPortrayal.setPortrayalForAll(new RectanglePortrayal2D(Color.orange, 1.5, false));
		display.attach(householdPortrayal, "Households");

		ContinuousPortrayal2D residentPortrayal = new ContinuousPortrayal2D();
		residentPortrayal.setField(kibera.world);
		residentPortrayal.setPortrayalForAll(new ResidentPortrayal());
		display.attach(residentPortrayal, "Residentsadfadfdf");

		//------------------Drawing the display-----------------	
		display.reset();
		display.setBackdrop(Color.white);
		// redraw the display
		display.repaint();
		//------------------------------------------------------
	}

	class ResidentPortrayal extends OvalPortrayal2D {
		public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
			if (object == null) {
				System.out.println("null");
				return;
			}

			ResidentTB r = (ResidentTB) object;

			//			if (r.getReligion() == ResidentTB.Religion.Christian)
			//				graphics.setColor(Color.blue);
			//			else
			//				graphics.setColor(Color.red);

			//			if (r.getEthnicity() != null) {
			//				if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(0))
			//					graphics.setColor(Color.blue);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(1))
			//					graphics.setColor(Color.black);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(2))
			//					graphics.setColor(Color.red);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(3))
			//					graphics.setColor(Color.orange);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(4))
			//					graphics.setColor(Color.lightGray);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(5))
			//					graphics.setColor(Color.cyan);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(6))
			//					graphics.setColor(Color.green);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(7))
			//					graphics.setColor(Color.pink);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(8))
			//					graphics.setColor(Color.magenta);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(9))
			//					graphics.setColor(Color.yellow);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(10))
			//					graphics.setColor(Color.black);
			//				else if (r.getEthnicity() == ((KiberaTB) state).getEthnicities(11))
			//					graphics.setColor(Color.darkGray);
			//				else
			//					return;
			//			}

			final Color healthy = new Color(0, 128, 0); // dark green
			final Color exposed = new Color(0, 0, 255); //  blue	0-0-255	255-255-0 184-134-11
			final Color latent =  new Color(199, 21, 133); // magenta
                        final Color infected = new Color(255, 0, 0); // red
			final Color recovered = new Color(0, 205, 205); // cyan 

			if (r.getHealthStatus() == r.susceptible) {
				graphics.setColor(healthy);
			} else if (r.getHealthStatus() == r.exposedTB) {
				graphics.setColor(exposed);
			} else if (r.getHealthStatus() == r.latentInfected) {
				graphics.setColor(latent);
			}else if (r.getHealthStatus() == r.activeInfected) {
				graphics.setColor(infected);
			} else if (r.getHealthStatus() == r.recovered) {
				graphics.setColor(recovered);
			}

			//			//graphics.setColor(Color.blue);

			super.scale = .4;

			paint = graphics.getColor();
			super.draw(r, graphics, info);
		}
	}

	public void init(Controller c) {
		super.init(c);

		display = new Display2D(1050, 625, this); //creates the display
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(false);

		//------Portray activity chart----------------------------
		JFreeChart chart = ChartFactory.createBarChart("Resident's Activity", "Activity", "Percentage", ((KiberaTB) this.state).dataset,
				PlotOrientation.VERTICAL, false, false, false);
		chart.setBackgroundPaint(Color.WHITE);
		chart.getTitle().setPaint(Color.BLACK);

		CategoryPlot p = chart.getCategoryPlot();
		p.setBackgroundPaint(Color.WHITE);
		p.setRangeGridlinePaint(Color.red);

		// set the range axis to display integers only...  
		NumberAxis rangeAxis = (NumberAxis) p.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		int max = 100; //((Dadaab) this.state).getInitialRefugeeNumber();
		rangeAxis.setRange(0, max);

		ChartFrame frame = new ChartFrame("Activity Chart", chart);
		frame.setVisible(false);
		frame.setSize(700, 350);

		frame.pack();
		c.registerFrame(frame);

		//-------------Portray bar graph of infected hotspots----------------------------------------
		JFreeChart infectedChart = ChartFactory.createBarChart("Hotspots for Infected Individuals", "Locations", "Percentage",
				((KiberaTB) this.state).infectedDataSet, PlotOrientation.VERTICAL, false, false, false);
		infectedChart.setBackgroundPaint(Color.WHITE);
		infectedChart.getTitle().setPaint(Color.BLACK);

		CategoryPlot p2 = infectedChart.getCategoryPlot();
		p2.setBackgroundPaint(Color.WHITE);
		p2.setRangeGridlinePaint(Color.blue);

		// set the range axis to display integers only...  
		NumberAxis rangeAxis2 = (NumberAxis) p2.getRangeAxis();
		rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis2.setRange(0, max);

		ChartFrame frame2 = new ChartFrame("Hotspots for Infected Individuals", infectedChart);
		frame2.setVisible(false);
		frame2.setSize(700, 350);

		frame2.pack();
		c.registerFrame(frame2);

		//-------------Portray bar graph of diseased hotspots----------------------------------------
		JFreeChart diseasedChart = ChartFactory.createBarChart("Hotspots for Diseased Individuals", "Locations", "Percentage",
				((KiberaTB) this.state).diseasedDataSet, PlotOrientation.VERTICAL, false, false, false);
		diseasedChart.setBackgroundPaint(Color.WHITE);
		diseasedChart.getTitle().setPaint(Color.BLACK);

		CategoryPlot p3 = diseasedChart.getCategoryPlot();
		p3.setBackgroundPaint(Color.WHITE);
		p3.setRangeGridlinePaint(Color.blue);

		// set the range axis to display integers only...  
		NumberAxis rangeAxis3 = (NumberAxis) p3.getRangeAxis();
		rangeAxis3.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis3.setRange(0, max);

		ChartFrame frame3 = new ChartFrame("Hotspots for Diseased Individuals", diseasedChart);
		frame3.setVisible(false);
		frame3.setSize(700, 350);

		frame3.pack();
		c.registerFrame(frame3);

		//-------------Portray bar graph for age groups of Infected----------------------------------------
		JFreeChart infectedAgeGroupChart = ChartFactory.createBarChart("Age Groups of Infected Individuals", "Age Distribution",
				"Percentage of Total Population", ((KiberaTB) this.state).infectedAgeGroupDataSet, PlotOrientation.VERTICAL, false, false,
				false);
		infectedAgeGroupChart.setBackgroundPaint(Color.WHITE);
		infectedAgeGroupChart.getTitle().setPaint(Color.BLACK);

		CategoryPlot p4 = infectedAgeGroupChart.getCategoryPlot();
		p4.setBackgroundPaint(Color.WHITE);
		p4.setRangeGridlinePaint(Color.blue);

		// set the range axis to display integers only...  
		NumberAxis rangeAxis4 = (NumberAxis) p4.getRangeAxis();
		rangeAxis4.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis4.setRange(0, max);

		ChartFrame frame4 = new ChartFrame("Age Groups of Infected Individuals", infectedAgeGroupChart);
		frame4.setVisible(false);
		frame4.setSize(700, 350);

		frame4.pack();
		c.registerFrame(frame4);

		//-------------Portray bar graph for age groups of diseased----------------------------------------
		JFreeChart diseasedAgeGroupChart = ChartFactory.createBarChart("Age Groups of Diseased Individuals", "Age Distribution",
				"Percentage of Total Population", ((KiberaTB) this.state).diseasedAgeGroupDataSet, PlotOrientation.VERTICAL, false, false,
				false);
		diseasedAgeGroupChart.setBackgroundPaint(Color.WHITE);
		diseasedAgeGroupChart.getTitle().setPaint(Color.BLACK);

		CategoryPlot p5 = diseasedAgeGroupChart.getCategoryPlot();
		p5.setBackgroundPaint(Color.WHITE);
		p5.setRangeGridlinePaint(Color.blue);

		// set the range axis to display integers only...  
		NumberAxis rangeAxis5 = (NumberAxis) p5.getRangeAxis();
		rangeAxis5.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis5.setRange(0, max);

		ChartFrame frame5 = new ChartFrame("Age Groups of Diseased Individuals", diseasedAgeGroupChart);
		frame5.setVisible(false);
		frame5.setSize(700, 350);

		frame5.pack();
		c.registerFrame(frame5);

		//-------------Portray bar graph for age groups----------------------------------------
		JFreeChart AgeGroupChart = ChartFactory.createBarChart("Age Distribution in Kibera", "Age Distribution",
				"Percentage of Total Population", ((KiberaTB) this.state).ageDataSet, PlotOrientation.VERTICAL, false, false, false);
		AgeGroupChart.setBackgroundPaint(Color.WHITE);
		AgeGroupChart.getTitle().setPaint(Color.BLACK);

		CategoryPlot p6 = AgeGroupChart.getCategoryPlot();
		p6.setBackgroundPaint(Color.WHITE);
		p6.setRangeGridlinePaint(Color.blue);

		// set the range axis to display integers only...  
		NumberAxis rangeAxis6 = (NumberAxis) p6.getRangeAxis();
		rangeAxis6.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis6.setRange(0, max);

		ChartFrame frame6 = new ChartFrame("Age Distribution in Kibera", AgeGroupChart);
		frame6.setVisible(false);
		frame6.setSize(700, 350);

		frame6.pack();
		c.registerFrame(frame6);

		//------------Add in graph for portraying SEIR among residents-----------------------------
		Dimension dm = new Dimension(30, 30);
		Dimension dmn = new Dimension(30, 30);

		chartSeriesTB = new sim.util.media.chart.TimeSeriesChartGenerator();
		chartSeriesTB.createFrame();
		chartSeriesTB.setSize(dm);
		chartSeriesTB.setTitle("Health Status");
		chartSeriesTB.setRangeAxisLabel("Number of People");
		chartSeriesTB.setDomainAxisLabel("Days");
		chartSeriesTB.setMaximumSize(dm);
		chartSeriesTB.setMinimumSize(dmn);
		//        chartSeriesTB.setMinimumChartDrawSize(400, 300); // makes it scale at small sizes
		//        chartSeriesTB.setPreferredChartSize(400, 300); // lets it be small

		chartSeriesTB.addSeries(((KiberaTB) this.state).totalsusceptibleSeries, null);
		chartSeriesTB.addSeries(((KiberaTB) this.state).totalExposedSeries, null);
                chartSeriesTB.addSeries(((KiberaTB) this.state).totalLatentSeries, null);
                chartSeriesTB.addSeries(((KiberaTB) this.state).totalLatentExposedTBSeries, null);
		chartSeriesTB.addSeries(((KiberaTB) this.state).totalInfectedSeries, null);
		chartSeriesTB.addSeries(((KiberaTB) this.state).totalRecoveredSeries, null);

		JFrame frameSeries = chartSeriesTB.createFrame(this);
		frameSeries.setVisible(false);
		frameSeries.pack();
		c.registerFrame(frameSeries);

		//Add in chart to show who is infected, diseased, dead 
		healthChart = new sim.util.media.chart.TimeSeriesChartGenerator();
		healthChart.createFrame();
		healthChart.setSize(dm);
		healthChart.setTitle("Health Condition");
		healthChart.setRangeAxisLabel("Number of People");
		healthChart.setDomainAxisLabel("Days");
		healthChart.setMaximumSize(dm);
		healthChart.setMinimumSize(dmn);

		healthChart.addSeries(((KiberaTB) this.state).totalFullyHealthySeries, null);
                healthChart.addSeries(((KiberaTB) this.state).totalExposedTBSeries, null);
                healthChart.addSeries(((KiberaTB) this.state).totalLatentTBSeries, null);
                healthChart.addSeries(((KiberaTB) this.state).totalLatentExposedTBSeries, null);
		healthChart.addSeries(((KiberaTB) this.state).totaInfectedTBSeries, null);
		healthChart.addSeries(((KiberaTB) this.state).totalContagiousSeries, null);
		healthChart.addSeries(((KiberaTB) this.state).totalDeadSeries, null);

		JFrame healthConditionFrame = healthChart.createFrame(this);
		healthConditionFrame.setVisible(false);
		healthConditionFrame.pack();
		c.registerFrame(healthConditionFrame);

		//------------Inserting a clock to see what time-------------
		StandardDialFrame dialFrame = new StandardDialFrame();
		DialBackground ddb = new DialBackground(Color.white);
		dialFrame.setBackgroundPaint(Color.lightGray);
		dialFrame.setForegroundPaint(Color.darkGray);

		DialPlot plot = new DialPlot();
		plot.setView(0.0, 0.0, 1.0, 1.0);
		plot.setBackground(ddb);
		plot.setDialFrame(dialFrame);

		plot.setDataset(0, ((KiberaTB) this.state).hourDialer);
		plot.setDataset(1, ((KiberaTB) this.state).dayDialer);

		DialTextAnnotation annotation1 = new DialTextAnnotation("Hour");
		annotation1.setFont(new Font("Dialog", Font.BOLD, 14));
		annotation1.setRadius(0.1);
		plot.addLayer(annotation1);

		DialValueIndicator dvi2 = new DialValueIndicator(1);
		dvi2.setFont(new Font("Dialog", Font.PLAIN, 22));
		dvi2.setOutlinePaint(Color.red);
		dvi2.setRadius(0.3);
		plot.addLayer(dvi2);

		DialTextAnnotation annotation2 = new DialTextAnnotation("Day");
		annotation2.setFont(new Font("Dialog", Font.BOLD, 18));
		annotation2.setRadius(0.4);
		plot.addLayer(annotation2);

		StandardDialScale scale = new StandardDialScale(0.0, 23.99, 90, -360, 1.0, 59);
		scale.setTickRadius(0.9);
		scale.setTickLabelOffset(0.15);
		scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
		plot.addScale(0, scale);
		scale.setMajorTickPaint(Color.black);
		scale.setMinorTickPaint(Color.lightGray);

		DialPointer needle = new DialPointer.Pointer(0);
		plot.addPointer(needle);

		DialCap cap = new DialCap();
		cap.setRadius(0.10);
		plot.setCap(cap);

		JFreeChart timeChart = new JFreeChart(plot);
		ChartFrame timeframe = new ChartFrame("Time Chart", timeChart);
		timeframe.setVisible(false);
		timeframe.setSize(200, 100);
		timeframe.pack();
		c.registerFrame(timeframe);

		Dimension dl = new Dimension(300, 700);
		LegendTB legend = new LegendTB();
		legend.setSize(dl);

		JFrame legendframe = new JFrame();
		legendframe.setVisible(false);
		legendframe.setPreferredSize(dl);
		legendframe.setSize(300, 700);

		legendframe.setBackground(Color.white);
		legendframe.setTitle("Legend");
		legendframe.getContentPane().add(legend);
		legendframe.pack();
		c.registerFrame(legendframe);
	}

	public void quit() {
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null;
		display = null;

	}

}
