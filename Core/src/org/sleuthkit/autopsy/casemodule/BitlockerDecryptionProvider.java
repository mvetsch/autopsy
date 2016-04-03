/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sleuthkit.autopsy.casemodule;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorCallback;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorProgressMonitor;

@ServiceProvider(service = DecryptionProvider.class)
public class BitlockerDecryptionProvider implements DecryptionProvider {

    private static final byte bitlockerMagicString[] = "-FVE-FS-        ".getBytes();
    private ConfigureBitlockerPanel panel;

    private VolumeMetaData volumeMetaData;
    private String dislockedPath;
    private final String deviceId;
    private AddImageTask addImageTask;

    public BitlockerDecryptionProvider() {
        this.deviceId = UUID.randomUUID().toString();
        panel = new ConfigureBitlockerPanel();
    }

    @Override
    public void setVolumeMetaData(VolumeMetaData volumeMetaData) {
        this.volumeMetaData = volumeMetaData;
    }

    private KeyType getKeyType() {
        return panel.getKeyType();
    }

    private String getKeyValue() {
        return panel.getKeyValue();
    }

    @Override
    public String getName() {
        return "Bitlocker";
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
        start();
        addImageTask = new AddImageTask(deviceId, getRawDeviceLocation(), "aa", false, progressMonitor, callback);
        new Thread(addImageTask).start();
    }

    @Override
    public void cancel() {
        addImageTask.cancelTask();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public enum KeyType {
        USER_PASSWORD, RECOVERY_KEY, BEK_FILE
    }

    @Override
    public void start() {
        this.dislockedPath = createMountPoint();
        dislock(volumeMetaData);
    }

    @Override
    public void stop() {
        List<String> command = new LinkedList<>();
        command.add("umount");
        command.add(dislockedPath);
        try {
            Process a = new ProcessBuilder(command).start();
            a.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    @Override
    public String getRawDeviceLocation() {
        return dislockedPath + "/dislocker-file";
    }

    private String createMountPoint() {
        String folderName = "/media/" + this.deviceId;

        new File(folderName).mkdir();

        return folderName;
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
        command.add(this.dislockedPath);

        try {
            Process a = new ProcessBuilder(command).start();
            a.waitFor();
            return a.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private long getOffset(VolumeMetaData volumeMetaData) {
        return volumeMetaData.getOffSet() * 512;
    }

    private long getOffset() {
        List<String> command = new LinkedList<>();
        command.add("fdisk");
        command.add("-l");
        command.add(this.volumeMetaData.getPath());

        byte[] b = new byte[2048];
        try {
            Process p = new ProcessBuilder(command).start();
            p.getInputStream().read(b);

            String output = new String(b);
            String[] lines = output.split("\n");

            long startSector = Long.parseLong(lines[lines.length - 2]
                    .split("\\s+")[1]);
            long sectorSize = Long.parseLong(lines[1].split("=")[1].trim()
                    .split("\\s+")[0]);

            return startSector * sectorSize;
        } catch (IOException ex) {

        }
        return 0;
    }
}
