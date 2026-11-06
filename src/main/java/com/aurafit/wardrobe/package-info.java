@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {
      "shared::config",
      "shared::error",
      "media::application",
      "media::domain",
      "generation::application",
      "generation::domain",
      "trustsafety::application"
    })
package com.aurafit.wardrobe;
