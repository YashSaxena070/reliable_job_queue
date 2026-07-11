package com.app.reliable_job_queue.worker;

import com.app.reliable_job_queue.model.Job;
import com.app.reliable_job_queue.service.QueueService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLOutput;
import java.util.Optional;

@Component
@EnableScheduling
@AllArgsConstructor
public class QueueWorker {
    private final QueueService queueService;

    @Scheduled(fixedDelayString = "${queue.poll-delay}")
    public void pollAndExecute(){
        Optional<Job> leasedJob = queueService.acquireJob("");

        leasedJob.ifPresent(this::processAsync);
    }

    @Async("queueExecutor")
    public void processAsync(Job job){

        System.out.println("[" + Thread.currentThread().getName() +
                "] Processing Job #" + job.getId());

        try{
            executeJobLogic(job);
            queueService.completeJob((job.getId()));
            System.out.println("[Worker Thread] Successfully completed Job #" + job.getId());

        } catch (Exception e){
            System.err.println("[Worker Thread] Failed executing Job #" + job.getId() + ": " + e.getMessage());
            queueService.handleFailure(job.getId(), e.getMessage());
        }
    }

    private void executeJobLogic(Job job) throws  Exception{
           // SIMULATION: If the payload contains the word "fail", explicitly throw an error
        if(job.getPayload().contains("fail")){
            throw new RuntimeException("Simulated external service failure.");
        }
        // Pretend the worker is doing heavy computation/I/O for 500ms
        Thread.sleep(500);
    }
}
