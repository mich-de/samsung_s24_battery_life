plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Forza l'uso delle ultime versioni (July 2026 Rules) per tutte le dipendenze transitive
subprojects {
    configurations.all {
        resolutionStrategy {
            force(libs.androidx.core.ktx)
            force(libs.androidx.activity.compose)
            force(libs.androidx.lifecycle.viewmodel.compose)
            force(libs.androidx.compose.ui)
            force(libs.androidx.compose.material3)
            force(libs.google.code.gson)
        }
    }
}
