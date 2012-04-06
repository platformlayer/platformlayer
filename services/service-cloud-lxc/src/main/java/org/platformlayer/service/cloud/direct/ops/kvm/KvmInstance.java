package org.platformlayer.service.cloud.direct.ops.kvm;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.CloudInstanceMapper;
import org.platformlayer.service.cloud.direct.ops.DirectCloudUtils;
import org.platformlayer.service.cloud.direct.ops.DownloadImage;
import org.platformlayer.service.cloud.direct.ops.kvm.monitor.KvmConfig.KvmDrive;
import org.platformlayer.service.cloud.direct.ops.kvm.monitor.KvmConfig.KvmNic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class KvmInstance extends OpsTreeBase {
    public File instanceDir;
    public String id;
    public int minimumMemoryMB;
    public PlatformLayerKey recipeId;
    public PublicKey sshPublicKey;

    // public MachineCreationRequest request;

    private File getInstanceDir() {
        return instanceDir;
    }

    String getEthernetDeviceName() {
        return "tun_" + id + "_0";
    }

    File getImagePath() {
        return new File(getInstanceDir(), "drive0");
    }

    File getConfigIsoPath() {
        return new File(getInstanceDir(), "config.iso");
    }

    @Handler
    public void handler() {
    }

    @Override
    protected void addChildren() throws OpsException {
        CloudInstanceMapper instance;
        {
            instance = injected(CloudInstanceMapper.class);
            instance.instance = OpsContext.get().getInstance(DirectInstance.class);
            addChild(instance);
        }

        instance.addChild(ManagedDirectory.build(getInstanceDir(), "700"));

        final PoolAssignment assignNetworkAddress;
        {
            assignNetworkAddress = injected(PoolAssignment.class);
            assignNetworkAddress.holder = getInstanceDir();
            assignNetworkAddress.poolProvider = DirectCloudUtils.getPoolProvider("network");
            instance.addChild(assignNetworkAddress);
        }

        {
            NetworkTunDevice tun = injected(NetworkTunDevice.class);
            tun.interfaceName = getEthernetDeviceName();
            tun.bridgeName = OpsProvider.getProperty(assignNetworkAddress, "bridge");
            instance.addChild(tun);
        }

        final PoolAssignment assignMonitorPort;
        {
            assignMonitorPort = injected(PoolAssignment.class);
            assignMonitorPort.holder = getInstanceDir();
            assignMonitorPort.poolProvider = DirectCloudUtils.getPoolProvider("monitor");
            instance.addChild(assignMonitorPort);
        }

        final PoolAssignment assignVncPort;
        {
            assignVncPort = injected(PoolAssignment.class);
            assignVncPort.holder = getInstanceDir();
            assignVncPort.poolProvider = DirectCloudUtils.getPoolProvider("vnc");
            instance.addChild(assignVncPort);
        }

        {
            ConfigIso iso = injected(ConfigIso.class);
            iso.isoFile = getConfigIsoPath();
            iso.buildDir = new File(getInstanceDir(), "config_iso_src");
            iso.model = new TemplateDataSource() {
                @Override
                public void buildTemplateModel(Map<String, Object> model) throws OpsException {
                    List<Map<String, String>> interfaces = Lists.newArrayList();
                    {
                        Map<String, String> conf = asMap(assignNetworkAddress.getAssigned());
                        conf.put("name", "eth0");
                        interfaces.add(conf);
                    }
                    model.put("interfaces", interfaces);

                    List<String> authorizedKeys = Lists.newArrayList();
                    try {
                        authorizedKeys.add(OpenSshUtils.serialize(sshPublicKey));
                    } catch (IOException e) {
                        throw new OpsException("Error serializing ssh key", e);
                    }
                    model.put("authorizedKeys", authorizedKeys);
                }
            };

            instance.addChild(iso);
        }

        {
            DownloadImage download = injected(DownloadImage.class);
            download.imageFile = getImagePath();
            download.recipeKey = recipeId;
            download.imageFormat = ImageFactory.ImageFormat.DiskRaw;
            instance.addChild(download);
        }

        {
            ManagedKvmInstance kvmInstance = injected(ManagedKvmInstance.class);

            kvmInstance.id = id;
            kvmInstance.memoryMb = Math.max(256, minimumMemoryMB);
            kvmInstance.vcpus = 1;
            kvmInstance.base = getInstanceDir();
            kvmInstance.monitor = assignMonitorPort;
            kvmInstance.vnc = assignVncPort;
            kvmInstance.nics = buildVnics();
            kvmInstance.drives = buildDrives();

            instance.addChild(kvmInstance);
        }

        {
            final DirectInstance model = OpsContext.get().getInstance(DirectInstance.class);

            OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
                @Override
                public TagChanges get() {
                    TagChanges tagChanges = new TagChanges();

                    tagChanges.addTags.add(new Tag(Tag.INSTANCE_KEY, OpsSystem.toKey(model).getUrl()));

                    String address = assignNetworkAddress.getAssigned().getProperty("address");
                    tagChanges.addTags.add(new Tag(Tag.NETWORK_ADDRESS, address));
                    return tagChanges;
                }
            };

            addChild(Tagger.build(model, tagChanges));
        }
    }

    protected Map<String, String> asMap(Properties properties) {
        Map<String, String> map = Maps.newHashMap();
        for (Object key : properties.keySet()) {
            map.put((String) key, (String) properties.get(key));
        }
        return map;
    }

    private List<KvmNic> buildVnics() {
        List<KvmNic> nics = Lists.newArrayList();

        {
            KvmNic nic = new KvmNic();
            nic.device = getEthernetDeviceName();
            // nic.mac ;
            // Also verify that the guest's running Kernel has CONFIG_PCI_MSI enabled:
            //
            // grep CONFIG_PCI_MSI /boot/config-`uname -r`
            // If both conditions are met, use the vhost-net driver by starting the guest with the following example command line:
            //
            // qemu-kvm [...] -netdev tap,id=guest0,vhost=on,script=no
            // -net nic,model=virtio,netdev=guest0,macaddr=00:16:35:AF:94:4B
            //
            // qemu: Supported NIC models: ne2k_pci,i82551,i82557b,i82559er,rtl8139,e1000,pcnet,virtio
            nic.model = "virtio"; // "e1000";
            nic.name = "nic0";
            nics.add(nic);
        }

        return nics;
    }

    private List<KvmDrive> buildDrives() {
        List<KvmDrive> drives = Lists.newArrayList();

        {
            KvmDrive drive = new KvmDrive();

            drive.path = getImagePath().getAbsolutePath();
            drive.id = "0";
            drive.boot = true;
            drive.format = "raw";
            drive.media = "disk";

            drives.add(drive);
        }
        {
            KvmDrive drive = new KvmDrive();

            drive.path = getConfigIsoPath().getAbsolutePath();
            drive.id = "config_cd";
            drive.boot = false;
            drive.format = "raw";
            drive.media = "cdrom";

            drives.add(drive);
        }

        return drives;
    }
}
