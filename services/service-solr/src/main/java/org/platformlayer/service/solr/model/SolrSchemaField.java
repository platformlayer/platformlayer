package org.platformlayer.service.solr.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.solr.ops.SolrSchemaFieldController;
import org.platformlayer.xaas.Controller;

/**
 * See http://wiki.apache.org/solr/SchemaXml
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(SolrSchemaFieldController.class)
public class SolrSchemaField extends ItemBase {
	/**
	 * Name of the field. If the name contains any wildcards, this will create a dynamic field instead.
	 */
	public String name;

	/**
	 * Type of the field. e.g. string, boolean, binary, int, float, long, double etc
	 */
	public String type;

	/**
	 * True if this field may contain multiple values per document, i.e. if it can appear multiple times in a document
	 */
	public boolean multiValued = false;

	/*
	 * True if this field should be "indexed". If (and only if) a field is indexed, then it is searchable, sortable, and
	 * facetable.
	 */
	public boolean indexed = true;

	/**
	 * True if the value of the field should be retrievable during a search
	 */
	public boolean stored = true;

	// Compression removed in 1.4.1
	// /**
	// * >=0 if this field should be stored using gzip compression. (This will only apply if the field type is
	// * compressable; among the standard field types, only TextField and StrField are.)
	// *
	// * If the size is less than the threshold, will not be compressed
	// */
	// public int compressThreshold = -1;
}
