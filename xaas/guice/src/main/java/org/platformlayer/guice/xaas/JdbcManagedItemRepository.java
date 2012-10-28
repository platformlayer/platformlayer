package org.platformlayer.guice.xaas;

import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;
import org.platformlayer.Filter;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.SecretInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ModelKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jdbc.DbHelperBase;
import org.platformlayer.jdbc.JdbcConnection;
import org.platformlayer.jdbc.JdbcTransaction;
import org.platformlayer.jdbc.JdbcUtils;
import org.platformlayer.jdbc.proxy.Query;
import org.platformlayer.jdbc.proxy.QueryFactory;
import org.platformlayer.jdbc.simplejpa.JoinedQueryResult;
import org.platformlayer.ops.crypto.SecretHelper;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xml.JaxbHelper;

import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class JdbcManagedItemRepository implements ManagedItemRepository {

	private static final Logger log = Logger.getLogger(JdbcManagedItemRepository.class);

	/**
	 * We originally weren't de-duplicating tags, but I think we want to
	 */
	static final boolean REMOVE_DUPLICATE_TAGS = true;

	@Inject
	ServiceProviderDictionary serviceProviderDirectory;

	@Inject
	Provider<JdbcConnection> connectionProvider;

	@Inject
	SecretHelper itemSecrets;

	@Override
	@JdbcTransaction
	public <T extends ItemBase> List<T> findAll(ModelClass<T> modelClass, ProjectId project, boolean fetchTags,
			SecretProvider secretProvider, Filter filter) throws RepositoryException {
		DbHelper db = new DbHelper(modelClass, project);
		JaxbHelper jaxbHelper = JaxbHelper.get(modelClass.getJavaClass());

		Map<Integer, T> items = Maps.newHashMap();

		try {
			// TODO: Our mapper may be able to do this join for us in one query...

			for (ItemEntity entity : db.listItems()) {
				T item = mapToModel(project, modelClass.getServiceType(), modelClass.getItemType(), entity, jaxbHelper,
						secretProvider);
				items.put(entity.id, item);
			}

			for (TagEntity tag : db.listTags()) {
				ItemBase managed = items.get(tag.item);
				if (managed == null) {
					// Looks like someone deleted an item without deleting a tag (this should be a foreign-key)
					continue;
				}
				Tags tags = managed.getTags();
				tags.add(Tag.build(tag.key, tag.data));
			}

			List<T> ret = Lists.newArrayList();
			for (T item : items.values()) {
				if (filter == null || filter.matches(item)) {
					ret.add(item);
				}
			}
			return ret;
		} catch (SQLException e) {
			throw new RepositoryException("Error fetching items", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<ItemBase> findRoots(ProjectId project, boolean fetchTags, SecretProvider secretProvider)
			throws RepositoryException {
		DbHelper db = new DbHelper(project);

		try {
			// TODO: We could maybe push the "root" filter into the DB

			// TODO: Use this logic for item selection as well

			JoinedQueryResult result = db.listAllItems();

			Multimap<Integer, Tag> itemTags = HashMultimap.create();
			for (TagEntity row : result.getAll(TagEntity.class)) {
				Tag tag = Tag.build(row.key, row.data);
				itemTags.put(row.item, tag);
			}

			List<Integer> rootIds = Lists.newArrayList();

			for (ItemEntity entity : result.getAll(ItemEntity.class)) {
				int itemId = entity.id;

				boolean isRoot = true;
				for (Tag tag : itemTags.get(itemId)) {
					boolean tagIsParent = Tag.PARENT.getKey().equals(tag.getKey());
					if (tagIsParent) {
						isRoot = false;
						break;
					}
				}

				if (isRoot) {
					rootIds.add(itemId);
				}
			}

			List<ItemBase> roots = Lists.newArrayList();

			for (Integer itemId : rootIds) {
				ItemEntity entity = result.get(ItemEntity.class, itemId);
				if (entity == null) {
					continue;
				}

				ServiceType serviceType = db.getServiceType(entity.service);
				ItemType itemType = db.getItemType(entity.model);

				JaxbHelper jaxbHelper = getJaxbHelper(db, serviceType, itemType);
				ItemBase item = mapToModel(project, serviceType, itemType, entity, jaxbHelper, secretProvider);

				Collection<Tag> tags = itemTags.get(itemId);
				item.getTags().addAll(tags);

				roots.add(item);
			}

			return roots;
		} catch (SQLException e) {
			throw new RepositoryException("Error fetching items", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public List<ItemBase> listAll(ProjectId project, Filter filter, SecretProvider secretProvider)
			throws RepositoryException {
		DbHelper db = new DbHelper(project);

		try {
			// TODO: We should maybe push the tag filter into the DB

			// TODO: Use this logic for item selection as well

			JoinedQueryResult result = db.listAllItems();

			Multimap<Integer, Tag> itemTags = HashMultimap.create();
			for (TagEntity row : result.getAll(TagEntity.class)) {
				Tag tag = Tag.build(row.key, row.data);
				itemTags.put(row.item, tag);
			}

			List<ItemBase> roots = Lists.newArrayList();

			for (ItemEntity entity : result.getAll(ItemEntity.class)) {
				if (entity == null) {
					throw new IllegalStateException();
				}

				ServiceType serviceType = db.getServiceType(entity.service);
				ItemType itemType = db.getItemType(entity.model);

				JaxbHelper jaxbHelper = getJaxbHelper(db, serviceType, itemType);
				ItemBase item = mapToModel(project, serviceType, itemType, entity, jaxbHelper, secretProvider);

				int itemId = entity.id;
				Collection<Tag> tags = itemTags.get(itemId);
				item.getTags().addAll(tags);

				if (filter.matchesItem(item)) {
					roots.add(item);
				}
			}

			return roots;
		} catch (SQLException e) {
			throw new RepositoryException("Error fetching items", e);
		} finally {
			db.close();
		}
	}

	private JaxbHelper getJaxbHelper(DbHelper db, ServiceType serviceType, ItemType itemType) throws SQLException {
		if (serviceType == null || itemType == null) {
			throw new IllegalStateException();
		}

		ServiceProvider serviceProvider = serviceProviderDirectory.getServiceProvider(serviceType);
		if (serviceProvider == null) {
			throw new IllegalStateException();
		}

		ModelClass<?> modelClass = serviceProvider.getModelClass(itemType);
		if (modelClass == null) {
			throw new IllegalStateException();
		}

		JaxbHelper jaxbHelper = JaxbHelper.get(modelClass.getJavaClass());
		return jaxbHelper;
	}

	static <T extends ItemBase> T mapToModel(ProjectId project, ServiceType serviceType, ItemType itemType,
			ItemEntity entity, JaxbHelper jaxb, SecretProvider secretProvider) throws RepositoryException {
		try {
			int id = entity.id;
			String key = entity.key;
			int stateCode = entity.state;
			byte[] data = entity.data;

			SecretInfo secret = new SecretInfo(entity.secret);
			CryptoKey itemSecret = secretProvider.getItemSecret(secret);

			if (itemSecret == null) {
				throw new RepositoryException("Could not get secret to decrypt item");
			}

			secret.unlock(itemSecret);

			byte[] plaintext = FathomdbCrypto.decrypt(itemSecret, data);
			String xml = new String(plaintext, Charsets.UTF_8);

			T model = (T) jaxb.unmarshal(xml);

			model.state = ManagedItemState.fromCode(stateCode);

			model.secret = secret;

			PlatformLayerKey plk = new PlatformLayerKey(null, project, serviceType, itemType, new ManagedItemId(key));
			model.setKey(plk);

			return model;
		} catch (JAXBException e) {
			throw new RepositoryException("Error deserializing data", e);
		}
	}

	static interface Queries {
		@Query("SELECT id, key, state, data, secret FROM items WHERE service=? and model=? and project=? and key=?")
		ItemEntity findByKey(int serviceId, int modelId, int projectId, String key) throws SQLException;

		@Query("SELECT id, key, state, data, secret FROM items WHERE service=? and model=? and project=?")
		List<ItemEntity> listItems(int serviceId, int modelId, int projectId) throws SQLException;

		@Query("SELECT i.*, t.* FROM items i LEFT JOIN item_tags t on t.item = i.id WHERE i.project=?")
		JoinedQueryResult listAllItems(int projectId) throws SQLException;

		@Query("UPDATE items set secret=? where service=? and model=? and project=? and key=?")
		int updateSecret(byte[] itemSecret, int serviceId, int itemId, int projectId, String key);

		@Query("UPDATE items SET data=?, state=? WHERE service=? and model=? and project=? and key=?")
		int updateItem(byte[] data, int newState, int serviceId, int itemTypeId, int projectId, String itemKey);

		@Query("SELECT item, key, data FROM item_tags WHERE service=? and model=? and project=?")
		List<TagEntity> listTags(int serviceId, int modelId, int projectId) throws SQLException;

		@Query("SELECT item, key, data FROM item_tags WHERE project=?")
		List<TagEntity> listAllProjectTags(int projectId) throws SQLException;

		@Query("SELECT key, data FROM item_tags where service=? and model=? and project=? and item=?")
		List<TagEntity> listTagsForItem(int serviceId, int modelId, int projectId, int itemId);
	}

	@Inject
	QueryFactory queryFactory;

	class DbHelper extends DbHelperBase {
		final Queries queries;

		public DbHelper(ModelKey key) {
			this(key.getServiceType(), key.getItemType(), key.getProject());
		}

		public ItemType getItemType(int code) throws SQLException {
			String v = mapCodeToKey(ItemType.class, code);
			if (v == null) {
				return null;
			}
			return new ItemType(v);
		}

		public ServiceType getServiceType(int code) throws SQLException {
			String v = mapCodeToKey(ServiceType.class, code);
			if (v == null) {
				return null;
			}
			return new ServiceType(v);
		}

		public DbHelper(PlatformLayerKey key) {
			this(key.getServiceType(), key.getItemType(), key.getProject());
		}

		public DbHelper(ServiceType serviceType, ItemType itemType, ProjectId project) {
			super(connectionProvider.get().getConnection());
			if (serviceType != null) {
				setAtom(serviceType);
			}
			if (itemType != null) {
				setAtom(itemType);
			}

			setAtom(project);

			this.queries = queryFactory.get(Queries.class);
		}

		public DbHelper(Class<? extends ItemBase> itemClass, ProjectId project) {
			this(serviceProviderDirectory.getModelClass(itemClass), project);
		}

		public DbHelper(ProjectId project) {
			this(null, null, project);
		}

		public DbHelper(ModelClass<?> modelClass, ProjectId project) {
			this(modelClass.getServiceType(), modelClass.getItemType(), project);
		}

		public ItemEntity findByKey(ManagedItemId managedItemId) throws SQLException {
			return queries.findByKey(getAtomValue(ServiceType.class), getAtomValue(ItemType.class),
					getAtomValue(ProjectId.class), managedItemId.getKey());
		}

		public List<ItemEntity> listItems() throws SQLException {
			return queries.listItems(getAtomValue(ServiceType.class), getAtomValue(ItemType.class),
					getAtomValue(ProjectId.class));
		}

		public JoinedQueryResult listAllItems() throws SQLException {
			return queries.listAllItems(getAtomValue(ProjectId.class));
		}

		public List<TagEntity> listTags() throws SQLException {
			return queries.listTags(getAtomValue(ServiceType.class), getAtomValue(ItemType.class),
					getAtomValue(ProjectId.class));
		}

		public List<TagEntity> listTagsForItem(int itemId) throws SQLException {
			// TODO: We could do this using a join, or two statements with
			// one round-trip

			return queries.listTagsForItem(getAtomValue(ServiceType.class), getAtomValue(ItemType.class),
					getAtomValue(ProjectId.class), itemId);
		}

		public void insertTags(int itemId, Tags tags) throws SQLException {
			for (Tag tag : tags) {
				insertTag(itemId, tag);
			}
		}

		public void insertTag(int itemId, Tag tag) throws SQLException {
			final String sql = "INSERT INTO item_tags (service, model, project, item, key, data) VALUES (?, ?, ?, ?, ?, ?)";

			PreparedStatement ps = prepareStatement(sql);
			setAtom(ps, 1, ServiceType.class);
			setAtom(ps, 2, ItemType.class);
			setAtom(ps, 3, ProjectId.class);
			ps.setInt(4, itemId);

			ps.setString(5, tag.key);
			ps.setString(6, tag.value);

			int updateCount = ps.executeUpdate();
			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows inserted");
			}
		}

		public void removeTags(int itemId, Tags tags) throws SQLException {
			for (Tag tag : tags) {
				removeTag(itemId, tag);
			}
		}

		public void removeTag(int itemId, Tag tag) throws SQLException {
			PreparedStatement ps;
			if (tag.getValue() != null) {
				final String sql = "DELETE FROM item_tags WHERE service = ? and model=? and project=? and item=? and key=? and data=?";

				ps = prepareStatement(sql);
				setAtom(ps, 1, ServiceType.class);
				setAtom(ps, 2, ItemType.class);
				setAtom(ps, 3, ProjectId.class);
				ps.setInt(4, itemId);

				ps.setString(5, tag.key);
				ps.setString(6, tag.value);
			} else {
				final String sql = "DELETE FROM item_tags WHERE service = ? and model=? and project=? and item=? and key=? and data is null";

				ps = prepareStatement(sql);
				setAtom(ps, 1, ServiceType.class);
				setAtom(ps, 2, ItemType.class);
				setAtom(ps, 3, ProjectId.class);
				ps.setInt(4, itemId);

				ps.setString(5, tag.key);
			}
			ps.executeUpdate();
		}

		public int insertItem(ItemBase item, byte[] data, byte[] secretData) throws SQLException {

			Integer itemId = null;
			final String sql = "INSERT INTO items (service, model, project, state, data, key, secret) VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
			ResultSet rs = null;
			try {
				ManagedItemState managedItemState = item.state;

				setAtom(ps, 1, ServiceType.class);
				setAtom(ps, 2, ItemType.class);
				setAtom(ps, 3, ProjectId.class);
				ps.setInt(4, managedItemState.getCode());
				ps.setBytes(5, data);
				ps.setString(6, item.getId());
				ps.setBytes(7, secretData);

				int updateCount = ps.executeUpdate();
				if (updateCount != 1) {
					throw new IllegalStateException("Unexpected number of rows inserted");
				}

				rs = ps.getGeneratedKeys();
				while (rs.next()) {
					if (itemId != null) {
						throw new IllegalStateException();
					}

					itemId = rs.getInt(1);
				}
			} finally {
				JdbcUtils.safeClose(rs);
				JdbcUtils.safeClose(ps);
			}

			if (itemId == null) {
				throw new IllegalStateException();
			}
			return itemId;
		}

		public void updateSecret(ManagedItemId itemKey, byte[] itemSecret) throws SQLException {
			int updateCount = queries.updateSecret(itemSecret, getAtomValue(ServiceType.class),
					getAtomValue(ItemType.class), getAtomValue(ProjectId.class), itemKey.getKey());
			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows inserted");
			}
		}

		public void updateItemState(ManagedItemState newState, ManagedItemId itemId) throws SQLException {
			final String sql = "UPDATE items set state=? where service=? and model=? and project=? and key=?";

			PreparedStatement ps = prepareStatement(sql);
			ps.setInt(1, newState.getCode());

			setAtom(ps, 2, ServiceType.class);
			setAtom(ps, 3, ItemType.class);
			setAtom(ps, 4, ProjectId.class);
			ps.setString(5, itemId.getKey());

			int updateCount = ps.executeUpdate();
			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows updated");
			}
		}

		public void updateItem(ManagedItemId itemKey, byte[] data, ManagedItemState newState) throws SQLException {
			int updateCount = queries.updateItem(data, newState.getCode(), getAtomValue(ServiceType.class),
					getAtomValue(ItemType.class), getAtomValue(ProjectId.class), itemKey.getKey());
			if (updateCount != 1) {
				throw new IllegalStateException("Unexpected number of rows inserted");
			}
		}
	}

	@Override
	@JdbcTransaction
	public ItemBase getManagedItem(PlatformLayerKey key, boolean fetchTags, SecretProvider secretProvider)
			throws RepositoryException {
		DbHelper db = new DbHelper(key);

		try {
			ServiceProvider serviceProvider = serviceProviderDirectory.getServiceProvider(key.getServiceType());
			if (serviceProvider == null) {
				throw new IllegalStateException();
			}

			ModelClass<?> modelClass = serviceProvider.getModelClass(key.getItemType());

			return fetchItem(db, key, modelClass.getJavaClass(), secretProvider, fetchTags);
		} catch (SQLException e) {
			throw new RepositoryException("Error running query", e);
		} finally {
			db.close();
		}
	}

	private <T extends ItemBase> T fetchItem(DbHelper db, PlatformLayerKey plk, Class<T> modelClass,
			SecretProvider secretProvider, boolean fetchTags) throws SQLException, RepositoryException {
		ItemEntity entity = db.findByKey(plk.getItemId());
		if (entity == null) {
			return null;
		}

		JaxbHelper jaxb = JaxbHelper.get(modelClass);

		if (plk.getProject() == null) {
			throw new IllegalStateException();
		}

		T item = mapToModel(plk.getProject(), plk.getServiceType(), plk.getItemType(), entity, jaxb, secretProvider);

		if (fetchTags) {
			// This is theoretically 1+N, but we only expect a single item to match
			mapToTags(db.listTagsForItem(entity.id), item.getTags());
		}

		item.setKey(plk);

		return item;
	}

	@Override
	@JdbcTransaction
	public <T extends ItemBase> T createManagedItem(ProjectId project, T item) throws RepositoryException {
		DbHelper db = new DbHelper(item.getClass(), project);
		try {
			CryptoKey itemSecret = FathomdbCrypto.generateKey();

			byte[] data = serialize(item, itemSecret);
			byte[] secretData = itemSecrets.encodeItemSecret(itemSecret);

			int itemId = db.insertItem(item, data, secretData);

			Tags tags = item.tags;
			if (tags != null && !tags.isEmpty()) {
				db.insertTags(itemId, tags);
			}

			return item;
		} catch (SQLException e) {
			throw new RepositoryException("Error running query", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public <T extends ItemBase> T updateManagedItem(ProjectId project, T item) throws RepositoryException {
		Class<T> itemClass = (Class<T>) item.getClass();

		DbHelper db = new DbHelper(itemClass, project);

		try {
			ManagedItemId itemId = new ManagedItemId(item.getId());

			ItemEntity rs = db.findByKey(itemId);
			if (rs == null) {
				throw new RepositoryException("Item not found");
			}

			byte[] secretData = rs.secret;

			CryptoKey itemSecret;

			if (secretData == null) {
				itemSecret = FathomdbCrypto.generateKey();
				secretData = itemSecrets.encodeItemSecret(itemSecret);

				db.updateSecret(itemId, secretData);
			} else {
				itemSecret = item.secret.getSecret();
			}

			byte[] data = serialize(item, itemSecret);

			db.updateItem(itemId, data, item.state);

			// Note: we can't change tags here (that needs a separate call to updateTags)

			SecretProvider secretProvider = SecretProvider.forKey(itemSecret);

			boolean fetchTags = true;
			return fetchItem(db, item.getKey(), itemClass, secretProvider, fetchTags);
		} catch (SQLException e) {
			throw new RepositoryException("Error running query", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public void changeState(PlatformLayerKey key, ManagedItemState newState) throws RepositoryException {
		DbHelper db = new DbHelper(key);

		try {
			db.updateItemState(newState, key.getItemId());
		} catch (SQLException e) {
			throw new RepositoryException("Error running query", e);
		} finally {
			db.close();
		}
	}

	@Override
	@JdbcTransaction
	public Tags changeTags(ModelClass<?> modelClass, ProjectId project, ManagedItemId itemKey, TagChanges changeTags)
			throws RepositoryException {
		DbHelper db = new DbHelper(modelClass, project);

		try {
			ItemEntity rs = db.findByKey(itemKey);
			if (rs == null) {
				// TODO: Better exception??
				throw new IllegalStateException("Not found");
			}

			int itemId = rs.id;

			Tags tags = new Tags();
			mapToTags(db.listTagsForItem(itemId), tags);

			if (changeTags.addTags != null) {
				for (Tag addTag : changeTags.addTags) {
					if (tags.hasTag(addTag)) {
						continue;
					}
					db.insertTag(itemId, addTag);
					tags.add(addTag);
				}
			}

			if (changeTags.removeTags != null) {
				for (Tag removeTag : changeTags.removeTags) {
					boolean removed = tags.remove(removeTag);
					if (!removed) {
						continue;
					}
					db.removeTag(itemId, removeTag);
				}
			}

			return tags;
		} catch (SQLException e) {
			throw new RepositoryException("Error running query", e);
		} finally {
			db.close();
		}
	}

	private void mapToTags(List<TagEntity> tagEntities, Tags tags) {
		// Once REMOVE_DUPLICATE_TAGS is false, we can add direct to tags
		List<Tag> addList = Lists.newArrayList();

		for (TagEntity tag : tagEntities) {
			addList.add(Tag.build(tag.key, tag.data));
		}

		if (REMOVE_DUPLICATE_TAGS) {
			List<Tag> deduplicated = Lists.newArrayList();
			HashMultimap<String, String> valueMap = HashMultimap.create();
			for (Tag tag : addList) {
				if (valueMap.put(tag.getKey(), tag.getValue())) {
					deduplicated.add(tag);
				}
			}

			addList = deduplicated;
		}

		tags.addAll(addList);
	}

	byte[] serialize(ItemBase item, CryptoKey itemSecret) {

		// Remove fields that are stored in other columns

		// TODO: Is this the best way to do this?

		// We use JAXB to avoid requiring everything to implement Serializable
		ItemBase mutableItem = CloneHelpers.cloneViaJaxb(item);

		mutableItem.tags = null;
		mutableItem.key = null;
		mutableItem.version = 0;
		mutableItem.state = null;

		JaxbHelper jaxbHelper = JaxbHelper.get(item.getClass());

		StringWriter writer = new StringWriter();
		try {
			Marshaller marshaller = jaxbHelper.createMarshaller();

			// OpsSecretEncryptionStrategy strategy = new OpsSecretEncryptionStrategy(itemSecret);
			// strategy.setAdapter(marshaller);

			marshaller.marshal(mutableItem, writer);
		} catch (JAXBException e) {
			throw new IllegalArgumentException("Could not serialize data", e);
		}
		String xml = writer.toString();

		byte[] ciphertext = FathomdbCrypto.encrypt(itemSecret, Utf8.getBytes(xml));
		return ciphertext;
	}
}
