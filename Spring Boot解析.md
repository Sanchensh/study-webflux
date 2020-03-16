# Spring Boot解析

##### 1、@SpringBootApplication注解详解

​		@SpringBootApplication注解是spring boot的核心注解，该注解组合了@Configuration、@EnableAutoConfiguration、@ComponentScan这几个注解，详细加如下代码：

```Java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
//@SpringBootConfiguration实质就是@Configuration
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
    @Filter(type = FilterType.CUSTOM,classes = {TypeExcludeFilter.class}), 
    @Filter(type = FilterType.CUSTOM,classes = {AutoConfigurationExcludeFilter.class})})
public @interface SpringBootApplication {
    @AliasFor(annotation = EnableAutoConfiguration.class)
    Class<?>[] exclude() default {};

    @AliasFor(annotation = EnableAutoConfiguration.class)
    String[] excludeName() default {};

    @AliasFor(annotation = ComponentScan.class,attribute = "basePackages")
    String[] scanBasePackages() default {};

    @AliasFor(annotation = ComponentScan.class,attribute = "basePackageClasses")
    Class<?>[] scanBasePackageClasses() default {};
}
```

当不想用@SpringBootApplication注解的时候，其实也可以用这三个注解代替。其中：

1. @EnableAutoConfiguration注解让spring boot根据类路径中的jar包依赖为当前项目做一个自动配置。
2. @ComponentScan注解默认扫描当前package下的所有加了@Component 、@Repository、@Service、@Controller的类到 IoC 容器中。
3. @Configuration意味着它也是一个ioc容器的一个配置类。

##### 2、SpringApplication.run(Application.class,args)方法详解

查看SpringApplication.run(AppConfig.class,args)方法的源码如下：

```java
public ConfigurableApplicationContext run(String... args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ConfigurableApplicationContext context = null;
        Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList();
        this.configureHeadlessProperty();
        SpringApplicationRunListeners listeners = this.getRunListeners(args);
        listeners.starting();

        Collection exceptionReporters;
        try {
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            ConfigurableEnvironment environment = this.prepareEnvironment(listeners, applicationArguments);
            this.configureIgnoreBeanInfo(environment);
            Banner printedBanner = this.printBanner(environment);
            context = this.createApplicationContext();
            exceptionReporters = this.getSpringFactoriesInstances(SpringBootExceptionReporter.class, new Class[]{ConfigurableApplicationContext.class}, context);
            this.prepareContext(context, environment, listeners, applicationArguments, printedBanner);
            //内部会解析我们的配置类上的标签.实现自动装配功能的注解@EnableAutoConfiguration
            this.refreshContext(context);
            this.afterRefresh(context, applicationArguments);
            stopWatch.stop();
            if (this.logStartupInfo) {
                (new StartupInfoLogger(this.mainApplicationClass)).logStarted(this.getApplicationLog(), stopWatch);
            }

            listeners.started(context);
            this.callRunners(context, applicationArguments);
        } catch (Throwable var10) {
            this.handleRunFailure(context, var10, exceptionReporters, listeners);
            throw new IllegalStateException(var10);
        }

        try {
            listeners.running(context);
            return context;
        } catch (Throwable var9) {
            this.handleRunFailure(context, var9, exceptionReporters, (SpringApplicationRunListeners)null);
            throw new IllegalStateException(var9);
        }
    }
```

@EnableAutoConfiguration注解的源码：

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
//会导入AutoConfigurationImportSelector
@Import({AutoConfigurationImportSelector.class})
public @interface EnableAutoConfiguration {
    String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

    Class<?>[] exclude() default {};

