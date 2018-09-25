package site.zido.elise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.zido.elise.downloader.Downloader;
import site.zido.elise.matcher.NumberExpressMatcher;
import site.zido.elise.pipeline.ConsolePipeline;
import site.zido.elise.pipeline.Pipeline;
import site.zido.elise.processor.PageProcessor;
import site.zido.elise.scheduler.TaskScheduler;
import site.zido.elise.utils.UrlUtils;
import site.zido.elise.utils.ValidateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * the main spider
 *
 * @author zido
 */
public class Spider implements RequestPutter {
    private Downloader downloader;
    private List<Pipeline> pipelines = new ArrayList<>();
    private PageProcessor pageProcessor;
    private DefaultSpiderListenProcessor processor = new DefaultSpiderListenProcessor();
    private static Logger logger = LoggerFactory.getLogger(Spider.class);

    private int threadNum = 1;

    private List<SpiderListener> spiderListeners;

    private TaskScheduler manager;

    @Override
    public void pushRequest(Task task, Request request) {
        manager.pushRequest(task, request);
    }

    private void pushRequest(Task task, List<Request> request) {
        for (Request r : request) {
            pushRequest(task, r);
        }
    }

    public Spider(TaskScheduler manager) {
        this.manager = manager;
    }

    class DefaultSpiderListenProcessor implements TaskScheduler.DownloadListener, TaskScheduler.AnalyzerListener {

        @Override
        public void onDownload(Task task, Request request) {
            Site site = task.getSite();
            if (site.getDomain() == null && request != null && request.getUrl() != null) {
                site.setDomain(UrlUtils.getDomain(request.getUrl()));
            }
            Page page = downloader.download(request, task);
            manager.process(task, request, page);
        }

        @Override
        public void onProcess(Task task, Request request, Page page) {
            if (page.isDownloadSuccess()) {
                Site site = task.getSite();
                String codeAccepter = site.getCodeAccepter();
                NumberExpressMatcher matcher = new NumberExpressMatcher(codeAccepter);
                if (matcher.matches(page.getStatusCode())) {
                    List<ResultItem> resultItems = pageProcessor.process(task, page, manager);
                    if (!ValidateUtils.isEmpty(resultItems)) {
                        for (ResultItem resultItem : resultItems) {
                            if (resultItem != null) {
                                resultItem.setRequest(request);
                                for (Pipeline pipeline : pipelines) {
                                    try {
                                        pipeline.process(resultItem, task);
                                    } catch (Throwable e) {
                                        logger.error("pipeline have made a exception", e);
                                    }
                                }
                            } else {
                                logger.info("page not find anything, page {}", request.getUrl());
                            }
                        }
                    }
                    sleep(site.getSleepTime());
                    onSuccess(request);
                    return;
                }
            }
            Site site = task.getSite();
            if (site.getCycleRetryTimes() == 0) {
                sleep(site.getSleepTime());
            } else {
                // for cycle retry
                doCycleRetry(task, request);
            }


        }
    }

    public Spider start() {
        if (downloader != null) {
            manager.registerDownloader(processor);
            downloader.setThread(threadNum);
        }
        if (pageProcessor != null) {
            manager.registerAnalyzer(processor);
        }
        if (pipelines.isEmpty()) {
            pipelines.add(new ConsolePipeline());
        }
        return this;
    }

    public void stop() {
        manager.removeAnalyzer(processor);
        manager.removeDownloader(processor);
    }

    public static SpiderOptionBuilder builder(TaskScheduler scheduler) {
        return new SpiderOptionBuilder(new Spider(scheduler));
    }

    private void onError(Request request) {
        if (!ValidateUtils.isEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onError(request);
            }
        }
    }

    private void onSuccess(Request request) {
        if (!ValidateUtils.isEmpty(spiderListeners)) {
            for (SpiderListener spiderListener : spiderListeners) {
                spiderListener.onSuccess(request);
            }
        }
    }

    private void doCycleRetry(Task task, Request request) {
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        if (cycleTriedTimesObject == null) {
            pushRequest(task, new Request(request).putExtra(Request.CYCLE_TRIED_TIMES, 1));
        } else {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            cycleTriedTimes++;
            if (cycleTriedTimes < task.getSite().getCycleRetryTimes()) {
                pushRequest(task, new Request(request).putExtra(Request.CYCLE_TRIED_TIMES, cycleTriedTimes));
            }
        }
        sleep(task.getSite().getRetrySleepTime());
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted when sleep", e);
        }
    }

    /**
     * Add urls to crawl. <br>
     *
     * @param urls urls
     * @return this
     */
    public Spider addUrl(String... urls) {
        for (String url : urls) {
            pushRequest(new Site().toTask(), new Request(url));
        }
        return this;
    }

    public Spider addUrl(Task task, String... urls) {
        for (String url : urls) {
            pushRequest(task, new Request(url));
        }
        return this;
    }

    public static class SpiderOptionBuilder {
        private Spider spider;

        private SpiderOptionBuilder(Spider spider) {
            this.spider = spider;
        }


        /**
         * add a pipeline for Spider
         *
         * @param pipeline pipeline
         * @return this
         * @see Pipeline
         * @since 0.2.1
         */
        public SpiderOptionBuilder addPipeline(Pipeline pipeline) {
            spider.pipelines.add(pipeline);
            return this;
        }

        /**
         * set pipelines for Spider
         *
         * @param pipelines pipelines
         * @return this
         * @see Pipeline
         * @since 0.4.1
         */
        public SpiderOptionBuilder setPipelines(List<Pipeline> pipelines) {
            spider.pipelines = pipelines;
            return this;
        }

        /**
         * clear the pipelines set
         *
         * @return this
         */
        public SpiderOptionBuilder clearPipeline() {
            spider.pipelines.clear();
            return this;
        }

        /**
         * set the downloader of spider
         *
         * @param downloader downloader
         * @return this
         * @see Downloader
         */
        public SpiderOptionBuilder setDownloader(Downloader downloader) {
            spider.downloader = downloader;
            return this;
        }

        public SpiderOptionBuilder setSpiderListeners(List<SpiderListener> spiderListeners) {
            spider.spiderListeners = spiderListeners;
            return this;
        }

        public SpiderOptionBuilder setPageProcessor(PageProcessor pageProcessor) {
            spider.pageProcessor = pageProcessor;
            return this;
        }

        public Spider build() {
            return spider;
        }
    }

}
