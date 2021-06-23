package fun.fanx.remote.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * 远程配置文件加载
 * @author fan
 */
public class RemoteConfigLoadPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationPreparedEvent> {
    /**
     * 用于缓存日志, 并在合适的时候打印
     */
    private static final DeferredLog LOGGER = new DeferredLog();


    /**
     * 先初始化一个yml的解析器
     * 加载properties文件的话自己初始化{@link org.springframework.boot.env.PropertiesPropertySourceLoader}对象即可
     */
    private final PropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 获取一个binder对象, 用来绑定配置文件中的远程地址到对象上
        final Binder binder = Binder.get(environment);
        final String[] configsUrl = binder.bind("xc.config.remote", String[].class).orElse(new String[]{});

        final MutablePropertySources propertySources = environment.getPropertySources();

        final int length = configsUrl.length;
        for (int i = 0; i < length; i++) {
            // 配置文件的name不能一致, 一致的话会被覆盖
            try {
                loadProperties("defaultXcRemoteConfigure" + i, configsUrl[i], propertySources);
            } catch (IOException e) {
                LOGGER.error("load fail, url is: " + configsUrl[i], e);
            }
        }
    }

    private void loadProperties(String name, String url, MutablePropertySources destination) throws IOException {
        Resource resource = new UrlResource(url);
        if (resource.exists()) {
            // 如果资源存在的话,使用PropertySourceLoader加载配置文件
            List<PropertySource<?>> load = loader.load(name, resource);
            // 将对应的资源放在RandomValuePropertySource前面,保证加载的远程资源会优先于系统配置使用
            load.forEach(it -> destination.addBefore(RandomValuePropertySource.RANDOM_PROPERTY_SOURCE_NAME, it));
            LOGGER.info("load configuration success from " + url);
        } else {
            LOGGER.error("get configuration fail from " + url + ", don't load this");
        }
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationPreparedEvent applicationPreparedEvent) {
        // 打印日志
        LOGGER.replayTo(RemoteConfigLoadPostProcessor.class);
    }
}
