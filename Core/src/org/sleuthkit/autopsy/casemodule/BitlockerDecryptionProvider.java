package org.sleuthkit.autopsy.casemodule;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorCallback;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorProgressMonitor;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

@ServiceProvider(service = DecryptionProvider.class)
public class BitlockerDecryptionProvider implements DecryptionProvider {

    private static final byte bitlockerMagicString[] = "-FVE-FS-        ".getBytes();
    private static final String dislockerImageName = "dislocker-file";
    private static final String mountBaseDir = "/media/";

    private ConfigureBitlockerPanel panel;

    private VolumeMetaData volumeMetaData;
    private String mountPointPath;
    private final String deviceId;
    private AddImageTask addImageTask;
    private AddImageWizardChooseDataSourceVisual.DataSourceConfiguration datasourceConfiguration;

    public BitlockerDecryptionProvider() {
        this.deviceId = UUID.randomUUID().toString();
        panel = new ConfigureBitlockerPanel();
    }

    private BitlockerDecryptionProvider(VolumeMetaData volumeMetaData, String key, KeyType keyType, String deviceId) {

        panel = new ConfigureBitlockerPanel() {
            @Override
            public KeyType getKeyType() {
                return keyType;
            }

            @Override
            public String getKeyValue() {
                return key;
            }
        };
        this.deviceId = deviceId;
        setVolumeMetaData(volumeMetaData);
    }

    @Override
    public DecryptionProvider decryptionProviderFactory(VolumeMetaData volumeMetaData, AddImageWizardChooseDataSourceVisual.DataSourceConfiguration dataSourceConfiguration) {
        BitlockerDecryptionProvider result = new BitlockerDecryptionProvider();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        result.setVolumeMetaData(volumeMetaData);
        return result;
    }

    private void setVolumeMetaData(VolumeMetaData volumeMetaData) {
        this.volumeMetaData = volumeMetaData;
        this.panel.setTitle("Set Bitlocker Keys for Volume " + volumeMetaData.getPartitionNumber());
    }

    public void addEncryptionInformationToCaseDB() {
        SleuthkitCase caseDB = Case.getCurrentCase().getSleuthkitCase();
        try {
            caseDB.insertDecryptionInformation(
                    this.getClass().toString(),
                    this.getKeyValue(),
                    this.volumeMetaData.getPath(),
                    this.deviceId,
                    this.getKeyType().ordinal(),
                    (int) this.volumeMetaData.getPartitionNumber());

        } catch (TskCoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private KeyType getKeyType() {
        return panel.getKeyType();
    }

    private String getKeyValue() {
        return panel.getKeyValue();
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public boolean matchesVolume(VolumeMetaData volumeMetaData) {

        boolean result = true;
        for (int index = 0; index <= 8; index++) {
            result &= bitlockerMagicString[index] == volumeMetaData.getFirstSector()[index + 3];
        }
        return result;
    }

    @Override
    public String getDataSourceType() {
        return "Bitlocker Volume";
    }

    @Override
    public boolean isPanelValid() {
        return this.panel.isValid();
    }

    @Override
    public void run(DataSourceProcessorProgressMonitor progressMonitor, DataSourceProcessorCallback callback) {
        this.addEncryptionInformationToCaseDB();
        if (startDislockerProcess()) {
            addImageTask = new AddImageTask(deviceId, getRawDeviceLocation(), datasourceConfiguration.timeZone, datasourceConfiguration.getOrphanFiles, progressMonitor, callback);
            new Thread(addImageTask).start();
        } else {
            List<String> errors = new LinkedList<>();
            errors.add("Unable to unlock dislocker Volume");
            callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errors, new LinkedList<>());
        }
    }

    @Override
    public void cancel() {
        addImageTask.cancelTask();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void setDataSourceConfiguration(AddImageWizardChooseDataSourceVisual.DataSourceConfiguration dataSourceConfiguration) {
        this.datasourceConfiguration = dataSourceConfiguration;
    }

    @Override
    public DecryptionProvider decryptionProviderFactory(VolumeMetaData volumeMetaData, String key, int keyType, String deviceId) {
        KeyType type = KeyType.values()[keyType];
        return new BitlockerDecryptionProvider(volumeMetaData, key, type, deviceId);
    }

    @Override
    public void start() {
        this.startDislockerProcess();
    }

    @Override
    public void stop() {
        this.umount();
    }

    public enum KeyType {
        USER_PASSWORD, RECOVERY_KEY, BEK_FILE
    }

    private boolean startDislockerProcess() {
        this.mountPointPath = createMountPoint();
        return dislock(volumeMetaData);
    }

    private void umount() {
        List<String> command = new LinkedList<>();
        command.add("umount");
        command.add(mountPointPath);
        try {
            Process a = new ProcessBuilder(command).start();
            a.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    private String getRawDeviceLocation() {
        return mountPointPath + "/" + dislockerImageName;
    }

    private String createMountPoint() {
        String mountPointPath = mountBaseDir + "/" + this.deviceId;

        File mountPoint = new File(mountPointPath);
        if (!mountPoint.exists()) {
            mountPoint.mkdir();
        }

        return mountPointPath;
    }

    private boolean dislock(VolumeMetaData volumeMetaData) {
        List<String> command = new LinkedList<>();
        command.add("dislocker");
        command.add("-o");
        command.add(Long.toString(this.getOffset(volumeMetaData)));
        command.add("-V");
        command.add(volumeMetaData.getPath());

        if (null != this.getKeyType()) {
            switch (this.getKeyType()) {
                case RECOVERY_KEY:
                    command.add("-p" + this.getKeyValue());
                    break;
                case USER_PASSWORD:
                    command.add("-u" + this.getKeyValue());
                    break;
                case BEK_FILE:
                    command.add("-f");
                    command.add(this.getKeyValue());
                    break;
                default:
                    break;
            }
        }

        command.add("--");
        command.add(this.mountPointPath);

        try {
            Process dislockerInitProcess = new ProcessBuilder(command).start();
            dislockerInitProcess.waitFor();
            return dislockerInitProcess.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private long getOffset(VolumeMetaData volumeMetaData) {
        return volumeMetaData.getOffSet() * 512;
    }
}
