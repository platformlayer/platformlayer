package org.platformlayer.service.cloud.direct.ops.cloud;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.choice.Chooser;
import org.platformlayer.choice.ScoreChooser;
import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.service.cloud.direct.model.DirectInstance;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScoreHostPolicy implements Chooser<DirectCloudHost> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ScoreHostPolicy.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	HostPolicy hostPolicy;
	DirectInstance newInstance;

	public static final String DEFAULT_GROUP = "default";

	class HostRecord {
		List<DirectInstance> all = Lists.newArrayList();
		Map<DirectInstance, PlatformLayerKey> owners;
		Map<DirectInstance, String> groups;

		DirectCloudHost candidate;
	}

	PlatformLayerKey findOwner(DirectInstance instance) throws OpsException {
		PlatformLayerKey parentKey = Tag.PARENT.findUnique(instance);
		if (parentKey != null) {
			ItemBase persistentInstance = platformLayer.getItem(parentKey);

			if (persistentInstance != null) {
				PlatformLayerKey grandparentKey = Tag.PARENT.findUnique(instance);
				return grandparentKey;
			}
		}
		return null;
	}

	@Override
	public DirectCloudHost choose(List<DirectCloudHost> candidates) throws OpsException {
		final String sameGroupId;
		if (Strings.isNullOrEmpty(hostPolicy.groupId)) {
			sameGroupId = DEFAULT_GROUP;
		} else {
			sameGroupId = hostPolicy.groupId;
		}
		final ItemType sameItemType;
		if (hostPolicy.scoreSameItemType != 0) {
			PlatformLayerKey owner = findOwner(newInstance);
			if (owner == null) {
				throw new OpsException();
			}

			sameItemType = owner.getItemType();
		} else {
			sameItemType = null;
		}

		List<HostRecord> records = Lists.newArrayList();
		for (DirectCloudHost candidate : candidates) {
			HostRecord record = new HostRecord();
			record.candidate = candidate;
			record.groups = Maps.newHashMap();
			if (hostPolicy.scoreSameItemType != 0) {
				record.owners = Maps.newHashMap();
			}
			records.add(record);

			for (Tag tag : candidate.getModel().getTags().findTags(Tag.ASSIGNED)) {
				PlatformLayerKey instanceKey = PlatformLayerKey.parse(tag.getValue());

				// TODO: Avoid 1+N
				DirectInstance instance = platformLayer.getItem(instanceKey);
				if (instance == null) {
					// TODO: Warn?
					throw new IllegalStateException();
				}

				switch (instance.getState()) {
				case DELETE_REQUESTED:
				case DELETED:
					continue;
				}

				HostPolicy instanceHostPolicy = instance.hostPolicy;
				String instanceGroupId = instanceHostPolicy.groupId;
				if (Strings.isNullOrEmpty(instanceGroupId)) {
					instanceGroupId = DEFAULT_GROUP;
				}

				record.groups.put(instance, instanceGroupId);

				if (sameItemType != null) {
					PlatformLayerKey owner = findOwner(instance);
					if (owner != null) {
						record.owners.put(instance, owner);
					}
				}

				record.all.add(instance);
			}
		}

		Function<HostRecord, Float> score = new Function<HostRecord, Float>() {
			@Override
			public Float apply(HostRecord record) {
				float score = 0;

				for (DirectInstance instance : record.all) {
					if (sameGroupId != null) {
						String instanceGroupId = record.groups.get(instance);
						if (Objects.equal(instanceGroupId, sameGroupId)) {
							score += hostPolicy.scoreSameGroup;
						}
					}

					if (sameItemType != null) {
						PlatformLayerKey owner = record.owners.get(instance);

						if (owner != null && owner.getItemType().equals(sameItemType)) {
							score += hostPolicy.scoreSameItemType;
						}
					}
				}

				// Break ties using least-loaded
				score -= record.all.size() / 1000.0f;

				return score;
			}
		};

		HostRecord bestRecord = ScoreChooser.chooseMax(score).choose(records);

		return bestRecord.candidate;
	}

	public static ScoreHostPolicy build(HostPolicy hostPolicy, DirectInstance newInstance) {
		if (hostPolicy == null) {
			hostPolicy = new HostPolicy();
		}

		ScoreHostPolicy chooser = Injection.getInstance(ScoreHostPolicy.class);
		chooser.hostPolicy = hostPolicy;
		chooser.newInstance = newInstance;

		return chooser;
	}
}
