/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.casemodule;

import javax.swing.JPanel;

public interface DecryptionProvider {

	/**
	 *
	 * @param volumeMetaData
	 */
	public void setVolumeMetaData(VolumeMetaData volumeMetaData);

	public void start();

	public void stop();

	public String getRawDeviceLocation();

	public String getName();

	public JPanel getPanel();

	/**
	 *
	 * @param volumeMetaData
	 * @return
	 */
	public boolean matchesVolume(VolumeMetaData volumeMetaData);
}
