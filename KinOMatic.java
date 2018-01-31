package up1project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

//object used to store motion parameters
class KinematicsSolver{
	private Double vf, vi, xf, xi, a, t;
	
	KinematicsSolver(Double vf, Double vi, Double xf, Double xi, Double a, Double t){
		this.vf = vf;
		this.vi = vi;
		this.xf = xf;
		this.xi = xi;
		this.a = a;
		this.t = t;
	}
	
	//getter methods
	
	Double returnVf(){
		return vf;
	}
	
	Double returnVi(){
		return vi;
	}
	
	Double returnXf(){
		return xf;
	}
	
	Double returnXi(){
		return xi;
	}
	
	Double returnA(){
		return a;
	}
	
	Double returnT(){
		return t;
	}
	
	//calculate method
	
	void calculateUnknowns(){
		if(getNullCount() > 2){
			JOptionPane.showMessageDialog(null, "Cannot solve for more than two unknowns!");
			return;
		}
		while(vf == null | vi == null | xf == null | xi == null | a == null | t == null){
			vf = getVf();
			vi = getVi();
			xf = getXf();
			xi = getXi();
			a = getA();
			t = getT();
		}
		
		if(t < 0){
			t *= -1;
		}
	}
	
	//show values
	
	void showValues(){
		JOptionPane.showMessageDialog(null, 
				"vf: " + vf +
				"\nvi: " + vi +
				"\nxf: " + xf +
				"\nxi: " + xi +
				"\na: " + a +
				"\nt: " + t
		);
	}
	
	private int getNullCount(){
		int nullCounter = 0;
		Double[] varArray = {vf, vi, xf, xi, a, t};
		
		for(Double var : varArray){
			if(var == null) nullCounter++;
		}
		
		return nullCounter;
	}
	
	//isolated expression methods
	
	private Double getVf(){
		if(vf != null) return vf;
		if(vi != null & a != null & t != null){
			return vi + a * t;
		}else if(vi != null & a != null & xf != null & xi != null){
			return Math.sqrt(Math.pow(vi, 2) + 2 * a * (xf - xi));
		}else return vf;
	}
	
	private Double getVi(){
		if(vi != null) return vi;
		if(vf != null & a != null & t != null){ 
			return vf - a * t;
		}else if(xf != null & xi != null & a != null & t != null){
			return (xf - xi - (0.5 * a * Math.pow(t, 2))) / t;
		}else if(xf != null & xi != null & a != null & vf != null){
			return Math.sqrt(Math.pow(vf, 2) - (2 * a * (xf - xi)));
		}else return vi;
	}
	
	private Double getXf(){
		if(xf != null) return xf;
		if(xi != null & vi != null & a != null & t != null){
			return xi + vi * t + (0.5 * a * Math.pow(t, 2));
		}else if(vf != null & vi != null & a != null & xi != null){
			return ((Math.pow(vf, 2) - Math.pow(vi, 2)) / (2 * a)) + xi;
		}else return xf;
	}
	
	private Double getXi(){
		if(xi != null) return xi;
		if(xf != null & vi != null & t != null & a != null){
			return xf - vi * t - 0.5 * a * Math.pow(t, 2);
		}else if(vf != null & vi != null & a != null & xf != null){
			return ((Math.pow(-vf, 2) + Math.pow(vi, 2)) / (2 * a)) + xf;
		}else return xi;
	}
	
	private Double getA(){
		if(a != null) return a;
		if(vf != null & vi != null & t != null){
			return (vf - vi) / t;
		}else if(xf != null & xi != null & vi != null & t != null){
			return (xf - xi - vi * t) / (0.5 * Math.pow(t, 2));
		}else if(vf != null & vi != null & xf != null & xi != null){
			return (Math.pow(vf, 2) - Math.pow(vi, 2)) / (2 * (xf - xi));
		}else return a;
	}
	
	private Double getT(){
		if(t != null) return t;
		if(vf != null & vi != null & a != null & a != 0){
			return (vf - vi) / a;
		}else if(a != null & vi != null & xi != null & xf != null & a != 0){
			double value = (-vi + Math.sqrt(Math.pow(vi,2) - 4 * (0.5 * a) * (xi - xf))) / a;
			if(value < 0){
				value = (-vi - Math.sqrt(Math.pow(vi,2) - 4 * (0.5 * a) * (xi - xf))) / a;
			}
			return value;
		}else if(xf != null & xi != null & vi != null){
			return (xf - xi) / vi;
		}else return t;
	}
	
