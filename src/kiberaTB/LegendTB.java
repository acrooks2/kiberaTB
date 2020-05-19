/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kiberaTB;

/**
 *
 * @author gmu
 */
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.*;
import java.awt.geom.Line2D;
import javax.swing.*;

public class LegendTB extends Canvas {

	public void paint(Graphics legend) {

		//        legend.setColor(Color.red);
		//        legend.drawRect(10, 10, 260, 660);
		//        

		Graphics2D leg = (Graphics2D) legend;
		leg.scale(0.7, 0.7);
		Line2D line = new Line2D.Double(20, 97, 70, 107);
		leg.setColor(Color.lightGray);
		leg.setStroke(new BasicStroke(3));
		leg.draw(line);

		// agent 
		leg.setColor(new Color(0, 128, 0));
		leg.fillOval(20, 150, 20, 20);

		leg.setColor(new Color(0, 0, 255)); //244, 165, 130
		leg.fillOval(20, 180, 20, 20);

		leg.setColor(new Color(255, 0, 0));
		leg.fillOval(20, 210, 20, 20);

		leg.setColor(new Color(102, 0, 102));
		leg.fillOval(20, 240, 20, 20);

		// camps
		leg.setColor(Color.orange);
		leg.drawRect(20, 295, 30, 30);

		leg.setColor(new Color(0, 128, 255));
		leg.drawRect(20, 335, 30, 30);

		// facilities
		leg.setColor(new Color(255, 0, 0));
		leg.drawRect(20, 400, 30, 30);

		leg.setColor(new Color(0, 255, 0));
		leg.drawRect(20, 440, 30, 30);

		leg.setColor(new Color(0, 0, 102));
		leg.drawRect(20, 480, 30, 30);

		leg.setColor(new Color(102, 0, 102));
		leg.drawRect(20, 520, 30, 30);

		leg.setColor(new Color(0, 102, 102));
		leg.drawRect(20, 560, 30, 30);

		// Graphics2D fontL = (Graphics2D)legend;
		Font f = new Font("Arial", Font.BOLD + Font.ITALIC, 30);
		leg.setFont(f);

		leg.setColor(Color.black);

		leg.drawString("LEGEND", 30, 70);

		Font f2 = new Font("Serif", Font.BOLD, 18);
		leg.setFont(f2);

		leg.setColor(Color.black);

		leg.drawString("Agent's TB Status", 20, 138);

		leg.drawString("Households and Businesses", 20, 285);
		leg.drawString("Kibera Facilities", 20, 390);

		Font f3 = new Font("Serif", Font.PLAIN, 20);
		leg.setFont(f3);

		leg.setColor(Color.black);

		leg.drawString("Road", 90, 110);
		//legend.drawString("River", 90, 115);

		leg.drawString("Susceptible", 70, 165);
		leg.drawString("Exposed", 70, 195);
		leg.drawString("Infected", 70, 225);
		leg.drawString("Recovered", 70, 255);

		leg.drawString("Household", 70, 315);
		leg.drawString("Business", 70, 355);

		leg.drawString("Health Facilities", 70, 422);
		leg.drawString("Schools", 70, 462);
		leg.drawString("Religious Facilities", 70, 502);
		leg.drawString("Restaurants", 70, 542);
		leg.drawString("Public Water Sources", 70, 582);
		

	}

}
