package com.jdroid.sample.android.experiment;

import com.jdroid.android.analytics.ExperimentHelper;
import com.jdroid.android.analytics.SimpleExperimentVariant;
import com.jdroid.java.collections.Lists;

import java.util.List;

public enum AndroidExperiment implements ExperimentHelper.Experiment {

	SAMPLE_EXPERIMENT("sampleExperiment", Lists.<ExperimentHelper.ExperimentVariant>newArrayList(SimpleExperimentVariant.A, SimpleExperimentVariant.B));

	private String id;
	private List<ExperimentHelper.ExperimentVariant> variants;

	private AndroidExperiment(String id, List<ExperimentHelper.ExperimentVariant> variants) {
		this.id = id;
		this.variants = variants;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<ExperimentHelper.ExperimentVariant> getVariants() {
		return variants;
	}
}