	public String toString(){
		String description = "vf = " + vf + ", vi = " + vi + ", xf = " + xf + ", xi = " + xi +
				", a = " + a + ", t = " + t;
		return description;
	}
}

//main class that houses GUI and main method
class KinOMatic implements ActionListener, ListSelectionListener{
	JFrame jfrm;
	JPanel calcPanel;
	JButton calculate, graphIt;
	JList<KinematicsSolver> motionList;
	JScrollPane listScroller;
	JPopupMenu popupMenu;
	JMenuItem removeElements, removeAllElements;
	JTextField vfField, viField, xfField, xiField, aField, tField;
	
	int SAMPLES = 20;
	
	//construct and initialize GUI components
	KinOMatic(){
		jfrm = new JFrame("Kin-O-Matic 9000");
		jfrm.setLayout(new FlowLayout());
		jfrm.setSize(300, 300);
		jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		vfField = new JTextField(10);
		vfField.setBorder(new TitledBorder("Final Velocity"));
		
		viField = new JTextField(10);
		viField.setBorder(new TitledBorder("Initial Velocity"));
		
		xfField = new JTextField(10);
		xfField.setBorder(new TitledBorder("Final Position"));
		
		xiField = new JTextField(10);
		xiField.setBorder(new TitledBorder("Initial Position"));
		
		aField = new JTextField(10);
		aField.setBorder(new TitledBorder("Acceleration"));
		
		tField = new JTextField(10);
		tField.setBorder(new TitledBorder("Time"));
		
		graphIt = new JButton("Graph It!");
		graphIt.addActionListener(this);
		graphIt.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		calculate = new JButton("Calculate!");
		calculate.addActionListener(this);
		calculate.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		motionList = new JList<KinematicsSolver>();
		motionList.setModel(new DefaultListModel<KinematicsSolver>());
		motionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		motionList.addListSelectionListener(this);
		listScroller = new JScrollPane(motionList);
		
		popupMenu = new JPopupMenu();
		removeElements = new JMenuItem("Remove Element(s)");
		removeElements.setActionCommand("RemoveElements");
		removeElements.addActionListener(this);
		removeAllElements = new JMenuItem("Remove All");
		removeAllElements.setActionCommand("RemoveAll");
		removeAllElements.addActionListener(this);
		popupMenu.add(removeElements);
		popupMenu.add(removeAllElements);
		
		calcPanel = new JPanel();
		calcPanel.setLayout(new BoxLayout(calcPanel, BoxLayout.Y_AXIS));
		calcPanel.add(vfField);
		calcPanel.add(viField);
		calcPanel.add(xfField);
		calcPanel.add(xiField);
		calcPanel.add(aField);
		calcPanel.add(tField);
		calcPanel.add(calculate);
		calcPanel.add(graphIt);
		calcPanel.add(listScroller);
		
		jfrm.add(calcPanel);
		jfrm.pack();
		jfrm.setVisible(true);
		
		motionList.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent me){
				if(me.isPopupTrigger()){
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}
			
			public void mousePressed(MouseEvent me){
				if(me.isPopupTrigger()){
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}
			
			public void mouseClicked(MouseEvent me){
				if(me.isPopupTrigger()){
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		});
	}
	
	//action listener for buttons and menu items
	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();
		DefaultListModel<KinematicsSolver> dlm = (DefaultListModel<KinematicsSolver>) motionList.getModel();
		
		switch(actionCommand){
		case "Calculate!":
			Double vf, vi, xf, xi, a, t;
			vf = null;
			vi = null;
			xf = null;
			xi = null;
			a = null;
			t = null;
			
			try{
				vf = Double.parseDouble(vfField.getText());
			}catch(Exception e){;}
			try{
				vi = Double.parseDouble(viField.getText());
			}catch(Exception e){;}
			try{
				xf = Double.parseDouble(xfField.getText());
			}catch(Exception e){;}
			try{
				xi = Double.parseDouble(xiField.getText());
			}catch(Exception e){;}
			try{
				a = Double.parseDouble(aField.getText());
			}catch(Exception e){;}
			try{
				t = Double.parseDouble(tField.getText());
			}catch(Exception e){;}
			
			if(t != null && t < 0){
				JOptionPane.showMessageDialog(jfrm, "Negative time is not a thing, unfortunately...");
				return; 
			}
			
			KinematicsSolver solver = new KinematicsSolver(vf, vi, xf, xi, a, t);
			solver.calculateUnknowns();
			dlm.addElement(solver);
			break;
		case "Graph It!":
			showGraphDialog();
			break;
		case "RemoveElements":
			if(motionList.getValueIsAdjusting()) return;
			Object[] elements = motionList.getSelectedValuesList().toArray();
			
			for(Object element : elements){
				dlm.removeElement(element);
			}
			break;
		case "RemoveAll":
			dlm.removeAllElements();
			break;
		}
	}
	
