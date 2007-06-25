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

package net.sf.mzmine.modules.normalization.simplestandardcompound;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.impl.SimpleParameterSet;
import net.sf.mzmine.io.RawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.userinterface.Desktop;
import net.sf.mzmine.userinterface.components.ExtendedCheckBox;
import net.sf.mzmine.userinterface.dialogs.ExitCode;
import net.sf.mzmine.util.PeakListRowSorterByMZ;
import net.sf.mzmine.util.GUIUtils;

class SimpleStandardCompoundNormalizerDialog extends JDialog implements ActionListener {

    static final int PADDING_SIZE = 5;

    private ExitCode exitCode = ExitCode.UNKNOWN;

    private Desktop desktop;
    
    private PeakList alignmentResult;
    private SimpleStandardCompoundNormalizerParameterSet parameters;
    
    private Vector<PeakListRow> selectedPeaks;

    // dialog components
    private JComboBox availableNormalizationTypesCombo;
    private ExtendedCheckBox<PeakListRow> peakCheckBoxes[];
    private JButton btnDeselectAllPeaks, btnOK, btnCancel;

    public SimpleStandardCompoundNormalizerDialog(Desktop desktop,
            PeakList alignmentResult,
            SimpleStandardCompoundNormalizerParameterSet parameters) {

        // make dialog modal
        super(desktop.getMainFrame(), "Simple standard compound normalizer setup dialog", true);

        this.desktop = desktop;
        
        this.alignmentResult = alignmentResult;
        this.parameters = parameters;

        GridBagConstraints constraints = new GridBagConstraints();

        // set default layout constraints
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(PADDING_SIZE, PADDING_SIZE,
                PADDING_SIZE, PADDING_SIZE);

        constraints.gridwidth = 1;
        constraints.gridheight = 1;

        JComponent comp;
        GridBagLayout layout = new GridBagLayout();

        JPanel components = new JPanel(layout);

        comp = GUIUtils.addLabel(components, "Normalized using ");
        constraints.gridx = 0;
        constraints.gridy = 1;
        layout.setConstraints(comp, constraints);

        Object[] availableNormalizationTypes = SimpleStandardCompoundNormalizerParameterSet.StandardUsageTypePossibleValues;
        availableNormalizationTypesCombo = new JComboBox(availableNormalizationTypes);
        constraints.gridx = 1;
        components.add(availableNormalizationTypesCombo, constraints);

        comp = GUIUtils.addLabel(components, "Select standard peaks");
        constraints.gridx = 0;
        constraints.gridy = 3;
        layout.setConstraints(comp, constraints);

        JPanel peakCheckBoxesPanel = new JPanel();
        peakCheckBoxesPanel.setBackground(Color.white);
        peakCheckBoxesPanel.setLayout(new BoxLayout(peakCheckBoxesPanel,
                BoxLayout.Y_AXIS));
        
        Vector<ExtendedCheckBox<PeakListRow>> peakCheckBoxesVector = new Vector<ExtendedCheckBox<PeakListRow>>(); 
        int minimumHorizSize = 0;
        PeakListRow rows[] = alignmentResult.getRows();
        Arrays.sort(rows, new PeakListRowSorterByMZ());
        for (int i = 0; i < rows.length; i++) {
        	// Add only fully detected peaks to list of potential standard peaks
        	if (rows[i].getNumberOfPeaks()==alignmentResult.getNumberOfRawDataFiles()) {
        		ExtendedCheckBox ecb = new ExtendedCheckBox<PeakListRow>(rows[i], true);
        		ecb.setSelected(false);
	            peakCheckBoxesVector.add(ecb);
	            minimumHorizSize = Math.max(minimumHorizSize,
	                    ecb.getPreferredWidth());
	            peakCheckBoxesPanel.add(ecb);
        	}
        }
        // If there are no peaks that are fully detected, then std compound normalization is not possible
        if (peakCheckBoxesVector.size()==0) {
        	desktop.displayErrorMessage("Aligned peak list does not contain any peaks that are detected in each raw data file.");
        }
        
        peakCheckBoxes = peakCheckBoxesVector.toArray(new ExtendedCheckBox[0]);
        int minimumVertSize = new JCheckBox().getHeight();
        if ((peakCheckBoxes!=null) && (peakCheckBoxes.length>0))
        	minimumVertSize = (int) peakCheckBoxes[0].getPreferredSize().getHeight() * 6;
        JScrollPane peakPanelScroll = new JScrollPane(peakCheckBoxesPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        peakPanelScroll.setPreferredSize(new Dimension(minimumHorizSize,
                minimumVertSize));
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        components.add(peakPanelScroll, constraints);

        comp = GUIUtils.addSeparator(components, PADDING_SIZE);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 2;
        layout.setConstraints(comp, constraints);

        JPanel buttonsPanel = new JPanel();
        btnOK = GUIUtils.addButton(buttonsPanel, "OK", null, this);
        btnCancel = GUIUtils.addButton(buttonsPanel, "Cancel", null, this);
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 2;
        components.add(buttonsPanel, constraints);

        GUIUtils.addMargin(components, PADDING_SIZE);
        add(components);

        // finalize the dialog
        pack();
        setLocationRelativeTo(desktop.getMainFrame());
        setResizable(true);

    }

    public void actionPerformed(ActionEvent event) {

        Object src = event.getSource();

        if (src == btnOK) {

        	if (availableNormalizationTypesCombo.getSelectedItem()==SimpleStandardCompoundNormalizerParameterSet.StandardUsageTypeNearest)
        		parameters.getParameters().setParameterValue(SimpleStandardCompoundNormalizerParameterSet.StandardUsageType, SimpleStandardCompoundNormalizerParameterSet.StandardUsageTypeNearest);
        
        	if (availableNormalizationTypesCombo.getSelectedItem()==SimpleStandardCompoundNormalizerParameterSet.StandardUsageTypeWeighted)
        		parameters.getParameters().setParameterValue(SimpleStandardCompoundNormalizerParameterSet.StandardUsageType, SimpleStandardCompoundNormalizerParameterSet.StandardUsageTypeWeighted);
        	
        	
            selectedPeaks = new Vector<PeakListRow>();

            for (ExtendedCheckBox<PeakListRow> box : peakCheckBoxes) {
                if (box.isSelected())
                    selectedPeaks.add(box.getObject());
            }          
            

            if (selectedPeaks.size() == 0) {
                desktop.displayErrorMessage(
                        "Please select at least one peak");
                return;
            }

            parameters.setSelectedStandardPeakListRows(selectedPeaks.toArray(new PeakListRow[0]));
            
            exitCode = ExitCode.OK;
            dispose();
            return;
        }

        if (src == btnCancel) {
            exitCode = ExitCode.CANCEL;
            dispose();
            return;
        }

        if (src == btnDeselectAllPeaks) {
            for (JCheckBox box : peakCheckBoxes)
                box.setSelected(false);
            return;
        }

    }

    public ExitCode getExitCode() {
        return exitCode;
    }
    
    public SimpleStandardCompoundNormalizerParameterSet getParameters() {
    	
    		
    	return parameters;
    }
    
    public PeakListRow[] getSelectedStandardPeakListRows() {
    	return selectedPeaks.toArray(new PeakListRow[0]);
    }

}