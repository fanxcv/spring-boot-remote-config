package fun.fanx.remote.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置管理接口
 * @author fan
 */
@Slf4j
@RefreshScope
@RestController
@RequiredArgsConstructor
@RequestMapping("/environment/api")
public class RefreshController implements ApplicationListener<ApplicationStartedEvent> {
    private final ContextRefresher contextRefresher;

    @Value("${remote.test}")
    private String test;

    /**
     * 刷新配置接口
     * 限制一分钟只能请求一次
     */
    @GetMapping("refresh")
    public String refresh() {
        contextRefresher.refresh();
        return test;
    }


    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent applicationStartedEvent) {
        System.out.println("加载到的配置:" + test);
    }
}
