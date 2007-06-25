/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.alignment.filterbygaps;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.impl.SimpleParameterSet;
import net.sf.mzmine.data.impl.SimplePeakList;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.taskcontrol.Task;

class GapsFilterTask implements Task {

    private PeakList originalPeakList;
    private SimplePeakList processedPeakList;
    private TaskStatus status;
    private String errorMessage;

    private float processedAlignmentRows;
    private float totalAlignmentRows;

    private int minPresent;

    public GapsFilterTask(PeakList alignmentResult,
            SimpleParameterSet parameters) {
        status = TaskStatus.WAITING;
        originalPeakList = alignmentResult;
        minPresent = (Integer) parameters.getParameterValue(GapsFilter.minPresent);
    }

    public void cancel() {
        status = TaskStatus.CANCELED;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public float getFinishedPercentage() {
        return processedAlignmentRows / totalAlignmentRows;
    }

    public Object getResult() {
        return processedPeakList;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getTaskDescription() {
        return "Filter alignment result by gaps.";
    }

    public void run() {

        status = TaskStatus.PROCESSING;

        totalAlignmentRows = originalPeakList.getNumberOfRows();
        processedAlignmentRows = 0;

        // Create new alignment result and add opened raw data files to it
        processedPeakList = new SimplePeakList(
                "Result after filtering by gaps");
        
        for (RawDataFile rawData : originalPeakList.getRawDataFiles()) {
            processedPeakList.addRawDataFile(rawData);
        }

        // Copy rows with enough peaks to new alignment result
        for (PeakListRow alignmentRow : originalPeakList.getRows()) {
            
            if (status == TaskStatus.CANCELED)
                return;
            
            if (alignmentRow.getNumberOfPeaks() >= minPresent)
                processedPeakList.addRow(alignmentRow);
            
            processedAlignmentRows++;
            
        }

        status = TaskStatus.FINISHED;

    }

}
