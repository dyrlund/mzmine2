package net.sf.mzmine.modules.dataanalysis.rtmzplots;

import java.util.Vector;
import java.util.logging.Logger;

import net.sf.mzmine.data.Peak;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.impl.SimpleParameterSet;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.util.CollectionUtils;
import net.sf.mzmine.util.MathUtils;

import org.jfree.data.xy.AbstractXYZDataset;

public class CVDataset extends AbstractXYZDataset implements RTMZDataset {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private float[] xCoords = new float[0];
	private float[] yCoords = new float[0];
	private float[] colorCoords = new float[0];
	private PeakListRow[] peakListRows = new PeakListRow[0];
	
	private String datasetTitle;
	
	public CVDataset(PeakList alignedPeakList, RawDataFile[] selectedFiles, SimpleParameterSet parameters) {
		int numOfRows = alignedPeakList.getNumberOfRows();
		
		boolean useArea = true;
		if (parameters.getParameterValue(RTMZAnalyzer.MeasurementType)==RTMZAnalyzer.MeasurementTypeHeight)
			useArea = false;
			
		// Generate title for the dataset
		datasetTitle = "Correlation of variation analysis";
		datasetTitle = datasetTitle.concat(" (");
		if (useArea) 
			datasetTitle = datasetTitle.concat("CV of peak areas");
		else
			datasetTitle = datasetTitle.concat("CV of peak heights");
		datasetTitle = datasetTitle.concat(" in " + selectedFiles.length + " files");
		datasetTitle = datasetTitle.concat(")");
		
		logger.finest("Computing: " + datasetTitle);

		// Loop through rows of aligned peak list
		Vector<Float> xCoordsV = new Vector<Float>();
		Vector<Float> yCoordsV = new Vector<Float>();
		Vector<Float> colorCoordsV = new Vector<Float>();
		Vector<PeakListRow> peakListRowsV = new Vector<PeakListRow>();

		for (int rowIndex=0; rowIndex<numOfRows; rowIndex++) {
			
			PeakListRow row = alignedPeakList.getRow(rowIndex);
			
			// Collect available peak intensities for selected files
			Vector<Float> peakIntensities = new Vector<Float>(); 
			for (int fileIndex=0; fileIndex<selectedFiles.length; fileIndex++) {
				Peak p = row.getPeak(selectedFiles[fileIndex]);
				if (p!=null) {
					if (useArea)
						peakIntensities.add(p.getArea());
					else 
						peakIntensities.add(p.getHeight());
				}
			}
			
			// If there are at least two measurements available for this peak then calc CV and include this peak in the plot
			if (peakIntensities.size()>1) {
				float[] ints = CollectionUtils.toFloatArray(peakIntensities);
				Float cv = MathUtils.calcCV(ints);
				
				Float rt = row.getAverageRT();
				Float mz = row.getAverageMZ();
				
				xCoordsV.add(rt);
				yCoordsV.add(mz);
				colorCoordsV.add(cv);
				peakListRowsV.add(row);
				
			} 
	
		}

		// Finally store all collected values in arrays
		xCoords = CollectionUtils.toFloatArray(xCoordsV);
		yCoords = CollectionUtils.toFloatArray(yCoordsV);
		colorCoords = CollectionUtils.toFloatArray(colorCoordsV);
		peakListRows = peakListRowsV.toArray(new PeakListRow[0]);
		
	}
	
	public String toString() {
		return datasetTitle;
	}
	
	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		if (series==0) return new Integer(1); else return null;
	}

	public Number getZ(int series, int item) {
		if (series!=0) return null;
		if ((colorCoords.length-1)<item) return null;
		return colorCoords[item];
	}

	public int getItemCount(int series) {
		return xCoords.length;
	}

	public Number getX(int series, int item) {
		if (series!=0) return null;
		if ((xCoords.length-1)<item) return null;
		return xCoords[item];
	}

	public Number getY(int series, int item) {
		if (series!=0) return null;
		if ((yCoords.length-1)<item) return null;
		return yCoords[item];	
	}
	
	public PeakListRow getPeakListRow(int item) {
		return peakListRows[item];
	}

}
