@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {
      "shared::config",
      "shared::error",
      "consent::application",
      "consent::domain",
      "media::application",
      "media::domain",
      "generation::application",
      "trustsafety::application"
    })
package com.aurafit.avatar;
