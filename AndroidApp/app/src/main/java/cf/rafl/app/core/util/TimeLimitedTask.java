package cf.rafl.app.core.util;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeLimitedTask
{
    private Callable callable;

    public TimeLimitedTask(Callable callable)
    {
        this.callable = callable;
    }

    public Object execute(long timeout, TimeUnit timeUnit) throws TimeoutException, ExecutionException, InterruptedException
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future result = executor.submit(callable);

        return result.get(timeout, timeUnit);
    }
}
