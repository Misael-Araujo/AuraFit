@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {
      "shared::config",
      "shared::error",
      "media::application",
      "media::domain",
      "consent::domain",
      "trustsafety::application"
    })
package com.aurafit.generation;
