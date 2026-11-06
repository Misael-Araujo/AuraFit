package com.aurafit.generation.domain;

public enum GenerationJobState {
  PENDING,
  PROCESSING,
  COMPLETED,
  FAILED,
  CANCELED,
  DEAD_LETTER
}
