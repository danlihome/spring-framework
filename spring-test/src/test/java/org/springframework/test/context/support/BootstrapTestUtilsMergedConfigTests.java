/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.AssertionsForClassTypes;

import org.springframework.test.context.BootstrapTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.WebDelegatingSmartContextLoader;
import org.springframework.test.context.web.WebMergedContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Unit tests for {@link BootstrapTestUtils} involving {@link MergedContextConfiguration}.
 *
 * @author Sam Brannen
 * @since 3.1
 */
class BootstrapTestUtilsMergedConfigTests extends AbstractContextConfigurationUtilsTests {

	@Test
	void buildImplicitMergedConfigWithoutAnnotation() {
		Class<?> testClass = Enigma.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, EMPTY_CLASS_ARRAY, DelegatingSmartContextLoader.class);
	}

	/**
	 * @since 4.3
	 */
	@Test
	void buildMergedConfigWithContextConfigurationWithoutLocationsClassesOrInitializers() {
		assertThatIllegalStateException().isThrownBy(() ->
				buildMergedContextConfiguration(MissingContextAttributesTestCase.class))
			.withMessageStartingWith("DelegatingSmartContextLoader was unable to detect defaults, "
					+ "and no ApplicationContextInitializers or ContextCustomizers were declared for context configuration attributes");
	}

	@Test
	void buildMergedConfigWithBareAnnotations() {
		Class<?> testClass = BareAnnotations.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(
			mergedConfig,
			testClass,
			array("classpath:org/springframework/test/context/support/AbstractContextConfigurationUtilsTests$BareAnnotations-context.xml"),
			EMPTY_CLASS_ARRAY, DelegatingSmartContextLoader.class);
	}

	@Test
	void buildMergedConfigWithLocalAnnotationAndLocations() {
		Class<?> testClass = LocationsFoo.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, array("classpath:/foo.xml"), EMPTY_CLASS_ARRAY,
			DelegatingSmartContextLoader.class);
	}

	@Test
	void buildMergedConfigWithMetaAnnotationAndLocations() {
		Class<?> testClass = MetaLocationsFoo.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, array("classpath:/foo.xml"), EMPTY_CLASS_ARRAY,
			DelegatingSmartContextLoader.class);
	}

	@Test
	void buildMergedConfigWithMetaAnnotationAndClasses() {
		buildMergedConfigWithMetaAnnotationAndClasses(Dog.class);
		buildMergedConfigWithMetaAnnotationAndClasses(WorkingDog.class);
		buildMergedConfigWithMetaAnnotationAndClasses(GermanShepherd.class);
	}

	private void buildMergedConfigWithMetaAnnotationAndClasses(Class<?> testClass) {
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);
		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, array(FooConfig.class,
			BarConfig.class), DelegatingSmartContextLoader.class);
	}

	@Test
	void buildMergedConfigWithLocalAnnotationAndClasses() {
		Class<?> testClass = ClassesFoo.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, array(FooConfig.class),
			DelegatingSmartContextLoader.class);
	}

	/**
	 * Introduced to investigate claims made in a discussion on
	 * <a href="https://stackoverflow.com/questions/24725438/what-could-cause-a-class-implementing-applicationlistenercontextrefreshedevent">Stack Overflow</a>.
	 */
	@Test
	void buildMergedConfigWithAtWebAppConfigurationWithAnnotationAndClassesOnSuperclass() {
		Class<?> webTestClass = WebClassesFoo.class;
		Class<?> standardTestClass = ClassesFoo.class;
		WebMergedContextConfiguration webMergedConfig = (WebMergedContextConfiguration) buildMergedContextConfiguration(webTestClass);
		MergedContextConfiguration standardMergedConfig = buildMergedContextConfiguration(standardTestClass);

		assertThat(webMergedConfig).isEqualTo(webMergedConfig);
		assertThat(standardMergedConfig).isEqualTo(standardMergedConfig);
		assertThat(webMergedConfig).isNotEqualTo(standardMergedConfig);
		assertThat(standardMergedConfig).isNotEqualTo(webMergedConfig);

		assertMergedConfig(webMergedConfig, webTestClass, EMPTY_STRING_ARRAY, array(FooConfig.class),
			WebDelegatingSmartContextLoader.class);
		assertMergedConfig(standardMergedConfig, standardTestClass, EMPTY_STRING_ARRAY,
			array(FooConfig.class), DelegatingSmartContextLoader.class);
	}

	@Test
	void buildMergedConfigWithLocalAnnotationAndOverriddenContextLoaderAndLocations() {
		Class<?> testClass = PropertiesLocationsFoo.class;
		Class<? extends ContextLoader> expectedContextLoaderClass = GenericPropertiesContextLoader.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, array("classpath:/foo.properties"), EMPTY_CLASS_ARRAY,
			expectedContextLoaderClass);
	}

	@Test
	void buildMergedConfigWithLocalAnnotationAndOverriddenContextLoaderAndClasses() {
		Class<?> testClass = PropertiesClassesFoo.class;
		Class<? extends ContextLoader> expectedContextLoaderClass = GenericPropertiesContextLoader.class;
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, array(FooConfig.class),
			expectedContextLoaderClass);
	}

	@Test
	void buildMergedConfigWithLocalAndInheritedAnnotationsAndLocations() {
		Class<?> testClass = LocationsBar.class;
		String[] expectedLocations = array("/foo.xml", "/bar.xml");
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, expectedLocations, EMPTY_CLASS_ARRAY,
			AnnotationConfigContextLoader.class);
	}

	@Test
	void buildMergedConfigWithLocalAndInheritedAnnotationsAndClasses() {
		Class<?> testClass = ClassesBar.class;
		Class<?>[] expectedClasses = array(FooConfig.class, BarConfig.class);
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	@Test
	void buildMergedConfigWithAnnotationsAndOverriddenLocations() {
		Class<?> testClass = OverriddenLocationsBar.class;
		String[] expectedLocations = array("/bar.xml");
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, expectedLocations, EMPTY_CLASS_ARRAY,
			AnnotationConfigContextLoader.class);
	}

	@Test
	void buildMergedConfigWithAnnotationsAndOverriddenClasses() {
		Class<?> testClass = OverriddenClassesBar.class;
		Class<?>[] expectedClasses = array(BarConfig.class);
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	/**
	 * @since 5.2
	 */
	@Test
	public void buildMergedConfigForNestedTestClassWithInheritedConfig() {
		Class<?> testClass = OuterTestCase.NestedTestCaseWithInheritedConfig.class;
		Class<?>[] expectedClasses = array(FooConfig.class);
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	/**
	 * @since 5.2
	 */
	@Test
	public void buildMergedConfigForNestedTestClassWithMergedInheritedConfig() {
		Class<?> testClass = OuterTestCase.NestedTestCaseWithMergedInheritedConfig.class;
		Class<?>[] expectedClasses = array(FooConfig.class, BarConfig.class);
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	/**
	 * @since 5.2
	 */
	@Test
	public void buildMergedConfigForNestedTestClassWithOverriddenConfig() {
		Class<?> testClass = OuterTestCase.NestedTestCaseWithOverriddenConfig.class;
		Class<?>[] expectedClasses = array(BarConfig.class);
		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	/**
	 * @since 5.2
	 */
	@Test
	public void buildMergedConfigForContextHierarchy() {
		Class<?> testClass = ContextHierarchyOuterTestCase.class;
		Class<?>[] expectedClasses = array(BarConfig.class);

		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);
		assertThat(mergedConfig).as("merged config").isNotNull();

		MergedContextConfiguration parent = mergedConfig.getParent();
		assertThat(parent).as("parent config").isNotNull();
		// The following does not work -- at least not in Eclipse.
		// asssertThat(parent.getClasses())...
		// So we use AssertionsForClassTypes directly.
		AssertionsForClassTypes.assertThat(parent.getClasses()).containsExactly(FooConfig.class);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	/**
	 * @since 5.2
	 */
	@Test
	public void buildMergedConfigForNestedTestClassWithInheritedConfigForContextHierarchy() {
		Class<?> enclosingTestClass = ContextHierarchyOuterTestCase.class;
		Class<?> testClass = ContextHierarchyOuterTestCase.NestedTestCaseWithInheritedConfig.class;
		Class<?>[] expectedClasses = array(BarConfig.class);

		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);
		assertThat(mergedConfig).as("merged config").isNotNull();

		MergedContextConfiguration parent = mergedConfig.getParent();
		assertThat(parent).as("parent config").isNotNull();
		// The following does not work -- at least not in Eclipse.
		// asssertThat(parent.getClasses())...
		// So we use AssertionsForClassTypes directly.
		AssertionsForClassTypes.assertThat(parent.getClasses()).containsExactly(FooConfig.class);

		assertMergedConfig(mergedConfig, enclosingTestClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}

	/**
	 * @since 5.2
	 */
	@Test
	@Disabled("Not yet working")
	public void buildMergedConfigForNestedTestClassWithMergedInheritedConfigForContextHierarchy() {
		Class<?> testClass = ContextHierarchyOuterTestCase.NestedTestCaseWithMergedInheritedConfig.class;
		Class<?>[] expectedClasses = array(BarConfig.class, BazConfig.class);

		MergedContextConfiguration mergedConfig = buildMergedContextConfiguration(testClass);
		assertThat(mergedConfig).as("merged config").isNotNull();

		MergedContextConfiguration parent = mergedConfig.getParent();
		assertThat(parent).as("parent config").isNotNull();
		// The following does not work -- at least not in Eclipse.
		// asssertThat(parent.getClasses())...
		// So we use AssertionsForClassTypes directly.
		AssertionsForClassTypes.assertThat(parent.getClasses()).containsExactly(FooConfig.class);

		assertMergedConfig(mergedConfig, testClass, EMPTY_STRING_ARRAY, expectedClasses,
			AnnotationConfigContextLoader.class);
	}


	@ContextConfiguration
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface SpringAppConfig {

		Class<?>[] classes() default {};
	}

	@SpringAppConfig(classes = { FooConfig.class, BarConfig.class })
	public static abstract class Dog {
	}

	public static abstract class WorkingDog extends Dog {
	}

	public static class GermanShepherd extends WorkingDog {
	}

	@ContextConfiguration
	static class MissingContextAttributesTestCase {
	}

}