	public void valueChanged(ListSelectionEvent lse) {
		KinematicsSolver solver = motionList.getSelectedValue();
		
		try{
			vfField.setText(solver.returnVf().toString());
			viField.setText(solver.returnVi().toString());
			xfField.setText(solver.returnXf().toString());
			xiField.setText(solver.returnXi().toString());
			aField.setText(solver.returnA().toString());
			tField.setText(solver.returnT().toString());
		}catch(Exception e){}
	}
	
	//generates plots for position graph
	private XYSeriesCollection createPositionDataset() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		DefaultListModel<KinematicsSolver> dlm = (DefaultListModel<KinematicsSolver>) motionList.getModel();
		
		double startTime = 0, endTime = 0;
		String key = "a";
		for(int i = 0; i < dlm.getSize(); i++){
			KinematicsSolver solver = dlm.getElementAt(i);
			
			startTime = endTime;
			endTime = solver.returnT() + startTime;
			
			double vi = solver.returnVi();
			double xi = solver.returnXi();
			double a = solver.returnA();
			
			XYSeries series = new XYSeries(key);
			
			double t;
			double pos = 0;
			for(t=0; t <= (endTime - startTime); t += ((endTime - startTime) / SAMPLES)){
				pos = xi + (vi * t) + (0.5 * a * Math.pow(t, 2));
				series.add(t + startTime, pos);
			}
			
			//fill in any gaps
			pos = xi + (vi * t) + (0.5 * a * Math.pow(t, 2));
			series.add(endTime, pos);
			
			dataSet.addSeries(series);
			key += "a";
		}
		return dataSet;
	 }
	
	//generates plots for velocity graph
	private XYSeriesCollection createVelocityDataset() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		DefaultListModel<KinematicsSolver> dlm = (DefaultListModel<KinematicsSolver>) motionList.getModel();
		
		double startTime = 0, endTime = 0;
		String key = "a";
		for(int i = 0; i < dlm.getSize(); i++){
			KinematicsSolver solver = dlm.getElementAt(i);
			
			startTime = endTime;
			endTime = solver.returnT() + startTime;
			
			double vi = solver.returnVi();
			double a = solver.returnA();
			XYSeries series = new XYSeries(key);
			
			double t;
			double vel = 0;
			for(t=0; t <= (endTime - startTime); t += ((endTime - startTime) / SAMPLES)){
				vel = vi + a * t;
				series.add(t + startTime, vel);
			}
			
			//fill in any gaps
			vel = vi + a * t;
			series.add(endTime, vel);
			
			dataSet.addSeries(series);
			key += "a";
		}
		return dataSet;
	}
	
	//generates plots for acceleration graph
	private XYSeriesCollection createAccelerationDataset() {
		XYSeriesCollection dataSet = new XYSeriesCollection();
		DefaultListModel<KinematicsSolver> dlm = (DefaultListModel<KinematicsSolver>) motionList.getModel();
		
		double startTime = 0, endTime = 0;
		String key = "a";
		for(int i = 0; i < dlm.getSize(); i++){
			KinematicsSolver solver = dlm.getElementAt(i);
			
			startTime = endTime;
			endTime = solver.returnT() + startTime;
			
			key += "a";
			double a = solver.returnA();
			
			XYSeries series = new XYSeries(key);
			
			for(double t=0; t <= (endTime - startTime); t += ((endTime - startTime) / SAMPLES)){
				series.add(t + startTime, a);
			}
			
			series.add(endTime, a);
			
			dataSet.addSeries(series);
			key += "a";
		}
		return dataSet;
	 }
	 
	//constructs graph dialog upon hitting "Graph It!" button
	 private void showGraphDialog(){
		 JDialog graphDialog = new JDialog(jfrm, "Motion Graphs");
		 graphDialog.setLayout(new BorderLayout());
		 graphDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		 graphDialog.setLocationRelativeTo(jfrm);
		 graphDialog.setSize(100, 100);
		 
		 JPanel graphPanel = new JPanel();
		 graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
		 
		 JFreeChart posChart, velChart, accelChart;
		 
		 posChart = ChartFactory.createXYLineChart("Position vs. Time", "Time", "Position", createPositionDataset(), PlotOrientation.VERTICAL , false, true, false);
		 posChart.getXYPlot().setRenderer(new XYSplineRenderer());
		 velChart = ChartFactory.createXYLineChart("Velocity vs. Time", "Time", "Velocity", createVelocityDataset(), PlotOrientation.VERTICAL, false, true, false);
		 velChart.getXYPlot().setRenderer(new XYSplineRenderer());
		 accelChart = ChartFactory.createXYLineChart("Acceleration vs. Time", "Time", "Acceleration", createAccelerationDataset(), PlotOrientation.VERTICAL, false, true, false);
		 accelChart.getXYPlot().setRenderer(new XYSplineRenderer());
		 
	     ChartPanel posChartPanel = new ChartPanel(posChart);
	     ChartPanel velChartPanel = new ChartPanel(velChart);
	     ChartPanel accelChartPanel = new ChartPanel(accelChart);
	     
	     JPanel findPanel = new JPanel();
		 findPanel.setLayout(new FlowLayout());
	     
	     JTextField timeField = new JTextField(8);
	     timeField.setBorder(new TitledBorder("Time Value"));
	     JLabel posLabel, velLabel, accelLabel;
	     JButton getValue;
	     
	     posLabel = new JLabel("Displacement: N/A");
	     velLabel = new JLabel("Velocity: N/A");
	     accelLabel = new JLabel("Acceleration: N/A");
	     getValue = new JButton("Get Values");
	     
	     graphPanel.add(posChartPanel);
	     graphPanel.add(velChartPanel);
	     graphPanel.add(accelChartPanel);
	     
	     findPanel.add(timeField);
	     findPanel.add(getValue);
	     findPanel.add(posLabel);
	     findPanel.add(velLabel);
	     findPanel.add(accelLabel);
	     
	     JPanel mainPanel = new JPanel();
	     mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
	     mainPanel.add(graphPanel);
	     mainPanel.add(findPanel);
	     
		 graphDialog.add(mainPanel, BorderLayout.CENTER);
		 graphDialog.pack();
		 graphDialog.setVisible(true);
		 
		 //provides position, velocity, and acceleration values for certain time
		 getValue.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent ae){
				 double targetValue;
				 DefaultListModel<KinematicsSolver> dlm = (DefaultListModel<KinematicsSolver>) motionList.getModel();
				 XYSeriesCollection collection = (XYSeriesCollection) posChart.getXYPlot().getDataset();
				 
				 try{
					 targetValue = Double.parseDouble(timeField.getText());
				 }catch(Exception e){
					 JOptionPane.showMessageDialog(graphDialog, "Value must be a number!");
					 return;
				 }
				 
				 if(targetValue > collection.getDomainUpperBound(true) || targetValue < collection.getDomainLowerBound(true)){
					 JOptionPane.showMessageDialog(graphDialog, "The value must be within the domain!");
					 return;
				 }
				 
				 double seriesStart = 0;
				 for(int i=0; i<dlm.getSize(); i++){
					 XYSeries series = collection.getSeries(i);
					 double start = series.getMinX();
					 double end = series.getMaxX();
					 
					 
					 if(targetValue <= end & targetValue >= start){
						 KinematicsSolver solver = dlm.getElementAt(i);
						 double initPos = solver.returnXi();
						 double initVel = solver.returnVi();
						 double accelVal = solver.returnA();
						 double time = targetValue - seriesStart;
						 
						 double position = initPos + initVel * time + 0.5 * accelVal * Math.pow(time, 2);
						 double velocity = initVel + accelVal * time;
						 
						 posLabel.setText("Position: " + position);
						 velLabel.setText("Velocity: " + velocity);
						 accelLabel.setText("Acceleration: " + accelVal);
						 return;
					 }
					 
					 seriesStart += end - start;
				 }
			 }
		 });
	}
	
	//main method of execution
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new KinOMatic();
			}
		});
	}
}