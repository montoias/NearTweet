package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;
import java.util.TreeMap;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

public class PollResultsChart {
	
	private TreeMap<String, ArrayList<String>> answers = new TreeMap<String, ArrayList<String>>();
	private TreeMap<String, ArrayList<Double>> counter = new TreeMap<String, ArrayList<Double>>();
	
	private int[] colors = {Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.RED, Color.YELLOW};
	
	public PollResultsChart() {
	}
	
	public void addNewPoll(String id, ArrayList<String> answers) {
		Log.d("Paulo", "Adding new poll to chart: " + id);
		for(String answer : answers)
			Log.d("Paulo", "\t" + answer);
		this.answers.put(id, answers);
		ArrayList<Double> counters = new ArrayList<Double>(answers.size());
		for(int i = answers.size(); i > 0; i--)
			counters.add(Double.valueOf(0));
		this.counter.put(id, counters);
	}
	
	public void updateCounter(String id, String answer) {
		int index = answers.get(id).indexOf(answer);
		Log.d("Paulo", "Updating poll chart for id " + id + ": " + answer);
		counter.get(id).set(index, counter.get(id).get(index) + 1);
	}
	
	private DefaultRenderer buildCategoryRenderer(int numColors) {
	    DefaultRenderer renderer = new DefaultRenderer();
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setMargins(new int[] { 20, 30, 15, 0 });
	    for (int i = 0; i < numColors; i++) {
	      SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	      r.setColor(colors[i % 8]);
	      renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	  }
	
	private CategorySeries buildCategoryDataset(String title, double[] values, ArrayList<String> answers) {
	    CategorySeries series = new CategorySeries(title);
	    int k = 0;
	    for (double value : values) {
	      series.add(answers.get(k++), value);
	    }

	    return series;
	  }
	
	public Intent execute(Context context, String id, String title) {
		double[] values = new double[counter.get(id).size()];
		for(int i = 0; i < counter.get(id).size(); i++)
			values[i] = counter.get(id).get(i);
		DefaultRenderer renderer = buildCategoryRenderer(values.length);
	    renderer.setZoomButtonsVisible(true);
	    renderer.setZoomEnabled(true);
	    renderer.setChartTitleTextSize(20);
	    return ChartFactory.getPieChartIntent(context, buildCategoryDataset("Answers", values, answers.get(id)),
	        renderer, title);
	}
	
//	public Intent execute(Context context) {
//	    double[] values = new double[] { 12, 14, 11, 10, 19 };
//	    ArrayList<String> labels = new ArrayList<String>();
//	    for(int i = 0; i < values.length; i++)
//	    	labels.add("Label " + i);
//	    DefaultRenderer renderer = buildCategoryRenderer(values.length);
//	    renderer.setZoomButtonsVisible(true);
//	    renderer.setZoomEnabled(true);
//	    renderer.setChartTitleTextSize(20);
//	    return ChartFactory.getPieChartIntent(context, buildCategoryDataset("Project budget", values, labels),
//	        renderer, "Budget");
//	  }

}
