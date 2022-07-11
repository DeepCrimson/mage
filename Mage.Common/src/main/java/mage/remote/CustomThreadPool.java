package mage.remote;

import org.jboss.util.threadpool.BasicThreadPool;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadPoolExecutor;

public class CustomThreadPool extends BasicThreadPool {

    @Override
    public void setMaximumPoolSize(int size) {
        /*
         * I really don't want to implement a whole new threadpool
         * just to fix this and the executor is private
         */
        try {
            Field executorField = BasicThreadPool.class.getField("executor");
            executorField.setAccessible(true);
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorField.get(this);
            synchronized (executor) {
                executor.setMaximumPoolSize(size);
                executor.setCorePoolSize(size);
            }
        } catch (NoSuchFieldException | SecurityException e) {
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
    }
}
