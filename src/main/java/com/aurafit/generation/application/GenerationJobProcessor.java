package com.aurafit.generation.application;

import com.aurafit.generation.domain.GenerationJob;

public interface GenerationJobProcessor {
  boolean supports(String jobType);

  void process(GenerationJob job) throws Exception;
}