    String[] excludeName() default {};
}
```

再看AutoConfigurationImportSelector类的源码：

```java
public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {
    private static final AutoConfigurationImportSelector.AutoConfigurationEntry EMPTY_ENTRY = new AutoConfigurationImportSelector.AutoConfigurationEntry();
    private static final String[] NO_IMPORTS = new String[0];
    private static final Log logger = LogFactory.getLog(AutoConfigurationImportSelector.class);
    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";
    private ConfigurableListableBeanFactory beanFactory;
    private Environment environment;
    private ClassLoader beanClassLoader;
    private ResourceLoader resourceLoader;

    public AutoConfigurationImportSelector() {
    }

    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        if (!this.isEnabled(annotationMetadata)) {
            return NO_IMPORTS;
        } else {
            AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
            AutoConfigurationImportSelector.AutoConfigurationEntry autoConfigurationEntry = this.getAutoConfigurationEntry(autoConfigurationMetadata, annotationMetadata);
            return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
        }
    }

    protected AutoConfigurationImportSelector.AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata) {
        if (!this.isEnabled(annotationMetadata)) {
            return EMPTY_ENTRY;
        } else {
            AnnotationAttributes attributes = this.getAttributes(annotationMetadata);
            List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
            configurations = this.removeDuplicates(configurations);
            Set<String> exclusions = this.getExclusions(annotationMetadata, attributes);
            this.checkExcludedClasses(configurations, exclusions);
            configurations.removeAll(exclusions);
            configurations = this.filter(configurations, autoConfigurationMetadata);
            this.fireAutoConfigurationImportEvents(configurations, exclusions);
            return new AutoConfigurationImportSelector.AutoConfigurationEntry(configurations, exclusions);
        }
    }

    public Class<? extends Group> getImportGroup() {
        return AutoConfigurationImportSelector.AutoConfigurationGroup.class;
    }

    protected boolean isEnabled(AnnotationMetadata metadata) {
        return this.getClass() == AutoConfigurationImportSelector.class ? (Boolean)this.getEnvironment().getProperty("spring.boot.enableautoconfiguration", Boolean.class, true) : true;
    }

    protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        String name = this.getAnnotationClass().getName();
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(name, true));
        Assert.notNull(attributes, () -> {
            return "No auto-configuration attributes found. Is " + metadata.getClassName() + " annotated with " + ClassUtils.getShortName(name) + "?";
        });
        return attributes;
    }

    protected Class<?> getAnnotationClass() {
        return EnableAutoConfiguration.class;
    }

    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        List<String> configurations = SpringFactoriesLoader.loadFactoryNames(this.getSpringFactoriesLoaderFactoryClass(), this.getBeanClassLoader());
        Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you are using a custom packaging, make sure that file is correct.");
        return configurations;
    }

    protected Class<?> getSpringFactoriesLoaderFactoryClass() {
        return EnableAutoConfiguration.class;
    }

    private void checkExcludedClasses(List<String> configurations, Set<String> exclusions) {
        List<String> invalidExcludes = new ArrayList(exclusions.size());
        Iterator var4 = exclusions.iterator();

        while(var4.hasNext()) {
            String exclusion = (String)var4.next();
            if (ClassUtils.isPresent(exclusion, this.getClass().getClassLoader()) && !configurations.contains(exclusion)) {
                invalidExcludes.add(exclusion);
            }
        }

        if (!invalidExcludes.isEmpty()) {
            this.handleInvalidExcludes(invalidExcludes);
        }

    }

    protected void handleInvalidExcludes(List<String> invalidExcludes) {
        StringBuilder message = new StringBuilder();
        Iterator var3 = invalidExcludes.iterator();

        while(var3.hasNext()) {
            String exclude = (String)var3.next();
            message.append("\t- ").append(exclude).append(String.format("%n"));
        }

        throw new IllegalStateException(String.format("The following classes could not be excluded because they are not auto-configuration classes:%n%s", message));
    }

    protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        Set<String> excluded = new LinkedHashSet();
        excluded.addAll(this.asList(attributes, "exclude"));
        excluded.addAll(Arrays.asList(attributes.getStringArray("excludeName")));
        excluded.addAll(this.getExcludeAutoConfigurationsProperty());
        return excluded;
    }

    private List<String> getExcludeAutoConfigurationsProperty() {
        if (this.getEnvironment() instanceof ConfigurableEnvironment) {
            Binder binder = Binder.get(this.getEnvironment());
            return (List)binder.bind("spring.autoconfigure.exclude", String[].class).map(Arrays::asList).orElse(Collections.emptyList());
        } else {
            String[] excludes = (String[])this.getEnvironment().getProperty("spring.autoconfigure.exclude", String[].class);
            return excludes != null ? Arrays.asList(excludes) : Collections.emptyList();
        }
    }

    private List<String> filter(List<String> configurations, AutoConfigurationMetadata autoConfigurationMetadata) {
        long startTime = System.nanoTime();
        String[] candidates = StringUtils.toStringArray(configurations);
        boolean[] skip = new boolean[candidates.length];
        boolean skipped = false;
        Iterator var8 = this.getAutoConfigurationImportFilters().iterator();

        while(var8.hasNext()) {
            AutoConfigurationImportFilter filter = (AutoConfigurationImportFilter)var8.next();
            this.invokeAwareMethods(filter);
            boolean[] match = filter.match(candidates, autoConfigurationMetadata);

            for(int i = 0; i < match.length; ++i) {
                if (!match[i]) {
                    skip[i] = true;
                    candidates[i] = null;
                    skipped = true;
                }
            }
        }

        if (!skipped) {
            return configurations;
        } else {
            List<String> result = new ArrayList(candidates.length);

            int numberFiltered;
            for(numberFiltered = 0; numberFiltered < candidates.length; ++numberFiltered) {
                if (!skip[numberFiltered]) {
                    result.add(candidates[numberFiltered]);
                }
            }

            if (logger.isTraceEnabled()) {
                numberFiltered = configurations.size() - result.size();
                logger.trace("Filtered " + numberFiltered + " auto configuration class in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
            }

            return new ArrayList(result);
        }
    }

    protected List<AutoConfigurationImportFilter> getAutoConfigurationImportFilters() {
        return SpringFactoriesLoader.loadFactories(AutoConfigurationImportFilter.class, this.beanClassLoader);
    }

    protected final <T> List<T> removeDuplicates(List<T> list) {
        return new ArrayList(new LinkedHashSet(list));
    }

    protected final List<String> asList(AnnotationAttributes attributes, String name) {
        String[] value = attributes.getStringArray(name);
        return Arrays.asList(value != null ? value : new String[0]);
    }

    private void fireAutoConfigurationImportEvents(List<String> configurations, Set<String> exclusions) {
        List<AutoConfigurationImportListener> listeners = this.getAutoConfigurationImportListeners();
        if (!listeners.isEmpty()) {
            AutoConfigurationImportEvent event = new AutoConfigurationImportEvent(this, configurations, exclusions);
            Iterator var5 = listeners.iterator();

            while(var5.hasNext()) {
                AutoConfigurationImportListener listener = (AutoConfigurationImportListener)var5.next();
                this.invokeAwareMethods(listener);
                listener.onAutoConfigurationImportEvent(event);
            }
        }

    }

    protected List<AutoConfigurationImportListener> getAutoConfigurationImportListeners() {
        return SpringFactoriesLoader.loadFactories(AutoConfigurationImportListener.class, this.beanClassLoader);
    }

    private void invokeAwareMethods(Object instance) {
        if (instance instanceof Aware) {
            if (instance instanceof BeanClassLoaderAware) {
                ((BeanClassLoaderAware)instance).setBeanClassLoader(this.beanClassLoader);
            }

            if (instance instanceof BeanFactoryAware) {
                ((BeanFactoryAware)instance).setBeanFactory(this.beanFactory);
            }

            if (instance instanceof EnvironmentAware) {
                ((EnvironmentAware)instance).setEnvironment(this.environment);
            }

            if (instance instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware)instance).setResourceLoader(this.resourceLoader);
            }
        }

    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
        this.beanFactory = (ConfigurableListableBeanFactory)beanFactory;
    }

    protected final ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    protected ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected final Environment getEnvironment() {
        return this.environment;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected final ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    public int getOrder() {
        return 2147483646;
    }

    protected static class AutoConfigurationEntry {
        private final List<String> configurations;
        private final Set<String> exclusions;

        private AutoConfigurationEntry() {
            this.configurations = Collections.emptyList();
            this.exclusions = Collections.emptySet();
        }

        AutoConfigurationEntry(Collection<String> configurations, Collection<String> exclusions) {
            this.configurations = new ArrayList(configurations);
            this.exclusions = new HashSet(exclusions);
        }

        public List<String> getConfigurations() {
            return this.configurations;
        }

        public Set<String> getExclusions() {
            return this.exclusions;
        }
    }

    private static class AutoConfigurationGroup implements Group, BeanClassLoaderAware, BeanFactoryAware, ResourceLoaderAware {
        private final Map<String, AnnotationMetadata> entries = new LinkedHashMap();
        private final List<AutoConfigurationImportSelector.AutoConfigurationEntry> autoConfigurationEntries = new ArrayList();
        private ClassLoader beanClassLoader;
        private BeanFactory beanFactory;
        private ResourceLoader resourceLoader;
        private AutoConfigurationMetadata autoConfigurationMetadata;

        private AutoConfigurationGroup() {
        }

        public void setBeanClassLoader(ClassLoader classLoader) {
            this.beanClassLoader = classLoader;
        }

        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
            Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector, () -> {
                return String.format("Only %s implementations are supported, got %s", AutoConfigurationImportSelector.class.getSimpleName(), deferredImportSelector.getClass().getName());
            });
            AutoConfigurationImportSelector.AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector)deferredImportSelector).getAutoConfigurationEntry(this.getAutoConfigurationMetadata(), annotationMetadata);
            this.autoConfigurationEntries.add(autoConfigurationEntry);
            Iterator var4 = autoConfigurationEntry.getConfigurations().iterator();

            while(var4.hasNext()) {
                String importClassName = (String)var4.next();
                this.entries.putIfAbsent(importClassName, annotationMetadata);
            }

        }

        public Iterable<Entry> selectImports() {
            if (this.autoConfigurationEntries.isEmpty()) {
                return Collections.emptyList();
            } else {
                Set<String> allExclusions = (Set)this.autoConfigurationEntries.stream().map(AutoConfigurationImportSelector.AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());
                Set<String> processedConfigurations = (Set)this.autoConfigurationEntries.stream().map(AutoConfigurationImportSelector.AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
                processedConfigurations.removeAll(allExclusions);
                return (Iterable)this.sortAutoConfigurations(processedConfigurations, this.getAutoConfigurationMetadata()).stream().map((importClassName) -> {
                    return new Entry((AnnotationMetadata)this.entries.get(importClassName), importClassName);
                }).collect(Collectors.toList());
            }
        }

        private AutoConfigurationMetadata getAutoConfigurationMetadata() {
            if (this.autoConfigurationMetadata == null) {
                this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
            }

            return this.autoConfigurationMetadata;
        }

        private List<String> sortAutoConfigurations(Set<String> configurations, AutoConfigurationMetadata autoConfigurationMetadata) {
            return (new AutoConfigurationSorter(this.getMetadataReaderFactory(), autoConfigurationMetadata)).getInPriorityOrder(configurations);
        }

        private MetadataReaderFactory getMetadataReaderFactory() {
            try {
                return (MetadataReaderFactory)this.beanFactory.getBean("org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory", MetadataReaderFactory.class);
            } catch (NoSuchBeanDefinitionException var2) {
                return new CachingMetadataReaderFactory(this.resourceLoader);
            }
        }
    }
}

```

