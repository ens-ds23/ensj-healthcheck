/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * File: EgMeta.java
 * Created by: dstaines
 * Created on: Mar 2, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * @author dstaines
 * 
 */
public class PermittedEgMeta extends AbstractEgMeta {

	/**
	 * @param metaKeys
	 */
	public PermittedEgMeta() {
		super(
				TestCaseUtils.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/meta_keys.txt"));
	}
	protected Map<String, Boolean> getKeys(SqlTemplate template, int speciesId) {
		Map<String, Boolean> metaKeyOut = CollectionUtils.createHashMap();
		for (Entry<String, List<String>> meta : template.queryForMap(
				META_QUERY, mapper, speciesId).entrySet()) {
			if (!metaKeys.contains(meta.getKey())) {
				metaKeyOut.put(meta.getKey(), true);
			}
		}
		return metaKeyOut;
	}

	protected boolean testKeys(DatabaseRegistryEntry dbre, int speciesId,
			Map<String, Boolean> metaKeyOut) {
		boolean passes = true;
		for (Entry<String, Boolean> e : metaKeyOut.entrySet()) {
				passes = false;
				ReportManager
						.problem(this, dbre.getConnection(), "Meta table for "
								+ speciesId + " contains the unrecognised key "
								+ e.getKey());
		}
		return passes;
	}
	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Checks if meta keys used are in the permitted list";
	}
}
