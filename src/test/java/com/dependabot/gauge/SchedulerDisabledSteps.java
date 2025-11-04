package com.dependabot.gauge;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dependabot.DependabotApplication;
import com.dependabot.scheduler.PRRefreshScheduler;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.Step;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContextManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DependabotApplication.class
)
public class SchedulerDisabledSteps {

    @Autowired(required = false)
    private PRRefreshScheduler scheduler;

    private TestContextManager testContextManager;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger schedulerLogger;

    @BeforeScenario
    public void setUp() throws Exception {
        testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);

        schedulerLogger = (Logger) LoggerFactory.getLogger(PRRefreshScheduler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        schedulerLogger.addAppender(listAppender);

        listAppender.list.clear();
    }

    @AfterScenario
    public void tearDown() {
        if (schedulerLogger != null && listAppender != null) {
            schedulerLogger.detachAppender(listAppender);
        }
    }

    @Step("Scheduler should not be enabled by default")
    public void verifySchedulerDisabledByDefault() {
        assertThat(scheduler)
                .as("Scheduler bean should not exist when disabled")
                .isNull();
    }

    @Step("No scheduled tasks should run when disabled")
    public void noScheduledTasksWhenDisabled() throws InterruptedException {
        // Since scheduler bean is null (verified in previous step),
        // there's nothing more to check - no scheduler means no execution
        // Just verify the bean is still null
        assertThat(scheduler)
                .as("Scheduler should remain null throughout the test")
                .isNull();
    }
}