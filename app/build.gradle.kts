plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
	id("com.google.devtools.ksp")
}

android {
	namespace = "com.nolly.surecharge"
	compileSdk {
		version = release(36)
	}

	defaultConfig {
		applicationId = "com.nolly.surecharge"
		description = "Battery Alerts You Can Trust"
		minSdk = 26
		targetSdk = 36
		versionCode = 1
		versionName = "0.1.0"
		vectorDrawables.useSupportLibrary = true
	}

	buildTypes {
		debug {
			isMinifyEnabled = false
		}
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.9.4"
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	// Core
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.runtime.compose)

	// Compose UI
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.tooling.preview)
	debugImplementation(libs.androidx.ui.tooling)

	// Material Design
	implementation(libs.google.material)
	implementation(libs.material3)
	implementation(libs.androidx.compose.material.icons.extended)

	// DataStore
	implementation(libs.androidx.datastore.preferences)

	// WorkManager
	implementation(libs.androidx.work.runtime.ktx)

	// Room
	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)
}
