package org.platformlayer.service.cloud.direct.ops.kvm.monitor;

import java.util.List;

import com.google.common.collect.Lists;

public class KvmConfig {
	final String id;
	public final List<KvmNic> nics = Lists.newArrayList();

	public static class KvmNic {
		public String name;
		public String device;
		public String mac;
		public String model;
	}

	public static class KvmDrive {
		public String id;
		public String path;
		public boolean boot;
		public String format;
		public String media;
	}

	public KvmConfig(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	// + def path_base(self):
	// 888
	// + return KvmInstance.build_basepath(self.instance_name())
	// 889
	// +
	// 890
	// + def path_monitor(self):
	// 891
	// + return os.path.join(self.path_base(), 'instance.monitor')
	// 892
	// +
	// 893
	// + def path_device_conf(self):
	// 894
	// + return os.path.join(self.path_base(), 'device.conf')
	// 895
	// +
	// 896
	// + def zvol_base(self):
	// 897
	// + return os.path.join(FLAGS.illumos_zvol_base, self.instance_name())
	// 898
	// +
	// 899
	// + @staticmethod
	// 900
	// + def build_basepath(instance_name):
	// 901
	// + return os.path.join(FLAGS.instances_path, instance_name)
	// 902
	// +
	// 903

	// + def smf_name(self):
	// 904
	// + return 'svc:/site/kvm:' + self.instance_name()
	// 905
	// +
	// 906
	// + def disable_smf(self):
	// 907
	// + smf = self.find_smf()
	// 908
	// + if not smf:
	// 909
	// + # SMF is already dead
	// 910
	// + return
	// 911
	// + Smf.disable_smf(smf.fmri())
	// 912
	// +
	// 913
	// + def delete_smf(self):
	// 914
	// + smf = self.find_smf()
	// 915
	// + if not smf:
	// 916
	// + # SMF is already dead
	// 917
	// + return
	// 918
	// + Smf.delete_smf(smf.fmri())
	// 919
	// +
	// 920
	// + def find_smf(self):
	// 921
	// + fmri = self.smf_name()
	// 922
	// + smfs = Smf.list_services(fmri)
	// 923
	// + if len(smfs) == 0:
	// 924
	// + return None
	// 925
	// + if len(smfs) == 1:
	// 926
	// + return smfs[0]
	// 927
	// + raise Exception(_("Multiple SMFs found with same fmri"))
	// 928
	// +
	// 929

	// QemuMonitor monitor = null;
	//
	// public QemuMonitor getMonitor() {
	// if (monitor == null) {
	// monitor = new QemuMonitor(monitorAddress, monitorPort);
	// }
	// return monitor;
	// }

	// 942
	// + def attach_drive(self, drive):
	// 943
	// + # TODO(justinsb): We need to persist this to survive host reboots
	// 944
	// +
	// 945
	// + path = drive['path']
	// 946
	// + interface = 'none'
	// 947
	// + drive_format = drive['format']
	// 948
	// + boot = drive['boot']
	// 949
	// + key = drive['id']
	// 950
	// + media = drive['media']
	// 951
	// +
	// 952
	// + bus = 'pci.0'
	// 953
	// + device_driver = 'virtio-blk-pci'
	// 954
	// +
	// 955
	// + drive_id = 'drive_%s' % (key)
	// 956
	// + device_id = 'device_%s' % (key)
	// 957
	// +
	// 958
	// + conn = self.get_monitor()
	// 959
	// + conn.drive_add(path, interface, drive_id, boot, drive_format, media)
	// 960
	// + conn.device_add(device_driver, bus, drive_id, device_id)
	// 961
	// +
	// 962
	// + def pause(self):
	// 963
	// + self.get_monitor().pause()
	// 964
	// +
	// 965
	// + def unpause(self):
	// 966
	// + self.get_monitor().unpause()
	// 967
	// +
	// 968
	// public void stop() {
	// getMonitor().stop();
	// }
}