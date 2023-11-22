package nz.govt.natlib.dashboard.domain.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatchdog {
    private static final Logger log = LoggerFactory.getLogger(DirectoryWatchdog.class);
    private Path rootPath;
    private List<Path> preparedSubFolders = new ArrayList<>();
    private Thread watchDog;

    public static DirectoryWatchdog getInstance(String rootPath) throws IOException, InterruptedException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        DirectoryWatchdog instance = new DirectoryWatchdog();
        instance.rootPath = new File(rootPath).toPath();

        instance.rootPath.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);

        Runnable dog = new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        WatchKey key = watcher.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == OVERFLOW) {
                                continue;
                            }

//                            WatchEvent<?> ev = (WatchEvent<?>)event;
                            Path path = (Path) event.context();

                            instance.preparedSubFolders.add(path);

                            System.out.println(path.getFileName());
                        }

                    } catch (InterruptedException e) {
                        log.error("Failed to wait for new files or directories", e);
                        return;
                    }
                }
            }
        };
        instance.watchDog = new Thread(dog);
        instance.watchDog.start();

        return instance;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DirectoryWatchdog dog = DirectoryWatchdog.getInstance("C:\\Users\\wa-leefra\\workspace\\temp");
        dog.watchDog.join();
    }
}
