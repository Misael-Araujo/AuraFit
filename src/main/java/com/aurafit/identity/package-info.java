@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {
      "shared::config",
      "shared::error",
      "consent::application",
      "consent::domain",
      "media::application"
    })
package com.aurafit.identity;
