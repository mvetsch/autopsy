/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.casemodule;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.sleuthkit.datamodel.SleuthkitJNI;
import org.sleuthkit.datamodel.TskCoreException;

public class AddImageWizardConfigureEncryptionPanel implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

	private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

	private AddImageWizardChooseDataSourcePanel dsPanel;
	private AddImageWizardConfigureEncryptionVisual visual;

	private String lastPath = "";
	private List<DecryptionProvider> decryptionProviders;

	/**
	 *
	 * @param dsPanel
	 */
	public AddImageWizardConfigureEncryptionPanel(AddImageWizardChooseDataSourcePanel dsPanel) {
		this.dsPanel = dsPanel;
		visual = new AddImageWizardConfigureEncryptionVisual(this);
	}

	@Override
	public Component getComponent() {
		WizardDescriptor settings = null;
		ImageFilePanel imageFilePanel = (ImageFilePanel) dsPanel.getComponent().getCurrentDSProcessor().getPanel();
		System.out.println(ObjectUtils.identityToString(this));
		if (!lastPath.equals(imageFilePanel.getContentPaths())) {
			lastPath = imageFilePanel.getContentPaths();
			lastPath = "/media/disk/images/forensic/forensic-win08.raw.dd";
			List<DecryptionProvider> cr = getEncryptionConfigurationPanelsForImage(lastPath);
			decryptionProviders = cr;
			visual.updateProviderList(cr);
		}

		return visual;
	}

	@Override
	public HelpCtx getHelp() {
		return HelpCtx.DEFAULT_HELP;
	}

	@Override
	public void readSettings(WizardDescriptor data) {
		return;
	}

	@Override
	public void storeSettings(WizardDescriptor data) {
		return;
	}

	@Override
	public boolean isValid() {
		if (decryptionProviders == null) {
			return true;
		}
		boolean result = true;
		for (DecryptionProvider dp : decryptionProviders) {
			result &= dp.getPanel().isValid();
		}
		return true;
	}

	@Override
	public void addChangeListener(ChangeListener cl) {
		synchronized (listeners) {
			listeners.add(cl);
		}
	}

	@Override
	public void removeChangeListener(ChangeListener cl) {
		synchronized (listeners) {
			listeners.remove(cl);
		}
	}

	private List<DecryptionProvider> getEncryptionConfigurationPanelsForImage(String contentPath) {
		List<DecryptionProvider> result = new LinkedList<>();

		if (!new File(contentPath).isFile()) {
			return result;
		}
		List<VolumeMetaData> karl = getVolumeMetaData(contentPath);

		for (VolumeMetaData m : karl) {
			DecryptionProvider u = getDecryptionProvider(m);
			if (u != null) {
				result.add(u);
			}
		}

		return result;
	}

	private DecryptionProvider getDecryptionProvider(VolumeMetaData volumeMetaData) {
		for (DecryptionProvider decryptionProvider : Lookup.getDefault().lookupAll(DecryptionProvider.class)) {
			if (decryptionProvider.matchesVolume(volumeMetaData)) {
				decryptionProvider.setVolumeMetaData(volumeMetaData);
				return decryptionProvider;
			}
		}
		return null;
	}

	private List<VolumeMetaData> getVolumeMetaData(String image) {
		String[] imageArray = {image};

		List<VolumeMetaData> result = new LinkedList<>();

		try {
			long handle = SleuthkitJNI.openImage(imageArray);
			long vsHandle = SleuthkitJNI.openVs(handle, 0);

			for (int volumeIndex = 0; volumeIndex < 5; volumeIndex++) {
				VolumeMetaData volumeMetaData = new VolumeMetaData();
				volumeMetaData.setPartitionNumber(volumeIndex);

				volumeMetaData.setOffSet(SleuthkitJNI.getVolOffset(vsHandle, volumeIndex));

				long volumePointer = SleuthkitJNI.openVsPart(vsHandle, volumeIndex);

				byte readBuffer[] = new byte[512];
				long c = SleuthkitJNI.readVsPart(volumePointer, readBuffer, 0, 512);
				volumeMetaData.setFirstSector(readBuffer);

				volumeMetaData.setPath(image);

				result.add(volumeMetaData);
			}

		} catch (TskCoreException ex) {
			Exceptions.printStackTrace(ex);
		}
		return result;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Iterator<ChangeListener> it;
		synchronized (listeners) {
			it = new HashSet<>(listeners).iterator();
		}
		ChangeEvent ev = new ChangeEvent(this);
		while (it.hasNext()) {
			it.next().stateChanged(ev);
		}
	}

}
