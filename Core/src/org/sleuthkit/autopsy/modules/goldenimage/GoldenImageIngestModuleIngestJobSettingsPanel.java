/*
 * Sample module ingest job settings panel in the public domain.  
 * Feel free to use this as a template for your module ingest job settings
 * panels.
 * 
 *  Contact: Brian Carrier [carrier <at> sleuthkit [dot] org]
 *
 *  This is free and unencumbered software released into the public domain.
 *  
 *  Anyone is free to copy, modify, publish, use, compile, sell, or
 *  distribute this software, either in source code form or as a compiled
 *  binary, for any purpose, commercial or non-commercial, and by any
 *  means.
 *  
 *  In jurisdictions that recognize copyright laws, the author or authors
 *  of this software dedicate any and all copyright interest in the
 *  software to the public domain. We make this dedication for the benefit
 *  of the public at large and to the detriment of our heirs and
 *  successors. We intend this dedication to be an overt act of
 *  relinquishment in perpetuity of all present and future rights to this
 *  software under copyright law.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE. 
 */
package org.sleuthkit.autopsy.modules.goldenimage;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;
import org.sleuthkit.autopsy.ingest.IngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestJobSettingsPanel;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettings;
import org.sleuthkit.autopsy.ingest.IngestModuleIngestJobSettingsPanel;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * UI component used to make per ingest job settings for sample ingest modules.
 */
public class GoldenImageIngestModuleIngestJobSettingsPanel extends IngestModuleIngestJobSettingsPanel {
	
    //private final IngestJobSettingsPanel ingestJobSettingsPanel;

	public IngestJobSettingsPanel ingestJobSettingsPanel;
	public static int counter = 0;
	
	private GoldenImageModuleIngestJobSettings settings;
    /**
     * Creates new form SampleIngestModuleIngestJobSettings
     */
    public GoldenImageIngestModuleIngestJobSettingsPanel(GoldenImageModuleIngestJobSettings pSettings) {
	    this.settings = pSettings;
	    /*
	    counter++;
	    if(counter > 1){
		    return;
	    }
	IngestJobSettings ingestJobSettings = new IngestJobSettings(GoldenImageGlobalSettingsPanel.class.getCanonicalName());
	this.ingestJobSettingsPanel = new IngestJobSettingsPanel(ingestJobSettings);*/
	    
        initComponents();
        customizeComponents(settings);
	/*
	IngestJobSettings ingestJobSettings = new IngestJobSettings(GoldenImageIngestModuleIngestJobSettingsPanel.class.getCanonicalName());
        //showWarnings(ingestJobSettings);
        this.ingestJobSettingsPanel = new IngestJobSettingsPanel(ingestJobSettings);*/
    }

	
    
    
    private ArrayList<Content> getDatasources(){
	    Case currentCase = Case.getCurrentCase();
	    ArrayList<Content> listDS = new ArrayList<>();
	    try {
		    listDS.addAll(currentCase.getDataSources());
		    
	    } catch (TskCoreException ex) {
		    java.util.logging.Logger.getLogger(GoldenImageModuleIngestJobSettings.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    
	    return listDS;
    }

    private void customizeComponents(GoldenImageModuleIngestJobSettings settings) {
	/*** Prepare and fill Combobox with Images ***/
	cbGoldenImage.removeAllItems();
	ArrayList<Content> datasources = getDatasources();
	if(datasources != null && !datasources.isEmpty()){
		DefaultComboBoxModel<DataSourceCBWrapper> cbModel = new DefaultComboBoxModel<>();
		for(Content c : datasources){
			DataSourceCBWrapper dsCBWrapper = new DataSourceCBWrapper(c);
			cbModel.addElement(dsCBWrapper);
			
			//Set selected Datasource if its given in the Settings
			if(settings.getDataSourceID() == c.getId()){
				cbModel.setSelectedItem(dsCBWrapper);
			}
		}
		cbGoldenImage.setModel(cbModel);
	}
	
	//Set first selected DS
	DataSourceCBWrapper dsWrapper = (DataSourceCBWrapper)cbGoldenImage.getSelectedItem();
	settings.setSelectedDatasource(dsWrapper.getContent());
	
	ingestPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ingestPanel.setLayout(new BorderLayout());
        //ingestPanel.add(this.ingestJobSettingsPanel, BorderLayout.CENTER);
	
	cbGoldenImage.addActionListener((ActionEvent e) -> {
		DataSourceCBWrapper dsTmpWrapper = (DataSourceCBWrapper)cbGoldenImage.getSelectedItem();
	    settings.setSelectedDatasource(dsTmpWrapper.getContent());
	    GIManager.getInstance().setGoldenImageContent(dsTmpWrapper.getContent());
	    System.out.println(settings.getSelectedDatasource().getName()+" <<name");
	});
	
    }

    /**
     * Gets the ingest job settings for an ingest module.
     *
     * @return The ingest settings.
     */
    @Override
    public IngestModuleIngestJobSettings getSettings() {
        return this.settings;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                txtTitle = new javax.swing.JLabel();
                txtDescription = new javax.swing.JLabel();
                txtSelectGI = new javax.swing.JLabel();
                cbGoldenImage = new javax.swing.JComboBox<DataSourceCBWrapper>();
                ingestPanel = new javax.swing.JPanel();

                txtTitle.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
                org.openide.awt.Mnemonics.setLocalizedText(txtTitle, org.openide.util.NbBundle.getMessage(GoldenImageIngestModuleIngestJobSettingsPanel.class, "GoldenImageIngestModuleIngestJobSettingsPanel.txtTitle.text")); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(txtDescription, org.openide.util.NbBundle.getMessage(GoldenImageIngestModuleIngestJobSettingsPanel.class, "GoldenImageIngestModuleIngestJobSettingsPanel.txtDescription.text")); // NOI18N

                org.openide.awt.Mnemonics.setLocalizedText(txtSelectGI, org.openide.util.NbBundle.getMessage(GoldenImageIngestModuleIngestJobSettingsPanel.class, "GoldenImageIngestModuleIngestJobSettingsPanel.txtSelectGI.text")); // NOI18N

                javax.swing.GroupLayout ingestPanelLayout = new javax.swing.GroupLayout(ingestPanel);
                ingestPanel.setLayout(ingestPanelLayout);
                ingestPanelLayout.setHorizontalGroup(
                        ingestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
                );
                ingestPanelLayout.setVerticalGroup(
                        ingestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
                );

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(ingestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(267, 267, 267))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(txtTitle)
                                                        .addComponent(txtDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txtSelectGI)
                                                        .addComponent(cbGoldenImage, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(38, 38, 38))))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(txtTitle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtDescription)
                                .addGap(18, 18, 18)
                                .addComponent(txtSelectGI)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbGoldenImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(ingestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                );
        }// </editor-fold>//GEN-END:initComponents
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JComboBox<DataSourceCBWrapper> cbGoldenImage;
        private javax.swing.JPanel ingestPanel;
        private javax.swing.JLabel txtDescription;
        private javax.swing.JLabel txtSelectGI;
        private javax.swing.JLabel txtTitle;
        // End of variables declaration//GEN-END:variables
}
