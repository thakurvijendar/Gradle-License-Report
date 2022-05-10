import io.github.thakurvijendar.license.render.ReportRenderer
import io.github.thakurvijendar.license.render.InventoryHtmlReportRenderer
import io.github.thakurvijendar.license.filter.DependencyFilter
import io.github.thakurvijendar.license.filter.LicenseBundleNormalizer

plugins {
    id("io.github.thakurvijendar.dependency-license-report") version "1.13"
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-tx:3.2.3.RELEASE")
    implementation("com.sun.mail:javax.mail:1.5.4")
    implementation("org.ehcache:ehcache:3.3.1")
    implementation("org.apache.geronimo.specs:geronimo-jta_1.0.1B_spec:1.0.1")
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html","Backend"))
    filters = arrayOf<DependencyFilter>(LicenseBundleNormalizer())
}
