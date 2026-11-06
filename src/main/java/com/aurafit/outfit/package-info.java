@org.springframework.modulith.ApplicationModule(
    allowedDependencies = {
      "shared::config",
      "shared::error",
      "wardrobe::application",
      "wardrobe::domain",
      "avatar::application",
      "generation::application",
      "media::application"
    })
package com.aurafit.outfit;
