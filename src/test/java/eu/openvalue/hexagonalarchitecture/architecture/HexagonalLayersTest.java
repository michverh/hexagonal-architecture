package eu.openvalue.hexagonalarchitecture.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(
    packages = "eu.openvalue.hexagonalarchitecture",
    importOptions = {
        ImportOption.DoNotIncludeTests.class,
        HexagonalLayersTest.ExcludeGenerated.class
    }
)
class HexagonalLayersTest {

  @ArchTest
  static final ArchRule respect_hexagonal_layers = layeredArchitecture()
      .consideringOnlyDependenciesInLayers()
      .layer("Domain").definedBy("eu.openvalue.hexagonalarchitecture.domain..")
      .layer("Application").definedBy("eu.openvalue.hexagonalarchitecture.application..")
      .layer("Adapters").definedBy("eu.openvalue.hexagonalarchitecture.adapter..")
      .whereLayer("Domain").mayOnlyBeAccessedByLayers("Domain", "Application", "Adapters")
      .whereLayer("Domain").mayOnlyAccessLayers("Domain")
      .whereLayer("Application").mayOnlyBeAccessedByLayers("Application", "Adapters")
      .whereLayer("Application").mayOnlyAccessLayers("Domain", "Application")
      .whereLayer("Adapters").mayOnlyAccessLayers("Application", "Domain", "Adapters");

  @ArchTest
  static final ArchRule checkDomain = classes()
      .that()
      .resideInAPackage("eu.openvalue.hexagonalarchitecture.domain..")
      .should()
      .onlyDependOnClassesThat()
      .resideInAnyPackage(
          "eu.openvalue.hexagonalarchitecture.domain..",
          "java.."
      );

  @ArchTest
  static final ArchRule checkApplication = classes()
      .that()
      .resideInAPackage("eu.openvalue.hexagonalarchitecture.application..")
      .should()
      .onlyDependOnClassesThat()
      .resideInAnyPackage(
          "eu.openvalue.hexagonalarchitecture.application..",
          "eu.openvalue.hexagonalarchitecture.domain..",
          "java..",
          "org.springframework.."
      );

  @ArchTest
  static final ArchRule checkAdapter = classes()
      .that()
      .resideInAPackage("eu.openvalue.hexagonalarchitecture.adapter..")
      .should()
      .onlyDependOnClassesThat()
      .resideInAnyPackage(
          "eu.openvalue.hexagonalarchitecture.adapter..",
          "eu.openvalue.hexagonalarchitecture.application..",
          "eu.openvalue.hexagonalarchitecture.domain..",
          "java..",
          "jakarta..",
          "org.hibernate..",
          "org.springframework..",
          "org.mapstruct..",
          "lombok.."
      );

  @ArchTest
  static final ArchRule checkNoShortCuts = classes()
      .that()
      .areNotAnnotatedWith(RestControllerAdvice.class)
      .and()
      .haveSimpleNameNotEndingWith("MapperImpl")
      .and()
      .resideInAPackage("eu.openvalue.hexagonalarchitecture.adapter.in..")
      .should()
      .onlyAccessClassesThat()
      .resideInAnyPackage(
          "eu.openvalue.hexagonalarchitecture.adapter.in..",
          "eu.openvalue.hexagonalarchitecture.application.port.in..",
          "org.springframework..",
          "java.."
      );

  static class ExcludeGenerated implements ImportOption {

    @Override
    public boolean includes(Location location) {
      return !location.contains("generated-sources");
    }
  }
}
